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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.Association;
import org.apache.olingo.odata2.api.edm.provider.AssociationSet;
import org.apache.olingo.odata2.api.edm.provider.ComplexType;
import org.apache.olingo.odata2.api.edm.provider.EntityContainerInfo;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.Schema;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.core.common.ODataJPATestConstants;
import org.apache.olingo.odata2.jpa.processor.core.mock.ODataJPAContextMock;
import org.apache.olingo.odata2.jpa.processor.core.mock.model.EdmSchemaMock;
import org.apache.olingo.odata2.jpa.processor.core.model.JPAEdmModel;
import org.junit.BeforeClass;
import org.junit.Test;

public class ODataJPAEdmProviderTest {

  private static ODataJPAEdmProvider edmProvider;

  @BeforeClass
  public static void setup() {

    edmProvider = new ODataJPAEdmProvider();
    try {
      Class<? extends ODataJPAEdmProvider> clazz = edmProvider.getClass();
      Field field = clazz.getDeclaredField("schemas");
      field.setAccessible(true);
      List<Schema> schemas = new ArrayList<Schema>();
      schemas.add(EdmSchemaMock.createMockEdmSchema());
      field.set(edmProvider, schemas);
      field = clazz.getDeclaredField("oDataJPAContext");
      field.setAccessible(true);
      field.set(edmProvider, ODataJPAContextMock.mockODataJPAContext());
      field = clazz.getDeclaredField("jpaEdmModel");
      field.setAccessible(true);
      field.set(edmProvider, new JPAEdmModel(null, null));
    } catch (IllegalArgumentException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (IllegalAccessException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (NoSuchFieldException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (SecurityException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }

  }

  @Test
  public void testConstructor() {
    try {
      ODataJPAEdmProvider edmProv = new ODataJPAEdmProvider(ODataJPAContextMock.mockODataJPAContext());
      edmProv.getClass();
    } catch (Exception e) {
      e.printStackTrace();
      assertTrue(true);
    }
  }

  @Test
  public void testGetODataJPAContext() {
    String pUnitName = edmProvider.getODataJPAContext().getPersistenceUnitName();
    assertEquals("salesorderprocessing", pUnitName);
  }

  @Test
  public void testGetEntityContainerInfo() {
    String entityContainerName = null;
    EntityContainerInfo entityContainer = null;
    try {
      entityContainer = edmProvider.getEntityContainerInfo("salesorderprocessingContainer");
      entityContainerName = entityContainer.getName();
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }

    assertEquals("salesorderprocessingContainer", entityContainerName);
    assertNotNull(entityContainer);
  }

  @Test
  public void testDefaultGetEntityContainerInfo() {
    String entityContainerName = null;
    EntityContainerInfo entityContainer = null;
    try {
      entityContainer = edmProvider.getEntityContainerInfo(null);
      entityContainerName = entityContainer.getName();
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }

    assertEquals("salesorderprocessingContainer", entityContainerName);
    assertNotNull(entityContainer);
  }

  @Test
  public void testGetEntityType() {
    FullQualifiedName entityTypeName = new FullQualifiedName("salesorderprocessing", "SalesOrderHeader");
    String entityName = null;
    try {
      entityName = edmProvider.getEntityType(entityTypeName).getName();
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    assertEquals("SalesOrderHeader", entityName);
    try {
      edmProvider.getEntityType(new FullQualifiedName("salesorder", "abc"));
    } catch (ODataException e) {
      assertTrue(true);
    }

  }

  @Test
  public void testGetComplexType() {
    FullQualifiedName complexTypeName = new FullQualifiedName("salesorderprocessing", "Address");
    String nameStr = null;
    try {
      nameStr = edmProvider.getComplexType(complexTypeName).getName();
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    assertEquals("Address", nameStr);
  }

  @Test
  public void testGetAssociationFullQualifiedName() {
    Association association = null;
    try {
      association =
          edmProvider.getAssociation(new FullQualifiedName("salesorderprocessing", "SalesOrderHeader_SalesOrderItem"));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    assertNotNull(association);
    assertEquals("SalesOrderHeader_SalesOrderItem", association.getName());
  }

  @Test
  public void testGetEntitySet() {
    String entitySetName = null;
    try {
      entitySetName = edmProvider.getEntitySet("salesorderprocessingContainer", "SalesOrderHeaders").getName();
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    assertEquals("SalesOrderHeaders", entitySetName);
    try {
      assertNull(edmProvider.getEntitySet("salesorderprocessing", "SalesOrderHeaders"));
    } catch (ODataException e) {
      assertTrue(true);
    }
  }

  @Test
  public void testGetAssociationSet() {
    AssociationSet associationSet = null;

    try {
      associationSet =
          edmProvider.getAssociationSet("salesorderprocessingContainer", new FullQualifiedName("salesorderprocessing",
              "SalesOrderHeader_SalesOrderItem"), "SalesOrderHeaders", "SalesOrderHeader");
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    assertNotNull(associationSet);
    assertEquals("SalesOrderHeader_SalesOrderItemSet", associationSet.getName());
    try {
      associationSet =
          edmProvider.getAssociationSet("salesorderprocessingContainer", new FullQualifiedName("salesorderprocessing",
              "SalesOrderHeader_SalesOrderItem"), "SalesOrderItems", "SalesOrderItem");
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    assertNotNull(associationSet);
    try {
      associationSet =
          edmProvider.getAssociationSet("salesorderproceContainer", new FullQualifiedName("salesorderprocessing",
              "SalesOrderHeader_SalesOrderItem"), "SalesOrderItems", "SalesOrderItem");
    } catch (ODataException e) {
      assertTrue(true);
    }
  }

  @Test
  public void testGetFunctionImport() {
    String functionImportName = null;
    try {
      functionImportName =
          edmProvider.getFunctionImport("salesorderprocessingContainer", "SalesOrder_FunctionImport1").getName();
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    assertEquals("SalesOrder_FunctionImport1", functionImportName);
    try {
      functionImportName =
          edmProvider.getFunctionImport("salesorderprocessingContainer", "SalesOrder_FunctionImport1").getName();
    } catch (ODataException e) {
      assertTrue(true);
    }
    try {
      assertNotNull(edmProvider.getFunctionImport("salesorderprocessingContainer", "SalesOrder_FunctionImport1"));
    } catch (ODataException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testGetSchemas() {
    try {
      assertNotNull(edmProvider.getSchemas());
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testgetComplexTypeWithBuffer() {
    HashMap<String, ComplexType> compTypes = new HashMap<String, ComplexType>();
    ComplexType comp = new ComplexType();
    comp.setName("Address");
    compTypes.put("salesorderprocessing" + "." + "Address", comp);
    ODataJPAEdmProvider jpaEdmProv = new ODataJPAEdmProvider();
    Class<?> claz = jpaEdmProv.getClass();
    Field f;
    try {
      f = claz.getDeclaredField("complexTypes");
      f.setAccessible(true);
      f.set(jpaEdmProv, compTypes);
    } catch (NoSuchFieldException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (SecurityException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (IllegalArgumentException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (IllegalAccessException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }

    try {
      assertEquals(comp, jpaEdmProv.getComplexType(new FullQualifiedName("salesorderprocessing", "Address")));
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    try {
      jpaEdmProv.getComplexType(new FullQualifiedName("salesorderessing", "abc"));
    } catch (ODataJPAModelException e) {
      assertTrue(true);
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testGetEntityContainerInfoWithBuffer() {
    HashMap<String, EntityContainerInfo> entityContainerInfos = new HashMap<String, EntityContainerInfo>();
    EntityContainerInfo entityContainer = new EntityContainerInfo();
    entityContainer.setName("salesorderprocessingContainer");
    entityContainerInfos.put("salesorderprocessingContainer", entityContainer);
    ODataJPAEdmProvider jpaEdmProv = new ODataJPAEdmProvider();
    Class<?> claz = jpaEdmProv.getClass();
    try {
      Field f = claz.getDeclaredField("entityContainerInfos");
      f.setAccessible(true);
      f.set(jpaEdmProv, entityContainerInfos);
      assertEquals(entityContainer, jpaEdmProv.getEntityContainerInfo("salesorderprocessingContainer"));
      jpaEdmProv.getEntityContainerInfo("abc");
    } catch (ODataJPAModelException e) {
      assertTrue(true);
    } catch (NoSuchFieldException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (SecurityException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (IllegalArgumentException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (IllegalAccessException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testGetEntityTypeWithBuffer() {
    HashMap<String, EntityType> entityTypes = new HashMap<String, EntityType>();
    org.apache.olingo.odata2.api.edm.provider.EntityType entity =
        new org.apache.olingo.odata2.api.edm.provider.EntityType();
    entity.setName("SalesOrderHeader");
    entityTypes.put("salesorderprocessing" + "." + "SalesorderHeader", entity);
    ODataJPAEdmProvider jpaEdmProv = new ODataJPAEdmProvider();
    Class<?> claz = jpaEdmProv.getClass();
    Field f;
    try {
      f = claz.getDeclaredField("entityTypes");
      f.setAccessible(true);
      f.set(jpaEdmProv, entityTypes);
      assertEquals(entity, jpaEdmProv.getEntityType(new FullQualifiedName("salesorderprocessing", "SalesorderHeader")));
    } catch (NoSuchFieldException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (SecurityException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (IllegalArgumentException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (IllegalAccessException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    try {
      jpaEdmProv.getEntityType(new FullQualifiedName("salesoprocessing", "abc"));
    } catch (ODataJPAModelException e) {
      assertTrue(true);
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

  @Test
  public void testGetAssociationWithBuffer() {
    HashMap<String, Association> associations = new HashMap<String, Association>();
    Association association = new Association();
    association.setName("SalesOrderHeader_SalesOrderItem");
    associations.put("salesorderprocessing" + "." + "SalesOrderHeader_SalesOrderItem", association);
    ODataJPAEdmProvider jpaEdmProv = new ODataJPAEdmProvider();
    Class<?> claz = jpaEdmProv.getClass();
    Field f;
    try {
      f = claz.getDeclaredField("associations");
      f.setAccessible(true);
      f.set(jpaEdmProv, associations);
      assertEquals(association, jpaEdmProv.getAssociation(new FullQualifiedName("salesorderprocessing",
          "SalesOrderHeader_SalesOrderItem")));
    } catch (NoSuchFieldException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (SecurityException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (IllegalArgumentException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (IllegalAccessException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
    try {
      jpaEdmProv.getAssociation(new FullQualifiedName("salesorderprocessing", "abc"));
    } catch (ODataJPAModelException e) {
      assertTrue(true);
    } catch (ODataException e) {
      fail(ODataJPATestConstants.EXCEPTION_MSG_PART_1 + e.getMessage() + ODataJPATestConstants.EXCEPTION_MSG_PART_2);
    }
  }

}
