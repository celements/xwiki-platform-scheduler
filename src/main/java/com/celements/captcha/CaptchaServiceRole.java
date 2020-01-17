package com.celements.captcha;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

import com.google.common.base.Optional;

@ComponentRole
public interface CaptchaServiceRole {

  @NotNull
  Optional<ReCaptchaResponse> verify();

}
