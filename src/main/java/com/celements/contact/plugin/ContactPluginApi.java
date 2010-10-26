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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Api;

public class ContactPluginApi extends Api {
  
  private static Log mLogger = LogFactory.getFactory().getInstance(ContactPluginApi.class);
  
  private ContactPlugin plugin;
  
  public ContactPluginApi(ContactPlugin plugin, XWikiContext context) {
    super(context);
    setPlugin(plugin);
  }
  
  public void setPlugin(ContactPlugin plugin) {
    this.plugin = plugin;
  }
  
  public ContactPlugin getPlugin(){
    return plugin;
  }
  
  public boolean saveAllContact(String docFullName) {
    boolean cont = saveContact(docFullName);
    cont &= saveAddress(docFullName);
    return cont;
  }
  
  public boolean saveContact(String docFullName) {
    return plugin.saveContact(docFullName, context);
  }
  
  public boolean saveAddress(String docFullName) {
    return plugin.saveAddress(docFullName, context);
  }
}