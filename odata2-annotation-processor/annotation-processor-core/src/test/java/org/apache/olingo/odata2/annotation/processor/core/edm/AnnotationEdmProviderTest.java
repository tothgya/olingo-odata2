/*
 * Copyright 2013 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.olingo.odata2.annotation.processor.core.edm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.olingo.odata2.annotation.processor.core.model.Building;
import org.apache.olingo.odata2.annotation.processor.core.model.City;
import org.apache.olingo.odata2.annotation.processor.core.model.Employee;
import org.apache.olingo.odata2.annotation.processor.core.model.Location;
import org.apache.olingo.odata2.annotation.processor.core.model.Manager;
import org.apache.olingo.odata2.annotation.processor.core.model.ModelSharedConstants;
import org.apache.olingo.odata2.annotation.processor.core.model.Photo;
import org.apache.olingo.odata2.annotation.processor.core.model.RefBase;
import org.apache.olingo.odata2.annotation.processor.core.model.Room;
import org.apache.olingo.odata2.annotation.processor.core.model.Team;
import org.apache.olingo.odata2.api.annotation.edm.EdmComplexType;
import org.apache.olingo.odata2.api.annotation.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.annotation.edm.EdmEntityType;
import org.apache.olingo.odata2.api.annotation.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmConcurrencyMode;
import org.apache.olingo.odata2.api.edm.EdmMultiplicity;
import org.apache.olingo.odata2.api.edm.FullQualifiedName;
import org.apache.olingo.odata2.api.edm.provider.Association;
import org.apache.olingo.odata2.api.edm.provider.AssociationEnd;
import org.apache.olingo.odata2.api.edm.provider.AssociationSet;
import org.apache.olingo.odata2.api.edm.provider.ComplexProperty;
import org.apache.olingo.odata2.api.edm.provider.ComplexType;
import org.apache.olingo.odata2.api.edm.provider.EntityContainer;
import org.apache.olingo.odata2.api.edm.provider.EntityContainerInfo;
import org.apache.olingo.odata2.api.edm.provider.EntitySet;
import org.apache.olingo.odata2.api.edm.provider.EntityType;
import org.apache.olingo.odata2.api.edm.provider.FunctionImport;
import org.apache.olingo.odata2.api.edm.provider.Key;
import org.apache.olingo.odata2.api.edm.provider.NavigationProperty;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.PropertyRef;
import org.apache.olingo.odata2.api.edm.provider.Schema;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.junit.Test;

/**
 *
 */
public class AnnotationEdmProviderTest {

  private static final String TEST_MODEL_PACKAGE = "org.apache.olingo.odata2.annotation.processor.core.model";

  @EdmEntityType
  @EdmEntitySet
  private static final class GeneratedNamesTestClass {
    @EdmProperty GeneratedNamesComplexTestClass myComplexProperty;
  }

  @EdmComplexType
  private static final class GeneratedNamesComplexTestClass {}

  @EdmEntityType(namespace = "MyTestNamespace")
  @EdmEntitySet(container = "MyTestContainer")
  private static final class DefinedNamesTestClass {}

  private final AnnotationEdmProvider aep;
  private final Collection<Class<?>> annotatedClasses = new ArrayList<Class<?>>();

  public AnnotationEdmProviderTest() throws ODataException {
    annotatedClasses.add(RefBase.class);
    annotatedClasses.add(Building.class);
    annotatedClasses.add(City.class);
    annotatedClasses.add(Employee.class);
    annotatedClasses.add(Location.class);
    annotatedClasses.add(Manager.class);
    annotatedClasses.add(Photo.class);
    annotatedClasses.add(Room.class);
    annotatedClasses.add(Team.class);

    aep = new AnnotationEdmProvider(annotatedClasses);
  }

  @Test
  public void defaultNameAndNamespaceGeneration() throws ODataException {
    Collection<Class<?>> localAnnotatedClasses = new ArrayList<Class<?>>();
    localAnnotatedClasses.add(GeneratedNamesTestClass.class);
    localAnnotatedClasses.add(GeneratedNamesComplexTestClass.class);
    AnnotationEdmProvider localAep = new AnnotationEdmProvider(localAnnotatedClasses);
    // validate
    EntityType testType = localAep.getEntityType(new FullQualifiedName(
        GeneratedNamesTestClass.class.getPackage().getName(),
        GeneratedNamesTestClass.class.getSimpleName()));
    assertNotNull("Requested entity not found.", testType);
    assertEquals("GeneratedNamesTestClass", testType.getName());
    assertNull("This should not have a base type", testType.getBaseType());
    List<Property> properties = testType.getProperties();
    assertEquals(1, properties.size());
    ComplexProperty propComplex = (ComplexProperty) properties.get(0);
    assertEquals("MyComplexProperty", propComplex.getName());
    assertEquals(GeneratedNamesComplexTestClass.class.getPackage().getName(), propComplex.getType().getNamespace());
    assertEquals(GeneratedNamesComplexTestClass.class.getSimpleName(), propComplex.getType().getName());


    ComplexType testComplexType = localAep.getComplexType(
        new FullQualifiedName(GeneratedNamesComplexTestClass.class.getPackage().getName(),
            GeneratedNamesComplexTestClass.class.getSimpleName()));
    assertNotNull("Requested entity not found.", testComplexType);
    assertEquals("GeneratedNamesComplexTestClass", testComplexType.getName());
    assertNull("This should not have a base type", testComplexType.getBaseType());
  }

  @Test
  public void defaultNamespaceGenerationComplexType() throws ODataException {
    Collection<Class<?>> localAnnotatedClasses = new ArrayList<Class<?>>();
    localAnnotatedClasses.add(GeneratedNamesComplexTestClass.class);
    AnnotationEdmProvider localAep = new AnnotationEdmProvider(localAnnotatedClasses);
    // validate
    ComplexType testType = localAep.getComplexType(new FullQualifiedName(
        GeneratedNamesComplexTestClass.class.getPackage().getName(),
        GeneratedNamesComplexTestClass.class.getSimpleName()));
    assertNotNull("Requested entity not found.", testType);
    assertEquals("GeneratedNamesComplexTestClass", testType.getName());
    assertNull("This should not have a base type", testType.getBaseType());
  }

  @Test
  public void defaultContainerNameGeneration() throws ODataException {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    AnnotationEdmProvider localAep =
        new AnnotationEdmProvider((Collection) Arrays.asList(GeneratedNamesTestClass.class));

    EntityContainerInfo containerInfo = localAep.getEntityContainerInfo(null);
    assertNotNull(containerInfo);
    assertEquals("DefaultContainer", containerInfo.getName());
  }

  @Test
  public void defaultNamespaceDefined() throws ODataException {
    Collection<Class<?>> localAnnotatedClasses = new ArrayList<Class<?>>();
    localAnnotatedClasses.add(DefinedNamesTestClass.class);
    AnnotationEdmProvider localAep = new AnnotationEdmProvider(localAnnotatedClasses);
    // validate
    EntityType testClass = localAep.getEntityType(new FullQualifiedName("MyTestNamespace",
        DefinedNamesTestClass.class.getSimpleName()));
    assertNotNull("Requested entity not found.", testClass);
    assertEquals("DefinedNamesTestClass", testClass.getName());
    assertNull("This should not have a base type", testClass.getBaseType());
  }

  @Test
  public void defaultContainerNameDefined() throws ODataException {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    AnnotationEdmProvider localAep = new AnnotationEdmProvider((Collection) Arrays.asList(DefinedNamesTestClass.class));

    EntityContainerInfo containerInfo = localAep.getEntityContainerInfo(null);
    assertNotNull(containerInfo);
    assertEquals("MyTestContainer", containerInfo.getName());
  }

  @Test
  public void loadAnnotatedClassesFromPackage() throws Exception {
    AnnotationEdmProvider localAep = new AnnotationEdmProvider(TEST_MODEL_PACKAGE);

    // validate employee
    EntityType employee = localAep.getEntityType(new FullQualifiedName(ModelSharedConstants.NAMESPACE_1, "Employee"));
    assertEquals("Employee", employee.getName());
    final List<PropertyRef> employeeKeys = employee.getKey().getKeys();
    assertEquals(1, employeeKeys.size());
    assertEquals("EmployeeId", employeeKeys.get(0).getName());
    assertEquals(6, employee.getProperties().size());
    assertEquals(3, employee.getNavigationProperties().size());

    List<Schema> schemas = localAep.getSchemas();
    assertEquals(1, schemas.size());
    EntityContainerInfo info = localAep.getEntityContainerInfo(ModelSharedConstants.CONTAINER_1);
    assertTrue(info.isDefaultEntityContainer());
  }

  @Test
  public void annotationProviderBasic() throws Exception {
    assertNotNull(aep);

    List<Schema> schemas = aep.getSchemas();
    assertEquals(1, schemas.size());
    EntityContainerInfo info = aep.getEntityContainerInfo(ModelSharedConstants.CONTAINER_1);
    assertTrue(info.isDefaultEntityContainer());

    FunctionImport funImp = aep.getFunctionImport(ModelSharedConstants.CONTAINER_1, "NoImport");
    assertNull(funImp);

    final FullQualifiedName associationFqn = new FullQualifiedName(
        ModelSharedConstants.NAMESPACE_1, "NoAssociation");
    Association noAssociation = aep.getAssociation(associationFqn);
    assertNull(noAssociation);

    AssociationSet noAssociationSet = aep.getAssociationSet(
        ModelSharedConstants.CONTAINER_1, associationFqn, "NoSrc", "NoSrcEntity");
    assertNull(noAssociationSet);

    AssociationSet asBuildingRooms = aep.getAssociationSet(
        ModelSharedConstants.CONTAINER_1, defaultFqn("BuildingRooms"), "Buildings", "r_Building");
    assertNotNull(asBuildingRooms);
    assertEquals("Buildings", asBuildingRooms.getEnd1().getEntitySet());
    assertEquals("r_Building", asBuildingRooms.getEnd1().getRole());
    assertEquals("Rooms", asBuildingRooms.getEnd2().getEntitySet());
    assertEquals("r_Rooms", asBuildingRooms.getEnd2().getRole());
  }

  @Test
  public void annotationProviderGetDefaultContainer() throws Exception {
    assertNotNull(aep);

    List<Schema> schemas = aep.getSchemas();
    assertEquals(1, schemas.size());
    EntityContainerInfo info = aep.getEntityContainerInfo(null);
    assertTrue(info.isDefaultEntityContainer());
    assertEquals(ModelSharedConstants.CONTAINER_1, info.getName());
  }

  @Test
  public void schemaBasic() throws Exception {
    assertNotNull(aep);

    List<Schema> schemas = aep.getSchemas();
    assertEquals(1, schemas.size());

    Schema schema = schemas.get(0);
    List<EntityContainer> containers = schema.getEntityContainers();
    assertEquals(1, containers.size());
    EntityContainer container = containers.get(0);
    assertEquals(ModelSharedConstants.CONTAINER_1, container.getName());
    final List<EntitySet> entitySets = container.getEntitySets();
    assertEquals(6, entitySets.size());

    List<Association> associations = schema.getAssociations();
    assertEquals(5, associations.size());
    for (Association association : associations) {
      assertNotNull(association.getName());
      validateAssociation(association);
    }
  }

  private FullQualifiedName defaultFqn(final String name) {
    return new FullQualifiedName(ModelSharedConstants.NAMESPACE_1, name);
  }

  private void validateAssociation(final Association association) {
    String name = association.getName();
    if (name.equals("r_Employees_2_r_Room")) {
      validateAssociation(association,
          "r_Room", EdmMultiplicity.ONE, defaultFqn("Room"),
          "r_Employees", EdmMultiplicity.MANY, defaultFqn("Employee"));
    } else if (name.equals("BuildingRooms")) {
      validateAssociation(association,
          "r_Building", EdmMultiplicity.ONE, defaultFqn("Building"),
          "r_Rooms", EdmMultiplicity.MANY, defaultFqn("Room"));
    } else if (name.equals("ManagerEmployees")) {
      validateAssociation(association,
          "r_Manager", EdmMultiplicity.ONE, defaultFqn("Manager"),
          "r_Employees", EdmMultiplicity.MANY, defaultFqn("Employee"));
    } else if (name.equals("TeamEmployees")) {
      validateAssociation(association,
          "r_Team", EdmMultiplicity.ONE, defaultFqn("Team"),
          "r_Employees", EdmMultiplicity.MANY, defaultFqn("Employee"));
    } else if (name.equals("Team_2_r_SubTeam")) {
      validateAssociation(association,
          "Team", EdmMultiplicity.ONE, defaultFqn("Team"),
          "r_SubTeam", EdmMultiplicity.ONE, defaultFqn("Team"));
    } else {
      fail("Got unknown association to validate with name '" + name + "'.");
    }
  }

  private void validateAssociation(final Association association,
      final String fromRole, final EdmMultiplicity fromMulti, final FullQualifiedName fromType,
      final String toRole, final EdmMultiplicity toMulti, final FullQualifiedName toType) {

    AssociationEnd[] ends = new AssociationEnd[] { association.getEnd1(), association.getEnd2() };
    for (AssociationEnd associationEnd : ends) {
      if (associationEnd.getRole().equals(fromRole)) {
        validateAssociationEnd(associationEnd, fromRole, fromMulti, fromType);
      } else if (associationEnd.getRole().equals(toRole)) {
        validateAssociationEnd(associationEnd, toRole, toMulti, toType);
      } else {
        fail("Unexpected navigation end '" + associationEnd.getRole()
            + "' for association with name '" + association.getName() + "'.");
      }
    }
  }

  private void validateAssociationEnd(final AssociationEnd associationEnd,
      final String role, final EdmMultiplicity multiplicity, final FullQualifiedName type) {
    assertEquals(role, associationEnd.getRole());
    assertEquals(multiplicity, associationEnd.getMultiplicity());
    assertEquals(type, associationEnd.getType());
  }

  @Test
  public void entitySetTeams() throws Exception {
    // validate teams
    EntitySet teams = aep.getEntitySet(ModelSharedConstants.CONTAINER_1, "Teams");
    assertEquals(ModelSharedConstants.NAMESPACE_1, teams.getEntityType().getNamespace());
    assertEquals("Team", teams.getEntityType().getName());
  }

  @Test
  public void entityTypeEmployee() throws Exception {
    // validate employee
    EntityType employee = aep.getEntityType(new FullQualifiedName(ModelSharedConstants.NAMESPACE_1, "Employee"));
    assertEquals("Employee", employee.getName());
    final List<PropertyRef> employeeKeys = employee.getKey().getKeys();
    assertEquals(1, employeeKeys.size());
    assertEquals("EmployeeId", employeeKeys.get(0).getName());
    assertEquals(6, employee.getProperties().size());
    assertEquals(3, employee.getNavigationProperties().size());
    Property name = getProperty(employee, "EmployeeName");
    assertEquals(Integer.valueOf(20), name.getFacets().getMaxLength());

    for (NavigationProperty navigationProperty : employee.getNavigationProperties()) {
      if (navigationProperty.getName().equals("ne_Manager")) {
        validateNavProperty(navigationProperty, "ManagerEmployees", "r_Employees", "r_Manager");
      } else if (navigationProperty.getName().equals("ne_Team")) {
        validateNavProperty(navigationProperty, "TeamEmployees", "r_Employees", "r_Team");
      } else if (navigationProperty.getName().equals("ne_Room")) {
        validateNavProperty(navigationProperty, "r_Employees_2_r_Room", "r_Employees", "r_Room");
      } else {
        fail("Got unexpected navigation property with name '" + navigationProperty.getName() + "'.");
      }
    }
  }


  @Test
  public void facetsTest() throws Exception {
    EntityType employee = aep.getEntityType(new FullQualifiedName(ModelSharedConstants.NAMESPACE_1, "Employee"));
    assertEquals("Employee", employee.getName());
    Property name = getProperty(employee, "EmployeeName");
    assertEquals(Integer.valueOf(20), name.getFacets().getMaxLength());
    assertNull(name.getFacets().getConcurrencyMode());
    assertTrue(name.getFacets().isNullable());
    Property id = getProperty(employee, "EmployeeId");
    assertFalse(id.getFacets().isNullable());

    ComplexType city = aep.getComplexType(new FullQualifiedName(ModelSharedConstants.NAMESPACE_1, "c_City"));
    Property postalCode = getProperty(city.getProperties(), "PostalCode");
    assertEquals(Integer.valueOf(5), postalCode.getFacets().getMaxLength());

    EntityType room = aep.getEntityType(new FullQualifiedName(ModelSharedConstants.NAMESPACE_1, "Room"));
    Property version = getProperty(room, "Version");
    assertEquals(Integer.valueOf(0), version.getFacets().getScale());
    assertEquals(Integer.valueOf(0), version.getFacets().getPrecision());
    assertEquals(EdmConcurrencyMode.Fixed, version.getFacets().getConcurrencyMode());
  }

  @Test
  public void entityTypeTeam() throws Exception {
    // validate team
    EntityType team = aep.getEntityType(new FullQualifiedName(ModelSharedConstants.NAMESPACE_1, "Team"));
    assertEquals("Team", team.getName());
    assertEquals("Base", team.getBaseType().getName());
    assertEquals(ModelSharedConstants.NAMESPACE_1, team.getBaseType().getNamespace());

    assertEquals(1, team.getProperties().size());
    assertEquals(2, team.getNavigationProperties().size());
    NavigationProperty navPropTeamEmployess = team.getNavigationProperties().get(0);
    validateNavProperty(navPropTeamEmployess, "TeamEmployees", "r_Team", "r_Employees");
    NavigationProperty navPropTeamTeam = team.getNavigationProperties().get(1);
    validateNavProperty(navPropTeamTeam, "Team_2_r_SubTeam", "Team", "r_SubTeam");
  }

  @Test
  public void entityTypePhotoWithTwoKeyProperties() throws Exception {
    // validate team
    EntityType photo = aep.getEntityType(new FullQualifiedName(ModelSharedConstants.NAMESPACE_1, "Photo"));
    assertEquals("Photo", photo.getName());
    final List<Property> properties = photo.getProperties();
    assertEquals(5, properties.size());
    assertTrue(containsProperty(properties, "Name"));
    assertTrue(containsProperty(properties, "ImageFormat"));
    assertTrue(containsProperty(properties, "MimeType"));
    assertTrue(containsProperty(properties, "ImageUrl"));
    assertTrue(containsProperty(properties, "Image"));
    assertFalse(photo.isAbstract());
    assertTrue(photo.isHasStream());
    assertEquals("MimeType",photo.getMapping().getMediaResourceMimeTypeKey());
    assertEquals("ImageUrl",photo.getMapping().getMediaResourceSourceKey());

    Key photoKey = photo.getKey();
    List<PropertyRef> keyReferences = photoKey.getKeys();
    assertEquals(2, keyReferences.size());
    PropertyRef name = getPropertyRef(keyReferences, "Name");
    assertEquals("Name", name.getName());
    PropertyRef imageFormat = getPropertyRef(keyReferences, "ImageFormat");
    assertEquals("ImageFormat", imageFormat.getName());

    // assertEquals(0, photo.getNavigationProperties().size());
    assertNull(photo.getNavigationProperties());
  }

  @Test
  public void entityTypeAbstractBaseType() throws Exception {
    // validate employee
    EntityType baseType = aep.getEntityType(new FullQualifiedName(ModelSharedConstants.NAMESPACE_1, "Base"));
    assertEquals("Base", baseType.getName());
    final List<PropertyRef> keys = baseType.getKey().getKeys();
    assertEquals(1, keys.size());
    assertEquals("Id", keys.get(0).getName());
    assertEquals(2, baseType.getProperties().size());
    assertTrue(baseType.isAbstract());

    // validate base for team
    EntityType team = aep.getEntityType(new FullQualifiedName(ModelSharedConstants.NAMESPACE_1, "Team"));
    assertEquals("Team", team.getName());
    assertEquals("Base", team.getBaseType().getName());
    assertEquals(ModelSharedConstants.NAMESPACE_1, team.getBaseType().getNamespace());
  }

  @Test
  public void complexTypeLocation() throws Exception {
    // validate employee
    EntityType employee = aep.getEntityType(new FullQualifiedName(ModelSharedConstants.NAMESPACE_1, "Employee"));
    final List<Property> properties = employee.getProperties();
    Property location = null;
    for (Property property : properties) {
      if (property.getName().equals("Location")) {
        location = property;
      }
    }
    assertNotNull(location);
    assertEquals("Location", location.getName());

    // validate location complex type
    ComplexType locationType = aep.getComplexType(
        new FullQualifiedName(ModelSharedConstants.NAMESPACE_1, "c_Location"));
    assertEquals("c_Location", locationType.getName());
    assertEquals(2, locationType.getProperties().size());
  }

  @Test
  public void entityTypeRoomWithNavigation() throws Exception {
    // validate employee
    EntityType room = aep.getEntityType(new FullQualifiedName(ModelSharedConstants.NAMESPACE_1, "Room"));
    assertEquals("Room", room.getName());
    assertEquals("Base", room.getBaseType().getName());
    assertEquals(2, room.getProperties().size());
    final List<NavigationProperty> navigationProperties = room.getNavigationProperties();
    assertEquals(2, navigationProperties.size());

    for (NavigationProperty navigationProperty : navigationProperties) {
      if (navigationProperty.getName().equals("nr_Employees")) {
        validateNavProperty(navigationProperty, "r_Employees_2_r_Room", "r_Room", "r_Employees");
      } else if (navigationProperty.getName().equals("nr_Building")) {
        validateNavProperty(navigationProperty, "BuildingRooms", "r_Rooms", "r_Building");
      } else {
        fail("Got unexpected navigation property with name '" + navigationProperty.getName() + "'.");
      }
    }
  }

  private void validateNavProperty(final NavigationProperty navigationProperty, final String name,
      final String relationship, final String fromRole, final String toRole) {
    if (name != null) {
      assertEquals(name, navigationProperty.getName());
    }
    FullQualifiedName fqn = new FullQualifiedName(ModelSharedConstants.NAMESPACE_1, relationship);
    assertEquals("Wrong relationship for navigation property.", fqn, navigationProperty.getRelationship());
    assertEquals("Wrong fromRole for navigation property.", fromRole, navigationProperty.getFromRole());
    assertEquals("Wrong toRole for navigation property.", toRole, navigationProperty.getToRole());
  }

  private void validateNavProperty(final NavigationProperty navigationProperty,
      final String relationship, final String fromRole, final String toRole) {
    validateNavProperty(navigationProperty, null, relationship, fromRole, toRole);
  }

  private boolean containsProperty(final List<Property> properties, final String propertyName) {
    return getProperty(properties, propertyName) != null;
  }

  private Property getProperty(EntityType entity, String propertyName) {
    return getProperty(entity.getProperties(), propertyName);
  }

  private Property getProperty(final List<Property> properties, final String name) {
    for (Property property : properties) {
      if (name.equals(property.getName())) {
        return property;
      }
    }
    fail("Requested property with name '" + name + "' not available.");
    return null;
  }

  private PropertyRef getPropertyRef(final List<PropertyRef> properties, final String name) {
    for (PropertyRef property : properties) {
      if (name.equals(property.getName())) {
        return property;
      }
    }
    return null;
  }
}
