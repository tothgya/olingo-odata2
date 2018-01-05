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
package org.apache.olingo.odata2.jpa.processor.api.jpql;

import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.factory.ODataJPAFactory;

/**
 * The class represents a Java Persistence Query Language (JPQL) Statement.
 * The JPQL statement is built using a builder namely
 * {@link org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLStatement.JPQLStatementBuilder} . Based upon the JPQL
 * Context types ( {@link org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLContextType} different
 * kinds of JPQL statements are built.
 * The JPQL statements thus generated can be executed using JPA Query APIs to fetch JPA entities.
 * 
 * 
 * @see org.apache.olingo.odata2.jpa.processor.api.factory.JPQLBuilderFactory
 * @see org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLContextView
 */
public class JPQLStatement {

  protected String statement;

  /**
   * The method is used for creating an instance of JPQL Statement Builder for
   * building JPQL statements. The JPQL Statement builder is created based
   * upon the JPQL Context.
   * 
   * @param context
   * a non null value of {@link org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLContextView} . The context is
   * expected to be set to be built with no
   * errors.
   * @return an instance of JPQL statement builder
   * @throws ODataJPARuntimeException
   */
  public static JPQLStatementBuilder createBuilder(final JPQLContextView context) throws ODataJPARuntimeException {
    return JPQLStatementBuilder.create(context);
  }

  private JPQLStatement(final String statement) {
    this.statement = statement;
  }

  /**
   * The method provides a String representation of JPQLStatement.
   */
  @Override
  public String toString() {
    return statement;
  }

  /**
   * The abstract class is extended by specific JPQL statement builders for
   * building JPQL statements like
   * <ol>
   * <li>Select statements</li>
   * <li>Select single statements</li>
   * <li>Select statements with Join</li>
   * <li>Insert/Modify/Delete statements</li>
   * </ol>
   * 
   * A default statement builder for building each kind of JPQL statements is
   * provided by the library.
   * 
   * 
   * 
   */
  public static abstract class JPQLStatementBuilder {

    protected JPQLStatementBuilder() {}

    private static final JPQLStatementBuilder create(final JPQLContextView context) throws ODataJPARuntimeException {
      return ODataJPAFactory.createFactory().getJPQLBuilderFactory().getStatementBuilder(context);
    }

    protected final JPQLStatement createStatement(final String statement) {
      return new JPQLStatement(statement);
    }

    /**
     * The abstract method is implemented by specific statement builder for
     * building JPQL Statement.
     * 
     * @return an instance of {@link org.apache.olingo.odata2.jpa.processor.api.jpql.JPQLStatement}
     * @throws ODataJPARuntimeException
     * in case there are errors building the statements
     */
    public abstract JPQLStatement build() throws ODataJPARuntimeException;

  }

  public static final class Operator {
    public static final String EQ = "=";
    public static final String NE = "<>";
    public static final String LT = "<";
    public static final String LE = "<=";
    public static final String GT = ">";
    public static final String GE = ">=";
    public static final String AND = "AND";
    public static final String NOT = "NOT";
    public static final String OR = "OR";

  }

  public static final class KEYWORD {
    public static final String SELECT = "SELECT";
    public static final String SELECT_DISTINCT = "SELECT DISTINCT";
    public static final String FROM = "FROM";
    public static final String WHERE = "WHERE";
    public static final String LEFT_OUTER_JOIN = "LEFT OUTER JOIN";
    public static final String OUTER = "OUTER";
    public static final String JOIN = "JOIN";
    public static final String ORDERBY = "ORDER BY";
    public static final String COUNT = "COUNT";
    public static final String OFFSET = ".000";
    public static final String TIMESTAMP = "ts";

  }

  public static final class DELIMITER {
    public static final char SPACE = ' ';
    public static final char COMMA = ',';
    public static final char PERIOD = '.';
    public static final char PARENTHESIS_LEFT = '(';
    public static final char PARENTHESIS_RIGHT = ')';
    public static final char COLON = ':';
    public static final char HYPHEN = '-';
    public static final char LEFT_BRACE = '{';
    public static final char RIGHT_BRACE = '}';
    public static final char LONG = 'L';
  }

}
