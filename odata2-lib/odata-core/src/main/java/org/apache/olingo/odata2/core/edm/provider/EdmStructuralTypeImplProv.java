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
package org.apache.olingo.odata2.core.edm.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmAnnotatable;
import org.apache.olingo.odata2.api.edm.EdmAnnotations;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmMapping;
import org.apache.olingo.odata2.api.edm.EdmStructuralType;
import org.apache.olingo.odata2.api.edm.EdmTypeKind;
import org.apache.olingo.odata2.api.edm.EdmTyped;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.ComplexProperty;
import org.apache.olingo.odata2.api.edm.provider.ComplexType;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;

/**
 *  
 */
public abstract class EdmStructuralTypeImplProv extends EdmNamedImplProv implements EdmStructuralType, EdmAnnotatable {

  protected EdmStructuralType edmBaseType;
  protected ComplexType structuralType;
  private EdmTypeKind edmTypeKind;
  protected String namespace;
  protected Map<String, EdmTyped> edmProperties;
  private Map<String, Property> properties;
  private List<String> edmPropertyNames;
  private EdmAnnotations annotations;

  public EdmStructuralTypeImplProv(final EdmImplProv edm, final ComplexType structuralType,
      final EdmTypeKind edmTypeKind, final String namespace) throws EdmException {
    super(edm, structuralType.getName());
    this.structuralType = structuralType;
    this.namespace = namespace;
    this.edmTypeKind = edmTypeKind;

    resolveBaseType();

    buildPropertiesInternal();

    edmProperties = new HashMap<String, EdmTyped>();
  }

  private void resolveBaseType() throws EdmException {
    FullQualifiedName fqName = structuralType.getBaseType();
    if (fqName != null) {
      if (EdmTypeKind.COMPLEX.equals(edmTypeKind)) {
        edmBaseType = edm.getComplexType(fqName.getNamespace(), fqName.getName());
      } else if (EdmTypeKind.ENTITY.equals(edmTypeKind)) {
        edmBaseType = edm.getEntityType(fqName.getNamespace(), fqName.getName());
      }
      if (edmBaseType == null) {
        throw new EdmException(EdmException.COMMON);
      }
    }
  }

  private void buildPropertiesInternal() throws EdmException {
    properties = new HashMap<String, Property>();

    if (structuralType.getProperties() != null) {
      for (final Property property : structuralType.getProperties()) {
        properties.put(property.getName(), property);
      }
    }
  }

  @Override
  public String getNamespace() throws EdmException {
    return namespace;
  }

  @Override
  public EdmTyped getProperty(final String name) throws EdmException {
    EdmTyped property = edmProperties.get(name);
    if (property == null) {
      property = getPropertyInternal(name);
      if (property == null && edmBaseType != null) {
        property = edmBaseType.getProperty(name);
      }
    }
    return property;
  }

  @Override
  public List<String> getPropertyNames() throws EdmException {
    if (edmPropertyNames == null) {
      final List<String> temp = new ArrayList<String>();
      if (edmBaseType != null) {
        temp.addAll(edmBaseType.getPropertyNames());
      }
      if (structuralType.getProperties() != null) {
        for (final Property property : structuralType.getProperties()) {
          temp.add(property.getName());
        }
      }
      edmPropertyNames = temp;
    }

    return edmPropertyNames;
  }

  @Override
  public EdmStructuralType getBaseType() throws EdmException {
    return edmBaseType;
  }

  @Override
  public EdmTypeKind getKind() {
    return edmTypeKind;
  }

  @Override
  public EdmMapping getMapping() throws EdmException {
    return structuralType.getMapping();
  }

  protected EdmTyped getPropertyInternal(final String name) throws EdmException {
    EdmTyped edmProperty = null;

    if (properties.containsKey(name)) {
      edmProperty = createProperty(properties.get(name));
      edmProperties.put(name, edmProperty);
    } else if (edmBaseType != null) {
      edmProperty = edmBaseType.getProperty(name);
      if (edmProperty != null) {
        edmProperties.put(name, edmProperty);
      }
    }

    return edmProperty;
  }

  protected EdmTyped createProperty(final Property property) throws EdmException {
    if (property instanceof SimpleProperty) {
      return new EdmSimplePropertyImplProv(edm, (SimpleProperty) property);
    } else if (property instanceof ComplexProperty) {
      return new EdmComplexPropertyImplProv(edm, (ComplexProperty) property);
    } else {
      throw new EdmException(EdmException.COMMON);
    }
  }

  @Override
  public String toString() {
    try {
      return namespace + Edm.DELIMITER + getName();
    } catch (final EdmException e) {
      return null;
    }
  }

  @Override
  public EdmAnnotations getAnnotations() throws EdmException {
    if (annotations == null) {
      annotations =
          new EdmAnnotationsImplProv(structuralType.getAnnotationAttributes(), structuralType.getAnnotationElements());
    }
    return annotations;
  }
}
