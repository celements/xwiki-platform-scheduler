package com.celements.search.lucene;

import java.util.Collection;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface ILuceneIndexService {

  public void queueForIndexing(DocumentReference docRef)
      throws DocumentLoadException, DocumentNotExistsException;

  public void queueForIndexing(XWikiDocument doc);

  public int rebuildIndexForAllWikis();

  public int rebuildIndex(WikiReference wikiRef);

  public int rebuildIndex(Collection<WikiReference> wikiRefs);

}
