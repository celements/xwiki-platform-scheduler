package com.celements.search.lucene.query;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang3.ObjectUtils;

public class LuceneQuery extends QueryRestrictionGroup {

  private static final long serialVersionUID = 20140913181251L;
  
  private String database;

  public LuceneQuery(String database) {
    super(Type.AND);
    this.database = database;
  }
  
  public String getDatabase() {
    return database;
  }

  @Override
  public String getQueryString() {
    String ret = super.getQueryString();
    if (ret.length() > 0) {
      ret += " ";
    }
    ret += "wiki:" + database;
    return ret;
  }

  @Override
  public LuceneQuery copy() {
    LuceneQuery copy  = new LuceneQuery(database);
    addAll(super.copy());
    return copy;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(super.hashCode()).append(database).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof LuceneQuery) {
      LuceneQuery other = (LuceneQuery) obj;
      return super.equals(obj) && ObjectUtils.equals(database, other.database);
    } else {
      return false;
    }
  }
  
  @Override
  public String toString() {
    return "LuceneQuery [queryString=" + getQueryString() + "]";
  }
  
}
