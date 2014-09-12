package com.celements.search.lucene;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

import com.celements.search.lucene.query.LuceneQueryApi;
import com.celements.search.lucene.query.LuceneQueryRestrictionApi;
import com.xpn.xwiki.XWikiContext;

@Component
public class LuceneSearchService implements ILuceneSearchService {

  static final DateFormat SDF = new SimpleDateFormat("yyyyMMddHHmm");
  static final String DATE_LOW = "000101010000";
  static final String DATE_HIGH = "999912312359";
  
  private static final boolean DEFAULT_TOKENIZE = true;
  private static final boolean DEFAULT_FUZZY = false;
  
  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty(
        XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  public LuceneQueryApi createQuery() {
    return new LuceneQueryApi(getContext().getDatabase());
  }

  public LuceneQueryApi createQuery(String database) {
    return new LuceneQueryApi(database);
  }

  public LuceneQueryApi createQuery(LuceneQueryApi query) {
    return new LuceneQueryApi(query);
  }

  public LuceneQueryRestrictionApi createRestriction(String field, String value) {
    return createRestriction(field, value, DEFAULT_TOKENIZE, DEFAULT_FUZZY);
  }

  public LuceneQueryRestrictionApi createRestriction(String field, String value,
      boolean tokenize) {
    return createRestriction(field, value, tokenize, DEFAULT_FUZZY);
  }
  
  public LuceneQueryRestrictionApi createRestriction(String field, String value,
      boolean tokenize, boolean fuzzy) {
    LuceneQueryRestrictionApi restriction = null;
    if (StringUtils.isNotBlank(field)) {
      restriction = new LuceneQueryRestrictionApi(field, value, tokenize);
      if (fuzzy) {
        restriction.setFuzzy();
      }
    }
    return restriction;
  }

  public List<LuceneQueryRestrictionApi> createRestrictionList(List<String> fields, 
      String value) {
    return createRestrictionList(fields, value, DEFAULT_TOKENIZE, DEFAULT_FUZZY);
  }

  public List<LuceneQueryRestrictionApi> createRestrictionList(List<String> fields, 
      String value, boolean tokenize, boolean fuzzy) {
    List<LuceneQueryRestrictionApi> restrictionList = 
        new ArrayList<LuceneQueryRestrictionApi>();
    for (String field : fields) {
      LuceneQueryRestrictionApi restr = createRestriction(field, value, tokenize, fuzzy);
      if (restr != null) {
        restrictionList.add(restr);
      }
    }
    return restrictionList;
  }

  public List<LuceneQueryRestrictionApi> createRestrictionList(String field, 
      List<String> values) {
    return createRestrictionList(field, values, DEFAULT_TOKENIZE, DEFAULT_FUZZY);
  }

  public List<LuceneQueryRestrictionApi> createRestrictionList(String field, 
      List<String> values, boolean tokenize, boolean fuzzy) {
    List<LuceneQueryRestrictionApi> restrictionList = 
        new ArrayList<LuceneQueryRestrictionApi>();
    for (String value : values) {
      LuceneQueryRestrictionApi restr = createRestriction(field, value, tokenize, fuzzy);
      if (restr != null) {
        restrictionList.add(restr);
      }
    }
    return restrictionList;
  }
  
  public LuceneQueryRestrictionApi createRangeRestriction(String field, String from, 
      String to) {
    return createRangeRestriction(field, from, to, true);
  }

  public LuceneQueryRestrictionApi createRangeRestriction(String field, String from,
      String to, boolean inclusive) {
    String value = from + " TO " + to;
    if(inclusive) {
      value = "[" + value + "]";
    } else {
      value = "{" + value + "}";
    }
    return createRestriction(field, value, false);
  }
  
  public LuceneQueryRestrictionApi createDateRestriction(String field, Date date) {
    return createRestriction(field, SDF.format(date), false);
  }

  public LuceneQueryRestrictionApi createFromDateRestriction(String field, Date fromDate, 
      boolean inclusive) {
    return createFromToDateRestriction(field, fromDate, null, inclusive);
  }

  public LuceneQueryRestrictionApi createToDateRestriction(String field, Date toDate, 
      boolean inclusive) {
    return createFromToDateRestriction(field, null, toDate, inclusive);
  }

  public LuceneQueryRestrictionApi createFromToDateRestriction(String field, 
      Date fromDate, Date toDate, boolean inclusive) {
    String from = (fromDate != null) ? SDF.format(fromDate) : DATE_LOW;
    String to = (toDate != null) ? SDF.format(toDate) : DATE_HIGH;
    return createRangeRestriction(field, from, to, inclusive);
  }
  
  public LuceneSearchResult search(LuceneQueryApi query, List<String> sortFields, 
      List<String> languages) {
    return new LuceneSearchResult(query, sortFields, languages, false, getContext());
  }
  
  public LuceneSearchResult searchWithoutChecks(LuceneQueryApi query, 
      List<String> sortFields, List<String> languages) {
    return new LuceneSearchResult(query, sortFields, languages, true, getContext());
  }

}
