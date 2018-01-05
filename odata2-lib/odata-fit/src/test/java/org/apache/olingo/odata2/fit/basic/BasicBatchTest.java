/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.fit.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.batch.BatchHandler;
import org.apache.olingo.odata2.api.batch.BatchRequestPart;
import org.apache.olingo.odata2.api.batch.BatchResponsePart;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderBatchProperties;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataRequest;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.api.uri.info.GetSimplePropertyUriInfo;
import org.apache.olingo.odata2.api.uri.info.PutMergePatchUriInfo;
import org.apache.olingo.odata2.core.PathInfoImpl;
import org.apache.olingo.odata2.core.ep.util.CircleStreamBuffer;
import org.apache.olingo.odata2.core.ep.util.FormatJson;
import org.apache.olingo.odata2.core.ep.util.JsonStreamWriter;
import org.apache.olingo.odata2.core.processor.ODataSingleProcessorService;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.apache.olingo.odata2.testutil.server.ServletType;
import org.junit.Test;

/**
 *  
 */
public class BasicBatchTest extends AbstractBasicTest {

  public BasicBatchTest(final ServletType servletType) {
    super(servletType);
  }

  private static final String CRLF = "\r\n";
  private static final String REG_EX_BOUNDARY =
      "(([a-zA-Z0-9_\\-\\.'\\+]{1,70})|\"([a-zA-Z0-9_\\-\\.'\\+\\s\\(\\),/:=\\?]" +
          "{1,69}[a-zA-Z0-9_\\-\\.'\\+\\(\\),/:=\\?])\")";
  private static final String REG_EX = "multipart/mixed;\\s*boundary=" + REG_EX_BOUNDARY + "\\s*";

  private static final String REQUEST_PAYLOAD =
      "--batch_98c1-8b13-36bb" + CRLF
          + "Content-Type: application/http" + CRLF
          + "Content-Transfer-Encoding: binary" + CRLF
          + "Content-Id: mimeHeaderContentId1" + CRLF
          + CRLF
          + "GET Employees('1')/EmployeeName HTTP/1.1" + CRLF
          + "Host: localhost:19000" + CRLF
          + "Accept: application/atomsvc+xml;q=0.8, application/json;odata=verbose;q=0.5, */*;q=0.1" + CRLF
          + "Accept-Language: en" + CRLF
          + "MaxDataServiceVersion: 2.0" + CRLF
          + "Content-Id: requestHeaderContentId1" + CRLF
          + CRLF
          + CRLF
          + "--batch_98c1-8b13-36bb" + CRLF
          + "Content-Type: multipart/mixed; boundary=changeset_f980-1cb6-94dd" + CRLF
          + CRLF
          + "--changeset_f980-1cb6-94dd" + CRLF
          + "Content-Type: application/http" + CRLF
          + "Content-Transfer-Encoding: binary" + CRLF
          + "Content-Id: mimeHeaderContentId2" + CRLF
          + CRLF
          + "PUT Employees('1')/EmployeeName HTTP/1.1" + CRLF
          + "Host: localhost:19000" + CRLF
          + "Content-Type: application/json;odata=verbose" + CRLF
          + "MaxDataServiceVersion: 2.0" + CRLF
          + "Content-Id: requestHeaderContentId2" + CRLF
          + CRLF
          + "{\"EmployeeName\":\"Walter Winter MODIFIED\"}" + CRLF
          + CRLF
          + "--changeset_f980-1cb6-94dd--" + CRLF
          + CRLF
          + "--batch_98c1-8b13-36bb--";

  @Test
  public void testBatch() throws Exception {
    final HttpPost post = new HttpPost(URI.create(getEndpoint().toString() + "$batch"));
    post.setHeader("Content-Type", "multipart/mixed;boundary=batch_98c1-8b13-36bb");
    HttpEntity entity = new StringEntity(REQUEST_PAYLOAD);
    post.setEntity(entity);
    HttpResponse response = getHttpClient().execute(post);

    assertNotNull(response);
    assertEquals(202, response.getStatusLine().getStatusCode());
    assertEquals("HTTP/1.1", response.getProtocolVersion().toString());
    assertTrue(response.containsHeader("Content-Length"));
    assertTrue(response.containsHeader("Content-Type"));
    assertTrue(response.containsHeader("DataServiceVersion"));
    assertTrue(response.getEntity().getContentType().getValue().matches(REG_EX));
    assertNotNull(response.getEntity().getContent());

    String body = StringHelper.inputStreamToString(response.getEntity().getContent(), true);
    assertTrue(body.contains("Content-Id: mimeHeaderContentId1"));
    assertTrue(body.contains("Content-Id: requestHeaderContentId1"));
    assertTrue(body.contains("Content-Id: mimeHeaderContentId2"));
    assertTrue(body.contains("Content-Id: requestHeaderContentId2"));
  }

  @Test
  public void testBatchWithODataBatchRequestHeaders() throws Exception {
    final HttpPost post = new HttpPost(URI.create(getEndpoint().toString() + "$batch"));
    post.setHeader("Content-Type", "multipart/mixed;boundary=batch_98c1-8b13-36bb");
    post.setHeader("testHeader", "abc123");
    HttpEntity entity = new StringEntity(REQUEST_PAYLOAD);
    post.setEntity(entity);
    HttpResponse response = getHttpClient().execute(post);

    assertNotNull(response);
    assertEquals(202, response.getStatusLine().getStatusCode());
    assertEquals("HTTP/1.1", response.getProtocolVersion().toString());
    assertTrue(response.containsHeader("Content-Length"));
    assertTrue(response.containsHeader("Content-Type"));
    assertTrue(response.containsHeader("DataServiceVersion"));
    assertTrue(response.getEntity().getContentType().getValue().matches(REG_EX));
    assertNotNull(response.getEntity().getContent());

    String body = StringHelper.inputStreamToString(response.getEntity().getContent(), true);
    assertTrue(body.contains("Content-Id: mimeHeaderContentId1"));
    assertTrue(body.contains("Content-Id: requestHeaderContentId1"));
    assertTrue(body.contains("Content-Id: mimeHeaderContentId2"));
    assertTrue(body.contains("Content-Id: requestHeaderContentId2"));
  }
  
  @Test
  public void testBatchUriEncoded() throws Exception {
    final HttpPost post = new HttpPost(URI.create(getEndpoint().toString() + "%24batch"));
    post.setHeader("Content-Type", "multipart/mixed;boundary=batch_98c1-8b13-36bb");
    HttpEntity entity = new StringEntity(REQUEST_PAYLOAD);
    post.setEntity(entity);
    HttpResponse response = getHttpClient().execute(post);

    assertNotNull(response);
    assertEquals(202, response.getStatusLine().getStatusCode());
    assertEquals("HTTP/1.1", response.getProtocolVersion().toString());
    assertTrue(response.containsHeader("Content-Length"));
    assertTrue(response.containsHeader("Content-Type"));
    assertTrue(response.containsHeader("DataServiceVersion"));
    assertTrue(response.getEntity().getContentType().getValue().matches(REG_EX));
    assertNotNull(response.getEntity().getContent());

    String body = StringHelper.inputStreamToString(response.getEntity().getContent(), true);
    assertTrue(body.contains("Content-Id: mimeHeaderContentId1"));
    assertTrue(body.contains("Content-Id: requestHeaderContentId1"));
    assertTrue(body.contains("Content-Id: mimeHeaderContentId2"));
    assertTrue(body.contains("Content-Id: requestHeaderContentId2"));
  }
  
  @Test
  public void testBatchInvalidContentTypeForPut() throws Exception {
    final HttpPost post = new HttpPost(URI.create(getEndpoint().toString() + "$batch"));
    post.setHeader("Content-Type", "multipart/mixed;boundary=batch_98c1-8b13-36bb");
    String replacedEntity = REQUEST_PAYLOAD.replace("Content-Type: application/json;odata=verbose" + CRLF, "");
    HttpEntity entity = new StringEntity(replacedEntity);
    post.setEntity(entity);
    HttpResponse response = getHttpClient().execute(post);

    assertNotNull(response);
    assertEquals(202, response.getStatusLine().getStatusCode());
    assertEquals("HTTP/1.1", response.getProtocolVersion().toString());
    assertTrue(response.containsHeader("Content-Length"));
    assertTrue(response.containsHeader("Content-Type"));
    assertTrue(response.containsHeader("DataServiceVersion"));
    assertTrue(response.getEntity().getContentType().getValue().matches(REG_EX));
    assertNotNull(response.getEntity().getContent());

    String body = StringHelper.inputStreamToString(response.getEntity().getContent(), true);
    assertTrue(body.contains("Content-Id: mimeHeaderContentId1"));
    assertTrue(body.contains("Content-Id: requestHeaderContentId1"));
    assertTrue(body.contains("HTTP/1.1 415 Unsupported Media Type"));
  }
  
 /* Tests for Custo Query options.A Custom Query String option is defined  
  * as any name/value pair query string parameter where the name of the 
  * parameter does not begin  with the "$" character. Any URI exposed by 
  * an OData service may include one or more Custom Query Options.*/
  
  @Test
  public void testBatchForCustomQuery() throws Exception {
    final HttpPost post = new HttpPost(URI.create(getEndpoint().toString() + 
        "$batch?language=de"));
    post.setHeader("Content-Type", "multipart/mixed;boundary=batch_98c1-8b13-36bb");
    HttpEntity entity = new StringEntity(REQUEST_PAYLOAD);
    post.setEntity(entity);
    HttpResponse response = getHttpClient().execute(post);

    assertNotNull(response);
    assertEquals(202, response.getStatusLine().getStatusCode());
    assertEquals("HTTP/1.1", response.getProtocolVersion().toString());
    assertTrue(response.containsHeader("Content-Length"));
    assertTrue(response.containsHeader("Content-Type"));
    assertTrue(response.containsHeader("DataServiceVersion"));
    assertTrue(response.getEntity().getContentType().getValue().matches(REG_EX));
    assertNotNull(response.getEntity().getContent());

    String body = StringHelper.inputStreamToString(response.getEntity().getContent(), true);
    assertTrue(body.contains("Content-Id: mimeHeaderContentId1"));
    assertTrue(body.contains("Content-Id: requestHeaderContentId1"));
    assertTrue(body.contains("Content-Id: mimeHeaderContentId2"));
    assertTrue(body.contains("Content-Id: requestHeaderContentId2"));
  }
  
  @Test
  public void testBatchForCustomQueryFail() throws Exception {
    final HttpPost post = new HttpPost(URI.create(getEndpoint().toString() + 
        "$batch?$language=de"));
    post.setHeader("Content-Type", "multipart/mixed;boundary=batch_98c1-8b13-36bb");
    HttpEntity entity = new StringEntity(REQUEST_PAYLOAD);
    post.setEntity(entity);
    HttpResponse result = getHttpClient().execute(post);
    assertEquals(HttpStatusCodes.BAD_REQUEST.getStatusCode(), result.getStatusLine().getStatusCode());

  }
  
  @Test
  public void testBatchForCustomQuery2() throws Exception {
    final HttpPost post = new HttpPost(URI.create(getEndpoint().toString() + 
        "$batch?@language=de"));
    post.setHeader("Content-Type", "multipart/mixed;boundary=batch_98c1-8b13-36bb");
    HttpEntity entity = new StringEntity(REQUEST_PAYLOAD);
    post.setEntity(entity);
    HttpResponse response = getHttpClient().execute(post);

    assertNotNull(response);
    assertEquals(202, response.getStatusLine().getStatusCode());
    assertEquals("HTTP/1.1", response.getProtocolVersion().toString());
    assertTrue(response.containsHeader("Content-Length"));
    assertTrue(response.containsHeader("Content-Type"));
    assertTrue(response.containsHeader("DataServiceVersion"));
    assertTrue(response.getEntity().getContentType().getValue().matches(REG_EX));
    assertNotNull(response.getEntity().getContent());

    String body = StringHelper.inputStreamToString(response.getEntity().getContent(), true);
    assertTrue(body.contains("Content-Id: mimeHeaderContentId1"));
    assertTrue(body.contains("Content-Id: requestHeaderContentId1"));
    assertTrue(body.contains("Content-Id: mimeHeaderContentId2"));
    assertTrue(body.contains("Content-Id: requestHeaderContentId2"));
  }
  
  @Test
  public void testBatchForCustomQuery3() throws Exception {
    final HttpPost post = new HttpPost(URI.create(getEndpoint().toString() + 
        "$batch?#language=de"));
    post.setHeader("Content-Type", "multipart/mixed;boundary=batch_98c1-8b13-36bb");
    HttpEntity entity = new StringEntity(REQUEST_PAYLOAD);
    post.setEntity(entity);
    HttpResponse response = getHttpClient().execute(post);

    assertNotNull(response);
    assertEquals(202, response.getStatusLine().getStatusCode());
    assertEquals("HTTP/1.1", response.getProtocolVersion().toString());
    assertTrue(response.containsHeader("Content-Length"));
    assertTrue(response.containsHeader("Content-Type"));
    assertTrue(response.containsHeader("DataServiceVersion"));
    assertTrue(response.getEntity().getContentType().getValue().matches(REG_EX));
    assertNotNull(response.getEntity().getContent());

    String body = StringHelper.inputStreamToString(response.getEntity().getContent(), true);
    assertTrue(body.contains("Content-Id: mimeHeaderContentId1"));
    assertTrue(body.contains("Content-Id: requestHeaderContentId1"));
    assertTrue(body.contains("Content-Id: mimeHeaderContentId2"));
    assertTrue(body.contains("Content-Id: requestHeaderContentId2"));
  }

  static class TestSingleProc extends ODataSingleProcessor {
    @SuppressWarnings("unchecked")
    @Override
    public ODataResponse executeBatch(final BatchHandler handler, final String requestContentType,
        final InputStream content) {

      assertFalse(getContext().isInBatchMode());

      ODataResponse batchResponse;
      List<BatchResponsePart> batchResponseParts = new ArrayList<BatchResponsePart>();
      PathInfoImpl pathInfo = new PathInfoImpl();
      try {
        pathInfo.setServiceRoot(new URI("http://localhost:19000/odata"));

        EntityProviderBatchProperties batchProperties = EntityProviderBatchProperties.init().pathInfo(pathInfo).build();
        List<BatchRequestPart> batchParts =
            EntityProvider.parseBatchRequest(requestContentType, content, batchProperties);
        for (BatchRequestPart batchPart : batchParts) {
          batchResponseParts.add(handler.handleBatchPart(batchPart));
          List<String> customHeader = ((ArrayList<String>)((HashMap<String, Object>)getContext().
              getParameter("batchODataRequestHeaders")).get("testheader"));
          if (getContext().getParameter("batchODataRequestHeaders") != null && 
              customHeader != null) {
            assertEquals("abc123", customHeader.get(0));
          }
        }
        batchResponse = EntityProvider.writeBatchResponse(batchResponseParts);
      } catch (URISyntaxException e) {
        throw new RuntimeException(e);
      } catch (ODataException e) {
        throw new RuntimeException(e);
      }
      return batchResponse;
    }

    @Override
    public BatchResponsePart executeChangeSet(final BatchHandler handler, final List<ODataRequest> requests)
        throws ODataException {
      assertTrue(getContext().isInBatchMode());

      List<ODataResponse> responses = new ArrayList<ODataResponse>();
      for (ODataRequest request : requests) {
        ODataResponse response = handler.handleRequest(request);
        if (response.getStatus().getStatusCode() >= HttpStatusCodes.BAD_REQUEST.getStatusCode()) {
          // Rollback
          List<ODataResponse> errorResponses = new ArrayList<ODataResponse>(1);
          errorResponses.add(response);
          return BatchResponsePart.responses(errorResponses).changeSet(false).build();
        }
        responses.add(response);
      }
      return BatchResponsePart.responses(responses).changeSet(true).build();
    }

    @Override
    public ODataResponse readEntitySimpleProperty(final GetSimplePropertyUriInfo uriInfo, final String contentType)
        throws ODataException {
      assertTrue(getContext().isInBatchMode());

      CircleStreamBuffer buffer = new CircleStreamBuffer();
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(buffer.getOutputStream()));
      JsonStreamWriter jsonStreamWriter = new JsonStreamWriter(writer);
      try {
        jsonStreamWriter.beginObject()
            .name(FormatJson.D)
            .beginObject()
            .namedStringValue("EmployeeName", "Walter Winter")
            .endObject()
            .endObject();
        writer.flush();
        buffer.closeWrite();
      } catch (IOException e) {
        buffer.close();
        throw new RuntimeException(e);
      }

      ODataResponse oDataResponse =
          ODataResponse.entity(buffer.getInputStream()).status(HttpStatusCodes.OK).contentHeader("application/json")
              .build();
      return oDataResponse;
    }

    @Override
    public ODataResponse updateEntitySimpleProperty(final PutMergePatchUriInfo uriInfo, final InputStream content,
        final String requestContentType, final String contentType) throws ODataException {
      assertTrue(getContext().isInBatchMode());

      ODataResponse oDataResponse = ODataResponse.status(HttpStatusCodes.NO_CONTENT).build();
      return oDataResponse;
    }
  }

  @Override
  protected ODataSingleProcessor createProcessor() throws ODataException {
    return new TestSingleProc();
  }

  @Override
  protected ODataService createService() throws ODataException {
    final EdmProvider provider = createEdmProvider();

    final ODataSingleProcessor processor = createProcessor();

    return new ODataSingleProcessorService(provider, processor) {
      Edm edm = MockFacade.getMockEdm();

      @Override
      public Edm getEntityDataModel() throws ODataException {
        return edm;
      }
    };
  }
}
