package com.celements.cleverreach;

import static com.celements.common.MoreObjectsCel.*;
import static com.celements.logging.LogUtils.*;
import static com.google.common.collect.ImmutableMap.*;

import java.io.IOException;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;

@Component
public class DefaultRestClientFactory implements IRestClientFactoryRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRestClientFactory.class);

  @Override
  public Client newClient() {
    Client client = ClientBuilder.newClient();
    client.register(new LoggingFilter());
    return client;
  }

  class LoggingFilter implements ClientRequestFilter {

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
      LOGGER.info("URL: [{}], Method: [{}], Entity: [{}], Properties: [{}], Headers: [{}]",
          requestContext.getUri(), requestContext.getMethod(),
          defer(() -> getEntity(requestContext)),
          defer(() -> getProperties(requestContext)),
          defer(requestContext::getStringHeaders));
    }

    private Object getEntity(ClientRequestContext requestContext) {
      return tryCast(requestContext.getEntity(), javax.ws.rs.core.Form.class)
          .map(form -> (Object) form.asMap())
          .orElse(requestContext.getEntity());
    }

    private Map<String, Object> getProperties(ClientRequestContext requestContext) {
      return requestContext.getPropertyNames().stream()
          .collect(toImmutableMap(name -> name, requestContext::getProperty));
    }

  }

}
