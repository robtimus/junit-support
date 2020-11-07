/*
 * MethodFinder.java
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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.jupiter.api.TestFactory;
import com.github.robtimus.junit.support.DelegateTests;

/**
 * A functional interface for finding methods that will be used with {@link DelegateTests#testDelegates()}.
 *
 * @author Rob Spoor
 */
@FunctionalInterface
public interface MethodFinder {

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

    /**
     * Returns a method finder that filters out results of this method finder.
     *
     * @param filter The filter to use. Any result matching this filter will not be returned by the resulting method finder.
     * @return The result of applying the filter to this method finder.
     */
    default MethodFinder without(Predicate<? super InvokableMethod> filter) {
        Objects.requireNonNull(filter);
        return type -> findMethods(type).filter(m -> !filter.test(m));
    }

    /**
     * Returns a method finder for one specific method.
     * If this method cannot be found, calling a terminal operation on the stream returned by {@link MethodFinder#findMethods(Class)} will cause an
     * exception to be thrown. When used in a dynamic test, annotated with {@link TestFactory}, the test factory method will fail in its entirety.
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
     * If this method cannot be found, calling a terminal operation on the stream returned by {@link MethodFinder#findMethods(Class)} will cause an
     * exception to be thrown. When used in a dynamic test, annotated with {@link TestFactory}, the test factory method will fail in its entirety.
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
     * If this method cannot be found, calling a terminal operation on the stream returned by {@link MethodFinder#findMethods(Class)} will cause an
     * exception to be thrown. When used in a dynamic test, annotated with {@link TestFactory}, the test factory method will fail in its entirety.
     * <p>
     * The method will be invoked with default arguments; {@code 0} for {@code int}, {@code false} for {@code boolean}, {@code null} for objects, etc.
     * This may cause an error if the result of the method is invoked on an object that performs some input validation before delegating.
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
     * <p>
     * The methods will be invoked with default arguments; {@code 0} for {@code int}, {@code false} for {@code boolean}, {@code null} for objects,
     * etc. This may cause an error if the result of the method is invoked on an object that performs some input validation before delegating.
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
     * etc. This may cause an error if the result of the method is invoked on an object that performs some input validation before delegating.
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
     * etc. This may cause an error if the result of the method is invoked on an object that performs some input validation before delegating.
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
     * etc. This may cause an error if the result of the method is invoked on an object that performs some input validation before delegating.
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
     * etc. This may cause an error if the result of the method is invoked on an object that performs some input validation before delegating.
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
}
