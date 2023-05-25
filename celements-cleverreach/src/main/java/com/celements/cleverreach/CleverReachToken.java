package com.celements.cleverreach;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.concurrent.Immutable;

import org.codehaus.jackson.annotate.JsonProperty;

@Immutable
public class CleverReachToken {

  private static final long SAFETY_INVALIDATION = 5000; // invalidate token 5 seconds before expiry

  private String token;
  private String tokenType;
  private String scope;
  private Date expiry;

  public CleverReachToken(@JsonProperty("access_token") String token,
      @JsonProperty("expires_in") long expiresIn, @JsonProperty("token_type") String tokenType,
      @JsonProperty("scope") String scope) {
    this.token = token;
    setExpiry(expiresIn);
    this.tokenType = tokenType;
    this.scope = scope;
  }

  public CleverReachToken(long expiresIn) {
    setExpiry(expiresIn);
  }

  void setExpiry(long expiresIn) {
    expiry = new Date(((new Date()).getTime() + expiresIn) - SAFETY_INVALIDATION);
  }

  public String getToken() {
    return token;
  }

  public Date getExpiry() {
    return new Date(expiry.getTime());
  }

  public boolean isValid() {
    return expiry.after(new Date());
  }

  public String getTokenType() {
    return ((tokenType != null) && (tokenType.length() > 1)) ? tokenType.substring(0,
        1).toUpperCase() + tokenType.substring(1) : "";
  }

  public String getScope() {
    return (scope != null) ? scope : "";
  }

  @Override
  public String toString() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return "Token length [" + getToken().length() + "], expiry [" + sdf.format(getExpiry())
        + "], token type [" + getTokenType() + "], scope [" + getScope() + "]";
  }
}
