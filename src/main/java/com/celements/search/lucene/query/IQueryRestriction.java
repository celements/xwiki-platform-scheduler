package com.celements.search.lucene.query;

import javax.annotation.Nullable;

import com.google.common.base.Optional;

public interface IQueryRestriction {

  public String getQueryString();

  public boolean getNegate();

  public IQueryRestriction setNegate(boolean negate);

  public Optional<Float> getFuzzy();

  public IQueryRestriction setFuzzy(@Nullable Float fuzzy);

  public IQueryRestriction copy();

  public boolean isEmpty();

}
