package com.celements.tag;

import java.util.Optional;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

import com.google.common.collect.Multimap;

@NotNull
public interface CelTagService {

  Optional<CelTag> getTag(String type, String name);

  Multimap<String, CelTag> getTagsByType();

  Stream<CelTag> streamTags();

  Stream<CelTag> getDocTags(DocumentReference docRef);

}
