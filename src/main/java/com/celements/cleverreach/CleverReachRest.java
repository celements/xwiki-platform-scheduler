package com.celements.cleverreach;

import static com.celements.model.util.References.*;
import static com.google.common.base.Preconditions.*;

import java.io.IOException;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.auth.classes.RemoteLoginClass;
import com.celements.cleverreach.exception.CleverReachRequestFailedException;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.context.ModelContext;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.google.common.base.Optional;
import com.sun.syndication.io.impl.Base64;
import com.xpn.xwiki.objects.BaseObject;

@Component(CleverReachRest.COMPONENT_NAME)
@InstantiationStrategy(ComponentInstantiationStrategy.SINGLETON)
public class CleverReachRest implements CleverReachService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CleverReachRest.class);

  public static final String COMPONENT_NAME = "rest";

  public static final String REST_CONFIG_SPACE_NAME = "Configs";
  public static final String REST_CONFIG_DOC_NAME = "CleverReachRest";
  protected static final String DEFAULT_REST_URL = "https://rest.cleverreach.com/";
  static final String PATH_VERSION = "v3/";
  static final String PATH_LOGIN = "oauth/token.php";
  static final String PATH_MAILINGS = "mailings.json/";
  static final String PATH_WHOAMI = "debug/whoami.json";
  static final String PATH_TTL = "debug/ttl.json";

  static final String CONTEXT_CONNECTION_KEY = "clever_reach_connection";

  static enum SubmitMethod {
    GET, POST, PUT, DELETE
  };

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private ModelContext modelContext;

  @Requirement(RemoteLoginClass.CLASS_DEF_HINT)
  private ClassDefinition remoteLoginClass;

  @Requirement
  private Execution execution;

  @Requirement
  private IRestClientFactoryRole clientFactory;

  @Override
  public boolean updateMailing(MailingConfig mailingConf) throws IOException {
    checkNotNull(mailingConf);
    Mailing formData = new Mailing();
    formData.subject = mailingConf.getSubject();
    formData.content.html = mailingConf.getContentHtmlCssInlined();
    formData.content.text = mailingConf.getContentPlain();
    Response response = sendRestRequest(PATH_MAILINGS + mailingConf.getId(), formData,
        SubmitMethod.PUT);
    LOGGER.debug("Mailing update response [{}]", response);
    if ((response != null) && response.hasEntity()) {
      String content = response.readEntity(String.class);
      LOGGER.debug("Mailing update response content [{}]", content);
      if (content.contains(mailingConf.getId()) && content.matches(
          ".*\"success\".{0,1}:.{0,1}true.*")) {
        return true;
      }
      LOGGER.warn("Mailing update not successful. Response content is [{}]", content);
    } else {
      LOGGER.warn("Mailing update failed with response [{}] and response hasEntity [{}]", response,
          (response != null) ? response.hasEntity() : false);
    }
    return false;
  }

  @Override
  public String whoami() throws IOException {
    return runDebugRequest(PATH_WHOAMI);
  }

  @Override
  public String ttl() throws IOException {
    return runDebugRequest(PATH_TTL);
  }

  @Override
  public DocumentReference getConfigDocRef() {
    return create(DocumentReference.class, REST_CONFIG_DOC_NAME, create(SpaceReference.class,
        REST_CONFIG_SPACE_NAME, modelContext.getWikiRef()));
  }

  Response sendRestRequest(String path, Object data, SubmitMethod method) throws IOException {
    CleverReachConnection connection = getConnection();
    if (connection.isConnected()) {
      CleverReachToken token = connection.getToken().get();
      method = (method != null) ? method : SubmitMethod.POST;
      String authHeader = token.getTokenType() + " " + token.getToken();
      if (data instanceof MultivaluedMap) {
        getMultivalueMapFromOjb(data).add("token", token.getToken());
      }
      Response response = sendRequest(PATH_VERSION + path, data, authHeader, method, connection);
      if (response.getStatus() == 200) {
        return response;
      } else {
        LOGGER.trace("Request response status != 200. Response [{}]", response);
        if (response.hasEntity()) {
          LOGGER.trace("Response content [{}]", response.readEntity(String.class));
        }
        throw new CleverReachRequestFailedException("Response code status != 200", response);
      }
    } else {
      throw new CleverReachRequestFailedException("Failed to connect", null);
    }
  }

  CleverReachToken initializeToken(Response response, String jsonResponse) {
    ObjectMapper objMapper = new ObjectMapper();
    try {
      CleverReachToken token = objMapper.readValue(jsonResponse.getBytes(), CleverReachToken.class);
      if (token.isValid()) {
        LOGGER.debug("new token received [{}]", token);
        return token;
      } else {
        LOGGER.warn("Unable to receive token. Response [{}]", response);
      }
    } catch (IOException ioe) {
      LOGGER.warn("IOException caught. Unable to connect and receive token. Response [{}].",
          response);
      if (LOGGER.isTraceEnabled()) {
        // Workaround to log stack trace in trace despite NoStackTracePatternLayout
        LOGGER.error("Exception Reason", ioe);
      }
    }
    return null;
  }

  CleverReachConnection getConnection() throws CleverReachRequestFailedException {
    CleverReachConnection cachedConnection = (CleverReachConnection) execution.getContext().getProperty(
        CONTEXT_CONNECTION_KEY);
    if ((null == cachedConnection) || !cachedConnection.isConnected()) {
      Optional<BaseObject> configObj = Optional.absent();
      try {
        configObj = XWikiObjectFetcher.on(modelAccess.getDocument(getConfigDocRef())).filter(
            remoteLoginClass).first();
      } catch (DocumentNotExistsException dnee) {
        LOGGER.warn("Document XWikiPreferences does not exist", dnee);
      }
      cachedConnection = buildConnection(configObj);
      execution.getContext().setProperty(CONTEXT_CONNECTION_KEY, cachedConnection);
    }
    return cachedConnection;
  }

  CleverReachConnection buildConnection(Optional<BaseObject> configObj)
      throws CleverReachRequestFailedException {
    if (configObj.isPresent()) {
      Optional<String> restBaseUrl = modelAccess.getFieldValue(configObj.get(),
          RemoteLoginClass.FIELD_URL);
      Optional<String> clientId = modelAccess.getFieldValue(configObj.get(),
          RemoteLoginClass.FIELD_USERNAME);
      Optional<String> clientSecret = modelAccess.getFieldValue(configObj.get(),
          RemoteLoginClass.FIELD_PASSWORD);
      if (clientId.isPresent() && clientSecret.isPresent()) {
        return new CleverReachConnection(restBaseUrl.orNull(), login(restBaseUrl.orNull(),
            clientId.get(), clientSecret.get()));
      }
    }
    LOGGER.warn("No config found on {}.{}", REST_CONFIG_SPACE_NAME, REST_CONFIG_DOC_NAME);
    return new CleverReachConnection();
  }

  CleverReachToken login(String restBaseUrl, String clientId, String clientSecret)
      throws CleverReachRequestFailedException {
    MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
    formData.putSingle("grant_type", "client_credentials");
    String authHeader = "Basic " + Base64.encode(clientId + ":" + clientSecret);
    Response response = sendRequest(PATH_LOGIN, formData, authHeader, SubmitMethod.POST,
        restBaseUrl);
    boolean hasContent = response.hasEntity();
    String content = null;
    if (hasContent) {
      // readEntity closes the response stream, so it can only be read once
      content = response.readEntity(String.class);
    }
    traceLogLoginResponse(response, content);
    if (hasContent) {
      return initializeToken(response, content);
    } else {
      throw new CleverReachRequestFailedException(
          "Unable to connect and receive token. Response [{}]", response);
    }
  }

  Response sendRequest(String path, Object data, String authHeader, SubmitMethod method,
      CleverReachConnection connection) {
    return sendRequest(path, data, authHeader, method, connection.getBaseUrl());
  }

  Response sendRequest(String path, Object data, String authHeader, SubmitMethod method,
      String baseUrl) {
    WebTarget target = clientFactory.newClient().target(baseUrl).path(path);
    addGetParameters(data, target, method);
    Builder request = target.request().header("Authorization", authHeader);
    switch (method) {
      case GET:
        return request.get();
      case PUT:
        return request.put(getRequestDataEntity(data));
      case DELETE:
        return request.delete();
      default: // Default to SubmitMethod.POST
        return request.post(getRequestDataEntity(data));
    }
  }

  String runDebugRequest(String path) throws IOException {
    Response response = sendRestRequest(path, new MultivaluedHashMap<String, String>(),
        SubmitMethod.GET);
    if ((response != null) && (response.getStatus() == 200) && response.hasEntity()) {
      return response.readEntity(String.class);
    }
    return "Response status [" + ((response != null) ? response.getStatus() : "?")
        + "]. See log for details.";
  }

  Entity<?> getRequestDataEntity(Object data) {
    if (data instanceof MultivaluedMap) {
      return Entity.form(getMultivalueMapFromOjb(data));
    } else { // POJO -> build JSON
      ObjectMapper mapper = new ObjectMapper();
      try {
        String dataJson = mapper.writeValueAsString(data);
        LOGGER.trace("JSON for request: [{}]", dataJson);
        return Entity.json(dataJson);
      } catch (IOException ioe) {
        LOGGER.error("Exception serializing data to json. Data [{}]", data, ioe);
      }
    }
    return Entity.text("");
  }

  void addGetParameters(Object data, WebTarget target, SubmitMethod method) {
    if ((method == SubmitMethod.GET) && (data instanceof MultivaluedMap)) {
      MultivaluedMap<String, String> fromData = getMultivalueMapFromOjb(data);
      for (String key : fromData.keySet()) {
        target.queryParam(key, fromData.get(key).toArray());
      }
    }
  }

  @SuppressWarnings("unchecked")
  MultivaluedMap<String, String> getMultivalueMapFromOjb(Object data) {
    return (MultivaluedMap<String, String>) data;
  }

  void traceLogLoginResponse(Response response, String content) {
    if (LOGGER.isTraceEnabled()) {
      MultivaluedMap<String, Object> headers = response.getHeaders();
      LOGGER.trace("Headers:");
      for (String header : headers.keySet()) {
        LOGGER.trace("header [{}] = [{}]", header, headers.get(header));
      }
      Map<String, NewCookie> cookies = response.getCookies();
      LOGGER.trace("Cookies:");
      for (String cookie : cookies.keySet()) {
        LOGGER.trace("cookie [{}] = [{}]", cookie, cookies.get(cookie));
      }
      LOGGER.trace("Content [{}]", content.replaceAll("^(.*\"access_token\":\")[^\"]*(.*)$",
          "$1********$2"));
    }
  }
}
