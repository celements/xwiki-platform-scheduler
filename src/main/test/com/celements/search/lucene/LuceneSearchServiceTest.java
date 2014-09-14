package com.celements.search.lucene;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.lucene.query.QueryRestriction;
import com.celements.search.lucene.query.QueryRestrictionGroup;
import com.celements.search.lucene.query.QueryRestrictionGroup.Type;
import com.xpn.xwiki.web.Utils;

public class LuceneSearchServiceTest extends AbstractBridgedComponentTestCase {
  
  private ILuceneSearchService searchService;
  
  @Before
  public void setUp_LuceneSearchServiceTest() throws Exception {
    searchService = Utils.getComponent(ILuceneSearchService.class);
  }

  @Test
  public void testCreateQuery() {
    LuceneQuery query = searchService.createQuery();
    assertNotNull(query);
    assertEquals(getContext().getDatabase(), query.getDatabase());
    assertEquals("(wiki:(+\"" + getContext().getDatabase() + "\"))", 
        query.getQueryString());
  }

  @Test
  public void testCreateQuery_withDB() {
    String database = "theDB";
    LuceneQuery query = searchService.createQuery(database);
    assertNotNull(query);
    assertEquals(database, query.getDatabase());
    assertEquals("(wiki:(+\"" + database + "\"))", query.getQueryString());
  }
  
  @Test
  public void testCreateAndRestrictionGroup() {
    QueryRestrictionGroup restrGrp = searchService.createAndRestrictionGroup();
    assertNotNull(restrGrp);
    assertEquals(Type.AND, restrGrp.getType());
    assertEquals(0, restrGrp.size());
  }
  
  @Test
  public void testCreateOrRestrictionGroup() {
    QueryRestrictionGroup restrGrp = searchService.createOrRestrictionGroup();
    assertNotNull(restrGrp);
    assertEquals(Type.OR, restrGrp.getType());
    assertEquals(0, restrGrp.size());
  }

  @Test
  public void testCreateRestriction() {
    QueryRestriction restr = searchService.createRestriction("XWiki.XWikiUsers." +
        "first_name", "Hans");
    assertNotNull(restr);
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans*)", restr.getQueryString());
  }

  @Test
  public void testCreateRestriction_false_true() {
    QueryRestriction restr = searchService.createRestriction("XWiki.XWikiUsers." +
        "first_name", "Hans", false, true);
    assertNotNull(restr);
    assertEquals("XWiki.XWikiUsers.first_name:(Hans~)", restr.getQueryString());
  }
  
  @Test
  public void testCreateRestrictionList() {
    List<QueryRestriction> restrList = searchService.createRestrictionList(
        Arrays.asList("XWiki.XWikiUsers.first_name", "XWiki.XWikiUsers.last_name"), "Hans");
    assertNotNull(restrList);
    assertEquals(2, restrList.size());
    assertEquals("XWiki.XWikiUsers.first_name:(+Hans*)", restrList.get(0).getQueryString());
    assertEquals("XWiki.XWikiUsers.last_name:(+Hans*)", restrList.get(1).getQueryString());
  }

  @Test
  public void testRangeRestriction() {
    QueryRestriction restr = searchService.createRangeRestriction("XWiki." +
        "XWikiUsers.first_name", "Hans", "Peter");
    assertNotNull(restr);
    assertEquals("XWiki.XWikiUsers.first_name:([Hans TO Peter])", restr.getQueryString());
  }
  
  @Test
  public void testRangeRestrictionExclusive() {
    QueryRestriction restr = searchService.createRangeRestriction("XWiki." +
        "XWikiUsers.first_name", "Hans", "Peter", false);
    assertNotNull(restr);
    assertEquals("XWiki.XWikiUsers.first_name:({Hans TO Peter})", restr.getQueryString());
  }

  @Test
  public void testRangeRestrictionInclusive() {
    QueryRestriction restr = searchService.createRangeRestriction("XWiki." +
        "XWikiUsers.first_name", "Hans", "Peter", true);
    assertNotNull(restr);
    assertEquals("XWiki.XWikiUsers.first_name:([Hans TO Peter])", restr.getQueryString());
  }
  
  @Test
  public void testCreateDateRestriction() throws ParseException {    
    Date date = LuceneSearchService.SDF.parse("199001151213");
    QueryRestriction restr = searchService.createDateRestriction("XWiki." +
        "XWikiUsers.date", date);
    assertNotNull(restr);
    assertEquals("XWiki.XWikiUsers.date:(199001151213)", restr.getQueryString());
  }
  
  @Test
  public void testCreateFromToDateRestriction() throws ParseException {  
    Date fromDate = LuceneSearchService.SDF.parse("111111111111");
    Date toDate = LuceneSearchService.SDF.parse("199001151213");
    QueryRestriction restr = searchService.createFromToDateRestriction("XWiki." +
        "XWikiUsers.date", fromDate, toDate, true);
    assertNotNull(restr);
    assertEquals("XWiki.XWikiUsers.date:([111111111111 TO 199001151213])", 
        restr.getQueryString());
  }
  
  @Test
  public void testCreateToDateRestriction() throws ParseException {
    Date toDate = LuceneSearchService.SDF.parse("199001151213");
    QueryRestriction restr = searchService.createToDateRestriction("XWiki." +
        "XWikiUsers.date", toDate, true);
    assertNotNull(restr);
    assertEquals("XWiki.XWikiUsers.date:([000101010000 TO 199001151213])", 
        restr.getQueryString());
  }
  
  @Test
  public void testCreateFromDateRestriction() throws ParseException {  
    Date fromDate = LuceneSearchService.SDF.parse("111111111111");
    QueryRestriction restr = searchService.createFromDateRestriction("XWiki." +
        "XWikiUsers.date", fromDate, true);
    assertNotNull(restr);
    assertEquals("XWiki.XWikiUsers.date:([111111111111 TO 999912312359])", 
        restr.getQueryString());
  }

}
