package com.celements.tag.classdefs;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.ref.DocumentReferenceField;
import com.celements.web.classes.CelementsClassDefinition;
import com.google.errorprone.annotations.Immutable;

@Singleton
@Immutable
@Component(CelTagDependencyClass.CLASS_DEF_HINT)
public class CelTagDependencyClass extends AbstractClassDefinition
    implements CelementsClassDefinition { // TODO define own class package

  public static final String DOC_NAME = "CelTagDependencyClass";
  public static final String CLASS_DEF_HINT = CelementsClassDefinition.SPACE_NAME + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE_NAME, DOC_NAME);

  public static final ClassField<DocumentReference> FIELD_REFERENCE = new DocumentReferenceField.Builder(
      CLASS_REF, "reference").build();

  public CelTagDependencyClass() {
    super(CLASS_REF);
  }

  @Override
  public boolean isInternalMapping() {
    return true;
  }

}
