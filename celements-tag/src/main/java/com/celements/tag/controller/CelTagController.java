package com.celements.tag.controller;

import static java.util.stream.Collectors.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.celements.tag.CelTag;
import com.celements.tag.CelTagService;

import one.util.streamex.EntryStream;

@RestController
@RequestMapping("/celtags")
public class CelTagController {

  private final CelTagService tagService;

  @Inject
  public CelTagController(CelTagService tagService) {
    this.tagService = tagService;
  }

  @GetMapping
  public Map<String, List<TagDto>> getTags() {
    return EntryStream.of(tagService.getTagsByType().entries().stream())
        .filterValues(CelTag::isRoot)
        .mapValues(TagDto::new)
        .grouping();
  }

  @GetMapping("/types")
  public Set<String> getTypes() {
    return tagService.getTagsByType().keySet();
  }

  @GetMapping("/{type}")
  public List<TagDto> getTagsByType(
      @PathVariable String type) {
    return tagService.getTagsByType().get(type).stream()
        .filter(CelTag::isRoot)
        .sorted(CelTag.CMP_ORDER)
        .map(TagDto::new)
        .collect(toList());
  }

  @GetMapping("/{type}/{name}")
  public ResponseEntity<TagDto> getTagByName(
      @PathVariable String type,
      @PathVariable String name) {
    return tagService.getTag(type, name)
        .map(TagDto::new)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @Immutable
  public class TagDto {

    public final String name;
    public final String type;
    public final List<TagDto> children;

    public TagDto(CelTag tag) {
      name = tag.getName();
      type = tag.getType();
      children = tag.getChildren().map(TagDto::new).toImmutableList(); // TODO sort?
    }
  }

}
