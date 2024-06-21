package com.celements.auth.user.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Component;
import org.xwiki.model.reference.ClassReference;

import com.celements.auth.user.UserService;
import com.celements.docform.DocFormRequestKey;
import com.celements.docform.DocFormRequestParam;
import com.celements.mailsender.IMailSenderRole;
import com.celements.model.reference.RefBuilder;
import com.celements.validation.IRequestValidationRule;
import com.celements.validation.ValidationResult;
import com.celements.validation.ValidationType;

@Component
public class XWikiUsersValidationRule implements IRequestValidationRule {

  private IMailSenderRole mailSenderService;
  private UserService userService;

  @Inject
  public XWikiUsersValidationRule(IMailSenderRole mailSenderService, UserService userService) {
    this.mailSenderService = mailSenderService;
    this.userService = userService;
  }

  @Override
  public @NotNull List<ValidationResult> validate(@NotNull List<DocFormRequestParam> params) {
    List<ValidationResult> validationResults = new ArrayList<>();

    // run validation only on Userdocs

    // filter params for email field (XWiki.XWikiUsers_0_email)
    Optional<DocFormRequestParam> emailParam = params.stream()
        .filter(p -> p.getKey().getType().equals(DocFormRequestKey.Type.OBJ_FIELD))
        .filter(p -> p.getKey().getClassRef()
            .equals(new RefBuilder().space("XWiki").doc("XWikiUsers").build(ClassReference.class)))
        .filter(p -> p.getKey().getFieldName().equals("email"))
        .findFirst();

    // check if email is a valid string
    if (emailParam.isPresent()
        && !mailSenderService.isValidEmail(emailParam.get().getValues().get(0))) {
      validationResults
          .add(new ValidationResult(ValidationType.ERROR, null, "cel_useradmin_emailInvalid"));
    }

    // check if email exists already in database
    if (false && !isEmailUnique("")) {
      // add entry to validationResults with dictionary key for suitable error message and add
      // dictionary entries to celements dictionary if necessary
    }

    return validationResults;
  }

  private boolean isEmailUnique(String email) {
    // Email has to be unique in database. If possible, reuse
    // com.celements.auth.user.CelementsUserService.checkIdentifiersForExistingUser(Map<String,
    // String>). If it is not unique return false. If a user is updated and the email hasn't
    // changed, the email exists already.
    Map<String, String> userData = new HashMap<>();
    userService.checkIdentifiersForExistingUser(userData);
    return true;
  }

}
