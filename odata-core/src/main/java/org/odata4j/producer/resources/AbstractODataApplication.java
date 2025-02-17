package org.odata4j.producer.resources;

import java.util.HashSet;
import java.util.Set;

import jakarta.ws.rs.core.Application;

/**
 * Abstract OData application.
 *
 * <p>Implementers should override the {@code getClasses} method, but call
 * {@code super.getClasses()} before adding container-specific resources and providers as
 * required.
 */
public abstract class AbstractODataApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> classes = new HashSet<Class<?>>();
    classes.add(BatchRequestResource.class);
    classes.add(EntitiesRequestResource.class);
    classes.add(EntityRequestResource.class);
    classes.add(MetadataResource.class);
    classes.add(ServiceDocumentResource.class);
    classes.add(ODataBatchUnitProvider.class);
    classes.add(ExceptionMappingProvider.class);
    classes.add(ODataWriteResponseProvider.class);
    return classes;
  }
}
