package com.celements.search.web;

import static com.celements.search.lucene.LuceneSearchUtil.*;
import static com.celements.search.web.classes.IWebSearchClassConfig.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.context.ModelContext;
import com.celements.model.util.ModelUtils;
import com.celements.pagetype.IPageTypeClassConfig;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.query.IQueryRestriction;
import com.celements.search.lucene.query.LuceneDocType;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;
import com.celements.search.web.classes.IWebSearchClassConfig;
import com.celements.search.web.module.WebSearchModule;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.plugin.lucene.IndexFields;

@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class DefaultWebSearchQueryBuilder implements WebSearchQueryBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWebSearchQueryBuilder.class);

  @Requirement
  private ILuceneSearchService searchService;

  @Requirement
  private List<WebSearchModule> modules;

  @Requirement
  private IWebSearchClassConfig classConf;

  @Requirement
  private IPageTypeClassConfig ptClassConf;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private ModelUtils modelUtils;

  private XWikiDocument configDoc;
  private String searchTerm = "";
  private List<WebSearchModule> activatedModules = new ArrayList<>();

  @Override
  public DocumentReference getConfigDocRef() {
    return getConfigDoc().getDocumentReference();
  }

  private XWikiDocument getConfigDoc() {
    Preconditions.checkState(configDoc != null, "no config doc defined");
    return configDoc;
  }

  @Override
  public WebSearchQueryBuilder setConfigDoc(DocumentReference docRef)
      throws DocumentNotExistsException {
    Preconditions.checkState(configDoc == null, "config doc already defined");
    configDoc = modelAccess.getDocument(docRef);
    Preconditions.checkState(getConfigObj() != null, "invalid config doc");
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
  public Collection<WebSearchModule> getModules() {
    Set<WebSearchModule> ret = new LinkedHashSet<>(activatedModules);
    Set<String> moduleNames = ImmutableSet.copyOf(getConfigObj().getStringValue(
        PROPERTY_PACKAGES).split("[,;\\| ]+"));
    for (WebSearchModule module : modules) {
      if (moduleNames.contains(module.getName()) || module.isRequired(getConfigDoc())) {
        ret.add(module);
      }
    }
    if (ret.isEmpty()) {
      for (WebSearchModule module : modules) {
        if (module.isDefault()) {
          ret.add(module);
        }
      }
    }
    Preconditions.checkState(!ret.isEmpty(), "no WebSearchModules defined");
    return ret;
  }

  @Override
  public WebSearchQueryBuilder addModule(WebSearchModule module) {
    activatedModules.add(module);
    return this;
  }

  @Override
  public LuceneQuery build() {
    LuceneQuery query = new LuceneQuery(Collections.<LuceneDocType>emptyList());
    query.add(getRestrExcludeWebPref());
    query.add(getRestrSpaces(false));
    query.add(getRestrSpaces(true));
    query.add(getRestrDocs(false));
    query.add(getRestrDocs(true));
    query.add(getRestrPageTypes(false));
    query.add(getRestrPageTypes(true));
    Collection<WebSearchModule> modules = getModules();
    query.add(getRestrModules(modules));
    query.add(getRestrLinkedDocsOnly(modules));
    LOGGER.info("build: for '{}' returning '{}'", this, query);
    return query;
  }

  private IQueryRestriction getRestrExcludeWebPref() {
    return searchService.createRestriction(IndexFields.DOCUMENT_NAME,
        ModelContext.WEB_PREF_DOC_NAME).setNegate(true);
  }

  private IQueryRestriction getRestrSpaces(boolean isBlacklist) {
    String fieldName = isBlacklist ? PROPERTY_SPACES_BLACK_LIST : PROPERTY_SPACES;
    return buildRestrictionFromField(fieldName, new Function<String, IQueryRestriction>() {

      @Override
      public IQueryRestriction apply(String str) {
        return searchService.createSpaceRestriction(modelUtils.resolveRef(str,
            SpaceReference.class));
      }
    }).setNegate(isBlacklist);
  }

  private IQueryRestriction getRestrDocs(boolean isBlacklist) {
    String fieldName = isBlacklist ? PROPERTY_DOCS_BLACK_LIST : PROPERTY_DOCS;
    return buildRestrictionFromField(fieldName, new Function<String, IQueryRestriction>() {

      @Override
      public IQueryRestriction apply(String str) {
        return searchService.createDocRestriction(modelUtils.resolveRef(str,
            DocumentReference.class));
      }
    }).setNegate(isBlacklist);
  }

  private IQueryRestriction getRestrPageTypes(boolean isBlacklist) {
    String fieldName = isBlacklist ? PROPERTY_PAGETYPES_BLACK_LIST : PROPERTY_PAGETYPES;
    return buildRestrictionFromField(fieldName, new Function<String, IQueryRestriction>() {

      @Override
      public IQueryRestriction apply(String str) {
        return searchService.createFieldRestriction(ptClassConf.getPageTypeClassRef(),
            IPageTypeClassConfig.PAGE_TYPE_FIELD, exactify(str));
      }
    }).setNegate(isBlacklist);
  }

  private IQueryRestriction buildRestrictionFromField(String fieldName,
      Function<String, IQueryRestriction> getRestrFunc) {
    return buildRestrictionGroup(getConfigObj().getStringValue(fieldName), Type.OR, getRestrFunc);
  }

  private IQueryRestriction getRestrModules(Collection<WebSearchModule> modules) {
    QueryRestrictionGroup grp = searchService.createRestrictionGroup(Type.OR);
    for (WebSearchModule module : modules) {
      QueryRestrictionGroup moduleGrp = searchService.createRestrictionGroup(Type.AND);
      moduleGrp.add(searchService.createDocTypeRestriction(module.getDocTypes()));
      moduleGrp.add(module.getQueryRestriction(getConfigDoc(), getSearchTerm()));
      grp.add(moduleGrp);
    }
    String fuzzy = getConfigObj().getStringValue(PROPERTY_FUZZY_SEARCH);
    if (!fuzzy.isEmpty()) {
      try {
        grp.setFuzzy(Float.parseFloat(fuzzy));
      } catch (NumberFormatException nfe) {
        LOGGER.warn("Failed parsing configured float of '{}'", fuzzy, nfe);
      }
    }
    return grp;
  }

  private IQueryRestriction getRestrLinkedDocsOnly(Collection<WebSearchModule> modules) {
    QueryRestrictionGroup grp = searchService.createRestrictionGroup(Type.OR);
    if (getConfigObj().getIntValue(PROPERTY_LINKED_DOCS_ONLY, 0) == 1) {
      for (WebSearchModule module : modules) {
        Optional<DocumentReference> classRef = module.getLinkedClassRef();
        if (classRef.isPresent()) {
          grp.add(searchService.createObjectRestriction(classRef.get()));
        }
      }
    }
    return grp;
  }

  private BaseObject getConfigObj() {
    return modelAccess.getXObject(getConfigDoc(), classConf.getWebSearchConfigClassRef());
  }

  @Override
  public String toString() {
    return new StringBuilder().append("DefaultWebSearchQueryBuilder [configDoc=").append(
        getConfigDocRef()).append(", searchTerm=").append(getSearchTerm()).append("]").toString();
  }

}
