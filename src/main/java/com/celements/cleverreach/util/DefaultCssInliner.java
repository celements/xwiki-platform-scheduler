package com.celements.cleverreach.util;

import static com.celements.common.MoreObjectsCel.*;
import static com.celements.common.lambda.LambdaExceptionUtil.*;
import static com.google.common.base.Preconditions.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;

import com.celements.cleverreach.exception.CssInlineException;
import com.celements.dom4j.Dom4JParser;

import io.sf.carte.doc.dom4j.CSSStylableElement;
import io.sf.carte.doc.dom4j.XHTMLDocument;
import io.sf.carte.doc.style.css.om.ComputedCSSStyle;

@Component
public class DefaultCssInliner implements CssInliner {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCssInliner.class);

  private static final String STYLE = "style";

  @Override
  public String inline(String html, List<String> cssList)
      throws CssInlineException {
    return inline(html, String.join("\n", cssList));
  }

  @Override
  public String inline(String html, String css)
      throws CssInlineException {
    checkNotNull(html);
    checkNotNull(css);
    LOGGER.trace("Applying the following CSS [{}] to HTML [{}]", css, html);
    try {
      String result = Dom4JParser.createXHtmlParser().allowDTDs()
          .readAndExecute(html, rethrowFunction(document -> applyInlineStyle(document, css)))
          .orElseThrow(() -> new CssInlineException(html, null));
      LOGGER.trace("HTML with CSS INLINED [{}]", result);
      return result;
    } catch (IOException excp) {
      LOGGER.warn("Failed to apply CSS [{}] to HTML [{}]", css, html, excp);
      throw new CssInlineException(html, excp);
    }
  }

  private Stream<XHTMLDocument> applyInlineStyle(XHTMLDocument document, String css)
      throws IOException {
    document.addStyleSheet(new org.w3c.css.sac.InputSource(new StringReader(css)));
    document.selectNodes("//*").stream()
        .flatMap(tryCast(CSSStylableElement.class))
        .forEach(element -> {
          ComputedCSSStyle style = element.getComputedStyle();
          if (style.getLength() != 0) {
            element.addAttribute(STYLE, style.getCssText());
          }
        });
    return Stream.of(document);
  }

}
