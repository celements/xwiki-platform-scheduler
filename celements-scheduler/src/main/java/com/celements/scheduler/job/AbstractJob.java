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

import static com.google.common.base.Preconditions.*;

import java.util.function.Supplier;

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
import org.xwiki.velocity.VelocityManager;

import com.google.common.base.Strings;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

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

  protected final Supplier<Execution> execution = () -> Utils.getComponent(Execution.class);
  protected final Supplier<ExecutionContextManager> executionContextManager = () -> Utils
      .getComponent(ExecutionContextManager.class);

  @Override
  public final void execute(JobExecutionContext jobContext) throws JobExecutionException {
    JobDataMap data = jobContext.getJobDetail().getJobDataMap();
    try {
      initExecutionContext(data);
      executeJob(jobContext);
    } catch (Throwable exp) {
      getLogger().error("Exception thrown during job '{}' execution",
          jobContext.getJobDetail().getFullName(), exp);
      throw new JobExecutionException("Failed to execute job '"
          + jobContext.getJobDetail().getFullName() + "'", exp);
    } finally {
      // We must ensure we clean the ThreadLocal variables located in the Execution
      // component as otherwise we will have a potential memory leak.
      execution.get().removeContext();
      Utils.getComponentList(PostJobAction.class).forEach(runnable -> {
        try {
          runnable.accept(jobContext);
        } catch (Exception exc) {
          getLogger().error("failed to execute [{}]", runnable, exc);
        }
      });
    }
  }

  void initExecutionContext(JobDataMap data) throws ExecutionContextException {
    checkNotNull(data);
    ExecutionContext execContext = new ExecutionContext();
    execution.get().setContext(execContext);
    executionContextManager.get().initialize(execContext);
    prepareJobXWikiContext(data);
    VelocityManager velocityManager = Utils.getComponent(VelocityManager.class);
    velocityManager.getVelocityContext();
  }

  /**
   * Feed the stub context created by the ExecutionContextManager for the job execution thread.
   *
   * @param job
   *          the job data which the context will be fed with
   */
  void prepareJobXWikiContext(JobDataMap data) {
    XWikiContext context = getXWikiContext();
    context.setDoc(((XWikiDocument) data.get("jobDoc")).clone());
    context.setAction("view");
    context.setUser("XWiki.Scheduler");
    String cUser = data.getString("jobUser");
    if (!Strings.isNullOrEmpty(cUser)) {
      context.setUser(cUser);
    }
    String cLang = data.getString("jobLang");
    if (!Strings.isNullOrEmpty(cLang)) {
      context.setLanguage(cLang);
    }
    String cDb = data.getString("jobDatabase");
    if (!Strings.isNullOrEmpty(cDb)) {
      context.setDatabase(cDb);
    }
  }

  protected XWikiContext getXWikiContext() {
    return (XWikiContext) execution.get().getContext()
        .getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  protected Logger getLogger() {
    return logger;
  }

  protected abstract void executeJob(JobExecutionContext jobContext) throws JobExecutionException;
}
