package com.celements.cleverreach;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;

import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.web.Utils;

public class CleverReachRestTest extends AbstractComponentTest {

  private CleverReachRest rest;
  private IRestClientFactoryRole clientBuilder;

  @Before
  public void setUp_CleverReachRestTest() throws Exception {
    clientBuilder = registerComponentMock(IRestClientFactoryRole.class);
    rest = (CleverReachRest) Utils.getComponent(CleverReachService.class,
        CleverReachRest.COMPONENT_NAME);
  }

  @Test
  public void testSendRequest() {
    String baseUrl = "http://www.test-base-url.test";
    String path = "/test/path";
    String token = "token123";
    Client client = createMockAndAddToDefault(Client.class);
    WebTarget target = createMockAndAddToDefault(WebTarget.class);
    expect(clientBuilder.newClient()).andReturn(client);
    expect(client.target(baseUrl)).andReturn(target);
    expect(target.path(path)).andReturn(target);
    Builder builder = createMockAndAddToDefault(Builder.class);
    expect(target.request()).andReturn(builder);
    Response response = createMockAndAddToDefault(Response.class);
    expect(builder.header("Authorization", token)).andReturn(builder);
    expect(builder.post((Entity<?>) anyObject())).andReturn(response);
    replayDefault();
    Response resultResponse = rest.sendRequest(path, getTestMailingEntity(), token,
        CleverReachRest.SubmitMethod.POST, baseUrl);
    verifyDefault();
    assertSame(response, resultResponse);
  }

  @Test
  public void testGetRequestDataEntity_Mailing() {
    Mailing mailing = (Mailing) getTestMailingEntity();
    Entity<?> entity = rest.getRequestDataEntity(mailing);
    assertEquals("{\"subject\":\"" + mailing.subject + "\",\"content\":{\"html\":\""
        + mailing.content.html + "\",\"text\":\"" + mailing.content.text + "\"}}",
        entity.getEntity());
  }

  @Test
  public void testGetConnection() throws Exception {
    CleverReachConnection connection = new CleverReachConnection(new CleverReachToken(3600000));
    Utils.getComponent(Execution.class).getContext().setProperty(
        CleverReachRest.CONTEXT_CONNECTION_KEY, connection);
    assertSame(connection, rest.getConnection());
  }

  @Test
  public void testGetRequestDataEntity_MultivaluedMap() {
    MultivaluedMap<String, String> map = getTestMultivaluedMap();
    Entity<?> entity = rest.getRequestDataEntity(map);
    assertTrue(entity.getEntity() instanceof Form);
  }

  @Test
  public void testAddGetParameters_methodGet() {
    MultivaluedMap<String, String> map = getTestMultivaluedMap();
    WebTarget target = createMockAndAddToDefault(WebTarget.class);
    expect(target.queryParam("a", new Object[] { "1" })).andReturn(target);
    expect(target.queryParam("b", new Object[] { "2" })).andReturn(target);
    expect(target.queryParam("c", new Object[] { "3" })).andReturn(target);
    replayDefault();
    rest.addGetParameters(map, target, CleverReachRest.SubmitMethod.GET);
    verifyDefault();
  }

  @Test
  public void testAddGetParameters_methodOther() {
    rest.addGetParameters(null, null, CleverReachRest.SubmitMethod.POST);
  }

  private Object getTestMailingEntity() {
    Mailing mailing = new Mailing();
    mailing.subject = "The Subject";
    mailing.content.html = "<html></html>";
    mailing.content.text = "plain";
    return mailing;
  }

  private MultivaluedMap<String, String> getTestMultivaluedMap() {
    MultivaluedMap<String, String> map = new MultivaluedHashMap<>();
    map.putSingle("a", "1");
    map.putSingle("b", "2");
    map.putSingle("c", "3");
    return map;
  }

}
