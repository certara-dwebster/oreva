package org.odata4j.examples.jersey.consumer;

import jakarta.ws.rs.client.Client;
import org.glassfish.jersey.client.ClientConfig;

/**
 * Client-side extension mechanism - provides a Jersey {@link Client} implementation given a configuration.
 */
public interface JerseyClientFactory {

  /**
   * Creates a new Jersey client.
   *
   * @param clientConfig  the Jersey client api configuration
   */
  Client createClient(ClientConfig clientConfig);

}
