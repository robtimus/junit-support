/*
 * TypeAndArgument.java
 * Copyright 2020 Rob Spoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.robtimus.junit.support.reflection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A wrapper for a type and a value for that type.
 *
 * @author Rob Spoor
 */
public final class TypeAndArgument {

    private static final Map<Class<?>, Object> DEFAULT_VALUES;

    static {
        Map<Class<?>, Object> defaultValues = new HashMap<>();
        defaultValues.put(boolean.class, false);
        defaultValues.put(char.class, '\0');
        defaultValues.put(byte.class, (byte) 0);
        defaultValues.put(short.class, (short) 0);
        defaultValues.put(int.class, 0);
        defaultValues.put(long.class, 0L);
        defaultValues.put(float.class, 0F);
        defaultValues.put(double.class, 0D);
        DEFAULT_VALUES = Collections.unmodifiableMap(defaultValues);
    }

    private final Class<?> type;
    private final Object value;

    TypeAndArgument(Class<?> type, Object value) {
        this.type = Objects.requireNonNull(type);
        this.value = value;
    }

    TypeAndArgument(Class<?> type) {
        this.type = Objects.requireNonNull(type);
        this.value = defaultValue(type);
    }

    /**
     * Returns the type.
     *
     * @return The type.
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Returns the value for the type.
     *
     * @return The value for the type.
     */
    public Object getValue() {
        return value;
    }

    static Object defaultValue(Class<?> type) {
        return DEFAULT_VALUES.getOrDefault(type, null);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TypeAndArgument other = (TypeAndArgument) obj;
        return type.equals(other.type)
                && Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        int hash = type.hashCode();
        hash = 31 * hash + Objects.hashCode(value);
        return hash;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return getClass().getSimpleName()
                + "[type=" + type
                + ",value=" + value
                + "]";
    }
}
