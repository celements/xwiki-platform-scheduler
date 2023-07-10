package com.celements.tag;

import java.util.Optional;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import com.google.common.collect.Multimap;
import com.xpn.xwiki.doc.XWikiDocument;

@NotNull
public interface CelTagService {

  Optional<CelTag> getTag(String type, String name);

  Multimap<String, CelTag> getTagsByType();

  Stream<CelTag> streamTags();

  Stream<CelTag> getDocTags(XWikiDocument doc);

  boolean addTags(XWikiDocument doc, CelTag... tags);

}
