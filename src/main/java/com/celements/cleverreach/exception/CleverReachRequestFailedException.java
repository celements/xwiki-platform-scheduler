package com.celements.cleverreach.exception;

import java.io.IOException;
import java.io.PrintStream;

import javax.ws.rs.core.Response;

public class CleverReachRequestFailedException extends IOException {

  private static final long serialVersionUID = 1L;

  private Response response;
  private String responseBody;

  public CleverReachRequestFailedException(String message, Response response, String responseBody) {
    super(message);
    this.response = response;
    this.responseBody = responseBody;
  }

  public CleverReachRequestFailedException(String message, String responseBody, Exception e) {
    super(message, e);
    this.responseBody = responseBody;
  }

  public Response getResponse() {
    return response;
  }

  public String getResponseBody() {
    return responseBody;
  }

  @Override
  public void printStackTrace() {
    printStackTrace(System.err);
  }

  @Override
  public void printStackTrace(PrintStream ps) {
    ps.println(getResponseStatusAndLength());
    super.printStackTrace(ps);
  }

  String getResponseStatusAndLength() {
    return "Response status|length: [" + ((response != null) ? response.getStatus()
        + "|" + response.getLength() : "null");
  }
}
