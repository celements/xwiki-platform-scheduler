package com.celements.cleverreach;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cleverreach.exception.CleverReachRequestFailedException;
import com.celements.common.MoreObjectsCel;
import com.celements.common.classes.IClassCollectionRole;
import com.celements.css.exception.CssInlineException;
import com.celements.mailsender.IMailSenderRole;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.context.ModelContext;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.reference.RefBuilder;
import com.celements.web.classcollections.OldCoreClasses;
import com.celements.web.classes.FormMailClass;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component
public class DefaultFailNotificationHandler implements FailNotificationHandlerRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      DefaultFailNotificationHandler.class);

  @Requirement
  private ModelContext context;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private IMailSenderRole mailSender;

  @Requirement("celements.oldCoreClasses")
  private IClassCollectionRole oldCoreClasses;

  @Override
  public void send(String msg, Exception excp) {
    LOGGER.error(msg, excp);
    try {
      XWikiDocument configDoc = modelAccess.getDocument(getConfigDocRef());
      Optional<String> fromMail = getFromMail(configDoc);
      if (fromMail.isPresent()) {
        List<BaseObject> receivers = XWikiObjectFetcher.on(configDoc).filter(
            getReceiverEmailClassRef()).list();
        String content = getMailingContent(msg, excp);
        for (BaseObject receiver : receivers) {
          if (1 == receiver.getIntValue("is_active")) {
            mailSender.sendMail(fromMail.get(), null, receiver.getStringValue("email"), null,
                null, "TAGESAGENDA UPDATE FAILED!", content, content, null, null);
          }
        }
      } else {
        LOGGER.error("Missing 'from' mail configuration for 'Tagesagenda update failed' "
            + "notification");
      }
    } catch (DocumentNotExistsException dnee) {
      LOGGER.error("Unable to read failed notification configuration. Doc does not exist.", dnee);
    }
  }

  String getMailingContent(String msg, Exception excp) {
    StringWriter content = new StringWriter();
    content.append("<h2>").append(excp.getMessage()).append("</h2>")
        .append("<div>").append(msg).append("</div>")
        .append("<hr /><pre>");
    Response resp = MoreObjectsCel.tryCast(excp, CleverReachRequestFailedException.class)
        .map(e -> e.getResponse()).orElse(null);
    if (resp != null) {
      content.append("Status Code: ").append(Integer.toString(resp.getStatus())).append("\n");
      String respHeaders = resp.getStringHeaders().entrySet().stream()
          .map(entry -> entry.getKey() + " = " + entry.getValue().stream()
              .collect(Collectors.joining(" | ")) + "\n")
          .collect(Collectors.joining());
      content.append("Header String:\n").append(respHeaders).append("\n");
      content.append("Body:\n").append(((CleverReachRequestFailedException) excp)
          .getResponseBody());
    } else if (excp instanceof CssInlineException) {
      content.append(((CssInlineException) excp).getExtendedMessage());
    }
    appendStackTrace(excp, content);
    return content.toString();
  }

  void appendStackTrace(Exception excp, StringWriter content) {
    StringWriter sw = new StringWriter();
    try (PrintWriter pw = new PrintWriter(sw)) {
      excp.printStackTrace(pw);
      pw.flush();
      content.append("</pre><hr /><pre>")
          .append(sw.toString())
          .append("</pre>");
    }
  }

  Optional<String> getFromMail(XWikiDocument configDoc) {
    return XWikiObjectFetcher.on(configDoc).fetchField(FormMailClass.FIELD_EMAIL_FROM).first()
        .toJavaUtil();
  }

  private ClassReference getReceiverEmailClassRef() {
    return new ClassReference(((OldCoreClasses) oldCoreClasses).getReceiverEmailClassRef(
        context.getWikiRef().getName()));
  }

  public DocumentReference getConfigDocRef() {
    return RefBuilder.from(context.getWikiRef()).space(CleverReachRest.REST_CONFIG_SPACE_NAME)
        .doc(CleverReachRest.REST_CONFIG_DOC_NAME).build(DocumentReference.class);
  }

}
