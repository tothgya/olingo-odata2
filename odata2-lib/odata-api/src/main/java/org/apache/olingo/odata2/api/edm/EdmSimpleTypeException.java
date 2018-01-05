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
package org.apache.olingo.odata2.api.edm;

import org.apache.olingo.odata2.api.exception.MessageReference;

/**
 * @org.apache.olingo.odata2.DoNotImplement
 * Exception for parsing errors with {@link EdmSimpleType}s
 * 
 */
public class EdmSimpleTypeException extends EdmException {

  private static final long serialVersionUID = 1L;

  public static final MessageReference COMMON = createMessageReference(EdmSimpleTypeException.class, "COMMON");

  public static final MessageReference LITERAL_KIND_MISSING = createMessageReference(EdmSimpleTypeException.class,
      "LITERAL_KIND_MISSING");
  public static final MessageReference LITERAL_KIND_NOT_SUPPORTED = createMessageReference(
      EdmSimpleTypeException.class, "LITERAL_KIND_NOT_SUPPORTED");

  public static final MessageReference LITERAL_NULL_NOT_ALLOWED = createMessageReference(EdmSimpleTypeException.class,
      "LITERAL_NULL_NOT_ALLOWED");
  public static final MessageReference LITERAL_ILLEGAL_CONTENT = createMessageReference(EdmSimpleTypeException.class,
      "LITERAL_ILLEGAL_CONTENT");
  public static final MessageReference LITERAL_FACETS_NOT_MATCHED = createMessageReference(
      EdmSimpleTypeException.class, "LITERAL_FACETS_NOT_MATCHED");
  public static final MessageReference LITERAL_UNCONVERTIBLE_TO_VALUE_TYPE = createMessageReference(
      EdmSimpleTypeException.class, "LITERAL_UNCONVERTIBLE_TO_VALUE_TYPE");

  public static final MessageReference VALUE_TYPE_NOT_SUPPORTED = createMessageReference(EdmSimpleTypeException.class,
      "VALUE_TYPE_NOT_SUPPORTED");
  public static final MessageReference VALUE_NULL_NOT_ALLOWED = createMessageReference(EdmSimpleTypeException.class,
      "VALUE_NULL_NOT_ALLOWED");
  public static final MessageReference VALUE_ILLEGAL_CONTENT = createMessageReference(EdmSimpleTypeException.class,
      "VALUE_ILLEGAL_CONTENT");
  public static final MessageReference VALUE_FACETS_NOT_MATCHED = createMessageReference(EdmSimpleTypeException.class,
      "VALUE_FACETS_NOT_MATCHED");
  public static final MessageReference PROPERTY_VALUE_NULL_NOT_ALLOWED = createMessageReference(
      EdmSimpleTypeException.class, "PROPERTY_VALUE_NULL_NOT_ALLOWED");
  public static final MessageReference PROPERTY_VALUE_FACETS_NOT_MATCHED = createMessageReference(
      EdmSimpleTypeException.class,"PROPERTY_VALUE_FACETS_NOT_MATCHED");

  public EdmSimpleTypeException(final MessageReference messageReference) {
    super(messageReference);
  }

  public EdmSimpleTypeException(final MessageReference messageReference, final Throwable cause) {
    super(messageReference, cause);
  }

  public EdmSimpleTypeException(final MessageReference messageReference, final String errorCode) {
    super(messageReference, errorCode);
  }

  public EdmSimpleTypeException(final MessageReference messageReference, final Throwable cause,
      final String errorCode) {
    super(messageReference, cause, errorCode);
  }
  
  public static MessageReference getMessageReference(final MessageReference messageReference) {
    if (EdmSimpleTypeException.VALUE_NULL_NOT_ALLOWED.equals(messageReference)) {
      return EdmSimpleTypeException.PROPERTY_VALUE_NULL_NOT_ALLOWED;
    } else if (EdmSimpleTypeException.VALUE_FACETS_NOT_MATCHED.equals(messageReference)) {
      return EdmSimpleTypeException.PROPERTY_VALUE_FACETS_NOT_MATCHED;
    }
    return messageReference;
  }
}
