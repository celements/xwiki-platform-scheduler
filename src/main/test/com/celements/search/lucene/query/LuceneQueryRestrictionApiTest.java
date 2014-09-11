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
    LuceneQueryRestrictionApi apiObj = new LuceneQueryRestrictionApi();
    assertEquals("", apiObj.getRestriction());
  }

  @Test
  public void testLuceneQueryRestrictionApi_specifierNotSet() {
    LuceneQueryRestrictionApi apiObj = new LuceneQueryRestrictionApi();
    apiObj.setQuery("Hans Peter");
    assertEquals("", apiObj.getRestriction());
  }

  @Test
  public void testLuceneQueryRestrictionApi_queryNotSet() {
    LuceneQueryRestrictionApi apiObj = new LuceneQueryRestrictionApi();
    apiObj.setSpecifier("XWiki.XWikiUsers.first_name");
    assertEquals("", apiObj.getRestriction());
  }

  @Test
  public void testLuceneQueryRestrictionApiStringString() {
    LuceneQueryRestrictionApi apiObj = new LuceneQueryRestrictionApi("XWiki.XWikiUsers." +
        "first_name", "Hans Peter");
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +Peter*)", apiObj.getRestriction());
  }

  @Test
  public void testLuceneQueryRestrictionApiStringString_quoted() {
    LuceneQueryRestrictionApi apiObj = new LuceneQueryRestrictionApi("XWiki.XWikiUsers." +
        "first_name", "Hans \"Robert Peter\"");
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +\"Robert Peter\")", 
        apiObj.getRestriction());
  }

  @Test
  public void testLuceneQueryRestrictionApiStringString_singleMinus() {
    LuceneQueryRestrictionApi apiObj = new LuceneQueryRestrictionApi("XWiki.XWikiUsers." +
        "first_name", "Hans 50% - 100%");
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +50%* +100%*)", 
        apiObj.getRestriction());
  }

  @Test
  public void testLuceneQueryRestrictionApiStringString_sinleMinusQuoted() {
    LuceneQueryRestrictionApi apiObj = new LuceneQueryRestrictionApi("XWiki.XWikiUsers." +
        "first_name", "Hans \"50% - 100%\"");
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +\"50% \\- 100%\")", 
        apiObj.getRestriction());
  }
  
  @Test
  public void testLuceneQueryRestrictionApiStringString_plus() {
    LuceneQueryRestrictionApi apiObj = new LuceneQueryRestrictionApi("XWiki.XWikiUsers." +
        "first_name", "+Hans");
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans*)", apiObj.getRestriction());
  }
  
  @Test
  public void testLuceneQueryRestrictionApiStringString_minus() {
    LuceneQueryRestrictionApi apiObj = new LuceneQueryRestrictionApi("XWiki.XWikiUsers." +
        "first_name", "Hans -Peter");
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* -Peter*)", apiObj.getRestriction());
  }
  
  @Test
  public void testLuceneQueryRestrictionApiStringString_wildcardOne() {
    LuceneQueryRestrictionApi apiObj = new LuceneQueryRestrictionApi("XWiki.XWikiUsers." +
        "first_name", "Hans Pe?er");
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +Pe?er*)", apiObj.getRestriction());
  }
  
  @Test
  public void testLuceneQueryRestrictionApiStringString_wildcardMultipleMiddle() {
    LuceneQueryRestrictionApi apiObj = new LuceneQueryRestrictionApi("XWiki.XWikiUsers." +
        "first_name", "Hans P*er");
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +P*er*)", apiObj.getRestriction());
  }
  
  @Test
  public void testLuceneQueryRestrictionApiStringString_wildcardMultipleEnd() {
    LuceneQueryRestrictionApi apiObj = new LuceneQueryRestrictionApi("XWiki.XWikiUsers." +
        "first_name", "Hans Pet*");
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +Pet*)", apiObj.getRestriction());
  }

  @Test
  public void testLuceneQueryRestrictionApiStringStringBoolean() {
    LuceneQueryRestrictionApi apiObj = new LuceneQueryRestrictionApi("XWiki.XWikiUsers." +
        "first_name", "Hans Peter", false);
    assertEquals("XWiki.XWikiUsers.first_name:(Hans Peter)", apiObj.getRestriction());
  }

  @Test
  public void testSetSpecifier() {
    LuceneQueryRestrictionApi apiObj = new LuceneQueryRestrictionApi("xyz", 
        "XWiki.XWikiUsers");
    apiObj.setSpecifier("object");
    assertEquals("object:(+XWiki.XWikiUsers*)", apiObj.getRestriction());
  }

  @Test
  public void testSetQuery() {
    LuceneQueryRestrictionApi apiObj = new LuceneQueryRestrictionApi("object", "xyz");
    apiObj.setQuery("XWiki.XWikiUsers");
    assertEquals("object:(+XWiki.XWikiUsers*)", apiObj.getRestriction());
  }

  @Test
  public void testSetTokenizeQuery_true() {
    LuceneQueryRestrictionApi apiObj = new LuceneQueryRestrictionApi("XWiki.XWikiUsers." +
        "first_name", "Hans Peter");
    apiObj.setTokenizeQuery(true);
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +Peter*)", apiObj.getRestriction());
  }

  @Test
  public void testSetTokenizeQuery_false() {
    LuceneQueryRestrictionApi apiObj = new LuceneQueryRestrictionApi("XWiki.XWikiUsers." +
        "first_name", "Hans Peter");
    apiObj.setTokenizeQuery(false);
    assertEquals("XWiki.XWikiUsers.first_name:(Hans Peter)", apiObj.getRestriction());
  }
  
  @Test
  public void testRangeQueryInclusive() {
    LuceneQueryRestrictionApi apiObj = new LuceneQueryRestrictionApi("XWiki.XWikiUsers." +
        "first_name", "[Hans TO Peter]");
    apiObj.setTokenizeQuery(false);
    assertEquals("XWiki.XWikiUsers.first_name:([Hans TO Peter])", apiObj.getRestriction());
  }

  @Test
  public void testRangeQueryExclusive() {
    LuceneQueryRestrictionApi apiObj = new LuceneQueryRestrictionApi("XWiki.XWikiUsers." +
        "first_name", "{Hans TO Peter}");
    apiObj.setTokenizeQuery(false);
    assertEquals("XWiki.XWikiUsers.first_name:({Hans TO Peter})", apiObj.getRestriction());
  }

  @Test
  public void testSetFuzzy_toHigh() {
    LuceneQueryRestrictionApi apiObj = new LuceneQueryRestrictionApi("XWiki.XWikiUsers." +
        "first_name", "Hans Peter");
    apiObj.setFuzzy(1.3f);
    assertEquals("XWiki.XWikiUsers.first_name:((Hans* OR Hans~) AND (Peter* OR Peter~))", apiObj.getRestriction());
  }
  
  @Test
  public void testSetFuzzy_toLow() {
    LuceneQueryRestrictionApi apiObj = new LuceneQueryRestrictionApi("XWiki.XWikiUsers." +
        "first_name", "Hans Peter");
    apiObj.setFuzzy(-0.2f);
    assertEquals("XWiki.XWikiUsers.first_name:((Hans* OR Hans~) AND (Peter* OR Peter~))", apiObj.getRestriction());
  }

  @Test
  public void testSetFuzzy_valid() {
    LuceneQueryRestrictionApi apiObj = new LuceneQueryRestrictionApi("XWiki.XWikiUsers." +
        "first_name", "Hans Peter");
    apiObj.setFuzzy(.8f);
    assertEquals("XWiki.XWikiUsers.first_name:((Hans* OR Hans~0.8) AND (Peter* OR Peter~0.8))", 
        apiObj.getRestriction());
  }

  @Test
  public void testSetFuzzy_valid_long() {
    LuceneQueryRestrictionApi apiObj = new LuceneQueryRestrictionApi("XWiki.XWikiUsers." +
        "first_name", "Hans Peter");
    apiObj.setFuzzy(.888888888888888f);
    assertEquals("XWiki.XWikiUsers.first_name:((Hans* OR Hans~0.889) AND (Peter* OR Peter~0.889))", 
        apiObj.getRestriction());
  }

  @Test
  public void testSetProximity_negative() {
    LuceneQueryRestrictionApi apiObj = new LuceneQueryRestrictionApi("XWiki.XWikiUsers." +
        "first_name", "Hans Peter");
    apiObj.setProximity(-8);
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +Peter*)", apiObj.getRestriction());
  }

  @Test
  public void testSetProximity_valid() {
    LuceneQueryRestrictionApi apiObj = new LuceneQueryRestrictionApi("XWiki.XWikiUsers." +
        "first_name", "Hans Peter");
    apiObj.setProximity(8);
    assertEquals("XWiki.XWikiUsers.first_name:(\"+Hans +Peter\"~8)", 
        apiObj.getRestriction());
  }

  @Test
  public void testSetBoost_negative() {
    LuceneQueryRestrictionApi apiObj = new LuceneQueryRestrictionApi("XWiki.XWikiUsers." +
        "first_name", "Hans Peter");
    apiObj.setBoost(-8f);
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +Peter*)", apiObj.getRestriction());
  }

  @Test
  public void testSetBoost_small() {
    LuceneQueryRestrictionApi apiObj = new LuceneQueryRestrictionApi("XWiki.XWikiUsers." +
        "first_name", "Hans Peter");
    apiObj.setBoost(.83333333333333f);
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +Peter*)^0.833", 
        apiObj.getRestriction());
  }

  @Test
  public void testSetBoost() {
    LuceneQueryRestrictionApi apiObj = new LuceneQueryRestrictionApi("XWiki.XWikiUsers." +
        "first_name", "Hans Peter");
    apiObj.setBoost(8f);
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +Peter*)^8", apiObj.getRestriction());
  }

  @Test
  public void testSetBoost_crazyHigh() {
    LuceneQueryRestrictionApi apiObj = new LuceneQueryRestrictionApi("XWiki.XWikiUsers." +
        "first_name", "Hans Peter");
    apiObj.setBoost(8000.01f);
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans* +Peter*)^8000.01", 
        apiObj.getRestriction());
  }
}
