package org.odata4j.test.integration;

import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import org.glassfish.jersey.client.ClientResponse;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.consumer.behaviors.OClientBehavior;
import org.odata4j.core.ODataConstants.Headers;
import org.odata4j.core.ODataHttpMethod;
import org.odata4j.core.Throwables;
import org.odata4j.examples.jersey.consumer.ODataJerseyConsumer;
import org.odata4j.examples.jersey.consumer.ODataJerseyConsumer.Builder;
import org.odata4j.examples.jersey.producer.server.ODataJerseyServer;
import org.odata4j.format.FormatType;
import org.odata4j.producer.resources.DefaultODataApplication;
import org.odata4j.producer.resources.RootApplication;
import org.odata4j.producer.server.ODataServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Map.Entry;

public class JerseyRuntimeFacade implements RuntimeFacade {

  @Override
  public void hostODataServer(String baseUri) {
    try {
      ODataServer server = startODataServer(baseUri);
      System.out.println("Press any key to exit");
      new BufferedReader(new InputStreamReader(System.in)).readLine();
      server.stop();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public ODataServer startODataServer(String baseUri) {
    return this.createODataServer(baseUri).start();
  }

  @Override
  public ODataServer createODataServer(String baseUri) {

    return new ODataJerseyServer(baseUri, DefaultODataApplication.class, RootApplication.class);
        //.addJerseyRequestFilter(ClientLoggingFilter.class); // log all requests
  }

  @Override
  public ODataConsumer createODataConsumer(String endpointUri, FormatType format, OClientBehavior... clientBehaviors) {
    Builder builder = ODataJerseyConsumer.newBuilder(endpointUri);

    if (format != null) {
      builder = builder.setFormatType(format);
    }

    if (clientBehaviors != null) {
      builder = builder.setClientBehaviors(clientBehaviors);
    }

    return builder.build();
  }

  @Override
  public ResponseData getWebResource(String uri) {
    WebTarget webResource = ClientBuilder.newClient().target(uri);

    ClientResponse response = webResource.request().get(ClientResponse.class);
    return new ResponseData(response.getStatus(), response.readEntity(String.class));
  }

  @Override
  public ResponseData postWebResource(String uri, InputStream content, MediaType mediaType, Map<String, Object> headers) {
    return this.requestWebResource(ODataHttpMethod.POST, uri, content, mediaType, headers);
  }

  public ResponseData putWebResource(String uri, InputStream content, MediaType mediaType, Map<String, Object> headers) {
    return this.requestWebResource(ODataHttpMethod.PUT, uri, content, mediaType, headers);
  }

  @Override
  public ResponseData acceptAndReturn(String uri, MediaType mediaType) {
    uri = uri.replace(" ", "%20");

    WebTarget webResource = ClientBuilder.newClient().target(uri);

    ClientResponse response = webResource.request(mediaType).get(ClientResponse.class);
    String body = response.readEntity(String.class);

    return new ResponseData(response.getStatus(), body);
  }

  @Override
  public ResponseData getWebResource(String uri, String accept) {
    WebTarget webResource = ClientBuilder.newClient().target(uri);

    ClientResponse response = webResource.request(accept).get(ClientResponse.class);
    String body = response.readEntity(String.class);

    return new ResponseData(response.getStatus(), body);
  }

  @Override
  public void accept(String uri, MediaType mediaType) {
    uri = uri.replace(" ", "%20");
    WebTarget webResource = ClientBuilder.newClient().target(uri);
    webResource.request(mediaType);
  }

  @Override
  public ResponseData mergeWebResource(String uri, InputStream content, MediaType mediaType, Map<String, Object> headers) {
    return this.requestWebResource(ODataHttpMethod.MERGE, uri, content, mediaType, headers);
  }

  @Override
  public ResponseData patchWebResource(String uri, InputStream content, MediaType mediaType, Map<String, Object> headers) {
    return this.requestWebResource(ODataHttpMethod.PATCH, uri, content, mediaType, headers);
  }

  @Override
  public ResponseData getWebResource(String uri, InputStream content, MediaType mediaType, Map<String, Object> headers) {
    return this.requestWebResource(ODataHttpMethod.GET, uri, content, mediaType, headers);
  }

  @Override
  public ResponseData deleteWebResource(String uri, InputStream content, MediaType mediaType, Map<String, Object> headers) {
    return this.requestWebResource(ODataHttpMethod.DELETE, uri, content, mediaType, headers);
  }

  private ResponseData requestWebResource(ODataHttpMethod method, String uri, InputStream content, MediaType mediaType, Map<String, Object> headers) {
    Client client = ClientBuilder.newClient();
    Invocation.Builder webResource = client.target(uri).request(mediaType);
    int statusCode;
    String entity = "";
      if (headers != null) {
        for (Entry<String, Object> entry : headers.entrySet()) {
          webResource = webResource.header(entry.getKey(), entry.getValue());
        }
      }

      ClientResponse response;
      switch (method) {
      case DELETE:
        response = webResource.delete(ClientResponse.class);
        break;
      case PATCH:
        webResource = webResource.header(Headers.X_HTTP_METHOD, "PATCH");
        response = webResource.post(Entity.entity(content, mediaType), ClientResponse.class);
        break;
      case GET:
        response = webResource.get(ClientResponse.class);
        break;
      case MERGE:
        webResource = webResource.header(Headers.X_HTTP_METHOD, "MERGE");
        response = webResource.post(Entity.entity(content, mediaType), ClientResponse.class);
        break;
      case POST:
        response = webResource.post(Entity.entity(content, mediaType), ClientResponse.class);
        break;
      case PUT:
        response = webResource.put(Entity.entity(content, mediaType), ClientResponse.class);
        break;
      default:
        throw new RuntimeException("Unsupported http method: " + method);
      }

      statusCode = response.getStatus();
      entity = response.readEntity(String.class);
    return new ResponseData(statusCode, entity);
  }

}
