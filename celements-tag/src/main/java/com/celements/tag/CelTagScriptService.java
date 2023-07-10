package com.celements.tag;

import static java.util.stream.Collectors.*;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.xwiki.model.EntityType;
import org.xwiki.script.service.ScriptService;

import com.celements.model.context.ModelContext;

@Component("celtag")
public class CelTagScriptService implements ScriptService {

  private final CelTagService tagService;
  private final ModelContext context;

  @Inject
  public CelTagScriptService(CelTagService tagService, ModelContext context) {
    this.tagService = tagService;
    this.context = context;
  }

  public List<CelTag> getTags(String type) {
    return tagService.getTagsByType().get(type).stream()
        .filter(this::isScopedToCurrentWiki)
        .collect(toList());
  }

  private boolean isScopedToCurrentWiki(CelTag tag) {
    return tag.getScope()
        .map(scope -> context.getWikiRef().equals(scope.extractRef(EntityType.WIKI).orElse(null)))
        .orElse(true); // no defined scope is also valid for the current wiki
  }

}
