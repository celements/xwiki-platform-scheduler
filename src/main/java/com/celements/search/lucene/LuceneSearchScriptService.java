package com.celements.search.lucene;

import java.util.Date;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.script.service.ScriptService;

import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.lucene.query.QueryRestriction;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;
import com.xpn.xwiki.plugin.lucene.IndexFields;

@Component("lucene")
public class LuceneSearchScriptService implements ScriptService {

  @Requirement
  private ILuceneSearchService service;

  public LuceneQuery createQuery() {
    return service.createQuery();
  }

  public LuceneQuery createQuery(List<String> types) {
    return service.createQuery(types);
  }

  /**
   * @deprecated instead use {@link LuceneQuery#copy()}
   * 
   * @param query
   * @return
   */
  @Deprecated
  public LuceneQuery createQuery(LuceneQuery query) {
    return query.copy();
  }

  public QueryRestrictionGroup createAndRestrictionGroup() {
    return service.createRestrictionGroup(Type.AND);
  }

  public QueryRestrictionGroup createOrRestrictionGroup() {
    return service.createRestrictionGroup(Type.OR);
  }

  public QueryRestrictionGroup createAndRestrictionGroup(List<String> fields, 
      List<String> values) {
    return service.createRestrictionGroup(Type.AND, fields, values);
  }

  public QueryRestrictionGroup createOrRestrictionGroup(List<String> fields, 
      List<String> values) {
    return service.createRestrictionGroup(Type.OR, fields, values);
  }

  public QueryRestriction createRestriction(String field, String value) {
    return service.createRestriction(field, value);
  }

  public QueryRestriction createObjectRestriction(String objectName) {
    return service.createRestriction(IndexFields.OBJECT, objectName);
  }

  public QueryRestriction createObjectFieldRestriction(String objectName, String field, 
      String value) {
    return service.createRestriction(objectName + "." + field, value);
  }

  public QueryRestriction createRangeRestriction(String field, String from, String to) {
    return createRangeRestriction(field, from, to, true);
  }

  public QueryRestriction createOjbectFieldRangeRestriction(String objectName, 
      String field, String from, String to, boolean inclusive) {
    return createRangeRestriction(objectName + "." + field, from, to, inclusive);
  }

  public QueryRestriction createRangeRestriction(String field, String from, String to, 
      boolean inclusive) {
    return service.createRangeRestriction(field, from, to, inclusive);
  }

  public QueryRestriction createDateRestriction(String field, Date date) {
    return service.createDateRestriction(field, date);
  }

  public QueryRestriction createFromDateRestriction(String field, Date fromDate, 
      boolean inclusive) {
    return service.createFromDateRestriction(field, fromDate, inclusive);
  }

  public QueryRestriction createToDateRestriction(String field, Date toDate, 
      boolean inclusive) {
    return service.createToDateRestriction(field, toDate, inclusive);
  }

  public QueryRestriction createFromToDateRestriction(String field, Date fromDate, 
      Date toDate, boolean inclusive) {
    return service.createFromToDateRestriction(field, fromDate, toDate, inclusive);
  }

  public LuceneSearchResult search(LuceneQuery query) {
    return service.search(query, null, null);
  }

  public LuceneSearchResult search(LuceneQuery query, List<String> sortFields) {
    return service.search(query, sortFields, null);
  }

  public LuceneSearchResult search(LuceneQuery query, List<String> sortFields, 
      List<String> languages) {
    return service.search(query, sortFields, languages);
  }

  public LuceneSearchResult search(String queryString) {
    return service.search(queryString, null, null);
  }

  public LuceneSearchResult search(String queryString, List<String> sortFields) {
    return service.search(queryString, sortFields, null);
  }

  public LuceneSearchResult search(String queryString, List<String> sortFields, 
      List<String> languages) {
    return service.search(queryString, sortFields, languages);
  }

  public int getResultLimit() {
    return service.getResultLimit();
  }

  public int getResultLimit(boolean skipChecks) {
    return service.getResultLimit(skipChecks);
  }

}
