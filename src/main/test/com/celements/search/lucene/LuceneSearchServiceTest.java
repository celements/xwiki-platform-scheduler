package com.celements.search.lucene;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.search.lucene.query.LuceneQueryRestrictionApi;
import com.xpn.xwiki.web.Utils;

public class LuceneSearchServiceTest extends AbstractBridgedComponentTestCase {
  
  private ILuceneSearchService searchService;
  
  @Before
  public void setUp_LuceneSearchServiceTest() throws Exception {
    searchService = Utils.getComponent(ILuceneSearchService.class);
  }

  @Test
  public void testCreateRestriction() {
    LuceneQueryRestrictionApi restr = searchService.createRestriction("XWiki.XWikiUsers." +
        "first_name", "Hans"); // true, false
    assertNotNull(restr);
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans*)", restr.getRestriction());
  }

  @Test
  public void testCreateRestriction_false_true() {
    LuceneQueryRestrictionApi restr = searchService.createRestriction("XWiki.XWikiUsers." +
        "first_name", "Hans", false, true);
    assertNotNull(restr);
    assertEquals("XWiki.XWikiUsers.first_name:(Hans~)", restr.getRestriction());
  }
  
  @Test
  public void testCreateRestrictionList() {
    List<LuceneQueryRestrictionApi> restrList = searchService.createRestrictionList(
        Arrays.asList("XWiki.XWikiUsers.first_name", "XWiki.XWikiUsers.last_name"), "Hans");
    assertNotNull(restrList);
    assertEquals(2, restrList.size());
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans*)", restrList.get(0).getRestriction());
    assertEquals("XWiki.XWikiUsers.last_name:(+Hans*)", restrList.get(1).getRestriction());
  }

  @Test
  public void testRangeQuery() {
    LuceneQueryRestrictionApi restr = searchService.createRangeRestriction("XWiki." +
        "XWikiUsers.first_name", "Hans", "Peter");
    assertNotNull(restr);
    assertEquals("XWiki.XWikiUsers.first_name:([Hans TO Peter])", restr.getRestriction());
  }
  
  @Test
  public void testRangeQueryExclusive() {
    LuceneQueryRestrictionApi restr = searchService.createRangeRestriction("XWiki." +
        "XWikiUsers.first_name", "Hans", "Peter", false);
    assertNotNull(restr);
    assertEquals("XWiki.XWikiUsers.first_name:({Hans TO Peter})", restr.getRestriction());
  }

  @Test
  public void testRangeQueryInclusive() {
    LuceneQueryRestrictionApi restr = searchService.createRangeRestriction("XWiki." +
        "XWikiUsers.first_name", "Hans", "Peter", true);
    assertNotNull(restr);
    assertEquals("XWiki.XWikiUsers.first_name:([Hans TO Peter])", restr.getRestriction());
  }
  
  @Test
  public void testCreateDateRestriction() throws ParseException {    
    Date date = LuceneSearchService.SDF.parse("199001151213");
    LuceneQueryRestrictionApi restr = searchService.createDateRestriction("XWiki." +
        "XWikiUsers.date", date);
    assertNotNull(restr);
    assertEquals("XWiki.XWikiUsers.date:(199001151213)", restr.getRestriction());
  }
  
  @Test
  public void testCreateFromToDateRestriction() throws ParseException {  
    Date fromDate = LuceneSearchService.SDF.parse("111111111111");
    Date toDate = LuceneSearchService.SDF.parse("199001151213");
    LuceneQueryRestrictionApi restr = searchService.createFromToDateRestriction("XWiki." +
        "XWikiUsers.date", fromDate, toDate, true);
    assertNotNull(restr);
    assertEquals("XWiki.XWikiUsers.date:([111111111111 TO 199001151213])", 
        restr.getRestriction());
  }
  
  @Test
  public void testCreateToDateRestriction() throws ParseException {
    Date toDate = LuceneSearchService.SDF.parse("199001151213");
    LuceneQueryRestrictionApi restr = searchService.createToDateRestriction("XWiki." +
        "XWikiUsers.date", toDate, true);
    assertNotNull(restr);
    assertEquals("XWiki.XWikiUsers.date:([000101010000 TO 199001151213])", 
        restr.getRestriction());
  }
  
  @Test
  public void testCreateFromDateRestriction() throws ParseException {  
    Date fromDate = LuceneSearchService.SDF.parse("111111111111");
    LuceneQueryRestrictionApi restr = searchService.createFromDateRestriction("XWiki." +
        "XWikiUsers.date", fromDate, true);
    assertNotNull(restr);
    assertEquals("XWiki.XWikiUsers.date:([111111111111 TO 999912312359])", 
        restr.getRestriction());
  }

}
