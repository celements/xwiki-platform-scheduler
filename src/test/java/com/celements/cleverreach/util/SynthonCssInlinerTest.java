package com.celements.cleverreach.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.celements.cleverreach.exception.CssInlineException;
import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.web.Utils;

public class SynthonCssInlinerTest extends AbstractComponentTest {

  private CssInliner cssInliner;

  @Before
  public void setUp_CssInlinerTest() {
    cssInliner = Utils.getComponent(CssInliner.class, "synthon");
  }

  @Test
  public void testInline_null() throws CssInlineException {
    try {
      cssInliner.inline(null, "");
      fail("Expecting NPE");
    } catch (NullPointerException npe) {
      // expected outcome
    }
  }

  @Test
  public void testInline_noStyles() throws CssInlineException {
    try {
      cssInliner.inline("<div></div>", (String) null);
      fail("Expecting NPE");
    } catch (NullPointerException npe) {
      // expected outcome
    }
  }

  @Test
  public void testInline_styleFile() throws Exception {
    String simpleStyle = "div {\n  display: none;\n  padding-top: 3px;\n}";
    String expect = "padding-top:3px";
    // Use to create a new inline using the synthon server
    // String result = cssInliner.inline("<!DOCTYPE html><html><head></head><body><div></div>"
    // + "</body></html>", simpleStyle);
    // System.out.println(result);
    String result = fileToString("/2021-11-08_synthonResult_styleFile.html");
    assertTrue(getExpectationMessage(expect, result), result.contains(expect));
    String expect2 = "^<!DOCTYPE html>[\\s\\S]*<html[\\s\\S]*";
    assertTrue(getExpectationMessage(expect2, result), Pattern.compile(expect2,
        Pattern.CASE_INSENSITIVE).matcher(result).matches());
  }

  @Test
  public void testInline_styleFiles() throws Exception {
    Map<String, String> config = new HashMap<>();
    config.put(SynthonCssInliner.CONFIG_KEY_REMOVE_CLASSES, Boolean.FALSE.toString());
    // Use to create a new inline using the synthon server
    // String result = cssInliner.inline(fileToString("/test.html"), Arrays.asList(fileToString(
    // "/testStyles1.css"), fileToString("/testStyles2.css")), config);
    // System.out.println(result);
    String result = fileToString("/2021-11-08_synthonResult_styleFiles.html");
    String expect = "background-color:#f00;";
    assertTrue(getExpectationMessage(expect, result), checkInResult(result, "body", null, expect));
    expect = "width:333px";
    assertTrue(getExpectationMessage(expect, result), checkInResult(result, "body", null, expect));
    expect = "color:#00f";
    assertTrue(getExpectationMessage(expect, result), checkInResult(result, "li",
        "class=\"listitem\"", expect, 2));
    expect = "width:800px";
    assertTrue(getExpectationMessage(expect, result), checkInResult(result, "table", null, expect));
    expect = "background-color:#ff0";
    assertTrue(getExpectationMessage(expect, result), checkInResult(result, "table",
        "id=\"contentTable", expect));
    assertTrue(getExpectationMessage(expect, result), checkInResult(result, "ul", null, expect));
    expect = "padding-right:2px";
    assertTrue(getExpectationMessage(expect, result), checkInResult(result, "<t[dh]",
        "class=\"column[24]", expect, 5));
    expect = "color:#fff";
    assertTrue(getExpectationMessage(expect, result), checkInResult(result, "<t[dh]",
        "class=\"column[24]", expect, 4));
    expect = "color:#000";
    assertTrue(getExpectationMessage(expect, result), checkInResult(result, "<t[dh]", "specialCell",
        expect));
    expect = "color:#abc";
    assertTrue(getExpectationMessage(expect, result), checkInResult(result, "td", "class=\"column3",
        expect, 3));
    expect = "height:auto";
    assertTrue(getExpectationMessage(expect, result), checkInResult(result, "tr", "class=\"row",
        expect));
  }

  @Test
  public void testInline_nbsp() throws Exception {
    String simpleStyle = "div {\n  display: none;\n  padding-top: 3px;\n}";
    String expect = "padding-top:3px";
    // Use to create a new inline using the synthon server
    // String result = cssInliner.inline(fileToString("/test_nbsp.html"), simpleStyle);
    // System.out.println(result);
    String result = fileToString("/2021-11-08_synthonResult_nbsp.html");
    assertTrue(getExpectationMessage(expect, result), result.contains(expect));
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
