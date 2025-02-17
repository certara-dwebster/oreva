package org.odata4j.producer.resources;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.sql.Blob;
import java.util.logging.Logger;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ContextResolver;

import org.core4j.Enumerable;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.ODataVersion;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmProperty;
import org.odata4j.exceptions.NotFoundException;
import org.odata4j.exceptions.NotImplementedException;
import org.odata4j.format.FormatWriter;
import org.odata4j.format.FormatWriterFactory;
import org.odata4j.internal.InternalUtil;
import org.odata4j.producer.BaseResponse;
import org.odata4j.producer.ContextStream;
import org.odata4j.producer.CountResponse;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.ODataContext;
import org.odata4j.producer.ODataContextImpl;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.PropertyResponse;
import org.odata4j.producer.QueryInfo;

public class PropertyRequestResource extends BaseResource {

  private static final Logger log =
      Logger.getLogger(PropertyRequestResource.class.getName());

  @PUT
  public Response updateEntity(
      @Context HttpHeaders httpHeaders,
      @Context UriInfo uriInfo,
      @Context ContextResolver<ODataProducer> producerResolver,
      @Context SecurityContext securityContext,
      @PathParam("entitySetName") String entitySetName,
      @PathParam("id") String id,
      @PathParam("navProp") String navProp,
      InputStream payload) {

    ODataProducer producer = producerResolver.getContext(ODataProducer.class);
    Enumerable<EdmProperty> props = producer.getMetadata().getEdmEntitySet(entitySetName).getType().getProperties();
    
    // only support update NamedStream property 
    for (EdmProperty prop : props) {
      if (prop.getName().equals(navProp)) {
        if (prop.getType().getFullyQualifiedTypeName().equals("Edm.Stream")) {
          QueryInfo query = new QueryInfo(
              null,
              null,
              null,
              null,
              null,
              null,
              OptionsQueryParser.parseCustomOptions(uriInfo),
              null,
              null);

          return updateNamedStreamResponse(producer, entitySetName, id, navProp, query, payload);
        }
      }
    }

    log.info("NavProp: updateEntity Not supported yet.");
    throw new NotImplementedException("NavProp: updateEntity not supported yet.");
  }


  private Response updateNamedStreamResponse(ODataProducer producer, String entitySetName, String id, String navProp, QueryInfo query, InputStream payload) {
    ContextStream streamContext = new ContextStream(payload, null, null);
    producer.updateEntityWithNamedStream(entitySetName, OEntityKey.parse(id), navProp, streamContext);

    // TODO: hmmh..isn't this supposed to be HTTP 204 No Content?
    return Response.ok().header(ODataConstants.Headers.DATA_SERVICE_VERSION, ODataConstants.DATA_SERVICE_VERSION_HEADER).build();
  }


  @POST
  public Response mergeEntity(
      @Context HttpHeaders httpHeaders,
      @Context UriInfo uriInfo,
      @Context ContextResolver<ODataProducer> producerResolver,
      @Context SecurityContext securityContext,
      @PathParam("entitySetName") String entitySetName,
      @PathParam("id") String id,
      @PathParam("navProp") String navProp,
      String payload) throws Exception {

    String method = httpHeaders.getRequestHeaders().getFirst(ODataConstants.Headers.X_HTTP_METHOD);
    if (!"MERGE".equals(method)) {

      ODataProducer producer = producerResolver.getContext(ODataProducer.class);

      // determine the expected entity set
      EdmDataServices metadata = producer.getMetadata();
      EdmEntitySet ees = metadata
          .getEdmEntitySet(metadata.getEdmEntitySet(entitySetName).getType()
              .findNavigationProperty(navProp).getToRole().getType());

      // parse the request entity
      OEntity entity = getRequestEntity(httpHeaders, uriInfo, payload, metadata, ees.getName(), OEntityKey.parse(id));

      // execute the create
      EntityResponse response = producer.createEntity(ODataContextImpl.builder().aspect(httpHeaders).aspect(securityContext).build(),
          entitySetName, OEntityKey.parse(id), navProp, entity);

      if (response == null) {
        throw new NotFoundException();
      }

      // get the FormatWriter for the accepted media types requested by client
      StringWriter sw = new StringWriter();
      FormatWriter<EntityResponse> fw = FormatWriterFactory
          .getFormatWriter(EntityResponse.class, httpHeaders.getAcceptableMediaTypes(), null, null);
      fw.write(uriInfo, sw, response);

      // calculate the uri for the location header
      String relid = InternalUtil.getEntityRelId(response.getEntity());
      String entryId = uriInfo.getBaseUri().toString() + relid;

      // create the response
      String responseEntity = sw.toString();
      return Response
          .ok(responseEntity, fw.getContentType())
          .status(Status.CREATED)
          .location(URI.create(entryId))
          .header(ODataConstants.Headers.DATA_SERVICE_VERSION, ODataConstants.DATA_SERVICE_VERSION_HEADER)
          .build();
    }

    throw new NotImplementedException("Not supported yet.");
  }

  @DELETE
  public Response deleteEntity(
      @Context ContextResolver<ODataProducer> producerResolver,
      @PathParam("entitySetName") String entitySetName,
      @PathParam("id") String id,
      @PathParam("navProp") String navProp) {
    throw new NotImplementedException("Not supported yet.");
  }

  protected Response getStreamResponse(HttpHeaders httpHeaders, UriInfo uriInfo, ODataProducer producer, String entitySetName, String entityId, String name,QueryInfo queryInfo,
      SecurityContext securityContext, ODataContext odataContext) {

//    ONamedStreamExtension namedStreamExtension = producer.findExtension(ONamedStreamExtension.class);
//    if (namedStreamExtension == null) {
//      throw new NotImplementedException();
//    }

//    ContextStream entityStreamCtx = namedStreamExtension.getInputStreamForNamedStream(odataContext, entitySetName, entityId, name, queryInfo);
    ContextStream entityStreamCtx = producer.getInputStreamForNamedStream(entitySetName, OEntityKey.parse(entityId), name, queryInfo);
    StreamingOutput outputStream = ValueRequestResource.getOutputStreamFromInputStream(entityStreamCtx.getInputStream());
    String contentType = entityStreamCtx.getContentType();
    String contentDisposition = entityStreamCtx.getContentDisposition();

    // this is from latest odata4j code, why we choose outputStream?
    //return Response.ok(entityStream, contentType).header("Content-Disposition", contentDisposition).build();

    return Response.ok(outputStream, contentType).header("Content-Disposition", contentDisposition).build();
  }
  
  
  @GET
  @Produces({
      ODataConstants.APPLICATION_ATOM_XML_CHARSET_UTF8,
      ODataConstants.TEXT_JAVASCRIPT_CHARSET_UTF8,
      ODataConstants.APPLICATION_JAVASCRIPT_CHARSET_UTF8 })
  public Response getNavProperty(
      @Context HttpHeaders httpHeaders,
      @Context UriInfo uriInfo,
      @Context ContextResolver<ODataProducer> producerResolver,
      @Context SecurityContext securityContext,
      @PathParam("entitySetName") String entitySetName,
      @PathParam("id") String id,
      @PathParam("navProp") String navProp,
      @QueryParam("$inlinecount") String inlineCount,
      @QueryParam("$top") String top,
      @QueryParam("$skip") String skip,
      @QueryParam("$filter") String filter,
      @QueryParam("$orderby") String orderBy,
      @QueryParam("$format") String format,
      @QueryParam("$callback") String callback,
      @QueryParam("$skiptoken") String skipToken,
      @QueryParam("$expand") String expand,
      @QueryParam("$select") String select) throws Exception {

    QueryInfo query = new QueryInfo(
        OptionsQueryParser.parseInlineCount(inlineCount),
        OptionsQueryParser.parseTop(top),
        OptionsQueryParser.parseSkip(skip),
        OptionsQueryParser.parseFilter(filter),
        OptionsQueryParser.parseOrderBy(orderBy),
        OptionsQueryParser.parseSkipToken(skipToken),
        OptionsQueryParser.parseCustomOptions(uriInfo),
        OptionsQueryParser.parseSelect(expand),
        OptionsQueryParser.parseSelect(select));

    ODataProducer producer = producerResolver.getContext(ODataProducer.class);
   
    Enumerable<EdmProperty> props = producer.getMetadata().getEdmEntitySet(entitySetName).getType().getProperties();
    
    for (EdmProperty prop : props) {
      if (prop.getName().equals(navProp)) {
        if (prop.getType().getFullyQualifiedTypeName().equals("Edm.Stream")) {
          return getStreamResponse(httpHeaders, uriInfo, producer, entitySetName, id, navProp, query, securityContext, null);
        }
      }
    }
   

    if (navProp.endsWith("/$count")
        || navProp.endsWith("/$count/")
        || navProp.contains("/$count?")
        || navProp.contains("/$count/?")) {

      navProp = navProp.replace("/$count", "");

      CountResponse response = producer.getNavPropertyCount(
          ODataContextImpl.builder().aspect(httpHeaders).aspect(securityContext).build(),
          entitySetName,
          OEntityKey.parse(id),
          navProp,
          query);

      if (response == null) {
        throw new NotFoundException();
      }

      String entity = Long.toString(response.getCount());

      ODataVersion version = ODataConstants.DATA_SERVICE_VERSION;

      return Response
          .ok(entity, ODataConstants.TEXT_PLAIN_CHARSET_UTF8)
          .header(ODataConstants.Headers.DATA_SERVICE_VERSION, version.asString)
          .build();
    }
    else {

      BaseResponse response = producer.getNavProperty(
          ODataContextImpl.builder().aspect(httpHeaders).aspect(securityContext).build(),
          entitySetName,
          OEntityKey.parse(id),
          navProp,
          query);

      if (response == null) {
        throw new NotFoundException();
      }

      ODataVersion version = ODataConstants.DATA_SERVICE_VERSION;
      
      if (response instanceof PropertyResponse) {
        String edmTypeName = ((PropertyResponse) response).getProperty().getType().getFullyQualifiedTypeName();
        if (edmTypeName.equals("Edm.Stream")) {
          Object object = ((PropertyResponse) response).getProperty().getValue();
          if (object != null && object instanceof Blob) {
            InputStream binaryStream = ((Blob) object).getBinaryStream();
            StreamingOutput outputStream = ValueRequestResource.getOutputStreamFromInputStream(binaryStream);
            String contentType = "application/octet-stream";
            String contentDisposition = null;

            return Response.ok(outputStream, contentType).header("Content-Disposition", contentDisposition).build();
          }
        }
      }


      StringWriter sw = new StringWriter();
      FormatWriter<?> fwBase;

      /**
       * The raw value of properties should be represented using the text/plain media type.
       * Response should be a plain text and shouldn't be wrapped by enclosing tags
       */
      if (navProp.endsWith("/$value")
          || navProp.endsWith("/$value/")
          || navProp.contains("/$value?")
          || navProp.contains("/$value/?")) {
        PropertyResponse pr = (PropertyResponse) response;
        Object value = pr.getProperty().getValue();
        if (value == null) {
          // if value is null, send 404 response as per specs 
          // http://www.odata.org/documentation/odata-v3-documentation/odata-core/#10221_Requesting_a_Propertys_Raw_Value_using_value
          return Response.status(Status.NOT_FOUND).build();
        } else {
          return Response
              .ok(value.toString(), ODataConstants.TEXT_PLAIN_CHARSET_UTF8)
              .header(ODataConstants.Headers.DATA_SERVICE_VERSION, version.asString)
              .build();
        }
      } else if (response instanceof PropertyResponse) {
        FormatWriter<PropertyResponse> fw =
            FormatWriterFactory.getFormatWriter(
                PropertyResponse.class,
                httpHeaders.getAcceptableMediaTypes(),
                format,
                callback);
        fw.write(uriInfo, sw, (PropertyResponse) response);
        fwBase = fw;
      } else if (response instanceof EntityResponse) {
        FormatWriter<EntityResponse> fw =
            FormatWriterFactory.getFormatWriter(
                EntityResponse.class,
                httpHeaders.getAcceptableMediaTypes(),
                format,
                callback);
        fw.write(uriInfo, sw, (EntityResponse) response);
        fwBase = fw;
      } else if (response instanceof EntitiesResponse) {
        FormatWriter<EntitiesResponse> fw =
            FormatWriterFactory.getFormatWriter(
                EntitiesResponse.class,
                httpHeaders.getAcceptableMediaTypes(),
                format,
                callback);
        fw.write(uriInfo, sw, (EntitiesResponse) response);
        fwBase = fw;

        // TODO remove this hack, check whether we are Version 2.0 compatible anyway
        // the JsonWriter writes feed currently always as Version 2.0
        version = ODataConstants.DATA_SERVICE_VERSION;
      } else {
        throw new NotImplementedException("Unknown BaseResponse type: " + response.getClass().getName());
      }

      String entity = sw.toString();
      return Response
          .ok(entity, fwBase.getContentType())
          .header(ODataConstants.Headers.DATA_SERVICE_VERSION, version.asString)
          .build();
    }
  }
}
