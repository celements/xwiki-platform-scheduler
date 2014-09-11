package com.celements.search.lucene.query;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;

public class LuceneQueryApiTest extends AbstractBridgedComponentTestCase {
  LuceneQueryApi apiObj;
  
  @Before
  public void setUp_LuceneQueryApiTest() throws Exception {
    apiObj = new LuceneQueryApi(getContext().getDatabase());
  }

  @Test
  public void testAddRestriction() {
    apiObj.addRestriction(new LuceneQueryRestrictionApi("object", "XWiki.XWikiUsers"));
    assertEquals("object:(+XWiki.XWikiUsers*) AND wiki:" + getContext().getDatabase(), apiObj.getQueryString());
  }
  
  @Test
  public void testGetQueryString_alwaysHasWikiRestriction() {
    assertEquals("wiki:" + getContext().getDatabase(), apiObj.getQueryString());
  }
  
  @Test
  public void testGetQueryString_withEmptyRestriction() {
    apiObj.addRestriction(new LuceneQueryRestrictionApi("object", ""));
    assertEquals("wiki:" + getContext().getDatabase(), apiObj.getQueryString());
  }
  
  @Test
  public void testAddOrRestrictionList() {
    List<LuceneQueryRestrictionApi> list = new ArrayList<LuceneQueryRestrictionApi>();
    list.add(new LuceneQueryRestrictionApi("object", "XWiki.XWikiUsers"));
    list.add(new LuceneQueryRestrictionApi("object", "XWiki.XWikiGroups"));
    apiObj.addOrRestrictionList(list);
    assertEquals("(object:(+XWiki.XWikiUsers*) OR object:(+XWiki.XWikiGroups*)) AND wiki:" 
        + getContext().getDatabase(), apiObj.getQueryString());
  }

  @Test
  public void testGetQueryString_andOnly() {
    apiObj.addRestriction(new LuceneQueryRestrictionApi("object", "XWiki.XWikiUsers"));
    apiObj.addRestriction(new LuceneQueryRestrictionApi("XWiki.XWikiUsers.first_name",
        "Hans Peter"));
    apiObj.addRestriction(new LuceneQueryRestrictionApi("XWiki.XWikiUsers.last_name", 
        "+Meier -Mueller"));
    assertEquals("object:(+XWiki.XWikiUsers*) AND XWiki.XWikiUsers.first_name:(+Hans* " +
        "+Peter*) AND XWiki.XWikiUsers.last_name:(+Meier* -Mueller*) AND wiki:" 
        + getContext().getDatabase(), apiObj.getQueryString());
  }

  @Test
  public void testGetQueryString_orOnly() {
    List<LuceneQueryRestrictionApi> list = new ArrayList<LuceneQueryRestrictionApi>();
    list.add(new LuceneQueryRestrictionApi("object", "XWiki.XWikiUsers"));
    list.add(new LuceneQueryRestrictionApi("object", "XWiki.XWikiGroups"));
    apiObj.addOrRestrictionList(list);
    apiObj.addOrRestrictionList(list);
    assertEquals("(object:(+XWiki.XWikiUsers*) OR object:(+XWiki.XWikiGroups*)) AND " +
        "(object:(+XWiki.XWikiUsers*) OR object:(+XWiki.XWikiGroups*)) AND wiki:" 
        + getContext().getDatabase(), apiObj.getQueryString());
  }

  @Test
  public void testGetQueryString_andOr() {
    String compareString = setFullQuery();
    assertEquals(compareString, apiObj.getQueryString());
  }

  @Test
  public void testGetQueryString_copy() {
    String compareString = setFullQuery();
    LuceneQueryApi apiObjCopy = new LuceneQueryApi(apiObj);
    assertEquals(compareString, apiObjCopy.getQueryString());
  }
  
  private String setFullQuery() {
    String compareString = "object:(+XWiki.XWikiUsers*) AND XWiki.XWikiUsers.first_name:(+Hans* " +
        "+Peter*) AND XWiki.XWikiUsers.last_name:(+Meier* -Mueller*) AND " +
        "(object:(+XWiki.XWikiUsers*) OR object:(+XWiki.XWikiGroups*)) AND " +
        "(object:(+XWiki.XWikiUsers*) OR object:(+XWiki.XWikiGroups*)) AND wiki:" 
        + getContext().getDatabase();
    List<LuceneQueryRestrictionApi> list = new ArrayList<LuceneQueryRestrictionApi>();
    list.add(new LuceneQueryRestrictionApi("object", "XWiki.XWikiUsers"));
    list.add(new LuceneQueryRestrictionApi("object", "XWiki.XWikiGroups"));
    apiObj.addOrRestrictionList(list);
    apiObj.addOrRestrictionList(list);
    apiObj.addRestriction(new LuceneQueryRestrictionApi("object", "XWiki.XWikiUsers"));
    apiObj.addRestriction(new LuceneQueryRestrictionApi("XWiki.XWikiUsers.first_name",
        "Hans Peter"));
    apiObj.addRestriction(new LuceneQueryRestrictionApi("XWiki.XWikiUsers.last_name", 
        "+Meier -Mueller"));
    return compareString;
  }

}
