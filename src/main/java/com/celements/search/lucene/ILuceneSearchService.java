package com.celements.search.lucene;

import java.util.Date;
import java.util.List;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.search.lucene.query.LuceneQueryApi;
import com.celements.search.lucene.query.LuceneQueryRestrictionApi;

@ComponentRole
public interface ILuceneSearchService {

  public LuceneQueryApi createQuery();

  public LuceneQueryApi createQuery(String database);

  public LuceneQueryApi createQuery(LuceneQueryApi query);

  public LuceneQueryRestrictionApi createRestriction(String field, String value);

  public LuceneQueryRestrictionApi createRestriction(String field, String value,
      boolean tokenize);
  
  public LuceneQueryRestrictionApi createRestriction(String field, String value,
      boolean tokenize, boolean fuzzy);

  public List<LuceneQueryRestrictionApi> createRestrictionList(List<String> fields, 
      String value);

  public List<LuceneQueryRestrictionApi> createRestrictionList(List<String> fields, 
      String value, boolean tokenize, boolean fuzzy);

  public List<LuceneQueryRestrictionApi> createRestrictionList(String field, 
      List<String> values);

  public List<LuceneQueryRestrictionApi> createRestrictionList(String field, 
      List<String> values, boolean tokenize, boolean fuzzy);
  
  public LuceneQueryRestrictionApi createRangeRestriction(String field, String from, 
      String to);

  public LuceneQueryRestrictionApi createRangeRestriction(String field, String from,
      String to, boolean inclusive);
  
  public LuceneQueryRestrictionApi createDateRestriction(String field, Date date);

  public LuceneQueryRestrictionApi createFromDateRestriction(String field, Date fromDate, 
      boolean inclusive);

  public LuceneQueryRestrictionApi createToDateRestriction(String field, Date toDate, 
      boolean inclusive);

  public LuceneQueryRestrictionApi createFromToDateRestriction(String field, 
      Date fromDate, Date toDate, boolean inclusive);
  
  public LuceneSearchResult search(LuceneQueryApi query, List<String> sortFields, 
      List<String> languages);
  
  public LuceneSearchResult searchWithoutChecks(LuceneQueryApi query, 
      List<String> sortFields, List<String> languages);
  
  public int getResultLimit(boolean skipChecks);

}
