package org.odata4j.examples.jersey.internal;

import org.glassfish.jersey.message.internal.AbstractMessageReaderWriterProvider;
import org.glassfish.jersey.message.internal.ReaderWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;

@Produces({ "text/plain", "*/*" })
@Consumes({ "text/plain", "*/*" })
public final class StringProvider2 extends AbstractMessageReaderWriterProvider<String> {

  public boolean isReadable(Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
    return type == String.class;
  }

  public String readFrom(Class<String> type, Type genericType, Annotation annotations[], MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException {
    return readFromAsString(entityStream, mediaType);
  }

  public boolean isWriteable(Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
    return type == String.class;
  }

  public void writeTo(String t, Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {
    // writeToAsString(t, entityStream, mediaType);

    Writer osw = new BufferedWriter(new OutputStreamWriter(entityStream, ReaderWriter.getCharset(mediaType)), 8 * 1024); // explicit 8k size FOR ANDROID
    osw.write(t);
    osw.flush();
  }

}