/*
 * MethodAndArguments.java
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
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.opentest4j.AssertionFailedError;

/**
 * A wrapper around a method and the arguments with which it can be {@link Method#invoke(Object, Object...) invoked}.
 *
 * @author Rob Spoor
 */
public final class MethodAndArguments {

    private final Method method;
    private final Object[] arguments;

    private final String methodDisplayName;

    @SuppressWarnings("nls")
    private MethodAndArguments(Method method, Object[] arguments) {
        this.method = Objects.requireNonNull(method);
        this.arguments = Objects.requireNonNull(arguments);

        methodDisplayName = Arrays.stream(method.getParameterTypes())
                .map(MethodAndArguments::getTypeName)
                .collect(Collectors.joining(", ", method.getName() + "(", ")"));
    }

    @SuppressWarnings("nls")
    private static String getTypeName(Class<?> parameterType) {
        if (parameterType.isArray()) {
            return getTypeName(parameterType.getComponentType()) + "[]";
        }
        return parameterType.getSimpleName();
    }

    /**
     * Returns the backing method.
     *
     * @return The backing method.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Returns the arguments for the method. Modifying the returned array will not alter this object.
     *
     * @return The arguments for the method.
     */
    public Object[] getArguments() {
        return arguments.clone();
    }

    /**
     * Invokes the method on an instance.
     *
     * @param instance The instance to invoke the method on.
     * @return The result of invoking the method.
     * @throws AssertionFailedError If the method could not be invoked.
     */
    public Object invoke(Object instance) {
        return assertDoesNotThrow(() -> method.invoke(instance, arguments));
    }

    /**
     * Returns a dynamic test based on this object.
     *
     * @param executor The object that performs the actual test.
     * @return A dynamic test that executes the given executor with this object as input.
     */
    public DynamicTest asTest(ThrowingConsumer<MethodAndArguments> executor) {
        return dynamicTest(methodDisplayName, () -> executor.accept(this));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MethodAndArguments other = (MethodAndArguments) obj;
        return methodDisplayName.equals(other.methodDisplayName)
                && Arrays.equals(arguments, other.arguments);
    }

    @Override
    public int hashCode() {
        int hash = methodDisplayName.hashCode();
        hash = 31 * hash + Arrays.hashCode(arguments);
        return hash;
    }

    @Override
    public String toString() {
        return methodDisplayName;
    }

    /**
     * Creates a new method-and-arguments wrapper.
     * <p>
     * The wrapper will use default arguments for the method's parameter types; {@code 0} for {@code int}, {@code false} for {@code boolean},
     * {@code null} for objects, etc. This may cause an error if these do not meet the preconditions of the instance to invoke the method on.
     * Use {@link #methodWithArguments(Method, Object...)} instead if the default arguments are not sufficient.
     *
     * @param method The backing method.
     * @return The created wrapper.
     */
    public static MethodAndArguments methodWithDefaultArguments(Method method) {
        Object[] arguments = Arrays.stream(method.getParameterTypes())
                .map(TypeAndArgument::defaultValue)
                .toArray();
        return new MethodAndArguments(method, arguments);
    }

    /**
     * Creates a new method-and-arguments wrapper.
     * <p>
     * The given arguments must match the method's parameter types. If not, calling {@link #invoke(Object)} will fail.
     *
     * @param method The backing method.
     * @param arguments The arguments to invoke the method with.
     * @return The created wrapper.
     */
    public static MethodAndArguments methodWithArguments(Method method, Object... arguments) {
        return new MethodAndArguments(method, arguments);
    }

    /**
     * Creates a new method-and-arguments wrapper for a public method without parameters.
     *
     * @param c The class that contains the method.
     * @param name The name of the method.
     * @return The created wrapper.
     */
    public static MethodAndArguments methodWithoutParameters(Class<?> c, String name) {
        Method method = assertDoesNotThrow(() -> c.getMethod(name));
        return methodWithDefaultArguments(method);
    }

    /**
     * Creates a new method-and-arguments wrapper for a public method.
     * <p>
     * The wrapper will use default arguments for the method's parameter types; {@code 0} for {@code int}, {@code false} for {@code boolean},
     * {@code null} for objects, etc. This may cause an error if these do not meet the preconditions of the instance to invoke the method on.
     * Use {@link #methodWithParameters(Class, String, TypeAndArgument...)} instead if the default arguments are not sufficient.
     *
     * @param c The class that contains the method.
     * @param name The name of the method.
     * @param parameterTypes The parameter types of the method.
     * @return The created wrapper.
     */
    public static MethodAndArguments methodWithParameters(Class<?> c, String name, Class<?>... parameterTypes) {
        Method method = assertDoesNotThrow(() -> c.getMethod(name, parameterTypes));
        return methodWithDefaultArguments(method);
    }

    /**
     * Creates a new method-and-arguments wrapper for a public method.
     *
     * @param c The class that contains the method.
     * @param name The name of the method.
     * @param parameters The parameter types of the method and the values for these parameters.
     * @return The created wrapper.
     */
    public static MethodAndArguments methodWithParameters(Class<?> c, String name, TypeAndArgument... parameters) {
        Class<?>[] parameterTypes = Arrays.stream(parameters)
                .map(TypeAndArgument::getType)
                .toArray(Class[]::new);
        Method method = assertDoesNotThrow(() -> c.getMethod(name, parameterTypes));

        Object[] arguments = Arrays.stream(parameters)
                .map(TypeAndArgument::getValue)
                .toArray();

        return methodWithArguments(method, arguments);
    }

    /**
     * Creates a new method parameter.
     * This is shorthand for {@link #parameter(Class, Object) parameter(value.getClass(), value)}.
     *
     * @param <T> The parameter type.
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    public static <T> TypeAndArgument parameter(T value) {
        return new TypeAndArgument(value.getClass(), value);
    }

    /**
     * Creates a new method parameter.
     * This is shorthand for {@link #parameter(Class, Object) parameter(type, defaultValue)} where {@code defaultValue} is the default value for the
     * given type: {@code 0} for {@code int}, {@code false} for {@code boolean}, {@code null} for objects, etc.
     *
     * @param type The parameter type.
     * @return The created method parameter.
     */
    public static TypeAndArgument parameter(Class<?> type) {
        return new TypeAndArgument(type);
    }

    /**
     * Creates a new method parameter.
     *
     * @param <T> The parameter type.
     * @param type The parameter type.
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    public static <T> TypeAndArgument parameter(Class<T> type, T value) {
        return new TypeAndArgument(type, value);
    }

    /**
     * Creates a new {@code boolean} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    public static TypeAndArgument booleanParameter(boolean value) {
        return new TypeAndArgument(boolean.class, value);
    }

    /**
     * Creates a new {@code char} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    public static TypeAndArgument charParameter(char value) {
        return new TypeAndArgument(char.class, value);
    }

    /**
     * Creates a new {@code byte} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    public static TypeAndArgument byteParameter(byte value) {
        return new TypeAndArgument(byte.class, value);
    }

    /**
     * Creates a new {@code short} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    public static TypeAndArgument shortParameter(short value) {
        return new TypeAndArgument(short.class, value);
    }

    /**
     * Creates a new {@code int} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    public static TypeAndArgument intParameter(int value) {
        return new TypeAndArgument(int.class, value);
    }

    /**
     * Creates a new {@code long} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    public static TypeAndArgument longParameter(long value) {
        return new TypeAndArgument(long.class, value);
    }

    /**
     * Creates a new {@code float} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    public static TypeAndArgument floatParameter(float value) {
        return new TypeAndArgument(float.class, value);
    }

    /**
     * Creates a new {@code double} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    public static TypeAndArgument doubleParameter(double value) {
        return new TypeAndArgument(double.class, value);
    }
}
