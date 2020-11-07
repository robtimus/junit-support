/*
 * TraitTest.java
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeElementsScanner;
import com.github.robtimus.junit.support.collections.EnumerationTests;
import com.github.robtimus.junit.support.collections.IteratorTests;
import com.github.robtimus.junit.support.collections.ListIteratorTests;
import com.github.robtimus.junit.support.io.InputStreamTests;
import com.github.robtimus.junit.support.io.ReaderTests;
import com.github.robtimus.junit.support.reflection.MethodFinder;

class TraitTest {

    private static final Set<Class<?>> IGNORED_CLASSES = getIgnoredClasses();

    private static final Map<Class<?>, Set<String>> ALLOWED_METHOD_NAMES = getAllowedMethodNames();

    private static Set<Class<?>> getIgnoredClasses() {
        Set<Class<?>> result = new HashSet<>();
        result.add(MethodFinder.class);
        return Collections.unmodifiableSet(result);
    }

    @SuppressWarnings("nls")
    private static Map<Class<?>, Set<String>> getAllowedMethodNames() {
        Map<Class<?>, Set<String>> result = new HashMap<>();

        Set<String> allowedNames = new HashSet<>();

        allowedNames.add("testNextElementWithoutHasMoreElements");

        result.put(EnumerationTests.IterationTests.class, new HashSet<>(allowedNames));

        allowedNames.clear();
        allowedNames.add("testNextWithoutHasNext");

        result.put(IteratorTests.IterationTests.class, new HashSet<>(allowedNames));

        allowedNames.add("testPreviousWithoutHasPrevious");

        result.put(ListIteratorTests.IterationTests.class, new HashSet<>(allowedNames));

        allowedNames.clear();
        allowedNames.add("testMarkSupported");
        allowedNames.add("testMarkAndReset");

        result.put(InputStreamTests.MarkResetTests.class, new HashSet<>(allowedNames));
        result.put(ReaderTests.MarkResetTests.class, new HashSet<>(allowedNames));

        return Collections.unmodifiableMap(result);
    }

    @TestFactory
    @DisplayName("Traits are implemented correctly")
    Stream<DynamicNode> testTraits() {
        Reflections reflections = new Reflections(getClass().getPackage().getName(), new TypeElementsScanner(), new SubTypesScanner(false));

        return ReflectionUtils.forNames(reflections.getAllTypes(), reflections.getConfiguration().getClassLoaders()).stream()
                .filter(Class::isInterface)
                .filter(c -> !"package-info".equals(c.getSimpleName())) //$NON-NLS-1$
                .filter(c -> !c.isAnnotation())
                .filter(c -> !IGNORED_CLASSES.contains(c))
                .sorted(Comparator.comparing(Class::getName))
                .map(this::testTrait);
    }

    private DynamicNode testTrait(Class<?> traitInterface) {
        Stream<DynamicTest> interfaceTests = Stream.of(testInterfaceNamedCorrectly(traitInterface));

        Stream<DynamicNode> methodTests = Arrays.stream(traitInterface.getMethods())
                .filter(m -> isTestMethod(m))
                .map(this::testMethod);

        Stream<DynamicNode> nodes = Stream.concat(interfaceTests, methodTests);

        return dynamicContainer(traitInterface.getName(), nodes);
    }

    @SuppressWarnings("nls")
    private DynamicTest testInterfaceNamedCorrectly(Class<?> traitInterface) {
        return dynamicTest("Interface named correctly", () -> {
            assertThat(traitInterface.getName(), endsWith("Tests"));
        });
    }

    private boolean isTestMethod(Method m) {
        return m.isAnnotationPresent(Test.class) || m.isAnnotationPresent(ParameterizedTest.class) || m.isAnnotationPresent(TestFactory.class);
    }

    private DynamicNode testMethod(Method method) {
        Stream<DynamicTest> tests = Stream.of(
                testMethodNamedCorrectly(method),
                testMethodIsDefault(method)
                );

        String methodNameAndArguments = getMethodNameAndArguments(method);
        return dynamicContainer(methodNameAndArguments, tests);
    }

    @SuppressWarnings("nls")
    private String getMethodNameAndArguments(Method method) {
        String methodNameAndArguments = Arrays.stream(method.getGenericParameterTypes())
                .map(Type::toString)
                .map(s -> s.replaceAll("^(class|enum|interface) ", ""))
                .collect(Collectors.joining(", ", method.getName() + "(", ")"));
        return methodNameAndArguments;
    }

    @SuppressWarnings("nls")
    private DynamicTest testMethodNamedCorrectly(Method method) {
        return dynamicTest("Method named correctly", () -> {
            Class<?> declaringClass = method.getDeclaringClass();

            String className = declaringClass.getSimpleName();
            String baseName = className.endsWith("Tests") ? className.substring(0, className.length() - 5) : className;

            String expected = "test" + baseName;

            String methodName = method.getName();
            if (!isAllowedMethodName(methodName, declaringClass)) {
                assertThat(methodName, startsWith(expected));
            }
        });
    }

    private boolean isAllowedMethodName(String methodName, Class<?> declaringClass) {
        return ALLOWED_METHOD_NAMES.getOrDefault(declaringClass, Collections.emptySet()).contains(methodName);
    }

    @SuppressWarnings("nls")
    private DynamicTest testMethodIsDefault(Method method) {
        return dynamicTest("Method has default modifier", () -> {
            assertTrue(method.isDefault(), "Method should have a default implementation");
        });
    }
}
