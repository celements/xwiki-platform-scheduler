package com.celements.search.lucene;

import java.util.Date;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.script.service.ScriptService;

import com.celements.search.lucene.query.LuceneQueryApi;
import com.celements.search.lucene.query.LuceneQueryRestrictionApi;

@Component("lucene")
public class LuceneSearchScriptService implements ScriptService {

  @Requirement
  private ILuceneSearchService service;

  public LuceneQueryApi createQuery() {
    return service.createQuery();
  }

  public LuceneQueryApi createQuery(LuceneQueryApi query) {
    return service.createQuery(query);
  }

  public LuceneQueryRestrictionApi createRestriction(String field, String value) {
    return service.createRestriction(field, value);
  }

  public LuceneQueryRestrictionApi createObjectRestriction(String objectName) {
    return service.createRestriction("object", objectName);
  }

  public LuceneQueryRestrictionApi createObjectFieldRestriction(String objectName,
      String field, String value) {
    return service.createRestriction(objectName + "." + field, value);
  }

  public LuceneQueryRestrictionApi createRangeRestriction(String field, String from,
      String to) {
    return createRangeRestriction(field, from, to, true);
  }

  public LuceneQueryRestrictionApi createOjbectFieldRangeRestriction(String objectName,
      String field, String from, String to, boolean inclusive) {
    return createRangeRestriction(objectName + "." + field, from, to, inclusive);
  }

  public LuceneQueryRestrictionApi createRangeRestriction(String field, String from,
      String to, boolean inclusive) {
    return service.createRangeRestriction(field, from, to, inclusive);
  }
  
  public LuceneQueryRestrictionApi createDateRestriction(String field, Date date) {
    return service.createDateRestriction(field, date);
  }

  public LuceneQueryRestrictionApi createFromDateRestriction(String field, Date fromDate, 
      boolean inclusive) {
    return service.createFromDateRestriction(field, fromDate, inclusive);
  }

  public LuceneQueryRestrictionApi createToDateRestriction(String field, Date toDate, 
      boolean inclusive) {
    return service.createToDateRestriction(field, toDate, inclusive);
  }

  public LuceneQueryRestrictionApi createFromToDateRestriction(String field, 
      Date fromDate, Date toDate, boolean inclusive) {
    return service.createFromToDateRestriction(field, fromDate, toDate, inclusive);
  }
  
  public LuceneSearchResult search(LuceneQueryApi query) {
    return service.search(query, null, null);
  }
  
  public LuceneSearchResult search(LuceneQueryApi query, List<String> sortFields) {
    return service.search(query, sortFields, null);
  }
  
  public LuceneSearchResult search(LuceneQueryApi query, List<String> sortFields, 
      List<String> languages) {
    return service.search(query, sortFields, languages);
  }
  
}
