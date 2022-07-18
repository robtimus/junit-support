/*
 * TestResourceTest.java
 * Copyright 2022 Rob Spoor
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

package com.github.robtimus.junit.support.extension.testresource;

import static com.github.robtimus.junit.support.AdditionalAssertions.assertIsPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.opentest4j.AssertionFailedError;

@SuppressWarnings("nls")
final class TestResourceTest {

    @TestResource("lorem.txt")
    private static String resourceAsString;
    @TestResource("lorem.txt")
    private static CharSequence resourceAsCharSequence;
    @TestResource("lorem.txt")
    private static StringBuilder resourceAsStringBuilder;
    @TestResource("lorem.txt")
    private static byte[] resourceAsBytes;

    private TestResourceTest() {
    }

    @Nested
    @DisplayName("constructor injection")
    class ConstructorInjection {

        private final String resourceAsString;
        private final CharSequence resourceAsCharSequence;
        private final StringBuilder resourceAsStringBuilder;
        private final byte[] resourceAsBytes;

        ConstructorInjection(
                @TestResource("lorem.txt") String resourceAsString,
                @TestResource("lorem.txt") CharSequence resourceAsCharSequence,
                @TestResource("lorem.txt") StringBuilder resourceAsStringBuilder,
                @TestResource("lorem.txt") byte[] resourceAsBytes) {

            this.resourceAsString = resourceAsString;
            this.resourceAsCharSequence = resourceAsCharSequence;
            this.resourceAsStringBuilder = resourceAsStringBuilder;
            this.resourceAsBytes = resourceAsBytes;
        }

        @Test
        @DisplayName("as String")
        void testAsString() {
            assertEquals(new String(readResource("lorem.txt")), resourceAsString);
        }

        @Test
        @DisplayName("as CharSequence")
        void testAsCharSequence() {
            assertNotEquals(String.class, resourceAsCharSequence.getClass());
            assertEquals(new String(readResource("lorem.txt")), resourceAsCharSequence.toString());
        }

        @Test
        @DisplayName("as StringBuilder")
        void testAsStringBuilder() {
            assertEquals(new String(readResource("lorem.txt")), resourceAsStringBuilder.toString());
        }

        @Test
        @DisplayName("as bytes")
        void testAsBytes() {
            assertArrayEquals(readResource("lorem.txt"), resourceAsBytes);
        }
    }

    @Nested
    @DisplayName("instance field injection")
    class InstanceFieldInjection {

        @TestResource("lorem.txt")
        private String resourceAsString;
        @TestResource("lorem.txt")
        private CharSequence resourceAsCharSequence;
        @TestResource("lorem.txt")
        private StringBuilder resourceAsStringBuilder;
        @TestResource("lorem.txt")
        private byte[] resourceAsBytes;

        @Test
        @DisplayName("as String")
        void testAsString() {
            assertEquals(new String(readResource("lorem.txt")), resourceAsString);
        }

        @Test
        @DisplayName("as CharSequence")
        void testAsCharSequence() {
            assertNotEquals(String.class, resourceAsCharSequence.getClass());
            assertEquals(new String(readResource("lorem.txt")), resourceAsCharSequence.toString());
        }

        @Test
        @DisplayName("as StringBuilder")
        void testAsStringBuilder() {
            assertEquals(new String(readResource("lorem.txt")), resourceAsStringBuilder.toString());
        }

        @Test
        @DisplayName("as bytes")
        void testAsBytes() {
            assertArrayEquals(readResource("lorem.txt"), resourceAsBytes);
        }
    }

    @Nested
    @DisplayName("static field injection")
    class StaticFieldInjection {

        @Test
        @DisplayName("as String")
        void testAsString() {
            assertEquals(new String(readResource("lorem.txt")), resourceAsString);
        }

        @Test
        @DisplayName("as CharSequence")
        void testAsCharSequence() {
            assertNotEquals(String.class, resourceAsCharSequence.getClass());
            assertEquals(new String(readResource("lorem.txt")), resourceAsCharSequence.toString());
        }

        @Test
        @DisplayName("as StringBuilder")
        void testAsStringBuilder() {
            assertEquals(new String(readResource("lorem.txt")), resourceAsStringBuilder.toString());
        }

        @Test
        @DisplayName("as bytes")
        void testAsBytes() {
            assertArrayEquals(readResource("lorem.txt"), resourceAsBytes);
        }
    }

    @Nested
    @DisplayName("method injection")
    class MethodInjection {

        @Test
        @DisplayName("as String")
        void testAsString(@TestResource("lorem.txt") String resource) {
            assertEquals(new String(readResource("lorem.txt")), resource);
        }

        @Test
        @DisplayName("as CharSequence")
        void testAsCharSequence(@TestResource("lorem.txt") CharSequence resource) {
            assertNotEquals(String.class, resource.getClass());
            assertEquals(new String(readResource("lorem.txt")), resource.toString());
        }

        @Test
        @DisplayName("as StringBuilder")
        void testAsStringBuilder(@TestResource("lorem.txt") StringBuilder resource) {
            assertEquals(new String(readResource("lorem.txt")), resource.toString());
        }

        @Test
        @DisplayName("as bytes")
        void testAsBytes(@TestResource("lorem.txt") byte[] resource) {
            assertArrayEquals(readResource("lorem.txt"), resource);
        }
    }

    @Nested
    @DisplayName("invalid usage")
    class InvalidUsage {

        @Nested
        @DisplayName("missing annotation")
        class MissingAnnotation {

            @Test
            @DisplayName("constructor injection")
            void testConstructorInjection() {
                assertSingleTestFailure(TestResourceTest.MissingAnnotation.WithConstructorInjection.class, ParameterResolutionException.class,
                        startsWith("No ParameterResolver registered for parameter [" + StringBuilder.class.getName()));
            }

            @Test
            @DisplayName("instance field injection")
            void testInstanceFieldInjection() {
                // Injection doesn't fail, because the field is ignored during field injection because it's not annotated
                assertSingleTestFailure(TestResourceTest.MissingAnnotation.WithInstanceFieldInjection.class, AssertionFailedError.class,
                        equalTo("expected: not <null>"));
            }

            @Test
            @DisplayName("static field injection")
            void testStaticFieldInjection() {
                // Injection doesn't fail, because the field is ignored during field injection because it's not annotated
                assertSingleTestFailure(TestResourceTest.MissingAnnotation.WithStaticFieldInjection.class, AssertionFailedError.class,
                        equalTo("expected: not <null>"));
            }

            @Test
            @DisplayName("method injection")
            void testMissingAnnotation() {
                assertSingleTestFailure(TestResourceTest.MissingAnnotation.WithMethodInjection.class, ParameterResolutionException.class,
                        startsWith("No ParameterResolver registered for parameter [" + StringBuilder.class.getName()));
            }
        }

        @Nested
        @DisplayName("unsupported type")
        class UnsupportedType {

            @Test
            @DisplayName("constructor injection")
            void testConstructorInjection() {
                assertSingleTestFailure(TestResourceTest.UnsupportedType.WithConstructorInjection.class, ParameterResolutionException.class,
                        startsWith("No ParameterResolver registered for parameter [" + LocalDate.class.getName()));
            }

            @Test
            @DisplayName("instance field injection")
            void testInstanceFieldInjection() {
                assertSingleTestFailure(TestResourceTest.UnsupportedType.WithInstanceFieldInjection.class, ExtensionConfigurationException.class,
                        equalTo("Target type not supported: " + LocalDate.class));
            }

            @Test
            @DisplayName("static field injection")
            void testStaticFieldInjection() {
                assertSingleContainerFailure(TestResourceTest.UnsupportedType.WithStaticFieldInjection.class, ExtensionConfigurationException.class,
                        equalTo("Target type not supported: " + LocalDate.class));
            }

            @Test
            @DisplayName("method injection")
            void testMissingAnnotation() {
                assertSingleTestFailure(TestResourceTest.UnsupportedType.WithMethodInjection.class, ParameterResolutionException.class,
                        startsWith("No ParameterResolver registered for parameter [" + LocalDate.class.getName()));
            }
        }

        @Nested
        @DisplayName("missing resource")
        class MissingResource {

            @Test
            @DisplayName("constructor injection")
            void testConstructorInjection() {
                assertSingleTestFailure(TestResourceTest.MissingResource.WithConstructorInjection.class, ParameterResolutionException.class,
                        equalTo("Resource not found: missing.txt"));
            }

            @Test
            @DisplayName("instance field injection")
            void testInstanceFieldInjection() {
                assertSingleTestFailure(TestResourceTest.MissingResource.WithInstanceFieldInjection.class, ExtensionConfigurationException.class,
                        equalTo("Resource not found: missing.txt"));
            }

            @Test
            @DisplayName("static field injection")
            void testStaticFieldInjection() {
                assertSingleContainerFailure(TestResourceTest.MissingResource.WithStaticFieldInjection.class, ExtensionConfigurationException.class,
                        equalTo("Resource not found: missing.txt"));
            }

            @Test
            @DisplayName("method injection")
            void testMissingAnnotation() {
                assertSingleTestFailure(TestResourceTest.MissingResource.WithMethodInjection.class, ParameterResolutionException.class,
                        equalTo("Resource not found: missing.txt"));
            }
        }

        private void assertSingleTestFailure(Class<?> testClass, Class<? extends Throwable> errorType, Matcher<String> messageMatcher) {
            EngineExecutionResults results = runTests(testClass);

            assertEquals(0, results.testEvents().succeeded().count());
            assertEquals(1, results.testEvents().failed().count());

            Throwable throwable = getSingleTestFailure(results);
            assertInstanceOf(errorType, throwable);
            assertThat(throwable.getMessage(), messageMatcher);
        }

        private void assertSingleContainerFailure(Class<?> testClass, Class<? extends Throwable> errorType, Matcher<String> messageMatcher) {
            EngineExecutionResults results = runTests(testClass);

            assertEquals(0, results.testEvents().count());
            assertEquals(1, results.containerEvents().succeeded().count());
            assertEquals(1, results.containerEvents().failed().count());

            Throwable throwable = getSingleContainerFailure(results);
            assertInstanceOf(errorType, throwable);
            assertThat(throwable.getMessage(), messageMatcher);
        }

        private EngineExecutionResults runTests(Class<?> testClass) {
            return EngineTestKit.engine(new JupiterTestEngine())
                    .selectors(DiscoverySelectors.selectClass(testClass))
                    .execute();
        }

        private Throwable getSingleTestFailure(EngineExecutionResults results) {
            TestExecutionResult result = assertIsPresent(results.testEvents().failed().stream()
                    .map(event -> event.getPayload(TestExecutionResult.class))
                    .findAny()
                    .orElse(null));

            Throwable throwable = assertIsPresent(result.getThrowable());
            return throwable;
        }

        private Throwable getSingleContainerFailure(EngineExecutionResults results) {
            TestExecutionResult result = assertIsPresent(results.containerEvents().failed().stream()
                    .map(event -> event.getPayload(TestExecutionResult.class))
                    .findAny()
                    .orElse(null));

            Throwable throwable = assertIsPresent(result.getThrowable());
            return throwable;
        }
    }

    private static byte[] readResource(String resource) {
        return assertDoesNotThrow(() -> {
            try (InputStream inputStream = TestResourceTest.class.getResourceAsStream(resource)) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
                return outputStream.toByteArray();
            }
        });
    }

    static final class MissingAnnotation {

        static final class WithConstructorInjection {

            private final StringBuilder resource;

            WithConstructorInjection(StringBuilder resource) {
                this.resource = resource;
            }

            @Test
            void testMissingAnnotation() {
                assertNotNull(resource);
            }
        }

        static final class WithInstanceFieldInjection {

            private StringBuilder resource;

            @Test
            void testMissingAnnotation() {
                assertNotNull(resource);
            }
        }

        static final class WithStaticFieldInjection {

            private static StringBuilder resource;

            @Test
            void testMissingAnnotation() {
                assertNotNull(resource);
            }
        }

        static final class WithMethodInjection {

            @Test
            void testMissingAnnotation(StringBuilder resource) {
                assertNotNull(resource);
            }
        }
    }

    static final class UnsupportedType {

        static final class WithConstructorInjection {

            private final LocalDate resource;

            WithConstructorInjection(@TestResource("lorem.txt") LocalDate resource) {
                this.resource = resource;
            }

            @Test
            void testUnsupportedType() {
                assertNotNull(resource);
            }
        }

        static final class WithInstanceFieldInjection {

            @TestResource("lorem.txt")
            private LocalDate resource;

            @Test
            void testUnsupportedType() {
                assertNotNull(resource);
            }
        }

        static final class WithStaticFieldInjection {

            @TestResource("lorem.txt")
            private static LocalDate resource;

            @Test
            void testUnsupportedType() {
                assertNotNull(resource);
            }
        }

        static final class WithMethodInjection {

            @Test
            void testUnsupportedType(@TestResource("lorem.txt") LocalDate resource) {
                assertNotNull(resource);
            }
        }
    }

    static final class MissingResource {

        static final class WithConstructorInjection {

            private final String resource;

            WithConstructorInjection(@TestResource("missing.txt") String resource) {
                this.resource = resource;
            }

            @Test
            void testMissingResource() {
                assertNotNull(resource);
            }
        }

        static final class WithInstanceFieldInjection {

            @TestResource("missing.txt")
            private String resource;

            @Test
            void testMissingResource() {
                assertNotNull(resource);
            }
        }

        static final class WithStaticFieldInjection {

            @TestResource("missing.txt")
            private static String resource;

            @Test
            void testMissingResource() {
                assertNotNull(resource);
            }
        }

        static final class WithMethodInjection {

            @Test
            void testMissingResource(@TestResource("missing.txt") String resource) {
                assertNotNull(resource);
            }
        }
    }
}
