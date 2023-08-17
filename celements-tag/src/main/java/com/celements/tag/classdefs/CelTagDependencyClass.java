package com.celements.tag.classdefs;

import org.springframework.stereotype.Component;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.ref.DocumentReferenceField;
import com.celements.web.classes.CelementsClassDefinition;

@Component(CelTagDependencyClass.CLASS_DEF_HINT)
public class CelTagDependencyClass extends AbstractClassDefinition implements CelTagClassRole {

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
