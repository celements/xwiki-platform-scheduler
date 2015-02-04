package com.celements.search.lucene.query;

import java.util.ArrayList;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.StringUtils;

public class QueryRestrictionGroup extends ArrayList<IQueryRestriction> implements IQueryRestriction {
  
  private static final long serialVersionUID = 20140913164350L;
  
  public static enum Type {
    AND, OR;
  }

  private boolean negate = false;
  private final Type type;
  
  public QueryRestrictionGroup(Type type) {
    this.type = type;
  }
  
  @Override
  public boolean add(IQueryRestriction restr) {
    if ((restr != null) && !restr.isEmpty() && !this.contains(restr)) {
      return super.add(restr);
    }
    return false;
  }
  
  @Override
  public void add(int index, IQueryRestriction restr) {
    if ((restr != null) && !restr.isEmpty()) {
      super.add(index, restr);
    }
  }

  @Override
  public QueryRestrictionGroup setNegate(boolean negate) {
    this.negate = negate;
    return this;
  }
  
  public Type getType() {
    return type;
  }

  @Override
  public String getQueryString() {
    String ret = "";
    for (IQueryRestriction restr : this) {
      String restrString;
      if ((restr != null) && StringUtils.isNotBlank(restrString = restr.getQueryString())) {
        if (ret.length() > 0) {
          ret += " " + type + " ";
        }
        ret += restrString;
      }
    }
    if ((ret.length() > 0) && (this.size() > 1)) {
      ret = "(" + ret + ")";
      if (negate) {
        ret = "NOT " + ret;
      }
    }
    return ret;
  }

  @Override
  public QueryRestrictionGroup copy() {
    QueryRestrictionGroup copy  = new QueryRestrictionGroup(type);
    for (IQueryRestriction restr : this) {
      copy.add(restr.copy());
    }
    return copy;
  }

  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(super.hashCode()).append(type).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof QueryRestrictionGroup) {
      QueryRestrictionGroup other = (QueryRestrictionGroup) obj;
      return super.equals(obj) && (type == other.type);
    } else {
      return false;
    }
  }
  
  @Override
  public String toString() {
    return "QueryRestrictionGroup [queryString=" + getQueryString() + "]";
  }

}
