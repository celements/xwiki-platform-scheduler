package com.celements.search.lucene.query;

public class LuceneQuery extends QueryRestrictionGroup {

  private static final long serialVersionUID = 20140913181251L;
  
  private String database;

  public LuceneQuery(String database) {
    super(Type.AND);
    this.database = database;
    this.add(new QueryRestriction("wiki", "\"" + database + "\""));
  }
  
  private LuceneQuery() {
    super(Type.AND);
  }
  
  public String getDatabase() {
    return database;
  }

  @Override
  public LuceneQuery copy() {
    LuceneQuery copy  = new LuceneQuery();
    copy.database = database;
    copy.addAll(super.copy());
    return copy;
  }
  
  @Override
  public String toString() {
    return "LuceneQuery [database=" + database + ", queryString=" + getQueryString() + "]";
  }
  
}
