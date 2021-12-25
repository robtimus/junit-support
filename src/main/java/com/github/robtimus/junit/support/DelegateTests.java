/*
 * DelegateTests.java
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

package com.github.robtimus.junit.support;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Base interface for testing that methods delegate to another object of the same type.
 *
 * @author Rob Spoor
 * @param <T> The type of object to test.
 */
public interface DelegateTests<T> {

    /**
     * Returns the type to delegate to.
     *
     * @return The type to delegate to.
     */
    Class<T> delegateType();

    /**
     * Creates the object to test.
     *
     * @param delegate The delegate to test against.
     * @return The created object.
     */
    T wrap(T delegate);

    /**
     * Returns the delegate methods to test.
     * <p>
     * Note: the result should <b>not</b> include a delegate method for {@link Object#equals(Object)}, {@link Object#hashCode()}, private methods,
     * static methods or final methods.
     *
     * @return A stream with the delegate methods to test.
     */
    Stream<DelegateMethod<T>> methods();

    /**
     * For each method returned by the object returned by {@link #methods()}, test that the result of {@link #wrap(Object)} delegates to its argument.
     *
     * @return A stream with the tests, one per method.
     */
    @TestFactory
    @DisplayName("delegates")
    default Stream<DynamicTest> testDelegates() {
        Class<T> delegateType = delegateType();
        return methods().map(m -> m.asDynamicTest(delegateType, this::wrap));
    }

    /**
     * A representation of a method that, when in invoked on an instance, delegates to another instance of the same type.
     *
     * @author Rob Spoor
     * @param <T> The type of object the method can be invoked on.
     */
    final class DelegateMethod<T> {

        private final String displayName;
        private final int modifiers;
        private final Consumer<T> action;

        private DelegateMethod(String displayName, int modifiers, Consumer<T> action) {
            this.displayName = displayName;
            this.modifiers = modifiers;
            this.action = action;
        }

        private DelegateMethod(String name, Class<?>[] parameterTypes, int modifiers, Consumer<T> action) {
            this.displayName = getDisplayName(name, parameterTypes);
            this.modifiers = modifiers;
            this.action = action;
        }

        @SuppressWarnings("nls")
        private static String getDisplayName(String name, Class<?>[] parameterTypes) {
            return Arrays.stream(parameterTypes)
                    .map(DelegateMethod::getTypeName)
                    .collect(Collectors.joining(", ", name + "(", ")"));
        }

        @SuppressWarnings("nls")
        private static String getTypeName(Class<?> parameterType) {
            if (parameterType.isArray()) {
                return getTypeName(parameterType.getComponentType()) + "[]";
            }
            return parameterType.getSimpleName();
        }

        private DynamicTest asDynamicTest(Class<T> delegateType, UnaryOperator<T> wrapper) {
            return dynamicTest(displayName, () -> {
                validate();

                T delegate = mock(delegateType);
                action.accept(wrapper.apply(delegate));
                action.accept(verify(delegate));
            });
        }

        private void validate() {
            validateNotEquals();
            validateNotHashCode();
            validateModifiers();
        }

        @SuppressWarnings("nls")
        private void validateModifiers() {
            assertFalse(Modifier.isPrivate(modifiers) || Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers),
                    () -> String.format("Method %s: private, static or final not supported", displayName));
        }

        @SuppressWarnings("nls")
        private void validateNotEquals() {
            assertFalse("equals(Object)".equals(displayName), "equals(Object) not supported");
        }

        @SuppressWarnings("nls")
        private void validateNotHashCode() {
            assertFalse("hashCode()".equals(displayName), "hashCode() not supported");
        }

        private static Method getMethod(Class<?> type, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
            NoSuchMethodException exception = null;

            try {
                // this will also take care of any interface method
                return type.getMethod(name, parameterTypes);
            } catch (NoSuchMethodException e) {
                exception = e;
            }

            Class<?> c = type;
            while (c != null) {
                try {
                    return type.getDeclaredMethod(name, parameterTypes);
                } catch (@SuppressWarnings("unused") NoSuchMethodException e) {
                    // ignore
                }
                c = c.getSuperclass();
            }
            throw exception;
        }
    }

    /**
     * Returns a delegate method for a method without arguments.
     *
     * @param name The name for the method.
     * @return A delegate method for the method with the given name and no arguments.
     */
    default DelegateMethod<T> method(String name) {
        Method method = assertDoesNotThrow(() -> DelegateMethod.getMethod(delegateType(), name));

        String displayName = name + "()"; //$NON-NLS-1$
        int modifiers = method.getModifiers();
        Consumer<T> action = t -> assertDoesNotThrow(() -> method.invoke(t));

        return new DelegateMethod<>(displayName, modifiers, action);
    }

    /**
     * Returns a delegate method for a method.
     * <p>
     * The method will be invoked with default arguments; {@code 0} for {@code int}, {@code false} for {@code boolean}, {@code null} for objects, etc.
     * This may cause an error if the result of the method is invoked on an object that performs some input validation before delegating.
     * Use {@link #method(String, Parameter...)} instead if the default arguments are not sufficient.
     *
     * @param name The name for the method.
     * @param parameterTypes The parameter types for the method.
     * @return A delegate method for the method with the given name and parameter types.
     */
    default DelegateMethod<T> method(String name, Class<?>... parameterTypes) {
        Method method = assertDoesNotThrow(() -> DelegateMethod.getMethod(delegateType(), name, parameterTypes));

        Object[] args = Arrays.stream(parameterTypes)
                .map(t -> Parameter.DEFAULT_VALUES.getOrDefault(t, null))
                .toArray();

        int modifiers = method.getModifiers();
        Consumer<T> action = t -> assertDoesNotThrow(() -> method.invoke(t, args));

        return new DelegateMethod<>(name, parameterTypes, modifiers, action);
    }

    /**
     * Returns a delegate  method for a method.
     *
     * @param name The name for the method.
     * @param parameters The parameters for the method.
     * @return A delegate method for the method with the given name and parameters.
     */
    default DelegateMethod<T> method(String name, Parameter... parameters) {
        Class<?>[] parameterTypes = Arrays.stream(parameters)
                .map(p -> p.type)
                .toArray(Class[]::new);

        Method method = assertDoesNotThrow(() -> DelegateMethod.getMethod(delegateType(), name, parameterTypes));

        Object[] args = Arrays.stream(parameters)
                .map(p -> p.value)
                .toArray();

        int modifiers = method.getModifiers();
        Consumer<T> action = t -> assertDoesNotThrow(() -> method.invoke(t, args));

        return new DelegateMethod<>(name, parameterTypes, modifiers, action);
    }

    /**
     * A representation of a method parameter.
     * It wraps the parameter type and value to use for the parameter when invoking the method the parameter is for.
     *
     * @author Rob Spoor
     */
    final class Parameter {

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

        private Parameter(Class<?> type, Object value) {
            this.type = Objects.requireNonNull(type);
            this.value = value;
        }
    }

    /**
     * Creates a new method parameter.
     * This is shorthand for {@link #parameter(Class, Object) parameter(value.getClass(), value)}.
     *
     * @param <U> The parameter type.
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    default <U> Parameter parameter(U value) {
        return new Parameter(value.getClass(), value);
    }

    /**
     * Creates a new method parameter.
     * This is shorthand for {@link #parameter(Class, Object) parameter(type, defaultValue)} where {@code defaultValue} is the default value for the
     * given type: {@code 0} for {@code int}, {@code false} for {@code boolean}, {@code null} for objects, etc.
     *
     * @param type The parameter type.
     * @return The created method parameter.
     */
    default Parameter parameter(Class<?> type) {
        return new Parameter(type, Parameter.DEFAULT_VALUES.getOrDefault(type, null));
    }

    /**
     * Creates a new method parameter.
     *
     * @param <U> The parameter type.
     * @param type The parameter type.
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    default <U> Parameter parameter(Class<U> type, U value) {
        return new Parameter(type, value);
    }

    /**
     * Creates a new {@code boolean} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    default Parameter parameter(boolean value) {
        return new Parameter(boolean.class, value);
    }

    /**
     * Creates a new {@code char} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    default Parameter parameter(char value) {
        return new Parameter(char.class, value);
    }

    /**
     * Creates a new {@code byte} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    default Parameter parameter(byte value) {
        return new Parameter(byte.class, value);
    }

    /**
     * Creates a new {@code short} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    default Parameter parameter(short value) {
        return new Parameter(short.class, value);
    }

    /**
     * Creates a new {@code int} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    default Parameter parameter(int value) {
        return new Parameter(int.class, value);
    }

    /**
     * Creates a new {@code long} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    default Parameter parameter(long value) {
        return new Parameter(long.class, value);
    }

    /**
     * Creates a new {@code float} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    default Parameter parameter(float value) {
        return new Parameter(float.class, value);
    }

    /**
     * Creates a new {@code double} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    default Parameter parameter(double value) {
        return new Parameter(double.class, value);
    }
}
