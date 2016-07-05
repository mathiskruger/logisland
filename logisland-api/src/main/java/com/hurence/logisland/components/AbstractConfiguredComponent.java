/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hurence.logisland.components;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractConfiguredComponent implements ConfigurableComponent, ConfiguredComponent {

    private final String id;
    private final ConfigurableComponent component;

    private final AtomicReference<String> name;
    private final AtomicReference<String> annotationData = new AtomicReference<>();

    private final Lock lock = new ReentrantLock();
    private final ConcurrentMap<PropertyDescriptor, String> properties = new ConcurrentHashMap<>();

    public AbstractConfiguredComponent(final ConfigurableComponent component, final String id) {
        this.id = id;
        this.component = component;
        this.name = new AtomicReference<>(component.getClass().getSimpleName());
    }

    @Override
    public String getIdentifier() {
        return id;
    }

    @Override
    public String getName() {
        return name.get();
    }

    @Override
    public void setName(final String name) {
        this.name.set(Objects.requireNonNull(name).intern());
    }

    @Override
    public String getAnnotationData() {
        return annotationData.get();
    }

    @Override
    public void setAnnotationData(final String data) {
        annotationData.set(data);
    }

    @Override
    public void setProperty(final String name, final String value) {
        if (null == name || null == value) {
            throw new IllegalArgumentException();
        }

        lock.lock();
        try {
            verifyModifiable();


            final PropertyDescriptor descriptor = component.getPropertyDescriptor(name);

            final String oldValue = properties.put(descriptor, value);
            if (!value.equals(oldValue)) {


                try {
                    component.onPropertyModified(descriptor, oldValue, value);
                } catch (final Exception e) {
                    // nothing really to do here...
                }
            }
        }catch (final Exception e) {
            // nothing really to do here...
        }
    }

    /**
     * Removes the property and value for the given property name if a
     * descriptor and value exists for the given name. If the property is
     * optional its value might be reset to default or will be removed entirely
     * if was a dynamic property.
     *
     * @param name the property to remove
     * @return true if removed; false otherwise
     * @throws IllegalArgumentException if the name is null
     */
    @Override
    public boolean removeProperty(final String name) {
        if (null == name) {
            throw new IllegalArgumentException();
        }

        lock.lock();
        try {
            verifyModifiable();


                final PropertyDescriptor descriptor = component.getPropertyDescriptor(name);
                String value = null;
                if (!descriptor.isRequired() && (value = properties.remove(descriptor)) != null) {



                    try {
                        component.onPropertyModified(descriptor, value, null);
                    } catch (final Exception e) {
                        // nothing really to do here...
                    }

                    return true;
                }
            }catch (final Exception e) {
            // nothing really to do here...
        }

        return false;
    }

    @Override
    public Map<PropertyDescriptor, String> getProperties() {

            final List<PropertyDescriptor> supported = component.getPropertyDescriptors();
            if (supported == null || supported.isEmpty()) {
                return Collections.unmodifiableMap(properties);
            } else {
                final Map<PropertyDescriptor, String> props = new LinkedHashMap<>();
                for (final PropertyDescriptor descriptor : supported) {
                    props.put(descriptor, null);
                }
                props.putAll(properties);
                return props;
            }

    }

    @Override
    public String getProperty(final PropertyDescriptor property) {
        return properties.get(property);
    }

    @Override
    public int hashCode() {
        return 273171 * id.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof ConfiguredComponent)) {
            return false;
        }

        final ConfiguredComponent other = (ConfiguredComponent) obj;
        return id.equals(other.getIdentifier());
    }

    @Override
    public String toString() {

            return component.toString();

    }


    @Override
    public PropertyDescriptor getPropertyDescriptor(final String name) {
            return component.getPropertyDescriptor(name);
    }

    @Override
    public void onPropertyModified(final PropertyDescriptor descriptor, final String oldValue, final String newValue) {
            component.onPropertyModified(descriptor, oldValue, newValue);
    }

    @Override
    public List<PropertyDescriptor> getPropertyDescriptors() {
            return component.getPropertyDescriptors();
    }

    @Override
    public boolean isValid() {

        return true;
    }

    @Override
    public Collection<ValidationResult> getValidationErrors() {
        return Collections.emptySet();
    }


    public abstract void verifyModifiable() throws IllegalStateException;


}