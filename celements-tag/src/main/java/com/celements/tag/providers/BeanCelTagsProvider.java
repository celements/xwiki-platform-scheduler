package com.celements.tag.providers;

import java.util.stream.Stream;

import javax.inject.Inject;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.stereotype.Component;

import com.celements.tag.CelTag;

@Component
public class BeanCelTagsProvider implements CelTagsProvider {

  private final ListableBeanFactory beanFactory;

  @Inject
  public BeanCelTagsProvider(ListableBeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  @Override
  public Stream<CelTag.Builder> get() {
    return beanFactory.getBeansOfType(CelTag.Builder.class).values().stream();
  }

}
