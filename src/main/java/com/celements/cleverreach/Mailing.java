package com.celements.cleverreach;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.annotate.JsonTypeName;

public class Mailing {

  @JsonTypeInfo(use = JsonTypeInfo.Id.NONE, include = As.WRAPPER_OBJECT)
  @JsonSubTypes({ @JsonSubTypes.Type(value = Content.class, name = "content"), })

  /**
   * Additional fields available in CleverReach API
   * "name": "my internal name"
   * "sender_name": "Bruce Wayne (Wayne corp.)"
   * ...."sender_email": "bruce.wayne@gotham.com"
   * ...."receivers": { // fill either a list of group ids or a filter id
   * .... "groups": [ "31939", "81942" ],
   * ..."filter": "66"
   * }
   * "settings": {
   * ...."editor": "wizard", // "wizard", "freeform", "advanced", "plaintext"
   * .... "open_tracking": true, // track opening of emails
   * .... "click_tracking": true, // track clicks of emails
   * .... "link_tracking_url": "27.wayne.cleverreach.com",
   * .... "link_tracking_type": "google", // "google", "intelliad", "crconnect"
   * .... "unsubscribe_form_id": "23",
   * .... "campaign_id": "52",
   * .... "category_id": "54"
   * }
   */
  public String subject; // "subject": "subject line"
  public Content content; // "content": { "type", "html", "text" }

  public Mailing() {
    content = new Content();
  }

  @JsonTypeName("content")
  public static class Content {

    /**
     * Additional fields available in CleverReach API
     * "type": "html/text" // "html", "text" or "html/text"
     */
    public String html; // "html": "<html><body>The two faces of Harvey Dent</body></html>"
    public String text; // "text": "The two faces of Harvey Dent"
  }
}
