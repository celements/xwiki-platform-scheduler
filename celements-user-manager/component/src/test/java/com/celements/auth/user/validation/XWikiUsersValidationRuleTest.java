package com.celements.auth.user.validation;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;

public class XWikiUsersValidationRuleTest extends AbstractComponentTest {

  private XWikiUsersValidationRule rule;

  @Before
  public void prepare() {
    rule = getBeanFactory().getBean(XWikiUsersValidationRule.class);
  }

  @Test
  public void text_validate_invalidEmail() {

  }

}
