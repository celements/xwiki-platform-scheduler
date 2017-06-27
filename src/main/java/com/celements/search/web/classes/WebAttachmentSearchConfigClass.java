package com.celements.search.web.classes;

import java.util.List;

import org.xwiki.component.annotation.Component;

import com.celements.marshalling.AbstractMarshaller;
import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.list.CustomListField;
import com.celements.model.classes.fields.list.StringListField;
import com.google.common.base.Optional;
import com.google.common.net.MediaType;

@Component(WebAttachmentSearchConfigClass.CLASS_DEF_HINT)
public class WebAttachmentSearchConfigClass extends AbstractClassDefinition {

  public static final String SPACE_NAME = "Celements2";
  public static final String DOC_NAME = "WebAttachmentSearchConfigClass";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;

  public ClassField<List<MediaType>> FIELD_MIMETYPES = new CustomListField.Builder<>(CLASS_DEF_HINT,
      "mimeTypes", new MediaTypeMarshaller()).build();

  public ClassField<List<MediaType>> FIELD_MIMETYPES_BLACK_LIST = new CustomListField.Builder<>(
      CLASS_DEF_HINT, "mimeTypesBlackList", new MediaTypeMarshaller()).build();

  public ClassField<List<String>> FIELD_FILENAME_PREFIXES = new StringListField.Builder(
      CLASS_DEF_HINT, "fileNamePrefixes").build();

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

  private class MediaTypeMarshaller extends AbstractMarshaller<MediaType> {

    public MediaTypeMarshaller() {
      super(MediaType.class);
    }

    @Override
    public Object serialize(MediaType val) {
      return val.toString();
    }

    @Override
    public Optional<MediaType> resolve(Object val) {
      MediaType mediaType = null;
      try {
        mediaType = MediaType.parse(val.toString());
      } catch (IllegalArgumentException exc) {
        LOGGER.info("failed to resolve '{}' for '{}'", val, getToken(), exc);
      }
      return Optional.fromNullable(mediaType);
    }

  }

}
