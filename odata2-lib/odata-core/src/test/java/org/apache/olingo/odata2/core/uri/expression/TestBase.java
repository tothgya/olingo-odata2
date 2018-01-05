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
package org.apache.olingo.odata2.core.uri.expression;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.rt.RuntimeDelegate;
import org.apache.olingo.odata2.testutil.mock.TecEdmInfo;
import org.apache.olingo.odata2.testutil.mock.TechnicalScenarioEdmProvider;

/**
 *  
 */
public class TestBase {

  protected TecEdmInfo edmInfo = null;

  public TestBase() {
    final Edm edm = RuntimeDelegate.createEdm(new TechnicalScenarioEdmProvider());
    edmInfo = new TecEdmInfo(edm);
  }

  static public ParserTool GetPTF(final String expression) {
    return new ParserTool(expression, false, true, false, null);
  }

  static public ParserTool GetPTF_onlyBinary(final String expression) {
    return new ParserTool(expression, false, true, true, null);
  }

  static public ParserTool GetPTFE(final String expression) {
    return new ParserTool(expression, false, true, false, null);
  }

  static public ParserTool GetPTF(final EdmEntityType resourceEntityType, final String expression) {
    return new ParserTool(expression, false, true, false, resourceEntityType);
  }

  static public ParserTool GetPTO(final String expression) {
    return new ParserTool(expression, true, true, false, null);
  }

  static public ParserTool GetPTO(final EdmEntityType resourceEntityType, final String expression) {
    return new ParserTool(expression, true, true, false, resourceEntityType);
  }

  static public ParserTool GetPTF_noTEST(final String expression) {
    return new ParserTool(expression, false, false, false, null);
  }

  static public ParserTool GetPTF_noTEST(final EdmEntityType resourceEntityType, final String expression) {
    return new ParserTool(expression, false, false, true, resourceEntityType);
  }

  static public ParserTool GetPTO_noTEST(final String expression) {
    return new ParserTool(expression, true, false, true, null);
  }

  static public ParserTool GetPTO_noTEST(final EdmEntityType resourceEntityType, final String expression) {
    return new ParserTool(expression, true, false, true, resourceEntityType);
  }

}
