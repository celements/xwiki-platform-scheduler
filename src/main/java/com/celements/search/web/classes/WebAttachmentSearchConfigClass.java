package com.celements.search.web.classes;

import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.list.CustomListField;
import com.celements.model.classes.fields.list.StringListField;
import com.google.common.net.MediaType;

@Immutable
@Singleton
@Component(WebAttachmentSearchConfigClass.CLASS_DEF_HINT)
public class WebAttachmentSearchConfigClass extends AbstractClassDefinition implements
    WebSearchClassDefinition {

  public static final String SPACE_NAME = "Celements2";
  public static final String DOC_NAME = "WebAttachmentSearchConfigClass";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;

  public static ClassField<List<MediaType>> FIELD_MIMETYPES = new CustomListField.Builder<>(
      CLASS_DEF_HINT, "mimeTypes", new MediaTypeMarshaller()).multiSelect(true).build();

  public static ClassField<List<MediaType>> FIELD_MIMETYPES_BLACK_LIST = new CustomListField.Builder<>(
      CLASS_DEF_HINT, "mimeTypesBlackList", new MediaTypeMarshaller()).multiSelect(true).build();

  public static ClassField<List<String>> FIELD_FILENAME_PREFIXES = new StringListField.Builder(
      CLASS_DEF_HINT, "fileNamePrefixes").multiSelect(true).build();

  @Override
  public String getName() {
    return CLASS_DEF_HINT;
  }

  @Override
  public boolean isInternalMapping() {
    return false;
  }

  @Override
  protected String getClassSpaceName() {
    return SPACE_NAME;
  }

  @Override
  protected String getClassDocName() {
    return DOC_NAME;
  }

}
