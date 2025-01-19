package com.celements.layout;

import static com.celements.cells.classes.PageLayoutPropertiesClass.*;
import static com.celements.common.lambda.LambdaExceptionUtil.*;
import static com.google.common.base.Preconditions.*;
import static com.google.common.base.Predicates.*;
import static com.google.common.collect.ImmutableMap.*;

import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.velocity.VelocityManager;

import com.celements.cells.HtmlDoctype;
import com.celements.cells.IRenderStrategy;
import com.celements.cells.RenderingEngine;
import com.celements.cells.classes.PageLayoutPropertiesClass;
import com.celements.cells.div.CellRenderStrategy;
import com.celements.cells.json.JsonRenderStrategy;
import com.celements.common.MoreOptional;
import com.celements.inheritor.InheritorFactory;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentDeleteException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.model.context.Contextualiser;
import com.celements.model.context.ModelContext;
import com.celements.model.object.xwiki.XWikiObjectEditor;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.reference.RefBuilder;
import com.celements.model.util.ModelUtils;
import com.celements.navigation.TreeNode;
import com.celements.navigation.service.ITreeNodeService;
import com.celements.pagelayout.LayoutServiceRole;
import com.celements.web.CelConstant;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.plugin.packaging.DocumentInfo;
import com.xpn.xwiki.plugin.packaging.PackageAPI;

import one.util.streamex.StreamEx;

@Component
@ThreadSafe
public final class DefaultLayoutService implements LayoutServiceRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLayoutService.class);

  private static final String CEL_RENDERING_LAYOUT_STACK_PROPERTY = "celRenderingLayoutStack";
  private static final String CEL_RENDERING_LAYOUT_CONTEXT_PROPERTY = "celRenderingLayout";

  static final String SIMPLE_LAYOUT = "SimpleLayout";
  static final String XWIKICFG_CELEMENTS_LAYOUT_DEFAULT = "celements.layout.default";
  static final String HQL_PAGE_LAYOUT = "select distinct doc.space"
      + " from XWikiDocument as doc, BaseObject obj, Celements.PageLayoutPropertiesClass as pl"
      + " where doc.fullName = obj.name"
      + " and obj.className='Celements.PageLayoutPropertiesClass'"
      + " and pl.id.id=obj.id"
      + " order by pl.prettyname asc";

  /**
   * The name of the internal packaging plugin.
   */
  private static final String PACKAGEPLUGIN_NAME = "package";

  private final ModelContext modelContext;
  private final ModelUtils modelUtils;
  private final IModelAccessFacade modelAccess;
  private final QueryManager queryManager;
  private final ConfigurationSource cfgSrc;
  private final VelocityManager velocityManager;
  private final ITreeNodeService treeNodeService;

  @Inject
  public DefaultLayoutService(ModelContext modelContext,
      ModelUtils modelUtils,
      IModelAccessFacade modelAccess,
      QueryManager queryManager,
      ConfigurationSource cfgSrc,
      IWebUtilsService webUtilsSrv,
      VelocityManager velocityManager,
      ITreeNodeService treeNodeService) {
    this.modelContext = modelContext;
    this.modelUtils = modelUtils;
    this.modelAccess = modelAccess;
    this.queryManager = queryManager;
    this.cfgSrc = cfgSrc;
    this.velocityManager = velocityManager;
    this.treeNodeService = treeNodeService;
  }

  @Override
  public Map<SpaceReference, String> getAllPageLayouts() {
    return streamAllLayoutsSpaces().collect(toImmutableMap(
        layout -> layout,
        layout -> getPrettyName(layout).orElse("Untitled Layout")));
  }

  @Override
  public Map<SpaceReference, String> getActivePageLayouts() {
    return streamAllLayoutsSpaces()
        .filter(this::isActive)
        .collect(toImmutableMap(
            layout -> layout,
            layout -> getPrettyName(layout).orElse("Untitled Layout")));
  }

  @Override
  public Stream<SpaceReference> streamAllLayoutsSpaces() {
    return streamLayoutsSpaces(modelContext.getWikiRef(), CelConstant.CENTRAL_WIKI)
        .distinct(SpaceReference::getName);
  }

  @Override
  public StreamEx<SpaceReference> streamLayoutsSpaces(WikiReference... wikis) {
    return StreamEx.of(wikis)
        .filter(Objects::nonNull)
        .flatMap(this::loadLayouts);
  }

  private Stream<SpaceReference> loadLayouts(WikiReference wiki) {
    try {
      Query query = queryManager.createQuery(HQL_PAGE_LAYOUT, Query.HQL)
          .setWiki(wiki.getName());
      return StreamEx.of(query.<String>execute())
          .mapPartial(MoreOptional::asNonBlank)
          .map(space -> RefBuilder.from(wiki)
              .space(space)
              .build(SpaceReference.class));
    } catch (QueryException exp) {
      LOGGER.error("Failed to get all page layouts", exp);
      return Stream.of();
    }
  }

  @Override
  public boolean createLayout(@NotNull SpaceReference layoutSpaceRef) {
    checkNotNull(layoutSpaceRef);
    final Optional<DocumentReference> layoutPropDocRef = getLayoutPropDocRef(layoutSpaceRef);
    if (!existsLayout(layoutSpaceRef) && layoutPropDocRef.isPresent()) {
      try {
        final XWikiDocument propXdoc = modelAccess.getOrCreateDocument(layoutPropDocRef.get());
        XWikiObjectEditor.on(propXdoc)
            .filter(PageLayoutPropertiesClass.CLASS_REF)
            .filter(FIELD_PRETTYNAME, layoutSpaceRef.getName() + " Layout")
            .filter(FIELD_LAYOUT_DOCTYPE, getDocType())
            .createFirst();
        modelAccess.saveDocument(propXdoc, "Creating page layout", false);
        return true;
      } catch (DocumentSaveException exp) {
        LOGGER.error("createNew: failed to create new page layout.", exp);
      }
    }
    return false;
  }

  @Override
  public boolean deleteLayout(SpaceReference layoutSpaceRef) {
    checkNotNull(layoutSpaceRef);
    final boolean moveToTrash = true;
    try {
      Query spaceDocsQuery = queryManager.createQuery("where doc.space = :space", Query.XWQL);
      spaceDocsQuery.bindValue("space", layoutSpaceRef.getName());
      for (String docName : spaceDocsQuery.<String>execute()) {
        modelAccess.deleteDocument(modelUtils.resolveRef(docName, DocumentReference.class),
            moveToTrash);
      }
      return true;
    } catch (QueryException exp) {
      LOGGER.warn("Failed to get the list of documents while trying to delete space [{}]",
          layoutSpaceRef, exp);
    } catch (DocumentDeleteException exp) {
      LOGGER.error("deleteLayout: Failed to delete documents for space [{}].", layoutSpaceRef, exp);
    }
    return false;
  }

  @Override
  public String renderLayout() {
    return renderLayoutLocal(getPageLayoutForCurrentDoc());
  }

  @Override
  public String renderLayout(@Nullable SpaceReference layoutSpaceRef) {
    LOGGER.info("renderPageLayout: for layoutRef '{}'", layoutSpaceRef);
    layoutSpaceRef = resolveValidLayoutSpace(layoutSpaceRef).orElse(null);
    LOGGER.debug("renderPageLayout: after resolveValidLayoutSpace layoutRef '{}'", layoutSpaceRef);
    return renderLayoutLocal(layoutSpaceRef);
  }

  @Override
  public String renderLayoutAsJson(@Nullable SpaceReference layoutSpaceRef) {
    LOGGER.info("renderLayoutAsJson: for layoutRef '{}'", layoutSpaceRef);
    final SpaceReference spaceRef = resolveValidLayoutSpace(layoutSpaceRef).orElse(null);
    LOGGER.debug("renderLayoutAsJson: after resolveValidLayoutSpace layoutRef '{}'", spaceRef);
    return renderLocal(new JsonRenderStrategy(), spaceRef,
        (RenderingEngine renderEngine) -> renderEngine.renderLayout(spaceRef));
  }

  @Override
  @NotNull
  public Optional<String> renderLayoutPartial(@Nullable DocumentReference startNodeRef) {
    if (startNodeRef == null) {
      return Optional.empty();
    }
    SpaceReference layoutSpaceRef = startNodeRef.getLastSpaceReference();
    LOGGER.info("renderLayoutPartial: for layoutRef '{}'", layoutSpaceRef);
    layoutSpaceRef = resolveValidLayoutSpace(layoutSpaceRef).orElse(null);
    LOGGER.debug("renderLayoutPartial: after resolveValidLayoutSpace layoutRef '{}'",
        layoutSpaceRef);
    DocumentReference localStartNodeRef = RefBuilder.from(layoutSpaceRef)
        .doc(startNodeRef.getName()).build(DocumentReference.class);
    if (treeNodeService.isTreeNode(localStartNodeRef)) {
      TreeNode startNode;
      try {
        startNode = treeNodeService.getTreeNodeForDocRef(localStartNodeRef);
        return Optional.ofNullable(renderLayoutPartialLocal(startNode));
      } catch (XWikiException exp) {
        LOGGER.warn("Failed to get TreeNode for {}", localStartNodeRef, exp);
      }
    } else {
      LOGGER.info("cannot render {}, because it must be a TreeNode.", localStartNodeRef);
    }
    return Optional.empty();
  }

  @Override
  public String renderLayoutLocal(@Nullable SpaceReference layoutSpaceRef) {
    return renderLocal(new CellRenderStrategy(), layoutSpaceRef,
        (RenderingEngine renderEngine) -> renderEngine.renderLayout(layoutSpaceRef));
  }

  @Override
  public String renderLayoutPartialLocal(TreeNode startNode) {
    return renderLocal(new CellRenderStrategy(),
        startNode.getDocumentReference().getLastSpaceReference(),
        (RenderingEngine renderEngine) -> renderEngine.renderLayoutPartial(startNode));
  }

  private String renderLocal(IRenderStrategy cellRenderer, SpaceReference layoutSpaceRef,
      Consumer<RenderingEngine> renderLocalFunc) {
    if (layoutSpaceRef == null) {
      return "";
    }
    long millisec = System.currentTimeMillis();
    LOGGER.debug("renderLocal for layout [{}].", layoutSpaceRef);
    RenderingEngine renderEngine = new RenderingEngine().setRenderStrategy(cellRenderer);
    getRenderingLayoutStack().push(layoutSpaceRef);
    new Contextualiser()
        .withVeloContext(CEL_RENDERING_LAYOUT_CONTEXT_PROPERTY, layoutSpaceRef)
        .execute(() -> renderLocalFunc.accept(renderEngine));
    getRenderingLayoutStack().pop();
    LOGGER.info("renderLocal finishing. Time used in millisec: {}",
        (System.currentTimeMillis() - millisec));
    return cellRenderer.getAsString();
  }

  private Optional<SpaceReference> overwriteLayoutRef() {
    return Optional.ofNullable(velocityManager.getVelocityContext())
        .map(vc -> vc.get(OVERWRITE_LAYOUT_REF))
        .filter(SpaceReference.class::isInstance)
        .map(SpaceReference.class::cast)
        .flatMap(this::resolveValidLayoutSpace);
  }

  @Override
  public SpaceReference getPageLayoutForCurrentDoc() {
    return overwriteLayoutRef()
        .or(() -> modelContext.getDocRef().map(this::getPageLayoutForDoc))
        .orElse(null);
  }

  @Override
  public boolean existsLayout(@Nullable SpaceReference layoutSpaceRef) {
    return getLayoutPropDocRef(layoutSpaceRef)
        .flatMap(this::getLayoutPropertyBaseObject)
        .isPresent();
  }

  @Override
  public Optional<DocumentReference> getLayoutPropDocRefForCurrentDoc() {
    SpaceReference currDocPageLayout = getPageLayoutForCurrentDoc();
    if (currDocPageLayout != null) {
      LOGGER.info("getLayoutPropDoc: found page layout [{}] for page [{}].", currDocPageLayout,
          modelContext.getDocRef());
      return getLayoutPropDocRef(currDocPageLayout);
    }
    LOGGER.info("getLayoutPropDoc: found NO page layout for page [{}].",
        modelContext.getDocRef());
    return Optional.empty();
  }

  @Override
  @NotNull
  public Optional<DocumentReference> getLayoutPropDocRef(@Nullable SpaceReference layoutSpaceRef) {
    return Optional.ofNullable(layoutSpaceRef)
        .map(spaceRef -> RefBuilder.from(spaceRef).doc("WebHome").build(DocumentReference.class));
  }

  @Override
  @Nullable
  public SpaceReference getPageLayoutForDoc(@Nullable DocumentReference documentReference) {
    InheritorFactory inheritorFactory = new InheritorFactory();
    return getPageLayoutForDoc(documentReference, inheritorFactory);
  }

  SpaceReference getPageLayoutForDoc(DocumentReference documentReference,
      InheritorFactory inheritorFactory) {
    long millisec = System.currentTimeMillis();
    LOGGER.debug("getPageLayoutForDoc: for [{}].", documentReference);
    if (documentReference == null) {
      return null;
    }
    SpaceReference layoutSpaceRef = null;
    if (existsLayout(documentReference.getLastSpaceReference())) {
      layoutSpaceRef = getCelLayoutEditorSpaceRef();
    } else {
      String spaceName = inheritorFactory.getPageLayoutInheritor(modelUtils.serializeRefLocal(
          documentReference), modelContext.getXWikiContext()).getStringValue("page_layout", null);
      if (spaceName != null) {
        layoutSpaceRef = modelUtils.resolveRef(spaceName, SpaceReference.class);
      }
    }
    layoutSpaceRef = resolveValidLayoutSpace(layoutSpaceRef)
        .orElseGet(this::getDefaultLayoutSpaceReference);
    LOGGER.info(
        "getPageLayoutForDoc: for [{}] returning [{}].  Time used in millisec: {}",
        documentReference, layoutSpaceRef, (System.currentTimeMillis() - millisec));
    return layoutSpaceRef;
  }

  @Override
  @Nullable
  public SpaceReference getDefaultLayoutSpaceReference() {
    SpaceReference defaultLayoutSpaceRef = RefBuilder
        .from(modelContext.getWikiRef())
        .space(getDefaultLayout())
        .build(SpaceReference.class);
    return resolveValidLayoutSpace(defaultLayoutSpaceRef).orElse(null);
  }

  @Override
  public Optional<SpaceReference> resolveValidLayoutSpace(
      @Nullable SpaceReference layoutSpaceRef) {
    if ((layoutSpaceRef != null) && !existsLayout(layoutSpaceRef)) {
      SpaceReference centralLayoutSpaceRef = RefBuilder.from(layoutSpaceRef)
          .with(CelConstant.CENTRAL_WIKI)
          .build(SpaceReference.class);
      if (!layoutSpaceRef.equals(centralLayoutSpaceRef) && existsLayout(centralLayoutSpaceRef)) {
        layoutSpaceRef = centralLayoutSpaceRef;
      } else {
        layoutSpaceRef = null;
      }
    }
    if ((layoutSpaceRef != null) && !checkLayoutAccess(layoutSpaceRef)) {
      layoutSpaceRef = null;
    }
    return Optional.ofNullable(layoutSpaceRef);
  }

  @Override
  public SpaceReference getCurrentRenderingLayout() {
    return getRenderingLayoutStack().peek();
  }

  @Override
  public boolean isLayoutEditorAvailable() {
    SpaceReference localLayoutEditorSpaceRef = getCelLayoutEditorSpaceRef();
    return resolveValidLayoutSpace(localLayoutEditorSpaceRef).isPresent();
  }

  @Override
  public boolean checkLayoutAccess(@NotNull SpaceReference layoutSpaceRef) {
    WikiReference layoutWikiRef = RefBuilder.from(layoutSpaceRef).build(WikiReference.class);
    return modelContext.getWikiRef().equals(layoutWikiRef)
        || CelConstant.CENTRAL_WIKI.equals(layoutWikiRef);
  }

  @Override
  public boolean canRenderLayout(@Nullable SpaceReference layoutSpaceRef) {
    return resolveValidLayoutSpace(layoutSpaceRef).isPresent();
  }

  @Override
  @NotNull
  public Optional<BaseObject> getLayoutPropertyObj(@Nullable SpaceReference layoutSpaceRef) {
    return resolveValidLayoutSpace(layoutSpaceRef)
        .flatMap(this::getLayoutPropDocRef)
        .flatMap(this::getLayoutPropertyBaseObject);
  }

  @NotNull
  private Optional<BaseObject> getLayoutPropertyBaseObject(
      @NotNull DocumentReference layoutPropDocRef) {
    try {
      return XWikiObjectFetcher.on(modelAccess.getDocument(layoutPropDocRef))
          .filter(PageLayoutPropertiesClass.CLASS_REF)
          .stream().findFirst();
    } catch (DocumentNotExistsException exp) {
      LOGGER.debug("Layout property doc [{}] does not exist.", layoutPropDocRef, exp);
    }
    return Optional.empty();
  }

  @Override
  public boolean isActive(@Nullable SpaceReference layoutSpaceRef) {
    return getLayoutPropertyObj(layoutSpaceRef)
        .filter(propObj -> (propObj.getIntValue("isActive", 0) > 0))
        .isPresent();
  }

  @Override
  @NotNull
  public Optional<String> getPrettyName(@Nullable SpaceReference layoutSpaceRef) {
    return getLayoutPropertyObj(layoutSpaceRef)
        .map(propObj -> propObj.getStringValue("prettyname"))
        .filter(not(Strings::isNullOrEmpty));
  }

  @Override
  @NotEmpty
  public String getLayoutType(@Nullable SpaceReference layoutSpaceRef) {
    return getLayoutPropertyObj(layoutSpaceRef)
        .map(propObj -> propObj.getStringValue(FIELD_LAYOUT_TYPE.getName()))
        .filter(not(Strings::isNullOrEmpty))
        .orElse(PAGE_LAYOUT_VALUE);
  }

  @NotNull
  @Override
  public HtmlDoctype getHTMLType(@NotNull SpaceReference layoutSpaceRef) {
    return getLayoutPropertyObj(layoutSpaceRef)
        .map(propObj -> propObj.getStringValue(FIELD_LAYOUT_DOCTYPE.getName()))
        .flatMap(docTypeStr -> HtmlDoctype.getHtmlDoctype(docTypeStr).toJavaUtil())
        .orElse(HtmlDoctype.XHTML);
  }

  @Override
  @NotNull
  public String getVersion(@Nullable SpaceReference layoutSpaceRef) {
    return getLayoutPropertyObj(layoutSpaceRef)
        .map(propObj -> propObj.getStringValue("version"))
        .orElse("");
  }

  @Override
  public boolean exportLayoutXAR(final SpaceReference layoutSpaceRef,
      boolean withDocHistory) throws XWikiException, IOException {
    final XWikiContext context = modelContext.getXWikiContext();
    PackageAPI export = ((PackageAPI) context.getWiki().getPluginApi(PACKAGEPLUGIN_NAME, context));
    export.setName(getPrettyName(layoutSpaceRef).orElse("") + "-" + getVersion(layoutSpaceRef));
    export.setWithVersions(withDocHistory);
    boolean result = modelUtils.getAllDocsForSpace(layoutSpaceRef)
        .map(rethrowFunction(documentName -> export.add(modelUtils.serializeRefLocal(documentName),
            DocumentInfo.ACTION_OVERWRITE)))
        .allMatch(Predicates.alwaysTrue());
    export.export();
    return result;
  }

  @Override
  @NotNull
  public String renderCelementsDocumentWithLayout(@NotNull DocumentReference docRef,
      @Nullable SpaceReference layoutSpaceRef) {
    checkNotNull(docRef);
    final Optional<XWikiDocument> oldContextDoc = modelContext.getDocument();
    LOGGER.debug("renderCelementsDocumentWithLayout for docRef [{}] and layoutSpaceRef [{}]"
        + " overwrite oldContextDoc [{}].", docRef, layoutSpaceRef,
        oldContextDoc.map(XWikiDocument::getDocumentReference).orElse(null));
    final XWikiContext xWikiContext = modelContext.getXWikiContext();
    final VelocityContext vcontext = (VelocityContext) xWikiContext.get("vcontext");
    try {
      final XWikiDocument newContextDoc = modelAccess.getDocument(docRef);
      xWikiContext.setDoc(newContextDoc);
      vcontext.put("doc", newContextDoc.newDocument(xWikiContext));
      return renderPageLayout(layoutSpaceRef);
    } catch (DocumentNotExistsException exp) {
      LOGGER.error("Failed to get '{}' document to renderCelementsDocumentWithLayout.", docRef,
          exp);
    } finally {
      oldContextDoc.ifPresent(oldDoc -> {
        xWikiContext.setDoc(oldDoc);
        vcontext.put("doc", oldDoc.newDocument(xWikiContext));
      });
    }
    return "";
  }

  String getDefaultLayout() {
    String defaultLayout = modelContext.getXWikiContext().getWiki().Param(
        XWIKICFG_CELEMENTS_LAYOUT_DEFAULT, SIMPLE_LAYOUT);
    final String requestAction = modelContext.getXWikiContext().getAction();
    if ((requestAction != null) && !"view".equals(requestAction)) {
      defaultLayout = modelContext.getXWikiContext().getWiki()
          .Param(XWIKICFG_CELEMENTS_LAYOUT_DEFAULT + "." + requestAction, defaultLayout);
      LOGGER.debug("getDefaultLayout for action [{}] got [{}].", requestAction, defaultLayout);
    } else {
      LOGGER.debug("getDefaultLayout got [{}].", defaultLayout);
    }
    return defaultLayout;
  }

  @NotNull
  SpaceReference getCelLayoutEditorSpaceRef() {
    return RefBuilder.from(modelContext.getWikiRef())
        .space(CEL_LAYOUT_EDITOR_PL_NAME)
        .build(SpaceReference.class);
  }

  private String getDocType() {
    return cfgSrc.getProperty("celements.layout.docType", HtmlDoctype.HTML5.getValue());
  }

  private Deque<SpaceReference> getRenderingLayoutStack() {
    return modelUtils.computeExecPropIfAbsent(CEL_RENDERING_LAYOUT_STACK_PROPERTY, LinkedList::new);
  }
}
