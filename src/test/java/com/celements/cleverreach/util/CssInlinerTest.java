package com.celements.cleverreach.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.web.Utils;

public class CssInlinerTest extends AbstractComponentTest {

  private CssInliner cssInliner;

  @Before
  public void setUp_CssInlinerTest() {
    cssInliner = Utils.getComponent(CssInliner.class);
  }

  @Test
  public void testInline_null() {
    try {
      cssInliner.inline(null, "");
      fail("Expecting NPE");
    } catch (NullPointerException npe) {
      // expected outcome
    }
  }

  @Test
  public void testInline_noStyles() {
    try {
      cssInliner.inline("<div></div>", (String) null);
      fail("Expecting NPE");
    } catch (NullPointerException npe) {
      // expected outcome
    }
  }

  @Test
  public void testInline_styleFile() {
    String simpleStyle = "div {\n  display: none;\n  padding-top: 3px;\n}";
    String expect = "padding-top: 3px";
    String result = cssInliner.inline("<!DOCTYPE html><html><head></head><body><div></div></body>"
        + "</html>", simpleStyle);
    assertTrue(getExpectationMessage(expect, result), result.contains(expect));
  }

  @Test
  public void testInline_styleFiles() throws Exception {
    String result = cssInliner.inline(fileToString("/test.html"), Arrays.asList(fileToString(
        "/testStyles1.css"), fileToString("/testStyles2.css")));
    String expect = "background-color: #f00;";
    assertTrue(getExpectationMessage(expect, result), checkInResult(result, "body", null, expect));
    expect = "color: #00f;";
    assertTrue(getExpectationMessage(expect, result), checkInResult(result, "li",
        "class=\"listitem\"", expect, 2));
    expect = "width: 800px;";
    assertTrue(getExpectationMessage(expect, result), checkInResult(result, "table", null, expect));
    expect = "background-color: #ff0;";
    assertTrue(getExpectationMessage(expect, result), checkInResult(result, "table",
        "id=\"contentTable", expect));
    assertTrue(getExpectationMessage(expect, result), checkInResult(result, "ul", null, expect));
    expect = "padding-right: 2px";
    assertTrue(getExpectationMessage(expect, result), checkInResult(result, "<t[dh]",
        "class=\"column[24]", expect, 5));
    expect = "color: #fff;";
    assertTrue(getExpectationMessage(expect, result), checkInResult(result, "<t[dh]",
        "class=\"column[24]", expect, 4));
    expect = "color: #0ff;";
    assertTrue(getExpectationMessage(expect, result), checkInResult(result, "<t[dh]", "specialCell",
        expect));
    expect = "color: #abc;";
    assertTrue(getExpectationMessage(expect, result), checkInResult(result, "td", "class=\"column3",
        expect, 3));
  }

  private boolean checkInResult(String result, String tag, String addition, String expect) {
    return checkInResult(result, tag, addition, expect, 1);
  }

  private boolean checkInResult(String result, String tag, String addition, String expect,
      int times) {
    String regex = tag + "[^>]*?";
    regex += (addition != null) ? addition + "[^>]*?" : "";
    regex += expect;
    Matcher m = Pattern.compile(regex).matcher(result);
    int count = 0;
    while (m.find()) {
      count++;
    }
    return count == times;
  }

  private String fileToString(String path) throws IOException {
    return IOUtils.toString(this.getClass().getResourceAsStream(path), "UTF-8");
  }

  private String getExpectationMessage(String expected, String result) {
    return "expected result to contain [" + expected + "], but was [" + result + "]";
  }
}
