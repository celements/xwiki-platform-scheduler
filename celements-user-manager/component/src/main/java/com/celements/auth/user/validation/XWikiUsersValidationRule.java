package com.celements.auth.user.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Component;
import org.xwiki.model.reference.ClassReference;

import com.celements.auth.user.User;
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

    // run validation only on Userdocs?

    Optional<DocFormRequestParam> emailParam = getEmailParam(params);
    String email = emailParam.get().getValues().get(0);

    if (emailParam.isPresent() && !mailSenderService.isValidEmail(email)) {
      validationResults
          .add(new ValidationResult(ValidationType.ERROR, null, "cel_useradmin_emailInvalid"));
    }
    if (emailParam.isPresent() && !isEmailUnique(email, emailParam.get())) {
      validationResults
          .add(new ValidationResult(ValidationType.ERROR, null, "cel_useradmin_emailNotUnique"));
    }

    return validationResults;
  }

  private Optional<DocFormRequestParam> getEmailParam(List<DocFormRequestParam> params) {
    return params.stream()
        .filter(p -> p.getKey().getType().equals(DocFormRequestKey.Type.OBJ_FIELD))
        .filter(p -> p.getKey().getClassRef()
            .equals(new RefBuilder().space("XWiki").doc("XWikiUsers").build(ClassReference.class)))
        .filter(p -> p.getKey().getFieldName().equals("email"))
        .findFirst();
  }

  private boolean isEmailUnique(String email, DocFormRequestParam emailParam) {
    Optional<User> user = userService.getPossibleUserForLoginField(email,
        userService.getPossibleLoginFields());
    return user.isEmpty() || user.get().getDocRef().equals(emailParam.getKey().getDocRef());
  }

}
