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
package org.apache.olingo.odata2.api.batch;

import org.apache.olingo.odata2.api.exception.MessageReference;
import org.apache.olingo.odata2.api.exception.ODataMessageException;

public class BatchException extends ODataMessageException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /** INVALID_CHANGESET_BOUNDARY requires 1 content value ('line number') */
  public static final MessageReference INVALID_CHANGESET_BOUNDARY = createMessageReference(BatchException.class,
      "INVALID_CHANGESET_BOUNDARY");

  /** INVALID_BOUNDARY_DELIMITER requires 1 content value ('line number') */
  public static final MessageReference INVALID_BOUNDARY_DELIMITER = createMessageReference(BatchException.class,
      "INVALID_BOUNDARY_DELIMITER");

  /** MISSING_BOUNDARY_DELIMITER requires 1 content value('line number') */
  public static final MessageReference MISSING_BOUNDARY_DELIMITER = createMessageReference(BatchException.class,
      "MISSING_BOUNDARY_DELIMITER");

  /** MISSING_CLOSE_DELIMITER requires 1 content value ('line number') */
  public static final MessageReference MISSING_CLOSE_DELIMITER = createMessageReference(BatchException.class,
      "MISSING_CLOSE_DELIMITER");
  
  /** MISSONG MANDATORY HEADER requires 1 content value ('header name') */
  public static final MessageReference MISSING_MANDATORY_HEADER = createMessageReference(BatchException.class, 
      "MISSING_MANDATORY_HEADER");
  
  /** INVALID_QUERY_OPERATION_METHOD requires 1 content value ('line number') */
  public static final MessageReference INVALID_QUERY_OPERATION_METHOD = createMessageReference(BatchException.class,
      "INVALID_QUERY_OPERATION_METHOD");

  /** INVALID_CHANGESET_METHOD requires 1 content value ('line number') */
  public static final MessageReference INVALID_CHANGESET_METHOD = createMessageReference(BatchException.class,
      "INVALID_CHANGESET_METHOD");

  /** INVALID_QUERY_PARAMETER requires no content value */
  public static final MessageReference INVALID_QUERY_PARAMETER = createMessageReference(BatchException.class,
      "INVALID_QUERY_PARAMETER");

  /** INVALID_URI requires 1 content value('line number') */
  public static final MessageReference INVALID_URI = createMessageReference(BatchException.class, "INVALID_URI");

  /** INVALID_BOUNDARY requires 1 content value('line number') */
  public static final MessageReference INVALID_BOUNDARY = createMessageReference(BatchException.class,
      "INVALID_BOUNDARY");

  /** NO_MATCH_WITH_BOUNDARY_STRING requires 2 content value ('required boundary', 'line number') */
  public static final MessageReference NO_MATCH_WITH_BOUNDARY_STRING = createMessageReference(BatchException.class,
      "NO_MATCH_WITH_BOUNDARY_STRING");

  /** MISSING_CONTENT_TYPE requires no content value */
  public static final MessageReference MISSING_CONTENT_TYPE = createMessageReference(BatchException.class,
      "MISSING_CONTENT_TYPE");

  /** INVALID_CONTENT_TYPE requires 1 content value ('required content-type') */
  public static final MessageReference INVALID_CONTENT_TYPE = createMessageReference(BatchException.class,
      "INVALID_CONTENT_TYPE");

  /** MISSING_PARAMETER_IN_CONTENT_TYPE requires no content value */
  public static final MessageReference MISSING_PARAMETER_IN_CONTENT_TYPE = createMessageReference(BatchException.class,
      "MISSING_PARAMETER_IN_CONTENT_TYPE");

  /** INVALID_HEADER requires 1 content value ('header', 'line number') */
  public static final MessageReference INVALID_HEADER = createMessageReference(BatchException.class, "INVALID_HEADER");

  /** INVALID_ACCEPT_HEADER requires 1 content value ('header') */
  public static final MessageReference INVALID_ACCEPT_HEADER = createMessageReference(BatchException.class,
      "INVALID_ACCEPT_HEADER");

  /** INVALID_ACCEPT_LANGUAGE_HEADER requires 1 content value ('header') */
  public static final MessageReference INVALID_ACCEPT_LANGUAGE_HEADER = createMessageReference(BatchException.class,
      "INVALID_ACCEPT_LANGUAGE_HEADER");

  /** INVALID_CONTENT_TRANSFER_ENCODING requires 1 content value */
  public static final MessageReference INVALID_CONTENT_TRANSFER_ENCODING = createMessageReference(BatchException.class,
      "INVALID_CONTENT_TRANSFER_ENCODING");

  /** MISSING_BLANK_LINE requires 2 content value ('supplied line','line number') */
  public static final MessageReference MISSING_BLANK_LINE = createMessageReference(BatchException.class,
      "MISSING_BLANK_LINE");

  /** INVALID_PATHINFO requires no content value */
  public static final MessageReference INVALID_PATHINFO = createMessageReference(BatchException.class,
      "INVALID_PATHINFO");

  /** MISSING_METHOD requires 1 content value ('request line') */
  public static final MessageReference MISSING_METHOD = createMessageReference(BatchException.class, "MISSING_METHOD");

  /** INVALID_REQUEST_LINE requires 2 content value ('request line', 'line number') */
  public static final MessageReference INVALID_REQUEST_LINE = createMessageReference(BatchException.class,
      "INVALID_REQUEST_LINE");

  /** INVALID_BODY_FOR_REQUEST requires 1 content value ('line number') */
  public static final MessageReference INVALID_BODY_FOR_REQUEST = createMessageReference(BatchException.class,
      "INVALID_BODY_FOR_REQUEST");

  /** INVALID_STATUS_LINE requires 2 content value ('status line', 'line number') */
  public static final MessageReference INVALID_STATUS_LINE = createMessageReference(BatchException.class,
      "INVALID_STATUS_LINE");

  /** TRUNCETED_BODY requires 1 content value ('line number') */
  public static final MessageReference TRUNCATED_BODY = createMessageReference(BatchException.class, "TRUNCATED_BODY");

  /** UNSUPPORTED_ABSOLUTE_PATH requires 1 content value ('line number') */
  public static final MessageReference UNSUPPORTED_ABSOLUTE_PATH = createMessageReference(BatchException.class,
      "UNSUPPORTED_ABSOLUTE_PATH");

  public BatchException(final MessageReference messageReference) {
    super(messageReference);
  }

  public BatchException(final MessageReference messageReference, final Throwable cause) {
    super(messageReference, cause);
  }

  public BatchException(final MessageReference messageReference, final String errorCode) {
    super(messageReference, errorCode);
  }

  public BatchException(final MessageReference messageReference, final Throwable cause, final String errorCode) {
    super(messageReference, cause, errorCode);
  }

}
