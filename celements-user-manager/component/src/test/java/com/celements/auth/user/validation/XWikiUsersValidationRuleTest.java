package com.celements.auth.user.validation;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.docform.DocFormRequestKey;
import com.celements.docform.DocFormRequestParam;
import com.celements.mailsender.IMailSenderRole;
import com.celements.model.reference.RefBuilder;
import com.celements.validation.ValidationResult;
import com.celements.validation.ValidationType;

public class XWikiUsersValidationRuleTest extends AbstractComponentTest {

  private XWikiUsersValidationRule rule;

  @Before
  public void prepare() throws Exception {
    registerComponentMocks(IMailSenderRole.class);
    rule = getBeanFactory().getBean(XWikiUsersValidationRule.class);
  }

  @Test
  public void test_validate_invalidEmail() {
    List<DocFormRequestParam> params = new ArrayList<>();
    DocFormRequestParam emailParam = new DocFormRequestParam(DocFormRequestKey.createObjFieldKey(
        "XWiki.XWikiUsers_0_email", new DocumentReference("wiki", "XWiki", "321adfawer"),
        new RefBuilder().space("XWiki").doc("XWikiUsers").build(ClassReference.class), 0, "email"),
        List.of("abc"));
    params.add(emailParam);
    expect(getMock(IMailSenderRole.class).isValidEmail("abc")).andReturn(false);

    replayDefault();
    List<ValidationResult> results = rule.validate(params);
    verifyDefault();

    assertEquals(1, results.size());
    assertEquals(ValidationType.ERROR, results.get(0).getType());
    assertEquals("cel_useradmin_emailInvalid", results.get(0).getMessage());
  }

}
