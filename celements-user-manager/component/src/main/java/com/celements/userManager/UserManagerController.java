package com.celements.userManager;

import javax.inject.Inject;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xwiki.context.Execution;

import com.celements.pagelayout.LayoutServiceRole;
import com.xpn.xwiki.XWikiContext;

@Controller
public class UserManagerController {

  @Inject
  private BeanFactory beanFactory;

  private final LayoutServiceRole layoutService;

  @Inject
  public UserManagerController(BeanFactory beanFactory, LayoutServiceRole layoutService) {
    this.beanFactory = beanFactory;
    this.layoutService = layoutService;
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
