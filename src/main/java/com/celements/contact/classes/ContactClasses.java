package com.celements.contact.classes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.celements.common.classes.CelementsClassCollection;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;

public class ContactClasses extends CelementsClassCollection {
  
  private static Log mLogger = LogFactory.getFactory().getInstance(
      ContactClasses.class);
  
  private static ContactClasses instance;
  
  public void initClasses(XWikiContext context) throws XWikiException {
    getContactClass(context);
  }
  
  private ContactClasses() {
  }
  
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
    } catch (XWikiException e) {
      mLogger.error(e);
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
    needsUpdate |= bclass.addStaticListField("sex", "Sex", 1, false, "female|male", 
        "select", "|");
    
    if(!"internal".equals(bclass.getCustomMapping())){
      needsUpdate = true;
      bclass.setCustomMapping("internal");
    }
    
    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }
  
  public String getConfigName() {
    return "manualizer";
  }
  
  @Override
  protected Log getLogger() {
    return mLogger;
  }
}
