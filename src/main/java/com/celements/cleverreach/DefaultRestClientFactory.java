package com.celements.cleverreach;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.xwiki.component.annotation.Component;

@Component
public class DefaultRestClientFactory implements IRestClientFactoryRole {

  @Override
  public Client newClient() {
    return ClientBuilder.newClient();
  }

}
