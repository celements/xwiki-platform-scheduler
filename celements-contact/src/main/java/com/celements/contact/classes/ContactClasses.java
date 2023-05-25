package com.celements.contact.classes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celements.common.classes.CelementsClassCollection;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;

public class ContactClasses extends CelementsClassCollection {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      ContactClasses.class);

  private static ContactClasses instance;

  @Override
  public void initClasses(XWikiContext context) throws XWikiException {
    getContactClass(context);
    getContactAddressClass(context);
  }

  private ContactClasses() {}

  public static ContactClasses getInstance() {
    if (instance == null) {
      instance = new ContactClasses();
    }
    return instance;
  }

  protected BaseClass getContactClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;

    try {
      doc = xwiki.getDocument("Celements.ContactClass", context);
    } catch (XWikiException xwe) {
      LOGGER.error("getContactClass failed", xwe);
      doc = new XWikiDocument();
      doc.setSpace("Celements");
      doc.setName("ContactClass");
      needsUpdate = true;
    }

    BaseClass bclass = doc.getxWikiClass();
    bclass.setName("Celements.ContactClass");

    needsUpdate |= bclass.addTextField("title", "Title", 30);
    needsUpdate |= bclass.addTextField("firstname", "First Name", 30);
    needsUpdate |= bclass.addTextField("lastname", "Last Name", 30);
    needsUpdate |= bclass.addStaticListField("sex", "Sex", 1, false, "---|female|male",
        "select", "|");

    if (!"internal".equals(bclass.getCustomMapping())) {
      needsUpdate = true;
      bclass.setCustomMapping("internal");
    }

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  protected BaseClass getContactAddressClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;

    try {
      doc = xwiki.getDocument("Celements.ContactAddressClass", context);
    } catch (XWikiException xwe) {
      LOGGER.error("getContactAddressClass failed", xwe);
      doc = new XWikiDocument();
      doc.setSpace("Celements");
      doc.setName("ContactAddressClass");
      needsUpdate = true;
    }

    BaseClass bclass = doc.getxWikiClass();
    bclass.setName("Celements.ContactAddressClass");
    needsUpdate |= bclass.addTextField("street_nr", "Street and Number", 30);
    needsUpdate |= bclass.addTextField("zip", "ZIP", 30);
    needsUpdate |= bclass.addTextField("city", "City", 30);
    needsUpdate |= bclass.addTextField("country", "Country", 30);

    if (!"internal".equals(bclass.getCustomMapping())) {
      needsUpdate = true;
      bclass.setCustomMapping("internal");
    }

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  @Override
  public String getConfigName() {
    return "contact";
  }
}
