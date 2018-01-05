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
package org.apache.olingo.odata2.annotation.processor.core.datasource;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.olingo.odata2.annotation.processor.core.util.AnnotationHelper;
import org.apache.olingo.odata2.annotation.processor.core.util.AnnotationRuntimeException;
import org.apache.olingo.odata2.annotation.processor.core.util.ClassHelper;
import org.apache.olingo.odata2.api.annotation.edm.EdmKey;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;

/**
 *
 */
public class DataStore<T> {

  private static final AnnotationHelper ANNOTATION_HELPER = new AnnotationHelper();
  private final Map<KeyElement, T> dataStore;
  private final Class<T> dataTypeClass;
  private final KeyAccess keyAccess;

  private static class InMemoryDataStore {
    private static final Map<Class<?>, DataStore<?>> c2ds = new HashMap<Class<?>, DataStore<?>>();

    @SuppressWarnings("unchecked")
    static synchronized DataStore<?> getInstance(final Class<?> clz, final boolean createNewInstance)
        throws DataStoreException {
      DataStore<?> ds = c2ds.get(clz);
      if (createNewInstance || ds == null) {
        ds = new DataStore<Object>((Class<Object>) clz);
        c2ds.put(clz, ds);
      }
      return ds;
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> DataStore<T> createInMemory(final Class<T> clazz) throws DataStoreException {
    return (DataStore<T>) InMemoryDataStore.getInstance(clazz, true);
  }

  @SuppressWarnings("unchecked")
  public static <T> DataStore<T> createInMemory(final Class<T> clazz, final boolean keepExisting)
      throws DataStoreException {
    return (DataStore<T>) InMemoryDataStore.getInstance(clazz, !keepExisting);
  }

  private DataStore(final Map<KeyElement, T> wrapStore, final Class<T> clz) throws DataStoreException {
    dataStore = Collections.synchronizedMap(wrapStore);
    dataTypeClass = clz;
    keyAccess = new KeyAccess(clz);
  }

  private DataStore(final Class<T> clz) throws DataStoreException {
    this(new HashMap<KeyElement, T>(), clz);
  }

  public Class<T> getDataTypeClass() {
    return dataTypeClass;
  }

  public String getEntityTypeName() {
    return ANNOTATION_HELPER.extractEntityTypeName(dataTypeClass);
  }

  public T createInstance() {
    try {
      return dataTypeClass.newInstance();
    } catch (InstantiationException e) {
      throw new AnnotationRuntimeException("Unable to create instance of class '" + dataTypeClass + "'.", e);
    } catch (IllegalAccessException e) {
      throw new AnnotationRuntimeException("Unable to create instance of class '" + dataTypeClass + "'.", e);
    }
  }

  public T read(final T obj) {
    KeyElement objKeys = getKeys(obj);
    return dataStore.get(objKeys);
  }

  public Collection<T> read() {
    return Collections.unmodifiableCollection(dataStore.values());
  }

  public T create(final T object) throws DataStoreException {
    KeyElement keyElement = getKeys(object);
    return create(object, keyElement);
  }

  /**
   * Store an entity, preserving any existing keys if possible. If the combination of
   * existing and generated keys would produce a duplicate entry, replace all keys.
   */
  private T create(final T object, final KeyElement keyElement) throws DataStoreException {
    synchronized (dataStore) {
      final boolean replaceKeys = dataStore.containsKey(keyElement);
      if (keyElement.keyValuesMissing() || replaceKeys) {
        KeyElement newKey = createSetAndGetKeys(object, replaceKeys);
        return this.create(object, newKey);
      }
      dataStore.put(keyElement, object);
    }
    return object;
  }

  public T update(final T object) {
    KeyElement keyElement = getKeys(object);
    synchronized (dataStore) {
      dataStore.remove(keyElement);
      dataStore.put(keyElement, object);
    }
    return object;
  }

  public T delete(final T object) {
    KeyElement keyElement = getKeys(object);
    synchronized (dataStore) {
      return dataStore.remove(keyElement);
    }
  }

  /**
   * Are the key values equal for both instances.
   * If all compared key values are <code>null</code> this also means equal.
   * 
   * @param first first instance to check for key equal
   * @param second second instance to check for key equal
   * @return <code>true</code> if object instance have equal keys set.
   */
  public boolean isKeyEqual(final T first, final T second) {
    KeyElement firstKeys = getKeys(first);
    KeyElement secondKeys = getKeys(second);

    return firstKeys.equals(secondKeys);
  }

  /**
   * Are the key values equal for both instances.
   * If all compared key values are <code>null</code> this also means equal.
   * Before object (keys) are compared it is validated that both object instance are NOT null
   * and that both are from the same class as this {@link DataStore} (see {@link #dataTypeClass}).
   * For the equal check on {@link #dataTypeClass} instances without validation see {@link #isKeyEqual(Object, Object)}.
   * 
   * @param first first instance to check for key equal
   * @param second second instance to check for key equal
   * @return <code>true</code> if object instance have equal keys set.
   */
  @SuppressWarnings("unchecked")
  public boolean isKeyEqualChecked(final Object first, final Object second) throws DataStoreException {
    if (first == null || second == null) {
      throw new DataStoreException("Tried to compare null values which is not allowed.");
    } else if (first.getClass() != dataTypeClass) {
      throw new DataStoreException("First value is no instance from required class '" + dataTypeClass + "'.");
    } else if (second.getClass() != dataTypeClass) {
      throw new DataStoreException("Second value is no instance from required class '" + dataTypeClass + "'.");
    }

    return isKeyEqual((T) first, (T) second);
  }

  private class KeyElement {
    private int cachedHashCode = 42;
    private final List<Object> keyValues;

    public KeyElement(final int size) {
      keyValues = new ArrayList<Object>(size);
    }

    private void addValue(final Object keyValue) {
      keyValues.add(keyValue);
      cachedHashCode = 89 * cachedHashCode + (keyValue != null ? keyValue.hashCode() : 0);
    }

    boolean keyValuesMissing() {
      return keyValues.contains(null);
    }

    @Override
    public int hashCode() {
      return cachedHashCode;
    }

    @Override
    public boolean equals(final Object obj) {
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      @SuppressWarnings("unchecked")
      final KeyElement other = (KeyElement) obj;
      if (this.keyValues != other.keyValues && (this.keyValues == null || !this.keyValues.equals(other.keyValues))) {
        return false;
      }
      return true;
    }

    @Override
    public String toString() {
      return "KeyElement{" + "cachedHashCode=" + cachedHashCode + ", keyValues=" + keyValues + '}';
    }
  }

  private class KeyAccess {
    final List<Field> keyFields;
    final AtomicInteger idCounter = new AtomicInteger(1);

    KeyAccess(final Class<?> clazz) throws DataStoreException {
      keyFields = ANNOTATION_HELPER.getAnnotatedFields(clazz, EdmKey.class);
      if (keyFields.isEmpty()) {
        throw new DataStoreException("No EdmKey annotated fields found for class " + clazz);
      }
    }

    KeyElement getKeyValues(final T object) {
      KeyElement keyElement = new KeyElement(keyFields.size());
      for (Field field : keyFields) {
        Object keyValue = ClassHelper.getFieldValue(object, field);
        keyElement.addValue(keyValue);
      }

      return keyElement;
    }

    KeyElement createSetAndGetKeys(final T object, boolean replaceKeys) throws DataStoreException {
      KeyElement keyElement = new KeyElement(keyFields.size());
      for (Field field : keyFields) {
        Object key = ClassHelper.getFieldValue(object, field);
        if (key == null || replaceKeys) {
          key = createKey(field);
          ClassHelper.setFieldValue(object, field, key);
        }
        keyElement.addValue(key);
      }

      return keyElement;
    }

    private Object createKey(final Field field) {
      Class<?> type = field.getType();

      if (type == String.class) {
        return String.valueOf(idCounter.getAndIncrement());
      } else if (type == Integer.class || type == int.class) {
        return Integer.valueOf(idCounter.getAndIncrement());
      } else if (type == Long.class || type == long.class) {
        return Long.valueOf(idCounter.getAndIncrement());
      } else if (type == UUID.class) {
        return UUID.randomUUID();
      }

      throw new UnsupportedOperationException("Automated key generation for type '" + type
          + "' is not supported (caused on field '" + field + "').");
    }
  }

  private KeyElement getKeys(final T object) {
    return keyAccess.getKeyValues(object);
  }

  private KeyElement createSetAndGetKeys(final T object, boolean replaceKeys) throws DataStoreException {
    return keyAccess.createSetAndGetKeys(object, replaceKeys);
  }

  public static class DataStoreException extends ODataApplicationException {
    private static final long serialVersionUID = 42L;

    public DataStoreException(final String message) {
      this(message, null);
    }

    public DataStoreException(final String message, final Throwable cause) {
      super(message, Locale.ENGLISH, cause);
    }
  }
}
