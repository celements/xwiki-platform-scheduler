package com.celements.tag.providers;

import java.util.Collection;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.stereotype.Component;

import com.celements.tag.CelTag;

@Component
public class BeanCelTagsProvider implements CelTagsProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(BeanCelTagsProvider.class);

  private final ListableBeanFactory beanFactory;

  @Inject
  public BeanCelTagsProvider(ListableBeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  @Override
  public Collection<CelTag.Builder> get() {
    var tags = beanFactory.getBeansOfType(CelTag.Builder.class).values();
    LOGGER.info("providing tags: {}", tags);
    return tags;
  }

}
