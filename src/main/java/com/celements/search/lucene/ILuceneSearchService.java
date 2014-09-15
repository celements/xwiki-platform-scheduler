package com.celements.search.lucene;

import java.util.Date;
import java.util.List;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.lucene.query.QueryRestriction;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;

@ComponentRole
public interface ILuceneSearchService {

  public LuceneQuery createQuery();

  public LuceneQuery createQuery(String database);
  
  public QueryRestrictionGroup createRestrictionGroup(Type type);

  public QueryRestrictionGroup createRestrictionGroup(Type type, List<String> fields, 
      List<String> values);

  public QueryRestrictionGroup createRestrictionGroup(Type type, List<String> fields, 
      List<String> values, boolean tokenize, boolean fuzzy);

  public QueryRestriction createRestriction(String field, String value);

  public QueryRestriction createRestriction(String field, String value, boolean tokenize);
  
  public QueryRestriction createRestriction(String field, String value, boolean tokenize, 
      boolean fuzzy);

  public QueryRestriction createSpaceRestriction(SpaceReference spaceRef);

  public QueryRestriction createObjectRestriction(DocumentReference classRef);

  public QueryRestriction createFieldRestriction(DocumentReference classRef, String field, 
      String value);

  public QueryRestriction createFieldRestriction(DocumentReference classRef, String field, 
      String value, boolean tokenize);
  
  public QueryRestriction createRangeRestriction(String field, String from, String to);

  public QueryRestriction createRangeRestriction(String field, String from, String to, 
      boolean inclusive);
  
  public QueryRestriction createDateRestriction(String field, Date date);

  public QueryRestriction createFromDateRestriction(String field, Date fromDate, 
      boolean inclusive);

  public QueryRestriction createToDateRestriction(String field, Date toDate, 
      boolean inclusive);

  public QueryRestriction createFromToDateRestriction(String field, Date fromDate, 
      Date toDate, boolean inclusive);
  
  public LuceneSearchResult search(LuceneQuery query, List<String> sortFields, 
      List<String> languages);
  
  public LuceneSearchResult searchWithoutChecks(LuceneQuery query, List<String> sortFields, 
      List<String> languages);
  
  public int getResultLimit(boolean skipChecks);

}
