package com.celements.captcha;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.script.service.ScriptService;

import com.celements.model.context.ModelContext;
import com.celements.web.plugin.cmd.CaptchaCommand;
import com.google.common.base.Optional;

@Component("captcha")
public class CaptchaScriptService implements ScriptService {

  @Requirement("reCaptcha")
  private CaptchaServiceRole reCaptcha;

  @Requirement
  private ModelContext context;

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
    return new CaptchaCommand().checkCaptcha(context.getXWikiContext());
  }

  /**
   * @deprecated since 3.0 - delegate to old implementation
   * @return
   */
  @Deprecated
  public String getCaptchaId() {
    return new CaptchaCommand().getCaptchaId(context.getXWikiContext());
  }
}
