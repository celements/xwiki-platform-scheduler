package com.celements.cleverreach;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.annotate.JsonTypeName;

public class Mailing {

  @JsonTypeInfo(use = JsonTypeInfo.Id.NONE, include = As.WRAPPER_OBJECT)
  @JsonSubTypes({ @JsonSubTypes.Type(value = Content.class, name = "content"), })

  public String subject;
  public Content content;

  public Mailing() {
    content = new Content();
  }

  @JsonTypeName("content")
  public static class Content {

    public String html;
    public String text;
  }
}
