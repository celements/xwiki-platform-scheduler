package com.celements.search.web.packages;

import static com.celements.search.lucene.LuceneUtils.*;
import static com.celements.search.web.classes.WebAttachmentSearchConfigClass.*;

import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.query.IQueryRestriction;
import com.celements.search.lucene.query.LuceneDocType;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;
import com.celements.search.web.classes.WebAttachmentSearchConfigClass;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.net.MediaType;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.lucene.IndexFields;

@Component(AttachmentWebSearchPackage.NAME)
public class AttachmentWebSearchPackage implements WebSearchPackage {

  public static final String NAME = "attachment";

  @Requirement
  private ILuceneSearchService searchService;

  @Requirement(WebAttachmentSearchConfigClass.CLASS_DEF_HINT)
  private ClassDefinition webAttSearchConfigClass;

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
  public LuceneDocType getDocType() {
    return LuceneDocType.ATT;
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
    ClassField<List<MediaType>> field = isBlacklist ? FIELD_MIMETYPES_BLACK_LIST : FIELD_MIMETYPES;
    return buildRestrictionFromField(cfgDoc, field, new Function<MediaType, IQueryRestriction>() {

      @Override
      public IQueryRestriction apply(MediaType mediaType) {
        return searchService.createRestriction(IndexFields.MIMETYPE, exactify(
            mediaType.toString()));
      }
    }).setNegate(isBlacklist);
  }

  private IQueryRestriction getRestrFilenamePrefixes(XWikiDocument cfgDoc) {
    return buildRestrictionFromField(cfgDoc, FIELD_FILENAME_PREFIXES,
        new Function<String, IQueryRestriction>() {

          @Override
          public IQueryRestriction apply(String str) {
            return searchService.createRestriction(IndexFields.FILENAME, str);
          }
        });
  }

  private <T> IQueryRestriction buildRestrictionFromField(XWikiDocument cfgDoc,
      ClassField<List<T>> field, Function<T, IQueryRestriction> restrictionFunc) {
    return buildRestrictionGroup(Type.OR, modelAccess.getFieldValue(cfgDoc, field).orNull(),
        restrictionFunc);
  }

  @Override
  public Optional<DocumentReference> getLinkedClassRef() {
    return Optional.absent();
  }

  private boolean hasConfigObj(XWikiDocument cfgDoc) {
    return modelAccess.getXObject(cfgDoc, webAttSearchConfigClass.getClassRef()) != null;
  }

}
