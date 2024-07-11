package com.celements.auth.user.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Component;
import org.xwiki.model.reference.DocumentReference;

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

/**
 * This class validates params with the ClassReference XWiki.XWikiUsers. It only handles one
 * user per request and one email per user. Other requests will be invalid. An email may not be
 * null, nor an empty String.
 *
 * @author cpichler
 */
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
    List<DocFormRequestParam> paramsToValidate = findParamsWithXWikiUsersClassRef(params);
    if (!paramsToValidate.isEmpty()) {
      if (isRequestInvalid(paramsToValidate)) {
        return List
            .of(new ValidationResult(ValidationType.ERROR, null, "cel_useradmin_invalidRequest"));
      }
      return validateParams(paramsToValidate);
    }
    return List.of();
  }

  private boolean isRequestInvalid(List<DocFormRequestParam> paramsToValidate) {
    return isNotSameUser(paramsToValidate)
        || hasSeveralEmailParams(paramsToValidate)
        || isNotXWikiSpace(paramsToValidate);
  }

  private boolean isNotSameUser(List<DocFormRequestParam> params) {
    int objNb = params.get(0).getKey().getObjNb();
    boolean isSameUser = true;
    DocumentReference userDocRef = params.get(0).getDocRef();
    for (DocFormRequestParam param : params) {
      isSameUser = isSameUser && (param.getKey().getObjNb() == objNb)
          && param.getDocRef().equals(userDocRef);
    }
    return !isSameUser;
  }

  private boolean hasSeveralEmailParams(List<DocFormRequestParam> params) {
    return getEmailParams(params).count() > 1;
  }

  private boolean isNotXWikiSpace(List<DocFormRequestParam> params) {
    DocFormRequestParam param = params.get(0);
    return !param.getDocRef().getLastSpaceReference().getName().equals(XWikiConstant.XWIKI_SPACE);
  }

  private List<ValidationResult> validateParams(List<DocFormRequestParam> params) {
    List<ValidationResult> validationResults = new ArrayList<>();
    Optional<DocFormRequestParam> emailParam = getEmailParams(params).findAny();
    validationResults.addAll(checkEmailValidity(emailParam));
    checkRegisterAccessRights(params.get(0)).ifPresent(validationResults::add);
    return validationResults;
  }

  List<ValidationResult> checkEmailValidity(Optional<DocFormRequestParam> emailParam) {
    List<ValidationResult> validationResults = new ArrayList<>();
    // DocFormRequestParam turns null values into empty Strings and deletes those from the list of
    // values. You should always get a list but it might be empty.
    if (emailParam.isPresent() || !emailParam.get().getValues().isEmpty()) {
      String email = emailParam.get().getValues().get(0);
      if (mailSenderService.isValidEmail(email)) {
        checkUniqueEmail(email, emailParam.get()).ifPresent(validationResults::add);
      } else {
        validationResults
            .add(new ValidationResult(ValidationType.ERROR, null, "cel_useradmin_emailInvalid"));
      }
    } else {
      validationResults
          .add(new ValidationResult(ValidationType.ERROR, null, "cel_useradmin_missingEmail"));
    }
    return validationResults;
  }

  Optional<ValidationResult> checkUniqueEmail(String email,
      DocFormRequestParam emailParam) {
    Optional<User> user = userService.getPossibleUserForLoginField(email,
        userService.getPossibleLoginFields());
    if (user.isEmpty() || user.get().getDocRef().equals(emailParam.getDocRef())) {
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

  private List<DocFormRequestParam> findParamsWithXWikiUsersClassRef(
      List<DocFormRequestParam> params) {
    return params.stream()
        .filter(p -> p.getKey().getType().equals(DocFormRequestKey.Type.OBJ_FIELD))
        .filter(p -> p.getKey().getClassRef().equals(XWikiUsersClass.CLASS_REF))
        .collect(Collectors.toList());
  }

  private Stream<DocFormRequestParam> getEmailParams(List<DocFormRequestParam> params) {
    return params.stream().filter(p -> p.getKey().getFieldName().equals("email"));
  }

}
