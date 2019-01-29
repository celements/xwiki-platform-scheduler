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
    mailingConf = new MailingConfig.Builder().setId(DEFAULT_ID).setSubject(
        DEFAULT_SUBJECT).setContentHtml(DEFAULT_HTML).setContentPlain(DEFAULT_PLAIN).build();
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

}
