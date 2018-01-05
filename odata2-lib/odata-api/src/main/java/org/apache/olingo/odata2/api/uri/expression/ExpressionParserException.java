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
package org.apache.olingo.odata2.api.uri.expression;

import org.apache.olingo.odata2.api.exception.MessageReference;
import org.apache.olingo.odata2.api.exception.ODataBadRequestException;

/**
 * Exception thrown while parsing a filter or orderby expression
 * 
 */
public class ExpressionParserException extends ODataBadRequestException {
  private static final long serialVersionUID = 7702L;

  public static final MessageReference COMMON_ERROR = createMessageReference(ExpressionParserException.class, "COMMON");

  // token related exception texts
  public static final MessageReference ERROR_IN_TOKENIZER = createMessageReference(ExpressionParserException.class,
      "ERROR_IN_TOKENIZER");
  public static final MessageReference TOKEN_UNDETERMINATED_STRING = createMessageReference(
      ExpressionParserException.class, "TOKEN_UNDETERMINATED_STRING");
  public static final MessageReference INVALID_TRAILING_TOKEN_DETECTED_AFTER_PARSING = createMessageReference(
      ExpressionParserException.class, "INVALID_TRAILING_TOKEN_DETECTED_AFTER_PARSING");

  // parsing
  public static final MessageReference EXPRESSION_EXPECTED_AFTER_POS = createMessageReference(
      ExpressionParserException.class, "EXPRESSION_EXPECTED_AFTER_POS");
  public static final MessageReference COMMA_OR_END_EXPECTED_AT_POS = createMessageReference(
      ExpressionParserException.class, "COMMA_OR_END_EXPECTED_AT_POS");
  public static final MessageReference EXPRESSION_EXPECTED_AT_POS = createMessageReference(
      ExpressionParserException.class, "EXPRESSION_EXPECTED_AT_POS");
  public static final MessageReference MISSING_CLOSING_PARENTHESIS = createMessageReference(
      ExpressionParserException.class, "MISSING_CLOSING_PARENTHESIS");
  public static final MessageReference COMMA_OR_CLOSING_PARENTHESIS_EXPECTED_AFTER_POS = createMessageReference(
      ExpressionParserException.class, "COMMA_OR_CLOSING_PARENTHESIS_EXPECTED_AFTER_POS");
  public static final MessageReference INVALID_METHOD_CALL = createMessageReference(ExpressionParserException.class,
      "INVALID_METHOD_CALL");
  public static final MessageReference TYPE_EXPECTED_AT = createMessageReference(ExpressionParserException.class,
      "TYPE_EXPECTED_AT");

  // validation exceptions texts - method
  public static final MessageReference METHOD_WRONG_ARG_EXACT = createMessageReference(ExpressionParserException.class,
      "METHOD_WRONG_ARG_EXACT");
  public static final MessageReference METHOD_WRONG_ARG_BETWEEN = createMessageReference(
      ExpressionParserException.class, "METHOD_WRONG_ARG_BETWEEN");
  public static final MessageReference METHOD_WRONG_ARG_X_OR_MORE = createMessageReference(
      ExpressionParserException.class, "METHOD_WRONG_ARG_X_OR_MORE");
  public static final MessageReference METHOD_WRONG_ARG_X_OR_LESS = createMessageReference(
      ExpressionParserException.class, "METHOD_WRONG_ARG_X_OR_LESS");
  public static final MessageReference METHOD_WRONG_INPUT_TYPE = createMessageReference(
      ExpressionParserException.class, "METHOD_WRONG_INPUT_TYPE");

  // validation exceptions texts - member
  public static final MessageReference LEFT_SIDE_NOT_STRUCTURAL_TYPE = createMessageReference(
      ExpressionParserException.class, "LEFT_SIDE_NOT_STRUCTURAL_TYPE");
  public static final MessageReference LEFT_SIDE_NOT_A_PROPERTY = createMessageReference(
      ExpressionParserException.class, "LEFT_SIDE_NOT_A_PROPERTY");
  public static final MessageReference PROPERTY_NAME_NOT_FOUND_IN_TYPE = createMessageReference(
      ExpressionParserException.class, "PROPERTY_NAME_NOT_FOUND_IN_TYPE");
  public static final MessageReference INVALID_MULTIPLICITY = createMessageReference(
      ExpressionParserException.class, "INVALID_MULTIPLICITY");

  // validation exceptions texts - binary
  public static final MessageReference INVALID_TYPES_FOR_BINARY_OPERATOR = createMessageReference(
      ExpressionParserException.class, "INVALID_TYPES_FOR_BINARY_OPERATOR");

  // orderby
  public static final MessageReference INVALID_SORT_ORDER = createMessageReference(ExpressionParserException.class,
      "INVALID_SORT_ORDER");

  // instance attributes
  private CommonExpression filterTree;

  // Constructors
  public ExpressionParserException() {
    super(COMMON_ERROR);
  }

  /**
   * Create {@link ExpressionParserException} with given {@link MessageReference}.
   * 
   * @param messageReference
   * references the message text (and additional values) of this {@link ExpressionParserException}
   */
  public ExpressionParserException(final MessageReference messageReference) {
    super(messageReference);
  }

  /**
   * Create {@link ExpressionParserException} with given {@link MessageReference} and cause {@link Throwable} which
   * caused
   * this {@link ExpressionParserException}.
   * 
   * @param messageReference
   * References the message text (and additional values) of this {@link ExpressionParserException}
   * @param cause
   * Exception which caused this {@link ExpressionParserException}
   */
  public ExpressionParserException(final MessageReference messageReference, final Throwable cause) {
    super(messageReference, cause);
  }

  /**
   * Gets erroneous filter expression tree for debug information.
   * @return erroneous filter tree
   */
  public CommonExpression getFilterTree() {
    return filterTree;
  }

  /**
   * Sets erroneous filter tree for debug information.
   * @param filterTree filter tree to be set
   */
  public void setFilterTree(final CommonExpression filterTree) {
    this.filterTree = filterTree;
  }
}
