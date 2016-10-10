package com.celements.search.web.module;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.query.IQueryRestriction;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.lucene.IndexFields;

@Component(ContentWebSearchModule.NAME)
public class ContentWebSearchModule implements WebSearchModule {

  public static final String NAME = "content";

  @Requirement
  private ILuceneSearchService searchService;

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
    return searchService.createRestriction(IndexFields.FULLTEXT, searchTerm);
  }

}
