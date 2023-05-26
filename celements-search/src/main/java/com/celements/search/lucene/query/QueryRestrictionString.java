package com.celements.search.lucene.query;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

public class QueryRestrictionString implements IQueryRestriction {

  private boolean negate = false;
  private String queryString;

  public QueryRestrictionString(String queryString) {
    this.queryString = Strings.nullToEmpty(queryString);
  }

  @Override
  public boolean getNegate() {
    return negate;
  }

  @Override
  public QueryRestrictionString setNegate(boolean negate) {
    this.negate = negate;
    return this;
  }

  @Override
  public Optional<Float> getFuzzy() {
    return Optional.of(1f);
  }

  @Override
  public IQueryRestriction setFuzzy(Float fuzzy) {
    throw new UnsupportedOperationException();
  }

  public QueryRestrictionString setQueryString(String queryString) {
    this.queryString = queryString;
    return this;
  }

  @Override
  public String getQueryString() {
    return (negate ? "NOT " : "") + "(" + queryString + ")";
  }

  @Override
  public boolean isEmpty() {
    return StringUtils.isBlank(queryString);
  }

  @Override
  public QueryRestrictionString copy() {
    QueryRestrictionString copy = new QueryRestrictionString(queryString);
    copy.setNegate(negate);
    return copy;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(super.hashCode()).append(this.getQueryString()).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof QueryRestrictionString) {
      QueryRestrictionString other = (QueryRestrictionString) obj;
      return super.equals(obj) && new EqualsBuilder().append(this.getQueryString(),
          other.getQueryString()).isEquals();
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return "QueryRestrictionString [queryString=" + getQueryString() + "]";
  }

}
