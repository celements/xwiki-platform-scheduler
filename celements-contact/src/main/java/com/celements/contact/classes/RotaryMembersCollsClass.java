package com.celements.contact.classes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celements.common.classes.CelementsClassCollection;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;

public class RotaryMembersCollsClass extends CelementsClassCollection {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      RotaryMembersCollsClass.class);

  private static RotaryMembersCollsClass instance;

  @Override
  public void initClasses(XWikiContext context) throws XWikiException {
    getMemberClass(context);
  }

  private RotaryMembersCollsClass() {}

  public static RotaryMembersCollsClass getInstance() {
    if (instance == null) {
      instance = new RotaryMembersCollsClass();
    }
    return instance;
  }

  protected BaseClass getMemberClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;

    try {
      doc = xwiki.getDocument("Classes.MemberClass", context);
    } catch (XWikiException xwe) {
      LOGGER.error("getMemberClass failed", xwe);
      doc = new XWikiDocument();
      doc.setSpace("Classes");
      doc.setName("MemberClass");
      needsUpdate = true;
    }

    BaseClass bclass = doc.getxWikiClass();
    bclass.setName("Classes.MemberClass");

    needsUpdate |= bclass.addTextField("amt", "amt", 30);
    needsUpdate |= bclass.addTextField("titel", "titel", 30);
    needsUpdate |= bclass.addTextField("status", "status", 30);
    needsUpdate |= bclass.addTextField("eintritt", "eintritt", 30);
    needsUpdate |= bclass.addTextField("austritt", "austritt", 30);
    needsUpdate |= bclass.addTextField("ehepartner", "ehepartner", 30);
    needsUpdate |= bclass.addTextField("email_geschaeft", "email_geschaeft", 30);
    needsUpdate |= bclass.addTextField("email_privat", "email_privat", 30);
    needsUpdate |= bclass.addTextField("fax_geschaeft", "fax_geschaeft", 30);
    needsUpdate |= bclass.addTextField("fax_privat", "fax_privat", 30);
    needsUpdate |= bclass.addTextField("firmenname_geschaeft", "firmenname_geschaeft", 30);
    needsUpdate |= bclass.addTextField("geburtsdatum", "geburtsdatum", 30);
    needsUpdate |= bclass.addTextField("klassifikation", "klassifikation", 30);
    needsUpdate |= bclass.addTextField("mobile_geschaeft", "mobile_geschaeft", 30);
    needsUpdate |= bclass.addTextField("mobile_privat", "mobile_privat", 30);
    needsUpdate |= bclass.addTextField("ort_geschaeft", "ort_geschaeft", 30);
    needsUpdate |= bclass.addTextField("ort_privat", "ort_privat", 30);
    needsUpdate |= bclass.addTextField("plz_geschaeft", "plz_geschaeft", 30);
    needsUpdate |= bclass.addTextField("plz_privat", "plz_privat", 30);
    needsUpdate |= bclass.addTextField("position_geschaeft", "position_geschaeft", 30);
    needsUpdate |= bclass.addTextField("strasse_geschaeft", "strasse_geschaeft", 30);
    needsUpdate |= bclass.addTextField("strasse_privat", "strasse_privat", 30);
    needsUpdate |= bclass.addTextField("telefon_geschaeft", "telefon_geschaeft", 30);
    needsUpdate |= bclass.addTextField("telefon_privat", "telefon_privat", 30);
    needsUpdate |= bclass.addTextField("todestag", "todestag", 30);

    if (!"internal".equals(bclass.getCustomMapping())) {
      needsUpdate = true;
      bclass.setCustomMapping("internal");
    }

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  @Override
  public String getConfigName() {
    return "MembersColls";
  }
}
