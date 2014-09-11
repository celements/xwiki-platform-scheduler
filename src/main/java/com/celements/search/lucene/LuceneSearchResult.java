package com.celements.search.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.queryParser.ParseException;
import org.xwiki.model.reference.DocumentReference;

import com.celements.search.lucene.query.LuceneQueryApi;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.plugin.lucene.LucenePlugin;
import com.xpn.xwiki.plugin.lucene.SearchResult;
import com.xpn.xwiki.plugin.lucene.SearchResults;

public class LuceneSearchResult {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      LuceneSearchResult.class);

  private SearchResults searchResultsCache;
  private LucenePlugin lucenePlugin;

  private final XWikiContext context;
  
  private final String queryString;
  private final List<String> sortFields;
  private final List<String> languages;
  private final boolean skipChecks;
  
  private int offset = 0;
  private int limit = 0;

  LuceneSearchResult(LuceneQueryApi query, List<String> sortFields, 
      List<String> languages, boolean skipChecks, XWikiContext context) {
    this.queryString = query.getQueryString();
    this.sortFields = getList(sortFields);
    this.languages = getList(languages);
    this.skipChecks = skipChecks;
    this.context = context;
  }

  private List<String> getList(List<String> list) {
    if (list != null) {
      return Collections.unmodifiableList(list);
    } else {
      return Collections.emptyList();
    }
  }

  public String getQueryString() {
    return queryString;
  }

  public List<String> getSortFields() {
    return sortFields;
  }

  private String[] getSortFieldsArray() {
    return sortFields.toArray(new String[sortFields.size()]);
  }

  public List<String> getLanguages() {
    return languages;
  }
  
  private String getLanguageString() {
    return StringUtils.join(languages, ",");
  }

  public boolean isSkipChecks() {
    return skipChecks;
  }

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public List<DocumentReference> getResults() throws LuceneSearchException {
    List<DocumentReference> ret = new ArrayList<DocumentReference>();
    SearchResults results = luceneSearch();
    int offset = getOffset() + 1;
    if (offset < 0) {
      offset = 0;
    }
    int limit = getLimit();
    if (limit <= 0) {
      limit = results.getHitcount();
    }
    for (SearchResult result : results.getResults(offset, limit)) {
      ret.add(result.getDocumentReference());
    }
    LOGGER.info("getResults: returning '" + ret.size() + "' results for: " + this);
    return ret;
  }

  public int getSize() throws LuceneSearchException {
    int hitcount = luceneSearch().getHitcount();
    LOGGER.debug("getSize: returning '" + hitcount + "' for: '" + this);
    return hitcount;
  }

  SearchResults luceneSearch() throws LuceneSearchException {
    try {
      if (searchResultsCache == null) {
        if (skipChecks) {
          searchResultsCache = getLucenePlugin().getSearchResultsWithoutChecks(
              queryString, getSortFieldsArray(), null, getLanguageString(), context);
        } else {
          searchResultsCache = getLucenePlugin().getSearchResults(queryString, 
              getSortFieldsArray(), null, getLanguageString(), context);
        }
        LOGGER.trace("luceneSearch: new searchResults for: " + this);
      } else {
        LOGGER.trace("luceneSearch: returning cached searchResults");
      }
      return searchResultsCache;
    } catch (IOException ioe) {
      throw new LuceneSearchException(ioe);
    } catch (ParseException exc) {
      throw new LuceneSearchException(exc);
    }
  }

  @Override
  public String toString() {
    return "LuceneSearchResult [queryString=" + queryString + ", sortFields=" + sortFields
        + ", languages=" + languages + ", skipChecks=" + skipChecks + ", offset="
        + offset + ", limit=" + limit + "]";
  }

  private LucenePlugin getLucenePlugin() {
    if (lucenePlugin == null) {
      lucenePlugin = (LucenePlugin) context.getWiki().getPlugin("lucene", context);
    }
    return lucenePlugin;
  }

  void injectLucenePlugin(LucenePlugin lucenePlugin) {
    this.lucenePlugin = lucenePlugin;
  }
  
  void injectSearchResultsCache(SearchResults searchResults) {
    this.searchResultsCache = searchResults;
  }

}
