package com.celements.search.web.classes;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.util.ModelUtils;

@Component
@Deprecated
public class WebSearchClassConfig implements IWebSearchClassConfig {

  @Requirement
  private ModelUtils modelUtils;

  @Override
  public DocumentReference getWebSearchConfigClassRef() {
    return getWebSearchConfigClassRef(null);
  }

  @Override
  public DocumentReference getWebSearchConfigClassRef(WikiReference wikiRef) {
    return new DocumentReference(CLASS_WEB_SEARCH_CONFIG_NAME, getSpaceRef(wikiRef));
  }

  @Override
  public DocumentReference getWebAttachmentSearchConfigClassRef() {
    return getWebAttachmentSearchConfigClassRef(null);
  }

  @Override
  public DocumentReference getWebAttachmentSearchConfigClassRef(WikiReference wikiRef) {
    return new DocumentReference(CLASS_WEB_ATTACHMENT_SEARCH_CONFIG_NAME, getSpaceRef(wikiRef));
  }

  private SpaceReference getSpaceRef(WikiReference wikiRef) {
    return modelUtils.resolveRef(CLASS_SPACE, SpaceReference.class, wikiRef);
  }

}
