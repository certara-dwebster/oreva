package org.odata4j.producer.resources;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Logger;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ContextResolver;

import org.core4j.Enumerable;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.OEntityId;
import org.odata4j.core.OEntityIds;
import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmMultiplicity;
import org.odata4j.exceptions.NotFoundException;
import org.odata4j.format.FormatParser;
import org.odata4j.format.FormatParserFactory;
import org.odata4j.format.FormatWriter;
import org.odata4j.format.FormatWriterFactory;
import org.odata4j.format.SingleLink;
import org.odata4j.format.SingleLinks;
import org.odata4j.producer.EntityIdResponse;
import org.odata4j.producer.ODataContextImpl;
import org.odata4j.producer.ODataProducer;

public class LinksRequestResource extends BaseResource {

  private static final Logger log = Logger.getLogger(LinksRequestResource.class.getName());

  private final OEntityId sourceEntity;
  private final String targetNavProp;
  private final OEntityKey targetEntityKey;

  public LinksRequestResource(OEntityId sourceEntity, String targetNavProp, OEntityKey targetEntityKey) {
    this.sourceEntity = sourceEntity;
    this.targetNavProp = targetNavProp;
    this.targetEntityKey = targetEntityKey;
  }

  @POST
  public Response createLink(@Context HttpHeaders httpHeaders,
      @Context UriInfo uriInfo,
      @Context ContextResolver<ODataProducer> producerResolver,
      @Context SecurityContext securityContext,
      String payload) {
    log.info(String.format(
        "createLink(%s,%s,%s,%s)",
        sourceEntity.getEntitySetName(),
        sourceEntity.getEntityKey(),
        targetNavProp,
        targetEntityKey));

    ODataProducer producer = producerResolver.getContext(ODataProducer.class);

    OEntityId newTargetEntity = parseRequestUri(httpHeaders, uriInfo, payload);
    producer.createLink(ODataContextImpl.builder().aspect(httpHeaders).aspect(securityContext).build(), sourceEntity, targetNavProp, newTargetEntity);
    return noContent();
  }

  @PUT
  public Response updateLink(@Context HttpHeaders httpHeaders,
      @Context UriInfo uriInfo,
      @Context ContextResolver<ODataProducer> producerResolver,
      @Context SecurityContext securityContext,
      String payload) {
    log.info(String.format(
        "updateLink(%s,%s,%s,%s)",
        sourceEntity.getEntitySetName(),
        sourceEntity.getEntityKey(),
        targetNavProp,
        targetEntityKey));

    ODataProducer producer = producerResolver.getContext(ODataProducer.class);

    OEntityId newTargetEntity = parseRequestUri(httpHeaders, uriInfo, payload);
    producer.updateLink(ODataContextImpl.builder().aspect(httpHeaders).aspect(securityContext).build(), sourceEntity, targetNavProp, targetEntityKey, newTargetEntity);
    return noContent();
  }

  private OEntityId parseRequestUri(HttpHeaders httpHeaders, UriInfo uriInfo, String payload) {
    FormatParser<SingleLink> parser = FormatParserFactory.getParser(SingleLink.class, httpHeaders.getMediaType(), null);
    SingleLink link = parser.parse(new StringReader(payload));
    return OEntityIds.parse(uriInfo.getBaseUri().toString(), link.getUri());
  }

  private Response noContent() {
    return Response.noContent().header(ODataConstants.Headers.DATA_SERVICE_VERSION, ODataConstants.DATA_SERVICE_VERSION_HEADER).build();
  }

  @DELETE
  public Response deleteLink(@Context HttpHeaders httpHeaders,
      @Context UriInfo uriInfo,
      @Context ContextResolver<ODataProducer> producerResolver,
      @Context SecurityContext securityContext) {
    log.info(String.format(
        "deleteLink(%s,%s,%s,%s)",
        sourceEntity.getEntitySetName(),
        sourceEntity.getEntityKey(),
        targetNavProp,
        targetEntityKey));

    ODataProducer producer = producerResolver.getContext(ODataProducer.class);

    producer.deleteLink(ODataContextImpl.builder().aspect(httpHeaders).aspect(securityContext).build(), sourceEntity, targetNavProp, targetEntityKey);
    return noContent();
  }

  @GET
  public Response getLinks(@Context HttpHeaders httpHeaders,
      @Context UriInfo uriInfo,
      @Context ContextResolver<ODataProducer> producerResolver,
      @Context SecurityContext securityContext,
      @QueryParam("$format") String format,
      @QueryParam("$callback") String callback) {

    log.info(String.format(
        "getLinks(%s,%s,%s,%s)",
        sourceEntity.getEntitySetName(),
        sourceEntity.getEntityKey(),
        targetNavProp,
        targetEntityKey));

    ODataProducer producer = producerResolver.getContext(ODataProducer.class);

    EntityIdResponse response = producer.getLinks(ODataContextImpl.builder().aspect(httpHeaders).aspect(securityContext).build(), sourceEntity, targetNavProp);

    StringWriter sw = new StringWriter();
    String serviceRootUri = uriInfo.getBaseUri().toString();
    String contentType;
    if (response.getMultiplicity() == EdmMultiplicity.MANY) {
      SingleLinks links = SingleLinks.create(serviceRootUri, response.getEntities(), sourceEntity, targetNavProp);
      FormatWriter<SingleLinks> fw = FormatWriterFactory.getFormatWriter(SingleLinks.class, httpHeaders.getAcceptableMediaTypes(), format, callback);
      fw.write(uriInfo, sw, links);
      contentType = fw.getContentType();
    } else {
      OEntityId entityId = Enumerable.create(response.getEntities()).firstOrNull();
      if (entityId == null)
        throw new NotFoundException();

      SingleLink link = SingleLinks.create(serviceRootUri, entityId, sourceEntity, targetNavProp);
      FormatWriter<SingleLink> fw = FormatWriterFactory.getFormatWriter(SingleLink.class, httpHeaders.getAcceptableMediaTypes(), format, callback);
      fw.write(uriInfo, sw, link);
      contentType = fw.getContentType();
    }

    String entity = sw.toString();

    return Response.ok(entity, contentType).header(ODataConstants.Headers.DATA_SERVICE_VERSION, ODataConstants.DATA_SERVICE_VERSION_HEADER).build();
  }

}
