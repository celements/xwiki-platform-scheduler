package com.celements.contact.plugin;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.XWikiRequest;

public class ContactPluginTest extends AbstractBridgedComponentTestCase {

  ContactPlugin plugin;
  XWikiContext context;
  XWiki xwiki;

  @Before
  public void setUp_ContactPluginTest() throws Exception {
    xwiki = createMock(XWiki.class);
    context = getContext();
    context.setWiki(xwiki);
    plugin = new ContactPlugin("", "", context);
  }

  @Test
  public void testSaveContact() throws XWikiException {
    BaseObject obj = new BaseObject();
    XWikiDocument doc = createMock(XWikiDocument.class);
    context.setDoc(doc);
    XWikiRequest request = createMock(XWikiRequest.class);
    context.setRequest(request);
    expect(request.get(eq("title"))).andReturn("");
    expect(request.get(eq("firstname"))).andReturn("Mynami");
    expect(request.get(eq("lastname"))).andReturn(null);
    expect(request.get(eq("sex"))).andReturn("male");
    xwiki.saveDocument(doc, context);
    expectLastCall();
    expect(doc.getObject(eq("Celements.ContactClass"))).andReturn(obj);
    replay(doc, request, xwiki);
    plugin.saveContact(null, context);
    verify(doc, request, xwiki);
  }

  @Test
  public void testSaveAddress() throws XWikiException {
    // String fullname = "Space.Document";
    BaseObject obj = new BaseObject();
    XWikiDocument doc = createMock(XWikiDocument.class);
    context.setDoc(doc);
    XWikiRequest request = createMock(XWikiRequest.class);
    context.setRequest(request);
    expect(request.get(eq("street_nr"))).andReturn("Street 10");
    expect(request.get(eq("zip"))).andReturn(null);
    expect(request.get(eq("city"))).andReturn("");
    expect(request.get(eq("country"))).andReturn("CH");
    xwiki.saveDocument(doc, context);
    expectLastCall();
    expect(doc.getObject(eq("Celements.ContactAddressClass"))).andReturn(obj);
    replay(doc, request, xwiki);
    plugin.saveAddress(null, context);
    verify(doc, request, xwiki);
  }

  @Test
  public void testGetObject_createNew() throws XWikiException {
    BaseObject obj = new BaseObject();
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(doc.getObject(eq("Class.ClassName"))).andReturn(null);
    expect(doc.newObject(eq("Class.ClassName"), same(context))).andReturn(obj);
    xwiki.saveDocument(doc, context);
    expectLastCall();
    replay(doc, xwiki);
    assertSame(obj, plugin.getObject("Class.ClassName", doc, context));
    verify(doc, xwiki);
  }

  @Test
  public void testGetObject() {
    BaseObject obj = new BaseObject();
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(doc.getObject(eq("Class.ClassName"))).andReturn(obj);
    replay(doc);
    assertSame(obj, plugin.getObject("Class.ClassName", doc, context));
    verify(doc);
  }

  @Test
  public void testSetStringField_null() {
    BaseObject obj = new BaseObject();
    plugin.setStringField("field", null, obj);
    assertEquals("", obj.getStringValue("field"));
  }

  @Test
  public void testSetStringField_empty() {
    BaseObject obj = new BaseObject();
    plugin.setStringField("field", " ", obj);
    assertEquals("", obj.getStringValue("field"));
  }

  @Test
  public void testSetStringField() {
    BaseObject obj = new BaseObject();
    plugin.setStringField("field", "value", obj);
    assertEquals("value", obj.getStringValue("field"));
  }

  @Test
  public void testGetDoc_null() {
    XWikiDocument doc = createMock(XWikiDocument.class);
    context.setDoc(doc);
    replay(doc, xwiki);
    assertSame(doc, plugin.getDoc(null, context));
    verify(doc, xwiki);
  }

  @Test
  public void testGetDoc_empty() {
    XWikiDocument doc = createMock(XWikiDocument.class);
    context.setDoc(doc);
    expect(xwiki.exists(eq(""), same(context))).andReturn(false);
    replay(doc, xwiki);
    assertSame(doc, plugin.getDoc("", context));
    verify(doc, xwiki);
  }

  @Test
  public void testGetDoc_exception() throws XWikiException {
    XWikiDocument doc = createMock(XWikiDocument.class);
    context.setDoc(doc);
    expect(xwiki.exists(eq("Test.Document"), same(context))).andReturn(true);
    expect(xwiki.getDocument(eq("Test.Document"), same(context)))
        .andThrow(new XWikiException());
    replay(doc, xwiki);
    assertSame(doc, plugin.getDoc("Test.Document", context));
    verify(doc, xwiki);
  }

  @Test
  public void testGetDoc_exists() throws XWikiException {
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(xwiki.exists(eq("Test.Document"), same(context))).andReturn(true);
    expect(xwiki.getDocument(eq("Test.Document"), same(context))).andReturn(doc);
    replay(doc, xwiki);
    assertSame(doc, plugin.getDoc("Test.Document", context));
    verify(doc, xwiki);
  }

}
