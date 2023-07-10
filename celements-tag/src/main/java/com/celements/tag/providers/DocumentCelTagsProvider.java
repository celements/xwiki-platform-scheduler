package com.celements.tag.providers;

import static com.celements.common.lambda.LambdaExceptionUtil.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.python.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.reference.RefBuilder;
import com.celements.model.util.ModelUtils;
import com.celements.pagetype.classes.PageTypeClass;
import com.celements.tag.CelTag;
import com.celements.tag.CelTagPageType;
import com.celements.tag.classdefs.CelTagDependencyClass;
import com.celements.web.CelConstant;
import com.celements.wiki.WikiService;
import com.xpn.xwiki.XWikiConstant;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
public class DocumentCelTagsProvider implements CelTagsProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(DocumentCelTagsProvider.class);

  // TODO order by menuitems
  private static final String XWQL_TAGS = "from doc.object("
      + PageTypeClass.CLASS_REF.serialize() + ") pt "
      + "where doc.translation = 0 "
      + "and pt." + PageTypeClass.FIELD_PAGE_TYPE.getName() + " = '" + CelTagPageType.NAME + "'";

  private final WikiService wikiService;
  private final QueryManager queryManager;
  private final IModelAccessFacade modelAccess;
  private final ModelUtils modelUtils;

  @Inject
  public DocumentCelTagsProvider(
      QueryManager queryManager,
      WikiService wikiService,
      IModelAccessFacade modelAccess,
      ModelUtils modelUtils) {
    this.wikiService = wikiService;
    this.queryManager = queryManager;
    this.modelAccess = modelAccess;
    this.modelUtils = modelUtils;
  }

  @Override
  public Collection<CelTag.Builder> get() throws CelTagsProvisionException {
    try {
      var tags = wikiService.streamAllWikis()
          .flatMap(rethrow(this::loadTagDocs))
          .map(modelAccess::getOrCreateDocument)
          .map(this::asCelTagBuilder)
          .collect(Collectors.toList());
      LOGGER.info("providing tags: {}", tags);
      return tags;
    } catch (QueryException exc) {
      throw new CelTagsProvisionException(exc);
    }
  }

  private Stream<DocumentReference> loadTagDocs(WikiReference wiki) throws QueryException {
    List<String> results = queryManager.createQuery(XWQL_TAGS, Query.XWQL)
        .setWiki(wiki.getName())
        .<String>execute();
    LOGGER.debug("loaded tags for {}: {}", wiki, results);
    return results.stream().map(fn -> modelUtils.resolveRef(fn, DocumentReference.class, wiki));
  }

  private CelTag.Builder asCelTagBuilder(XWikiDocument tagDefDoc) {
    var builder = new CelTag.Builder();
    builder.source(tagDefDoc.getDocRef());
    builder.type(getTagType(tagDefDoc));
    builder.name(getSanitisedDocTitle(tagDefDoc).orElseGet(() -> tagDefDoc.getDocRef().getName()));
    if (!CelConstant.CENTRAL_WIKI.equals(tagDefDoc.getWikiRef())) {
      builder.scope(tagDefDoc.getWikiRef());
    }
    Optional.ofNullable(tagDefDoc.getParentReference())
        .map(DocumentReference::getName)
        .ifPresent(builder::parent);
    XWikiObjectFetcher.on(tagDefDoc)
        .fetchField(CelTagDependencyClass.FIELD_REFERENCE)
        .stream()
        .map(DocumentReference::getName)
        .forEach(builder::expectDependency);
    return builder;
  }

  /**
   * space title (WebPreferences) or space name
   */
  private String getTagType(XWikiDocument tagDefDoc) {
    var tagSpaceRef = tagDefDoc.getDocRef().getLastSpaceReference();
    return modelAccess.getDocumentOpt(RefBuilder.from(tagSpaceRef)
        .doc(XWikiConstant.WEB_PREF_DOC_NAME)
        .build(DocumentReference.class))
        .flatMap(this::getSanitisedDocTitle)
        .orElse(tagSpaceRef.getName());
  }

  private Optional<String> getSanitisedDocTitle(XWikiDocument doc) {
    return Optional.ofNullable(Strings.emptyToNull(doc.getTitle().trim()
        .replaceAll("[^!-~]", ""))); // printable ASCII only
  }

}
