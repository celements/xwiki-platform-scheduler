package com.celements.captcha;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;

import com.celements.model.context.ModelContext;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.xpn.xwiki.web.XWikiRequest;

@Component("reCaptcha")
public class ReCaptchaService implements CaptchaServiceRole {

  private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  // Google ReCaptcha URL and params
  private static final String CFG_RECAPTCHA_VALIDATION_URL = "https://www.google.com/recaptcha/api/siteverify?";
  private static final String CFG_RECAPTCHA_VALIDATION_URL_PARAM_CAPTCHA = "&response=";
  private static final String CFG_RECAPTCHA_VALIDATION_URL_PARAM_CLIENTIP = "&remoteip=";
  private static final String CFG_RECAPTCHA_VALIDATION_URL_PARAM_SECRET = "&secret=";

  private static final String CFG_KEY_RECAPTCHA_SECRET = "reCaptchaServerSecret";
  private static final String CFG_FORM_FIELD_KEY = "g-recaptcha-response";

  @Requirement
  private ModelContext context;

  @Requirement
  private ConfigurationSource configSource;

  @Override
  public Optional<ReCaptchaResponse> verify() {
    String secret = configSource.getProperty(CFG_KEY_RECAPTCHA_SECRET, "");
    if (!Strings.isNullOrEmpty(secret) && context.getRequest().isPresent() && context
        .getRequestParameter(CFG_FORM_FIELD_KEY).isPresent()) {
      String urlString = CFG_RECAPTCHA_VALIDATION_URL
          + CFG_RECAPTCHA_VALIDATION_URL_PARAM_CAPTCHA + context.getRequestParameter(
              CFG_FORM_FIELD_KEY).get()
          + CFG_RECAPTCHA_VALIDATION_URL_PARAM_CLIENTIP + getClientIp(context.getRequest().get())
          + CFG_RECAPTCHA_VALIDATION_URL_PARAM_SECRET + secret;
      try {
        return Optional.fromNullable(new ObjectMapper().readValue(new URL(urlString),
            ReCaptchaResponse.class));
      } catch (MalformedURLException e) {
        LOGGER.error("malformed URL [{}]", urlString, e);
      } catch (JsonParseException | JsonMappingException je) {
        LOGGER.error("failed mapping response json to ReCaptchaResponse", je);
      } catch (IOException ioe) {
        LOGGER.error("failed contacting reCAPTCHA endpoint [{}]", urlString, ioe);
      }
    }
    return Optional.absent();
  }

  private String getClientIp(XWikiRequest request) {
    String ip = request.getHeader("X-FORWARDED-FOR");
    return Strings.isNullOrEmpty(ip) ? request.getRemoteAddr() : ip;
  }

}
