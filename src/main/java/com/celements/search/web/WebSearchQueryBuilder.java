package com.celements.search.web;

import java.util.Collection;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.web.packages.WebSearchPackage;

@ComponentRole
public interface WebSearchQueryBuilder {

  @NotNull
  public DocumentReference getConfigDocRef();

  @NotNull
  public WebSearchQueryBuilder setConfigDoc(@NotNull DocumentReference docRef)
      throws DocumentNotExistsException;

  @NotNull
  public String getSearchTerm();

  @NotNull
  public WebSearchQueryBuilder setSearchTerm(@NotNull String searchTerm);

  @NotNull
  public Collection<WebSearchPackage> getPackages();

  @NotNull
  public WebSearchQueryBuilder addPackage(@NotNull WebSearchPackage searchPackage);

  @NotNull
  public LuceneQuery build();

}
