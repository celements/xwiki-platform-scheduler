package com.celements.scheduler.job;

import static com.xpn.xwiki.XWikiExecutionProp.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.net.URI;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.init.XWikiProvider;
import com.celements.scheduler.XWikiServletRequestStub;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.doc.XWikiDocument;

public class AbstractJobTest extends AbstractComponentTest {

  private TestJob testJob;
  private DocumentReference testDocRef;

  @Before
  public void setUp_AbstractJobTest() throws Exception {
    testDocRef = new DocumentReference(getXContext().getDatabase(), "TestSpace", "testDoc");
    getXContext().setDoc(new XWikiDocument(testDocRef));
    getXContext().setRequest(new XWikiServletRequestStub());
    getXContext().setUri(URI.create("http://testhost:8015/testfile"));
    testJob = new TestJob();
    expect(registerComponentMock(XWikiProvider.class).get())
        .andReturn(Optional.of(getMock(XWiki.class))).anyTimes();
  }

  @Test
  public void test_initExecutionContext() throws Exception {
    assertNull(getXContext().get("vcontext"));
    JobDataMap data = new JobDataMap();
    XWikiDocument jobDoc = new XWikiDocument(testDocRef);
    data.put("jobDoc", jobDoc);
    data.put("jobUser", "XWiki.MyJobUser");
    data.put("jobDatabase", "thedb");
    replayDefault();
    ExecutionContext eContext = testJob.initExecutionContext(data);
    verifyDefault();
    assertNotNull(getXContext().get("vcontext"));
    assertNotSame(jobDoc, eContext.get(DOC).get());
    assertEquals("thedb", eContext.get(WIKI).get().getName());
    assertEquals(jobDoc.getDocumentReference(), eContext.get(DOC).get().getDocumentReference());
    assertEquals("XWiki.MyJobUser", getXContext().getUser());
  }

  private class TestJob extends AbstractJob {

    @Override
    protected Logger getLogger() {
      return LoggerFactory.getLogger(TestJob.class);
    }

    @Override
    protected void executeJob(JobExecutionContext jobContext) throws JobExecutionException {}

  }

}
