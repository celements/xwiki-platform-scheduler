package com.celements.search.lucene.query;

public interface IQueryRestriction {
  
  public String getQueryString();
  
  public IQueryRestriction copy();

}
