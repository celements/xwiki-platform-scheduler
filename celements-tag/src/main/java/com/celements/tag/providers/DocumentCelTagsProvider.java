package com.celements.tag.providers;

import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.util.ModelUtils;
import com.celements.pagetype.classes.PageTypeClass;
import com.celements.tag.CelTag;
import com.celements.tag.CelTagPageType;
import com.celements.tag.classdefs.CelTagDependencyClass;
import com.xpn.xwiki.XWikiConstant;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
public class DocumentCelTagsProvider implements CelTagsProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(DocumentCelTagsProvider.class);

  // TODO menuitems? see CelTagClass
  private static final String XWQL_TAGS = "from doc.object("
      + PageTypeClass.CLASS_REF.serialize() + ") pt "
      + "where doc.translation = 0 "
      + "and pt." + PageTypeClass.FIELD_PAGE_TYPE.getName() + " = '" + CelTagPageType.NAME + "'";

  private final QueryManager queryManager;
  private final IModelAccessFacade modelAccess;
  private final ModelUtils modelUtils;

  @Inject
  public DocumentCelTagsProvider(
      ModelUtils modelUtils,
      IModelAccessFacade modelAccess,
      QueryManager queryManager) {
    this.queryManager = queryManager;
    this.modelAccess = modelAccess;
    this.modelUtils = modelUtils;
  }

  @Override
  public Stream<CelTag.Builder> get() throws CelTagsProvisionException {
    try {
      return queryManager.createQuery(XWQL_TAGS, Query.XWQL)
          .setWiki(XWikiConstant.MAIN_WIKI.getName())
          .<String>execute().stream()
          .map(fn -> modelUtils.resolveRef(fn, DocumentReference.class, XWikiConstant.MAIN_WIKI))
          .map(modelAccess::getOrCreateDocument)
          .map(this::asCelTagBuilder);
    } catch (QueryException exc) {
      throw new CelTagsProvisionException(exc);
    }
  }

  private CelTag.Builder asCelTagBuilder(XWikiDocument tagDefDoc) {
    var builder = new CelTag.Builder();
    var tagDefDocRef = tagDefDoc.getDocumentReference();
    builder.source(tagDefDocRef);
    builder.type(tagDefDocRef.getLastSpaceReference().getName());
    builder.name(tagDefDocRef.getName());
    Optional.ofNullable(tagDefDoc.getParentReference())
        .map(DocumentReference::getName)
        .ifPresent(builder::parent);
    XWikiObjectFetcher.on(tagDefDoc)
        .fetchField(CelTagDependencyClass.FIELD_REFERENCE)
        .stream()
        .map(DocumentReference::getName)
        .forEach(builder::expectDependency);
    LOGGER.info("providing tag {} from {}", builder, tagDefDocRef);
    return builder;
  }

}
