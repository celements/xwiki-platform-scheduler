package com.celements.tag;

import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.celements.pagetype.category.IPageTypeCategoryRole;
import com.celements.pagetype.java.AbstractJavaPageType;
import com.google.common.collect.Sets;

@Component(CelTagPageType.NAME)
public class CelTagPageType extends AbstractJavaPageType {

  public static final String NAME = "CelTag";

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

  @Override
  public String getRenderTemplateForRenderMode(String renderMode) {
    return "";
  }

  @Override
  public boolean useInlineEditorMode() {
    return true;
  }

}
