/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.layout;

import static com.google.common.collect.ImmutableList.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.script.service.ScriptService;

import com.celements.execution.XWikiExecutionProp;
import com.celements.model.util.ModelUtils;
import com.celements.pagelayout.LayoutServiceRole;
import com.celements.rights.access.EAccessLevel;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.celements.web.plugin.api.PageLayoutApi;
import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.xpn.xwiki.XWikiContext;

@Component("layout")
public class LayoutScriptService implements ScriptService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LayoutScriptService.class);

  public static final String CELEMENTS_PAGE_LAYOUT_COMMAND = "com.celements.web.PageLayoutCommand";

  private final LayoutServiceRole layoutService;
  private final ModelUtils modelUtils;
  private final IRightsAccessFacadeRole rightsAccess;
  private final Execution excecution;

  @Inject
  public LayoutScriptService(
      LayoutServiceRole layoutService,
      ModelUtils modelUtils,
      IRightsAccessFacadeRole rightsAccess,
      Execution excecution) {
    this.layoutService = layoutService;
    this.modelUtils = modelUtils;
    this.rightsAccess = rightsAccess;
    this.excecution = excecution;
  }

  /**
   * @deprecated instead use {@link #renderLayout()})
   */
  @Deprecated(since = "6.10", forRemoval = true)
  @NotNull
  public String renderPageLayout() {
    return layoutService.renderLayout();
  }

  @NotNull
  public String renderLayout() {
    return layoutService.renderLayout();
  }

  /**
   * @deprecated instead use {@link #renderLayout(SpaceReference)})
   */
  @Deprecated(since = "6.10", forRemoval = true)
  @NotNull
  public String renderPageLayout(@Nullable SpaceReference spaceRef) {
    return layoutService.renderLayout(spaceRef);
  }

  @NotNull
  public String renderLayout(@Nullable SpaceReference spaceRef) {
    return layoutService.renderLayout(spaceRef);
  }

  /**
   * @deprecated since 5.4 instead use {@link #getActivePageLayoutSpaceRefs()}
   */
  @Deprecated
  public Map<String, String> getActivePageLayouts() {
    return getPageLayoutCmd().getActivePageLyouts();
  }

  /**
   * @deprecated since 5.4 instead use {@link #getAllPageLayoutSpaceRefs()}
   */
  @Deprecated
  public Map<String, String> getAllPageLayouts() {
    return getPageLayoutCmd().getAllPageLayouts();
  }

  @NotNull
  public Map<SpaceReference, String> getActivePageLayoutSpaceRefs() {
    return layoutService.getActivePageLayouts();
  }

  @NotNull
  public Map<SpaceReference, String> getAllPageLayoutSpaceRefs() {
    return layoutService.getAllPageLayouts();
  }

  @NotNull
  public List<PageLayoutApi> getActiveLayouts() {
    return layoutService.streamAllLayoutsSpaces()
        .filter(layoutService::isActive)
        .map(PageLayoutApi::new)
        .collect(toImmutableList());
  }

  @NotNull
  public List<PageLayoutApi> getAllLayouts() {
    return layoutService.streamAllLayoutsSpaces()
        .map(PageLayoutApi::new)
        .collect(toImmutableList());
  }

  @NotNull
  public List<PageLayoutApi> getLocalLayouts() {
    return layoutService.streamLayoutsSpaces(getWikiRef())
        .map(PageLayoutApi::new)
        .collect(toImmutableList());
  }

  /**
   * @deprecated since 5.4 instead use {@link #createLayout(SpaceReference)}
   */
  @Deprecated
  @NotNull
  public String createNewLayout(@Nullable String layoutSpaceName) {
    SpaceReference layoutSpaceRef = Optional
        .ofNullable(modelUtils.resolveRef(layoutSpaceName, SpaceReference.class)).orElse(null);
    return createLayout(layoutSpaceRef);
  }

  @NotNull
  public String createLayout(@Nullable SpaceReference layoutSpaceRef) {
    if ((layoutSpaceRef != null) && rightsAccess.hasAccessLevel(layoutSpaceRef,
        EAccessLevel.EDIT)) {
      if (layoutService.createLayout(layoutSpaceRef)) {
        return "cel_layout_create_successful";
      } else {
        return "cel_layout_empty_name_msg";
      }
    }
    return "cel_layout_no_rights_or_empty_msg";
  }

  /**
   * @deprecated since 5.4 instead use {@link #deleteLayout(SpaceReference)}
   */
  @Deprecated
  public boolean deleteLayout(@Nullable String layoutSpaceName) {
    SpaceReference layoutSpaceRef = modelUtils.resolveRef(layoutSpaceName, SpaceReference.class);
    return deleteLayout(layoutSpaceRef);
  }

  public boolean deleteLayout(@Nullable SpaceReference layoutSpaceRef) {
    Optional<DocumentReference> layoutPropDocRef = layoutService
        .getLayoutPropDocRef(layoutSpaceRef);
    if (layoutPropDocRef.isPresent()
        && rightsAccess.hasAccessLevel(layoutPropDocRef.get(), EAccessLevel.DELETE)) {
      // layoutPropDocRef is Optional.empty for layoutSpaceRef == null
      return layoutService.deleteLayout(layoutSpaceRef);
    } else {
      LOGGER.warn("NO delete rights on [{}] for user [{}].", layoutPropDocRef.orElse(null),
          getContext().getUser());
    }
    return false;
  }

  public PageLayoutApi getPageLayoutApiForRef(@Nullable SpaceReference layoutSpaceRef) {
    return layoutService.resolveValidLayoutSpace(layoutSpaceRef)
        .map(PageLayoutApi::new)
        .orElse(null);
  }

  /**
   * getPageLayouApiForDocRef computes the layoutSpaceRef rendered for the given docRef
   *
   * @return PageLayoutApi for the layoutSpaceRef computed
   */
  public PageLayoutApi getPageLayoutApiForDocRef(@Nullable DocumentReference docRef) {
    SpaceReference pageLayoutForDoc = layoutService.getPageLayoutForDoc(docRef);
    if (pageLayoutForDoc != null) {
      return new PageLayoutApi(pageLayoutForDoc);
    }
    return null;
  }

  /**
   * @deprecated since 2.82 instead use {@link #getPageLayoutApiForRef(SpaceReference)}
   *             since 2.86 : or {@link #getPageLayoutApiForDocRef(DocumentReference)}
   */
  @Deprecated
  public PageLayoutApi getPageLayoutApiForName(@Nullable String layoutSpaceName) {
    if (layoutSpaceName != null) {
      return new PageLayoutApi(modelUtils.resolveRef(layoutSpaceName, SpaceReference.class));
    }
    return null;
  }

  public String getPageLayoutForDoc(@Nullable DocumentReference docRef) {
    SpaceReference pageLayoutForDoc = layoutService.getPageLayoutForDoc(docRef);
    if (pageLayoutForDoc != null) {
      return pageLayoutForDoc.getName();
    }
    return "";
  }

  public boolean layoutExists(@Nullable SpaceReference layoutSpaceRef) {
    return layoutService.existsLayout(layoutSpaceRef);
  }

  public boolean canRenderLayout(@Nullable SpaceReference spaceRef) {
    return layoutService.canRenderLayout(spaceRef);
  }

  public boolean useXWikiLoginLayout() {
    return "1".equals(getContext().getWiki().getSpacePreference("xwikiLoginLayout",
        "celements.xwikiLoginLayout", "1", getContext()));
  }

  public boolean layoutEditorAvailable() {
    return layoutService.isLayoutEditorAvailable();
  }

  public String renderCelementsDocumentWithLayout(@Nullable DocumentReference docRef,
      @Nullable SpaceReference layoutSpaceRef) {
    if (docRef != null) {
      return layoutService.renderCelementsDocumentWithLayout(docRef, layoutSpaceRef);
    }
    return "";
  }

  public SpaceReference getCurrentRenderingLayout() {
    return layoutService.getCurrentRenderingLayout();
  }

  @Deprecated
  private PageLayoutCommand getPageLayoutCmd() {
    if (!getContext().containsKey(CELEMENTS_PAGE_LAYOUT_COMMAND)) {
      getContext().put(CELEMENTS_PAGE_LAYOUT_COMMAND, new PageLayoutCommand());
    }
    return (PageLayoutCommand) getContext().get(CELEMENTS_PAGE_LAYOUT_COMMAND);
  }

  private XWikiContext getContext() {
    return excecution.getContext().get(XWikiExecutionProp.XWIKI_CONTEXT).orElseThrow();
  }

  private WikiReference getWikiRef() {
    return excecution.getContext().get(XWikiExecutionProp.WIKI).orElseThrow();
  }

}
