package com.celements.tag.observation;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.xwiki.bridge.event.AbstractDocumentEvent;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.bridge.event.WikiCreatedEvent;
import org.xwiki.bridge.event.WikiDeletedEvent;
import org.xwiki.bridge.event.WikiEvent;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.pagetype.classes.PageTypeClass;
import com.celements.tag.CelTagPageType;
import com.celements.tag.DefaultCelTagService;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
public class CelTagReloadListener implements EventListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(CelTagReloadListener.class);

  private final WebApplicationContext springContext;

  @Inject
  public CelTagReloadListener(WebApplicationContext springContext) {
    this.springContext = springContext;
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
  public void onEvent(Event event, Object source, Object data) {
    LOGGER.trace("onEvent - '{}', source '{}', data '{}'", event.getClass(), source, data);
    if ((event instanceof WikiEvent) || ((event instanceof AbstractDocumentEvent)
        && XWikiObjectFetcher.on((XWikiDocument) source)
            .filter(PageTypeClass.FIELD_PAGE_TYPE, CelTagPageType.NAME)
            .exists())) {
      springContext.publishEvent(new DefaultCelTagService.RefreshEvent(event));
    }
  }

}
