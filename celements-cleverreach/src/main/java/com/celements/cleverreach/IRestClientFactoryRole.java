package com.celements.cleverreach;

import javax.validation.constraints.NotNull;
import javax.ws.rs.client.Client;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface IRestClientFactoryRole {

  @NotNull
  Client newClient();
}
