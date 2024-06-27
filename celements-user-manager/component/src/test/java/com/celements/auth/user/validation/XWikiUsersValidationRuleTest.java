package com.celements.auth.user.validation;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.auth.user.User;
import com.celements.auth.user.UserService;
import com.celements.common.test.AbstractComponentTest;
import com.celements.docform.DocFormRequestKey;
import com.celements.docform.DocFormRequestParam;
import com.celements.mailsender.IMailSenderRole;
import com.celements.model.reference.RefBuilder;
import com.celements.rights.access.EAccessLevel;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.celements.validation.ValidationResult;
import com.celements.validation.ValidationType;

public class XWikiUsersValidationRuleTest extends AbstractComponentTest {

  private XWikiUsersValidationRule rule;
  private DocumentReference userDocRef1 = new DocumentReference("wiki", "XWiki", "321adfawer");

  @Before
  public void prepare() throws Exception {
    registerComponentMocks(IMailSenderRole.class,
        UserService.class,
        User.class,
        IRightsAccessFacadeRole.class);
    rule = getBeanFactory().getBean(XWikiUsersValidationRule.class);
  }

  @Test
  public void test_validate_noEmailParam() {
    List<DocFormRequestParam> params = new ArrayList<>();

    replayDefault();
    List<ValidationResult> results = rule.validate(params);
    verifyDefault();

    assertEquals(0, results.size());
  }

  @Test
  public void test_checkEmailValidity_invalidEmail() {
    String email = "abc";
    expect(getMock(IMailSenderRole.class).isValidEmail(email)).andReturn(false);

    replayDefault();
    Optional<ValidationResult> result = rule.checkEmailValidity(email);
    verifyDefault();

    assertTrue(result.isPresent());
    assertEquals(ValidationType.ERROR, result.get().getType());
    assertEquals("cel_useradmin_emailInvalid", result.get().getMessage());
  }

  @Test
  public void test_checkUniqueEmail_emailNotUnique() {
    String email = "abc+test@synventis.com";
    DocFormRequestParam emailParam = createEmailParam(email, 0);
    DocumentReference userDocRef2 = new DocumentReference("wiki", "XWiki", "a65e4rafgoij");
    User user = createDefaultMock(User.class);
    expect(getMock(UserService.class).getPossibleLoginFields()).andReturn(new HashSet<String>());
    expect(getMock(UserService.class)
        .getPossibleUserForLoginField("abc+test@synventis.com", new HashSet<String>()))
            .andReturn(Optional.of(user));
    expect(user.getDocRef()).andReturn(userDocRef2);

    replayDefault();
    Optional<ValidationResult> result = rule.checkUniqueEmail(email, emailParam);
    verifyDefault();

    assertTrue(result.isPresent());
    assertEquals(ValidationType.ERROR, result.get().getType());
    assertEquals("cel_useradmin_emailNotUnique", result.get().getMessage());
  }

  @Test
  public void test_validate_allOk() {
    List<DocFormRequestParam> params = new ArrayList<>();
    params.add(createEmailParam("abc+test@synventis.com", 0));
    expect(getMock(IMailSenderRole.class).isValidEmail("abc+test@synventis.com")).andReturn(true);
    expect(getMock(UserService.class).getPossibleLoginFields()).andReturn(new HashSet<String>());
    expect(getMock(UserService.class)
        .getPossibleUserForLoginField("abc+test@synventis.com", new HashSet<String>()))
            .andReturn(Optional.empty());

    replayDefault();
    List<ValidationResult> results = rule.validate(params);
    verifyDefault();

    assertEquals(0, results.size());
  }

  @Test
  public void test_checkRegisterAccessRights_noRights() {
    String email = "abc+test@synventis.com";
    DocFormRequestParam emailParam = createEmailParam(email, -1);
    expect(getMock(IRightsAccessFacadeRole.class).isAdmin()).andReturn(false);
    expect(getMock(IRightsAccessFacadeRole.class)
        .hasAccessLevel(emailParam.getDocRef(), EAccessLevel.REGISTER)).andReturn(false);

    replayDefault();
    Optional<ValidationResult> result = rule.checkRegisterAccessRights(emailParam);
    verifyDefault();

    assertTrue(result.isPresent());
    assertEquals(ValidationType.ERROR, result.get().getType());
    assertEquals("cel_useradmin_noRegisterRights", result.get().getMessage());
  }

  private DocFormRequestParam createEmailParam(String email, int objNb) {
    DocFormRequestParam emailParam = new DocFormRequestParam(DocFormRequestKey.createObjFieldKey(
        "XWiki.XWikiUsers_" + objNb + "_email", userDocRef1,
        new RefBuilder().space("XWiki").doc("XWikiUsers").build(ClassReference.class),
        objNb,
        "email"),
        List.of(email));
    return emailParam;

  }

}
