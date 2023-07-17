package com.celements.tag.observation;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.xwiki.bridge.event.AbstractDocumentEvent;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.bridge.event.WikiCreatedEvent;
import org.xwiki.bridge.event.WikiDeletedEvent;
import org.xwiki.bridge.event.WikiEvent;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.init.CelementsInitialisedEvent;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.pagetype.classes.PageTypeClass;
import com.celements.tag.CelTagPageType;
import com.celements.tag.DefaultCelTagService;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
public class CelTagReloadListener
    implements ApplicationListener<CelementsInitialisedEvent>, EventListener, Ordered {

  private static final Logger LOGGER = LoggerFactory.getLogger(CelTagReloadListener.class);

  private final ApplicationEventPublisher eventPublisher;

  @Inject
  public CelTagReloadListener(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  @Override
  public String getName() {
    return this.getClass().getName();
  }

  @Override
  public List<Event> getEvents() {
    return List.of(
        new WikiCreatedEvent(),
        new WikiDeletedEvent(),
        new DocumentCreatedEvent(),
        new DocumentUpdatedEvent(),
        new DocumentDeletedEvent());
  }

  @Override
  public void onApplicationEvent(CelementsInitialisedEvent event) {
    eventPublisher.publishEvent(new DefaultCelTagService.RefreshEvent(event));
  }

  @Override
  public void onEvent(Event event, Object source, Object data) {
    LOGGER.trace("onEvent - '{}', source '{}', data '{}'", event.getClass(), source, data);
    if ((event instanceof WikiEvent) || ((event instanceof AbstractDocumentEvent)
        && XWikiObjectFetcher.on((XWikiDocument) source)
            .filter(PageTypeClass.FIELD_PAGE_TYPE, CelTagPageType.NAME)
            .exists())) {
      eventPublisher.publishEvent(new DefaultCelTagService.RefreshEvent(event));
    }
  }

  @Override
  public int getOrder() {
    return 100; // low precedence
  }

}
