package com.celements.tag.controller;

import static java.util.stream.Collectors.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.celements.tag.CelTag;
import com.celements.tag.CelTagService;

@RestController
@RequestMapping("/celtags")
public class CelTagController {

  private final CelTagService tagService;

  @Inject
  public CelTagController(CelTagService tagService) {
    this.tagService = tagService;
  }

  @GetMapping
  public Map<String, Collection<CelTag>> getTags() {
    return tagService.getTagsByType().asMap();
  }

  @GetMapping("/types")
  public Set<String> getTypes() {
    return tagService.getTagsByType().keySet();
  }

  @GetMapping("/types/{type}")
  public List<CelTag> getTagsByType(
      @PathVariable String type) {
    return tagService.getTagsByType().get(type).stream()
        .sorted(CelTag.CMP_ORDER)
        .collect(toList());
  }

  @GetMapping("/types/{type}/names/{name}")
  public ResponseEntity<CelTag> getTagByName(
      @PathVariable String type,
      @PathVariable String name) {
    return tagService.getTag(type, name)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

}
