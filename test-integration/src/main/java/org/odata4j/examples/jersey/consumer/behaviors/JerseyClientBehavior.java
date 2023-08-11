package org.odata4j.examples.jersey.consumer.behaviors;

import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.Configurable;
import org.odata4j.consumer.behaviors.OClientBehavior;

import org.glassfish.jersey.client.ClientConfig;

public interface JerseyClientBehavior extends OClientBehavior {

  public void modify(ClientConfig clientConfig);

  public void modifyClientFilters(Configurable client);

  public void modifyWebResourceFilters(Configurable webResource);

}
