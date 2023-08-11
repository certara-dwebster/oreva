package org.odata4j.consumer;

import java.io.InputStream;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;

/**
 * Generic OData http response used by the low-level {@link ODataClient} api.
 *
 * @see ODataClient
 */
public interface ODataClientResponse {

  MultivaluedMap<String, String> getHeaders();

  InputStream getEntityInputStream();
  MediaType getMediaType();

  void close();
}
