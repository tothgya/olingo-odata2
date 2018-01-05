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
package org.apache.olingo.odata2.core.ep.producer;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathNotExists;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.callback.TombstoneCallback;
import org.apache.olingo.odata2.core.commons.ContentType;
import org.apache.olingo.odata2.core.commons.XmlHelper;
import org.apache.olingo.odata2.core.ep.AbstractProviderTest;
import org.apache.olingo.odata2.core.ep.aggregator.EntityInfoAggregator;
import org.apache.olingo.odata2.core.ep.util.CircleStreamBuffer;
import org.apache.olingo.odata2.testutil.helper.StringHelper;
import org.apache.olingo.odata2.testutil.mock.MockFacade;
import org.junit.Before;
import org.junit.Test;

public class TombstoneProducerTest extends AbstractProviderTest {

  private static final String DEFAULT_CHARSET = ContentType.CHARSET_UTF_8;
  private static final String XML_VERSION = "1.0";
  private XMLStreamWriter writer;
  private EntityProviderWriteProperties defaultProperties;
  private EntityInfoAggregator defaultEia;
  private CircleStreamBuffer csb;

  public TombstoneProducerTest(final StreamWriterImplType type) {
    super(type);
  }

  @Before
  public void initialize() throws Exception {
    csb = new CircleStreamBuffer();
    OutputStream outStream = csb.getOutputStream();
    writer = XmlHelper.getXMLOutputFactory().createXMLStreamWriter(outStream, DEFAULT_CHARSET);
    defaultProperties = EntityProviderWriteProperties.serviceRoot(BASE_URI).build();
    defaultEia =
        EntityInfoAggregator.create(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Rooms"),
            defaultProperties.getExpandSelectTree());
  }

  @Test
  public void oneDeletedEntryWithAllProperties() throws Exception {
    // Prepare Data
    List<Map<String, Object>> deletedEntries = new ArrayList<Map<String, Object>>();
    Map<String, Object> data = new HashMap<String, Object>();
    data.put("Id", "1");
    data.put("Name", "Neu Schwanstein");
    data.put("Seats", new Integer(20));
    data.put("Version", new Integer(3));
    deletedEntries.add(data);
    // Execute producer
    execute(deletedEntries);
    // Verify
    String xml = getXML();
    assertXpathExists("/a:feed/at:deleted-entry[@ref and @when]", xml);
    assertXpathEvaluatesTo("http://host:80/service/Rooms('1')", "/a:feed/at:deleted-entry/@ref", xml);
  }

  @Test
  public void twoDeletedEntriesWithAllProperties() throws Exception {
    // Prepare Data
    List<Map<String, Object>> deletedEntries = new ArrayList<Map<String, Object>>();
    Map<String, Object> data = new HashMap<String, Object>();
    data.put("Id", "1");
    data.put("Name", "Neu Schwanstein");
    data.put("Seats", new Integer(20));
    data.put("Version", new Integer(3));
    deletedEntries.add(data);

    Map<String, Object> data2 = new HashMap<String, Object>();
    data2.put("Id", "2");
    data2.put("Name", "Neu Schwanstein");
    data2.put("Seats", new Integer(20));
    data2.put("Version", new Integer(3));
    deletedEntries.add(data2);
    // Execute producer
    execute(deletedEntries);
    // Verify
    String xml = getXML();
    assertXpathExists("/a:feed/at:deleted-entry[@ref and @when]", xml);
    assertXpathExists("/a:feed/at:deleted-entry[@ref=\"http://host:80/service/Rooms('1')\"]", xml);
    assertXpathExists("/a:feed/at:deleted-entry[@ref=\"http://host:80/service/Rooms('2')\"]", xml);
  }

  @Test
  public void oneDeletedEntryWithKeyProperties() throws Exception {
    // Prepare Data
    List<Map<String, Object>> deletedEntries = new ArrayList<Map<String, Object>>();
    Map<String, Object> data = new HashMap<String, Object>();
    data.put("Id", "1");
    deletedEntries.add(data);
    // Execute producer
    execute(deletedEntries);
    // Verify
    String xml = getXML();
    assertXpathExists("/a:feed/at:deleted-entry[@ref and @when]", xml);
    assertXpathEvaluatesTo("http://host:80/service/Rooms('1')", "/a:feed/at:deleted-entry/@ref", xml);
  }

  @Test(expected = EntityProviderException.class)
  public void oneDeletedEntryWithoutProperties() throws Exception {
    // Prepare Data
    List<Map<String, Object>> deletedEntries = new ArrayList<Map<String, Object>>();
    Map<String, Object> data = new HashMap<String, Object>();
    deletedEntries.add(data);
    // Execute producer
    execute(deletedEntries);
  }

  @Test
  public void emptyEntryList() throws Exception {
    // Prepare Data
    List<Map<String, Object>> deletedEntries = new ArrayList<Map<String, Object>>();
    // Execute producer
    execute(deletedEntries);
    // Verify
    String xml = getXML();
    assertXpathExists("/a:feed", xml);
    assertXpathNotExists("/a:feed/at:deleted-entry[@ref and @when]", xml);
  }

  @Test
  public void entryWithSyndicatedUpdatedMappingPresent() throws Exception {
    // Prepare Data
    List<Map<String, Object>> deletedEntries = new ArrayList<Map<String, Object>>();
    deletedEntries.add(employeeData);
    defaultEia =
        EntityInfoAggregator.create(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees"),
            defaultProperties.getExpandSelectTree());
    // Execute producer
    execute(deletedEntries);
    // Verify
    String xml = getXML();
    assertXpathExists("/a:feed/at:deleted-entry[@ref and @when]", xml);
    assertXpathEvaluatesTo("http://host:80/service/Employees('1')", "/a:feed/at:deleted-entry/@ref", xml);
    assertXpathEvaluatesTo("1999-01-01T00:00:00Z", "/a:feed/at:deleted-entry/@when", xml);
  }

  @Test
  public void entryWithSyndicatedUpdatedMappingNotPresent() throws Exception {
    // Prepare Data
    List<Map<String, Object>> deletedEntries = new ArrayList<Map<String, Object>>();
    Map<String, Object> data = new HashMap<String, Object>();
    data.put("EmployeeId", "1");
    deletedEntries.add(data);
    defaultEia =
        EntityInfoAggregator.create(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees"),
            defaultProperties.getExpandSelectTree());
    // Execute producer
    execute(deletedEntries);
    // Verify
    String xml = getXML();
    assertXpathExists("/a:feed/at:deleted-entry[@ref and @when]", xml);
    assertXpathEvaluatesTo("http://host:80/service/Employees('1')", "/a:feed/at:deleted-entry/@ref", xml);
    assertXpathNotExists("/a:feed/at:deleted-entry[@when='1999-01-01T00:00:00Z']", xml);
  }

  @Test
  public void entryWithSyndicatedUpdatedMappingNull() throws Exception {
    // Prepare Data
    List<Map<String, Object>> deletedEntries = new ArrayList<Map<String, Object>>();
    employeeData.put("EntryDate", null);
    deletedEntries.add(employeeData);
    defaultEia =
        EntityInfoAggregator.create(MockFacade.getMockEdm().getDefaultEntityContainer().getEntitySet("Employees"),
            defaultProperties.getExpandSelectTree());
    // Execute producer
    execute(deletedEntries);
    // Verify
    String xml = getXML();
    assertXpathExists("/a:feed/at:deleted-entry[@ref and @when]", xml);
    assertXpathEvaluatesTo("http://host:80/service/Employees('1')", "/a:feed/at:deleted-entry/@ref", xml);
    assertXpathNotExists("/a:feed/at:deleted-entry[@when='1999-01-01T00:00:00Z']", xml);
  }

  private String getXML() throws IOException {
    InputStream inputStream = csb.getInputStream();
    assertNotNull(inputStream);
    String xml = StringHelper.inputStreamToString(inputStream);
    assertNotNull(xml);
    return xml;
  }

  void execute(final List<Map<String, Object>> deletedEntries) throws XMLStreamException, EntityProviderException {
    TombstoneProducer producer = new TombstoneProducer();
    writer.writeStartDocument(DEFAULT_CHARSET, XML_VERSION);
    writer.writeStartElement("feed");
    writer.writeDefaultNamespace(Edm.NAMESPACE_ATOM_2005);
    writer.writeNamespace(TombstoneCallback.PREFIX_TOMBSTONE, TombstoneCallback.NAMESPACE_TOMBSTONE);
    producer.appendTombstones(writer, defaultEia, defaultProperties, deletedEntries);
    writer.writeEndDocument();
  }

}
