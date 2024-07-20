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
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.platform.commons.support.ReflectionSupport;
import com.github.robtimus.junit.support.extension.testlogger.LogCaptor;
import com.github.robtimus.junit.support.test.collections.CollectionTests;
import com.github.robtimus.junit.support.test.collections.EnumerationTests;
import com.github.robtimus.junit.support.test.collections.IteratorTests;
import com.github.robtimus.junit.support.test.collections.ListIteratorTests;
import com.github.robtimus.junit.support.test.io.InputStreamTests;
import com.github.robtimus.junit.support.test.io.ReaderTests;

class TraitTest {

    private static final Set<Class<?>> IGNORED_CLASSES = Set.of(LogCaptor.class);

    @SuppressWarnings("nls")
    private static final Map<Class<?>, Set<String>> ALLOWED_METHOD_NAMES = Map.of(
            CollectionTests.ToArrayWithGeneratorTests.class, Set.of("testToArrayWithNullGenerator"),

            EnumerationTests.IterationTests.class, Set.of("testNextElementWithoutHasMoreElements"),
            EnumerationTests.AsIteratorTests.class, Set.of("testNextWithoutHasNext"),

            IteratorTests.IterationTests.class, Set.of("testNextWithoutHasNext"),

            ListIteratorTests.IterationTests.class, Set.of("testNextWithoutHasNext", "testPreviousWithoutHasPrevious"),

            InputStreamTests.MarkResetTests.class, Set.of("testMarkSupported", "testMarkAndReset", "testResetWithoutMark"),
            ReaderTests.MarkResetTests.class, Set.of("testMarkSupported", "testMarkAndReset", "testResetWithoutMark"));

    @TestFactory
    @DisplayName("Traits are implemented correctly")
    Stream<DynamicNode> testTraits() {
        return findAllClasses()
                .stream()
                .sorted(Comparator.comparing(Class::getName))
                .map(this::testTrait);
    }

    @SuppressWarnings("nls")
    private List<Class<?>> findAllClasses() {
        Predicate<Class<?>> classFilter = c ->
                c.isInterface()
                && !c.isAnnotation()
                && !Modifier.isPrivate(c.getModifiers())
                && !IGNORED_CLASSES.contains(c);
        Predicate<String> nameFilter = n -> !n.endsWith(".package-info");

        if (JRE.currentVersion().compareTo(JRE.JAVA_8) > 0) {
            // Java 9 or up; use the module name if available
            try {
                Method method = Class.class.getMethod("getModule");
                Object module = method.invoke(getClass());
                String moduleName = (String) module.getClass().getMethod("getName").invoke(module);
                if (moduleName != null) {
                    return ReflectionSupport.findAllClassesInModule(moduleName, classFilter, nameFilter);
                }
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }

        // Either Java 8, or an unnamed module
        String packageName = getClass().getPackage().getName();
        return ReflectionSupport.findAllClassesInPackage(packageName, classFilter, nameFilter);
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
