package com.celements.cleverreach;

import static com.google.common.base.Preconditions.*;
import static com.google.common.base.Strings.*;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.google.common.base.Optional;

public class CleverReachConnection {

  private String restBaseUrl;
  private CleverReachToken token;

  public CleverReachConnection() {
    this((CleverReachToken) null);
  }

  public CleverReachConnection(@NotNull String restBaseUrl) {
    this(restBaseUrl, null);
  }

  public CleverReachConnection(@Nullable CleverReachToken token) {
    this(CleverReachRest.DEFAULT_REST_URL, token);
  }

  public CleverReachConnection(@NotNull String restBaseUrl, @Nullable CleverReachToken token) {
    checkArgument(!isNullOrEmpty(restBaseUrl));
    this.restBaseUrl = restBaseUrl;
    this.token = token;
  }

  public String getBaseUrl() {
    return restBaseUrl;
  }

  public Optional<CleverReachToken> getToken() {
    return Optional.fromNullable(token);
  }

  public void setToken(@Nullable CleverReachToken token) {
    this.token = token;
  }

  public boolean isConnected() {
    return getToken().isPresent() && getToken().get().isValid();
  }
}
