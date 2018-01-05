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
package org.apache.olingo.odata2.ref.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.olingo.odata2.api.commons.HttpContentType;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.exception.ODataNotFoundException;
import org.apache.olingo.odata2.api.exception.ODataNotImplementedException;
import org.apache.olingo.odata2.ref.model.Building;
import org.apache.olingo.odata2.ref.model.DataContainer;
import org.apache.olingo.odata2.ref.model.Employee;
import org.apache.olingo.odata2.ref.model.Location;
import org.apache.olingo.odata2.ref.model.Manager;
import org.apache.olingo.odata2.ref.model.Photo;
import org.apache.olingo.odata2.ref.model.Room;
import org.apache.olingo.odata2.ref.model.Team;

/**
 * Data for the reference scenario
 * 
 */
public class ScenarioDataSource {

  private static final String ENTITYSET_1_1 = "Employees";
  private static final String ENTITYSET_1_2 = "Teams";
  private static final String ENTITYSET_1_3 = "Rooms";
  private static final String ENTITYSET_1_4 = "Managers";
  private static final String ENTITYSET_1_5 = "Buildings";
  private static final String ENTITYSET_2_1 = "Photos";

  private final DataContainer dataContainer;

  public ScenarioDataSource(final DataContainer dataContainer) {
    this.dataContainer = dataContainer;
  }

  public List<?> readData(final EdmEntitySet entitySet) throws ODataNotImplementedException, ODataNotFoundException,
      EdmException {
    if (ENTITYSET_1_1.equals(entitySet.getName())) {
      return Arrays.asList(dataContainer.getEmployees().toArray());
    } else if (ENTITYSET_1_2.equals(entitySet.getName())) {
      return Arrays.asList(dataContainer.getTeams().toArray());
    } else if (ENTITYSET_1_3.equals(entitySet.getName())) {
      return Arrays.asList(dataContainer.getRooms().toArray());
    } else if (ENTITYSET_1_4.equals(entitySet.getName())) {
      return Arrays.asList(dataContainer.getManagers().toArray());
    } else if (ENTITYSET_1_5.equals(entitySet.getName())) {
      return Arrays.asList(dataContainer.getBuildings().toArray());
    } else if (ENTITYSET_2_1.equals(entitySet.getName())) {
      return Arrays.asList(dataContainer.getPhotos().toArray());
    } else {
      throw new ODataNotImplementedException();
    }
  }

  public Object readData(final EdmEntitySet entitySet, final Map<String, Object> keys)
      throws ODataNotImplementedException, ODataNotFoundException, EdmException {
    if (ENTITYSET_1_1.equals(entitySet.getName())) {
      for (final Employee employee : dataContainer.getEmployees()) {
        if (employee.getId().equals(keys.get("EmployeeId"))) {
          return employee;
        }
      }
      throw new ODataNotFoundException(ODataNotFoundException.ENTITY);

    } else if (ENTITYSET_1_2.equals(entitySet.getName())) {
      for (final Team team : dataContainer.getTeams()) {
        if (team.getId().equals(keys.get("Id"))) {
          return team;
        }
      }
      throw new ODataNotFoundException(ODataNotFoundException.ENTITY);

    } else if (ENTITYSET_1_3.equals(entitySet.getName())) {
      for (final Room room : dataContainer.getRooms()) {
        if (room.getId().equals(keys.get("Id"))) {
          return room;
        }
      }
      throw new ODataNotFoundException(ODataNotFoundException.ENTITY);

    } else if (ENTITYSET_1_4.equals(entitySet.getName())) {
      for (final Manager manager : dataContainer.getManagers()) {
        if (manager.getId().equals(keys.get("EmployeeId"))) {
          return manager;
        }
      }
      throw new ODataNotFoundException(ODataNotFoundException.ENTITY);

    } else if (ENTITYSET_1_5.equals(entitySet.getName())) {
      for (final Building building : dataContainer.getBuildings()) {
        if (building.getId().equals(keys.get("Id"))) {
          return building;
        }
      }
      throw new ODataNotFoundException(ODataNotFoundException.ENTITY);

    } else if (ENTITYSET_2_1.equals(entitySet.getName())) {
      for (final Photo photo : dataContainer.getPhotos()) {
        if (photo.getId() == (Integer) keys.get("Id")
            && photo.getType().equals(keys.get("Type"))) {
          return photo;
        }
      }
      throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
    }

    throw new ODataNotImplementedException();
  }

  public Object readRelatedData(final EdmEntitySet sourceEntitySet, final Object sourceData,
      final EdmEntitySet targetEntitySet, final Map<String, Object> targetKeys) throws ODataNotImplementedException,
      ODataNotFoundException, EdmException {
    if (ENTITYSET_1_1.equals(targetEntitySet.getName())) {
      List<?> data = Collections.emptyList();
      if (ENTITYSET_1_2.equals(sourceEntitySet.getName())) {
        data = ((Team) sourceData).getEmployees();
      } else if (ENTITYSET_1_3.equals(sourceEntitySet.getName())) {
        data = ((Room) sourceData).getEmployees();
      } else if (ENTITYSET_1_4.equals(sourceEntitySet.getName())) {
        data = ((Manager) sourceData).getEmployees();
      }

      if (data.isEmpty()) {
        throw new ODataNotFoundException(null);
      }
      if (targetKeys.isEmpty()) {
        return Arrays.asList(data.toArray());
      } else {
        for (final Object employee : data) {
          if (((Employee) employee).getId().equals(targetKeys.get("EmployeeId"))) {
            return employee;
          }
        }
      }
      throw new ODataNotFoundException(ODataNotFoundException.ENTITY);

    } else if (ENTITYSET_1_2.equals(targetEntitySet.getName())) {
      if (((Employee) sourceData).getTeam() == null) {
        throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
      } else {
        return ((Employee) sourceData).getTeam();
      }

    } else if (ENTITYSET_1_3.equals(targetEntitySet.getName())) {
      if (ENTITYSET_1_1.equals(sourceEntitySet.getName())) {
        if (((Employee) sourceData).getRoom() == null) {
          throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
        } else {
          return ((Employee) sourceData).getRoom();
        }
      } else if (ENTITYSET_1_5.equals(sourceEntitySet.getName())) {
        List<Room> data = ((Building) sourceData).getRooms();
        if (data.isEmpty()) {
          throw new ODataNotFoundException(null);
        }
        if (targetKeys.isEmpty()) {
          return Arrays.asList(data.toArray());
        } else {
          for (final Object room : data) {
            if (((Room) room).getId().equals(targetKeys.get("Id"))) {
              return room;
            }
          }
        }
        throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
      }
      throw new ODataNotImplementedException();

    } else if (ENTITYSET_1_4.equals(targetEntitySet.getName())) {
      if (((Employee) sourceData).getManager() == null) {
        throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
      } else {
        return ((Employee) sourceData).getManager();
      }

    } else if (ENTITYSET_1_5.equals(targetEntitySet.getName())) {
      if (((Room) sourceData).getBuilding() == null) {
        throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
      } else {
        return ((Room) sourceData).getBuilding();
      }

    } else {
      throw new ODataNotImplementedException();
    }
  }

  public Object readData(final EdmFunctionImport function, final Map<String, Object> parameters,
      final Map<String, Object> keys) throws ODataNotImplementedException, ODataNotFoundException, EdmException {
    if (function.getName().equals("EmployeeSearch")) {
      if (parameters.get("q") == null) {
        throw new ODataNotFoundException(null);
      } else {
        final List<Employee> found = searchEmployees((String) parameters.get("q"));
        if (keys.isEmpty()) {
          return found;
        } else {
          for (final Employee employee : found) {
            if (employee.getId().equals(keys.get("EmployeeId"))) {
              return employee;
            }
          }
        }
        throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
      }

    } else if (function.getName().equals("AllLocations")) {
      return Arrays.asList(getLocations().keySet().toArray());

    } else if (function.getName().equals("AllUsedRoomIds")) {
      List<String> data = new ArrayList<String>();
      for (final Room room : dataContainer.getRooms()) {
        if (!room.getEmployees().isEmpty()) {
          data.add(room.getId());
        }
      }
      if (data.isEmpty()) {
        throw new ODataNotFoundException(null);
      } else {
        return data;
      }

    } else if (function.getName().equals("MaximalAge")) {
      return getOldestEmployee().getAge();

    } else if (function.getName().equals("MostCommonLocation")) {
      return getMostCommonLocation();

    } else if (function.getName().equals("ManagerPhoto")) {
      if (parameters.get("Id") == null) {
        throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
      }
      for (final Manager manager : dataContainer.getManagers()) {
        if (manager.getId().equals(parameters.get("Id"))) {
          return new BinaryData(manager.getImage(), manager.getImageType());
        }
      }
      throw new ODataNotFoundException(ODataNotFoundException.ENTITY);

    } else if (function.getName().equals("OldestEmployee")) {
      return getOldestEmployee();

    } else {
      throw new ODataNotImplementedException();
    }
  }

  private List<Employee> searchEmployees(final String search) {
    List<Employee> employees = new ArrayList<Employee>();
    for (final Employee employee : dataContainer.getEmployees()) {
      if (employee.getEmployeeName().contains(search)
          || employee.getLocation() != null
          && (employee.getLocation().getCity().getCityName().contains(search)
              || employee.getLocation().getCity().getPostalCode().contains(search)
              || employee.getLocation().getCountry().contains(search))) {
        employees.add(employee);
      }
    }
    return employees;
  }

  private Map<Location, Integer> getLocations() throws ODataNotFoundException {
    Map<Location, Integer> locations = new LinkedHashMap<Location, Integer>();
    for (Employee employee : dataContainer.getEmployees()) {
      if (employee.getLocation() != null && employee.getLocation().getCity() != null) {
        boolean found = false;
        for (final Location location : locations.keySet()) {
          if (employee.getLocation().getCity().getPostalCode() == location.getCity().getPostalCode()
              && employee.getLocation().getCity().getCityName() == location.getCity().getCityName()
              && employee.getLocation().getCountry() == location.getCountry()) {
            found = true;
            locations.put(location, locations.get(location) + 1);
          }
        }
        if (!found) {
          locations.put(employee.getLocation(), 1);
        }
      }
    }
    if (locations.isEmpty()) {
      throw new ODataNotFoundException(null);
    } else {
      return locations;
    }
  }

  private Location getMostCommonLocation() throws ODataNotFoundException {
    Integer count = 0;
    Location location = null;
    for (Entry<Location, Integer> entry : getLocations().entrySet()) {
      if (entry.getValue() > count) {
        count = entry.getValue();
        location = entry.getKey();
      }
    }
    return location;
  }

  private Employee getOldestEmployee() {
    Employee oldestEmployee = null;
    for (final Employee employee : dataContainer.getEmployees()) {
      if (oldestEmployee == null || employee.getAge() > oldestEmployee.getAge()) {
        oldestEmployee = employee;
      }
    }
    return oldestEmployee;
  }

  public BinaryData readBinaryData(final EdmEntitySet entitySet, final Object mediaLinkEntryData)
      throws ODataNotImplementedException, ODataNotFoundException, EdmException, ODataApplicationException {
    if (mediaLinkEntryData == null) {
      throw new ODataNotFoundException(null);
    }

    if (ENTITYSET_1_1.equals(entitySet.getName()) || ENTITYSET_1_4.equals(entitySet.getName())) {
      final Employee employee = (Employee) mediaLinkEntryData;
      if (employee.getImage() == null) {
        throw new ODataNotFoundException(null);
      }
      return new BinaryData(employee.getImage(), employee.getImageType());
    } else if (ENTITYSET_2_1.equals(entitySet.getName())) {
      final Photo photo = (Photo) mediaLinkEntryData;
      return new BinaryData(photo.getImage(), photo.getImageType());
    } else {
      throw new ODataNotImplementedException();
    }
  }

  public void
      writeBinaryData(final EdmEntitySet entitySet, final Object mediaLinkEntryData, final BinaryData binaryData)
          throws ODataNotImplementedException, ODataNotFoundException, EdmException, ODataApplicationException {
    if (mediaLinkEntryData == null) {
      throw new ODataNotFoundException(null);
    }

    if (ENTITYSET_1_1.equals(entitySet.getName()) || ENTITYSET_1_4.equals(entitySet.getName())) {
      final Employee employee = (Employee) mediaLinkEntryData;
      employee.setImage(binaryData.getData());
      employee.setImageType(binaryData.getMimeType());
    //Storing the binary data to be used for comparison in the tests
      Util.getInstance().setBinaryContent(employee.getImage());
    } else if (ENTITYSET_2_1.equals(entitySet.getName())) {
      final Photo photo = (Photo) mediaLinkEntryData;
      photo.setImage(binaryData.getData());
      photo.setImageType(binaryData.getMimeType());
    } else {
      throw new ODataNotImplementedException();
    }
  }

  public Object newDataObject(final EdmEntitySet entitySet) throws ODataNotImplementedException, EdmException {
    if (ENTITYSET_1_1.equals(entitySet.getName())) {
      Employee employee = dataContainer.createEmployee();
      employee.setAge(0);
      employee.setLocation(new Location(null, null, null));
      return employee;
    } else if (ENTITYSET_1_2.equals(entitySet.getName())) {
      return dataContainer.createTeam();
    } else if (ENTITYSET_1_3.equals(entitySet.getName())) {
      Room room = dataContainer.createRoom();
      room.setSeats(0);
      room.setVersion(0);
      return room;
    } else if (ENTITYSET_1_4.equals(entitySet.getName())) {
      Manager manager = dataContainer.createManager();
      manager.setAge(0);
      manager.setLocation(new Location(null, null, null));
      return manager;
    } else if (ENTITYSET_1_5.equals(entitySet.getName())) {
      return dataContainer.createBuilding();
    } else if (ENTITYSET_2_1.equals(entitySet.getName())) {
      return dataContainer.createPhoto(HttpContentType.APPLICATION_OCTET_STREAM);
    } else {
      throw new ODataNotImplementedException();
    }
  }

  public void deleteData(final EdmEntitySet entitySet, final Map<String, Object> keys)
      throws ODataNotImplementedException, ODataNotFoundException, EdmException, ODataApplicationException {
    final Object data = readData(entitySet, keys);

    if (ENTITYSET_1_1.equals(entitySet.getName()) || ENTITYSET_1_4.equals(entitySet.getName())) {
      if (data instanceof Manager) {
        for (Employee employee : ((Manager) data).getEmployees()) {
          employee.setManager(null);
        }
      }
      final Employee employee = (Employee) data;
      if (employee.getManager() != null) {
        employee.getManager().getEmployees().remove(employee);
      }
      if (employee.getTeam() != null) {
        employee.getTeam().getEmployees().remove(employee);
      }
      if (employee.getRoom() != null) {
        employee.getRoom().getEmployees().remove(employee);
      }
      if (data instanceof Manager) {
        dataContainer.getManagers().remove(data);
      }
      dataContainer.getEmployees().remove(data);

    } else if (ENTITYSET_1_2.equals(entitySet.getName())) {
      for (Employee employee : ((Team) data).getEmployees()) {
        employee.setTeam(null);
      }
      dataContainer.getTeams().remove(data);

    } else if (ENTITYSET_1_3.equals(entitySet.getName())) {
      for (Employee employee : ((Room) data).getEmployees()) {
        employee.setRoom(null);
      }
      if (((Room) data).getBuilding() != null) {
        ((Room) data).getBuilding().getRooms().remove(data);
      }
      dataContainer.getRooms().remove(data);

    } else if (ENTITYSET_1_5.equals(entitySet.getName())) {
      for (Room room : ((Building) data).getRooms()) {
        room.setBuilding(null);
      }
      dataContainer.getBuildings().remove(data);

    } else if (ENTITYSET_2_1.equals(entitySet.getName())) {
      dataContainer.getPhotos().remove(data);

    } else {
      throw new ODataNotImplementedException();
    }
  }

  public void createData(final EdmEntitySet entitySet, final Object data) throws ODataNotImplementedException,
      EdmException, ODataApplicationException {
    if (ENTITYSET_1_1.equals(entitySet.getName())) {
      dataContainer.getEmployees().add((Employee) data);
    } else if (ENTITYSET_1_2.equals(entitySet.getName())) {
      dataContainer.getTeams().add((Team) data);
    } else if (ENTITYSET_1_3.equals(entitySet.getName())) {
      dataContainer.getRooms().add((Room) data);
    } else if (ENTITYSET_1_4.equals(entitySet.getName())) {
      dataContainer.getManagers().add((Manager) data);
    } else if (ENTITYSET_1_5.equals(entitySet.getName())) {
      dataContainer.getBuildings().add((Building) data);
    } else if (ENTITYSET_2_1.equals(entitySet.getName())) {
      dataContainer.getPhotos().add((Photo) data);
    } else {
      throw new ODataNotImplementedException();
    }
  }

  public void deleteRelation(final EdmEntitySet sourceEntitySet, final Object sourceData,
      final EdmEntitySet targetEntitySet, final Map<String, Object> targetKeys) throws ODataNotImplementedException,
      ODataNotFoundException, EdmException, ODataApplicationException {
    if (ENTITYSET_1_1.equals(targetEntitySet.getName())) {
      if (ENTITYSET_1_2.equals(sourceEntitySet.getName())) {
        for (Iterator<Employee> iterator = ((Team) sourceData).getEmployees().iterator(); iterator.hasNext();) {
          final Employee employee = iterator.next();
          if (employee.getId().equals(targetKeys.get("EmployeeId"))) {
            employee.setTeam(null);
            iterator.remove();
          }
        }
      } else if (ENTITYSET_1_3.equals(sourceEntitySet.getName())) {
        for (Iterator<Employee> iterator = ((Room) sourceData).getEmployees().iterator(); iterator.hasNext();) {
          final Employee employee = iterator.next();
          if (employee.getId().equals(targetKeys.get("EmployeeId"))) {
            employee.setRoom(null);
            iterator.remove();
          }
        }
      } else if (ENTITYSET_1_4.equals(sourceEntitySet.getName())) {
        for (Iterator<Employee> iterator = ((Manager) sourceData).getEmployees().iterator(); iterator.hasNext();) {
          final Employee employee = iterator.next();
          if (employee.getId().equals(targetKeys.get("EmployeeId"))) {
            employee.setManager(null);
            iterator.remove();
          }
        }
      }

    } else if (ENTITYSET_1_2.equals(targetEntitySet.getName())) {
      ((Employee) sourceData).getTeam().getEmployees().remove(sourceData);
      ((Employee) sourceData).setTeam(null);

    } else if (ENTITYSET_1_3.equals(targetEntitySet.getName())) {
      if (ENTITYSET_1_1.equals(sourceEntitySet.getName())) {
        ((Employee) sourceData).getRoom().getEmployees().remove(sourceData);
        ((Employee) sourceData).setRoom(null);
      } else if (ENTITYSET_1_5.equals(sourceEntitySet.getName())) {
        for (Iterator<Room> iterator = ((Building) sourceData).getRooms().iterator(); iterator.hasNext();) {
          final Room room = iterator.next();
          if (room.getId().equals(targetKeys.get("Id"))) {
            room.setBuilding(null);
            iterator.remove();
          }
        }
      }

    } else if (ENTITYSET_1_4.equals(targetEntitySet.getName())) {
      ((Employee) sourceData).getManager().getEmployees().remove(sourceData);
      ((Employee) sourceData).setManager(null);

    } else if (ENTITYSET_1_5.equals(targetEntitySet.getName())) {
      ((Room) sourceData).getBuilding().getRooms().remove(sourceData);
      ((Room) sourceData).setBuilding(null);

    } else {
      throw new ODataNotImplementedException();
    }
  }

  public void writeRelation(final EdmEntitySet sourceEntitySet, final Object sourceData,
      final EdmEntitySet targetEntitySet, final Map<String, Object> targetKeys) throws ODataNotImplementedException,
      ODataNotFoundException, EdmException, ODataApplicationException {
    if (ENTITYSET_1_1.equals(targetEntitySet.getName())) {
      final Employee employee = (Employee) readData(targetEntitySet, targetKeys);
      if (ENTITYSET_1_2.equals(sourceEntitySet.getName())) {
        if (employee.getTeam() != null) {
          employee.getTeam().getEmployees().remove(employee);
        }
        employee.setTeam((Team) sourceData);
        ((Team) sourceData).getEmployees().add(employee);
      } else if (ENTITYSET_1_3.equals(sourceEntitySet.getName())) {
        if (employee.getRoom() != null) {
          employee.getRoom().getEmployees().remove(employee);
        }
        employee.setRoom((Room) sourceData);
        ((Room) sourceData).getEmployees().add(employee);
      } else if (ENTITYSET_1_4.equals(sourceEntitySet.getName())) {
        if (employee.getManager() != null) {
          employee.getManager().getEmployees().remove(employee);
        }
        employee.setManager((Manager) sourceData);
        ((Manager) sourceData).getEmployees().add(employee);
      }

    } else if (ENTITYSET_1_2.equals(targetEntitySet.getName())) {
      final Team team = (Team) readData(targetEntitySet, targetKeys);
      if (((Employee) sourceData).getTeam() != null) {
        ((Employee) sourceData).getTeam().getEmployees().remove(sourceData);
      }
      ((Employee) sourceData).setTeam(team);
      team.getEmployees().add((Employee) sourceData);

    } else if (ENTITYSET_1_3.equals(targetEntitySet.getName())) {
      final Room room = (Room) readData(targetEntitySet, targetKeys);
      if (ENTITYSET_1_1.equals(sourceEntitySet.getName())) {
        if (((Employee) sourceData).getRoom() != null) {
          ((Employee) sourceData).getRoom().getEmployees().remove(sourceData);
        }
        ((Employee) sourceData).setRoom(room);
        room.getEmployees().add((Employee) sourceData);
      } else if (ENTITYSET_1_5.equals(sourceEntitySet.getName())) {
        if (room.getBuilding() != null) {
          room.getBuilding().getRooms().remove(room);
        }
        room.setBuilding((Building) sourceData);
        ((Building) sourceData).getRooms().add(room);
      }

    } else if (ENTITYSET_1_4.equals(targetEntitySet.getName())) {
      final Manager manager = (Manager) readData(targetEntitySet, targetKeys);
      if (((Employee) sourceData).getManager() != null) {
        ((Employee) sourceData).getManager().getEmployees().remove(sourceData);
      }
      ((Employee) sourceData).setManager(manager);
      manager.getEmployees().add((Employee) sourceData);

    } else if (ENTITYSET_1_5.equals(targetEntitySet.getName())) {
      final Building building = (Building) readData(targetEntitySet, targetKeys);
      if (((Room) sourceData).getBuilding() != null) {
        ((Room) sourceData).getBuilding().getRooms().remove(sourceData);
      }
      ((Room) sourceData).setBuilding(building);
      building.getRooms().add((Room) sourceData);

    } else {
      throw new ODataNotImplementedException();
    }
  }

  /**
   * Container to store binary data (as byte array) and the associated MIME type.
   */
  public static class BinaryData {
    private final byte[] data;
    private final String mimeType;

    public BinaryData(final byte[] data, final String mimeType) {
      this.data = data;
      this.mimeType = mimeType;
    }

    public byte[] getData() {
      return data;
    }

    public String getMimeType() {
      return mimeType;
    }

    @Override
    public String toString() {
      return "data=" + Arrays.toString(data) + ", mimeType=" + mimeType;
    }
  }

}
