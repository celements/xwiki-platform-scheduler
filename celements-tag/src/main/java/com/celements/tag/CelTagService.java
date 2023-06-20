package com.celements.tag;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.DocumentReference;

@NotNull
public interface CelTagService {

  Optional<CelTag> getTagForName(String name);

  Stream<CelTag> getTagsOfType(String type);

  Stream<CelTag> getTags(Predicate<CelTag> filter);

  Stream<CelTag> getDocTags(DocumentReference docRef);

}
