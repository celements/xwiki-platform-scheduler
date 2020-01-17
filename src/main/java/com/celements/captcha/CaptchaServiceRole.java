package com.celements.captcha;

import org.xwiki.component.annotation.ComponentRole;

import com.google.common.base.Optional;

@ComponentRole
public interface CaptchaServiceRole {

  Optional<ReCaptchaResponse> verify();

}
