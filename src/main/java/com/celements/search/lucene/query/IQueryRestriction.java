package com.celements.search.lucene.query;

public interface IQueryRestriction {

  public String getQueryString();

  public boolean getNegate();

  public IQueryRestriction setNegate(boolean negate);

  public IQueryRestriction copy();

  public boolean isEmpty();

}
