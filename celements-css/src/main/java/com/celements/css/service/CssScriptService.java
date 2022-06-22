package com.celements.css.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.script.service.ScriptService;

import com.celements.css.exception.CssInlineException;
import com.celements.css.util.CssInliner;
import com.google.common.collect.ImmutableMap;

@Component(CssScriptService.CSS)
public class CssScriptService implements ScriptService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CssScriptService.class);

  public static final String CSS = "css";

  @Requirement
  private CssInliner cssInliner;

  public String inline(String html) {
    return inline(html, null);
  }

  public String inline(String html, String css) {
    try {
      return cssInliner.inline(html, css);
    } catch (CssInlineException exp) {
      LOGGER.info("css inlining with '{}' failed.", html, exp);
    }
    return html;
  }

  public String inline(String html, String css, Map<String, String> configs) {
    try {
      return cssInliner.inline(html, css, ImmutableMap.copyOf(configs));
    } catch (CssInlineException exp) {
      LOGGER.info("css inlining with '{}' failed.", html, exp);
    }
    return html;
  }

}
