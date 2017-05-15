package com.celements.search.web.classes;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

@ComponentRole
public interface IWebSearchClassConfig {

  public static final String CLASS_SPACE = "Celements2";
  public static final String CLASS_WEB_SEARCH_CONFIG_NAME = "WebSearchConfigClass";
  public static final String CLASS_WEB_ATTACHMENT_SEARCH_CONFIG_NAME = "WebAttachmentSearchConfigClass";

  public static final String PROPERTY_PACKAGES = "packages";
  public static final String PROPERTY_LINKED_DOCS_ONLY = "linkedDocsOnly";
  public static final String PROPERTY_FUZZY_SEARCH = "fuzzySearch";
  public static final String PROPERTY_DOCS = "docs";
  public static final String PROPERTY_DOCS_BLACK_LIST = "docsBlackList";
  public static final String PROPERTY_SPACES = "spaces";
  public static final String PROPERTY_SPACES_BLACK_LIST = "spacesBlackList";
  public static final String PROPERTY_PAGETYPES = "pageTypes";
  public static final String PROPERTY_PAGETYPES_BLACK_LIST = "pageTypesBlackList";
  public static final String PROPERTY_SORT_FIELDS = "sortFields";
  public static final String PROPERTY_RESULT_ITEM_RENDER_SCRIPT = "resultItemRenderScript";

  public static final String PROPERTY_MIMETYPES = "mimeTypes";
  public static final String PROPERTY_MIMETYPES_BLACK_LIST = "mimeTypesBlackList";
  public static final String PROPERTY_FILENAME_PREFIXES = "fileNamePrefixes";

  @NotNull
  public DocumentReference getWebSearchConfigClassRef();

  @NotNull
  public DocumentReference getWebSearchConfigClassRef(@Nullable WikiReference wikiRef);

  @NotNull
  public DocumentReference getWebAttachmentSearchConfigClassRef();

  @NotNull
  public DocumentReference getWebAttachmentSearchConfigClassRef(@Nullable WikiReference wikiRef);

}
