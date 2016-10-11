package com.celements.search.web.module;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.model.reference.DocumentReference;

import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.query.IQueryRestriction;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.lucene.IndexFields;

@Component(ContentWebSearchModule.NAME)
public class ContentWebSearchModule implements WebSearchModule {

  public static final String NAME = "content";

  public static final String CFGSRC_PROP_BOOST = "celements.search.web." + NAME + ".boost";

  @Requirement
  private ILuceneSearchService searchService;

  @Requirement
  private ConfigurationSource cfgSrc;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean isDefault() {
    return true;
  }

  @Override
  public boolean isRequired(XWikiDocument cfgDoc) {
    return false;
  }

  @Override
  public IQueryRestriction getQueryRestriction(XWikiDocument cfgDoc, String searchTerm) {
    float boost = cfgSrc.getProperty(CFGSRC_PROP_BOOST, 20f);
    return searchService.createRestriction(IndexFields.FULLTEXT, searchTerm).setBoost(boost);
  }

  @Override
  public Optional<DocumentReference> getLinkedClassRef() {
    return Optional.absent();
  }

}
