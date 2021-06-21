/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.contact.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celements.contact.classes.ContactClasses;
import com.celements.contact.classes.RotaryMembersCollsClass;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.plugin.XWikiDefaultPlugin;
import com.xpn.xwiki.plugin.XWikiPluginInterface;
import com.xpn.xwiki.web.XWikiRequest;

public class ContactPlugin extends XWikiDefaultPlugin {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContactPlugin.class);

  public ContactPlugin(String name, String className, XWikiContext context) {
    super(name, className, context);
    init(context);
  }

  @Override
  public Api getPluginApi(XWikiPluginInterface plugin, XWikiContext context) {
    return new ContactPluginApi((ContactPlugin) plugin, context);
  }

  @Override
  public String getName() {
    LOGGER.debug("Entered method getName");
    return "celcontact";
  }

  @Override
  public void init(XWikiContext context) {
    super.init(context);
  }

  @Override
  public void virtualInit(XWikiContext context) {
    try {
      ContactClasses.getInstance().runUpdate(context);
      RotaryMembersCollsClass.getInstance().runUpdate(context);
    } catch (Exception xwe) {
      LOGGER.error("virtualInit", xwe);
    }
    super.virtualInit(context);
  }

  public boolean saveContact(String docFullName, XWikiContext context) {
    XWikiRequest req = context.getRequest();
    XWikiDocument doc = getDoc(docFullName, context);
    BaseObject obj = getObject("Celements.ContactClass", doc, context);
    boolean dirty = false;
    dirty |= setStringField("title", req.get("title"), obj);
    dirty |= setStringField("firstname", req.get("firstname"), obj);
    dirty |= setStringField("lastname", req.get("lastname"), obj);
    dirty |= setStringField("sex", req.get("sex"), obj);
    if (dirty) {
      try {
        context.getWiki().saveDocument(doc, context);
        dirty = false;
      } catch (XWikiException e) {
        LOGGER.error("Could not save '" + doc.getFullName() + "'", e);
      }
    }
    return !dirty;
  }

  public boolean saveAddress(String docFullName, XWikiContext context) {
    XWikiRequest req = context.getRequest();
    XWikiDocument doc = getDoc(docFullName, context);
    BaseObject obj = getObject("Celements.ContactAddressClass", doc, context);
    boolean dirty = false;
    dirty |= setStringField("street_nr", req.get("street_nr"), obj);
    dirty |= setStringField("zip", req.get("zip"), obj);
    dirty |= setStringField("city", req.get("city"), obj);
    dirty |= setStringField("country", req.get("country"), obj);
    if (dirty) {
      try {
        context.getWiki().saveDocument(doc, context);
        dirty = false;
      } catch (XWikiException e) {
        LOGGER.error("Could not save '" + doc.getFullName() + "'", e);
      }
    }
    return !dirty;
  }

  BaseObject getObject(String className, XWikiDocument doc, XWikiContext context) {
    BaseObject obj = doc.getObject(className);
    if (obj == null) {
      try {
        obj = doc.newObject(className, context);
        context.getWiki().saveDocument(doc, context);
      } catch (XWikiException e) {
        LOGGER.error("Could not add a new '" + className + "' object to document '" +
            doc.getFullName() + "'", e);
      }
    }
    return obj;
  }

  boolean setStringField(String field, String value, BaseObject obj) {
    if ((value != null) && !"".equals(value.trim())) {
      obj.setStringValue(field, value);
      return true;
    }
    return false;
  }

  XWikiDocument getDoc(String fullName, XWikiContext context) {
    XWikiDocument doc = context.getDoc();
    if ((fullName != null) && context.getWiki().exists(fullName, context)) {
      try {
        doc = context.getWiki().getDocument(fullName, context);
      } catch (XWikiException e) {
        LOGGER.error("Could not get document '" + fullName + "'", e);
      }
    }
    return doc;
  }
}
