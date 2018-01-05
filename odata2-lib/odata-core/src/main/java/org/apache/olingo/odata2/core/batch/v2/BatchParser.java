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
package org.apache.olingo.odata2.core.batch.v2;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.olingo.odata2.api.batch.BatchException;
import org.apache.olingo.odata2.api.batch.BatchParserResult;
import org.apache.olingo.odata2.api.batch.BatchRequestPart;
import org.apache.olingo.odata2.api.client.batch.BatchSingleResponse;
import org.apache.olingo.odata2.api.ep.EntityProviderBatchProperties;
import org.apache.olingo.odata2.api.uri.PathInfo;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;

public class BatchParser {

  private final PathInfo batchRequestPathInfo;
  private final String contentTypeMime;
  private final boolean isStrict;

  public BatchParser(final String contentType, final boolean isStrict) {
    this(contentType, null, isStrict);
  }

  public BatchParser(final String contentType, final EntityProviderBatchProperties properties, final boolean isStrict) {
    contentTypeMime = contentType;
    batchRequestPathInfo = (properties != null) ? properties.getPathInfo() : null;
    this.isStrict = isStrict;
  }

  @SuppressWarnings("unchecked")
  public List<BatchSingleResponse> parseBatchResponse(final InputStream in) throws BatchException {
    return (List<BatchSingleResponse>) parse(in, new BatchResponseTransformator());
  }

  @SuppressWarnings("unchecked")
  public List<BatchRequestPart> parseBatchRequest(final InputStream in) throws BatchException {
    return (List<BatchRequestPart>) parse(in, new BatchRequestTransformator());
  }

  private List<? extends BatchParserResult> parse(final InputStream in, final BatchTransformator transformator)
      throws BatchException {
    try {
      return parseBatch(in, transformator);
    } catch (IOException e) {
      throw new ODataRuntimeException(e);
    } finally {
      try {
        in.close();
      } catch (IOException e) {
        throw new ODataRuntimeException(e);
      }
    }
  }

  private List<BatchParserResult> parseBatch(final InputStream in,
      final BatchTransformator transformator) throws BatchException, IOException {

    final String baseUri = getBaseUri();
    final String boundary = BatchParserCommon.getBoundary(contentTypeMime, 1);
    final List<BatchParserResult> resultList = new LinkedList<BatchParserResult>();
    final List<List<Line>> bodyPartStrings = splitBodyParts(in, boundary);

    for (List<Line> bodyPartString : bodyPartStrings) {
      BatchBodyPart bodyPart = new BatchBodyPart(bodyPartString, boundary, isStrict).parse();
      resultList.addAll(transformator.transform(bodyPart, batchRequestPathInfo, baseUri));
    }

    return resultList;
  }
  
  private List<List<Line>> splitBodyParts(final InputStream in, final String boundary)
      throws IOException, BatchException {

    final BatchLineReader reader = new BatchLineReader(in);
    final List<Line> message = reader.toLineList();
    reader.close();

    return BatchParserCommon.splitMessageByBoundary(message, boundary);
  }


  private String getBaseUri() throws BatchException {
    String baseUri = "";

    //The service root already contains any additional path parameters
    if (batchRequestPathInfo != null && batchRequestPathInfo.getServiceRoot() != null) {
      final String uri = batchRequestPathInfo.getServiceRoot().toASCIIString();
      baseUri = removeLastSlash(uri);
    }

    return baseUri;
  }

  private String removeLastSlash(String baseUri) {
    if (baseUri.lastIndexOf('/') == baseUri.length() - 1) {
      baseUri = baseUri.substring(0, baseUri.length() - 1);
    }

    return baseUri;
  }
}
