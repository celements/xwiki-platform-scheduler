package com.celements.tag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.lambda.Try;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.tag.classdefs.CelTagClass;
import com.celements.tag.providers.CelTagsProvider;
import com.celements.tag.providers.CelTagsProvider.CelTagsProvisionException;
import com.google.common.collect.ImmutableMap;

@Service
public class DefaultCelTagService implements CelTagService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCelTagService.class);

  private final IModelAccessFacade modelAccess;
  private final List<CelTagsProvider> tagsProviders;
  private final AtomicReference<Try<Map<String, CelTag>, CelTagsProvisionException>> cache;

  @Inject
  public DefaultCelTagService(
      IModelAccessFacade modelAccess,
      List<CelTagsProvider> tagsProviders) {
    this.tagsProviders = List.copyOf(tagsProviders);
    this.modelAccess = modelAccess;
    this.cache = new AtomicReference<>();
  }

  @Override
  public Optional<CelTag> getTagForName(String name) {
    return Optional.ofNullable(getTagMap().get(name));
  }

  @Override
  public Stream<CelTag> getTagsOfType(String type) {
    return getTags(tag -> tag.getType().equals(type));
  }

  @Override
  public Stream<CelTag> getTags(Predicate<CelTag> filter) {
    return getTagMap().values().stream().filter(filter);
  }

  @Override
  public Stream<CelTag> getDocTags(DocumentReference docRef) {
    return XWikiObjectFetcher.on(modelAccess.getOrCreateDocument(docRef))
        .fetchField(CelTagClass.FIELD_TAGS)
        .stream().flatMap(List::stream)
        .map(this::getTagForName)
        .flatMap(Optional::stream);
  }

  Map<String, CelTag> getTagMap() {
    try {
      return cache.updateAndGet(trying -> (trying != null) && trying.isSuccess()
          ? trying
          : Try.to(this::collectAllTags))
          .getOrThrow();
    } catch (CelTagsProvisionException exc) {
      LOGGER.error("getTagMap - failed", exc);
    }
    return Map.of();
  }

  private Map<String, CelTag> collectAllTags() throws CelTagsProvisionException {
    List<CelTag.Builder> tagBuilders = new ArrayList<>();
    for (CelTagsProvider provider : tagsProviders) {
      // TODO log list for each provider
      provider.get().forEach(tagBuilders::add);
    }
    Map<String, CelTag> tags = topologicalBuild(tagBuilders);
    LOGGER.info("collectAllTags - {}", tags);
    return tags;
  }

  /**
   * build tag graph in topological order with some form of Kahn's algorithm, assuming directed
   * acyclic graph
   */
  private Map<String, CelTag> topologicalBuild(List<CelTag.Builder> tagBuilders) {
    ImmutableMap.Builder<String, CelTag> tags = ImmutableMap.builder();
    while (!tagBuilders.isEmpty()) {
      List<CelTag> builtTags = buildTagsWithAllDependencies(tagBuilders.iterator());
      for (CelTag tag : builtTags) {
        tags.put(tag.getName(), tag);
        tagBuilders.stream().forEach(b -> b.addDependency(tag));
      }
      if (builtTags.isEmpty()) {
        throw new IllegalStateException("tags don't form a directed acyclic graph: " + tagBuilders);
      }
    }
    return tags.build();
  }

  private List<CelTag> buildTagsWithAllDependencies(Iterator<CelTag.Builder> tagBuilderIter) {
    List<CelTag> built = new ArrayList<>();
    while (tagBuilderIter.hasNext()) {
      CelTag.Builder builder = tagBuilderIter.next();
      if (builder.hasAllDependencies()) {
        try {
          built.add(builder.build());
        } catch (IllegalArgumentException iae) {
          LOGGER.info("unable to build tag [{}]", builder, iae);
        }
        tagBuilderIter.remove();
      }
    }
    return built;
  }

  public void refresh() {
    cache.set(null);
  }

}
