package com.celements.search.lucene;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.script.service.ScriptService;

import com.celements.model.access.exception.DocumentAccessException;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.lucene.query.QueryRestriction;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;
import com.celements.web.service.IWebUtilsService;

@Component(LuceneSearchScriptService.NAME)
public class LuceneSearchScriptService implements ScriptService {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      LuceneSearchScriptService.class);

  public static final String NAME = "lucene";

  @Requirement
  private ILuceneSearchService service;

  @Requirement
  private IWebUtilsService webUtilsService;

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

  public QueryRestriction createSpaceRestriction(String spaceName) {
    spaceName = spaceName.replace("\"", "");
    SpaceReference spaceRef = null;
    if (StringUtils.isNotBlank(spaceName)) {
      spaceRef = webUtilsService.resolveSpaceReference(spaceName);
    }
    return service.createSpaceRestriction(spaceRef);
  }

  public QueryRestriction createDocRestriction(String fullName) {
    fullName = fullName.replace("\"", "");
    DocumentReference docRef = null;
    if (StringUtils.isNotBlank(fullName)) {
      docRef = webUtilsService.resolveDocumentReference(fullName);
    }
    return service.createDocRestriction(docRef);
  }

  public QueryRestriction createObjectRestriction(String objectName) {
    objectName = objectName.replace("\"", "");
    return service.createObjectRestriction(webUtilsService.resolveDocumentReference(
        objectName));
  }

  public QueryRestriction createObjectFieldRestriction(String objectName, String field,
      String value) {
    objectName = objectName.replace("\"", "");
    return service.createFieldRestriction(webUtilsService.resolveDocumentReference(
        objectName), field, value);
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

  public QueryRestrictionGroup createAttachmentRestrictionGroup(List<String> mimeTypes,
      List<String> mimeTypesBlackList, List<String> filenamePrefs) {
    return service.createAttachmentRestrictionGroup(mimeTypes, mimeTypesBlackList,
        filenamePrefs);
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

  public void queueIndexing(DocumentReference docRef) {
    try {
      service.queueForIndexing(docRef);
    } catch (DocumentAccessException dae) {
      LOGGER.error("Failed to access doc '{}'", docRef, dae);
    }
  }

}
