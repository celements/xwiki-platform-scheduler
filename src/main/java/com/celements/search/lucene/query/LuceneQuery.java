package com.celements.search.lucene.query;

import java.util.List;

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
  
  /**
   * @deprecated use {@link #add} directly
   * 
   * @param restriction
   * @return
   */
  @Deprecated
  public LuceneQuery addRestriction(IQueryRestriction restriction) {
    if (restriction != null) {
      this.add(restriction);
    }
    return this;
  }

  /**
   * @deprecated use {@link addAll} directly
   * 
   * @param restriction
   * @return
   */
  @Deprecated
  public LuceneQuery addRestrictionList(List<IQueryRestriction> restrictionList) {
    if (restrictionList != null) {
      addAll(restrictionList);
    }
    return this;
  }

  /**
   * @deprecated use {@link QueryRestrictionGroup}
   * 
   * @param restriction
   * @return
   */
  @Deprecated
  public LuceneQuery addOrRestrictionList(List<IQueryRestriction> restrictionList) {
    if (restrictionList != null) {
      QueryRestrictionGroup orRestrGrp = new QueryRestrictionGroup(Type.OR);
      orRestrGrp.addAll(restrictionList);
      this.add(orRestrGrp);
    }
    return this;
  }
  
  @Override
  public String toString() {
    return "LuceneQuery [database=" + database + ", queryString=" + getQueryString() + "]";
  }
  
}
