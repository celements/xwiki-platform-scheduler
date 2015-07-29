package com.celements.search.lucene;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.script.service.ScriptService;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.search.lucene.query.QueryRestriction;
import com.xpn.xwiki.web.Utils;

public class LuceneSearchScriptServiceTest extends AbstractBridgedComponentTestCase {
  
  private LuceneSearchScriptService scriptService;
  
  @Before
  public void setUp_LuceneSearchServiceTest() throws Exception {
    scriptService = (LuceneSearchScriptService) Utils.getComponent(ScriptService.class,
        LuceneSearchScriptService.NAME);
  }

  @Test
  public void test_createSpaceRestriction() {
    QueryRestriction restr = scriptService.createSpaceRestriction("someSpace");
    assertNotNull(restr);
    assertEquals("space:(+\"someSpace\")", restr.getQueryString());
  }

  @Test
  public void test_createSpaceRestriction_quoted() {
    QueryRestriction restr = scriptService.createSpaceRestriction("\"someSpace\"");
    assertNotNull(restr);
    assertEquals("space:(+\"someSpace\")", restr.getQueryString());
  }

  @Test
  public void test_createDocRestriction() {
    QueryRestriction restr = scriptService.createDocRestriction("Space.Doc");
    assertNotNull(restr);
    assertEquals("fullname:(+\"Space.Doc\")", restr.getQueryString());
  }

  @Test
  public void test_createDocRestriction_quoted() {
    QueryRestriction restr = scriptService.createDocRestriction("\"Space.Doc\"");
    assertNotNull(restr);
    assertEquals("fullname:(+\"Space.Doc\")", restr.getQueryString());
  }

  @Test
  public void test_createObjectRestriction() {
    QueryRestriction restr = scriptService.createObjectRestriction("XWiki.XWikiUsers");
    assertNotNull(restr);
    assertEquals("object:(+\"XWiki.XWikiUsers\")", restr.getQueryString());
  }

  @Test
  public void test_createObjectRestriction_quoted() {
    QueryRestriction restr = scriptService.createObjectRestriction("\"XWiki.XWikiUsers\"");
    assertNotNull(restr);
    assertEquals("object:(+\"XWiki.XWikiUsers\")", restr.getQueryString());
  }

}
