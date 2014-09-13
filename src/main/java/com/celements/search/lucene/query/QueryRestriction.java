package com.celements.search.lucene.query;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.queryParser.QueryParser;

public class QueryRestriction {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      QueryRestriction.class);

  private String specifier = null;
  private String query = null;
  private boolean tokenizeQuery = true;
  private Float fuzzy = null;
  private Integer proximity = null;
  private Float boost = null;
  private boolean negate = false;

  public QueryRestriction() { }

  public QueryRestriction(QueryRestriction restriction) {
    specifier = restriction.specifier;
    query = restriction.query;
    tokenizeQuery = restriction.tokenizeQuery;
    fuzzy = restriction.fuzzy;
    proximity = restriction.proximity;
    boost = restriction.boost;
    negate = restriction.negate;
  }

  public QueryRestriction(String specifier, String query) {
    this.specifier = specifier;
    this.query = query;
  }

  public QueryRestriction(String specifier, String query, boolean tokenizeQuery
      ) {
    this.specifier = specifier;
    this.query = query;
    this.tokenizeQuery = tokenizeQuery;
  }

  public QueryRestriction setSpecifier(String specifier) {
    this.specifier = specifier;
    return this;
  }

  public QueryRestriction setQuery(String query) {
    this.query = query;
    return this;
  }

  /**
   * Uses required/AND for multiple keywords when true, e.g. (+keyword1 +keyword2),
   * else (keyword1 keyword2)
   * @param tokenizeQuery
   * @return
   */
  public QueryRestriction setTokenizeQuery(boolean tokenizeQuery) {
    this.tokenizeQuery = tokenizeQuery;
    return this;
  }

  /**
   * Use fuzzy search to find similar words.
   * @param fuzzy Allowed distance from 0 to 1 where the closer to 1 the parameter is,
   *              the higher similarity the match needs to have.
   */
  public QueryRestriction setFuzzy(String fuzzy) {
    try {
      setFuzzy(Float.parseFloat(fuzzy));
    } catch(NumberFormatException nfe) {
      LOGGER.error("Exception parsing float of '" + fuzzy + "'.", nfe);
    }
    return this;
  }

  public QueryRestriction setFuzzy(float fuzzy) {
    this.fuzzy = fuzzy;
    return this;
  }

  public QueryRestriction setFuzzy() {
    fuzzy = -1f; //use Lucene's default (which is 0.5)
    return this;
  }

  public QueryRestriction setNegate(boolean negate) {
    this.negate = negate;
    return this;
  }

  /**
   * The term's words have to be in the given proximity e.g. at most 8 words apart.
   * @param proximity How many words apart can the words be.
   */
  public QueryRestriction setProximity(String proximity) {
    try {
      setProximity(Integer.parseInt(proximity));
    } catch(NumberFormatException nfe) {
      LOGGER.error("Exception parsing float of '" + fuzzy + "'.", nfe);
    }
    return this;
  }

  public QueryRestriction setProximity(int proximity) {
    this.proximity = proximity;
    return this;
  }

  /**
   * A boost factor for the term. The higher the boost factor, the more relevant the term.
   * The boost factor has to be positive, can be < 1 though. The default is 1.
   * @param boost The factor to boost the term.
   * @return
   */
  public QueryRestriction setBoost(String boost) {
    try {
      setBoost(Float.parseFloat(boost));
    } catch(NumberFormatException nfe) {
      LOGGER.error("Exception parsing float of '" + fuzzy + "'.", nfe);
    }
    return this;
  }

  public QueryRestriction setBoost(float boost) {
    this.boost = boost;
    return this;
  }

  public String getRestriction() {
    String queryString = "";
    if((specifier != null) && (query != null) && (specifier.trim().length() > 0)
        && (query.trim().length() > 0)) {
      if(tokenizeQuery) {
        StringBuilder tokenizedQuery = new StringBuilder();
        Matcher m = getQueryTokenMatcher();
        while(m.find()) {
          String token = m.group(0).trim();
          if(!token.matches("[\"+-]")) {
            token = QueryParser.escape(token);
            token = token.replaceAll("^(\\\\([+-]))?(\\\\(\"))?(.*?)(\\\\(\"))?$",
                "$2$4$5$7");
            token = token.replaceAll("^(.+)\\\\([\\?\\*].*)$", "$1$2");
            if(!token.matches("^[+-].*")) {
              token = "+" + token;
            }
            if(!token.matches("^.*[\\*\\\"]$")
                && ((proximity == null) || (proximity <= 1))) {
              token += "*";
            }
            tokenizedQuery.append(" " + token);
          }
        }
        queryString = tokenizedQuery.toString().trim();
      } else {
        queryString = query;
      }
      DecimalFormat formater = getDecimalFormater();
      queryString = makeRestrictionFuzzy(queryString, formater);
      if((proximity != null) && (proximity > 1)) {
        queryString = "\"" + queryString + "\"~" + formater.format(proximity);
      }
      queryString = specifier + ":(" + queryString + ")";
      if((boost != null) && (boost > 0)) {
        queryString += "^" + formater.format(boost);
      }
      if(negate) {
        queryString = "NOT " + queryString;
      }
    }
    return queryString;
  }

  Matcher getQueryTokenMatcher() {
    String regex = "( ?\"[^\"]*\" ?)|[^ ]+";
    Pattern p = Pattern.compile(regex);
    Matcher m = p.matcher(query);
    return m;
  }

  DecimalFormat getDecimalFormater() {
    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.GERMAN);
    symbols.setDecimalSeparator('.');
    return new DecimalFormat("#.###", symbols);
  }

  String makeRestrictionFuzzy(String queryString, DecimalFormat formater) {
    String finalQuery = queryString;
    if(fuzzy != null) {
      String fuzzyStr = "~";
      if((fuzzy >= 0) && (fuzzy <= 1)) {
        fuzzyStr += formater.format(fuzzy);
      }
      finalQuery = "";
      for(String term : queryString.split(" ")) {
        String termString = term + fuzzyStr;
        if(term.endsWith("*")) {
          termString = "(" + term.replaceAll("\\+?(.*)", "$1") + " OR " 
              + term.replaceAll("\\+?(.*)\\*$", "$1" + fuzzyStr) + ")";
        }
        finalQuery += termString + " AND ";
      }
      finalQuery = finalQuery.replaceAll("(.*) AND $", "$1");
    }
    return finalQuery;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(boost).append(fuzzy).append(negate).append(
        proximity).append(query).append(specifier).append(tokenizeQuery).toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof QueryRestriction) {
      QueryRestriction other = (QueryRestriction) obj;
      return new EqualsBuilder().append(boost, other.boost).append(fuzzy, other.fuzzy
          ).append(negate, other.negate).append(proximity, other.proximity).append(query, 
              other.query).append(specifier, other.specifier).append(tokenizeQuery, 
                  other.tokenizeQuery).isEquals();
    } else {
      return false;
    }
  }
  
  @Override
  public String toString() {
    return "LuceneQueryRestrictionApi [queryString=" + getRestriction() + "]";
  }
  
}
