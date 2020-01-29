package com.celements.captcha;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.script.service.ScriptService;

import com.google.common.base.Optional;

@Component("captcha")
public class CaptchaScriptService implements ScriptService {

  @Requirement("reCaptcha")
  private CaptchaServiceRole reCaptcha;

  /** @deprecated since 3.0 - delegate to old implementation */
  @Requirement("captcha_old")
  @Deprecated
  private ScriptService captchaLegacy;

  public Optional<ReCaptchaResponse> reCaptchaVerify() {
    return reCaptcha.verify();
  }

  public boolean reCaptchaVerifySuccess() {
    Optional<ReCaptchaResponse> response = reCaptchaVerify();
    return response.isPresent() ? response.get().isSuccess() : false;
  }

  /**
   * @deprecated since 3.0 - delegate to old implementation
   * @return
   */
  @Deprecated
  public boolean checkCaptcha() {
    return ((CaptchaScriptService) captchaLegacy).checkCaptcha();
  }

  /**
   * @deprecated since 3.0 - delegate to old implementation
   * @return
   */
  @Deprecated
  public String getCaptchaId() {
    return ((CaptchaScriptService) captchaLegacy).getCaptchaId();
  }
}
