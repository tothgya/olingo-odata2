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

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.core.ep.EntityProviderProducerException;
import org.apache.olingo.odata2.core.ep.aggregator.EntityPropertyInfo;
import org.apache.olingo.odata2.core.ep.util.FormatXml;

/**
 * Provider for writing a collection of simple-type or complex-type instances
 * 
 */
public class XmlCollectionEntityProducer {

  public static void append(final XMLStreamWriter writer, final EntityPropertyInfo propertyInfo, final List<?> data)
      throws EntityProviderException {
    try {
      writer.writeStartElement(propertyInfo.getName());
      writer.writeDefaultNamespace(Edm.NAMESPACE_D_2007_08);
      if (propertyInfo.isComplex()) {
        writer.writeNamespace(Edm.PREFIX_M, Edm.NAMESPACE_M_2007_08);
      }
      XmlPropertyEntityProducer provider = new XmlPropertyEntityProducer(false, true);
      for (final Object propertyData : data) {
        provider.append(writer, FormatXml.D_ELEMENT, propertyInfo, propertyData);
      }
      writer.writeEndElement();
      writer.flush();
    } catch (XMLStreamException e) {
      throw new EntityProviderProducerException(EntityProviderException.COMMON, e);
    }
  }
}
