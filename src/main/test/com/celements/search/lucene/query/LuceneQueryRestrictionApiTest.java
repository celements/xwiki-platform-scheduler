package com.celements.search.lucene.query;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class LuceneQueryRestrictionApiTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testLuceneQueryRestrictionApi_nothingSet() {
    QueryRestriction apiObj = new QueryRestriction();
    assertEquals("", apiObj.getRestriction());
  }

  @Test
  public void testLuceneQueryRestrictionApi_specifierNotSet() {
    QueryRestriction apiObj = new QueryRestriction();
    apiObj.setQuery("Hans Peter");
    assertEquals("", apiObj.getRestriction());
  }

  @Test
  public void testLuceneQueryRestrictionApi_queryNotSet() {
    QueryRestriction apiObj = new QueryRestriction();
    apiObj.setSpecifier("XWiki.XWikiUsers.first_name");
    assertEquals("", apiObj.getRestriction());
  }

  @Test
  public void testLuceneQueryRestrictionApiStringString() {
    QueryRestriction apiObj = new QueryRestriction("XWiki.XWikiUsers." +
        "first_name", "Hans Peter");
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +Peter*)", apiObj.getRestriction());
  }

  @Test
  public void testLuceneQueryRestrictionApiStringString_quoted() {
    QueryRestriction apiObj = new QueryRestriction("XWiki.XWikiUsers." +
        "first_name", "Hans \"Robert Peter\"");
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +\"Robert Peter\")", 
        apiObj.getRestriction());
  }

  @Test
  public void testLuceneQueryRestrictionApiStringString_singleMinus() {
    QueryRestriction apiObj = new QueryRestriction("XWiki.XWikiUsers." +
        "first_name", "Hans 50% - 100%");
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +50%* +100%*)", 
        apiObj.getRestriction());
  }

  @Test
  public void testLuceneQueryRestrictionApiStringString_sinleMinusQuoted() {
    QueryRestriction apiObj = new QueryRestriction("XWiki.XWikiUsers." +
        "first_name", "Hans \"50% - 100%\"");
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +\"50% \\- 100%\")", 
        apiObj.getRestriction());
  }
  
  @Test
  public void testLuceneQueryRestrictionApiStringString_plus() {
    QueryRestriction apiObj = new QueryRestriction("XWiki.XWikiUsers." +
        "first_name", "+Hans");
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans*)", apiObj.getRestriction());
  }
  
  @Test
  public void testLuceneQueryRestrictionApiStringString_minus() {
    QueryRestriction apiObj = new QueryRestriction("XWiki.XWikiUsers." +
        "first_name", "Hans -Peter");
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* -Peter*)", apiObj.getRestriction());
  }
  
  @Test
  public void testLuceneQueryRestrictionApiStringString_wildcardOne() {
    QueryRestriction apiObj = new QueryRestriction("XWiki.XWikiUsers." +
        "first_name", "Hans Pe?er");
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +Pe?er*)", apiObj.getRestriction());
  }
  
  @Test
  public void testLuceneQueryRestrictionApiStringString_wildcardMultipleMiddle() {
    QueryRestriction apiObj = new QueryRestriction("XWiki.XWikiUsers." +
        "first_name", "Hans P*er");
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +P*er*)", apiObj.getRestriction());
  }
  
  @Test
  public void testLuceneQueryRestrictionApiStringString_wildcardMultipleEnd() {
    QueryRestriction apiObj = new QueryRestriction("XWiki.XWikiUsers." +
        "first_name", "Hans Pet*");
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +Pet*)", apiObj.getRestriction());
  }

  @Test
  public void testLuceneQueryRestrictionApiStringStringBoolean() {
    QueryRestriction apiObj = new QueryRestriction("XWiki.XWikiUsers." +
        "first_name", "Hans Peter", false);
    assertEquals("XWiki.XWikiUsers.first_name:(Hans Peter)", apiObj.getRestriction());
  }

  @Test
  public void testSetSpecifier() {
    QueryRestriction apiObj = new QueryRestriction("xyz", 
        "XWiki.XWikiUsers");
    apiObj.setSpecifier("object");
    assertEquals("object:(+XWiki.XWikiUsers*)", apiObj.getRestriction());
  }

  @Test
  public void testSetQuery() {
    QueryRestriction apiObj = new QueryRestriction("object", "xyz");
    apiObj.setQuery("XWiki.XWikiUsers");
    assertEquals("object:(+XWiki.XWikiUsers*)", apiObj.getRestriction());
  }

  @Test
  public void testSetTokenizeQuery_true() {
    QueryRestriction apiObj = new QueryRestriction("XWiki.XWikiUsers." +
        "first_name", "Hans Peter");
    apiObj.setTokenizeQuery(true);
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +Peter*)", apiObj.getRestriction());
  }

  @Test
  public void testSetTokenizeQuery_false() {
    QueryRestriction apiObj = new QueryRestriction("XWiki.XWikiUsers." +
        "first_name", "Hans Peter");
    apiObj.setTokenizeQuery(false);
    assertEquals("XWiki.XWikiUsers.first_name:(Hans Peter)", apiObj.getRestriction());
  }
  
  @Test
  public void testRangeQueryInclusive() {
    QueryRestriction apiObj = new QueryRestriction("XWiki.XWikiUsers." +
        "first_name", "[Hans TO Peter]");
    apiObj.setTokenizeQuery(false);
    assertEquals("XWiki.XWikiUsers.first_name:([Hans TO Peter])", apiObj.getRestriction());
  }

  @Test
  public void testRangeQueryExclusive() {
    QueryRestriction apiObj = new QueryRestriction("XWiki.XWikiUsers." +
        "first_name", "{Hans TO Peter}");
    apiObj.setTokenizeQuery(false);
    assertEquals("XWiki.XWikiUsers.first_name:({Hans TO Peter})", apiObj.getRestriction());
  }

  @Test
  public void testSetFuzzy_toHigh() {
    QueryRestriction apiObj = new QueryRestriction("XWiki.XWikiUsers." +
        "first_name", "Hans Peter");
    apiObj.setFuzzy(1.3f);
    assertEquals("XWiki.XWikiUsers.first_name:((Hans* OR Hans~) AND (Peter* OR Peter~))", apiObj.getRestriction());
  }
  
  @Test
  public void testSetFuzzy_toLow() {
    QueryRestriction apiObj = new QueryRestriction("XWiki.XWikiUsers." +
        "first_name", "Hans Peter");
    apiObj.setFuzzy(-0.2f);
    assertEquals("XWiki.XWikiUsers.first_name:((Hans* OR Hans~) AND (Peter* OR Peter~))", apiObj.getRestriction());
  }

  @Test
  public void testSetFuzzy_valid() {
    QueryRestriction apiObj = new QueryRestriction("XWiki.XWikiUsers." +
        "first_name", "Hans Peter");
    apiObj.setFuzzy(.8f);
    assertEquals("XWiki.XWikiUsers.first_name:((Hans* OR Hans~0.8) AND (Peter* OR Peter~0.8))", 
        apiObj.getRestriction());
  }

  @Test
  public void testSetFuzzy_valid_long() {
    QueryRestriction apiObj = new QueryRestriction("XWiki.XWikiUsers." +
        "first_name", "Hans Peter");
    apiObj.setFuzzy(.888888888888888f);
    assertEquals("XWiki.XWikiUsers.first_name:((Hans* OR Hans~0.889) AND (Peter* OR Peter~0.889))", 
        apiObj.getRestriction());
  }

  @Test
  public void testSetProximity_negative() {
    QueryRestriction apiObj = new QueryRestriction("XWiki.XWikiUsers." +
        "first_name", "Hans Peter");
    apiObj.setProximity(-8);
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +Peter*)", apiObj.getRestriction());
  }

  @Test
  public void testSetProximity_valid() {
    QueryRestriction apiObj = new QueryRestriction("XWiki.XWikiUsers." +
        "first_name", "Hans Peter");
    apiObj.setProximity(8);
    assertEquals("XWiki.XWikiUsers.first_name:(\"+Hans +Peter\"~8)", 
        apiObj.getRestriction());
  }

  @Test
  public void testSetBoost_negative() {
    QueryRestriction apiObj = new QueryRestriction("XWiki.XWikiUsers." +
        "first_name", "Hans Peter");
    apiObj.setBoost(-8f);
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +Peter*)", apiObj.getRestriction());
  }

  @Test
  public void testSetBoost_small() {
    QueryRestriction apiObj = new QueryRestriction("XWiki.XWikiUsers." +
        "first_name", "Hans Peter");
    apiObj.setBoost(.83333333333333f);
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +Peter*)^0.833", 
        apiObj.getRestriction());
  }

  @Test
  public void testSetBoost() {
    QueryRestriction apiObj = new QueryRestriction("XWiki.XWikiUsers." +
        "first_name", "Hans Peter");
    apiObj.setBoost(8f);
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +Peter*)^8", apiObj.getRestriction());
  }

  @Test
  public void testSetBoost_crazyHigh() {
    QueryRestriction apiObj = new QueryRestriction("XWiki.XWikiUsers." +
        "first_name", "Hans Peter");
    apiObj.setBoost(8000.01f);
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +Peter*)^8000.01", 
        apiObj.getRestriction());
  }
}
