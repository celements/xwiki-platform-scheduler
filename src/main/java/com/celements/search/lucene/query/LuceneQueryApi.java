package com.celements.search.lucene.query;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


public class LuceneQueryApi {
  
  private List<LuceneQueryRestrictionApi> andRestrictions;
  private List<List<LuceneQueryRestrictionApi>> orRestrictions;
  private String database;

  public LuceneQueryApi(String database) {
    this.database = database;
  }

  public LuceneQueryApi(LuceneQueryApi query) {
    andRestrictions = copyRestrictionList(query.andRestrictions);
    orRestrictions = copyRestrictionListList(query.orRestrictions);
    database = query.database;
  }

  public LuceneQueryApi addRestriction(LuceneQueryRestrictionApi restriction) {
    if (andRestrictions == null) {
      andRestrictions = new ArrayList<LuceneQueryRestrictionApi>();
    }
    if (restriction != null) {
      andRestrictions.add(restriction);
    }
    return this;
  }

  public LuceneQueryApi addRestrictionList(
      List<LuceneQueryRestrictionApi> restrictionsList) {
    if (restrictionsList != null) {
      for (LuceneQueryRestrictionApi restriction : restrictionsList) {
        addRestriction(restriction);
      }
    }
    return this;
  }

  public LuceneQueryApi addOrRestrictionList(
      List<LuceneQueryRestrictionApi> restrictionsList) {
    if (orRestrictions == null) {
      orRestrictions = new ArrayList<List<LuceneQueryRestrictionApi>>();
    }
    if ((restrictionsList != null) && (restrictionsList.size() > 0)) {
      orRestrictions.add(restrictionsList);
    }
    return this;
  }

  public String getQueryString() {
    String queryString = "";
    if (andRestrictions != null) {
      for (LuceneQueryRestrictionApi restriction : andRestrictions) {
        String restrStr = restriction.getRestriction();
        if (!"".equals(restrStr.trim())) {
          queryString += restrStr + " AND ";
        }
      }
    }
    if(orRestrictions != null) {
      for (List<LuceneQueryRestrictionApi> restrictions : orRestrictions) {
        String orQuery = new String();
        for (LuceneQueryRestrictionApi restriction : restrictions) {
          if (orQuery.length() > 0) {
            orQuery += " OR ";
          }
          orQuery += restriction.getRestriction();
        }
        queryString += "(" + orQuery + ") AND ";
      }
    }
    return queryString + "wiki:" + database;
  }

  private static List<LuceneQueryRestrictionApi> copyRestrictionList(
      List<LuceneQueryRestrictionApi> list) {
    List<LuceneQueryRestrictionApi> ret = null;
    if (list != null){
      ret = new ArrayList<LuceneQueryRestrictionApi>();
      for (LuceneQueryRestrictionApi restriction : list) {
        ret.add(new LuceneQueryRestrictionApi(restriction));
      }
    }
    return ret;
  }

  private static List<List<LuceneQueryRestrictionApi>> copyRestrictionListList(
      List<List<LuceneQueryRestrictionApi>> list) {
    List<List<LuceneQueryRestrictionApi>> ret = null;
    if (list != null) {
      ret = new ArrayList<List<LuceneQueryRestrictionApi>>();
      for (List<LuceneQueryRestrictionApi> restrictions : list) {
        ret.add(copyRestrictionList(restrictions));
      }
    }
    return ret;
  }

  @Override
  public int hashCode() {
    andRestrictions.hashCode();
    return new HashCodeBuilder().append(database).append(andRestrictions).append(
        orRestrictions).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof LuceneQueryApi) {
      LuceneQueryApi other = (LuceneQueryApi) obj;
      return new EqualsBuilder().append(database, other.database).append(andRestrictions, 
          other.andRestrictions).append(orRestrictions, other.orRestrictions).isEquals();
    } else {
      return false;
    }
  }
  
  @Override
  public String toString() {
    return "LuceneQueryRestrictionApi [queryString=" + getQueryString() + "]";
  }
  
}
