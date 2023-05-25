package com.celements.cleverreach;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;

public class CleverReachConnectionTest extends AbstractComponentTest {

  private final String DEFAULT_TEST_BASE_URL = "https://www.test-connection-url.com";

  private CleverReachConnection connection;
  private CleverReachToken token;

  @Before
  public void setUp_CleverReachConnectionTest() {
    token = new CleverReachToken(3600000);
    connection = new CleverReachConnection(DEFAULT_TEST_BASE_URL, token);
  }

  @Test
  public void testConstructors() {
    connection = new CleverReachConnection();
    assertEquals(CleverReachRest.DEFAULT_REST_URL, connection.getBaseUrl());
    assertFalse(connection.isConnected());
    connection = new CleverReachConnection(DEFAULT_TEST_BASE_URL);
    assertEquals(DEFAULT_TEST_BASE_URL, connection.getBaseUrl());
    assertFalse(connection.isConnected());
    connection = new CleverReachConnection((CleverReachToken) null);
    assertEquals(CleverReachRest.DEFAULT_REST_URL, connection.getBaseUrl());
    assertFalse(connection.isConnected());
    connection = new CleverReachConnection(DEFAULT_TEST_BASE_URL, (CleverReachToken) null);
    assertEquals(DEFAULT_TEST_BASE_URL, connection.getBaseUrl());
    assertFalse(connection.isConnected());
  }

  @Test
  public void testGetBaseUrl_default() {
    connection = new CleverReachConnection();
    assertEquals(CleverReachRest.DEFAULT_REST_URL, connection.getBaseUrl());
  }

  @Test
  public void testGetBaseUrl() {
    assertEquals(DEFAULT_TEST_BASE_URL, connection.getBaseUrl());
  }

  @Test
  public void testGetToken() {
    assertSame(token, connection.getToken().get());
  }

  @Test
  public void testSetToken() {
    assertSame(token, connection.getToken().get());
    CleverReachToken newToken = new CleverReachToken(1000);
    connection.setToken(newToken);
    assertSame(newToken, connection.getToken().get());
  }

  @Test
  public void testIsConnected() {
    assertTrue(connection.isConnected());
    connection = new CleverReachConnection((CleverReachToken) null);
    assertFalse(connection.isConnected());
  }

}
