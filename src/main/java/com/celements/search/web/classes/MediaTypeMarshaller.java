package com.celements.search.web.classes;

import static com.google.common.base.Preconditions.*;

import javax.annotation.concurrent.Immutable;

import com.celements.marshalling.AbstractMarshaller;
import com.google.common.base.Optional;
import com.google.common.net.MediaType;

@Immutable
public final class MediaTypeMarshaller extends AbstractMarshaller<MediaType> {

  public MediaTypeMarshaller() {
    super(MediaType.class);
  }

  @Override
  public String serialize(MediaType val) {
    return val.toString();
  }

  @Override
  public Optional<MediaType> resolve(String val) {
    MediaType mediaType = null;
    try {
      mediaType = MediaType.parse(checkNotNull(val));
    } catch (IllegalArgumentException exc) {
      LOGGER.info("failed to resolve '{}' for '{}'", val, getToken(), exc);
    }
    return Optional.fromNullable(mediaType);
  }

}
