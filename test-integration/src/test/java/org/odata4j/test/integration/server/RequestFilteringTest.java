package org.odata4j.test.integration.server;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.junit.Test;
import org.odata4j.examples.cxf.producer.server.ODataCxfServer;
import org.odata4j.examples.jersey.producer.server.ODataJerseyServer;
import org.odata4j.producer.resources.DefaultODataProducerProvider;
import org.odata4j.test.integration.AbstractJettyHttpClientTest;
import org.odata4j.test.integration.TestInMemoryProducers;


public class RequestFilteringTest extends AbstractJettyHttpClientTest {

  public RequestFilteringTest(RuntimeFacadeType type) {
    super(type);
  }

  @Override
  protected void startODataServer() throws Exception {
    server = rtFacade.createODataServer(BASE_URI);
    addRequestFilter();
    server.start();
  }

  private Handler jettyRequestHandler = spy(new DefaultHandler());

  public static class JerseyRequestFilterStub implements ContainerRequestFilter {
    static boolean isCalled = false;

    @Override
    public void filter(ContainerRequestContext request) {
      isCalled = true;
    }
  }

  private void addRequestFilter() {
    if (server instanceof ODataCxfServer)
      ((ODataCxfServer) server).addJettyRequestHandler(jettyRequestHandler);
    else if (server instanceof ODataJerseyServer)
      ((ODataJerseyServer) server).addJerseyRequestFilter(JerseyRequestFilterStub.class);
  }

  @Override
  protected void registerODataProducer() throws Exception {
    DefaultODataProducerProvider.setInstance(TestInMemoryProducers.simple());
  }

  @Test
  public void filterIsCalled() throws Exception {
//    ContentExchange exchange = sendRequest(BASE_URI);
//    exchange.waitForDone();
    verifyFilterIsCalled();
  }

  private void verifyFilterIsCalled() throws Exception {
    if (server instanceof ODataCxfServer)
      verify(jettyRequestHandler, atLeastOnce()).handle(anyString(), any(Request.class), any(HttpServletRequest.class), any(HttpServletResponse.class));
    else if (server instanceof ODataJerseyServer)
      assertTrue("Request filter has not been called", JerseyRequestFilterStub.isCalled);
  }
}
