package com.celements.cleverreach.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Scanner;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class CssInlineException extends Exception {

  private static final Logger LOGGER = LoggerFactory.getLogger(CssInlineException.class);

  private static final long serialVersionUID = 1L;

  static final BiFunction<String, Integer, String> SHORT_MESSAGE = (String str, Integer line) -> str
      .trim().replaceAll("^.*Error on line (\\d+) .*?:(.*)$", "$1 : $2");
  static final BiFunction<String, Integer, String> SNIPPET = (String str, Integer lineNr) -> lineNr
      + ": " + str.trim();
  private final String html;
  private String snippet;

  public CssInlineException(@NotNull String html, Exception excp) {
    super(excp);
    this.html = html;
  }

  @Override
  public String getMessage() {
    return "CSS could not be inlined.";
  }

  public String getExtendedMessage() {
    return "Culprit was:\n" + getExceptionRangeSnippet();
  }

  @Override
  public String toString() {
    return getMessage() + "\n\n" + super.toString();
  }

  String getExceptionRangeSnippet() {
    if (Strings.isNullOrEmpty(snippet) && !Strings.isNullOrEmpty(html)) {
      snippet = getMessage.get() + "\n";
      try {
        StringWriter sw = (injected_sw != null) ? injected_sw : new StringWriter();
        printStackTrace.accept(sw);
        int exceptionLine = Integer.parseInt(sw.toString().replaceAll(
            "^[\\s\\S]*Error on line (\\d+) [\\s\\S]*", "$1"));
        snippet += scanLines(html, exceptionLine - 2, exceptionLine + 2, SNIPPET);
      } catch (NumberFormatException nfe) {
        LOGGER.warn("Exception Message has no line number referenced. Message: [{}]",
            super.getMessage());
      }
    }
    return snippet;
  }

  String scanLines(String str, int min, int max, BiFunction<String, Integer, String> doForEach) {
    int lineNr = 1;
    StringBuilder result = new StringBuilder();
    Scanner scanner = new Scanner(str);
    while (scanner.hasNextLine() && (max > 0)) {
      String line = scanner.nextLine();
      if (min <= 1) {
        result.append(doForEach.apply(line, lineNr) + "\n");
      }
      min--;
      max--;
      lineNr++;
    }
    scanner.close();
    return result.toString();
  }

  // Unit test helpers
  Exception injected_excp;
  StringWriter injected_sw;
  private final Supplier<String> getMessage = () -> {
    if (injected_excp != null) {
      return injected_excp.getMessage();
    } else {
      return super.getMessage();
    }
  };
  private final Consumer<StringWriter> printStackTrace = (sw) -> {
    PrintWriter pw = new PrintWriter(sw);
    if (injected_excp != null) {
      injected_excp.printStackTrace(pw);
    } else {
      super.printStackTrace(pw);
    }
  };
}
