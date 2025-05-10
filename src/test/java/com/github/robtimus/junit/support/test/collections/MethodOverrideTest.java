/*
 * MethodOverrideTest.java
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

package com.github.robtimus.junit.support.test.collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsProvider;

@SuppressWarnings("nls")
class MethodOverrideTest {

    @Nested
    @DisplayName("MapTests")
    class MapTestsTest {

        @Nested
        @DisplayName("KeySetTests")
        class KeySetTestsTest extends AbstractMethodOverrideTest {

            KeySetTestsTest() {
                super(SetTests.class, MapTests.KeySetTests.class);
            }

            @Nested
            @DisplayName("IteratorTests")
            class IteratorTestsTest extends AbstractMethodOverrideTest {

                IteratorTestsTest() {
                    super(IteratorTests.class, MapTests.KeySetTests.IteratorTests.class);
                }
            }
        }

        @Nested
        @DisplayName("ValuesTests")
        class ValuesTestsTest extends AbstractMethodOverrideTest {

            ValuesTestsTest() {
                super(CollectionTests.class, MapTests.ValuesTests.class);
            }

            @Nested
            @DisplayName("IteratorTests")
            class IteratorTestsTest extends AbstractMethodOverrideTest {

                IteratorTestsTest() {
                    super(IteratorTests.class, MapTests.ValuesTests.IteratorTests.class);
                }
            }
        }

        @Nested
        @DisplayName("EntrySetTests")
        class EntrySetTestsTest extends AbstractMethodOverrideTest {

            EntrySetTestsTest() {
                super(SetTests.class, MapTests.EntrySetTests.class);
            }

            @Nested
            @DisplayName("IteratorTests")
            class IteratorTestsTest extends AbstractMethodOverrideTest {

                IteratorTestsTest() {
                    super(IteratorTests.class, MapTests.EntrySetTests.IteratorTests.class);
                }
            }
        }
    }

    @Nested
    @DisplayName("UnmodifiableMapTests")
    class UnmodifiableMapTestsTest {

        @Nested
        @DisplayName("KeySetTests")
        class KeySetTestsTest extends AbstractMethodOverrideTest {

            KeySetTestsTest() {
                super(UnmodifiableSetTests.class, UnmodifiableMapTests.KeySetTests.class);
            }

            @Nested
            @DisplayName("IteratorTests")
            class IteratorTestsTest extends AbstractMethodOverrideTest {

                IteratorTestsTest() {
                    super(UnmodifiableIteratorTests.class, UnmodifiableMapTests.KeySetTests.IteratorTests.class,
                            // Ignore IteratorTests.RemoveTests tests, as UnmodifiableTests.RemoveTests should be used
                            IteratorTests.RemoveTests.class);
                }
            }
        }

        @Nested
        @DisplayName("ValuesTests")
        class ValuesTestsTest extends AbstractMethodOverrideTest {

            ValuesTestsTest() {
                super(UnmodifiableCollectionTests.class, UnmodifiableMapTests.ValuesTests.class);
            }

            @Nested
            @DisplayName("IteratorTests")
            class IteratorTestsTest extends AbstractMethodOverrideTest {

                IteratorTestsTest() {
                    super(UnmodifiableIteratorTests.class, UnmodifiableMapTests.ValuesTests.IteratorTests.class,
                            // Ignore IteratorTests.RemoveTests tests, as UnmodifiableTests.RemoveTests should be used
                            IteratorTests.RemoveTests.class);
                }
            }
        }

        @Nested
        @DisplayName("EntrySetTests")
        class EntrySetTestsTest extends AbstractMethodOverrideTest {

            EntrySetTestsTest() {
                super(UnmodifiableSetTests.class, UnmodifiableMapTests.EntrySetTests.class);
            }

            @Nested
            @DisplayName("IteratorTests")
            class IteratorTestsTest extends AbstractMethodOverrideTest {

                IteratorTestsTest() {
                    super(UnmodifiableIteratorTests.class, UnmodifiableMapTests.EntrySetTests.IteratorTests.class,
                            // Ignore IteratorTests.RemoveTests tests, as UnmodifiableTests.RemoveTests should be used
                            IteratorTests.RemoveTests.class);
                }
            }
        }
    }

    abstract static class AbstractMethodOverrideTest {

        private final Class<?> base;
        private final Class<?> sub;
        private final Set<Class<?>> interfacesToIgnore;

        private AbstractMethodOverrideTest(Class<?> baseInterface, Class<?> subInterface, Class<?>... interfacesToIgnore) {
            this.base = baseInterface;
            this.sub = subInterface;
            this.interfacesToIgnore = new HashSet<>(Arrays.asList(interfacesToIgnore));
        }

        @TestFactory
        @DisplayName("nested interfaces")
        Stream<DynamicNode> testInterfaces() {
            return testInterfaces(base, sub);
        }

        private Stream<DynamicNode> testInterfaces(Class<?> baseInterface, Class<?> subInterface) {
            boolean hasAnyMethods = Arrays.stream(subInterface.getDeclaredMethods())
                    .filter(this::isTestMethod)
                    .count() > 0;

            Stream<DynamicTest> methodTests = hasAnyMethods
                    ? Arrays.stream(baseInterface.getMethods())
                            .filter(this::isTestMethod)
                            .map(m -> testMethod(m, subInterface))
                    : Stream.empty();
            Stream<DynamicContainer> interfaceTests = getAllNestedClasses(baseInterface)
                    .distinct()
                    .filter(this::isTestsInterface)
                    .filter(this::isNonIgnoredInterface)
                    .map(i -> testInterface(i, subInterface));

            return Stream.concat(methodTests, interfaceTests);
        }

        private DynamicTest testMethod(Method method, Class<?> subInterface) {
            return dynamicTest(method.toString(), () -> {
                assertDoesNotThrow(() -> subInterface.getDeclaredMethod(method.getName(), method.getParameterTypes()));
            });
        }

        private Stream<Class<?>> getAllNestedClasses(Class<?> c) {
            Stream<Class<?>> ownNestedClasses = Arrays.stream(c.getDeclaredClasses());
            Stream<Class<?>> inheritedClassNestedClasses = Arrays.stream(c.getInterfaces())
                    .filter(i -> !i.equals(c.getEnclosingClass()))
                    .flatMap(this::getAllNestedClasses);
            return Stream.concat(inheritedClassNestedClasses, ownNestedClasses);
        }

        private DynamicContainer testInterface(Class<?> i, Class<?> subInterface) {
            Optional<Class<?>> actualSub = Arrays.stream(subInterface.getDeclaredClasses())
                    .filter(c -> c.getSimpleName().equals(i.getSimpleName())) // NOSONAR, isAssignableFrom will not do the same
                    .findAny();

            Stream<DynamicTest> interfaceTests = Stream.of(dynamicTest("interface exists", () -> {
                assertDoesNotThrow(actualSub::get, String.format("Cannot find nested interface %s in interface %s", i.getSimpleName(), subInterface));
            }));

            Stream<DynamicNode> subTests = actualSub.map(c -> testInterfaces(i, c))
                    .orElse(Stream.empty());

            return dynamicContainer(i.getSimpleName(), Stream.concat(interfaceTests, subTests));
        }

        private boolean isTestMethod(Method method) {
            return method.isAnnotationPresent(Test.class)
                    || method.isAnnotationPresent(ParameterizedTest.class)
                    || method.isAnnotationPresent(TestFactory.class);
        }

        private boolean isTestsInterface(Class<?> i) {
            return !ArgumentsProvider.class.isAssignableFrom(i);
        }

        private boolean isNonIgnoredInterface(Class<?> i) {
            return !interfacesToIgnore.contains(i);
        }
    }
}
