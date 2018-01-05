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

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.uri.ExpandSelectTreeNode;
import org.apache.olingo.odata2.core.commons.XmlHelper;
import org.apache.olingo.odata2.core.ep.aggregator.EntityInfoAggregator;
import org.apache.olingo.odata2.core.ep.producer.AtomEntryEntityProducer;
import org.apache.olingo.odata2.core.ep.util.CircleStreamBuffer;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 *  
 */
public class PerformanceTest extends AbstractProviderTest {

  public PerformanceTest(final StreamWriterImplType type) {
    super(type);
  }

  private static final long TIMES = 100L; // increase for manual performance testing (also increase vm memory -Xmx1G)

  private AtomEntryEntityProducer provider;
  private EdmEntitySet edmEntitySet;
  private OutputStream outStream = null;
  private CircleStreamBuffer csb;
  private XMLStreamWriter writer;

  private boolean useCsb = true;

  @Before
  public void before() throws Exception {
    EntityProviderWriteProperties properties = EntityProviderWriteProperties.serviceRoot(BASE_URI).build();
    provider = new AtomEntryEntityProducer(properties);
    edmEntitySet = MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms");

    if (useCsb) {
      csb = new CircleStreamBuffer();
      outStream = csb.getOutputStream();
    } else {
      outStream = new ByteArrayOutputStream();
    }

    writer = XmlHelper.getXMLOutputFactory().createXMLStreamWriter(outStream, "utf-8");

    writer.writeStartElement("junit");
    writer.writeDefaultNamespace(Edm.NAMESPACE_ATOM_2005);
    writer.writeNamespace(Edm.PREFIX_M, Edm.NAMESPACE_M_2007_08);
    writer.writeNamespace(Edm.PREFIX_D, Edm.NAMESPACE_D_2007_08);
    writer.writeAttribute(Edm.PREFIX_XML, Edm.NAMESPACE_XML_1998, "base", "xxx");
  }

  @After
  public void after() throws Exception {
    writer.writeEndElement();
    writer.flush();
    outStream.flush();

    if (useCsb) {
      String content = StringHelper.inputStreamToString(csb.getInputStream());
      assertNotNull(content);
    } else {
      InputStream in = new ByteArrayInputStream(((ByteArrayOutputStream) outStream).toByteArray());
      String content = StringHelper.inputStreamToString(in);
      assertNotNull(content);
    }

    try {

    } finally {
      outStream.close();
    }
  }

  @Test
  public void readAtomEntry() throws IOException, XpathException, SAXException, XMLStreamException,
      FactoryConfigurationError, ODataException {
    long t = startTimer();

    for (int i = 0; i < TIMES; i++) {
      ExpandSelectTreeNode epProperties = null;
      EntityInfoAggregator eia = EntityInfoAggregator.create(edmEntitySet, epProperties);
      provider.append(writer, eia, roomData, false, false);
    }
    stopTimer(t, "readAtomEntry");
  }

  @Test
  public void readAtomEntryCsb() throws Exception {
    useCsb = true;
    before();
    assertNotNull(csb);

    long t = startTimer();

    for (int i = 0; i < TIMES; i++) {
      ExpandSelectTreeNode epProperties = null;
      EntityInfoAggregator eia = EntityInfoAggregator.create(edmEntitySet, epProperties);
      provider.append(writer, eia, roomData, false, false);
    }
    stopTimer(t, "readAtomEntry");
  }

  @Test
  public void readAtomEntryOptimized() throws IOException, XpathException, SAXException, XMLStreamException,
      FactoryConfigurationError, ODataException {
    long t = startTimer();

    ExpandSelectTreeNode epProperties = null;
    EntityInfoAggregator eia = EntityInfoAggregator.create(edmEntitySet, epProperties);
    for (int i = 0; i < TIMES; i++) {
      provider.append(writer, eia, roomData, false, false);
    }
    stopTimer(t, "readAtomEntryOptimized");
  }

  @Test
  public void readAtomEntryOptimizedCsb() throws Exception {
    useCsb = true;
    before();
    assertNotNull(csb);

    long t = startTimer();

    ExpandSelectTreeNode epProperties = null;
    EntityInfoAggregator eia = EntityInfoAggregator.create(edmEntitySet, epProperties);
    for (int i = 0; i < TIMES; i++) {
      provider.append(writer, eia, roomData, false, false);
    }
    stopTimer(t, "readAtomEntryOptimizedCsb");
  }

  private void stopTimer(long t, final String msg) {
    t = (System.nanoTime() - t) / TIMES;

    long millis = t / (1000L * 1000L);
    long micros = t % (1000L * 1000L);

    long sum = (t * TIMES) / (1000L * 1000L);

    log.debug(msg + ": " + millis + "." + micros / 1000L + "[ms] (" + TIMES + " in " + sum + " [ms])");
  }

  private long startTimer() {
    long t = System.nanoTime();
    return t;
  }

}
