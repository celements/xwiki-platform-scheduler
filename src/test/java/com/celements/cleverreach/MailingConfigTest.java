package com.celements.cleverreach;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;

public class MailingConfigTest extends AbstractComponentTest {

  private final String DEFAULT_ID = "12345678";
  private final String DEFAULT_SUBJECT = "Mailing Subject";
  private final String DEFAULT_HTML = "<html><body>HTML content.</body></html>";
  private final String DEFAULT_PLAIN = "Plain content.";

  private MailingConfig mailingConf;

  @Before
  public void setUp_MailingConfigTest() {
    setUpMailingConf(DEFAULT_HTML);
  }

  @Test
  public void testEmpty() {
    try {
      new MailingConfig.Builder().build();
      fail("Exception expected: Missing ID");
    } catch (IllegalArgumentException iae) {
      // expected result
    }
  }

  @Test
  public void testIdOnly() {
    new MailingConfig.Builder().setId(DEFAULT_ID).build();
  }

  @Test
  public void testGetId() {
    assertEquals(DEFAULT_ID, mailingConf.getId());
  }

  @Test
  public void testGetSubject() {
    assertEquals(DEFAULT_SUBJECT, mailingConf.getSubject());
  }

  @Test
  public void testGetContentHtml() {
    assertEquals(DEFAULT_HTML, mailingConf.getContentHtml());
  }

  @Test
  public void testGetContentPlain() {
    assertEquals(DEFAULT_PLAIN, mailingConf.getContentPlain());
  }

  @Test
  public void testGetContentHtmlCleanXml() {
    setUpMailingConf("<div>&nbsp;</div>\n");
    assertEquals("<div>&#160;</div>\n\n", mailingConf.getContentHtmlCleanXml());
  }

  @Test
  public void testGetContentHtmlCssInlined() throws Exception {
    setUpMailingConf(
        "<!DOCTYPE html><html><head></head><body><div><div>&nbsp;</div>\n<p class=\"unsubscribe\">"
            + "Um auf <span class=\"link\">{EMAIL}</span> die Tagesagenda in Zukunft nicht mehr zu "
            + "erhalten können Sie sich <span class=\"link\"><a href=\"{UNSUBSCRIBE}\">hier "
            + "abmelden</a></span>.<span>$hi</span></p></div></body></html>");
    String expect = "<!DOCTYPE html><html";
    String inlined = mailingConf.getContentHtmlCssInlined();
    assertTrue(getExpectationMessage(expect, inlined), inlined.contains(expect));
    assertFalse("Result contains [<?xml] and shouldn't ", inlined.contains("<?xml"));
    assertTrue("Should contain 'style=', but is [" + inlined + "]", inlined.contains("style="));
  }

  private void setUpMailingConf(String html) {
    mailingConf = new MailingConfig.Builder().setId(DEFAULT_ID).setSubject(
        DEFAULT_SUBJECT).setContentHtml(html).setContentPlain(DEFAULT_PLAIN).build();
  }

  private String getExpectationMessage(String expected, String result) {
    return "expected result to contain [" + expected + "], but was [" + result + "]";
  }

}
