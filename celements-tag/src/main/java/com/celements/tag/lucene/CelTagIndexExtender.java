package com.celements.tag.lucene;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.celements.model.access.IModelAccessFacade;
import com.celements.tag.CelTagService;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.lucene.AbstractIndexData;
import com.xpn.xwiki.plugin.lucene.DocumentData;
import com.xpn.xwiki.plugin.lucene.indexExtension.ILuceneIndexExtender;
import com.xpn.xwiki.plugin.lucene.indexExtension.IndexExtensionField;
import com.xpn.xwiki.plugin.lucene.indexExtension.IndexExtensionField.ExtensionType;

@Component
public class CelTagIndexExtender implements ILuceneIndexExtender {

  public static final String INDEX_FIELD = "celtags";

  private final CelTagService tagService;
  private final IModelAccessFacade modelAccess;

  @Inject
  public CelTagIndexExtender(CelTagService tagService, IModelAccessFacade modelAccess) {
    this.tagService = tagService;
    this.modelAccess = modelAccess;
  }

  @Override
  public String getName() {
    return INDEX_FIELD;
  }

  @Override
  public boolean isEligibleIndexData(AbstractIndexData data) {
    return data instanceof DocumentData;
  }

  @Override
  public Collection<IndexExtensionField> getExtensionFields(AbstractIndexData data) {
    DocumentData docData = (DocumentData) data;
    XWikiDocument doc = modelAccess.getOrCreateDocument(docData.getDocumentReference());
    return tagService.getDocTags(doc)
        .map(tag -> new IndexExtensionField.Builder(INDEX_FIELD + "_" + tag.getType())
            .extensionType(ExtensionType.ADD)
            .value(tag.getName())
            .build())
        .collect(Collectors.toList());
  }

}
