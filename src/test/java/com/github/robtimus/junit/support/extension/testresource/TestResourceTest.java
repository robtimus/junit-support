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

import static com.github.robtimus.junit.support.OptionalAssertions.assertIsPresent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.opentest4j.AssertionFailedError;
import com.github.robtimus.junit.support.extension.InjectionTarget;

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
    @TestResource("test.properties")
    @AsLines
    private static List<String> resourceAsLines;
    @TestResource("test.properties")
    @AsLinesArray
    private static String[] resourceAsLinesArray;
    @TestResource("test.properties")
    @AsProperties
    private static Properties resourceAsProperties;
    @TestResource("test.properties")
    @EOL(EOL.NONE)
    private static String resourceAsStringWithCustomEOL;
    @TestResource("test.properties")
    @EOL(EOL.NONE)
    private static CharSequence resourceAsCharSequenceWithCustomEOL;
    @TestResource("test.properties")
    @EOL(EOL.NONE)
    private static StringBuilder resourceAsStringBuilderWithCustomEOL;

    private TestResourceTest() {
    }

    @Nested
    @DisplayName("constructor injection")
    class ConstructorInjection {

        private final String resourceAsString;
        private final CharSequence resourceAsCharSequence;
        private final StringBuilder resourceAsStringBuilder;
        private final byte[] resourceAsBytes;
        private final List<String> resourceAsLines;
        private final String[] resourceAsLinesArray;
        private final Properties resourceAsProperties;
        private final String resourceAsStringWithCustomEOL;
        private final CharSequence resourceAsCharSequenceWithCustomEOL;
        private final StringBuilder resourceAsStringBuilderWithCustomEOL;

        ConstructorInjection(
                @TestResource("lorem.txt") String resourceAsString,
                @TestResource("lorem.txt") CharSequence resourceAsCharSequence,
                @TestResource("lorem.txt") StringBuilder resourceAsStringBuilder,
                @TestResource("lorem.txt") byte[] resourceAsBytes,
                @TestResource("test.properties") @AsLines List<String> resourceAsLines,
                @TestResource("test.properties") @AsLinesArray String[] resourceAsLinesArray,
                @TestResource("test.properties") @AsProperties Properties resourceAsProperties,
                @TestResource("test.properties") @EOL(EOL.NONE) String resourceAsStringWithCustomEOL,
                @TestResource("test.properties") @EOL(EOL.NONE) CharSequence resourceAsCharSequenceWithCustomEOL,
                @TestResource("test.properties") @EOL(EOL.NONE) StringBuilder resourceAsStringBuilderWithCustomEOL) {

            this.resourceAsString = resourceAsString;
            this.resourceAsCharSequence = resourceAsCharSequence;
            this.resourceAsStringBuilder = resourceAsStringBuilder;
            this.resourceAsBytes = resourceAsBytes;
            this.resourceAsLines = resourceAsLines;
            this.resourceAsLinesArray = resourceAsLinesArray;
            this.resourceAsProperties = resourceAsProperties;
            this.resourceAsStringWithCustomEOL = resourceAsStringWithCustomEOL;
            this.resourceAsCharSequenceWithCustomEOL = resourceAsCharSequenceWithCustomEOL;
            this.resourceAsStringBuilderWithCustomEOL = resourceAsStringBuilderWithCustomEOL;
        }

        @Test
        @DisplayName("as String")
        void testAsString() {
            assertEquals(new String(readResource("lorem.txt")), resourceAsString);
        }

        @Test
        @DisplayName("as CharSequence")
        void testAsCharSequence() {
            assertEquals(StringBuilder.class, resourceAsCharSequence.getClass());
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

        @Test
        @DisplayName("as lines")
        void testAsLines() {
            List<String> expected = Arrays.asList("key1=value1", "key2=value2");
            assertEquals(expected, resourceAsLines);
        }

        @Test
        @DisplayName("as lines array")
        void testAsLinesArray() {
            String[] expected = { "key1=value1", "key2=value2" };
            assertArrayEquals(expected, resourceAsLinesArray);
        }

        @Test
        @DisplayName("as Properties")
        void testAsProperties() {
            Properties expected = new Properties();
            expected.setProperty("key1", "value1");
            expected.setProperty("key2", "value2");
            assertEquals(expected, resourceAsProperties);
        }

        @Test
        @DisplayName("as String with custom EOL")
        void testAsStringWithCustomEOL() {
            assertEquals(new String(readResource("test.properties")).replaceAll("[\r\n]", ""), resourceAsStringWithCustomEOL);
        }

        @Test
        @DisplayName("as CharSequence custom EOL")
        void testAsCharSequenceWithCustomEOL() {
            assertEquals(String.class, resourceAsCharSequenceWithCustomEOL.getClass());
            assertEquals(new String(readResource("test.properties")).replaceAll("[\r\n]", ""), resourceAsCharSequenceWithCustomEOL.toString());
        }

        @Test
        @DisplayName("as StringBuilder custom EOL")
        void testAsStringBuilderWithCustomEOL() {
            assertEquals(new String(readResource("test.properties")).replaceAll("[\r\n]", ""), resourceAsStringBuilderWithCustomEOL.toString());
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
        @TestResource("test.properties")
        @AsLines
        private List<String> resourceAsLines;
        @TestResource("test.properties")
        @AsLinesArray
        private String[] resourceAsLinesArray;
        @TestResource("test.properties")
        @AsProperties
        private Properties resourceAsProperties;
        @TestResource("test.properties")
        @EOL(EOL.NONE)
        private String resourceAsStringWithCustomEOL;
        @TestResource("test.properties")
        @EOL(EOL.NONE)
        private CharSequence resourceAsCharSequenceWithCustomEOL;
        @TestResource("test.properties")
        @EOL(EOL.NONE)
        private StringBuilder resourceAsStringBuilderWithCustomEOL;

        @Test
        @DisplayName("as String")
        void testAsString() {
            assertEquals(new String(readResource("lorem.txt")), resourceAsString);
        }

        @Test
        @DisplayName("as CharSequence")
        void testAsCharSequence() {
            assertEquals(StringBuilder.class, resourceAsCharSequence.getClass());
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

        @Test
        @DisplayName("as lines")
        void testAsLines() {
            List<String> expected = Arrays.asList("key1=value1", "key2=value2");
            assertEquals(expected, resourceAsLines);
        }

        @Test
        @DisplayName("as lines array")
        void testAsLinesArray() {
            String[] expected = { "key1=value1", "key2=value2" };
            assertArrayEquals(expected, resourceAsLinesArray);
        }

        @Test
        @DisplayName("as Properties")
        void testAsProperties() {
            Properties expected = new Properties();
            expected.setProperty("key1", "value1");
            expected.setProperty("key2", "value2");
            assertEquals(expected, resourceAsProperties);
        }

        @Test
        @DisplayName("as String with custom EOL")
        void testAsStringWithCustomEOL() {
            assertEquals(new String(readResource("test.properties")).replaceAll("[\r\n]", ""), resourceAsStringWithCustomEOL);
        }

        @Test
        @DisplayName("as CharSequence custom EOL")
        void testAsCharSequenceWithCustomEOL() {
            assertEquals(String.class, resourceAsCharSequenceWithCustomEOL.getClass());
            assertEquals(new String(readResource("test.properties")).replaceAll("[\r\n]", ""), resourceAsCharSequenceWithCustomEOL.toString());
        }

        @Test
        @DisplayName("as StringBuilder custom EOL")
        void testAsStringBuilderWithCustomEOL() {
            assertEquals(new String(readResource("test.properties")).replaceAll("[\r\n]", ""), resourceAsStringBuilderWithCustomEOL.toString());
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
            assertEquals(StringBuilder.class, resourceAsCharSequence.getClass());
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

        @Test
        @DisplayName("as lines")
        void testAsLines() {
            List<String> expected = Arrays.asList("key1=value1", "key2=value2");
            assertEquals(expected, resourceAsLines);
        }

        @Test
        @DisplayName("as lines array")
        void testAsLinesArray() {
            String[] expected = { "key1=value1", "key2=value2" };
            assertArrayEquals(expected, resourceAsLinesArray);
        }

        @Test
        @DisplayName("as Properties")
        void testAsProperties() {
            Properties expected = new Properties();
            expected.setProperty("key1", "value1");
            expected.setProperty("key2", "value2");
            assertEquals(expected, resourceAsProperties);
        }

        @Test
        @DisplayName("as String with custom EOL")
        void testAsStringWithCustomEOL() {
            assertEquals(new String(readResource("test.properties")).replaceAll("[\r\n]", ""), resourceAsStringWithCustomEOL);
        }

        @Test
        @DisplayName("as CharSequence custom EOL")
        void testAsCharSequenceWithCustomEOL() {
            assertEquals(String.class, resourceAsCharSequenceWithCustomEOL.getClass());
            assertEquals(new String(readResource("test.properties")).replaceAll("[\r\n]", ""), resourceAsCharSequenceWithCustomEOL.toString());
        }

        @Test
        @DisplayName("as StringBuilder custom EOL")
        void testAsStringBuilderWithCustomEOL() {
            assertEquals(new String(readResource("test.properties")).replaceAll("[\r\n]", ""), resourceAsStringBuilderWithCustomEOL.toString());
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
            assertEquals(StringBuilder.class, resource.getClass());
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

        @Test
        @DisplayName("as lines")
        void testAsLines(@TestResource("test.properties") @AsLines List<String> resource) {
            List<String> expected = Arrays.asList("key1=value1", "key2=value2");
            assertEquals(expected, resource);
        }

        @Test
        @DisplayName("as lines array")
        void testAsLinesArray(@TestResource("test.properties") @AsLinesArray String[] resource) {
            String[] expected = { "key1=value1", "key2=value2" };
            assertArrayEquals(expected, resource);
        }

        @Test
        @DisplayName("as Properties")
        void testAsProperties(@TestResource("test.properties") @AsProperties Properties resource) {
            Properties expected = new Properties();
            expected.setProperty("key1", "value1");
            expected.setProperty("key2", "value2");
            assertEquals(expected, resource);
        }

        @Test
        @DisplayName("as String with custom EOL")
        void testAsStringWithCustomEOL(@TestResource("test.properties") @EOL(EOL.NONE) String resource) {
            assertEquals(new String(readResource("test.properties")).replaceAll("[\r\n]", ""), resource);
        }

        @Test
        @DisplayName("as CharSequence custom EOL")
        void testAsCharSequenceWithCustomEOL(@TestResource("test.properties") @EOL(EOL.NONE) CharSequence resource) {
            assertEquals(String.class, resourceAsCharSequenceWithCustomEOL.getClass());
            assertEquals(new String(readResource("test.properties")).replaceAll("[\r\n]", ""), resource.toString());
        }

        @Test
        @DisplayName("as StringBuilder custom EOL")
        void testAsStringBuilderWithCustomEOL(@TestResource("test.properties") @EOL(EOL.NONE) StringBuilder resource) {
            assertEquals(new String(readResource("test.properties")).replaceAll("[\r\n]", ""), resource.toString());
        }
    }

    @Nested
    @DisplayName("@LoadWith")
    class LoadWithTest {

        @Nested
        @DisplayName("local method")
        class LocalMethod {

            @Nested
            @DisplayName("without argument type")
            class WithoutArgumentType {

                @Test
                @DisplayName("uses InputStream")
                void testUsesInputStream(@TestResource("lorem.txt") @LoadWith("loadBytes") byte[] resource) {
                    assertArrayEquals(readResource("lorem.txt"), resource);
                }

                @Test
                @DisplayName("uses Reader")
                void testUsesReader(@TestResource("lorem.txt") @LoadWith("loadString") String resource) {
                    assertEquals(new String(readResource("lorem.txt")), resource);
                }

                @SuppressWarnings("unused")
                private byte[] loadBytes(InputStream inputStream) throws IOException {
                    return TestResourceLoaders.toBytes(inputStream);
                }

                @SuppressWarnings("unused")
                private String loadString(Reader reader) throws IOException {
                    return TestResourceLoaders.toString(reader);
                }
            }

            @Nested
            @DisplayName("with argument type")
            class WithArgumentType {

                @Test
                @DisplayName("uses InputStream")
                void testUsesInputStream(@TestResource("lorem.txt") @LoadWith("loadResource(java.io.InputStream)") byte[] resource) {
                    assertArrayEquals(readResource("lorem.txt"), resource);
                }

                @Test
                @DisplayName("uses InputStream and target")
                void testUsesInputStreamAndTarget(
                        @TestResource("lorem.txt")
                        @LoadWith("loadResource(java.io.InputStream, com.github.robtimus.junit.support.extension.InjectionTarget)")
                        InjectionTarget target) {

                    assertEquals(InjectionTarget.class, target.type());
                    assertInstanceOf(ParameterResolutionException.class, target.createException("test"));
                }

                @Test
                @DisplayName("uses InputStream and target type")
                void testUsesInputStreamAndTargetType(
                        @TestResource("lorem.txt") @LoadWith("loadResource(java.io.InputStream, java.lang.Class)") Class<?> targetType) {

                    assertEquals(Class.class, targetType);
                }

                @Test
                @DisplayName("uses Reader")
                void testUsesReader(@TestResource("lorem.txt") @LoadWith("loadResource(java.io.Reader)") String resource) {
                    assertEquals(new String(readResource("lorem.txt")), resource);
                }

                @Test
                @DisplayName("uses Reader and target")
                void testUsesReaderAndTarget(
                        @TestResource("lorem.txt")
                        @LoadWith("loadResource(java.io.Reader, com.github.robtimus.junit.support.extension.InjectionTarget)")
                        InjectionTarget target) {

                    assertEquals(InjectionTarget.class, target.type());
                    assertInstanceOf(ParameterResolutionException.class, target.createException("test"));
                }

                @Test
                @DisplayName("uses Reader and target type")
                void testUsesReaderAndTargetType(
                        @TestResource("lorem.txt") @LoadWith("loadResource(java.io.Reader, java.lang.Class)") Class<?> targetType) {

                    assertEquals(Class.class, targetType);
                }

                @SuppressWarnings("unused")
                private byte[] loadResource(InputStream inputStream) throws IOException {
                    return TestResourceLoaders.toBytes(inputStream);
                }

                @SuppressWarnings("unused")
                private InjectionTarget loadResource(InputStream inputStream, InjectionTarget target) throws IOException {
                    return target;
                }

                @SuppressWarnings("unused")
                private Class<?> loadResource(InputStream inputStream, Class<?> targetType) throws IOException {
                    return targetType;
                }

                @SuppressWarnings("unused")
                private String loadResource(Reader reader) throws IOException {
                    return TestResourceLoaders.toString(reader);
                }

                @SuppressWarnings("unused")
                private InjectionTarget loadResource(Reader reader, InjectionTarget target) throws IOException {
                    return target;
                }

                @SuppressWarnings("unused")
                private Class<?> loadResource(Reader reader, Class<?> targetType) throws IOException {
                    return targetType;
                }
            }

            @Nested
            @DisplayName("factory not static")
            class FactoryNotStatic {

                @Nested
                @DisplayName("instance field injection")
                class InstanceFieldInection {

                    @Nested
                    @DisplayName("with InputStream")
                    class WithInputStream {

                        @TestResource("lorem.txt")
                        @LoadWith("load")
                        private byte[] resource;

                        @Test
                        void testInjectionSucceeded() {
                            assertNotNull(resource);
                        }

                        byte[] load(InputStream inputStream) throws IOException {
                            return TestResourceLoaders.toBytes(inputStream);
                        }
                    }

                    @Nested
                    @DisplayName("with Reader")
                    class WithReader {

                        @TestResource("lorem.txt")
                        @LoadWith("load")
                        private String resource;

                        @Test
                        void testInjectionSucceeded() {
                            assertNotNull(resource);
                        }

                        String load(Reader reader) throws IOException {
                            return TestResourceLoaders.toString(reader);
                        }
                    }
                }

                @Nested
                @DisplayName("method injection")
                class MethodInection {

                    @Nested
                    @DisplayName("with InputStream")
                    class WithInputStream {

                        @Test
                        void testInjectionSucceeded(@TestResource("lorem.txt") @LoadWith("load") byte[] resource) {
                            assertNotNull(resource);
                        }

                        byte[] load(InputStream inputStream) throws IOException {
                            return TestResourceLoaders.toBytes(inputStream);
                        }
                    }

                    @Nested
                    @DisplayName("with Reader")
                    class WithReader {

                        @Test
                        void testInjectionSucceeded(@TestResource("lorem.txt") @LoadWith("load") String resource) {
                            assertNotNull(resource);
                        }

                        String load(Reader reader) throws IOException {
                            return TestResourceLoaders.toString(reader);
                        }
                    }
                }
            }
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
            void testMethodInjection() {
                assertSingleTestFailure(TestResourceTest.MissingResource.WithMethodInjection.class, ParameterResolutionException.class,
                        equalTo("Resource not found: missing.txt"));
            }
        }

        @Nested
        @DisplayName("invalid annotation combinations")
        class InvalidAnnotationCombinations {

            @Test
            @DisplayName("@EOL used with byte[]")
            void testEOLWithBytes() {
                assertSingleContainerFailure(TestResourceTest.InvalidAnnotationCombinations.EOLWithBytes.class, PreconditionViolationException.class,
                        equalTo("@EOL not allowed for byte[]"));
            }

            @Test
            @DisplayName("@Encoding used with byte[]")
            void testEncodingWithBytes() {
                assertSingleContainerFailure(TestResourceTest.InvalidAnnotationCombinations.EncodingWithBytes.class,
                        PreconditionViolationException.class,
                        equalTo("@Encoding not allowed for byte[]"));
            }

            @Test
            @DisplayName("@LoadWith combined with @EOL")
            void testLoadWithCombinedWithEOL() {
                assertSingleContainerFailure(TestResourceTest.InvalidAnnotationCombinations.LoadWithCombinedWithEOL.class,
                        PreconditionViolationException.class,
                        equalTo("@EOL not allowed in combination with @LoadWith"));
            }

            @Test
            @DisplayName("@LoadWith using InputStream combined with @Encoding")
            void testLoadWithInputStreamCombinedWithEOL() {
                assertSingleContainerFailure(TestResourceTest.InvalidAnnotationCombinations.LoadWithInputStreamCombinedWithEncoding.class,
                        PreconditionViolationException.class,
                        equalTo("@Encoding not allowed when using InputStream"));
            }
        }

        @Nested
        @DisplayName("@LoadWith errors")
        class LoadWithErrors {

            @Test
            @DisplayName("blank method")
            void testBlankMethod() {
                assertSingleContainerFailure(TestResourceTest.LoadWithErrors.BlankMethod.class, PreconditionViolationException.class,
                        equalTo("methodReference must not be null or blank"));
            }

            @Test
            @DisplayName("invalid method syntax")
            void testInvalidMethodSyntax() {
                assertSingleContainerFailure(TestResourceTest.LoadWithErrors.InvalidMethodSyntax.class, PreconditionViolationException.class,
                        equalTo(String.format("[%s] is not a valid method reference: "
                                + "it must be the method name, optionally preceded by a fully qualified class name followed by a '#', "
                                + "and optionally followed by a parameter list enclosed in parentheses.",
                                "com.github.robtimus.junit.support.extension.testresource.TestResourceLoaders#")));
            }

            @Test
            @DisplayName("class not found")
            void testClassNotFound() {
                assertSingleContainerFailure(TestResourceTest.LoadWithErrors.ClassNotFound.class, JUnitException.class,
                        equalTo(String.format("Could not load class [%s]", "com.github.robtimus.junit.support.extension.testresource.NonExisting")));
            }

            @Nested
            @DisplayName("method not found")
            class MethodNotFound {

                @Test
                @DisplayName("without parameters")
                void testWithoutParameters() {
                    String parameterTargetTypes = Arrays.asList(
                            "(java.io.Reader, com.github.robtimus.junit.support.extension.InjectionTarget)",
                            "(java.io.Reader, java.lang.Class)",
                            "(java.io.Reader)",
                            "(java.io.InputStream, com.github.robtimus.junit.support.extension.InjectionTarget)",
                            "(java.io.InputStream, java.lang.Class)",
                            "(java.io.InputStream)")
                            .stream()
                            .collect(Collectors.joining(", "));

                    assertSingleContainerFailure(TestResourceTest.LoadWithErrors.MethodNotFound.WithoutParameters.class,
                            PreconditionViolationException.class,
                            equalTo(String.format("Could not find method [%s] in class [%s] with a parameter combination in [%s]",
                                    "nonExisting", "com.github.robtimus.junit.support.extension.testresource.TestResourceLoaders",
                                    parameterTargetTypes)));
                }

                @Test
                @DisplayName("with InputStream parameters")
                void testWithInputStreamParameters() {
                    assertSingleContainerFailure(TestResourceTest.LoadWithErrors.MethodNotFound.WithInputStreamParameter.class,
                            PreconditionViolationException.class,
                            equalTo(String.format("Could not find method [%s] in class [%s]",
                                    "toString(java.io.InputStream)",
                                    "com.github.robtimus.junit.support.extension.testresource.TestResourceLoaders")));
                }

                @Test
                @DisplayName("with Reader parameters")
                void testWithReaderParameter() {
                    assertSingleContainerFailure(TestResourceTest.LoadWithErrors.MethodNotFound.WithReaderParameter.class,
                            PreconditionViolationException.class,
                            equalTo(String.format("Could not find method [%s] in class [%s]",
                                    "toBytes(java.io.Reader)", "com.github.robtimus.junit.support.extension.testresource.TestResourceLoaders")));
                }

                @Test
                @DisplayName("with simple parameter names")
                void testWithSimpleParameterName() {
                    assertSingleContainerFailure(TestResourceTest.LoadWithErrors.MethodNotFound.WithSimpleParameterName.class, JUnitException.class,
                            equalTo(String.format("Failed to load parameter type [%s] for method [%s] in class [%s].",
                                    "InputStream", "toBytes", "com.github.robtimus.junit.support.extension.testresource.TestResourceLoaders")));
                }
            }

            @Test
            @DisplayName("invalid parameters")
            void testInvalidParameters() {
                String parameterTargetTypes = Arrays.asList(
                        "(java.io.Reader, com.github.robtimus.junit.support.extension.InjectionTarget)",
                        "(java.io.Reader, java.lang.Class)",
                        "(java.io.Reader)",
                        "(java.io.InputStream, com.github.robtimus.junit.support.extension.InjectionTarget)",
                        "(java.io.InputStream, java.lang.Class)",
                        "(java.io.InputStream)")
                        .stream()
                        .collect(Collectors.joining(", "));

                assertSingleContainerFailure(TestResourceTest.LoadWithErrors.InvalidParameters.class, PreconditionViolationException.class,
                        equalTo(String.format("Method [readResource(java.lang.String)] in class [%s] does not have a parameter combination in [%s]",
                                "com.github.robtimus.junit.support.extension.testresource.TestResourceTest",
                                parameterTargetTypes)));
            }

            @Nested
            @DisplayName("target type mismatch")
            class TargetTypeMismatch {

                @Test
                @DisplayName("with InputStream")
                void testWithInputStream() {
                    assertSingleContainerFailure(TestResourceTest.LoadWithErrors.TargetTypeMismatch.WithInputStream.class,
                            IllegalArgumentException.class, startsWith("Can not set static java.lang.String field"));
                }

                @Test
                @DisplayName("with Reader")
                void testWithReader() {
                    assertSingleContainerFailure(TestResourceTest.LoadWithErrors.TargetTypeMismatch.WithReader.class,
                            IllegalArgumentException.class, startsWith("Can not set static [B field"));
                }
            }

            @Nested
            @DisplayName("void return type")
            class VoidReturnType {

                @Test
                @DisplayName("with InputStream")
                void testWithInputStream() {
                    // Injection doesn't fail, because calling a void method through reflection returns null
                    assertSingleTestFailure(TestResourceTest.LoadWithErrors.VoidReturnType.WithInputStream.class, AssertionFailedError.class,
                            equalTo("expected: not <null>"));
                }

                @Test
                @DisplayName("with Reader")
                void testWithReader() {
                    // Injection doesn't fail, because calling a void method through reflection returns null
                    assertSingleTestFailure(TestResourceTest.LoadWithErrors.VoidReturnType.WithReader.class, AssertionFailedError.class,
                            equalTo("expected: not <null>"));
                }
            }

            @Nested
            @DisplayName("factory throws exception")
            class FactoryThrowsException {

                @Test
                @DisplayName("with InputStream")
                void testWithInputStream() {
                    assertSingleContainerFailure(TestResourceTest.LoadWithErrors.FactoryThrowsException.WithInputStream.class, IOException.class,
                            equalTo("error"));
                }

                @Test
                @DisplayName("with Reader")
                void testWithReader() {
                    assertSingleContainerFailure(TestResourceTest.LoadWithErrors.FactoryThrowsException.WithReader.class, IOException.class,
                            equalTo("error"));
                }
            }

            @Nested
            @DisplayName("factory not static")
            class FactoryNotStatic {

                @Nested
                @DisplayName("constructor injection")
                class ConstructorInjection {

                    @Test
                    @DisplayName("with InputStream")
                    void testWithInputStream() {
                        assertSingleTestFailure(TestResourceTest.LoadWithErrors.FactoryNotStatic.ConstructorInjection.WithInputStream.class,
                                ParameterResolutionException.class, matchesRegex(".*Cannot invoke non-static method .* on a null target.*"));
                    }

                    @Test
                    @DisplayName("with Reader")
                    void testWithReader() {
                        assertSingleTestFailure(TestResourceTest.LoadWithErrors.FactoryNotStatic.ConstructorInjection.WithReader.class,
                                ParameterResolutionException.class, matchesRegex(".*Cannot invoke non-static method .* on a null target.*"));
                    }
                }

                @Nested
                @DisplayName("static field injection")
                class StaticFieldInjection {

                    @Test
                    @DisplayName("with InputStream")
                    void testWithInputStream() {
                        assertSingleContainerFailure(TestResourceTest.LoadWithErrors.FactoryNotStatic.StaticFieldInjection.WithInputStream.class,
                                PreconditionViolationException.class, matchesRegex(".*Cannot invoke non-static method .* on a null target.*"));
                    }

                    @Test
                    @DisplayName("with Reader")
                    void testWithReader() {
                        assertSingleContainerFailure(TestResourceTest.LoadWithErrors.FactoryNotStatic.StaticFieldInjection.WithReader.class,
                                PreconditionViolationException.class, matchesRegex(".*Cannot invoke non-static method .* on a null target.*"));
                    }
                }

                @Nested
                @DisplayName("different class")
                class DifferentClass {

                    @Nested
                    @DisplayName("constructor injection")
                    class ConstructorInjection {

                        @Test
                        @DisplayName("with InputStream")
                        void testWithInputStream() {
                            assertSingleTestFailure(
                                    TestResourceTest.LoadWithErrors.FactoryNotStatic.DifferentClass.ConstructorInjection.WithInputStream.class,
                                    ParameterResolutionException.class, matchesRegex(".*Cannot invoke non-static method .* on a null target.*"));
                        }

                        @Test
                        @DisplayName("with Reader")
                        void testWithReader() {
                            assertSingleTestFailure(
                                    TestResourceTest.LoadWithErrors.FactoryNotStatic.DifferentClass.ConstructorInjection.WithReader.class,
                                    ParameterResolutionException.class, matchesRegex(".*Cannot invoke non-static method .* on a null target.*"));
                        }
                    }

                    @Nested
                    @DisplayName("instance field injection")
                    class InstanceFieldInjection {

                        @Test
                        @DisplayName("with InputStream")
                        void testWithInputStream() {
                            assertSingleTestFailure(
                                    TestResourceTest.LoadWithErrors.FactoryNotStatic.DifferentClass.InstanceFieldInjection.WithInputStream.class,
                                    IllegalArgumentException.class, equalTo("object is not an instance of declaring class"));
                        }

                        @Test
                        @DisplayName("with Reader")
                        void testWithReader() {
                            assertSingleTestFailure(
                                    TestResourceTest.LoadWithErrors.FactoryNotStatic.DifferentClass.InstanceFieldInjection.WithReader.class,
                                    IllegalArgumentException.class, equalTo("object is not an instance of declaring class"));
                        }
                    }

                    @Nested
                    @DisplayName("static field injection")
                    class StaticFieldInjection {

                        @Test
                        @DisplayName("with InputStream")
                        void testWithInputStream() {
                            assertSingleContainerFailure(
                                    TestResourceTest.LoadWithErrors.FactoryNotStatic.DifferentClass.StaticFieldInjection.WithInputStream.class,
                                    PreconditionViolationException.class, matchesRegex(".*Cannot invoke non-static method .* on a null target.*"));
                        }

                        @Test
                        @DisplayName("with Reader")
                        void testWithReader() {
                            assertSingleContainerFailure(
                                    TestResourceTest.LoadWithErrors.FactoryNotStatic.DifferentClass.StaticFieldInjection.WithReader.class,
                                    PreconditionViolationException.class, matchesRegex(".*Cannot invoke non-static method .* on a null target.*"));
                        }
                    }

                    @Nested
                    @DisplayName("method injection")
                    class MethodInjection {

                        @Test
                        @DisplayName("with InputStream")
                        void testWithInputStream() {
                            assertSingleTestFailure(
                                    TestResourceTest.LoadWithErrors.FactoryNotStatic.DifferentClass.MethodInjection.WithInputStream.class,
                                    ParameterResolutionException.class, containsString("object is not an instance of declaring class"));
                        }

                        @Test
                        @DisplayName("with Reader")
                        void testWithReader() {
                            assertSingleTestFailure(
                                    TestResourceTest.LoadWithErrors.FactoryNotStatic.DifferentClass.MethodInjection.WithReader.class,
                                    ParameterResolutionException.class, containsString("object is not an instance of declaring class"));
                        }
                    }
                }
            }
        }

        private void assertSingleTestFailure(Class<?> testClass, Class<? extends Throwable> errorType, Matcher<String> messageMatcher) {
            EngineExecutionResults results = runTests(testClass);

            assertEquals(0, results.testEvents().succeeded().count());
            assertEquals(1, results.testEvents().failed().count());

            Throwable throwable = getSingleTestFailure(results);
            assertEquals(errorType, throwable.getClass());
            assertThat(throwable.getMessage(), messageMatcher);
        }

        private void assertSingleContainerFailure(Class<?> testClass, Class<? extends Throwable> errorType, Matcher<String> messageMatcher) {
            EngineExecutionResults results = runTests(testClass);

            assertEquals(0, results.testEvents().count());
            assertEquals(1, results.containerEvents().succeeded().count());
            assertEquals(1, results.containerEvents().failed().count());

            Throwable throwable = getSingleContainerFailure(results);
            assertEquals(errorType, throwable.getClass());
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

    static final class InvalidAnnotationCombinations {

        static class EOLWithBytes {

            @TestResource("lorem.txt")
            @EOL(EOL.ORIGINAL)
            private static byte[] resource;

            @Test
            void testMethod() {
                assertNotNull(resource);
            }
        }

        static class EncodingWithBytes {

            @TestResource("lorem.txt")
            @Encoding("UTF-8")
            private static byte[] resource;

            @Test
            void testMethod() {
                assertNotNull(resource);
            }
        }

        static class LoadWithCombinedWithEOL {

            @TestResource("lorem.txt")
            @EOL(EOL.ORIGINAL)
            @LoadWith("com.github.robtimus.junit.support.extension.testresource.TestResourceLoaders#toBytes(java.io.InputStream)")
            private static String resource;

            @Test
            void testMethod() {
                assertNotNull(resource);
            }
        }

        static class LoadWithInputStreamCombinedWithEncoding {

            @TestResource("lorem.txt")
            @Encoding("UTF-8")
            @LoadWith("com.github.robtimus.junit.support.extension.testresource.TestResourceLoaders#toBytes(java.io.InputStream)")
            private static byte[] resource;

            @Test
            void testMethod() {
                assertNotNull(resource);
            }
        }
    }

    static final class LoadWithErrors {

        static final class BlankMethod {

            @TestResource("lorem.txt")
            @LoadWith("")
            private static byte[] resource;

            @Test
            void testMethod() {
                assertNotNull(resource);
            }
        }

        static final class InvalidMethodSyntax {

            @TestResource("lorem.txt")
            @LoadWith("com.github.robtimus.junit.support.extension.testresource.TestResourceLoaders#")
            private static byte[] resource;

            @Test
            void testMethod() {
                assertNotNull(resource);
            }
        }

        static final class ClassNotFound {

            @TestResource("lorem.txt")
            @LoadWith("com.github.robtimus.junit.support.extension.testresource.NonExisting#toProperties")
            private static byte[] resource;

            @Test
            void testMethod() {
                assertNotNull(resource);
            }
        }

        static final class MethodNotFound {

            static final class WithoutParameters {

                @TestResource("lorem.txt")
                @LoadWith("com.github.robtimus.junit.support.extension.testresource.TestResourceLoaders#nonExisting")
                private static byte[] resource;

                @Test
                void testMethod() {
                    assertNotNull(resource);
                }
            }

            static final class WithInputStreamParameter {

                @TestResource("lorem.txt")
                @LoadWith("com.github.robtimus.junit.support.extension.testresource.TestResourceLoaders#toString(java.io.InputStream)")
                private static byte[] resource;

                @Test
                void testMethod() {
                    assertNotNull(resource);
                }
            }

            static final class WithReaderParameter {

                @TestResource("lorem.txt")
                @LoadWith("com.github.robtimus.junit.support.extension.testresource.TestResourceLoaders#toBytes(java.io.Reader)")
                private static byte[] resource;

                @Test
                void testMethod() {
                    assertNotNull(resource);
                }
            }

            static final class WithSimpleParameterName {

                @TestResource("lorem.txt")
                @LoadWith("com.github.robtimus.junit.support.extension.testresource.TestResourceLoaders#toBytes(InputStream)")
                private static byte[] resource;

                @Test
                void testMethod() {
                    assertNotNull(resource);
                }
            }
        }

        static final class InvalidParameters {

            @TestResource("lorem.txt")
            @LoadWith("com.github.robtimus.junit.support.extension.testresource.TestResourceTest#readResource(java.lang.String)")
            private static byte[] resource;

            @Test
            void testMethod() {
                assertNotNull(resource);
            }
        }

        static final class TargetTypeMismatch {

            static final class WithInputStream {

                @TestResource("lorem.txt")
                @LoadWith("com.github.robtimus.junit.support.extension.testresource.TestResourceLoaders#toBytes")
                private static String resource;

                @Test
                void testMethod() {
                    assertNotNull(resource);
                }
            }

            static final class WithReader {

                @TestResource("lorem.txt")
                @LoadWith("com.github.robtimus.junit.support.extension.testresource.TestResourceLoaders#toString")
                private static byte[] resource;

                @Test
                void testMethod() {
                    assertNotNull(resource);
                }
            }
        }

        static final class VoidReturnType {

            static final class WithInputStream {

                @TestResource("lorem.txt")
                @LoadWith("load")
                private static byte[] resource;

                @Test
                void testMethod() {
                    assertNotNull(resource);
                }

                static void load(InputStream inputStream) {
                    assertNotNull(inputStream);
                }
            }

            static final class WithReader {

                @TestResource("lorem.txt")
                @LoadWith("load")
                private static String resource;

                @Test
                void testMethod() {
                    assertNotNull(resource);
                }

                static void load(Reader reader) {
                    assertNotNull(reader);
                }
            }
        }

        static final class FactoryThrowsException {

            static final class WithInputStream {

                @TestResource("lorem.txt")
                @LoadWith("load")
                private static byte[] resource;

                @Test
                void testMethod() {
                    assertNotNull(resource);
                }

                static byte[] load(InputStream inputStream) throws IOException {
                    assertNotNull(inputStream);
                    throw new IOException("error");
                }
            }

            static final class WithReader {

                @TestResource("lorem.txt")
                @LoadWith("load")
                private static String resource;

                @Test
                void testMethod() {
                    assertNotNull(resource);
                }

                static String load(Reader reader) throws IOException {
                    assertNotNull(reader);
                    throw new IOException("error");
                }
            }
        }

        static final class FactoryNotStatic {

            static final class ConstructorInjection {

                static final class WithInputStream {

                    private final byte[] resource;

                    WithInputStream(@TestResource("lorem.txt") @LoadWith("load") byte[] resource) {
                        this.resource = resource;
                    }

                    @Test
                    void testMethod() {
                        assertNotNull(resource);
                    }

                    byte[] load(InputStream inputStream) throws IOException {
                        return TestResourceLoaders.toBytes(inputStream);
                    }
                }

                static final class WithReader {

                    private final String resource;

                    WithReader(@TestResource("lorem.txt") @LoadWith("load") String resource) {
                        this.resource = resource;
                    }

                    @Test
                    void testMethod() {
                        assertNotNull(resource);
                    }

                    String load(Reader reader) throws IOException {
                        return TestResourceLoaders.toString(reader);
                    }
                }
            }

            static final class StaticFieldInjection {

                static final class WithInputStream {

                    @TestResource("lorem.txt")
                    @LoadWith("load")
                    private static byte[] resource;

                    @Test
                    void testMethod() {
                        assertNotNull(resource);
                    }

                    byte[] load(InputStream inputStream) throws IOException {
                        return TestResourceLoaders.toBytes(inputStream);
                    }
                }

                static final class WithReader {

                    @TestResource("lorem.txt")
                    @LoadWith("load")
                    private static String resource;

                    @Test
                    void testMethod() {
                        assertNotNull(resource);
                    }

                    String load(Reader reader) throws IOException {
                        return TestResourceLoaders.toString(reader);
                    }
                }
            }

            static final class DifferentClass {

                private static final String PACKAGE_NAME = "com.github.robtimus.junit.support.extension.testresource";
                private static final String CLASS_NAME = PACKAGE_NAME + ".TestResourceTest$LoadWithErrors$FactoryNotStatic$DifferentClass";

                private static final String LOAD_WITH_INPUT_STREAM = CLASS_NAME + "#load(java.io.InputStream)";
                private static final String LOAD_WITH_READER = CLASS_NAME + "#load(java.io.Reader)";

                byte[] load(InputStream inputStream) throws IOException {
                    return TestResourceLoaders.toBytes(inputStream);
                }

                String load(Reader reader) throws IOException {
                    return TestResourceLoaders.toString(reader);
                }

                static final class ConstructorInjection {

                    static final class WithInputStream {

                        private final byte[] resource;

                        WithInputStream(@TestResource("lorem.txt") @LoadWith(LOAD_WITH_INPUT_STREAM) byte[] resource) {
                            this.resource = resource;
                        }

                        @Test
                        void testMethod() {
                            assertNotNull(resource);
                        }
                    }

                    static final class WithReader {

                        private final String resource;

                        WithReader(@TestResource("lorem.txt") @LoadWith(LOAD_WITH_READER) String resource) {
                            this.resource = resource;
                        }

                        @Test
                        void testMethod() {
                            assertNotNull(resource);
                        }
                    }
                }

                static final class InstanceFieldInjection {

                    static final class WithInputStream {

                        @TestResource("lorem.txt")
                        @LoadWith(LOAD_WITH_INPUT_STREAM)
                        private byte[] resource;

                        @Test
                        void testMethod() {
                            assertNotNull(resource);
                        }
                    }

                    static final class WithReader {

                        @TestResource("lorem.txt")
                        @LoadWith(LOAD_WITH_READER)
                        private String resource;

                        @Test
                        void testMethod() {
                            assertNotNull(resource);
                        }
                    }
                }

                static final class StaticFieldInjection {

                    static final class WithInputStream {

                        @TestResource("lorem.txt")
                        @LoadWith(LOAD_WITH_INPUT_STREAM)
                        private static byte[] resource;

                        @Test
                        void testMethod() {
                            assertNotNull(resource);
                        }
                    }

                    static final class WithReader {

                        @TestResource("lorem.txt")
                        @LoadWith(LOAD_WITH_READER)
                        private static String resource;

                        @Test
                        void testMethod() {
                            assertNotNull(resource);
                        }
                    }
                }

                static final class MethodInjection {

                    static final class WithInputStream {

                        @Test
                        void testMethod(@TestResource("lorem.txt") @LoadWith(LOAD_WITH_INPUT_STREAM) byte[] resource) {
                            assertNotNull(resource);
                        }
                    }

                    static final class WithReader {

                        @Test
                        void testMethod(@TestResource("lorem.txt") @LoadWith(LOAD_WITH_READER) String resource) {
                            assertNotNull(resource);
                        }
                    }
                }
            }
        }
    }
}
