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
package org.apache.olingo.odata2.core.ep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.xml.stream.XMLOutputFactory;

import org.junit.Test;

/**
 *
 */
public class LoadXMLFactoryTest {
  // CHECKSTYLE:OFF
  @Test
  public void loadWoodstockFactory() throws Exception {
    System.setProperty("javax.xml.stream.XMLOutputFactory", "com.ctc.wstx.stax.WstxOutputFactory"); // NOSONAR
    XMLOutputFactory factory = XMLOutputFactory.newInstance();
    assertNotNull(factory);
    assertEquals("com.ctc.wstx.stax.WstxOutputFactory", factory.getClass().getName());
  }

  @Test
  public void loadSunFactory() throws Exception {
    System.setProperty("javax.xml.stream.XMLOutputFactory", "com.sun.xml.internal.stream.XMLOutputFactoryImpl"); // NOSONAR
    XMLOutputFactory factory = XMLOutputFactory.newInstance();
    assertNotNull(factory);
    assertEquals("com.sun.xml.internal.stream.XMLOutputFactoryImpl", factory.getClass().getName());
  }
  // CHECKSTYLE:ON
}
