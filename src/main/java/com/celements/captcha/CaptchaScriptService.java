/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.captcha;

import java.util.Optional;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.script.service.ScriptService;

import com.celements.model.context.ModelContext;
import com.celements.web.plugin.cmd.CaptchaCommand;

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
    return response.isPresent() && response.get().isSuccess();
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
