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
package org.apache.olingo.odata2.core.edm;

import org.apache.olingo.odata2.api.edm.EdmFacets;
import org.apache.olingo.odata2.api.edm.EdmLiteralKind;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeException;
import org.apache.olingo.odata2.core.commons.Base64;
import org.apache.olingo.odata2.core.commons.Hex;

import java.io.IOException;

/**
 * Implementation of the EDM simple type Binary.
 * 
 */
public class EdmBinary extends AbstractSimpleType {

  private static final EdmBinary instance = new EdmBinary();

  public static EdmBinary getInstance() {
    return instance;
  }

  @Override
  public Class<?> getDefaultType() {
    return byte[].class;
  }

  @Override
  public boolean validate(final String value, final EdmLiteralKind literalKind, final EdmFacets facets) {
    if (value == null) {
      return facets == null || facets.isNullable() == null || facets.isNullable();
    }

    if (literalKind == null) {
      return false;
    }

    return validateLiteral(value, literalKind) && validateMaxLength(value, literalKind, facets);
  }

  private static boolean validateLiteral(final String value, final EdmLiteralKind literalKind) {
    return literalKind == EdmLiteralKind.URI ?
        value.matches("(?:X|binary)'(?:\\p{XDigit}{2})*'") : Base64.isBase64(value);
  }

  private static boolean
      validateMaxLength(final String value, final EdmLiteralKind literalKind, final EdmFacets facets) {
    return facets == null || facets.getMaxLength() == null ? true :
        literalKind == EdmLiteralKind.URI ?
            // In URI representation, each byte is represented as two hexadecimal digits;
            // additionally, we have to account for the prefix and the surrounding "'"s.
            facets.getMaxLength() >= (value.length() - (value.startsWith("X") ? 3 : 8)) / 2
            :
            // In default representation, every three bytes are represented as four base-64 characters.
            // Additionally, there could be up to two padding "=" characters if the number of bytes is
            // not a multiple of three, and there could be carriage return/line feed combinations.
            facets.getMaxLength() >= value.length() * 3 / 4 - (value.endsWith("==") ? 2 : value.endsWith("=") ? 1 : 0)
                - crlfLength(value);
  }

  private static int crlfLength(final String value) {
    int result = 0;
    int index = 0;
    while (index >= 0) {
      index = value.indexOf("\r\n", index);
      if (index > 0) {
        result++;
        index++;
      }
    }
    return result * 2;
  }

  @Override
  protected <T> T internalValueOfString(final String value, final EdmLiteralKind literalKind, final EdmFacets facets,
      final Class<T> returnType) throws EdmSimpleTypeException {
    if (!validateLiteral(value, literalKind)) {
      throw new EdmSimpleTypeException(EdmSimpleTypeException.LITERAL_ILLEGAL_CONTENT.addContent(value));
    }
    if (!validateMaxLength(value, literalKind, facets)) {
      throw new EdmSimpleTypeException(EdmSimpleTypeException.LITERAL_FACETS_NOT_MATCHED.addContent(value, facets));
    }

    byte[] result;
    if (literalKind == EdmLiteralKind.URI) {
      try {
        result = Hex.decodeHex(value.substring(value.startsWith("X") ? 2 : 7, value.length() - 1).toCharArray());
      } catch (final IOException e) {
        throw new EdmSimpleTypeException(EdmSimpleTypeException.LITERAL_ILLEGAL_CONTENT.addContent(value), e);
      }
    } else {
      result = Base64.decodeBase64(value);
    }

    if (returnType.isAssignableFrom(byte[].class)) {
      return returnType.cast(result);
    } else if (returnType.isAssignableFrom(Byte[].class)) {
      Byte[] byteArray = new Byte[result.length];
      for (int i = 0; i < result.length; i++) {
        byteArray[i] = result[i];
      }
      return returnType.cast(byteArray);
    } else {
      throw new EdmSimpleTypeException(EdmSimpleTypeException.VALUE_TYPE_NOT_SUPPORTED.addContent(returnType));
    }
  }

  @Override
  protected <T> String internalValueToString(final T value, final EdmLiteralKind literalKind, final EdmFacets facets)
      throws EdmSimpleTypeException {
    byte[] byteArrayValue;
    if (value instanceof byte[]) {
      byteArrayValue = (byte[]) value;
    } else if (value instanceof Byte[]) {
      final int length = ((Byte[]) value).length;
      byteArrayValue = new byte[length];
      for (int i = 0; i < length; i++) {
        byteArrayValue[i] = ((Byte[]) value)[i].byteValue();
      }
    } else {
      throw new EdmSimpleTypeException(EdmSimpleTypeException.VALUE_TYPE_NOT_SUPPORTED.addContent(value.getClass()));
    }

    if (facets != null && facets.getMaxLength() != null && byteArrayValue.length > facets.getMaxLength()) {
      throw new EdmSimpleTypeException(EdmSimpleTypeException.VALUE_FACETS_NOT_MATCHED.addContent(value, facets));
    }

    return Base64.encodeBase64String(byteArrayValue);
  }

  @Override
  public String toUriLiteral(final String literal) throws EdmSimpleTypeException {
    return "binary'" + String.valueOf(Hex.encodeHex(Base64.decodeBase64(literal), false)) + "'";
  }
}
