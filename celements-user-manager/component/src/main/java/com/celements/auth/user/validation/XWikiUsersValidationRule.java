package com.celements.auth.user.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Component;

import com.celements.auth.user.User;
import com.celements.auth.user.UserService;
import com.celements.docform.DocFormRequestKey;
import com.celements.docform.DocFormRequestParam;
import com.celements.mailsender.IMailSenderRole;
import com.celements.rights.access.EAccessLevel;
import com.celements.rights.access.IRightsAccessFacadeRole;
import com.celements.validation.IRequestValidationRule;
import com.celements.validation.ValidationResult;
import com.celements.validation.ValidationType;
import com.celements.web.classes.oldcore.XWikiUsersClass;
import com.xpn.xwiki.XWikiConstant;

@Component
public class XWikiUsersValidationRule implements IRequestValidationRule {

  private final IMailSenderRole mailSenderService;
  private final UserService userService;
  private final IRightsAccessFacadeRole rightsAccess;

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
    return findParamsWithXWikiUsersClassRef(params)
        .flatMap(this::validate)
        .collect(Collectors.toList());
  }

  // TODO neue Validierung ergänzen: alle Params mit der ClassReference XWiki.XWikiUsers müssen die
  // gleiche Objektnummer und die gleiche DocRef haben. Den Case, dass mehrere User in einem Request
  // sind, decken wir momentan nicht ab. Im Javadoc festhalten!!!

  private Stream<ValidationResult> validate(DocFormRequestParam emailParam) {
    List<ValidationResult> validationResults = new ArrayList<>();
    Stream<String> emails = emailParam.getValues().stream();
    // checkEmailValidity(email).ifPresent(validationResults::add);
    // checkUniqueEmail(email, emailParam).ifPresent(validationResults::add);
    checkRegisterAccessRights(emailParam).ifPresent(validationResults::add);
    checkXWikiSpace(emailParam).ifPresent(validationResults::add);
    return validationResults.stream();

  }

  // TODO wenn im Request mehrere EmailParams für die gleiche DocRef vorhanden sind, ist das für den
  // Moment ein ungültiger Request

  Optional<ValidationResult> checkEmailValidity(String email) {
    // TODO darf nicht null sein, darf kein EmptyString sein, muss valides Format haben.
    // evtl. 2 verschiedene Validationmessages ausliefern

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

  private Optional<DocFormRequestParam> checkForNewUser(DocFormRequestParam emailParam) {
    return Optional.of(emailParam).filter(p -> p.getKey().getObjNb() == -1);
  }

  Optional<ValidationResult> checkXWikiSpace(DocFormRequestParam emailParam) {
    if (isXWikiSpace(emailParam)) {
      return Optional.empty();
    }
    return Optional
        .of(new ValidationResult(ValidationType.ERROR, null, "cel_useradmin_notXwikiSpace"));
  }

  private boolean isXWikiSpace(DocFormRequestParam emailParam) {
    return emailParam.getDocRef().getLastSpaceReference().getName()
        .equals(XWikiConstant.XWIKI_SPACE);
  }

  private Stream<DocFormRequestParam> findParamsWithXWikiUsersClassRef(
      List<DocFormRequestParam> params) {
    return params.stream()
        .filter(p -> p.getKey().getType().equals(DocFormRequestKey.Type.OBJ_FIELD))
        .filter(p -> p.getKey().getClassRef().equals(XWikiUsersClass.CLASS_REF));
  }

  private Optional<DocFormRequestParam> getEmailParam(Stream<DocFormRequestParam> params) {
    return params
        .filter(p -> p.getKey().getFieldName().equals("email"))
        .findFirst();
  }
}
