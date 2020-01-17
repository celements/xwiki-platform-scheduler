package com.celements.captcha;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.script.service.ScriptService;

import com.google.common.base.Optional;

@Component("captcha")
public class CaptchaScriptService implements ScriptService {

  @Requirement("reCaptcha")
  CaptchaServiceRole reCaptcha;

  public Optional<ReCaptchaResponse> reCaptchaVerify() {
    return reCaptcha.verify();
  }

  public boolean reCaptchaVerifies() {
    Optional<ReCaptchaResponse> response = reCaptchaVerify();
    return response.isPresent() ? response.get().isSuccess() : false;
  }
}
