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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.olingo.odata2.api.ODataServiceVersion;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.commons.ODataHttpHeaders;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmType;
import org.apache.olingo.odata2.api.edm.EdmTypeKind;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataDeltaFeed;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;
import org.apache.olingo.odata2.api.processor.ODataErrorContext;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataResponse.ODataResponseBuilder;
import org.apache.olingo.odata2.api.servicedocument.ServiceDocument;
import org.apache.olingo.odata2.core.commons.ContentType;
import org.apache.olingo.odata2.core.commons.ContentType.ODataFormat;
import org.apache.olingo.odata2.core.commons.XmlHelper;
import org.apache.olingo.odata2.core.ep.aggregator.EntityInfoAggregator;
import org.apache.olingo.odata2.core.ep.aggregator.EntityPropertyInfo;
import org.apache.olingo.odata2.core.ep.consumer.AtomServiceDocumentConsumer;
import org.apache.olingo.odata2.core.ep.consumer.XmlEntityConsumer;
import org.apache.olingo.odata2.core.ep.consumer.XmlErrorDocumentConsumer;
import org.apache.olingo.odata2.core.ep.producer.AtomEntryEntityProducer;
import org.apache.olingo.odata2.core.ep.producer.AtomFeedProducer;
import org.apache.olingo.odata2.core.ep.producer.AtomServiceDocumentProducer;
import org.apache.olingo.odata2.core.ep.producer.XmlCollectionEntityProducer;
import org.apache.olingo.odata2.core.ep.producer.XmlErrorDocumentProducer;
import org.apache.olingo.odata2.core.ep.producer.XmlLinkEntityProducer;
import org.apache.olingo.odata2.core.ep.producer.XmlLinksEntityProducer;
import org.apache.olingo.odata2.core.ep.producer.XmlPropertyEntityProducer;
import org.apache.olingo.odata2.core.ep.util.CircleStreamBuffer;
import org.apache.olingo.odata2.core.exception.ODataRuntimeException;

/**
 *  
 */
public class AtomEntityProvider implements ContentTypeBasedEntityProvider {

  /** Default used charset for writer and response content header */
  private static final String DEFAULT_CHARSET = ContentType.CHARSET_UTF_8;
  private static final String XML_VERSION = "1.0";

  public AtomEntityProvider() throws EntityProviderException {
    this(ODataFormat.ATOM);
  }

  public AtomEntityProvider(final ODataFormat odataFormat) throws EntityProviderException {
    if (odataFormat != ODataFormat.ATOM && odataFormat != ODataFormat.XML) {
      throw new EntityProviderException(EntityProviderException.ILLEGAL_ARGUMENT
          .addContent("Got unsupported ODataFormat '" + odataFormat + "'."));
    }
  }

  /**
   * <p>Serializes an error message according to the OData standard.</p>
   * <p>In case an error occurs, it is logged.
   * An exception is not thrown because this method is used in exception handling.</p>
   * @param status the {@link HttpStatusCodes} associated with this error
   * @param errorCode a String that serves as a substatus to the HTTP response code
   * @param message a human-readable message describing the error
   * @param locale the {@link Locale} that should be used to format the error message
   * @param innerError the inner error for this message. If it is null or an empty String no inner error tag is shown
   * inside the response xml
   * @return an {@link ODataResponse} containing the serialized error message
   */
  @Override
  public ODataResponse writeErrorDocument(final HttpStatusCodes status, final String errorCode, final String message,
      final Locale locale, final String innerError) {
    CircleStreamBuffer csb = new CircleStreamBuffer();

    try {
      OutputStream outStream = csb.getOutputStream();
      XMLStreamWriter writer = XmlHelper.getXMLOutputFactory().createXMLStreamWriter(outStream, DEFAULT_CHARSET);

      XmlErrorDocumentProducer producer = new XmlErrorDocumentProducer();
      producer.writeErrorDocument(writer, errorCode, message, locale, innerError);

      writer.flush();
      csb.closeWrite();

      ODataResponseBuilder response = ODataResponse.entity(csb.getInputStream())
          .header(ODataHttpHeaders.DATASERVICEVERSION, ODataServiceVersion.V10)
          .status(status);
      return response.build();
    } catch (Exception e) {
      csb.close();
      throw new ODataRuntimeException(e);
    }
  }

  /**
   * Write service document based on given {@link Edm} and <code>service root</code> as
   * <code>AtomPub Service Document/code> with charset encoding {@value #DEFAULT_CHARSET}.
   * 
   * @param edm the Entity Data Model
   * @param serviceRoot the root URI of the service
   * @return resulting {@link ODataResponse} with written service document
   * @throws EntityProviderException
   */
  @Override
  public ODataResponse writeServiceDocument(final Edm edm, final String serviceRoot) throws EntityProviderException {
    CircleStreamBuffer csb = new CircleStreamBuffer();

    try {
      OutputStreamWriter writer = new OutputStreamWriter(csb.getOutputStream(), DEFAULT_CHARSET);
      AtomServiceDocumentProducer as = new AtomServiceDocumentProducer(edm, serviceRoot);
      as.writeServiceDocument(writer);
      csb.closeWrite();

      return ODataResponse.entity(csb.getInputStream()).build();
    } catch (EntityProviderException e) {
      csb.close();
      throw e;
    } catch (Exception e) {
      csb.close();
      throw new EntityProviderProducerException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }
  }

  @Override
  public ODataResponse writeEntry(final EdmEntitySet entitySet, final Map<String, Object> data,
      final EntityProviderWriteProperties properties) throws EntityProviderException {
    CircleStreamBuffer csb = new CircleStreamBuffer();

    try {
      OutputStream outStream = csb.getOutputStream();
      XMLStreamWriter writer = XmlHelper.getXMLOutputFactory().createXMLStreamWriter(outStream, DEFAULT_CHARSET);
      writer.writeStartDocument(DEFAULT_CHARSET, XML_VERSION);

      AtomEntryEntityProducer as = new AtomEntryEntityProducer(properties);
      EntityInfoAggregator eia = EntityInfoAggregator.create(entitySet, properties.getExpandSelectTree());
      as.append(writer, eia, data, true, false);

      writer.flush();
      csb.closeWrite();

      ODataResponseBuilder response = ODataResponse.entity(csb.getInputStream())
          .eTag(as.getETag())
          .idLiteral(as.getLocation());
      return response.build();
    } catch (EntityProviderException e) {
      csb.close();
      throw e;
    } catch (Exception e) {
      csb.close();
      throw new EntityProviderProducerException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }
  }

  @Override
  public ODataResponse writeProperty(final EdmProperty edmProperty, final Object value) throws EntityProviderException {
    EntityPropertyInfo propertyInfo = EntityInfoAggregator.create(edmProperty);
    return writeSingleTypedElement(propertyInfo, value);
  }

  private ODataResponse writeSingleTypedElement(final EntityPropertyInfo propertyInfo, final Object value)
      throws EntityProviderException {
    CircleStreamBuffer csb = new CircleStreamBuffer();

    try {
      OutputStream outStream = csb.getOutputStream();
      XMLStreamWriter writer = XmlHelper.getXMLOutputFactory().createXMLStreamWriter(outStream, DEFAULT_CHARSET);
      writer.writeStartDocument(DEFAULT_CHARSET, XML_VERSION);

      XmlPropertyEntityProducer ps = new XmlPropertyEntityProducer(false, true);
      ps.append(writer, propertyInfo, value);

      writer.flush();
      csb.closeWrite();

      return ODataResponse.entity(csb.getInputStream()).build();
    } catch (EntityProviderException e) {
      csb.close();
      throw e;
    } catch (Exception e) {
      csb.close();
      throw new EntityProviderProducerException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }
  }

  @Override
  public ODataResponse writeFeed(final EdmEntitySet entitySet, final List<Map<String, Object>> data,
      final EntityProviderWriteProperties properties) throws EntityProviderException {
    CircleStreamBuffer csb = new CircleStreamBuffer();

    try {
      OutputStream outStream = csb.getOutputStream();
      XMLStreamWriter writer = XmlHelper.getXMLOutputFactory().createXMLStreamWriter(outStream, DEFAULT_CHARSET);
      writer.writeStartDocument(DEFAULT_CHARSET, XML_VERSION);

      AtomFeedProducer atomFeedProvider = new AtomFeedProducer(properties);
      EntityInfoAggregator eia = EntityInfoAggregator.create(entitySet, properties.getExpandSelectTree());
      atomFeedProvider.append(writer, eia, data, false);

      writer.flush();
      csb.closeWrite();

      ODataResponse response = ODataResponse.entity(csb.getInputStream()).build();
      return response;
    } catch (EntityProviderException e) {
      csb.close();
      throw e;
    } catch (XMLStreamException e) {
      csb.close();
      throw new EntityProviderProducerException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }
  }

  @Override
  public ODataResponse writeLink(final EdmEntitySet entitySet, final Map<String, Object> data,
      final EntityProviderWriteProperties properties) throws EntityProviderException {
    CircleStreamBuffer csb = new CircleStreamBuffer();

    try {
      OutputStream outStream = csb.getOutputStream();
      XMLStreamWriter writer = XmlHelper.getXMLOutputFactory().createXMLStreamWriter(outStream, DEFAULT_CHARSET);
      writer.writeStartDocument(DEFAULT_CHARSET, XML_VERSION);

      XmlLinkEntityProducer entity = new XmlLinkEntityProducer(properties);
      final EntityInfoAggregator entityInfo = EntityInfoAggregator.create(entitySet, properties.getExpandSelectTree());
      entity.append(writer, entityInfo, data, true);

      writer.flush();
      csb.closeWrite();

      return ODataResponse.entity(csb.getInputStream()).build();
    } catch (EntityProviderException e) {
      csb.close();
      throw e;
    } catch (Exception e) {
      csb.close();
      throw new EntityProviderProducerException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }

  }

  @Override
  public ODataResponse writeLinks(final EdmEntitySet entitySet, final List<Map<String, Object>> data,
      final EntityProviderWriteProperties properties) throws EntityProviderException {
    CircleStreamBuffer csb = new CircleStreamBuffer();

    try {
      OutputStream outStream = csb.getOutputStream();
      XMLStreamWriter writer = XmlHelper.getXMLOutputFactory().createXMLStreamWriter(outStream, DEFAULT_CHARSET);
      writer.writeStartDocument(DEFAULT_CHARSET, XML_VERSION);

      XmlLinksEntityProducer entity = new XmlLinksEntityProducer(properties);
      final EntityInfoAggregator entityInfo = EntityInfoAggregator.create(entitySet, properties.getExpandSelectTree());
      entity.append(writer, entityInfo, data);

      writer.flush();
      csb.closeWrite();

      return ODataResponse.entity(csb.getInputStream()).build();
    } catch (EntityProviderException e) {
      csb.close();
      throw e;
    } catch (Exception e) {
      csb.close();
      throw new EntityProviderProducerException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }
  }

  private ODataResponse writeCollection(final EntityPropertyInfo propertyInfo, final List<?> data)
      throws EntityProviderException {
    CircleStreamBuffer csb = new CircleStreamBuffer();

    try {
      OutputStream outStream = csb.getOutputStream();
      XMLStreamWriter writer = XmlHelper.getXMLOutputFactory().createXMLStreamWriter(outStream, DEFAULT_CHARSET);
      writer.writeStartDocument(DEFAULT_CHARSET, XML_VERSION);

      XmlCollectionEntityProducer.append(writer, propertyInfo, data);

      writer.flush();
      csb.closeWrite();

      return ODataResponse.entity(csb.getInputStream()).build();
    } catch (EntityProviderException e) {
      csb.close();
      throw e;
    } catch (Exception e) {
      csb.close();
      throw new EntityProviderProducerException(EntityProviderException.EXCEPTION_OCCURRED.addContent(e.getClass()
          .getSimpleName()), e);
    }
  }

  @Override
  public ODataResponse writeFunctionImport(final EdmFunctionImport functionImport, final Object data,
      final EntityProviderWriteProperties properties) throws EntityProviderException {
    try {
      final EdmType type = functionImport.getReturnType().getType();
      final boolean isCollection = functionImport.getReturnType().getMultiplicity() == EdmMultiplicity.MANY;

      if (type.getKind() == EdmTypeKind.ENTITY) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) data;
        return writeEntry(functionImport.getEntitySet(), map, properties);
      }

      final EntityPropertyInfo info = EntityInfoAggregator.create(functionImport);
      if (isCollection) {
        return writeCollection(info, (List<?>) data);
      } else {
        return writeSingleTypedElement(info, data);
      }
    } catch (EdmException e) {
      throw new EntityProviderProducerException(e.getMessageReference(), e);
    }
  }

  @Override
  public ODataFeed readFeed(final EdmEntitySet entitySet, final InputStream content,
      final EntityProviderReadProperties properties) throws EntityProviderException {
    return readDeltaFeed(entitySet, content, properties);
  }

  @Override
  public ODataDeltaFeed readDeltaFeed(final EdmEntitySet entitySet, final InputStream content,
      final EntityProviderReadProperties properties) throws EntityProviderException {
    XmlEntityConsumer xec = new XmlEntityConsumer();
    return xec.readFeed(entitySet, content, properties);
  }

  @Override
  public ODataEntry readEntry(final EdmEntitySet entitySet, final InputStream content,
      final EntityProviderReadProperties properties) throws EntityProviderException {
    XmlEntityConsumer xec = new XmlEntityConsumer();
    return xec.readEntry(entitySet, content, properties);
  }

  @Override
  public Map<String, Object> readProperty(final EdmProperty edmProperty, final InputStream content,
      final EntityProviderReadProperties properties) throws EntityProviderException {
    XmlEntityConsumer xec = new XmlEntityConsumer();
    return xec.readProperty(edmProperty, content, properties);
  }

  @Override
  public String readLink(final EdmEntitySet entitySet, final InputStream content) throws EntityProviderException {
    XmlEntityConsumer xec = new XmlEntityConsumer();
    return xec.readLink(entitySet, content);
  }

  @Override
  public List<String> readLinks(final EdmEntitySet entitySet, final InputStream content)
      throws EntityProviderException {
    XmlEntityConsumer xec = new XmlEntityConsumer();
    return xec.readLinks(entitySet, content);
  }

  @Override
  public ServiceDocument readServiceDocument(final InputStream serviceDocument) throws EntityProviderException {
    AtomServiceDocumentConsumer serviceDocConsumer = new AtomServiceDocumentConsumer();
    return serviceDocConsumer.parseXml(serviceDocument);
  }

  @Override
  public ODataErrorContext readErrorDocument(final InputStream errorDocument) throws EntityProviderException {
    XmlErrorDocumentConsumer xmlErrorDocumentConsumer = new XmlErrorDocumentConsumer();
    return xmlErrorDocumentConsumer.readError(errorDocument);
  }

  @Override
  public Object readFunctionImport(final EdmFunctionImport functionImport, InputStream content,
      final EntityProviderReadProperties properties) throws EntityProviderException {
    try {
      if (functionImport.getReturnType().getType().getKind() == EdmTypeKind.ENTITY) {
        return new XmlEntityConsumer().readEntry(functionImport.getEntitySet(), content, properties);
      } else {
        final EntityPropertyInfo info = EntityInfoAggregator.create(functionImport);
        return functionImport.getReturnType().getMultiplicity() == EdmMultiplicity.MANY ?
          new XmlEntityConsumer().readCollection(info, content, properties) :
          new XmlEntityConsumer().readProperty(info, content, properties).get(info.getName());
      }
    } catch (final EdmException e) {
      throw new EntityProviderException(e.getMessageReference(), e);
    }
  }
}
