package com.celements.tag;

import java.util.Set;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Component;

import com.celements.pagetype.category.IPageTypeCategoryRole;
import com.celements.pagetype.java.AbstractJavaPageType;
import com.google.common.collect.Sets;

@Component(CelTagPageType.NAME)
public class CelTagPageType extends AbstractJavaPageType {

  public static final String NAME = "CelTag";

  static final String VIEW_TEMPLATE_NAME = "CelTagView";

  static final String EDIT_TEMPLATE_NAME = "CelTagEdit";

  private final IPageTypeCategoryRole pageTypeCategory;

  @Inject
  public CelTagPageType(IPageTypeCategoryRole pageTypeCategory) {
    super();
    this.pageTypeCategory = pageTypeCategory;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean displayInFrameLayout() {
    return true;
  }

  @Override
  public Set<IPageTypeCategoryRole> getCategories() {
    return Sets.newHashSet(pageTypeCategory);
  }

  @Override
  public boolean hasPageTitle() {
    return false;
  }

  @Override
  public boolean isUnconnectedParent() {
    return false;
  }

  @Override
  public boolean isVisible() {
    return true;
  }

  String getViewTemplateName() {
    return VIEW_TEMPLATE_NAME;
  }

  String getEditTemplateName() {
    return EDIT_TEMPLATE_NAME;
  }

  @Override
  public @NotNull String getRenderTemplateForRenderMode(String renderMode) {
    if ("edit".equals(renderMode)) {
      return getEditTemplateName();
    } else {
      return getViewTemplateName();
    }
  }

  @Override
  public boolean useInlineEditorMode() {
    return true;
  }

}
