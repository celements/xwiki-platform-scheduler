package com.celements.cleverreach.util;

import static com.google.common.base.Preconditions.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.xml.sax.InputSource;
import org.xwiki.component.annotation.Component;

import com.celements.cleverreach.exception.CssInlineException;

import io.sf.carte.doc.dom4j.CSSStylableElement;
import io.sf.carte.doc.dom4j.XHTMLDocument;
import io.sf.carte.doc.dom4j.XHTMLDocumentFactory;
import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;

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
      XHTMLDocument document = prepareInput(html);
      document.addStyleSheet(new org.w3c.css.sac.InputSource(new StringReader(css)));
      applyInlineStyle(document.getRootElement());
      String result = prepareOutput(document);
      LOGGER.trace("HTML with CSS INLINED [{}]", result);
      return result;
    } catch (DocumentException | IOException excp) {
      LOGGER.warn("Failed to apply CSS [{}] to HTML [{}]", css, html, excp);
      throw new CssInlineException(html, excp);
    }
  }

  XHTMLDocument prepareInput(String html) throws DocumentException {
    Reader re = new StringReader(html);
    InputSource source = new InputSource(re);
    SAXReader reader = new SAXReader(XHTMLDocumentFactory.getInstance());
    reader.setEntityResolver(new DefaultEntityResolver());
    return (XHTMLDocument) reader.read(source);
  }

  String prepareOutput(XHTMLDocument document) throws IOException {
    OutputFormat outputFormat = new OutputFormat("", false, "UTF-8");
    StringWriter out = new StringWriter();
    XMLWriter writer = new XMLWriter(out, outputFormat);
    writer.write(document);
    return out.toString();
  }

  void applyInlineStyle(Element element) {
    int nodeCount = element.nodeCount();
    for (int i = 0; i < nodeCount; i++) {
      Node node = element.node(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        // Element node are always also CSSStylableElement elements
        CSSStylableElement styleElement = (CSSStylableElement) node;
        CSSStyleDeclaration style = styleElement.getComputedStyle();
        if (style.getLength() != 0) {
          styleElement.addAttribute(STYLE, style.getCssText());
        }
        applyInlineStyle(styleElement);
      }
    }
  }
}
