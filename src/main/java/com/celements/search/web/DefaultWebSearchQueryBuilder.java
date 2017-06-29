package com.celements.search.web;

import static com.celements.search.lucene.LuceneUtils.*;
import static com.celements.search.web.classes.WebSearchConfigClass.*;
import static com.google.common.base.MoreObjects.*;
import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.pagetype.IPageTypeClassConfig;
import com.celements.pagetype.PageTypeReference;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.query.IQueryRestriction;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;
import com.celements.search.web.classes.WebSearchConfigClass;
import com.celements.search.web.packages.WebSearchPackage;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.lucene.IndexFields;
import com.xpn.xwiki.web.Utils;

@NotThreadSafe
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class DefaultWebSearchQueryBuilder implements WebSearchQueryBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWebSearchQueryBuilder.class);

  @Requirement
  private ILuceneSearchService searchService;

  @Requirement
  private List<WebSearchPackage> webSearchPackages;

  @Requirement(WebSearchConfigClass.CLASS_DEF_HINT)
  private ClassDefinition webSearchConfigClass;

  @Requirement
  private IPageTypeClassConfig ptClassConf;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private ModelUtils modelUtils;

  @Requirement
  private ModelContext context;

  private WikiReference wikiRef;
  private XWikiDocument configDoc;
  private String searchTerm = "";
  private List<WebSearchPackage> activatedPackages = new ArrayList<>();

  @Override
  public WikiReference getWikiRef() {
    return firstNonNull(wikiRef, context.getWikiRef());
  }

  @Override
  public WebSearchQueryBuilder setWikiRef(WikiReference wikiRef) {
    this.wikiRef = wikiRef;
    return this;
  }

  @Override
  public DocumentReference getConfigDocRef() {
    return getConfigDoc().getDocumentReference();
  }

  private XWikiDocument getConfigDoc() {
    checkState(configDoc != null, "no config doc defined");
    return configDoc;
  }

  @Override
  public WebSearchQueryBuilder setConfigDoc(XWikiDocument doc) {
    checkState(configDoc == null, "config doc already defined");
    configDoc = doc;
    checkState(modelAccess.getXObject(configDoc, webSearchConfigClass.getClassRef()) != null,
        "invalid config doc");
    return this;
  }

  @Override
  public String getSearchTerm() {
    return searchTerm;
  }

  @Override
  public DefaultWebSearchQueryBuilder setSearchTerm(String searchTerm) {
    this.searchTerm = Strings.nullToEmpty(searchTerm).trim();
    return this;
  }

  @Override
  public Collection<WebSearchPackage> getPackages() {
    Set<WebSearchPackage> ret = new LinkedHashSet<>();
    ret.addAll(activatedPackages);
    ret.addAll(getRequiredPackages());
    ret.addAll(getConfiguredPackages());
    if (ret.isEmpty()) {
      ret.addAll(getDefaultPackages());
    }
    checkState(!ret.isEmpty(), "no WebSearchPackages defined");
    return ret;
  }

  private List<WebSearchPackage> getRequiredPackages() {
    return FluentIterable.from(webSearchPackages).filter(new Predicate<WebSearchPackage>() {

      @Override
      public boolean apply(WebSearchPackage searchPackage) {
        return searchPackage.isRequired(getConfigDoc());
      }
    }).toList();
  }

  private List<WebSearchPackage> getDefaultPackages() {
    return FluentIterable.from(webSearchPackages).filter(
        WebSearchPackage.PREDICATE_DEFAULT).toList();
  }

  private List<WebSearchPackage> getConfiguredPackages() {
    return modelAccess.getFieldValue(getConfigDoc(), WebSearchConfigClass.FIELD_PACKAGES).orNull();
  }

  @Override
  public WebSearchQueryBuilder addPackage(WebSearchPackage searchPackage) {
    activatedPackages.add(searchPackage);
    return this;
  }

  @Override
  public WebSearchQueryBuilder addPackage(String packageName) {
    try {
      addPackage(Utils.getComponentManager().lookup(WebSearchPackage.class, packageName));
    } catch (ComponentLookupException exc) {
      LOGGER.info("addPackage: invalid package '{}'", packageName);
    }
    return this;
  }

  @Override
  public LuceneQuery build() {
    LuceneQuery query = new LuceneQuery();
    query.setWiki(getWikiRef());
    query.add(getRestrExcludeWebPref());
    query.add(getRestrSpaces(false));
    query.add(getRestrSpaces(true));
    query.add(getRestrDocs(false));
    query.add(getRestrDocs(true));
    query.add(getRestrPageTypes(false));
    query.add(getRestrPageTypes(true));
    Collection<WebSearchPackage> searchPackages = getPackages();
    query.add(getRestrPackages(searchPackages));
    query.add(getRestrLinkedDocsOnly(searchPackages));
    LOGGER.info("build: for '{}' returning '{}'", this, query);
    return query;
  }

  private IQueryRestriction getRestrExcludeWebPref() {
    return searchService.createRestriction(IndexFields.DOCUMENT_NAME,
        ModelContext.WEB_PREF_DOC_NAME).setNegate(true);
  }

  private IQueryRestriction getRestrSpaces(boolean isBlacklist) {
    ClassField<List<SpaceReference>> field = isBlacklist ? FIELD_SPACES_BLACK_LIST : FIELD_SPACES;
    return buildRestrictionFromField(field, new Function<SpaceReference, IQueryRestriction>() {

      @Override
      public IQueryRestriction apply(SpaceReference spaceRef) {
        return searchService.createSpaceRestriction(spaceRef);
      }
    }).setNegate(isBlacklist);
  }

  private IQueryRestriction getRestrDocs(boolean isBlacklist) {
    ClassField<List<DocumentReference>> field = isBlacklist ? FIELD_DOCS_BLACK_LIST : FIELD_DOCS;
    return buildRestrictionFromField(field, new Function<DocumentReference, IQueryRestriction>() {

      @Override
      public IQueryRestriction apply(DocumentReference docRef) {
        return searchService.createDocRestriction(docRef);
      }
    }).setNegate(isBlacklist);
  }

  private IQueryRestriction getRestrPageTypes(boolean isBlacklist) {
    ClassField<List<PageTypeReference>> field = isBlacklist ? FIELD_PAGETYPES_BLACK_LIST
        : FIELD_PAGETYPES;
    return buildRestrictionFromField(field, new Function<PageTypeReference, IQueryRestriction>() {

      @Override
      public IQueryRestriction apply(PageTypeReference pageTypeRef) {
        return searchService.createFieldRestriction(ptClassConf.getPageTypeClassRef(),
            IPageTypeClassConfig.PAGE_TYPE_FIELD, exactify(pageTypeRef.getConfigName()));
      }
    }).setNegate(isBlacklist);
  }

  private <T> IQueryRestriction buildRestrictionFromField(ClassField<List<T>> field,
      Function<T, IQueryRestriction> restrictionFunc) {
    return buildRestrictionGroup(Type.OR, modelAccess.getFieldValue(getConfigDoc(), field).orNull(),
        restrictionFunc);
  }

  private IQueryRestriction getRestrPackages(Collection<WebSearchPackage> searchPackages) {
    QueryRestrictionGroup grp = searchService.createRestrictionGroup(Type.OR);
    for (WebSearchPackage searchPackage : searchPackages) {
      QueryRestrictionGroup searchPackageGrp = searchService.createRestrictionGroup(Type.AND);
      searchPackageGrp.add(searchService.createDocTypeRestriction(searchPackage.getDocTypes()));
      searchPackageGrp.add(searchPackage.getQueryRestriction(getConfigDoc(), getSearchTerm()));
      grp.add(searchPackageGrp);
    }
    Optional<Float> fuzzy = modelAccess.getFieldValue(getConfigDoc(), FIELD_FUZZY_SEARCH);
    if (fuzzy.isPresent()) {
      grp.setFuzzy(fuzzy.get());
    }
    return grp;
  }

  private IQueryRestriction getRestrLinkedDocsOnly(Collection<WebSearchPackage> searchPackages) {
    QueryRestrictionGroup grp = searchService.createRestrictionGroup(Type.OR);
    if (modelAccess.getFieldValue(getConfigDoc(), FIELD_LINKED_DOCS_ONLY).or(false)) {
      for (WebSearchPackage searchPackage : searchPackages) {
        if (searchPackage.getLinkedClassRef().isPresent()) {
          grp.add(searchService.createObjectRestriction(searchPackage.getLinkedClassRef().get()));
        }
      }
    }
    return grp;
  }

  @Override
  public String toString() {
    return new StringBuilder().append("DefaultWebSearchQueryBuilder [configDoc=").append(
        getConfigDocRef()).append(", searchTerm=").append(getSearchTerm()).append("]").toString();
  }

}
