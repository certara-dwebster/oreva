package org.odata4j.examples.jersey.consumer;

import jakarta.ws.rs.client.Client;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;

/**
 * The default factory implementation for Jersey clients.
 *
 * <p>Use {@link #INSTANCE} to obtain a reference to the singleton instance of this factory.</p>
 */
public class DefaultJerseyClientFactory implements JerseyClientFactory {

  public static final DefaultJerseyClientFactory INSTANCE = new DefaultJerseyClientFactory();

  private DefaultJerseyClientFactory() {}

  /**
   * Creates a new default {@link Client} by calling: <code>Client.create(clientConfig)</code>
   */
  @Override
  public Client createClient(ClientConfig clientConfig) {
    JerseyClient client = JerseyClientBuilder.createClient(clientConfig);
    return client;
  }

}
