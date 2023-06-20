package com.celements.tag.providers;

import java.util.List;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.celements.tag.CelTag;

@Component
public class BeanCelTagsProvider implements CelTagsProvider {

  private final List<CelTag.Builder> builderBeans;

  @Inject
  public BeanCelTagsProvider(List<CelTag.Builder> builderBeans) {
    this.builderBeans = List.copyOf(builderBeans);
  }

  @Override
  public Stream<CelTag.Builder> get() {
    return builderBeans.stream();
  }

}
