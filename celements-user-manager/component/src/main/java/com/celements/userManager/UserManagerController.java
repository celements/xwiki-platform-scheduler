package com.celements.userManager;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xwiki.model.reference.SpaceReference;

import com.celements.model.context.ModelContext;
import com.celements.model.reference.RefBuilder;
import com.celements.pagelayout.LayoutServiceRole;

@Controller
public class UserManagerController {

  private final ModelContext context;

  private final LayoutServiceRole layoutService;

  @Inject
  public UserManagerController(
      LayoutServiceRole layoutService,
      ModelContext context) {
    this.layoutService = layoutService;
    this.context = context;
  }

  // TODO celSkinHeader is missing and Groupcell doesn't render
  @GetMapping("/celAdmin")
  @ResponseBody
  public String celAdmin() {
    SpaceReference userAdminSpaceRef = RefBuilder.from(context.getWikiRef()).space("UserAdmin")
        .build(SpaceReference.class);
    return layoutService.renderPageLayout(userAdminSpaceRef);
  }
}
