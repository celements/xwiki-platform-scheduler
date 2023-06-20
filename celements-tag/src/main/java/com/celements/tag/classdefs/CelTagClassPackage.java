package com.celements.tag.classdefs;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.celements.model.classes.AbstractClassPackage;
import com.celements.model.classes.ClassDefinition;

@Component
public class CelTagClassPackage extends AbstractClassPackage {

  public static final String NAME = "celtag";

  private final List<CelTagClassRole> classDefs;

  @Inject
  public CelTagClassPackage(List<CelTagClassRole> classDefs) {
    this.classDefs = List.copyOf(classDefs);
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<? extends ClassDefinition> getClassDefinitions() {
    return classDefs;
  }

}
