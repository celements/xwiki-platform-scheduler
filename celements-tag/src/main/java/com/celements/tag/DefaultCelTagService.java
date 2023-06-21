package com.celements.tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.stereotype.Service;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.lambda.Try;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.field.XObjectFieldAccessor;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.tag.classdefs.CelTagClass;
import com.celements.tag.providers.CelTagsProvider;
import com.celements.tag.providers.CelTagsProvider.CelTagsProvisionException;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

@Service
public class DefaultCelTagService implements CelTagService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCelTagService.class);

  private final ListableBeanFactory beanFactory;
  private final IModelAccessFacade modelAccess;
  private final XObjectFieldAccessor fieldAccessor;
  private final AtomicReference<Try<Multimap<String, CelTag>, CelTagsProvisionException>> cache;

  @Inject
  public DefaultCelTagService(
      ListableBeanFactory beanFactory,
      IModelAccessFacade modelAccess,
      XObjectFieldAccessor fieldAccessor) {
    this.beanFactory = beanFactory;
    this.modelAccess = modelAccess;
    this.fieldAccessor = fieldAccessor;
    this.cache = new AtomicReference<>();
  }

  @Override
  public Optional<CelTag> getTag(String type, String name) {
    return getTagsByType().get(type).stream()
        .filter(tag -> tag.getName().equals(name))
        .findFirst();
  }

  @Override
  public Stream<CelTag> streamTags() {
    return getTagsByType().values().stream();
  }

  @Override
  public Multimap<String, CelTag> getTagsByType() {
    try {
      return cache.updateAndGet(trying -> (trying != null) && trying.isSuccess()
          ? trying
          : Try.to(this::collectAllTags))
          .getOrThrow();
    } catch (CelTagsProvisionException exc) {
      LOGGER.error("getTagsByType - failed", exc);
    }
    return ImmutableMultimap.of();
  }

  private Multimap<String, CelTag> collectAllTags() throws CelTagsProvisionException {
    List<CelTag.Builder> tagBuilders = new ArrayList<>();
    for (CelTagsProvider provider : beanFactory.getBeansOfType(CelTagsProvider.class).values()) {
      // TODO log list for each provider
      provider.get().forEach(tagBuilders::add);
    }
    Multimap<String, CelTag> tags = topologicalBuild(tagBuilders);
    LOGGER.info("collectAllTags - {}", tags);
    return tags;
  }

  /**
   * build tag graph in topological order with some form of Kahn's algorithm, assuming directed
   * acyclic graph
   */
  private Multimap<String, CelTag> topologicalBuild(List<CelTag.Builder> tagBuilders) {
    ImmutableMultimap.Builder<String, CelTag> tags = ImmutableMultimap.builder();
    while (!tagBuilders.isEmpty()) {
      List<CelTag> builtTags = buildTagsWithAllDependencies(tagBuilders.iterator());
      for (CelTag tag : builtTags) {
        tags.put(tag.getType(), tag);
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

  @Override
  public Stream<CelTag> getDocTags(DocumentReference docRef) {
    return XWikiObjectFetcher.on(modelAccess.getOrCreateDocument(docRef))
        .filter(CelTagClass.CLASS_REF).stream()
        .flatMap(obj -> getTags(
            fieldAccessor.get(obj, CelTagClass.FIELD_TYPE),
            fieldAccessor.get(obj, CelTagClass.FIELD_TAGS)
                .map(Set::copyOf).orElse(Set.of())));
  }

  private Stream<CelTag> getTags(Optional<String> type, Set<String> tags) {
    return type.map(getTagsByType()::get)
        .map(Collection::stream)
        .orElse(Stream.empty())
        .filter(tag -> tags.contains(tag.getName()));
  }

  public void refresh() {
    cache.set(null);
  }

}
