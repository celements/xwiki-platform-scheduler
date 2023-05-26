package com.celements.cleverreach;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.script.service.ScriptService;

@Component(CleverReachScriptService.COMPONENT_NAME)
public class CleverReachScriptService implements ScriptService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CleverReachScriptService.class);

  public static final String COMPONENT_NAME = "cleverreach";

  @Requirement(CleverReachRest.COMPONENT_NAME)
  private CleverReachService client;

  public String debugWhoami() {
    String start = "<h3>WHOAMI:</h3><div>";
    String end = "</div>";
    try {
      return start + "<pre>" + formateDebugOutput(client.whoami())
          + "</pre></div><h3>TTL:</h3><div><pre>" + formateDebugOutput(client.ttl()) + "</pre>"
          + end;
    } catch (IOException ioe) {
      LOGGER.warn("Exception in debugging CleverReach Rest connection", ioe);
      return start + "Exception!<pre>" + ioe.getMessage() + "</pre>" + end;
    }
  }

  String formateDebugOutput(String debugStr) {
    return debugStr.replaceAll("\\{", "{\n").replaceAll(",", ",\n  ").replaceAll("\\}", "\n}");
  }

}
