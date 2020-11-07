/*
 * MethodParameter.java
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

import java.util.Objects;

/**
 * A wrapper for a method parameter type and the value for that parameter.
 *
 * @author Rob Spoor
 */
public final class MethodParameter {

    private final Class<?> type;
    private final Object value;

    private MethodParameter(Class<?> type, Object value) {
        this.type = Objects.requireNonNull(type);
        this.value = value;
    }

    /**
     * Returns the parameter type.
     *
     * @return The parameter type.
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Returns the value for the parameter.
     *
     * @return The value for the parameter.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Creates a new method parameter.
     * This is shorthand for {@link #parameter(Class, Object) parameter(value.getClass(), value)}.
     *
     * @param <T> The parameter type.
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    public static <T> MethodParameter parameter(T value) {
        return new MethodParameter(value.getClass(), value);
    }

    /**
     * Creates a new method parameter.
     *
     * @param <T> The parameter type.
     * @param type The parameter type.
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    public static <T> MethodParameter parameter(Class<T> type, T value) {
        return new MethodParameter(type, value);
    }

    /**
     * Creates a new {@code boolean} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    public static MethodParameter booleanParameter(boolean value) {
        return new MethodParameter(boolean.class, value);
    }

    /**
     * Creates a new {@code char} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    public static MethodParameter charParameter(char value) {
        return new MethodParameter(char.class, value);
    }

    /**
     * Creates a new {@code byte} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    public static MethodParameter byteParameter(byte value) {
        return new MethodParameter(byte.class, value);
    }

    /**
     * Creates a new {@code short} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    public static MethodParameter shortParameter(short value) {
        return new MethodParameter(short.class, value);
    }

    /**
     * Creates a new {@code int} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    public static MethodParameter intParameter(int value) {
        return new MethodParameter(int.class, value);
    }

    /**
     * Creates a new {@code long} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    public static MethodParameter longParameter(long value) {
        return new MethodParameter(long.class, value);
    }

    /**
     * Creates a new {@code float} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    public static MethodParameter floatParameter(float value) {
        return new MethodParameter(float.class, value);
    }

    /**
     * Creates a new {@code double} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    public static MethodParameter doubleParameter(double value) {
        return new MethodParameter(double.class, value);
    }
}
