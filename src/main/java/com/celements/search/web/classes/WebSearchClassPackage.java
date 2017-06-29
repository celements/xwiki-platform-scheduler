package com.celements.search.web.classes;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.model.classes.AbstractClassPackage;
import com.celements.model.classes.ClassDefinition;

@Component(WebSearchClassPackage.NAME)
public class WebSearchClassPackage extends AbstractClassPackage {

  public static final String NAME = "websearch";

  @Requirement
  private List<WebSearchClassDefinition> classDefs;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<? extends ClassDefinition> getClassDefinitions() {
    return new ArrayList<>(classDefs);
  }

}
