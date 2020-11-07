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
import java.util.function.BiPredicate;
import java.util.function.Predicate;
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
     * Returns an object that will return all methods to test.
     *
     * @return An object that will return all methods to test.
     */
    MethodFinder methods();

    /**
     * For each method returned by the object returned by {@link #methods()}, test that the result of {@link #wrap(Object)} delegates to its argument.
     *
     * @return A stream with the tests, one per method.
     */
    @TestFactory
    @DisplayName("delegates")
    default Stream<DynamicTest> testDelegates() {
        Class<T> delegateType = delegateType();
        return methods().findMethods(delegateType)
                .distinct()
                .map(i -> i.asTest(delegateType, this::wrap));
    }

    /**
     * Returns a method finder for one specific method.
     * If this method cannot be found, {@link #testDelegates()} will fail in its entirety.
     *
     * @param name The name for the method.
     * @return A method finder that will only find the method with the given name without parameters.
     */
    static MethodFinder method(String name) {
        Objects.requireNonNull(name);
        return c -> Stream.of(InvokableMethod.of(c, name));
    }

    /**
     * Returns a method finder for one specific method.
     * If this method cannot be found, {@link #testDelegates()} will fail in its entirety.
     *
     * @param name The name for the method.
     * @param parameters The parameters for the method.
     * @return A method finder that will only find the method with the given name and parameters.
     */
    static MethodFinder method(String name, MethodParameter... parameters) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(parameters);
        return c -> Stream.of(InvokableMethod.of(c, name, parameters));
    }

    /**
     * Returns a method finder for one specific method.
     * If this method cannot be found, {@link #testDelegates()} will fail in its entirety.
     * <p>
     * The method will be invoked with default arguments; {@code 0} for {@code int}, {@code false} for {@code boolean}, {@code null} for objects, etc.
     * This may cause an error if the result of {@link #wrap(Object)} performs some input validation before delegating.
     * Use {@link #method(String, MethodParameter...)} instead if the default arguments are not sufficient.
     *
     * @param name The name for the method.
     * @param parameterTypes The parameter types for the method.
     * @return A method finder that will only find the method with the given name and parameter types.
     */
    static MethodFinder method(String name, Class<?>... parameterTypes) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(parameterTypes);
        return c -> Stream.of(InvokableMethod.of(c, name, parameterTypes));
    }

    /**
     * Returns a method finder for all overloads of a method.
     * If there are no such methods, the method finder will lead to no additional tests for {@link #testDelegates()}.
     * This could result in no tests at all.
     * <p>
     * The methods will be invoked with default arguments; {@code 0} for {@code int}, {@code false} for {@code boolean}, {@code null} for objects,
     * etc. This may cause an error if the result of {@link #wrap(Object)} performs some input validation before delegating.
     * Use {@link #method(String, MethodParameter...)} instead if the default arguments are not sufficient. These methods can be filtered out using
     * {@link MethodFinder#without(Predicate)}.
     *
     * @param name The name for the methods.
     * @return A method finder that will only find methods with the given name.
     */
    static MethodFinder methods(String name) {
        Objects.requireNonNull(name);
        return methodsFilteredBy(m -> name.equals(m.getName()));
    }

    /**
     * Returns a method finder for all methods.
     * <p>
     * The methods will be invoked with default arguments; {@code 0} for {@code int}, {@code false} for {@code boolean}, {@code null} for objects,
     * etc. This may cause an error if the result of {@link #wrap(Object)} performs some input validation before delegating.
     * Use {@link #method(String, MethodParameter...)} instead if the default arguments are not sufficient. These methods can be filtered out using
     * {@link MethodFinder#without(Predicate)}.
     *
     * @return A method finder for all methods.
     */
    static MethodFinder allMethods() {
        return methodsFilteredBy(m -> true);
    }

    /**
     * Returns a method finder that will only find methods that are declared in the type to search.
     * <p>
     * The methods will be invoked with default arguments; {@code 0} for {@code int}, {@code false} for {@code boolean}, {@code null} for objects,
     * etc. This may cause an error if the result of {@link #wrap(Object)} performs some input validation before delegating.
     * Use {@link #method(String, MethodParameter...)} instead if the default arguments are not sufficient. These methods can be filtered out using
     * {@link MethodFinder#without(Predicate)}.
     *
     * @return A method finder that will only find methods that are declared in the type to search.
     */
    static MethodFinder methodsDeclaredByType() {
        return methodsFilteredBy((c, m) -> m.getDeclaringClass() == c);
    }

    /**
     * Returns a method finder that will find all methods that match a predicate.
     * <p>
     * The methods will be invoked with default arguments; {@code 0} for {@code int}, {@code false} for {@code boolean}, {@code null} for objects,
     * etc. This may cause an error if the result of {@link #wrap(Object)} performs some input validation before delegating.
     * Use {@link #method(String, MethodParameter...)} instead if the default arguments are not sufficient. These methods should be filtered out by
     * the given predicate.
     *
     * @param filter The predicate to use.
     * @return A method finder that will only find methods that match the given predicate.
     */
    static MethodFinder methodsFilteredBy(Predicate<? super Method> filter) {
        Objects.requireNonNull(filter);
        return c -> Arrays.stream(c.getMethods())
                .filter(m -> !Modifier.isStatic(m.getModifiers()))
                .filter(m -> !m.isSynthetic())
                .filter(filter)
                .map(InvokableMethod::of);
    }

    /**
     * Returns a method finder that will find all methods that match a predicate.
     * <p>
     * The methods will be invoked with default arguments; {@code 0} for {@code int}, {@code false} for {@code boolean}, {@code null} for objects,
     * etc. This may cause an error if the result of {@link #wrap(Object)} performs some input validation before delegating.
     * Use {@link #method(String, MethodParameter...)} instead if the default arguments are not sufficient. These methods should be filtered out by
     * the given predicate.
     *
     * @param filter The predicate to use.
     * @return A method finder that will only find methods that match the given predicate.
     */
    static MethodFinder methodsFilteredBy(BiPredicate<? super Class<?>, ? super Method> filter) {
        Objects.requireNonNull(filter);
        return c -> Arrays.stream(c.getMethods())
                .filter(m -> !Modifier.isStatic(m.getModifiers()))
                .filter(m -> !m.isSynthetic())
                .filter(m -> filter.test(c, m))
                .map(InvokableMethod::of);
    }

    /**
     * A functional interface for finding methods that will be used with {@link DelegateTests#testDelegates()}.
     *
     * @author Rob Spoor
     */
    @FunctionalInterface
    interface MethodFinder {

        /**
         * Returns a stream with methods that were found for a specific type.
         *
         * @param c The class to find methods in.
         * @return A stream with methods that were found in the given type.
         */
        Stream<InvokableMethod> findMethods(Class<?> c);

        /**
         * Combines this method finder with another. The result will find all methods that are found by either method finder.
         *
         * @param other The method finder to combine this method finder with.
         * @return The result of combining the two method finders.
         */
        default MethodFinder and(MethodFinder other) {
            Objects.requireNonNull(other);
            return type -> Stream.concat(findMethods(type), other.findMethods(type));
        }

        default MethodFinder without(Predicate<? super InvokableMethod> filter) {
            Objects.requireNonNull(filter);
            return type -> findMethods(type).filter(m -> !filter.test(m));
        }
    }

    /**
     * A representation of a method that can be invoked. This is basically a wrapper around {@link Method} and arguments to
     * {@link Method#invoke(Object, Object...) invoke} the method with.
     *
     * @author Rob Spoor
     */
    final class InvokableMethod {

        private static final Map<Class<?>, Object> DEFAULT_VALUES = getDefaultValues();

        private final Method method;
        private final Object[] arguments;

        private final String methodDisplayName;

        @SuppressWarnings("nls")
        private InvokableMethod(Method method, Object[] arguments) {
            this.method = Objects.requireNonNull(method);
            this.arguments = Objects.requireNonNull(arguments);

            methodDisplayName = Arrays.stream(method.getParameterTypes())
                    .map(Class::getTypeName)
                    .collect(Collectors.joining(", ", method.getName() + "(", ")"));
        }

        private static Map<Class<?>, Object> getDefaultValues() {
            Map<Class<?>, Object> defaultValues = new HashMap<>();
            defaultValues.put(boolean.class, false);
            defaultValues.put(char.class, '\0');
            defaultValues.put(byte.class, (byte) 0);
            defaultValues.put(short.class, (short) 0);
            defaultValues.put(int.class, 0);
            defaultValues.put(long.class, 0L);
            defaultValues.put(float.class, 0F);
            defaultValues.put(double.class, 0D);
            return Collections.unmodifiableMap(defaultValues);
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

        <T> DynamicTest asTest(Class<T> delegateType, UnaryOperator<T> wrapper) {
            return dynamicTest(methodDisplayName, () -> {
                T delegate = mock(delegateType);
                invoke(wrapper.apply(delegate));
                invoke(verify(delegate));
            });
        }

        /**
         * Creates a new invokable method.
         * <p>
         * The method will be invoked with default arguments; {@code 0} for {@code int}, {@code false} for {@code boolean}, {@code null} for objects,
         * etc. This may cause an error if these do not meet the preconditions of the instance to invoke the method on.
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
         * The method will be invoked with default arguments; {@code 0} for {@code int}, {@code false} for {@code boolean}, {@code null} for objects,
         * etc. This may cause an error if these do not meet the preconditions of the instance to invoke the method on.
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

    /**
     * Creates a new method parameter.
     * This is shorthand for {@link #parameter(Class, Object) parameter(value.getClass(), value)}.
     *
     * @param <T> The parameter type.
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    static <T> MethodParameter parameter(T value) {
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
    static <T> MethodParameter parameter(Class<T> type, T value) {
        return new MethodParameter(type, value);
    }

    /**
     * Creates a new {@code boolean} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    static MethodParameter booleanParameter(boolean value) {
        return new MethodParameter(boolean.class, value);
    }

    /**
     * Creates a new {@code char} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    static MethodParameter charParameter(char value) {
        return new MethodParameter(char.class, value);
    }

    /**
     * Creates a new {@code byte} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    static MethodParameter byteParameter(byte value) {
        return new MethodParameter(byte.class, value);
    }

    /**
     * Creates a new {@code short} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    static MethodParameter shortParameter(short value) {
        return new MethodParameter(short.class, value);
    }

    /**
     * Creates a new {@code int} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    static MethodParameter intParameter(int value) {
        return new MethodParameter(int.class, value);
    }

    /**
     * Creates a new {@code long} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    static MethodParameter longParameter(long value) {
        return new MethodParameter(long.class, value);
    }

    /**
     * Creates a new {@code float} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    static MethodParameter floatParameter(float value) {
        return new MethodParameter(float.class, value);
    }

    /**
     * Creates a new {@code double} method parameter.
     *
     * @param value The value for the parameter.
     * @return The created method parameter.
     */
    static MethodParameter doubleParameter(double value) {
        return new MethodParameter(double.class, value);
    }

    /**
     * A wrapper for a method parameter type and the value for that parameter.
     *
     * @author Rob Spoor
     */
    final class MethodParameter {

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
    }
}
