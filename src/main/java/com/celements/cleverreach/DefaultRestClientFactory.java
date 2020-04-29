package com.celements.cleverreach;

import java.io.IOException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;

@Component
public class DefaultRestClientFactory implements IRestClientFactoryRole {

  Logger LOGGER = LoggerFactory.getLogger(DefaultRestClientFactory.class);

  @Override
  public Client newClient() {
    Client client = ClientBuilder.newClient();
    client.register(new LoggingFilter());
    return client;
  }

  class LoggingFilter implements ClientRequestFilter {

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
      LOGGER.info("URL: [{}], Prop 'group_id': [{}], Body: [{}]", requestContext.getUri(),
          requestContext.getEntity(), requestContext.getProperty("group_id"));
    }

  }

}
