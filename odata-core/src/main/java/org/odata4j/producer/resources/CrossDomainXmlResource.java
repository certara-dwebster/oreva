package org.odata4j.producer.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Path("crossdomain.xml")
public class CrossDomainXmlResource {

  @GET
  @Produces("text/xml")
  public String getCrossDomainXml() {
    String content = "<?xml version=\"1.0\"?>" +
        "<!DOCTYPE cross-domain-policy SYSTEM \"http://www.adobe.com/xml/dtds/cross-domain-policy.dtd\">" +
        "<cross-domain-policy>" +
        "  <allow-access-from domain=\"*\"/>" +
        "</cross-domain-policy>";
    return content;
  }

}
