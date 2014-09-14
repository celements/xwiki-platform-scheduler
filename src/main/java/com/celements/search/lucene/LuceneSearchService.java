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
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.lucene.query.QueryRestriction;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.plugin.lucene.LucenePlugin;

@Component
public class LuceneSearchService implements ILuceneSearchService {

  static final DateFormat SDF = new SimpleDateFormat("yyyyMMddHHmm");
  static final String DATE_LOW = "000101010000";
  static final String DATE_HIGH = "999912312359";
  
  private static final boolean DEFAULT_TOKENIZE = true;
  private static final boolean DEFAULT_FUZZY = false;
  
  @Requirement
  private IWebUtilsService webUtilsService;
  
  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty(
        XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  public LuceneQuery createQuery() {
    return new LuceneQuery(getContext().getDatabase());
  }

  public LuceneQuery createQuery(String database) {
    LuceneQuery query;
    if (StringUtils.isNotBlank(database)) {
      query = new LuceneQuery(database);
    } else {
      query = createQuery();
    }
    return query;
  }
  
  public QueryRestrictionGroup createAndRestrictionGroup() {
    return new QueryRestrictionGroup(Type.AND);
  }
  
  public QueryRestrictionGroup createOrRestrictionGroup() {
    return new QueryRestrictionGroup(Type.OR);
  }

  public QueryRestriction createRestriction(String field, String value) {
    return createRestriction(field, value, DEFAULT_TOKENIZE, DEFAULT_FUZZY);
  }

  public QueryRestriction createRestriction(String field, String value, boolean tokenize) {
    return createRestriction(field, value, tokenize, DEFAULT_FUZZY);
  }
  
  public QueryRestriction createRestriction(String field, String value, boolean tokenize, 
      boolean fuzzy) {
    QueryRestriction restriction = null;
    if (StringUtils.isNotBlank(field)) {
      restriction = new QueryRestriction(field, value, tokenize);
      if (fuzzy) {
        restriction.setFuzzy();
      }
    }
    return restriction;
  }

  public QueryRestriction createSpaceRestriction(SpaceReference spaceRef) {
    String spaceName = "";
    if (spaceRef != null) {
      spaceName = spaceRef.getName();
    }
    return createRestriction("space", "\"" + spaceName + "\"");
  }

  public List<QueryRestriction> createSpaceRestrictionList(
      List<SpaceReference> spaceRefs) {
    List<QueryRestriction> restrictionList = new ArrayList<QueryRestriction>();
    for (SpaceReference spaceRef : spaceRefs) {
      QueryRestriction restr = createSpaceRestriction(spaceRef);
      if (restr != null) {
        restrictionList.add(restr);
      }
    }
    return restrictionList;
  }

  public QueryRestriction createObjectRestriction(DocumentReference classRef) {
    QueryRestriction restriction = null;
    if (classRef != null) {
      String className = webUtilsService.getRefLocalSerializer().serialize(classRef);
      restriction = createRestriction("object", "\"" + className + "\"");
    }
    return restriction;
  }

  public QueryRestriction createFieldRestriction(DocumentReference classRef, String field, 
      String value) {
    return createFieldRestriction(classRef, field, value, DEFAULT_TOKENIZE);
  }

  public QueryRestriction createFieldRestriction(DocumentReference classRef, String field, 
      String value, boolean tokenize) {
    QueryRestriction restriction = null;
    if (classRef != null && StringUtils.isNotBlank(field)) {
      String className = webUtilsService.getRefLocalSerializer().serialize(classRef);
      restriction = createRestriction(className + "." + field, value, tokenize);
    }
    return restriction;
  }

  public List<QueryRestriction> createRestrictionList(List<String> fields, String value) {
    return createRestrictionList(fields, value, DEFAULT_TOKENIZE, DEFAULT_FUZZY);
  }

  public List<QueryRestriction> createRestrictionList(List<String> fields, String value, 
      boolean tokenize, boolean fuzzy) {
    List<QueryRestriction> restrictionList = new ArrayList<QueryRestriction>();
    for (String field : fields) {
      QueryRestriction restr = createRestriction(field, value, tokenize, fuzzy);
      if (restr != null) {
        restrictionList.add(restr);
      }
    }
    return restrictionList;
  }

  public List<QueryRestriction> createRestrictionList(String field, List<String> values) {
    return createRestrictionList(field, values, DEFAULT_TOKENIZE, DEFAULT_FUZZY);
  }

  public List<QueryRestriction> createRestrictionList(String field, List<String> values, 
      boolean tokenize, boolean fuzzy) {
    List<QueryRestriction> restrictionList = new ArrayList<QueryRestriction>();
    for (String value : values) {
      QueryRestriction restr = createRestriction(field, value, tokenize, fuzzy);
      if (restr != null) {
        restrictionList.add(restr);
      }
    }
    return restrictionList;
  }
  
  public QueryRestriction createRangeRestriction(String field, String from, String to) {
    return createRangeRestriction(field, from, to, true);
  }

  public QueryRestriction createRangeRestriction(String field, String from, String to, 
      boolean inclusive) {
    String value = from + " TO " + to;
    if(inclusive) {
      value = "[" + value + "]";
    } else {
      value = "{" + value + "}";
    }
    return createRestriction(field, value, false);
  }
  
  public QueryRestriction createDateRestriction(String field, Date date) {
    return createRestriction(field, SDF.format(date), false);
  }

  public QueryRestriction createFromDateRestriction(String field, Date fromDate, 
      boolean inclusive) {
    return createFromToDateRestriction(field, fromDate, null, inclusive);
  }

  public QueryRestriction createToDateRestriction(String field, Date toDate, 
      boolean inclusive) {
    return createFromToDateRestriction(field, null, toDate, inclusive);
  }

  public QueryRestriction createFromToDateRestriction(String field, Date fromDate, 
      Date toDate, boolean inclusive) {
    String from = (fromDate != null) ? SDF.format(fromDate) : DATE_LOW;
    String to = (toDate != null) ? SDF.format(toDate) : DATE_HIGH;
    return createRangeRestriction(field, from, to, inclusive);
  }
  
  public LuceneSearchResult search(LuceneQuery query, List<String> sortFields, 
      List<String> languages) {
    return new LuceneSearchResult(query, sortFields, languages, false, getContext());
  }
  
  public LuceneSearchResult searchWithoutChecks(LuceneQuery query, List<String> sortFields, 
      List<String> languages) {
    return new LuceneSearchResult(query, sortFields, languages, true, getContext());
  }

  public int getResultLimit(boolean skipChecks) {
    LucenePlugin lucenePlugin = (LucenePlugin) getContext().getWiki().getPlugin("lucene", 
        getContext());
    return lucenePlugin.getResultLimit(skipChecks, getContext());
  }

}
