package com.celements.search.web;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.classes.ClassDefinition;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.web.classes.WebSearchConfigClass;
import com.celements.search.web.packages.AttachmentWebSearchPackage;
import com.celements.search.web.packages.ContentWebSearchPackage;
import com.celements.search.web.packages.MenuWebSearchPackage;
import com.celements.search.web.packages.WebSearchPackage;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class WebSearchQueryBuilderTest extends AbstractComponentTest {

  private DocumentReference docRef;
  private WebSearchQueryBuilder builder;

  @Before
  public void prepareTest() throws Exception {
    docRef = new DocumentReference("wiki", "space", "doc");
    getContext().setDatabase(docRef.getWikiReference().getName());
    builder = Utils.getComponent(WebSearchQueryBuilder.class);
  }

  @Test
  public void test_getPackages_default() {
    builder.setConfigDoc(createCfDoc(docRef));

    replayDefault();
    Collection<WebSearchPackage> ret = builder.getPackages();
    verifyDefault();

    assertEquals(2, ret.size());
    assertTrue(ret.contains(Utils.getComponent(WebSearchPackage.class, MenuWebSearchPackage.NAME)));
    assertTrue(ret.contains(Utils.getComponent(WebSearchPackage.class,
        ContentWebSearchPackage.NAME)));
  }

  @Test
  public void test_addPackage() {
    builder.setConfigDoc(createCfDoc(docRef));
    WebSearchPackage webPackage = Utils.getComponent(WebSearchPackage.class,
        AttachmentWebSearchPackage.NAME);
    builder.addPackage(webPackage);

    replayDefault();
    Collection<WebSearchPackage> ret = builder.getPackages();
    verifyDefault();

    assertEquals(1, ret.size());
    assertTrue(ret.contains(webPackage));
  }

  @Test
  public void test_build_noTerm() throws Exception {
    builder.setConfigDoc(createCfDoc(docRef));

    replayDefault();
    LuceneQuery query = builder.build();
    verifyDefault();

    assertNotNull(query);
    assertEquals("(wiki:(+\"wiki\") AND NOT name:(+WebPreferences*) AND type:(+\"wikipage\"))",
        query.getQueryString());
  }

  @Test
  public void test_build_content() throws Exception {
    builder.setConfigDoc(createCfDoc(docRef));
    builder.setSearchTerm("welt");
    builder.addPackage(ContentWebSearchPackage.NAME);

    replayDefault();
    LuceneQuery query = builder.build();
    verifyDefault();

    assertNotNull(query);
    assertEquals(builder.getPackages().size(), 1);
    assertEquals("(wiki:(+\"wiki\") AND NOT name:(+WebPreferences*) "
        + "AND (type:(+\"wikipage\") AND ft:(+welt*)^20))", query.getQueryString());
  }

  @Test
  public void test_build_menu() throws Exception {
    builder.setConfigDoc(createCfDoc(docRef));
    builder.setSearchTerm("welt");
    builder.addPackage(MenuWebSearchPackage.NAME);

    replayDefault();
    LuceneQuery query = builder.build();
    verifyDefault();

    assertNotNull(query);
    assertEquals(builder.getPackages().size(), 1);
    assertEquals("(wiki:(+\"wiki\") AND NOT name:(+WebPreferences*) AND (type:(+\"wikipage\") "
        + "AND (Celements2.MenuName.menu_name:(+welt*)^30 OR title:(+welt*)^30)))",
        query.getQueryString());
  }

  @Test
  public void test_build_default() throws Exception {
    builder.setConfigDoc(createCfDoc(docRef));
    builder.setSearchTerm("welt");

    replayDefault();
    LuceneQuery query = builder.build();
    verifyDefault();

    assertNotNull(query);
    assertEquals(builder.getPackages().size(), 2);
    String queryString = query.getQueryString();
    assertTrue(queryString.startsWith("(wiki:(+\"wiki\") AND NOT name:(+WebPreferences*) AND "));
    assertTrue(queryString.contains("(type:(+\"wikipage\") AND ft:(+welt*)^20)"));
    assertTrue(queryString.contains("(type:(+\"wikipage\") "
        + "AND (Celements2.MenuName.menu_name:(+welt*)^30 OR title:(+welt*)^30)"));
  }

  @Test
  public void test_build_attachment() throws Exception {
    builder.setConfigDoc(createCfDoc(docRef));
    builder.setSearchTerm("welt");
    builder.addPackage(AttachmentWebSearchPackage.NAME);

    replayDefault();
    LuceneQuery query = builder.build();
    verifyDefault();

    assertNotNull(query);
    assertEquals(builder.getPackages().size(), 1);
    assertEquals("(wiki:(+\"wiki\") AND NOT name:(+WebPreferences*) AND type:(+\"attachment\"))",
        query.getQueryString());
  }

  private XWikiDocument createCfDoc(DocumentReference docRef) {
    XWikiDocument doc = new XWikiDocument(docRef);
    BaseObject obj = new BaseObject();
    obj.setXClassReference(Utils.getComponent(ClassDefinition.class,
        WebSearchConfigClass.CLASS_DEF_HINT).getClassRef(docRef.getWikiReference()));
    doc.addXObject(obj);
    return doc;
  }

}
