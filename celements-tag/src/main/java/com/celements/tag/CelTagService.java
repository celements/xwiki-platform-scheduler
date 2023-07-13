package com.celements.tag;

import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.google.common.collect.Multimap;
import com.xpn.xwiki.doc.XWikiDocument;

@NotNull
public interface CelTagService {

  @NotNull
  Optional<CelTag> getTag(@Nullable String type, @Nullable String name);

  @NotNull
  Stream<CelTag> streamAllTags();

  @NotNull
  Multimap<String, CelTag> getTagsByType();

  @NotNull
  Stream<CelTag> getDocTags(@NotNull XWikiDocument doc);

  boolean addTags(@NotNull XWikiDocument doc, @NotNull CelTag... tags);

}
