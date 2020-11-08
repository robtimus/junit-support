/*
 * MethodProvider.java
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

import static com.github.robtimus.junit.support.reflection.MethodAndArguments.methodWithParameters;
import static com.github.robtimus.junit.support.reflection.MethodAndArguments.methodWithoutParameters;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.jupiter.api.TestFactory;

/**
 * A provider for methods for dynamic tests.
 *
 * @author Rob Spoor
 */
@FunctionalInterface
public interface MethodProvider {

    /**
     * Returns the methods to use for a specific type.
     *
     * @param c The type for which to return methods to use.
     * @return A stream with the methods to use for the given type.
     */
    Stream<MethodAndArguments> methods(Class<?> c);

    /**
     * Combines this method provider with another. The result will return all methods that are returned by either method provider.
     *
     * @param other The method provider to combine this method provider with.
     * @return The result of combining the two method providers.
     */
    default MethodProvider and(MethodProvider other) {
        Objects.requireNonNull(other);
        return type -> Stream.concat(methods(type), other.methods(type));
    }

    /**
     * Returns a method provider that filters out results of this method provider.
     *
     * @param filter The filter to use. Any result matching this filter will be returned by the resulting method provider.
     * @return The result of applying the filter to this method provider.
     */
    default MethodProvider with(Predicate<? super Method> filter) {
        Objects.requireNonNull(filter);
        return type -> methods(type).filter(m -> filter.test(m.getMethod()));
    }

    /**
     * Returns a method provider that filters out results of this method provider.
     *
     * @param filter The filter to use. Any result matching this filter will be returned by the resulting method provider.
     * @return The result of applying the filter to this method provider.
     */
    default MethodProvider with(BiPredicate<? super Class<?>, ? super Method> filter) {
        Objects.requireNonNull(filter);
        return type -> methods(type).filter(m -> filter.test(type, m.getMethod()));
    }

    /**
     * Returns a method provider that filters out results of this method provider.
     *
     * @param filter The filter to use. Any result matching this filter will not be returned by the resulting method provider.
     * @return The result of applying the filter to this method provider.
     */
    default MethodProvider without(Predicate<? super Method> filter) {
        Objects.requireNonNull(filter);
        return type -> methods(type).filter(m -> !filter.test(m.getMethod()));
    }

    /**
     * Returns a method provider that filters out results of this method provider.
     *
     * @param filter The filter to use. Any result matching this filter will not be returned by the resulting method provider.
     * @return The result of applying the filter to this method provider.
     */
    default MethodProvider without(BiPredicate<? super Class<?>, ? super Method> filter) {
        Objects.requireNonNull(filter);
        return type -> methods(type).filter(m -> !filter.test(type, m.getMethod()));
    }

    /**
     * Returns a method provider for one specific public method.
     * If this method cannot be found or is not public, calling a terminal operation on the stream returned by {@link MethodProvider#methods(Class)}
     * will cause an exception to be thrown. When used in a dynamic test, annotated with {@link TestFactory}, the test factory method will fail in its
     * entirety.
     *
     * @param name The name for the method.
     * @return A method provider that will only return the method with the given name without parameters.
     */
    static MethodProvider method(String name) {
        Objects.requireNonNull(name);
        return c -> Stream.of(methodWithoutParameters(c, name));
    }

    /**
     * Returns a method provider for one specific public method.
     * If this method cannot be found or is not public, calling a terminal operation on the stream returned by {@link MethodProvider#methods(Class)}
     * will cause an exception to be thrown. When used in a dynamic test, annotated with {@link TestFactory}, the test factory method will fail in its
     * entirety.
     *
     * @param name The name for the method.
     * @param parameters The parameters for the method.
     * @return A method provider that will only return the method with the given name and parameters.
     */
    static MethodProvider method(String name, TypeAndArgument... parameters) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(parameters);
        return c -> Stream.of(methodWithParameters(c, name, parameters));
    }

    /**
     * Returns a method provider for one specific public method.
     * If this method cannot be found or is not public, calling a terminal operation on the stream returned by {@link MethodProvider#methods(Class)}
     * will cause an exception to be thrown. When used in a dynamic test, annotated with {@link TestFactory}, the test factory method will fail in its
     * entirety.
     * <p>
     * The method will be invoked with default arguments; {@code 0} for {@code int}, {@code false} for {@code boolean}, {@code null} for objects, etc.
     * This may cause an error if the result of the method is invoked on an object that performs some input validation before delegating.
     * Use {@link #method(String, TypeAndArgument...)} instead if the default arguments are not sufficient.
     *
     * @param name The name for the method.
     * @param parameterTypes The parameter types for the method.
     * @return A method provider that will only return the method with the given name and parameter types.
     */
    static MethodProvider method(String name, Class<?>... parameterTypes) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(parameterTypes);
        return c -> Stream.of(methodWithParameters(c, name, parameterTypes));
    }

    /**
     * Returns a method provider for all public overloads of a method.
     * <p>
     * The methods will be invoked with default arguments; {@code 0} for {@code int}, {@code false} for {@code boolean}, {@code null} for objects,
     * etc. This may cause an error if the result of the method is invoked on an object that performs some input validation before delegating.
     * Use {@link #method(String, TypeAndArgument...)} instead if the default arguments are not sufficient. These methods can be filtered out using
     * {@link MethodProvider#without(Predicate)}.
     * <p>
     * Note that static methods are not filtered out.
     *
     * @param name The name for the methods.
     * @return A method provider that will only return methods with the given name.
     */
    static MethodProvider methods(String name) {
        Objects.requireNonNull(name);
        return allMethods().with(m -> name.equals(m.getName()));
    }

    /**
     * Returns a method provider for all public, {@link Method#isSynthetic() non-synthetic} methods.
     * <p>
     * The methods will be invoked with default arguments; {@code 0} for {@code int}, {@code false} for {@code boolean}, {@code null} for objects,
     * etc. This may cause an error if the result of the method is invoked on an object that performs some input validation before delegating.
     * Use {@link #method(String, TypeAndArgument...)} instead if the default arguments are not sufficient. These methods can be filtered out using
     * {@link MethodProvider#without(Predicate)}.
     * <p>
     * Note that static methods are not filtered out.
     *
     * @return A method provider for all methods.
     */
    static MethodProvider allMethods() {
        return c -> Arrays.stream(c.getMethods())
                .filter(m -> !m.isSynthetic())
                .map(MethodAndArguments::methodWithDefaultArguments);
    }

    /**
     * Returns a method provider that will only return public, {@link Method#isSynthetic() non-synthetic} methods that are declared in the type to
     * search.
     * The methods will be invoked with default arguments; {@code 0} for {@code int}, {@code false} for {@code boolean}, {@code null} for objects,
     * etc. This may cause an error if the result of the method is invoked on an object that performs some input validation before delegating.
     * Use {@link #method(String, TypeAndArgument...)} instead if the default arguments are not sufficient. These methods can be filtered out using
     * {@link MethodProvider#without(Predicate)}.
     * <p>
     * Note that static methods are not filtered out.
     *
     * @return A method provider that will only return methods that are declared in the type to search.
     */
    static MethodProvider methodsDeclaredByType() {
        return allMethods().with((c, m) -> m.getDeclaringClass() == c);
    }
}
