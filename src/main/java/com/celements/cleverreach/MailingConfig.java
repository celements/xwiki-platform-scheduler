package com.celements.cleverreach;

import static com.google.common.base.Preconditions.*;
import static com.google.common.base.Strings.*;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

@Immutable
public class MailingConfig {

  public static class Builder {

    private String id;
    private String subject;
    private String contentHtml;
    private String contentPlain;
    private List<Byte[]> css = new ArrayList<>();

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

    public Builder addCssForInlining(@Nullable Byte[] cssFile) {
      if (cssFile != null) {
        css.add(cssFile);
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
  private final List<Byte[]> css;

  private MailingConfig(Builder builder) {
    checkArgument(!isNullOrEmpty(builder.id));
    id = builder.id;
    subject = builder.subject;
    contentHtml = builder.contentHtml;
    contentPlain = builder.contentPlain;
    css = builder.css;
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

  public @Nullable String getContentHtmlCssInlined() {
    // TODO implement
    return getContentHtml();
  }

  public @Nullable String getContentPlain() {
    return contentPlain;
  }

  public @NotNull List<Byte[]> getCssForInlining() {
    return css;
  }
}
