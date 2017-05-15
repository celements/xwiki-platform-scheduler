package com.celements.search.web.packages;

import static com.celements.search.lucene.LuceneSearchUtil.*;
import static com.celements.search.web.classes.IWebSearchClassConfig.*;

import java.util.Set;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.query.IQueryRestriction;
import com.celements.search.lucene.query.LuceneDocType;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;
import com.celements.search.web.classes.IWebSearchClassConfig;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.plugin.lucene.IndexFields;

@Component(AttachmentWebSearchPackage.NAME)
public class AttachmentWebSearchPackage implements WebSearchPackage {

  public static final String NAME = "attachment";

  @Requirement
  private ILuceneSearchService searchService;

  @Requirement
  private IWebSearchClassConfig classConf;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement(ContentWebSearchPackage.NAME)
  private WebSearchPackage contentModule;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean isDefault() {
    return false;
  }

  @Override
  public boolean isRequired(XWikiDocument cfgDoc) {
    return hasConfigObj(cfgDoc);
  }

  @Override
  public Set<LuceneDocType> getDocTypes() {
    return ImmutableSet.of(LuceneDocType.ATT);
  }

  @Override
  public IQueryRestriction getQueryRestriction(XWikiDocument cfgDoc, String searchTerm) {
    QueryRestrictionGroup grp = searchService.createRestrictionGroup(Type.AND);
    if (hasConfigObj(cfgDoc)) {
      grp.add(getRestrMimeTypes(cfgDoc, false));
      grp.add(getRestrMimeTypes(cfgDoc, true));
      grp.add(getRestrFilenamePrefixes(cfgDoc));
      grp.add(contentModule.getQueryRestriction(cfgDoc, searchTerm));
    }
    return grp;
  }

  private IQueryRestriction getRestrMimeTypes(XWikiDocument cfgDoc, boolean isBlacklist) {
    String fieldName = isBlacklist ? PROPERTY_MIMETYPES_BLACK_LIST : PROPERTY_MIMETYPES;
    return buildRestrictionFromField(cfgDoc, fieldName, new Function<String, IQueryRestriction>() {

      @Override
      public IQueryRestriction apply(String str) {
        return searchService.createRestriction(IndexFields.MIMETYPE, exactify(str));
      }
    }).setNegate(isBlacklist);
  }

  private IQueryRestriction getRestrFilenamePrefixes(XWikiDocument cfgDoc) {
    String fieldName = PROPERTY_FILENAME_PREFIXES;
    return buildRestrictionFromField(cfgDoc, fieldName, new Function<String, IQueryRestriction>() {

      @Override
      public IQueryRestriction apply(String str) {
        return searchService.createRestriction(IndexFields.FILENAME, str);
      }
    });
  }

  private IQueryRestriction buildRestrictionFromField(XWikiDocument cfgDoc, String fieldName,
      Function<String, IQueryRestriction> getRestrFunc) {
    return buildRestrictionGroup(cfgDoc.getStringValue(fieldName), Type.OR, getRestrFunc);
  }

  @Override
  public Optional<DocumentReference> getLinkedClassRef() {
    return Optional.absent();
  }

  private boolean hasConfigObj(XWikiDocument cfgDoc) {
    return getConfigObj(cfgDoc) != null;
  }

  private BaseObject getConfigObj(XWikiDocument cfgDoc) {
    return modelAccess.getXObject(cfgDoc, classConf.getWebAttachmentSearchConfigClassRef());
  }

}
