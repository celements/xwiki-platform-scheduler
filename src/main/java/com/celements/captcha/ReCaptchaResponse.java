package com.celements.captcha;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

// {
// "success": true|false,
// "challenge_ts": timestamp, // timestamp of the challenge load (ISO format
// yyyy-MM-dd'T'HH:mm:ssZZ)
// "hostname": string, // the hostname of the site where the reCAPTCHA was solved
// "error-codes": [...] // optional
// }
public class ReCaptchaResponse {

  private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  private static final String RECAPTCHA_RESPONSE_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZ";

  public static final Map<String, String> ERROR_MESSAGES = new ImmutableMap.Builder<String, String>()
      .put("missing-input-secret", "The secret parameter is missing.")
      .put("invalid-input-secret", "The secret parameter is invalid or malformed.")
      .put("missing-input-response", "The response parameter is missing.")
      .put("invalid-input-response", "The response parameter is invalid or malformed.")
      .put("bad-request", "The request is invalid or malformed.")
      .put("timeout-or-duplicate", "The response is no longer valid: either is too old or has "
          + "been used previously.").build();

  private boolean success;
  private Date timestamp; // ISO format yyyy-MM-dd'T'HH:mm:ssZZ
  private String hostname;
  private List<String> errorCodes = Collections.emptyList();

  public ReCaptchaResponse(@JsonProperty("success") boolean success,
      @JsonProperty("challenge_ts") String timestamp,
      @JsonProperty("hostname") String hostname,
      @JsonProperty("error-codes") String[] errors) {
    setSuccess(success);
    setTimestamp(timestamp);
    setHostname(hostname);
    setErrorCodes(errors);
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    try {
      this.timestamp = new SimpleDateFormat(RECAPTCHA_RESPONSE_TIMESTAMP_FORMAT).parse(timestamp);
    } catch (ParseException pe) {
      LOGGER.error("timestamp [{}] could not be parsed with format [{}]", timestamp,
          RECAPTCHA_RESPONSE_TIMESTAMP_FORMAT);
    }
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public List<String> getErrorCodes() {
    return errorCodes;
  }

  public void setErrorCodes(String[] errorCodes) {
    this.errorCodes = ImmutableList.copyOf(errorCodes);
  }
}
