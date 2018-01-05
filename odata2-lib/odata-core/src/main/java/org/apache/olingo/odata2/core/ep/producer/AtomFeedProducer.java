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

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.olingo.odata2.api.commons.InlineCount;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmFacets;
import org.apache.olingo.odata2.api.edm.EdmLiteralKind;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeException;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.callback.TombstoneCallback;
import org.apache.olingo.odata2.api.ep.callback.TombstoneCallbackResult;
import org.apache.olingo.odata2.core.commons.Encoder;
import org.apache.olingo.odata2.core.edm.EdmDateTimeOffset;
import org.apache.olingo.odata2.core.ep.EntityProviderProducerException;
import org.apache.olingo.odata2.core.ep.aggregator.EntityInfoAggregator;
import org.apache.olingo.odata2.core.ep.util.FormatXml;

/**
 * Serializes an ATOM feed.
 * 
 */
public class AtomFeedProducer {

  private final EntityProviderWriteProperties properties;

  public AtomFeedProducer(final EntityProviderWriteProperties properties) {
    this.properties = properties == null ? EntityProviderWriteProperties.serviceRoot(null).build() : properties;
  }

  public void append(final XMLStreamWriter writer, final EntityInfoAggregator eia,
      final List<Map<String, Object>> data, final boolean isInline) throws EntityProviderException {
    try {
      writer.writeStartElement(FormatXml.ATOM_FEED);
      TombstoneCallback callback = null;
      if (!isInline) {
        writer.writeDefaultNamespace(Edm.NAMESPACE_ATOM_2005);
        writer.writeNamespace(Edm.PREFIX_M, Edm.NAMESPACE_M_2007_08);
        writer.writeNamespace(Edm.PREFIX_D, Edm.NAMESPACE_D_2007_08);
        callback = getTombstoneCallback();
        if (callback != null) {
          writer.writeNamespace(TombstoneCallback.PREFIX_TOMBSTONE, TombstoneCallback.NAMESPACE_TOMBSTONE);
        }
      }
      writer.writeAttribute(Edm.PREFIX_XML, Edm.NAMESPACE_XML_1998, FormatXml.XML_BASE, properties.getServiceRoot()
          .toASCIIString());

      // write all atom infos (mandatory and optional)
      appendAtomMandatoryParts(writer, eia);
      appendAtomSelfLink(writer, eia);
      if (properties.getInlineCountType() == InlineCount.ALLPAGES) {
        appendInlineCount(writer, properties.getInlineCount());
      }

      appendEntries(writer, eia, data);

      if (callback != null) {
        appendDeletedEntries(writer, eia, callback);
      }

      if (properties.getNextLink() != null) {
        appendNextLink(writer, properties.getNextLink());
      }

      writer.writeEndElement();
    } catch (XMLStreamException e) {
      throw new EntityProviderProducerException(EntityProviderException.COMMON, e);
    }
  }

  private TombstoneCallback getTombstoneCallback() {
    if (properties.getCallbacks() != null
        && properties.getCallbacks().containsKey(TombstoneCallback.CALLBACK_KEY_TOMBSTONE)) {
      TombstoneCallback callback =
          (TombstoneCallback) properties.getCallbacks().get(TombstoneCallback.CALLBACK_KEY_TOMBSTONE);
      return callback;
    } else {
      return null;
    }
  }

  private void appendDeletedEntries(final XMLStreamWriter writer, final EntityInfoAggregator eia,
      final TombstoneCallback callback) throws EntityProviderException {
    TombstoneCallbackResult callbackResult = callback.getTombstoneCallbackResult();
    List<Map<String, Object>> tombstoneData = callbackResult.getDeletedEntriesData();
    if (tombstoneData != null) {
      TombstoneProducer tombstoneProducer = new TombstoneProducer();
      tombstoneProducer.appendTombstones(writer, eia, properties, tombstoneData);
    }

    String deltaLink = callbackResult.getDeltaLink();
    if (deltaLink != null) {
      try {
        writer.writeStartElement(FormatXml.ATOM_LINK);
        writer.writeAttribute(FormatXml.ATOM_REL, FormatXml.ATOM_DELTA_LINK);
        writer.writeAttribute(FormatXml.ATOM_HREF, deltaLink);
        writer.writeEndElement();
      } catch (XMLStreamException e) {
        throw new EntityProviderProducerException(EntityProviderException.COMMON, e);
      }
    }
  }

  private void appendNextLink(final XMLStreamWriter writer, final String nextLink) throws EntityProviderException {
    try {
      writer.writeStartElement(FormatXml.ATOM_LINK);
      writer.writeAttribute(FormatXml.ATOM_HREF, nextLink);
      writer.writeAttribute(FormatXml.ATOM_REL, FormatXml.ATOM_NEXT_LINK);
      writer.writeEndElement();
    } catch (XMLStreamException e) {
      throw new EntityProviderProducerException(EntityProviderException.COMMON, e);
    }
  }

  private void appendEntries(final XMLStreamWriter writer, final EntityInfoAggregator eia,
      final List<Map<String, Object>> data) throws EntityProviderException {
    AtomEntryEntityProducer entryProvider = new AtomEntryEntityProducer(properties);
    for (Map<String, Object> singleEntryData : data) {
      entryProvider.append(writer, eia, singleEntryData, false, true);
    }
  }

  private void appendInlineCount(final XMLStreamWriter writer, final Integer inlineCount)
      throws EntityProviderException {
    if (inlineCount == null || inlineCount < 0) {
      throw new EntityProviderProducerException(EntityProviderException.INLINECOUNT_INVALID);
    }
    try {
      writer.writeStartElement(Edm.NAMESPACE_M_2007_08, FormatXml.M_COUNT);
      writer.writeCharacters(String.valueOf(inlineCount));
      writer.writeEndElement();
    } catch (XMLStreamException e) {
      throw new EntityProviderProducerException(EntityProviderException.COMMON, e);
    }
  }

  private void appendAtomSelfLink(final XMLStreamWriter writer, final EntityInfoAggregator eia)
      throws EntityProviderException {

    URI self = properties.getSelfLink();
    String selfLink = "";
    if (self == null) {
      selfLink = createSelfLink(eia);
    } else {
      selfLink = self.toASCIIString();
    }
    try {
      writer.writeStartElement(FormatXml.ATOM_LINK);
      writer.writeAttribute(FormatXml.ATOM_HREF, selfLink);
      writer.writeAttribute(FormatXml.ATOM_REL, Edm.LINK_REL_SELF);
      writer.writeAttribute(FormatXml.ATOM_TITLE, eia.getEntitySetName());
      writer.writeEndElement();
    } catch (XMLStreamException e) {
      throw new EntityProviderProducerException(EntityProviderException.COMMON, e);
    }
  }

  private String createSelfLink(final EntityInfoAggregator eia) throws EntityProviderException {
    StringBuilder sb = new StringBuilder();
    if (!eia.isDefaultEntityContainer()) {
      final String entityContainerName = Encoder.encode(eia.getEntityContainerName());
      sb.append(entityContainerName).append(Edm.DELIMITER);
    }
    final String entitySetName = Encoder.encode(eia.getEntitySetName());
    sb.append(entitySetName);
    return sb.toString();
  }

  private void appendAtomMandatoryParts(final XMLStreamWriter writer, final EntityInfoAggregator eia)
      throws EntityProviderException {
    try {
      writer.writeStartElement(FormatXml.ATOM_ID);
      writer.writeCharacters(createAtomId(eia));
      writer.writeEndElement();

      writer.writeStartElement(FormatXml.ATOM_TITLE);
      writer.writeAttribute(FormatXml.M_TYPE, FormatXml.ATOM_TEXT);
      writer.writeCharacters(eia.getEntitySetName());
      writer.writeEndElement();

      writer.writeStartElement(FormatXml.ATOM_UPDATED);

      Object updateDate = null;
      EdmFacets updateFacets = null;
      updateDate = new Date();
      writer.writeCharacters(EdmDateTimeOffset.getInstance().valueToString(updateDate, EdmLiteralKind.DEFAULT,
          updateFacets));
      writer.writeEndElement();

      writer.writeStartElement(FormatXml.ATOM_AUTHOR);
      writer.writeStartElement(FormatXml.ATOM_AUTHOR_NAME);
      writer.writeEndElement();
      writer.writeEndElement();

    } catch (XMLStreamException e) {
      throw new EntityProviderProducerException(EntityProviderException.COMMON, e);
    } catch (EdmSimpleTypeException e) {
      throw new EntityProviderProducerException(e.getMessageReference(), e);
    }
  }

  private String createAtomId(final EntityInfoAggregator eia) throws EntityProviderException {
    return properties.getServiceRoot() + createSelfLink(eia);
  }
}
