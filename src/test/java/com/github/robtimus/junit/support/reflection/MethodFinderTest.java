/*
 * MethodFinderTest.java
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

import static com.github.robtimus.junit.support.reflection.MethodFinder.allMethods;
import static com.github.robtimus.junit.support.reflection.MethodFinder.method;
import static com.github.robtimus.junit.support.reflection.MethodFinder.methods;
import static com.github.robtimus.junit.support.reflection.MethodFinder.methodsDeclaredByType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

@SuppressWarnings("nls")
class MethodFinderTest {

    @Nested
    @DisplayName("method(String, Class...)")
    class MethodTest {

        @Test
        @DisplayName("method found")
        void testMethodFound() {
            Method method = assertDoesNotThrow(() -> List.class.getMethod("add", int.class, Object.class));

            MethodFinder finder = method("add", int.class, Object.class);
            List<Method> methods = finder.findMethods(List.class)
                    .map(InvokableMethod::getMethod)
                    .collect(Collectors.toList());

            assertEquals(Collections.singletonList(method), methods);

            List<Object[]> arguments = finder.findMethods(List.class)
                    .map(InvokableMethod::getArguments)
                    .collect(Collectors.toList());

            assertThat(arguments, contains(arrayContaining(0, null)));
        }

        @Test
        @DisplayName("method not found")
        void testMethodNotFound() {
            MethodFinder finder = method("add", int.class, String.class);
            assertThrows(AssertionFailedError.class, () -> finder.findMethods(List.class).collect(Collectors.toList()));
        }
    }

    @Nested
    @DisplayName("methods(String)")
    class MethodsTest {

        @Test
        @DisplayName("methods found")
        void testMethodFound() {
            Method method1 = assertDoesNotThrow(() -> List.class.getMethod("add", Object.class));
            Method method2 = assertDoesNotThrow(() -> List.class.getMethod("add", int.class, Object.class));

            MethodFinder finder = methods("add");
            List<Method> methods = finder.findMethods(List.class)
                    .map(InvokableMethod::getMethod)
                    .collect(Collectors.toList());

            assertThat(methods, containsInAnyOrder(method1, method2));

            List<Object[]> arguments = finder.findMethods(List.class)
                    .map(InvokableMethod::getArguments)
                    .collect(Collectors.toList());

            Matcher<Object[]> addArgumentsMatcher = arrayContaining((Object) null);
            Matcher<Object[]> addIndexedArgumentsMatcher = arrayContaining(0, null);
            assertThat(arguments, containsInAnyOrder(addArgumentsMatcher, addIndexedArgumentsMatcher));
        }

        @Test
        @DisplayName("methods not found")
        void testMethodNotFound() {
            MethodFinder finder = methods("bogus");
            List<Method> methods = finder.findMethods(List.class)
                    .map(InvokableMethod::getMethod)
                    .collect(Collectors.toList());

            assertEquals(Collections.emptyList(), methods);
        }
    }

    @Nested
    @DisplayName("allMethods()")
    class AllMethodsTest {

        @Test
        @DisplayName("unfiltered")
        void testUnfiltered() {
            Method method1 = assertDoesNotThrow(() -> InputStream.class.getMethod("read"));
            Method method2 = assertDoesNotThrow(() -> InputStream.class.getMethod("read", byte[].class, int.class, int.class));
            Method method3 = assertDoesNotThrow(() -> InputStream.class.getMethod("equals", Object.class));
            Method method4 = assertDoesNotThrow(() -> InputStream.class.getMethod("hashCode"));

            MethodFinder finder = allMethods();
            List<Method> methods = finder.findMethods(InputStream.class)
                    .map(InvokableMethod::getMethod)
                    .collect(Collectors.toList());

            assertThat(methods, hasItems(method1, method2, method3, method4));

            List<Object[]> arguments = finder.findMethods(InputStream.class)
                    .map(InvokableMethod::getArguments)
                    .collect(Collectors.toList());

            Matcher<Object[]> readArgumentsMatcher = arrayContaining(null, 0, 0);
            assertThat(arguments, hasItems(
                    emptyArray(),
                    readArgumentsMatcher,
                    arrayContaining((Object) null),
                    emptyArray()));
        }

        @Test
        @DisplayName("filtered")
        void testFiltered() {
            Method method1 = assertDoesNotThrow(() -> InputStream.class.getMethod("read"));
            Method method2 = assertDoesNotThrow(() -> InputStream.class.getMethod("read", byte[].class, int.class, int.class));
            Method method3 = assertDoesNotThrow(() -> InputStream.class.getMethod("equals", Object.class));
            Method method4 = assertDoesNotThrow(() -> InputStream.class.getMethod("hashCode"));

            MethodFinder finder = allMethods().without(m -> m.getMethod().getParameterCount() > 0);
            List<Method> methods = finder.findMethods(InputStream.class)
                    .map(InvokableMethod::getMethod)
                    .collect(Collectors.toList());

            assertThat(methods, hasItems(method1, method4));
            assertThat(methods, not(hasItems(method2, method3)));

            List<Object[]> arguments = finder.findMethods(InputStream.class)
                    .map(InvokableMethod::getArguments)
                    .collect(Collectors.toList());

            assertThat(arguments, hasItems(emptyArray(), emptyArray()));
        }
    }

    @Test
    @DisplayName("methodsDeclaredByType()")
    void testMethodsDeclaredByType() {
        Method method1 = assertDoesNotThrow(() -> InputStream.class.getMethod("read"));
        Method method2 = assertDoesNotThrow(() -> InputStream.class.getMethod("read", byte[].class, int.class, int.class));

        MethodFinder finder = methodsDeclaredByType();
        List<Method> methods = finder.findMethods(InputStream.class)
                .map(InvokableMethod::getMethod)
                .collect(Collectors.toList());

        assertThat(methods, hasItems(method1, method2));

        assertThat(methods, everyItem(not(hasProperty("name", equalTo("equals")))));
        assertThat(methods, everyItem(not(hasProperty("name", equalTo("hashCode")))));

        List<Object[]> arguments = finder.findMethods(InputStream.class)
                .map(InvokableMethod::getArguments)
                .collect(Collectors.toList());

        Matcher<Object[]> readArgumentsMatcher = arrayContaining(null, 0, 0);
        assertThat(arguments, hasItems(
                emptyArray(),
                readArgumentsMatcher));
    }
}
