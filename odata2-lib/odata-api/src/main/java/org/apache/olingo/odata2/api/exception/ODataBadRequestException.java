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
package org.apache.olingo.odata2.api.exception;

import org.apache.olingo.odata2.api.commons.HttpStatusCodes;

/**
 * Exceptions of this class will result in a HTTP status 400 bad request
 * 
 */
public class ODataBadRequestException extends ODataHttpException {

  private static final long serialVersionUID = 1L;

  public static final MessageReference COMMON = createMessageReference(ODataBadRequestException.class, "COMMON");
  public static final MessageReference NOTSUPPORTED = createMessageReference(ODataBadRequestException.class,
      "NOTSUPPORTED");
  public static final MessageReference URLTOOSHORT = createMessageReference(ODataBadRequestException.class,
      "URLTOOSHORT");
  public static final MessageReference VERSIONERROR = createMessageReference(ODataBadRequestException.class,
      "VERSIONERROR");
  public static final MessageReference PARSEVERSIONERROR = createMessageReference(ODataBadRequestException.class,
      "PARSEVERSIONERROR");
  public static final MessageReference BODY = createMessageReference(ODataBadRequestException.class, "BODY");
  public static final MessageReference AMBIGUOUS_XMETHOD = createMessageReference(ODataBadRequestException.class,
      "AMBIGUOUS_XMETHOD");
  /** INVALID_HEADER requires 2 content values ('header key' and 'header value') */
  public static final MessageReference INVALID_HEADER = createMessageReference(ODataBadRequestException.class,
      "INVALID_HEADER");
  /** INVALID_SYNTAX requires NO content values */
  public static final MessageReference INVALID_SYNTAX = createMessageReference(ODataBadRequestException.class,
      "INVALID_SYNTAX");
  /** INVALID_REQUEST requires NO content values */
  public static final MessageReference INVALID_REQUEST = createMessageReference(ODataBadRequestException.class,
      "INVALID_REQUEST");

  public ODataBadRequestException(final MessageReference messageReference) {
    super(messageReference, HttpStatusCodes.BAD_REQUEST);
  }

  public ODataBadRequestException(final MessageReference messageReference, final String errorCode) {
    super(messageReference, HttpStatusCodes.BAD_REQUEST, errorCode);
  }

  public ODataBadRequestException(final MessageReference messageReference, final Throwable cause) {
    super(messageReference, cause, HttpStatusCodes.BAD_REQUEST);
  }

  public ODataBadRequestException(final MessageReference messageReference, final Throwable cause,
      final String errorCode) {
    super(messageReference, cause, HttpStatusCodes.BAD_REQUEST, errorCode);
  }
}
