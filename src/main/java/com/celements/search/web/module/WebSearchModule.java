package com.celements.search.web.module;

import java.util.Set;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.search.lucene.query.IQueryRestriction;
import com.celements.search.lucene.query.LuceneDocType;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface WebSearchModule {

  @NotNull
  public String getName();

  public boolean isDefault();

  public boolean isRequired(@NotNull XWikiDocument cfgDoc);

  @NotNull
  public Set<LuceneDocType> getDocTypes();

  @NotNull
  public IQueryRestriction getQueryRestriction(@NotNull XWikiDocument cfgDoc,
      @NotNull String searchTerm);

  @NotNull
  public Optional<DocumentReference> getLinkedClassRef();

}
