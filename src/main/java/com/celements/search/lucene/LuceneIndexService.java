package com.celements.search.lucene;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.plugin.lucene.LucenePlugin;

@Component
public class LuceneIndexService implements ILuceneIndexService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LuceneIndexService.class);

  @Requirement
  private IModelAccessFacade modelAccess;
  
  @Requirement
  private IWebUtilsService webUtils;

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty(
        XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  @Override
  public void queueForIndexing(DocumentReference docRef) throws DocumentLoadException,
      DocumentNotExistsException {
    XWikiDocument doc = modelAccess.getDocument(docRef);
    queueForIndexing(doc);
  }

  @Override
  public void queueForIndexing(XWikiDocument doc) {
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("adding to index queue '{}'", webUtils.serializeRef(
          doc.getDocumentReference()));
    }
    getLucenePlugin().queueDocument(doc, getContext());
    getLucenePlugin().queueAttachment(doc, getContext());
  }

  @Override
  public int rebuildIndexForAllWikis() {
    return getLucenePlugin().startIndex(null, "", false, false, getContext());
  }

  @Override
  public int rebuildIndex(WikiReference wikiRef) {
    return rebuildIndex(Arrays.asList(wikiRef));
  }

  @Override
  public int rebuildIndex(Collection<WikiReference> wikiRefs) {
    List<String> wikis = new ArrayList<>();
    if (wikiRefs != null) {
      for (WikiReference wikiRef : wikiRefs) {
        if (wikiRef != null) {
          wikis.add(wikiRef.getName());
        }
      }
    }
    return getLucenePlugin().startIndex(wikis, "", false, false, getContext());
  }

  private LucenePlugin getLucenePlugin() {
    return (LucenePlugin) getContext().getWiki().getPlugin("lucene", getContext());
  }

}
