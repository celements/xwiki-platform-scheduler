package com.celements.search.lucene.query;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.search.lucene.query.QueryRestrictionGroup.Type;

public class LuceneQueryTest {
  
  private String database;
  private LuceneQuery query;
  
  @Before
  public void setUp_LuceneQueryTest() throws Exception {
    database = "theDB";
    query = new LuceneQuery(database);
  }
  
  @Test
  public void testGetType() {
    assertEquals(Type.AND, query.getType());
  }

  @Test
  public void testGetQueryString() {
    query.add(new QueryRestriction("object", "XWiki.XWikiUsers"));
    assertEquals("(wiki:(+\"" + database + "\") AND object:(+XWiki.XWikiUsers*))", 
        query.getQueryString());
  }
  
  @Test
  public void testGetQueryString_alwaysHasWikiRestriction() {
    assertEquals(1, query.size());
    assertEquals("wiki:(+\"" + database + "\")", query.getQueryString());
  }
  
  @Test
  public void testGetQueryString_withEmptyRestriction() {
    query.add(new QueryRestriction("object", ""));
    assertEquals("wiki:(+\"" + database + "\")", query.getQueryString());
  }

  @Test
  public void testGetQueryString_filled() {
    LuceneQuery query = getNewFilledQuery();
    assertEquals("(wiki:(+\"" + database + "\") AND (field1:(+value1*) "
        + "OR field2:(+value2*)) AND (field3:(+value3*) OR field4:(+value4*)) "
        + "AND field5:(+value5*))", query.getQueryString());
  }

  @Test
  public void testCopy() {
    LuceneQuery query = getNewFilledQuery();
    LuceneQuery queryCopy = query.copy();
    assertNotSame(query, queryCopy);
    assertEquals(query, queryCopy);
    assertEquals(query.getDatabase(), queryCopy.getDatabase());
  }
  
  private LuceneQuery getNewFilledQuery() {
    LuceneQuery query = new LuceneQuery(database);
    QueryRestrictionGroup restrGrpUser = new QueryRestrictionGroup(Type.OR);
    restrGrpUser.add(new QueryRestriction("field1", "value1"));
    restrGrpUser.add(new QueryRestriction("field2", "value2"));
    query.add(restrGrpUser);
    QueryRestrictionGroup restrGrpGroup = new QueryRestrictionGroup(Type.OR);
    restrGrpGroup.add(new QueryRestriction("field3", "value3"));
    restrGrpGroup.add(new QueryRestriction("field4", "value4"));
    query.add(restrGrpGroup);
    query.add(new QueryRestriction("field5", "value5"));
    return query;
  }

}
