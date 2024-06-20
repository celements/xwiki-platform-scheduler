package com.celements.auth.user.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Component;

import com.celements.auth.user.UserService;
import com.celements.docform.DocFormRequestParam;
import com.celements.mailsender.IMailSenderRole;
import com.celements.validation.IRequestValidationRule;
import com.celements.validation.ValidationResult;

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
    // if everything is valid return empty List
    List<ValidationResult> validationResults = new ArrayList<>();

    // filter params for email field (XWiki.XWikiUsers_0_email)

    // check if email is a valid string
    String email = "";
    if (!mailSenderService.isValidEmail(email)) {
      // add entry to validationResults with dictionary key for suitable error message and add
      // dictionary entries to celements dictionary if necessary
    }

    // check if email exists already in database
    if (!isEmailUnique("")) {
      // add entry to validationResults with dictionary key for suitable error message and add
      // dictionary entries to celements dictionary if necessary
    }

    return validationResults;
  }

  private boolean isEmailUnique(String email) {
    // Email has to be unique in database. If possible, reuse
    // com.celements.auth.user.CelementsUserService.checkIdentifiersForExistingUser(Map<String,
    // String>). If it is not unique return false.
    Map<String, String> userData = new HashMap<>();

    return userService.checkIdentifiersForExistingUser(userData).isPresent();
  }

}
