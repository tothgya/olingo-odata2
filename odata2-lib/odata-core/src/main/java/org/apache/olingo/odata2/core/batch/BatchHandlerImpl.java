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
package org.apache.olingo.odata2.core.batch;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.batch.BatchHandler;
import org.apache.olingo.odata2.api.batch.BatchRequestPart;
import org.apache.olingo.odata2.api.batch.BatchResponsePart;
import org.apache.olingo.odata2.api.commons.HttpHeaders;
import org.apache.olingo.odata2.api.commons.ODataHttpMethod;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataRequest;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.uri.PathSegment;
import org.apache.olingo.odata2.core.ODataContextImpl;
import org.apache.olingo.odata2.core.ODataPathSegmentImpl;
import org.apache.olingo.odata2.core.ODataRequestHandler;
import org.apache.olingo.odata2.core.PathInfoImpl;

public class BatchHandlerImpl implements BatchHandler {
  private static final int BAD_REQUEST = 400;
  private ODataServiceFactory factory;
  private ODataService service;
  private Map<String, String> contentIdMap;
  private static final String BATCH_ODATA_REQUEST_HEADERS = "batchODataRequestHeaders";

  public BatchHandlerImpl(final ODataServiceFactory factory, final ODataService service) {
    this.factory = factory;
    this.service = service;
  }

  @Override
  public BatchResponsePart handleBatchPart(final BatchRequestPart batchPart) throws ODataException {
    if (batchPart.isChangeSet()) {
      List<ODataRequest> changeSetRequests = batchPart.getRequests();
      contentIdMap = new HashMap<String, String>();
      return service.getBatchProcessor().executeChangeSet(this, changeSetRequests);
    } else {
      if (batchPart.getRequests().size() != 1) {
        throw new ODataException("Query Operation should contain one request");
      }
      ODataRequest request = batchPart.getRequests().get(0);
      ODataRequestHandler handler = createHandler(request);
      String mimeHeaderContentId =
          request.getRequestHeaderValue(BatchHelper.MIME_HEADER_CONTENT_ID.toLowerCase(Locale.ENGLISH));
      String requestHeaderContentId =
          request.getRequestHeaderValue(BatchHelper.REQUEST_HEADER_CONTENT_ID.toLowerCase(Locale.ENGLISH));
      ODataResponse response = setContentIdHeader(handler.handle(request), mimeHeaderContentId, requestHeaderContentId);
      List<ODataResponse> responses = new ArrayList<ODataResponse>(1);
      responses.add(response);
      return BatchResponsePart.responses(responses).changeSet(false).build();
    }
  }

  @Override
  public ODataResponse handleRequest(final ODataRequest suppliedRequest) throws ODataException {
    ODataRequest request;
    String mimeHeaderContentId =
        suppliedRequest.getRequestHeaderValue(BatchHelper.MIME_HEADER_CONTENT_ID.toLowerCase(Locale.ENGLISH));
    String requestHeaderContentId =
        suppliedRequest.getRequestHeaderValue(BatchHelper.REQUEST_HEADER_CONTENT_ID.toLowerCase(Locale.ENGLISH));

    List<PathSegment> odataSegments = suppliedRequest.getPathInfo().getODataSegments();
    if (!odataSegments.isEmpty() && odataSegments.get(0).getPath().matches("\\$.*")) {
      request = modifyRequest(suppliedRequest, odataSegments);
    } else {
      request = suppliedRequest;
    }
    ODataRequestHandler handler = createHandler(request);
    ODataResponse response = handler.handle(request);
    if (response.getStatus().getStatusCode() < BAD_REQUEST) {
      response = setContentIdHeader(response, mimeHeaderContentId, requestHeaderContentId);
    }
    if (request.getMethod().equals(ODataHttpMethod.POST)) {
      String baseUri = getBaseUri(request);
      if (mimeHeaderContentId != null) {
        fillContentIdMap(response, mimeHeaderContentId, baseUri);
      } else if (requestHeaderContentId != null) {
        fillContentIdMap(response, requestHeaderContentId, baseUri);
      }
    }
    return response;
  }

  private void fillContentIdMap(final ODataResponse response, final String contentId, final String baseUri) {
    String location = response.getHeader(HttpHeaders.LOCATION);
    if (location != null) {
      String relLocation = location.replace(baseUri + "/", "");
      contentIdMap.put("$" + contentId, relLocation);
    }
  }

  private ODataRequest modifyRequest(final ODataRequest request, final List<PathSegment> odataSegments)
      throws ODataException {
    String contentId = contentIdMap.get(odataSegments.get(0).getPath());
    if (contentId == null) {
      // invalid content ID. But throwing an exception here is wrong so we use the base request and fail later
      return request;
    }
    PathInfoImpl pathInfo = new PathInfoImpl();
    try {
      List<PathSegment> modifiedODataSegments = new ArrayList<PathSegment>();
      String[] segments = contentId.split("/");
      for (String segment : segments) {
        modifiedODataSegments.add(new ODataPathSegmentImpl(segment, null));
      }
      String newRequestUri = getBaseUri(request);
      newRequestUri += "/" + contentId;
      for (int i = 1; i < odataSegments.size(); i++) {
        newRequestUri += "/" + odataSegments.get(i).getPath();
        modifiedODataSegments.add(odataSegments.get(i));
      }
      for (Map.Entry<String, String> entry : request.getQueryParameters().entrySet()) {
        newRequestUri += "/" + entry;
      }

      pathInfo.setServiceRoot(request.getPathInfo().getServiceRoot());
      pathInfo.setPrecedingPathSegment(request.getPathInfo().getPrecedingSegments());
      pathInfo.setRequestUri(new URI(newRequestUri));
      pathInfo.setODataPathSegment(modifiedODataSegments);
    } catch (URISyntaxException e) {
      throw new ODataException(e);
    }
    ODataRequest modifiedRequest = ODataRequest.fromRequest(request).pathInfo(pathInfo).build();
    return modifiedRequest;
  }

  private ODataResponse setContentIdHeader(final ODataResponse response, final String mimeHeaderContentId,
      final String requestHeaderContentId) {
    ODataResponse modifiedResponse;
    if (requestHeaderContentId != null && mimeHeaderContentId != null) {
      modifiedResponse =
          ODataResponse.fromResponse(response).header(BatchHelper.REQUEST_HEADER_CONTENT_ID, requestHeaderContentId)
              .header(BatchHelper.MIME_HEADER_CONTENT_ID, mimeHeaderContentId).build();
    } else if (requestHeaderContentId != null) {
      modifiedResponse =
          ODataResponse.fromResponse(response).header(BatchHelper.REQUEST_HEADER_CONTENT_ID, requestHeaderContentId)
              .build();
    } else if (mimeHeaderContentId != null) {
      modifiedResponse =
          ODataResponse.fromResponse(response).header(BatchHelper.MIME_HEADER_CONTENT_ID, mimeHeaderContentId).build();
    } else {
      return response;
    }
    return modifiedResponse;
  }

  private String getBaseUri(final ODataRequest request) {
    // The service root already contains any additional path parameters
    String baseUri = request.getPathInfo().getServiceRoot().toASCIIString();
    if (baseUri.endsWith("/")) {
      baseUri = baseUri.substring(0, baseUri.length() - 1);
    }
    return baseUri;
  }

  private ODataRequestHandler createHandler(final ODataRequest request) throws ODataException {
    ODataContextImpl context = new ODataContextImpl(request, factory);
    ODataContext parentContext = service.getProcessor().getContext();
    context.setBatchParentContext(parentContext);
    context.setService(service);
    if (parentContext != null && parentContext.getParameter(BATCH_ODATA_REQUEST_HEADERS) != null) {
      context.setParameter(BATCH_ODATA_REQUEST_HEADERS, parentContext.getParameter(BATCH_ODATA_REQUEST_HEADERS));
    } else if (parentContext != null && parentContext.getRequestHeaders() != null) {
      context.setParameter(BATCH_ODATA_REQUEST_HEADERS, parentContext.getRequestHeaders());
    }
    service.getProcessor().setContext(context);
    return new ODataRequestHandler(factory, service, context);
  }

}
