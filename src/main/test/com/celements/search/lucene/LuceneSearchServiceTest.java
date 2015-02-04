package com.celements.search.lucene;

import static org.easymock.EasyMock.*;
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
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.plugin.lucene.LucenePlugin;
import com.xpn.xwiki.web.Utils;

public class LuceneSearchServiceTest extends AbstractBridgedComponentTestCase {
  
  private ILuceneSearchService searchService;
  private XWiki xwiki;
  private XWikiContext context;
  
  @Before
  public void setUp_LuceneSearchServiceTest() throws Exception {
    xwiki = getWikiMock();
    context = getContext();
    searchService = Utils.getComponent(ILuceneSearchService.class);
  }

  @Test
  public void testCreateQuery() {
    LuceneQuery query = searchService.createQuery();
    assertNotNull(query);
    assertEquals(getContext().getDatabase(), query.getDatabase());
    assertEquals("wiki:(+\"" + getContext().getDatabase() + "\")", 
        query.getQueryString());
  }

  @Test
  public void testCreateQuery_withDB() {
    String database = "theDB";
    LuceneQuery query = searchService.createQuery(database);
    assertNotNull(query);
    assertEquals(database, query.getDatabase());
    assertEquals("wiki:(+\"" + database + "\")", query.getQueryString());
  }
  
  @Test
  public void testCreateAndRestrictionGroup() {
    QueryRestrictionGroup restrGrp = searchService.createRestrictionGroup(Type.AND);
    assertNotNull(restrGrp);
    assertEquals(Type.AND, restrGrp.getType());
    assertEquals(0, restrGrp.size());
  }
  
  @Test
  public void testCreateOrRestrictionGroup() {
    QueryRestrictionGroup restrGrp = searchService.createRestrictionGroup(Type.OR);
    assertNotNull(restrGrp);
    assertEquals(Type.OR, restrGrp.getType());
    assertEquals(0, restrGrp.size());
  }
  
  @Test
  public void testCreateRestrictionGroup() {
    List<String> fields = Arrays.asList("field1", "field2", "field3");
    List<String> values = Arrays.asList("value1", "value2", "value3");
    QueryRestrictionGroup restrGrp = searchService.createRestrictionGroup(Type.AND, 
        fields, values);
    assertNotNull(restrGrp);
    assertEquals(Type.AND, restrGrp.getType());
    assertEquals(3, restrGrp.size());
    assertEquals("(field1:(+value1*) AND field2:(+value2*) AND field3:(+value3*))", 
        restrGrp.getQueryString());
  }
  
  @Test
  public void testCreateRestrictionGroup_oneField() {
    List<String> fields = Arrays.asList("field");
    List<String> values = Arrays.asList("value1", "value2", "value3");
    QueryRestrictionGroup restrGrp = searchService.createRestrictionGroup(Type.OR, fields, 
        values);
    assertNotNull(restrGrp);
    assertEquals(Type.OR, restrGrp.getType());
    assertEquals(3, restrGrp.size());
    assertEquals("(field:(+value1*) OR field:(+value2*) OR field:(+value3*))", 
        restrGrp.getQueryString());
  }
  
  @Test
  public void testCreateRestrictionGroup_twoValues() {
    List<String> fields = Arrays.asList("field1", "field2", "field3");
    List<String> values = Arrays.asList("value1", "value2");
    QueryRestrictionGroup restrGrp = searchService.createRestrictionGroup(Type.AND, 
        fields, values);
    assertNotNull(restrGrp);
    assertEquals(Type.AND, restrGrp.getType());
    assertEquals(3, restrGrp.size());
    assertEquals("(field1:(+value1*) AND field2:(+value2*) AND field3:(+value2*))", 
        restrGrp.getQueryString());
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
  public void testCreateWikiPageTypeRestriction() {
    QueryRestriction restr = searchService.createWikiPageTypeRestriction();
    assertNotNull(restr);
    assertEquals("type:(+\"wikipage\")", restr.getQueryString());
  }

  @Test
  public void testCreateAttachmentTypeRestriction() {
    QueryRestriction restr = searchService.createAttachmentTypeRestriction();
    assertNotNull(restr);
    assertEquals("type:(+\"attachment\")", restr.getQueryString());
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
  
  @Test
  public void testGetResultLimit() {
    int limit = 1234;
    LucenePlugin pluginMock = createMockAndAddToDefault(LucenePlugin.class);
    expect(xwiki.getPlugin(eq("lucene"), same(getContext()))).andReturn(pluginMock).once();
    expect(pluginMock.getResultLimit(eq(false), same(context))).andReturn(limit
        ).once();
    
    replayDefault();
    int ret = searchService.getResultLimit();
    verifyDefault();
    
    assertEquals(limit, ret);
  }
  
  @Test
  public void testGetResultLimit_skipChecks() {
    int limit = 1234;
    LucenePlugin pluginMock = createMockAndAddToDefault(LucenePlugin.class);
    expect(xwiki.getPlugin(eq("lucene"), same(getContext()))).andReturn(pluginMock).once();
    expect(pluginMock.getResultLimit(eq(true), same(context))).andReturn(limit
        ).once();
    
    replayDefault();
    int ret = searchService.getResultLimit(true);
    verifyDefault();
    
    assertEquals(limit, ret);
  }

}
