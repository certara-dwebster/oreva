package org.odata4j.examples.jersey.consumer;

import java.io.InputStream;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;

import org.odata4j.consumer.ODataClientResponse;

import org.glassfish.jersey.client.ClientResponse;

public class JerseyClientResponse implements ODataClientResponse {

  private ClientResponse clientResponse;

  public JerseyClientResponse(ClientResponse clientResponse) {
    this.clientResponse = clientResponse;
  }

  public ClientResponse getClientResponse() {
    return clientResponse;
  }

  @Override
  public MultivaluedMap<String, String> getHeaders() {
    return clientResponse.getHeaders();
  }

  @Override
  public void close() {}

  @Override
  public InputStream getEntityInputStream() {
    return clientResponse.getEntityStream();
  }

  @Override
  public MediaType getMediaType() {
    return clientResponse.getMediaType();
  }
}
