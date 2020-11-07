/*
 * InvokableMethod.java
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A representation of a method that can be invoked. This is basically a wrapper around {@link Method} and arguments to
 * {@link Method#invoke(Object, Object...) invoke} the method with.
 *
 * @author Rob Spoor
 */
public final class InvokableMethod {

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

    private final Method method;
    private final Object[] arguments;

    private InvokableMethod(Method method, Object[] arguments) {
        this.method = Objects.requireNonNull(method);
        this.arguments = Objects.requireNonNull(arguments);
    }

    /**
     * Returns the backing method.
     *
     * @return The backing method.
     */
    public Method getMethod() {
        return method;
    }

    Object[] getArguments() {
        return arguments;
    }

    /**
     * Invokes the method on an instance.
     *
     * @param instance The instance to invoke the method on.
     */
    public void invoke(Object instance) {
        assertDoesNotThrow(() -> method.invoke(instance, arguments));
    }

    /**
     * Creates a new invokable method.
     * <p>
     * The method will be invoked with default arguments; {@code 0} for {@code int}, {@code false} for {@code boolean}, {@code null} for objects, etc.
     * This may cause an error if these do not meet the preconditions of the instance to invoke the method on.
     * Use {@link #of(Method, Object...)} instead if the default arguments are not sufficient.
     *
     * @param method The backing method.
     * @return The created invokable method.
     */
    public static InvokableMethod of(Method method) {
        Object[] arguments = Arrays.stream(method.getParameterTypes())
                .map(c -> DEFAULT_VALUES.getOrDefault(c, null))
                .toArray();
        return of(method, arguments);
    }

    /**
     * Creates a new invokable method.
     * <p>
     * The given arguments must match the method's parameter types. If not, calling {@link #invoke(Object)} will fail.
     *
     * @param method The backing method.
     * @param arguments The arguments to invoke the method with.
     * @return The created invokable method.
     */
    public static InvokableMethod of(Method method, Object... arguments) {
        return new InvokableMethod(method, arguments);
    }

    /**
     * Creates a new invokable method.
     *
     * @param c The class that contains the method.
     * @param name The name of the method. This method should have no arguments.
     * @return The created invokable method.
     */
    public static InvokableMethod of(Class<?> c, String name) {
        Method method = assertDoesNotThrow(() -> c.getMethod(name));
        return of(method);
    }

    /**
     * Creates a new invokable method.
     * <p>
     * The method will be invoked with default arguments; {@code 0} for {@code int}, {@code false} for {@code boolean}, {@code null} for objects, etc.
     * This may cause an error if these do not meet the preconditions of the instance to invoke the method on.
     * Use {@link #of(Class, String, MethodParameter...)} instead if the default arguments are not sufficient.
     *
     * @param c The class that contains the method.
     * @param name The name of the method.
     * @param parameterTypes The parameter types of the method.
     * @return The created invokable method.
     */
    public static InvokableMethod of(Class<?> c, String name, Class<?>... parameterTypes) {
        Method method = assertDoesNotThrow(() -> c.getMethod(name, parameterTypes));
        return of(method);
    }

    /**
     * Creates a new invokable method.
     *
     * @param c The class that contains the method.
     * @param name The name of the method.
     * @param parameters The parameter types of the method and the values for these parameters.
     * @return The created invokable method.
     */
    public static InvokableMethod of(Class<?> c, String name, MethodParameter... parameters) {
        Class<?>[] parameterTypes = Arrays.stream(parameters)
                .map(MethodParameter::getType)
                .toArray(Class[]::new);
        Object[] arguments = Arrays.stream(parameters)
                .map(MethodParameter::getValue)
                .toArray();

        Method method = assertDoesNotThrow(() -> c.getMethod(name, parameterTypes));
        return of(method, arguments);
    }
}
