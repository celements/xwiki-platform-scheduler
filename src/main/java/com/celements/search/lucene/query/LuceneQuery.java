package com.celements.search.lucene.query;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


public class LuceneQuery {
  
  private List<QueryRestriction> andRestrictions;
  private List<List<QueryRestriction>> orRestrictions;
  private String database;

  public LuceneQuery(String database) {
    this.database = database;
  }

  public LuceneQuery(LuceneQuery query) {
    andRestrictions = copyRestrictionList(query.andRestrictions);
    orRestrictions = copyRestrictionListList(query.orRestrictions);
    database = query.database;
  }

  public LuceneQuery addRestriction(QueryRestriction restriction) {
    if (andRestrictions == null) {
      andRestrictions = new ArrayList<QueryRestriction>();
    }
    if (restriction != null) {
      andRestrictions.add(restriction);
    }
    return this;
  }

  public LuceneQuery addRestrictionList(
      List<QueryRestriction> restrictionsList) {
    if (restrictionsList != null) {
      for (QueryRestriction restriction : restrictionsList) {
        addRestriction(restriction);
      }
    }
    return this;
  }

  public LuceneQuery addOrRestrictionList(
      List<QueryRestriction> restrictionsList) {
    if (orRestrictions == null) {
      orRestrictions = new ArrayList<List<QueryRestriction>>();
    }
    if ((restrictionsList != null) && (restrictionsList.size() > 0)) {
      orRestrictions.add(restrictionsList);
    }
    return this;
  }

  public String getQueryString() {
    String queryString = "";
    if (andRestrictions != null) {
      for (QueryRestriction restriction : andRestrictions) {
        String restrStr = restriction.getRestriction();
        if (!"".equals(restrStr.trim())) {
          queryString += restrStr + " AND ";
        }
      }
    }
    if(orRestrictions != null) {
      for (List<QueryRestriction> restrictions : orRestrictions) {
        String orQuery = new String();
        for (QueryRestriction restriction : restrictions) {
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

  private static List<QueryRestriction> copyRestrictionList(
      List<QueryRestriction> list) {
    List<QueryRestriction> ret = null;
    if (list != null){
      ret = new ArrayList<QueryRestriction>();
      for (QueryRestriction restriction : list) {
        ret.add(new QueryRestriction(restriction));
      }
    }
    return ret;
  }

  private static List<List<QueryRestriction>> copyRestrictionListList(
      List<List<QueryRestriction>> list) {
    List<List<QueryRestriction>> ret = null;
    if (list != null) {
      ret = new ArrayList<List<QueryRestriction>>();
      for (List<QueryRestriction> restrictions : list) {
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
    if (obj instanceof LuceneQuery) {
      LuceneQuery other = (LuceneQuery) obj;
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
