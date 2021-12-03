package com.celements.cleverreach;

import static com.google.common.base.Preconditions.*;
import static com.google.common.base.Strings.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celements.cleverreach.CleverReachService.ServerClass;
import com.celements.cleverreach.exception.CssInlineException;
import com.celements.cleverreach.util.CssInliner;
import com.google.common.base.Strings;
import com.xpn.xwiki.web.Utils;

@Immutable
public class MailingConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(MailingConfig.class);

  public static class Builder {

    private String id;
    private String subject;
    private String contentHtml;
    private String contentPlain;
    private ServerClass serverClass;
    private String referenceUserId;
    private String referenceGroupId;
    private String referenceAttributeId;
    private List<String> css = new ArrayList<>();
    private String inlinerComponent = "synthon"; // default inliner is "synthon"
    private Map<String, String> inlinerConfig = new HashMap<>();

    public Builder setId(@NotNull String id) {
      this.id = !isNullOrEmpty(id) ? id : null;
      return this;
    }

    public Builder setSubject(@Nullable String subject) {
      this.subject = !isNullOrEmpty(subject) ? subject : null;
      return this;
    }

    public Builder setContentHtml(@Nullable String contentHtml) {
      this.contentHtml = !isNullOrEmpty(contentHtml) ? contentHtml : null;
      return this;
    }

    public Builder setContentPlain(@Nullable String contentPlain) {
      this.contentPlain = !isNullOrEmpty(contentPlain) ? contentPlain : null;
      return this;
    }

    public Builder setServerClass(@NotNull ServerClass serverClass) {
      checkNotNull(serverClass);
      this.serverClass = serverClass;
      return this;
    }

    public Builder setReferenceUserId(@NotEmpty String referenceUserId) {
      checkArgument(!Strings.isNullOrEmpty(referenceUserId));
      this.referenceUserId = referenceUserId;
      return this;
    }

    public Builder setReferenceGroupId(@NotEmpty String referenceGroupId) {
      checkArgument(!Strings.isNullOrEmpty(referenceGroupId));
      this.referenceGroupId = referenceGroupId;
      return this;
    }

    public Builder setReferenceAttributeId(@NotEmpty String referenceAttributeId) {
      checkArgument(!Strings.isNullOrEmpty(referenceAttributeId));
      this.referenceAttributeId = referenceAttributeId;
      return this;
    }

    public Builder addCssForInlining(@Nullable String cssFile) {
      if (cssFile != null) {
        css.add(cssFile);
      }
      return this;
    }

    public Builder setInlinerComponent(@NotEmpty String inlinerComponentName) {
      checkArgument(!Strings.isNullOrEmpty(inlinerComponentName));
      inlinerComponent = inlinerComponentName;
      return this;
    }

    public Builder addInlinerConfig(@Nullable Map<String, String> config) {
      if (config != null) {
        inlinerConfig.putAll(config);
      }
      return this;
    }

    public MailingConfig build() {
      return new MailingConfig(this);
    }

  }

  private final String id;
  private final String subject;
  private final String contentHtml;
  private final String contentPlain;
  private final ServerClass serverClass;
  private final String referenceUserId;
  private final String referenceGroupId;
  private final String referenceAttributeId;
  private final List<String> css;
  private final String inlinerComponent;
  private final Map<String, String> inlinerConfig;

  private MailingConfig(Builder builder) {
    checkArgument(!isNullOrEmpty(builder.id));
    id = builder.id;
    subject = builder.subject;
    contentHtml = builder.contentHtml;
    contentPlain = builder.contentPlain;
    css = builder.css;
    serverClass = builder.serverClass;
    referenceUserId = builder.referenceUserId;
    referenceGroupId = builder.referenceGroupId;
    referenceAttributeId = builder.referenceAttributeId;
    inlinerComponent = builder.inlinerComponent;
    inlinerConfig = builder.inlinerConfig;
  }

  public @NotNull String getId() {
    return id;
  }

  public @Nullable String getSubject() {
    return subject;
  }

  public @Nullable String getContentHtml() {
    return contentHtml;
  }

  public @Nullable String getContentHtmlCssInlined() throws CssInlineException {
    return getCssInliner().inline(getContentHtml(), css, inlinerConfig);
  }

  public @Nullable String getContentPlain() {
    return contentPlain;
  }

  public @NotNull List<String> getCssForInlining() {
    return css;
  }

  public @NotNull ServerClass getServerClass() {
    return serverClass;
  }

  public @NotEmpty String getReferenceGroupId() {
    return referenceGroupId;
  }

  public @NotEmpty String getReferenceAttributeId() {
    return referenceAttributeId;
  }

  public @NotEmpty String getReferenceUserId() {
    return referenceUserId;
  }

  private CssInliner getCssInliner() {
    return Utils.getComponent(CssInliner.class, inlinerComponent);
  }
}
