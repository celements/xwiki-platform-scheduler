package com.celements.search.web;

import static com.celements.search.web.classes.IWebSearchClassConfig.*;

import java.util.ArrayList;
import java.util.Collection;
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
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;
import com.celements.search.web.classes.IWebSearchClassConfig;
import com.celements.search.web.module.WebSearchModule;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
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
  private List<WebSearchModule> availableModules;

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
  private List<WebSearchModule> modules = new ArrayList<>();

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
    Set<WebSearchModule> ret = new LinkedHashSet<>(modules);
    boolean addDefault = ret.isEmpty();
    for (WebSearchModule module : availableModules) {
      if ((addDefault && module.isDefault()) || module.isRequired(getConfigDoc())) {
        ret.add(module);
      }
    }
    return ret;
  }

  @Override
  public WebSearchQueryBuilder addModule(WebSearchModule module) {
    modules.add(module);
    return this;
  }

  @Override
  public LuceneQuery build() {
    LuceneQuery query = searchService.createQuery(getTypes());
    query.add(getRestrExcludeWebPref());
    query.add(getRestrSpaces(PROPERTY_SPACES));
    query.add(getRestrSpaces(PROPERTY_SPACES_BLACK_LIST).setNegate(true));
    query.add(getRestrDocs(PROPERTY_DOCS));
    query.add(getRestrDocs(PROPERTY_DOCS_BLACK_LIST).setNegate(true));
    query.add(getRestrPageTypes(PROPERTY_PAGETYPES));
    query.add(getRestrPageTypes(PROPERTY_PAGETYPES_BLACK_LIST).setNegate(true));
    query.add(getRestrModules());
    LOGGER.info("build: for '{}' returning '{}'", this, query);
    return query;
  }

  private List<String> getTypes() {
    // TODO fix velo bug with types
    return null;
  }

  private IQueryRestriction getRestrExcludeWebPref() {
    return searchService.createRestriction(IndexFields.DOCUMENT_NAME,
        ModelContext.WEB_PREF_DOC_NAME).setNegate(true);
  }

  private IQueryRestriction getRestrSpaces(String fieldName) {
    return getRestrictionFromField(fieldName, new Function<String, IQueryRestriction>() {

      @Override
      public IQueryRestriction apply(String str) {
        return searchService.createSpaceRestriction(modelUtils.resolveRef(str,
            SpaceReference.class));
      }
    });
  }

  private IQueryRestriction getRestrDocs(String fieldName) {
    return getRestrictionFromField(fieldName, new Function<String, IQueryRestriction>() {

      @Override
      public IQueryRestriction apply(String str) {
        return searchService.createDocRestriction(modelUtils.resolveRef(str,
            DocumentReference.class));
      }
    });
  }

  private IQueryRestriction getRestrPageTypes(String fieldName) {
    return getRestrictionFromField(fieldName, new Function<String, IQueryRestriction>() {

      @Override
      public IQueryRestriction apply(String str) {
        return searchService.createFieldRestriction(ptClassConf.getPageTypeClassRef(),
            IPageTypeClassConfig.PAGE_TYPE_FIELD, "\"" + str + "\"");
      }
    });
  }

  private IQueryRestriction getRestrictionFromField(String fieldName,
      Function<String, IQueryRestriction> getRestrFunc) {
    QueryRestrictionGroup grp = searchService.createRestrictionGroup(Type.OR);
    for (String str : getConfigObj().getStringValue(fieldName).split("[,;\\| ]+")) {
      str = str.trim();
      if (!str.isEmpty()) {
        try {
          grp.add(getRestrFunc.apply(str));
        } catch (IllegalArgumentException iae) {
          LOGGER.warn("invalid configuration '{}'", str);
        }
      }
    }
    return grp;
  }

  private IQueryRestriction getRestrModules() {
    QueryRestrictionGroup grp = searchService.createRestrictionGroup(Type.OR);
    for (WebSearchModule module : getModules()) {
      grp.add(module.getQueryRestriction(getConfigDoc(), getSearchTerm()));
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

  private BaseObject getConfigObj() {
    return modelAccess.getXObject(getConfigDoc(), classConf.getWebSearchConfigClassRef());
  }

  @Override
  public String toString() {
    return new StringBuilder().append("DefaultWebSearchQueryBuilder [configDoc=").append(
        getConfigDocRef()).append(", searchTerm=").append(getSearchTerm()).append("]").toString();
  }

}
