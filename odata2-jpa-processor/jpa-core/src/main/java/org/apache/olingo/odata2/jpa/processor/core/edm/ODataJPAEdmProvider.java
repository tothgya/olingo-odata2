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
package org.apache.olingo.odata2.jpa.processor.core.edm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.Association;
import org.apache.olingo.odata2.api.edm.provider.AssociationSet;
import org.apache.olingo.odata2.api.edm.provider.AssociationSetEnd;
import org.apache.olingo.odata2.api.edm.provider.ComplexType;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.edm.provider.EntityContainer;
import org.apache.olingo.odata2.api.edm.provider.EntityContainerInfo;
import org.apache.olingo.odata2.api.edm.provider.EntitySet;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.FunctionImport;
import org.apache.olingo.odata2.api.edm.provider.Schema;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.factory.ODataJPAFactory;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmModelView;

public class ODataJPAEdmProvider extends EdmProvider {

  private ODataJPAContext oDataJPAContext;
  private JPAEdmModelView jpaEdmModel;

  private List<Schema> schemas;
  private HashMap<String, EntityType> entityTypes;
  private HashMap<String, EntityContainerInfo> entityContainerInfos;
  private HashMap<String, ComplexType> complexTypes;
  private HashMap<String, Association> associations;
  private HashMap<String, FunctionImport> functionImports;

  public ODataJPAEdmProvider() {
    entityTypes = new LinkedHashMap<String, EntityType>();
    entityContainerInfos = new LinkedHashMap<String, EntityContainerInfo>();
    complexTypes = new LinkedHashMap<String, ComplexType>();
    associations = new LinkedHashMap<String, Association>();
    functionImports = new LinkedHashMap<String, FunctionImport>();
  }

  public ODataJPAEdmProvider(final ODataJPAContext oDataJPAContext) {
    if (oDataJPAContext == null) {
      throw new IllegalArgumentException(ODataJPAException.ODATA_JPACTX_NULL);
    }
    entityTypes = new LinkedHashMap<String, EntityType>();
    entityContainerInfos = new LinkedHashMap<String, EntityContainerInfo>();
    complexTypes = new LinkedHashMap<String, ComplexType>();
    associations = new LinkedHashMap<String, Association>();
    functionImports = new LinkedHashMap<String, FunctionImport>();
    jpaEdmModel = ODataJPAFactory.createFactory().getJPAAccessFactory().getJPAEdmModelView(oDataJPAContext);
  }

  public ODataJPAContext getODataJPAContext() {
    return oDataJPAContext;
  }

  public void setODataJPAContext(final ODataJPAContext jpaContext) {
    oDataJPAContext = jpaContext;
  }

  @Override
  public EntityContainerInfo getEntityContainerInfo(final String name) throws ODataException {

    if (entityContainerInfos.containsKey(name)) {
      return entityContainerInfos.get(name);
    } else {

      if (schemas == null) {
        getSchemas();
      }
      List<EntityContainer> containerList = schemas.get(0).getEntityContainers();
      if (containerList == null) {
        return null;
      }
      for (EntityContainer container : containerList) {
        if (name == null && container.isDefaultEntityContainer()) {
          entityContainerInfos.put(name, container);
          return container;
        } else if (name != null && name.equals(container.getName())) {
          return container;
        }
      }
    }
    return null;
  }

  @Override
  public EntityType getEntityType(final FullQualifiedName edmFQName) throws ODataException {

    String strEdmFQName = null;

    if (edmFQName != null) {
      strEdmFQName = edmFQName.toString();
      if (entityTypes.containsKey(strEdmFQName)) {
        return entityTypes.get(strEdmFQName);
      } else if (schemas == null) {
        getSchemas();
      }

      String entityTypeNamespace = edmFQName.getNamespace();
      String entityTypeName = edmFQName.getName();

      for (Schema schema : schemas) {
        String schemaNamespace = schema.getNamespace();
        if (schemaNamespace.equals(entityTypeNamespace)) {
          if (schema.getEntityTypes() == null) {
            return null;
          }
          for (EntityType et : schema.getEntityTypes()) {
            if (et.getName().equals(entityTypeName)) {
              entityTypes.put(strEdmFQName, et);
              return et;
            }
          }
        }
      }
    }

    return null;
  }

  @Override
  public ComplexType getComplexType(final FullQualifiedName edmFQName) throws ODataException {

    if (edmFQName != null) {
      if (complexTypes.containsKey(edmFQName.toString())) {
        return complexTypes.get(edmFQName.toString());
      } else if (schemas == null) {
        getSchemas();
      }

      for (Schema schema : schemas) {
        if (schema.getNamespace().equals(edmFQName.getNamespace())) {
          if (schema.getComplexTypes() == null) {
            return null;
          }
          for (ComplexType ct : schema.getComplexTypes()) {
            if (ct.getName().equals(edmFQName.getName())) {
              complexTypes.put(edmFQName.toString(), ct);
              return ct;
            }
          }
        }
      }
    }

    return null;
  }

  @Override
  public Association getAssociation(final FullQualifiedName edmFQName) throws ODataException {
    if (edmFQName != null) {
      if (associations.containsKey(edmFQName.toString())) {
        return associations.get(edmFQName.toString());
      } else if (schemas == null) {
        getSchemas();
      }

      for (Schema schema : schemas) {
        if (schema.getNamespace().equals(edmFQName.getNamespace())) {
          if (schema.getAssociations() == null) {
            return null;
          }
          for (Association association : schema.getAssociations()) {
            if (association.getName().equals(edmFQName.getName())) {
              associations.put(edmFQName.toString(), association);
              return association;
            }
          }
        }
      }

    }
    return null;
  }

  @Override
  public EntitySet getEntitySet(final String entityContainer, final String name) throws ODataException {

    EntitySet returnedSet = null;
    EntityContainer container = null;
    if (!entityContainerInfos.containsKey(entityContainer)) {
      container = (EntityContainer) getEntityContainerInfo(entityContainer);
    } else {
      container = (EntityContainer) entityContainerInfos.get(entityContainer);
    }

    if (container != null && name != null) {
      for (EntitySet es : container.getEntitySets()) {
        if (name.equals(es.getName())) {
          returnedSet = es;
          break;
        }
      }
    }

    return returnedSet;
  }

  @Override
  public AssociationSet getAssociationSet(final String entityContainer, final FullQualifiedName association,
      final String sourceEntitySetName, final String sourceEntitySetRole) throws ODataException {

    EntityContainer container = null;
    if (!entityContainerInfos.containsKey(entityContainer)) {
      container = (EntityContainer) getEntityContainerInfo(entityContainer);
    } else {
      container = (EntityContainer) entityContainerInfos.get(entityContainer);
    }

    if (container != null && association != null && container.getAssociationSets() != null) {
      for (AssociationSet as : container.getAssociationSets()) {
        if (association.equals(as.getAssociation())) {
          AssociationSetEnd end = as.getEnd1();
          if (sourceEntitySetName.equals(end.getEntitySet()) && sourceEntitySetRole.equals(end.getRole())) {
            return as;
          } else {
            end = as.getEnd2();
            if (sourceEntitySetName.equals(end.getEntitySet()) && sourceEntitySetRole.equals(end.getRole())) {
              return as;
            }
          }
        }
      }
    }
    return null;
  }

  @Override
  public FunctionImport getFunctionImport(final String entityContainer, final String name) throws ODataException {

    if (functionImports.containsKey(name)) {
      return functionImports.get(name);
    }

    EntityContainer container = null;
    if (!entityContainerInfos.containsKey(entityContainer)) {
      container = (EntityContainer) getEntityContainerInfo(entityContainer);
    } else {
      container = (EntityContainer) entityContainerInfos.get(entityContainer);
    }

    if (container != null && name != null) {
      if (container.getFunctionImports() == null) {
        return null;
      }
      for (FunctionImport fi : container.getFunctionImports()) {
        if (name.equals(fi.getName())) {
          functionImports.put(name, fi);
          return fi;
        }
      }
    }
    return null;
  }

  @Override
  public List<Schema> getSchemas() throws ODataException {
    if (schemas == null && jpaEdmModel != null) {
      jpaEdmModel.getBuilder().build();
      schemas = new ArrayList<Schema>();
      schemas.add(jpaEdmModel.getEdmSchemaView().getEdmSchema());
    }
    if (jpaEdmModel == null) {

      throw ODataJPAModelException.throwException(ODataJPAModelException.BUILDER_NULL, null);
    }

    return schemas;

  }

}
