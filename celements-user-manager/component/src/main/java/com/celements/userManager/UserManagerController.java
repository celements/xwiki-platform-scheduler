package com.celements.userManager;

import javax.inject.Inject;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xwiki.context.Execution;

import com.xpn.xwiki.XWikiContext;

@Controller
public class UserManagerController {

  @Inject
  private BeanFactory beanFactory;

  @Inject
  public UserManagerController(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  @GetMapping("/celAdmin")
  @ResponseBody
  public XWikiContext celAdmin() {
    return (XWikiContext) beanFactory
        .getBean(Execution.class)
        .getContext()
        .getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
  }
}
