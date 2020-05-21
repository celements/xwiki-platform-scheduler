package com.celements.cleverreach;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;

import com.celements.cleverreach.CleverReachRest.ResponseBodyObj;
import com.celements.cleverreach.CleverReachService.ServerClass;
import com.celements.cleverreach.exception.CleverReachRequestFailedException;
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
  public void testSendRequest() throws CleverReachRequestFailedException {
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
    rest.addGetParameters(map, new AtomicReference<>(target),
        CleverReachRest.SubmitMethod.GET);
    verifyDefault();
  }

  @Test
  public void testAddGetParameters_methodOther() {
    rest.addGetParameters(null, null, CleverReachRest.SubmitMethod.POST);
  }

  @Test
  public void testIsReadyToSend_get_response() {
    String responseContent = "[\n" +
        "  {\n" +
        "    \"name\": \"vorname\",\n" +
        "    \"value\": \"Do Not Change\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"name\": \"name\",\n" +
        "    \"value\": \"Really, don't!\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"name\": \"ready_to_send_dev\",\n" +
        "    \"value\": \"\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"name\": \"ready_to_send_int\",\n" +
        "    \"value\": \"1\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"name\": \"ready_to_send_prod\",\n" +
        "    \"value\": \"3\"\n" +
        "  }\n" +
        "]";
    Response response = createMockAndAddToDefault(Response.class);
    expect(response.hasEntity()).andReturn(true).anyTimes();
    expect(response.readEntity(eq(String.class))).andReturn(responseContent).times(3);
    replayDefault();
    assertTrue("Expected ready to send INT == true", rest.isReadyToSendGet(response,
        ServerClass.INT));
    assertFalse("Expected ready to send DEV == false", rest.isReadyToSendGet(response,
        ServerClass.DEV));
    assertFalse("Expected ready to send PROD == false", rest.isReadyToSendGet(response,
        ServerClass.PROD));
    verifyDefault();
  }

  @Test
  public void testIsReadyToSend_put_response() {
    String responseContent = "{\n    \"value\": \"1\"\n}";
    Response response = createMockAndAddToDefault(Response.class);
    expect(response.hasEntity()).andReturn(true).anyTimes();
    expect(response.readEntity(eq(String.class))).andReturn(responseContent).once();
    replayDefault();
    assertTrue("Expected ready to send PROD == true", rest.isReadyToSendPut(response));
    verifyDefault();
  }

  @Test
  public void testIgnoreUnknownUnmarshalling() throws Exception {
    String content = "{\"id\":\"1142028\",\"name\":\"ready_to_send_prod\",\"value\":\"1\"}";
    assertTrue("Expecting correct unmarshalling even with unknown fields in content",
        Optional.ofNullable(new ObjectMapper().readValue(content, ResponseBodyObj.class)).isPresent());

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
