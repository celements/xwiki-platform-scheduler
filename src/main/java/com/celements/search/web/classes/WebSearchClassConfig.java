package com.celements.search.web.classes;

import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.web.service.IWebUtilsService;

public class WebSearchClassConfig implements IWebSearchClassConfig {

  @Requirement
  private IWebUtilsService webUtils;

  @Override
  public DocumentReference getWebSearchConfigClassRef(WikiReference wikiRef) {
    return new DocumentReference(CLASS_WEB_SEARCH_CONFIG_NAME, getSpaceRef(wikiRef));
  }

  @Override
  public DocumentReference getWebAttachmentSearchConfigClassRef(WikiReference wikiRef) {
    return new DocumentReference(CLASS_WEB_ATTACHMENT_SEARCH_CONFIG_NAME, getSpaceRef(
        wikiRef));
  }

  private SpaceReference getSpaceRef(WikiReference wikiRef) {
    return webUtils.resolveSpaceReference(CLASS_SPACE, wikiRef);
  }

}
