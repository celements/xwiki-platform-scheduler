package com.celements.cleverreach.exception;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;

public class CssInlineExceptionTest extends AbstractComponentTest {

  @Test
  public void testScanLines_message_ampersand() throws Exception {
    String html = IOUtils.toString(this.getClass().getResourceAsStream(
        "/test_ampersand.html"), "UTF-8");
    String result = new CssInlineException(html, null).scanLines(STACK_TRACE1, 1, 1,
        CssInlineException.SHORT_MESSAGE);
    assertTrue("Result should start with line number [" + LINE_NR1 + "] but is [" + result + "]",
        result.startsWith(LINE_NR1));
    assertTrue("Result should end with message. Result: [" + result + "]", result.trim().endsWith(
        MESSAGE1));
  }

  @Test
  public void testScanLines_html_ampersand() throws Exception {
    String html = IOUtils.toString(this.getClass().getResourceAsStream(
        "/test_ampersand.html"), "UTF-8");
    String result = new CssInlineException(html, null).scanLines(html, 245, 249,
        CssInlineException.SNIPPET);
    assertTrue("Result should be around line [" + LINE_NR1 + "] but is [" + result + "]",
        result.startsWith("245: ") && result.contains("247: <b>Jazzmatizz"));
  }

  @Test
  public void testScanLines_message_nonClosingTag() throws IOException {
    String html = IOUtils.toString(this.getClass().getResourceAsStream(
        "/FailingCssInlnlineNonClosingDiv.html"), "UTF-8");
    Exception excp = createMockAndAddToDefault(Exception.class);
    expect(excp.getCause()).andReturn(excp).anyTimes();
    expect(excp.getMessage()).andReturn(MESSAGE2).anyTimes();
    excp.printStackTrace(anyObject(PrintWriter.class));
    expectLastCall();
    CssInlineException inlineExcp = new CssInlineException(html, excp);
    inlineExcp.injected_sw = new StringWriter();
    inlineExcp.injected_sw.append(STACK_TRACE2);
    inlineExcp.injected_excp = excp;
    replayDefault();
    String result = inlineExcp.getExceptionRangeSnippet();
    verifyDefault();
    assertTrue("Result should start with line number [" + MESSAGE2 + "] but is [" + result + "]",
        result.startsWith(MESSAGE2));
    assertTrue("Result should contain [" + result + "]", result.contains("691: Eintritt") && result
        .contains("694: <a href="));
  }

  @Test
  public void testScanLines_html_nonClosingTag() throws IOException {
    String html = IOUtils.toString(this.getClass().getResourceAsStream(
        "/FailingCssInlnlineNonClosingDiv.html"), "UTF-8");
    String result = new CssInlineException(html, null).scanLines(html, 690, 694,
        CssInlineException.SNIPPET);
    assertTrue("Result should be around line [" + LINE_NR2 + "] but is [" + result + "]",
        result.startsWith("690: <b>ABGESAGT") && result.contains("691: Eintritt"));
  }

  static final String LINE_NR1 = "247";

  static final String MESSAGE1 = "The entity name must immediately follow the '&' in the entity reference. "
      + "Nested exception: The entity name must immediately follow the '&' in the entity reference.";

  static final String STACK_TRACE1 = "org.dom4j.DocumentException: Error on line " + LINE_NR1
      + " of document  : "
      + MESSAGE1 + "\n        at org.dom4j.io.SAXReader.read(SAXReader.java:482)\n"
      + "        at com.celements.cleverreach.util.DefaultCssInliner.prepareInput(DefaultCssInliner.java:65)\n"
      + "        at com.celements.cleverreach.util.DefaultCssInliner.inline(DefaultCssInliner.java:48)\n"
      + "        at com.celements.cleverreach.util.DefaultCssInliner.inline(DefaultCssInliner.java:39)\n"
      + "        at com.celements.cleverreach.MailingConfig.getContentHtmlCssInlined(MailingConfig.java:88)\n"
      + "        at com.celements.cleverreach.CleverReachRest.updateMailing(CleverReachRest.java:82)\n"
      + "        at ch.programmonline.proz.TagesagendaCleverReachUpdateJob.executeJob(TagesagendaCleverReachUpdateJob.java:69)\n"
      + "        at com.celements.scheduler.job.AbstractJob.execute(AbstractJob.java:80)\n"
      + "        at org.quartz.core.JobRunShell.run(JobRunShell.java:202)\n"
      + "        at org.quartz.simpl.SimpleThreadPool$WorkerThread.run(SimpleThreadPool.java:525)\n"
      + "Nested exception:\n"
      + "org.xml.sax.SAXParseException; lineNumber: 247; columnNumber: 114; The entity name must immediately follow the '&' in the entity reference.\n"
      + "        at org.apache.xerces.util.ErrorHandlerWrapper.createSAXParseException(Unknown Source)\n"
      + "        at org.apache.xerces.util.ErrorHandlerWrapper.fatalError(Unknown Source)\n"
      + "        at org.apache.xerces.impl.XMLErrorReporter.reportError(Unknown Source)\n"
      + "        at org.apache.xerces.impl.XMLErrorReporter.reportError(Unknown Source)\n"
      + "        at org.apache.xerces.impl.XMLScanner.reportFatalError(Unknown Source)\n"
      + "        at org.apache.xerces.impl.XMLDocumentFragmentScannerImpl.scanEntityReference(Unknown Source)\n"
      + "        at org.apache.xerces.impl.XMLDocumentFragmentScannerImpl$FragmentContentDispatcher.dispatch(Unknown Source)\n"
      + "        at org.apache.xerces.impl.XMLDocumentFragmentScannerImpl.scanDocument(Unknown Source)\n"
      + "        at org.apache.xerces.parsers.XML11Configuration.parse(Unknown Source)\n"
      + "        at org.apache.xerces.parsers.XML11Configuration.parse(Unknown Source)\n"
      + "        at org.apache.xerces.parsers.XMLParser.parse(Unknown Source)\n"
      + "        at org.apache.xerces.parsers.AbstractSAXParser.parse(Unknown Source)\n"
      + "        at org.apache.xerces.jaxp.SAXParserImpl$JAXPSAXParser.parse(Unknown Source)\n"
      + "        at org.dom4j.io.SAXReader.read(SAXReader.java:465)\n"
      + "        at com.celements.cleverreach.util.DefaultCssInliner.prepareInput(DefaultCssInliner.java:65)\n"
      + "        at com.celements.cleverreach.util.DefaultCssInliner.inline(DefaultCssInliner.java:48)\n"
      + "        at com.celements.cleverreach.util.DefaultCssInliner.inline(DefaultCssInliner.java:39)\n"
      + "        at com.celements.cleverreach.MailingConfig.getContentHtmlCssInlined(MailingConfig.java:88)\n"
      + "        at com.celements.cleverreach.CleverReachRest.updateMailing(CleverReachRest.java:82)\n"
      + "        at ch.programmonline.proz.TagesagendaCleverReachUpdateJob.executeJob(TagesagendaCleverReachUpdateJob.java:69)\n"
      + "        at com.celements.scheduler.job.AbstractJob.execute(AbstractJob.java:80)\n"
      + "        at org.quartz.core.JobRunShell.run(JobRunShell.java:202)\n"
      + "        at org.quartz.simpl.SimpleThreadPool$WorkerThread.run(SimpleThreadPool.java:525)";

  private static final String EXCEPTION_CAUSE_5_LINES1 = "        <td><div class=\"td\">\n"
      + "                      <a href=\"https://programmzeitung.ch/app/Veranstaltung?col=Content.Webseite&amp;event=programmzeitung:ProgonEvent.ProgonEvent128127&amp;performance=453165\" class=\"veranstaltung\" target=\"_blank\"><span style=\"color: #015470; text-decoration: none !important;\"><font color=\"#015470\" style=\"border-top-style: none; border-right-style: none; border-bottom-style: none; border-left-style: none; text-decoration: none !important;\">\n"
      + "                    <b>Jazzmatizz</b></font></span></a>          Niko Seibold (sax) Meets Christoph Neuhaus (g) & Thomas Bauser (h-org)\n"
      + "                      &nbsp;&#x25c6;&nbsp;\n"
      + "                    Jazz. Eintritt frei\n";

  static final String LINE_NR2 = "692";

  static final String MESSAGE2 = "The element type \"div\" must be terminated by the matching "
      + "end-tag \"</div>\". Nested exception: The element type \"div\" must be terminated by the "
      + "matching end-tag \"</div>\".";

  static final String STACK_TRACE2 = "com.celements.cleverreach.exception.CssInlineException: CSS could not be inlined.\n"
      + "        at com.celements.cleverreach.util.DefaultCssInliner.inline(DefaultCssInliner.java:60)\n"
      + "        at com.celements.cleverreach.util.DefaultCssInliner.inline(DefaultCssInliner.java:42)\n"
      + "        at com.celements.cleverreach.MailingConfig.getContentHtmlCssInlined(MailingConfig.java:127)\n"
      + "        at com.celements.cleverreach.CleverReachRest.buildMailing(CleverReachRest.java:153)\n"
      + "        at com.celements.cleverreach.CleverReachRest.updateMailingInternal(CleverReachRest.java:128)\n"
      + "        at com.celements.cleverreach.CleverReachRest.updateMailingRehearsal(CleverReachRest.java:117)\n"
      + "        at ch.programmonline.proz.TagesagendaCleverReachUpdateRehearsalJob.executeJob(TagesagendaCleverReachUpdateRehearsalJob.java:31)\n"
      + "        at com.celements.scheduler.job.AbstractJob.execute(AbstractJob.java:80)\n" +
      "        at org.quartz.core.JobRunShell.run(JobRunShell.java:202)\n" +
      "        at org.quartz.simpl.SimpleThreadPool$WorkerThread.run(SimpleThreadPool.java:525)\n" +
      "Caused by: org.dom4j.DocumentException: Error on line " + LINE_NR2 + " of document  : "
      + MESSAGE2 + "\n" + "        at org.dom4j.io.SAXReader.read(SAXReader.java:482)\n" +
      "        at com.celements.cleverreach.util.DefaultCssInliner.prepareInput(DefaultCssInliner.java:69)\n"
      + "        at com.celements.cleverreach.util.DefaultCssInliner.inline(DefaultCssInliner.java:52)\n"
      + "        ... 9 more";

  static final String STACK_TRACE3 = "com.celements.cleverreach.exception.CssInlineException: CSS could not be inlined.\n"
      + "        at com.celements.cleverreach.util.DefaultCssInliner.inline(DefaultCssInliner.java:60)\n"
      + "        at com.celements.cleverreach.util.DefaultCssInliner.inline(DefaultCssInliner.java:42)\n"
      + "        at com.celements.cleverreach.MailingConfig.getContentHtmlCssInlined(MailingConfig.java:127)\n"
      + "        at com.celements.cleverreach.CleverReachRest.buildMailing(CleverReachRest.java:153)\n"
      + "        at com.celements.cleverreach.CleverReachRest.updateMailingInternal(CleverReachRest.java:128)\n"
      + "        at com.celements.cleverreach.CleverReachRest.updateMailingRehearsal(CleverReachRest.java:117)\n"
      + "        at ch.programmonline.proz.TagesagendaCleverReachUpdateRehearsalJob.executeJob(TagesagendaCleverReachUpdateRehearsalJob.java:31)\n"
      + "        at com.celements.scheduler.job.AbstractJob.execute(AbstractJob.java:80)\n" +
      "        at org.quartz.core.JobRunShell.run(JobRunShell.java:202)\n" +
      "        at org.quartz.simpl.SimpleThreadPool$WorkerThread.run(SimpleThreadPool.java:525)\n" +
      "        ... 9 more";
  /*
   * String EXCEPTION_CAUSE2 =
   * "                    <b>ABGESAGT: &amp; Vortrag der NGiB</b></font></span></a>\n" +
   * "                    Eintritt frei. <div>&amp; Infos: <a href=\"http://programmzeitung.progdev.sneakapeek.ch:8015/ProgonEvent/ProgonEvent138884?xpage=celements_ajax&amp;ajax_mode=redirectURL&amp;url=http://www.ngib.ch\" rel=\"nofollow\" target=\"_blank\"><span style=\"color: #015470; text-decoration: none !important;\"><font color=\"#015470\" style=\"border-top-style: none; border-right-style: none; border-bottom-style: none; border-left-style: none; text-decoration: none !important;\">www.ngib.ch</font></span></a>\n"
   * +
   * "                            </div></td>\n"+
   * "        <td><div class=\"td\">\n"+
   * "                                                        <a href=\"http://programmzeitung.cel.sneakapeek.ch/app/Veranstaltung?col=Content.Webseite&amp;event=programmzeitung:ProgonEvent.ProgonEvent138884&amp;performance=454596\" class=\"veranstaltung\" target=\"_blank\"><span style=\"color: #015470; text-decoration: none !important;\"><font color=\"#015470\" style=\"border-top-style: none; border-right-style: none; border-bottom-style: none; border-left-style: none; text-decoration: none !important;\">"
   * ;
   */
}
