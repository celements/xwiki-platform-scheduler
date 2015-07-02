package com.celements.search.lucene;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.search.lucene.query.IQueryRestriction;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.lucene.query.QueryRestriction;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.plugin.lucene.IndexFields;
import com.xpn.xwiki.plugin.lucene.LucenePlugin;

@Component
public class LuceneSearchService implements ILuceneSearchService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LuceneSearchService.class);

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

  @Override
  public LuceneQuery createQuery() {
    return new LuceneQuery(Arrays.asList(LucenePlugin.DOCTYPE_WIKIPAGE));
  }

  @Override
  public LuceneQuery createQuery(List<String> types) {
    if ((types == null) || types.isEmpty()) {
      types = Arrays.asList(LucenePlugin.DOCTYPE_WIKIPAGE, LucenePlugin.DOCTYPE_ATTACHMENT);
    }
    return new LuceneQuery(types);
  }
  
  @Override
  public QueryRestrictionGroup createRestrictionGroup(Type type) {
    return new QueryRestrictionGroup(type);
  }

  @Override
  public QueryRestrictionGroup createRestrictionGroup(Type type, List<String> fields, 
      List<String> values) {
    return createRestrictionGroup(type, fields, values, DEFAULT_TOKENIZE, DEFAULT_FUZZY);
  }

  @Override
  public QueryRestrictionGroup createRestrictionGroup(Type type, List<String> fields, 
      List<String> values, boolean tokenize, boolean fuzzy) {
    QueryRestrictionGroup restrGrp = createRestrictionGroup(type);
    Iterator<String> fieldIter = fields.iterator();
    Iterator<String> valueIter = values.iterator();
    String field = null;
    String value = null;
    while (fieldIter.hasNext() || valueIter.hasNext()) {
      if (fieldIter.hasNext()) {
        field = fieldIter.next();
      }
      if (valueIter.hasNext()) {
        value = valueIter.next();
      }
      QueryRestriction restr = createRestriction(field, value, tokenize, fuzzy);
      if (restr != null) {
        restrGrp.add(restr);
      }
    }
    return restrGrp;
  }

  @Override
  public QueryRestriction createRestriction(String field, String value) {
    return createRestriction(field, value, DEFAULT_TOKENIZE, DEFAULT_FUZZY);
  }

  @Override
  public QueryRestriction createRestriction(String field, String value, boolean tokenize) {
    return createRestriction(field, value, tokenize, DEFAULT_FUZZY);
  }
  
  @Override
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

  @Override
  public QueryRestriction createSpaceRestriction(SpaceReference spaceRef) {
    String spaceName;
    if (spaceRef != null) {
      spaceName = spaceRef.getName();
    } else {
      spaceName = getContext().getDoc().getDocumentReference().getLastSpaceReference(
          ).getName();
    }
    return createRestriction(IndexFields.DOCUMENT_SPACE, exactify(spaceName));
  }

  @Override
  public QueryRestriction createObjectRestriction(DocumentReference classRef) {
    QueryRestriction restriction = null;
    if (classRef != null) {
      String className = serialize(classRef);
      // workaround bug Ticket #7230
      String spaceName = classRef.getLastSpaceReference().getName();
      if (!Character.isDigit(spaceName.charAt(spaceName.length() - 1))) {
        className = exactify(className);
      }
      restriction = createRestriction(IndexFields.OBJECT, className);
    }
    return restriction;
  }

  @Override
  public QueryRestriction createFieldRestriction(DocumentReference classRef, String field, 
      String value) {
    return createFieldRestriction(classRef, field, value, DEFAULT_TOKENIZE);
  }

  @Override
  public QueryRestriction createFieldRestriction(DocumentReference classRef, String field, 
      String value, boolean tokenize) {
    QueryRestriction restriction = null;
    if ((classRef != null) && StringUtils.isNotBlank(field)) {
      restriction = createRestriction(serialize(classRef) + "." + field, value, tokenize);
    }
    return restriction;
  }
  
  @Override
  public IQueryRestriction createFieldRefRestriction(DocumentReference classRef, 
      String field, EntityReference ref) {
    IQueryRestriction restriction = null;
    if (classRef != null && StringUtils.isNotBlank(field)) {
      String fieldStr = serialize(classRef) + "." + field;
      if (ref != null) {
        QueryRestrictionGroup restrGrp = createRestrictionGroup(Type.OR);
        restrGrp.add(createRestriction(fieldStr, exactify(serialize(ref))));
        restrGrp.add(createRestriction(fieldStr, exactify(serialize(ref))));
        restriction = restrGrp;
      } else {
        restriction = createRestriction(fieldStr, "");
      }
    }
    return restriction;
  }
  
  @Override
  public QueryRestriction createRangeRestriction(String field, String from, String to) {
    return createRangeRestriction(field, from, to, true);
  }

  @Override
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
  
  @Override
  public QueryRestriction createDateRestriction(String field, Date date) {
    return createRestriction(field, SDF.format(date), false);
  }

  @Override
  public QueryRestriction createFromDateRestriction(String field, Date fromDate, 
      boolean inclusive) {
    return createFromToDateRestriction(field, fromDate, null, inclusive);
  }

  @Override
  public QueryRestriction createToDateRestriction(String field, Date toDate, 
      boolean inclusive) {
    return createFromToDateRestriction(field, null, toDate, inclusive);
  }

  @Override
  public QueryRestriction createFromToDateRestriction(String field, Date fromDate, 
      Date toDate, boolean inclusive) {
    String from = (fromDate != null) ? SDF.format(fromDate) : DATE_LOW;
    String to = (toDate != null) ? SDF.format(toDate) : DATE_HIGH;
    return createRangeRestriction(field, from, to, inclusive);
  }

  @Override
  public QueryRestrictionGroup createAttachmentRestrictionGroup(DocumentReference docRef,
      String mimetype, List<String> filenamePrefs) {
    QueryRestrictionGroup attGrp = createRestrictionGroup(Type.AND);
    if (docRef != null) {
      attGrp.add(createRestriction(IndexFields.DOCUMENT_FULLNAME, 
          exactify(serialize(docRef))));
    }
    if (StringUtils.isNotBlank(mimetype)) {
      attGrp.add(createRestriction(IndexFields.MIMETYPE, exactify(mimetype)));
    }
    if (filenamePrefs != null) {
      QueryRestrictionGroup filenameGrp = createRestrictionGroup(Type.OR);
      for (String prefix : filenamePrefs) {
        if (StringUtils.isNotBlank(prefix)) {
          filenameGrp.add(createRestriction(IndexFields.FILENAME, prefix));
        }
      }
      attGrp.add(filenameGrp);
    }
    return attGrp;
  }

  @Override
  public LuceneSearchResult search(LuceneQuery query, List<String> sortFields, 
      List<String> languages) {
    return new LuceneSearchResult(query, sortFields, languages, false, getContext());
  }

  @Override
  public LuceneSearchResult searchWithoutChecks(LuceneQuery query, 
      List<String> sortFields, List<String> languages) {
    return new LuceneSearchResult(query, sortFields, languages, true, getContext());
  }

  @Override
  public LuceneSearchResult search(String queryString, List<String> sortFields, 
      List<String> languages) {
    return new LuceneSearchResult(queryString, sortFields, languages, false, getContext());
  }

  @Override
  public LuceneSearchResult searchWithoutChecks(String queryString, 
      List<String> sortFields, List<String> languages) {
    return new LuceneSearchResult(queryString, sortFields, languages, true, getContext());
  }

  @Override
  public int getResultLimit() {
    return getResultLimit(false);
  }

  @Override
  public int getResultLimit(boolean skipChecks) {
    LucenePlugin lucenePlugin = (LucenePlugin) getContext().getWiki().getPlugin("lucene", 
        getContext());
    int limit = lucenePlugin.getResultLimit(skipChecks, getContext());
    LOGGER.debug("getResultLimit: got '{}' for skipChecks '{}'", limit, skipChecks);
    return limit;
  }

  private String exactify(String str) {
    return "\"" + str + "\"";
  }

  private String serialize(EntityReference ref) {
    return webUtilsService.serializeRef(ref, true);
  }

}
