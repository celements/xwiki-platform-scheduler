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
package com.celements.scheduler.job;

import java.net.MalformedURLException;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextException;
import org.xwiki.context.ExecutionContextManager;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.velocity.VelocityManager;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.scheduler.XWikiServletRequestStub;
import com.celements.scheduler.XWikiServletResponseStub;
import com.xpn.xwiki.ServerUrlUtilsRole;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.util.XWikiStubContextProvider;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiResponse;
import com.xpn.xwiki.web.XWikiServletRequest;
import com.xpn.xwiki.web.XWikiURLFactory;
import com.xpn.xwiki.web.XWikiURLFactoryService;

/**
 * Base class for any XWiki Quartz Job. This class take care of initializing ExecutionContext
 * properly.
 * <p>
 * A class extending {@link AbstractJob} should implements {@link #executeJob(JobExecutionContext)}.
 *
 * @since 2.90
 * @author fabian pichler
 */
public abstract class AbstractJob implements Job {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Override
  public final void execute(JobExecutionContext jobContext) throws JobExecutionException {
    JobDataMap data = jobContext.getJobDetail().getJobDataMap();
    XWikiContext xwikiContext = (XWikiContext) data.get("context");
    try {
      initExecutionContext(xwikiContext);
      executeJob(jobContext);
    } catch (Throwable exp) {
      getLogger().error("Exception thrown during job '{}' execution",
          jobContext.getJobDetail().getFullName(), exp);
      throw new JobExecutionException("Failed to execute job '"
          + jobContext.getJobDetail().getFullName() + "'", exp);
    } finally {
      // We must ensure we clean the ThreadLocal variables located in the Execution
      // component as otherwise we will have a potential memory leak.
      Utils.getComponent(Execution.class).removeContext();
      Utils.getComponentList(PostJobAction.class).forEach(runnable -> {
        try {
          runnable.accept(jobContext);
        } catch (Exception exc) {
          getLogger().error("failed to execute [{}]", runnable, exc);
        }
      });
    }
  }

  void initExecutionContext(XWikiContext xwikiContext) throws ExecutionContextException,
      DocumentNotExistsException, MalformedURLException {
    ExecutionContext ec = new ExecutionContext();
    XWikiContext scontext = createJobContext(xwikiContext);
    // Bridge with old XWiki Context, required for old code.
    ec.setProperty("xwikicontext", scontext);
    Utils.getComponent(ExecutionContextManager.class).initialize(ec);
    Utils.getComponent(Execution.class).setContext(ec);
    setupServerUrlAndFactory(scontext, xwikiContext);
    VelocityManager velocityManager = Utils.getComponent(VelocityManager.class);
    velocityManager.getVelocityContext();
  }

  /**
   * create a new copy of the xwiki context. Job Executions always use a different thread.
   * The xwiki context is NOT thread safe and may not be shared.
   * TODO CELDEV-534 use {@link XWikiStubContextProvider#createStubContext()}
   *
   * @throws DocumentNotExistsException
   * @throws MalformedURLException
   */
  XWikiContext createJobContext(XWikiContext xwikiContext) {
    final XWiki xwiki = xwikiContext.getWiki();
    final String database = xwikiContext.getDatabase();

    XWikiServletRequestStub dummy = new XWikiServletRequestStub();
    if (xwikiContext.getRequest() != null) {
      dummy.setHost(xwikiContext.getRequest().getHeader("x-forwarded-host"));
      dummy.setScheme(xwikiContext.getRequest().getScheme());
    } else if (xwikiContext.getURL() != null) {
      dummy.setHost(xwikiContext.getURL().getHost());
      dummy.setScheme(xwikiContext.getURL().getProtocol());
    }
    XWikiServletRequest request = new XWikiServletRequest(dummy);

    // Force forged context response to a stub response, since the current context response
    // will not mean anything anymore when running in the scheduler's thread, and can cause
    // errors.
    XWikiResponse response = new XWikiServletResponseStub();

    // IMPORTANT: do NOT clone xwikiContext. You would need to ensure that no reference or
    // unwanted value leaks in the new context.
    // IMPORTANT: following lines base on XWikiRequestInitializer.prepareContext
    XWikiContext scontext = new XWikiContext();
    scontext.setEngineContext(xwikiContext.getEngineContext());
    scontext.setRequest(request);
    scontext.setResponse(response);
    scontext.setAction("view");
    scontext.setDatabase(database);

    // feed the job context
    scontext.setUser(xwikiContext.getUser());
    scontext.setLanguage(xwikiContext.getLanguage());
    scontext.setMainXWiki(xwikiContext.getMainXWiki());
    scontext.setMode(XWikiContext.MODE_SERVLET);

    scontext.setWiki(xwiki);
    scontext.getWiki().getStore().cleanUp(scontext);

    scontext.flushClassCache();
    scontext.flushArchiveCache();
    return scontext;
  }

  void setupServerUrlAndFactory(XWikiContext scontext, XWikiContext xwikiContext)
      throws MalformedURLException, DocumentNotExistsException {
    scontext.setDoc(getModelAccess().getDocument(xwikiContext.getDoc().getDocumentReference()));
    scontext.setURL(Utils.getComponent(ServerUrlUtilsRole.class)
        .getServerURL(new WikiReference(xwikiContext.getDatabase()))
        .orElseThrow(() -> new IllegalArgumentException(
            "wiki [" + xwikiContext.getDatabase() + "] doesn't exist")));
    XWikiURLFactory urlFactory = xwikiContext.getURLFactory();
    if (urlFactory == null) {
      urlFactory = Utils.getComponent(XWikiURLFactoryService.class)
          .createURLFactory(scontext);
    }
    scontext.setURLFactory(urlFactory);
  }

  private IModelAccessFacade getModelAccess() {
    return Utils.getComponent(IModelAccessFacade.class);
  }

  protected Logger getLogger() {
    return logger;
  }

  protected abstract void executeJob(JobExecutionContext jobContext) throws JobExecutionException;
}
