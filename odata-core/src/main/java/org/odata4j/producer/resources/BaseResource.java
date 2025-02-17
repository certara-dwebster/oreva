package org.odata4j.producer.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

import org.odata4j.core.ODataConstants;
import org.odata4j.core.ODataVersion;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.exceptions.NotAcceptableException;
import org.odata4j.exceptions.NotImplementedException;
import org.odata4j.format.Entry;
import org.odata4j.format.FormatParser;
import org.odata4j.format.FormatParserFactory;
import org.odata4j.format.Settings;
import org.odata4j.internal.InternalUtil;
import org.odata4j.producer.ODataContext;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.OMediaLinkExtension;

public abstract class BaseResource {

  protected OEntity getRequestEntity(HttpHeaders httpHeaders, UriInfo uriInfo, String payload, EdmDataServices metadata, String entitySetName, OEntityKey entityKey) {
    // TODO validation of MaxDataServiceVersion against DataServiceVersion
    // see spec [ms-odata] section 1.7

    ODataVersion version = InternalUtil.getDataServiceVersion(httpHeaders.getRequestHeaders().getFirst(ODataConstants.Headers.DATA_SERVICE_VERSION));
    return convertFromString(payload, httpHeaders.getMediaType(), version, metadata, entitySetName, entityKey, false);
  }

  protected static OEntity convertFromString(String requestEntity, MediaType type, ODataVersion version, EdmDataServices metadata, String entitySetName, OEntityKey entityKey, Boolean isResponse) throws NotAcceptableException {
    //previously we are hard coding it to have false, since we always get the entity to be created as name, value pair. 
    //setting the isResponse to true only when we are building the OEntity from the response as it contain root element, metadata, data,etc
    FormatParser<Entry> parser = FormatParserFactory.getParser(Entry.class, type,
        new Settings(version, metadata, entitySetName, entityKey, null, isResponse));
    Entry entry = parser.parse(new StringReader(requestEntity));
    return entry.getEntity();
  }

  protected OEntity getRequestEntity(HttpHeaders httpHeaders, UriInfo uriInfo, InputStream payload, EdmDataServices metadata, String entitySetName, OEntityKey entityKey) throws UnsupportedEncodingException {
    // TODO validation of MaxDataServiceVersion against DataServiceVersion
    // see spec [ms-odata] section 1.7

    ODataVersion version = InternalUtil.getDataServiceVersion(httpHeaders.getRequestHeaders().getFirst(ODataConstants.Headers.DATA_SERVICE_VERSION));
    FormatParser<Entry> parser = FormatParserFactory.getParser(Entry.class, httpHeaders.getMediaType(),
        new Settings(version, metadata, entitySetName, entityKey, null, false));

    String charset = httpHeaders.getMediaType().getParameters().get("charset");
    if (charset == null) {
      charset = ODataConstants.Charsets.Upper.ISO_8859_1; // from HTTP 1.1
    }

    Entry entry = parser.parse(new BufferedReader(
        new InputStreamReader(payload, charset)));

    return entry.getEntity();
  }

  // some helpers for media link entries
  protected OMediaLinkExtension getMediaLinkExtension(HttpHeaders httpHeaders, UriInfo uriInfo, EdmEntitySet entitySet, ODataProducer producer,
      ODataContext context) {

    OMediaLinkExtension mediaLinkExtension = producer.findExtension(OMediaLinkExtension.class);

    if (mediaLinkExtension == null) {
      throw new NotImplementedException();
    }

    return mediaLinkExtension;
  }

  protected OEntity createOrUpdateMediaLinkEntry(HttpHeaders httpHeaders,
      UriInfo uriInfo, EdmEntitySet entitySet, ODataProducer producer,
      InputStream payload, OEntityKey key, ODataContext context) throws IOException {

    /*
     * this post has a great descriptions of the twists and turns of creating
     * a media resource + media link entry:  http://blogs.msdn.com/b/astoriateam/archive/2010/08/04/data-services-streaming-provider-series-implementing-a-streaming-provider-part-1.aspx
     */

    // first, the producer must support OMediaLinkExtension
    OMediaLinkExtension mediaLinkExtension = getMediaLinkExtension(httpHeaders, uriInfo, entitySet, producer, context);

    // get a media link entry from the extension
    OEntity mle = key == null
        ? mediaLinkExtension.createMediaLinkEntry(context, entitySet, httpHeaders)
        : mediaLinkExtension.getMediaLinkEntryForUpdateOrDelete(context, entitySet, key, httpHeaders);

    // write the stream
    mle.setMediaLinkStream(payload);

    // more info about the mle may be available now.
    return mle;
  }
}