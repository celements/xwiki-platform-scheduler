package com.celements.search.lucene;

import java.util.Collection;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.access.exception.DocumentLoadException;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface ILuceneIndexService {

  public void queueForIndexing(DocumentReference docRef) throws DocumentLoadException,
      DocumentNotExistsException;

  public void queueForIndexing(XWikiDocument doc);

  public boolean rebuildIndexForAllWikis();

  public boolean rebuildIndexForAllWikis(Optional<EntityReference> entityRef);

  public boolean rebuildIndex(Collection<WikiReference> wikiRefs);

  public boolean rebuildIndex(Collection<WikiReference> wikiRefs,
      Optional<EntityReference> entityRef);

  public void optimizeIndex();

}
