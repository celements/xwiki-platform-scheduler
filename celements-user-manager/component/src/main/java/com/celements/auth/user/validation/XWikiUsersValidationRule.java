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
import com.celements.rights.access.EAccessLevel;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.celements.validation.IRequestValidationRule;
import com.celements.validation.ValidationResult;
import com.celements.validation.ValidationType;

@Component
public class XWikiUsersValidationRule implements IRequestValidationRule {

  private IMailSenderRole mailSenderService;
  private UserService userService;
  private IRightsAccessFacadeRole rightsAccess;

  @Inject
  public XWikiUsersValidationRule(IMailSenderRole mailSenderService,
      UserService userService,
      IRightsAccessFacadeRole rightsAccess) {
    this.mailSenderService = mailSenderService;
    this.userService = userService;
    this.rightsAccess = rightsAccess;
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
    validationResults = Stream.concat(validationResults,
        checkRegisterAccessRights(emailParam).stream());
    return validationResults;

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

  Optional<ValidationResult> checkRegisterAccessRights(DocFormRequestParam emailParam) {
    if (checkForNewUser(emailParam).isEmpty()
        || rightsAccess.isAdmin()
        || rightsAccess.hasAccessLevel(emailParam.getDocRef(), EAccessLevel.REGISTER)) {
      return Optional.empty();
    }
    return Optional
        .of(new ValidationResult(ValidationType.ERROR, null, "cel_useradmin_noRegisterRights"));
  }

  private Optional<DocFormRequestParam> getEmailParam(List<DocFormRequestParam> params) {
    return params.stream()
        .filter(p -> p.getKey().getType().equals(DocFormRequestKey.Type.OBJ_FIELD))
        .filter(p -> p.getKey().getClassRef()
            .equals(new RefBuilder().space("XWiki").doc("XWikiUsers").build(ClassReference.class)))
        .filter(p -> p.getKey().getFieldName().equals("email"))
        .findFirst();
  }

  private Optional<DocFormRequestParam> checkForNewUser(DocFormRequestParam emailParam) {
    return Optional.of(emailParam).filter(p -> p.getKey().getObjNb() == -1);
  }

}
