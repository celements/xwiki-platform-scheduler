package com.celements.auth.user.validation;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    Stream<ValidationResult> validationResults = Stream.empty();
    validationResults = Stream.concat(validationResults,
        getEmailParam(params).map(emailParam -> validate(emailParam)).orElse(Stream.empty()));

    return validationResults.collect(Collectors.toList());
  }

  private Stream<ValidationResult> validate(DocFormRequestParam emailParam) {
    Stream<ValidationResult> validationResults = Stream.empty();
    String email = emailParam.getValues().get(0);
    validationResults = Stream.concat(validationResults, checkEmailValidity(email).stream());
    validationResults = Stream.concat(validationResults,
        checkUniqueEmail(email, emailParam).stream());
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

  Optional<ValidationResult> checkEmailValidity(String email) {
    if (mailSenderService.isValidEmail(email)) {
      return Optional.empty();
    }
    return Optional
        .of(new ValidationResult(ValidationType.ERROR, null, "cel_useradmin_emailInvalid"));
  }

  Optional<ValidationResult> checkUniqueEmail(String email,
      DocFormRequestParam emailParam) {
    Optional<User> user = userService.getPossibleUserForLoginField(email,
        userService.getPossibleLoginFields());
    if (user.isEmpty() || user.get().getDocRef().equals(emailParam.getKey().getDocRef())) {
      return Optional.empty();
    }
    return Optional
        .of(new ValidationResult(ValidationType.ERROR, null, "cel_useradmin_emailNotUnique"));
  }

}
