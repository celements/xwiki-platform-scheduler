package com.celements.cleverreach;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;

public class CleverReachTokenTest extends AbstractComponentTest {

  private final String DEFAULT_TOKEN = "123tokenvalue321";
  private final long DEFAULT_EXPIRES = 3600000;
  private final String DEFAULT_TYPE = "tokenType";
  private final String DEFAULT_SCOPE = "tokenScope";
  private CleverReachToken token;

  @Before
  public void setUp_CleverReachTokenTest() {
    token = new CleverReachToken(DEFAULT_TOKEN, DEFAULT_EXPIRES, DEFAULT_TYPE, DEFAULT_SCOPE);
  }

  @Test
  public void testGetExpiry() {
    assertTrue(token.getExpiry().after(new Date()));
  }

  @Test
  public void testSetExpiry() {
    token.setExpiry(0);
    assertTrue(token.getExpiry().before(new Date()));
  }

  @Test
  public void testGetToken() {
    assertEquals(DEFAULT_TOKEN, token.getToken());
  }

  @Test
  public void testIsValid() {
    assertTrue(token.isValid());
  }

  @Test
  public void testGetTokenType() {
    assertEquals(DEFAULT_TYPE.replaceFirst("^t(.*)$", "T$1"), token.getTokenType());
  }

  @Test
  public void testGetScope() {
    assertEquals(DEFAULT_SCOPE, token.getScope());
  }

}
