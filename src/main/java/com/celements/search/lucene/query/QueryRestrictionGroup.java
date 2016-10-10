package com.celements.search.lucene.query;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Optional;

public class QueryRestrictionGroup extends ArrayList<IQueryRestriction> implements
    IQueryRestriction {

  private static final long serialVersionUID = 20140913164350L;

  public static enum Type {
    AND, OR;
  }

  private boolean negate = false;
  private Type type;

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
    if ((restr != null) && !restr.isEmpty() && !this.contains(restr)) {
      super.add(index, restr);
    }
  }

  @Override
  public boolean getNegate() {
    return negate;
  }

  @Override
  public QueryRestrictionGroup setNegate(boolean negate) {
    this.negate = negate;
    return this;
  }

  @Override
  public Optional<Float> getFuzzy() {
    return Optional.absent();
  }

  @Override
  public IQueryRestriction setFuzzy(Float fuzzy) {
    for (IQueryRestriction restr : this) {
      if (!restr.getFuzzy().isPresent()) {
        restr.setFuzzy(fuzzy);
      }
    }
    return this;
  }

  public Type getType() {
    return type;
  }

  public QueryRestrictionGroup setType(Type type) {
    this.type = type;
    return this;
  }

  @Override
  public String getQueryString() {
    QueryRestrictionGroup copy = this.copy().cleanup();
    String ret = "";
    for (IQueryRestriction restr : copy) {
      String queryStr = restr.getQueryString();
      if (StringUtils.isNotBlank(queryStr)) {
        if (!ret.isEmpty()) {
          ret += " " + copy.getType() + " ";
        }
        ret += queryStr.trim();
      }
    }
    if (!ret.isEmpty()) {
      if (copy.size() > 1) {
        ret = "(" + ret + ")";
      }
      if (copy.getNegate()) {
        ret = "NOT " + ret;
      }
    }
    return ret;
  }

  /**
   * cleans up negated expressions so that lucene can handle it, e.g.:<br>
   * !A && !B -> !(A || B)<br>
   * !A || !B -> !(A && B)
   */
  public QueryRestrictionGroup cleanup() {
    boolean onlyNegated = this.size() > 1;
    for (IQueryRestriction restr : this) {
      onlyNegated &= restr.getNegate();
    }
    if (onlyNegated) {
      for (IQueryRestriction restr : this) {
        restr.setNegate(false);
      }
      this.setType(this.getType() == Type.AND ? Type.OR : Type.AND);
      this.setNegate(!this.getNegate());
    }
    return this;
  }

  @Override
  public QueryRestrictionGroup copy() {
    QueryRestrictionGroup copy = new QueryRestrictionGroup(this.getType());
    for (IQueryRestriction restr : this) {
      copy.add(restr.copy());
    }
    copy.setNegate(this.getNegate());
    return copy;
  }

  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(super.hashCode()).append(this.getType()).append(
        this.getNegate()).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof QueryRestrictionGroup) {
      QueryRestrictionGroup other = (QueryRestrictionGroup) obj;
      return super.equals(obj) && new EqualsBuilder().append(this.getType(),
          other.getType()).append(this.getNegate(), other.getNegate()).isEquals();
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return "QueryRestrictionGroup [queryString=" + getQueryString() + "]";
  }

}
