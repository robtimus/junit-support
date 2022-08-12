/*
 * TestResourceExtensionTest.java
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

import static com.github.robtimus.junit.support.extension.testresource.TestResourceExtension.lookupEncoding;
import static com.github.robtimus.junit.support.extension.testresource.TestResourceExtension.lookupLineSeparator;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.Charset;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;
import com.github.robtimus.junit.support.extension.InjectionTarget;

@SuppressWarnings({ "nls", "unused" })
class TestResourceExtensionTest {

    @Nested
    @DisplayName("lookupLineSeparator")
    class LookupLineSeparator {

        @Nested
        @DisplayName("for field")
        class ForField {

            @Nested
            @DisplayName("class annotated")
            class ClassAnnotated {

                @Test
                @DisplayName("field not annotated")
                void testFieldNotAnnotated() {
                    testField("fieldWithNoAnnotation", "--class--");
                }

                @Test
                @DisplayName("field annotated with EOL.LF")
                void testFieldAnnotatedWithLF() {
                    testField("fieldWithLfEOL", "\n");
                }

                @Test
                @DisplayName("field annotated with EOL.CR")
                void testFieldAnnotatedWithCR() {
                    testField("fieldWithCrEOL", "\r");
                }

                @Test
                @DisplayName("field annotated with EOL.CRLF")
                void testFieldAnnotatedWithCRLF() {
                    testField("fieldWithCrlfEOL", "\r\n");
                }

                @Test
                @DisplayName("field annotated with EOL.SYSTEM")
                void testFieldAnnotatedWithSystem() {
                    // Ideally this test would be run with @SetSystemProperty to test against multiple values,
                    // but System.lineSeparator() only reads the property once

                    testField("fieldWithSystemEOL", System.lineSeparator());
                }

                @Test
                @DisplayName("field annotated with EOL.ORIGINAL")
                void testFieldAnnotatedWithOriginal() {
                    testField("fieldWithOriginalEOL", EOL.ORIGINAL);
                }

                @Test
                @DisplayName("field annotated with EOL.NONE")
                void testFieldAnnotatedWithNone() {
                    testField("fieldWithEmptyEOL", "");
                }

                @Test
                @DisplayName("field annotated with custom EOL")
                void testFieldAnnotatedWithCustom() {
                    testField("fieldWithCustomEOL", "--");
                }

                private void testField(String fieldName, String expectedLineSeparator) {
                    Field field = assertDoesNotThrow(() -> AnnotatedClassWithTestFieldsAndMethods.class.getDeclaredField(fieldName));
                    InjectionTarget target = InjectionTarget.forField(field);

                    String lineSeparator = lookupLineSeparator(target, context());

                    assertEquals(expectedLineSeparator, lineSeparator);
                }
            }

            @Nested
            @DisplayName("class not annotated")
            class ClassNotAnnotated {

                @Nested
                @DisplayName("field not annotated")
                class FieldNotAnnotated {

                    @Test
                    @DisplayName("no configuration parameter")
                    void testNoConfigurationParameter() {
                        testField(context(), EOL.ORIGINAL);
                    }

                    @Test
                    @DisplayName("configuration parameter LF")
                    void testConfigurationParameterLF() {
                        testField("LF", "\n");
                    }

                    @Test
                    @DisplayName("configuration parameter CR")
                    void testConfigurationParameterCR() {
                        testField("CR", "\r");
                    }

                    @Test
                    @DisplayName("configuration parameter CRLF")
                    void testConfigurationParameterCRLF() {
                        testField("CRLF", "\r\n");
                    }

                    @Test
                    @DisplayName("configuration parameter SYSTEM")
                    void testConfigurationParameterSystem() {
                        // Ideally this test would be run with @SetSystemProperty to test against multiple values,
                        // but System.lineSeparator() only reads the property once

                        testField("SYSTEM", System.lineSeparator());
                    }

                    @Test
                    @DisplayName("configuration parameter ORIGINAL")
                    void testConfigurationParameterOriginal() {
                        testField("ORIGINAL", EOL.ORIGINAL);
                    }

                    @Test
                    @DisplayName("configuration parameter NONE")
                    void testConfigurationParameterNone() {
                        testField("NONE", "");
                    }

                    @Test
                    @DisplayName("custom configuration parameter")
                    void testCustomConfigurationParameter() {
                        testField("--", "--");
                    }

                    private void testField(String configurationParameterValue, String expectedLineSeparator) {
                        testField(context(EOL.DEFAULT_EOL_PROPERTY_NAME, configurationParameterValue), expectedLineSeparator);
                    }

                    private void testField(ExtensionContext context, String expectedLineSeparator) {
                        ClassNotAnnotated.this.testField("fieldWithNoAnnotation", context, expectedLineSeparator);
                    }
                }

                @Test
                @DisplayName("field annotated with EOL.LF")
                void testFieldAnnotatedWithLF() {
                    testField("fieldWithLfEOL", "\n");
                }

                @Test
                @DisplayName("field annotated with EOL.CR")
                void testFieldAnnotatedWithCR() {
                    testField("fieldWithCrEOL", "\r");
                }

                @Test
                @DisplayName("field annotated with EOL.CRLF")
                void testFieldAnnotatedWithCRLF() {
                    testField("fieldWithCrlfEOL", "\r\n");
                }

                @Test
                @DisplayName("field annotated with EOL.SYSTEM")
                void testFieldAnnotatedWithSystem() {
                    // Ideally this test would be run with @SetSystemProperty to test against multiple values,
                    // but System.lineSeparator() only reads the property once

                    testField("fieldWithSystemEOL", System.lineSeparator());
                }

                @Test
                @DisplayName("field annotated with EOL.ORIGINAL")
                void testFieldAnnotatedWithOriginal() {
                    testField("fieldWithOriginalEOL", EOL.ORIGINAL);
                }

                @Test
                @DisplayName("field annotated with EOL.NONE")
                void testFieldAnnotatedWithNone() {
                    testField("fieldWithEmptyEOL", "");
                }

                @Test
                @DisplayName("field annotated with custom EOL")
                void testFieldAnnotatedWithCustom() {
                    testField("fieldWithCustomEOL", "--");
                }

                private void testField(String fieldName, String expectedLineSeparator) {
                    testField(fieldName, context(), expectedLineSeparator);
                }

                private void testField(String fieldName, ExtensionContext context, String expectedLineSeparator) {
                    Field field = assertDoesNotThrow(() -> NonAnnotatedClassWithTestFieldsAndMethods.class.getDeclaredField(fieldName));
                    InjectionTarget target = InjectionTarget.forField(field);

                    String lineSeparator = lookupLineSeparator(target, context);

                    assertEquals(expectedLineSeparator, lineSeparator);
                }
            }
        }

        @Nested
        @DisplayName("for method")
        class ForMethod {

            @Nested
            @DisplayName("class annotated")
            class ClassAnnotated {

                @Test
                @DisplayName("method with no annotations")
                void testMethodWithNoAnnotations() {
                    testMethod("methodWithNoAnnotations", "--class--");
                }

                @Nested
                @DisplayName("parameter annotated")
                class ParameterAnnotated {

                    @Test
                    @DisplayName("parameter annotated with EOL.LF")
                    void testParameterAnnotatedWithLF() {
                        testMethod("methodWithParameterWithLfEOL", "\n");
                    }

                    @Test
                    @DisplayName("parameter annotated with EOL.CR")
                    void testParameterAnnotatedWithCR() {
                        testMethod("methodWithParameterWithCrEOL", "\r");
                    }

                    @Test
                    @DisplayName("parameter annotated with EOL.CRLF")
                    void testParameterAnnotatedWithCRLF() {
                        testMethod("methodWithParameterWithCrlfEOL", "\r\n");
                    }

                    @Test
                    @DisplayName("parameter annotated with EOL.SYSTEM")
                    void testParameterAnnotatedWithSystem() {
                        // Ideally this test would be run with @SetSystemProperty to test against multiple values,
                        // but System.lineSeparator() only reads the property once

                        testMethod("methodWithParameterWithSystemEOL", System.lineSeparator());
                    }

                    @Test
                    @DisplayName("parameter annotated with EOL.ORIGINAL")
                    void testParameterAnnotatedWithOriginal() {
                        testMethod("methodWithParameterWithOriginalEOL", EOL.ORIGINAL);
                    }

                    @Test
                    @DisplayName("parameter annotated with EOL.NONE")
                    void testParameterAnnotatedWithNone() {
                        testMethod("methodWithParameterWithEmptyEOL", "");
                    }

                    @Test
                    @DisplayName("parameter annotated with custom EOL")
                    void testParameterAnnotatedWithCustom() {
                        testMethod("methodWithParameterWithCustomEOL", "--");
                    }
                }

                @Nested
                @DisplayName("method annotated")
                class MethodAnnotated {

                    @Test
                    @DisplayName("method annotated with EOL.LF")
                    void testMethodAnnotatedWithLF() {
                        testMethod("methodWithLfEOL", "\n");
                    }

                    @Test
                    @DisplayName("method annotated with EOL.CR")
                    void testMethodAnnotatedWithCR() {
                        testMethod("methodWithCrEOL", "\r");
                    }

                    @Test
                    @DisplayName("method annotated with EOL.CRLF")
                    void testMethodAnnotatedWithCRLF() {
                        testMethod("methodWithCrlfEOL", "\r\n");
                    }

                    @Test
                    @DisplayName("method annotated with EOL.SYSTEM")
                    void testMethodAnnotatedWithSystem() {
                        // Ideally this test would be run with @SetSystemProperty to test against multiple values,
                        // but System.lineSeparator() only reads the property once

                        testMethod("methodWithSystemEOL", System.lineSeparator());
                    }

                    @Test
                    @DisplayName("method annotated with EOL.ORIGINAL")
                    void testMethodAnnotatedWithOriginal() {
                        testMethod("methodWithOriginalEOL", EOL.ORIGINAL);
                    }

                    @Test
                    @DisplayName("method annotated with EOL.NONE")
                    void testMethodAnnotatedWithNone() {
                        testMethod("methodWithEmptyEOL", "");
                    }

                    @Test
                    @DisplayName("method annotated with custom EOL")
                    void testMethodAnnotatedWithCustom() {
                        testMethod("methodWithCustomEOL", "--");
                    }
                }

                private void testMethod(String methodName, String expectedLineSeparator) {
                    Method method = assertDoesNotThrow(
                            () -> AnnotatedClassWithTestFieldsAndMethods.class.getDeclaredMethod(methodName, String.class));
                    Parameter parameter = method.getParameters()[0];

                    ParameterContext context = mock(ParameterContext.class);
                    when(context.getParameter()).thenReturn(parameter);
                    when(context.getDeclaringExecutable()).thenReturn(method);
                    when(context.findAnnotation(any())).thenAnswer(i -> AnnotationSupport.findAnnotation(parameter, i.getArgument(0)));
                    when(context.findRepeatableAnnotations(any()))
                            .thenAnswer(i -> AnnotationSupport.findRepeatableAnnotations(parameter, i.getArgument(0)));

                    InjectionTarget target = InjectionTarget.forParameter(context);

                    String lineSeparator = lookupLineSeparator(target, context());

                    assertEquals(expectedLineSeparator, lineSeparator);
                }
            }

            @Nested
            @DisplayName("class not annotated")
            class ClassNotAnnotated {

                @Nested
                @DisplayName("method with no annotations")
                class MethodWithNoAnnotations {

                    @Test
                    @DisplayName("no configuration parameter")
                    void testNoConfigurationParameter() {
                        testMethod(context(), EOL.ORIGINAL);
                    }

                    @Test
                    @DisplayName("configuration parameter LF")
                    void testConfigurationParameterLF() {
                        testMethod("LF", "\n");
                    }

                    @Test
                    @DisplayName("configuration parameter CR")
                    void testConfigurationParameterCR() {
                        testMethod("CR", "\r");
                    }

                    @Test
                    @DisplayName("configuration parameter CRLF")
                    void testConfigurationParameterCRLF() {
                        testMethod("CRLF", "\r\n");
                    }

                    @Test
                    @DisplayName("configuration parameter SYSTEM")
                    void testConfigurationParameterSystem() {
                        // Ideally this test would be run with @SetSystemProperty to test against multiple values,
                        // but System.lineSeparator() only reads the property once

                        testMethod("SYSTEM", System.lineSeparator());
                    }

                    @Test
                    @DisplayName("configuration parameter ORIGINAL")
                    void testConfigurationParameterOriginal() {
                        testMethod("ORIGINAL", EOL.ORIGINAL);
                    }

                    @Test
                    @DisplayName("configuration parameter NONE")
                    void testConfigurationParameterNone() {
                        testMethod("NONE", "");
                    }

                    @Test
                    @DisplayName("custom configuration parameter")
                    void testCustomConfigurationParameter() {
                        testMethod("--", "--");
                    }

                    private void testMethod(String configurationParameterValue, String expectedLineSeparator) {
                        testMethod(context(EOL.DEFAULT_EOL_PROPERTY_NAME, configurationParameterValue), expectedLineSeparator);
                    }

                    private void testMethod(ExtensionContext context, String expectedLineSeparator) {
                        ClassNotAnnotated.this.testMethod("methodWithNoAnnotations", context, expectedLineSeparator);
                    }
                }

                @Nested
                @DisplayName("parameter annotated")
                class ParameterAnnotated {

                    @Test
                    @DisplayName("parameter annotated with EOL.LF")
                    void testParameterAnnotatedWithLF() {
                        testMethod("methodWithParameterWithLfEOL", "\n");
                    }

                    @Test
                    @DisplayName("parameter annotated with EOL.CR")
                    void testParameterAnnotatedWithCR() {
                        testMethod("methodWithParameterWithCrEOL", "\r");
                    }

                    @Test
                    @DisplayName("parameter annotated with EOL.CRLF")
                    void testParameterAnnotatedWithCRLF() {
                        testMethod("methodWithParameterWithCrlfEOL", "\r\n");
                    }

                    @Test
                    @DisplayName("parameter annotated with EOL.SYSTEM")
                    void testParameterAnnotatedWithSystem() {
                        // Ideally this test would be run with @SetSystemProperty to test against multiple values,
                        // but System.lineSeparator() only reads the property once

                        testMethod("methodWithParameterWithSystemEOL", System.lineSeparator());
                    }

                    @Test
                    @DisplayName("parameter annotated with EOL.ORIGINAL")
                    void testParameterAnnotatedWithOriginal() {
                        testMethod("methodWithParameterWithOriginalEOL", EOL.ORIGINAL);
                    }

                    @Test
                    @DisplayName("parameter annotated with EOL.NONE")
                    void testParameterAnnotatedWithNone() {
                        testMethod("methodWithParameterWithEmptyEOL", "");
                    }

                    @Test
                    @DisplayName("parameter annotated with custom EOL")
                    void testParameterAnnotatedWithCustom() {
                        testMethod("methodWithParameterWithCustomEOL", "--");
                    }
                }

                @Nested
                @DisplayName("method annotated")
                class MethodAnnotated {

                    @Test
                    @DisplayName("method annotated with EOL.LF")
                    void testMethodAnnotatedWithLF() {
                        testMethod("methodWithLfEOL", "\n");
                    }

                    @Test
                    @DisplayName("method annotated with EOL.CR")
                    void testMethodAnnotatedWithCR() {
                        testMethod("methodWithCrEOL", "\r");
                    }

                    @Test
                    @DisplayName("method annotated with EOL.CRLF")
                    void testMethodAnnotatedWithCRLF() {
                        testMethod("methodWithCrlfEOL", "\r\n");
                    }

                    @Test
                    @DisplayName("method annotated with EOL.SYSTEM")
                    void testMethodAnnotatedWithSystem() {
                        // Ideally this test would be run with @SetSystemProperty to test against multiple values,
                        // but System.lineSeparator() only reads the property once

                        testMethod("methodWithSystemEOL", System.lineSeparator());
                    }

                    @Test
                    @DisplayName("method annotated with EOL.ORIGINAL")
                    void testMethodAnnotatedWithOriginal() {
                        testMethod("methodWithOriginalEOL", EOL.ORIGINAL);
                    }

                    @Test
                    @DisplayName("method annotated with EOL.NONE")
                    void testMethodAnnotatedWithNone() {
                        testMethod("methodWithEmptyEOL", "");
                    }

                    @Test
                    @DisplayName("method annotated with custom EOL")
                    void testMethodAnnotatedWithCustom() {
                        testMethod("methodWithCustomEOL", "--");
                    }
                }

                private void testMethod(String methodName, String expectedLineSeparator) {
                    testMethod(methodName, context(), expectedLineSeparator);
                }

                private void testMethod(String methodName, ExtensionContext context, String expectedLineSeparator) {
                    Method method = assertDoesNotThrow(
                            () -> NonAnnotatedClassWithTestFieldsAndMethods.class.getDeclaredMethod(methodName, String.class));
                    Parameter parameter = method.getParameters()[0];

                    ParameterContext parameterContext = mock(ParameterContext.class);
                    when(parameterContext.getParameter()).thenReturn(parameter);
                    when(parameterContext.getDeclaringExecutable()).thenReturn(method);
                    when(parameterContext.findAnnotation(any())).thenAnswer(i -> AnnotationSupport.findAnnotation(parameter, i.getArgument(0)));
                    when(parameterContext.findRepeatableAnnotations(any()))
                            .thenAnswer(i -> AnnotationSupport.findRepeatableAnnotations(parameter, i.getArgument(0)));

                    InjectionTarget target = InjectionTarget.forParameter(parameterContext);

                    String lineSeparator = lookupLineSeparator(target, context);

                    assertEquals(expectedLineSeparator, lineSeparator);
                }
            }
        }
    }

    @Nested
    @DisplayName("lookupEncoding")
    class LookupEncoding {

        @Nested
        @DisplayName("for field")
        class ForField {

            @Nested
            @DisplayName("class annotated")
            class ClassAnnotated {

                @Test
                @DisplayName("field not annotated")
                void testFieldNotAnnotated() {
                    testField("fieldWithNoAnnotation", "ISO-8851");
                }

                @Test
                @DisplayName("field annotated with Encoding.DEFAULT")
                void testFieldAnnotatedWithDefault() {
                    // Ideally this test would be run with @SetSystemProperty to test against multiple values,
                    // but Charset.defaultCharset() only reads the property once

                    testField("fieldWithDefaultEncoding", Charset.defaultCharset().name());
                }

                @Nested
                @DisplayName("field annotated with Encoding.SYSTEM")
                class FieldAnnotatedWithSystem {

                    @Test
                    @SetSystemProperty(key = "file.encoding", value = "ASCII")
                    void testWithASCII() {
                        testField("fieldWithSystemEncoding", "ASCII");
                    }

                    @Test
                    @SetSystemProperty(key = "file.encoding", value = "ISO-8859-1")
                    void testWithISO88959() {
                        testField("fieldWithSystemEncoding", "ISO-8859-1");
                    }

                    @Test
                    @ClearSystemProperty(key = "file.encoding")
                    void testWithMissingProperty() {
                        InjectionTarget target = target("fieldWithSystemEncoding");
                        ExtensionContext context = context();

                        PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
                                () -> lookupEncoding(target, context));
                        assertEquals("System property [file.encoding] not available", exception.getMessage());
                    }
                }

                @Nested
                @DisplayName("field annotated with Encoding.NATIVE")
                class FieldAnnotatedWithNative {

                    @Test
                    @SetSystemProperty(key = "native.encoding", value = "ASCII")
                    void testWithASCII() {
                        testField("fieldWithNativeEncoding", "ASCII");
                    }

                    @Test
                    @SetSystemProperty(key = "native.encoding", value = "ISO-8859-1")
                    void testWithISO88959() {
                        testField("fieldWithNativeEncoding", "ISO-8859-1");
                    }

                    @Test
                    @ClearSystemProperty(key = "native.encoding")
                    void testWithMissingProperty() {
                        InjectionTarget target = target("fieldWithNativeEncoding");
                        ExtensionContext context = context();

                        PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
                                () -> lookupEncoding(target, context));
                        assertEquals("System property [native.encoding] not available", exception.getMessage());
                    }
                }

                @Test
                @DisplayName("field annotated with custom encoding")
                void testFieldAnnotatedWithCustom() {
                    testField("fieldWithCustomEncoding", "ASCII");
                }

                private void testField(String fieldName, String expectedEncoding) {
                    InjectionTarget target = target(fieldName);

                    String encoding = lookupEncoding(target, context());

                    assertEquals(expectedEncoding, encoding);
                }

                private InjectionTarget target(String fieldName) {
                    Field field = assertDoesNotThrow(() -> AnnotatedClassWithTestFieldsAndMethods.class.getDeclaredField(fieldName));
                    return InjectionTarget.forField(field);
                }
            }

            @Nested
            @DisplayName("class not annotated")
            class ClassNotAnnotated {

                @Nested
                @DisplayName("field not annotated")
                class FieldNotAnnotated {

                    @Test
                    @DisplayName("no configuration parameter")
                    void testNoConfigurationParameter() {
                        testField(context(), "UTF-8");
                    }

                    @Test
                    @DisplayName("configuration parameter DEFAULT")
                    void testConfigurationParameterDefault() {
                        // Ideally this test would be run with @SetSystemProperty to test against multiple values,
                        // but Charset.defaultCharset() only reads the property once

                        testField("DEFAULT", Charset.defaultCharset().name());
                    }

                    @Nested
                    @DisplayName("configuration parameter SYSTEM")
                    class ConfigurationParameterSystem {

                        @Test
                        @SetSystemProperty(key = "file.encoding", value = "ASCII")
                        void testWithASCII() {
                            testField("SYSTEM", "ASCII");
                        }

                        @Test
                        @SetSystemProperty(key = "file.encoding", value = "ISO-8859-1")
                        void testWithISO88959() {
                            testField("SYSTEM", "ISO-8859-1");
                        }

                        @Test
                        @ClearSystemProperty(key = "file.encoding")
                        void testWithMissingProperty() {
                            InjectionTarget target = target("fieldWithNoAnnotation");
                            ExtensionContext context = context(Encoding.DEFAULT_ENCODING_PROPERTY_NAME, "SYSTEM");

                            PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
                                    () -> lookupEncoding(target, context));
                            assertEquals("System property [file.encoding] not available", exception.getMessage());
                        }
                    }

                    @Nested
                    @DisplayName("configuration parameter NATIVE")
                    class ConfigurationParameterNative {

                        @Test
                        @SetSystemProperty(key = "native.encoding", value = "ASCII")
                        void testWithASCII() {
                            testField("NATIVE", "ASCII");
                        }

                        @Test
                        @SetSystemProperty(key = "native.encoding", value = "ISO-8859-1")
                        void testWithISO88959() {
                            testField("NATIVE", "ISO-8859-1");
                        }

                        @Test
                        @ClearSystemProperty(key = "native.encoding")
                        void testWithMissingProperty() {
                            InjectionTarget target = target("fieldWithNoAnnotation");
                            ExtensionContext context = context(Encoding.DEFAULT_ENCODING_PROPERTY_NAME, "NATIVE");

                            PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
                                    () -> lookupEncoding(target, context));
                            assertEquals("System property [native.encoding] not available", exception.getMessage());
                        }
                    }

                    @Test
                    @DisplayName("custom configuration parameter")
                    void testCustomConfigurationParameter() {
                        testField("ASCII", "ASCII");
                    }

                    private void testField(String configurationParameterValue, String expectedEncoding) {
                        testField(context(Encoding.DEFAULT_ENCODING_PROPERTY_NAME, configurationParameterValue), expectedEncoding);
                    }

                    private void testField(ExtensionContext context, String expectedEncoding) {
                        ClassNotAnnotated.this.testField("fieldWithNoAnnotation", context, expectedEncoding);
                    }
                }

                @Test
                @DisplayName("field annotated with Encoding.DEFAULT")
                void testFieldAnnotatedWithDefault() {
                    // Ideally this test would be run with @SetSystemProperty to test against multiple values,
                    // but Charset.defaultCharset() only reads the property once

                    testField("fieldWithDefaultEncoding", Charset.defaultCharset().name());
                }

                @Nested
                @DisplayName("field annotated with Encoding.SYSTEM")
                class FieldAnnotatedWithSystem {

                    @Test
                    @SetSystemProperty(key = "file.encoding", value = "ASCII")
                    void testWithASCII() {
                        testField("fieldWithSystemEncoding", "ASCII");
                    }

                    @Test
                    @SetSystemProperty(key = "file.encoding", value = "ISO-8859-1")
                    void testWithISO88959() {
                        testField("fieldWithSystemEncoding", "ISO-8859-1");
                    }

                    @Test
                    @ClearSystemProperty(key = "file.encoding")
                    void testWithMissingProperty() {
                        InjectionTarget target = target("fieldWithSystemEncoding");
                        ExtensionContext context = context();

                        PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
                                () -> lookupEncoding(target, context));
                        assertEquals("System property [file.encoding] not available", exception.getMessage());
                    }
                }

                @Nested
                @DisplayName("field annotated with Encoding.NATIVE")
                class FieldAnnotatedWithNative {

                    @Test
                    @SetSystemProperty(key = "native.encoding", value = "ASCII")
                    void testWithASCII() {
                        testField("fieldWithNativeEncoding", "ASCII");
                    }

                    @Test
                    @SetSystemProperty(key = "native.encoding", value = "ISO-8859-1")
                    void testWithISO88959() {
                        testField("fieldWithNativeEncoding", "ISO-8859-1");
                    }

                    @Test
                    @ClearSystemProperty(key = "native.encoding")
                    void testWithMissingProperty() {
                        InjectionTarget target = target("fieldWithNativeEncoding");
                        ExtensionContext context = context();

                        PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
                                () -> lookupEncoding(target, context));
                        assertEquals("System property [native.encoding] not available", exception.getMessage());
                    }
                }

                @Test
                @DisplayName("field annotated with custom encoding")
                void testFieldAnnotatedWithCustom() {
                    testField("fieldWithCustomEncoding", "ASCII");
                }

                private void testField(String fieldName, String expectedEncoding) {
                    testField(fieldName, context(), expectedEncoding);
                }

                private void testField(String fieldName, ExtensionContext context, String expectedEncoding) {
                    InjectionTarget target = target(fieldName);

                    String encoding = lookupEncoding(target, context);

                    assertEquals(expectedEncoding, encoding);
                }

                private InjectionTarget target(String fieldName) {
                    Field field = assertDoesNotThrow(() -> NonAnnotatedClassWithTestFieldsAndMethods.class.getDeclaredField(fieldName));
                    return InjectionTarget.forField(field);
                }
            }
        }

        @Nested
        @DisplayName("for method")
        class ForMethod {

            @Nested
            @DisplayName("class annotated")
            class ClassAnnotated {

                @Test
                @DisplayName("method with no annotations")
                void testMethodWithNoAnnotations() {
                    testMethod("methodWithNoAnnotations", "ISO-8851");
                }

                @Nested
                @DisplayName("parameter annotated")
                class ParameterAnnotated {

                    @Test
                    @DisplayName("parameter annotated with Encoding.DEFAULT")
                    void testParameterAnnotatedWithDefault() {
                        // Ideally this test would be run with @SetSystemProperty to test against multiple values,
                        // but Charset.defaultCharset() only reads the property once

                        testMethod("methodWithParameterWithDefaultEncoding", Charset.defaultCharset().name());
                    }

                    @Nested
                    @DisplayName("parameter annotated with Encoding.SYSTEM")
                    class ParameterAnnotatedWithSystem {

                        @Test
                        @SetSystemProperty(key = "file.encoding", value = "ASCII")
                        void testWithASCII() {
                            testMethod("methodWithParameterWithSystemEncoding", "ASCII");
                        }

                        @Test
                        @SetSystemProperty(key = "file.encoding", value = "ISO-8859-1")
                        void testWithISO88959() {
                            testMethod("methodWithParameterWithSystemEncoding", "ISO-8859-1");
                        }

                        @Test
                        @ClearSystemProperty(key = "file.encoding")
                        void testWithMissingProperty() {
                            InjectionTarget target = target("methodWithParameterWithSystemEncoding");
                            ExtensionContext context = context();

                            PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
                                    () -> lookupEncoding(target, context));
                            assertEquals("System property [file.encoding] not available", exception.getMessage());
                        }
                    }

                    @Nested
                    @DisplayName("parameter annotated with Encoding.NATIVE")
                    class ParameterAnnotatedWithNative {

                        @Test
                        @SetSystemProperty(key = "native.encoding", value = "ASCII")
                        void testWithASCII() {
                            testMethod("methodWithParameterWithNativeEncoding", "ASCII");
                        }

                        @Test
                        @SetSystemProperty(key = "native.encoding", value = "ISO-8859-1")
                        void testWithISO88959() {
                            testMethod("methodWithParameterWithNativeEncoding", "ISO-8859-1");
                        }

                        @Test
                        @ClearSystemProperty(key = "native.encoding")
                        void testWithMissingProperty() {
                            InjectionTarget target = target("methodWithParameterWithNativeEncoding");
                            ExtensionContext context = context();

                            PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
                                    () -> lookupEncoding(target, context));
                            assertEquals("System property [native.encoding] not available", exception.getMessage());
                        }
                    }

                    @Test
                    @DisplayName("parameter annotated with custom encoding")
                    void testParameterAnnotatedWithCustom() {
                        testMethod("methodWithParameterWithCustomEncoding", "ASCII");
                    }
                }

                @Nested
                @DisplayName("method annotated")
                class MethodAnnotated {

                    @Test
                    @DisplayName("method annotated with Encoding.DEFAULT")
                    void testMethodAnnotatedWithDefault() {
                        // Ideally this test would be run with @SetSystemProperty to test against multiple values,
                        // but Charset.defaultCharset() only reads the property once

                        testMethod("methodWithDefaultEncoding", Charset.defaultCharset().name());
                    }

                    @Nested
                    @DisplayName("method annotated with Encoding.SYSTEM")
                    class MethodAnnotatedWithSystem {

                        @Test
                        @SetSystemProperty(key = "file.encoding", value = "ASCII")
                        void testWithASCII() {
                            testMethod("methodWithSystemEncoding", "ASCII");
                        }

                        @Test
                        @SetSystemProperty(key = "file.encoding", value = "ISO-8859-1")
                        void testWithISO88959() {
                            testMethod("methodWithSystemEncoding", "ISO-8859-1");
                        }

                        @Test
                        @ClearSystemProperty(key = "file.encoding")
                        void testWithMissingProperty() {
                            InjectionTarget target = target("methodWithSystemEncoding");
                            ExtensionContext context = context();

                            PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
                                    () -> lookupEncoding(target, context));
                            assertEquals("System property [file.encoding] not available", exception.getMessage());
                        }
                    }

                    @Nested
                    @DisplayName("method annotated with Encoding.NATIVE")
                    class MethodAnnotatedWithNative {

                        @Test
                        @SetSystemProperty(key = "native.encoding", value = "ASCII")
                        void testWithASCII() {
                            testMethod("methodWithNativeEncoding", "ASCII");
                        }

                        @Test
                        @SetSystemProperty(key = "native.encoding", value = "ISO-8859-1")
                        void testWithISO88959() {
                            testMethod("methodWithNativeEncoding", "ISO-8859-1");
                        }

                        @Test
                        @ClearSystemProperty(key = "native.encoding")
                        void testWithMissingProperty() {
                            InjectionTarget target = target("methodWithNativeEncoding");
                            ExtensionContext context = context();

                            PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
                                    () -> lookupEncoding(target, context));
                            assertEquals("System property [native.encoding] not available", exception.getMessage());
                        }
                    }

                    @Test
                    @DisplayName("method annotated with custom encoding")
                    void testMethodAnnotatedWithCustom() {
                        testMethod("methodWithCustomEncoding", "ASCII");
                    }
                }

                private void testMethod(String methodName, String expectedEncoding) {
                    InjectionTarget target = target(methodName);

                    String encoding = lookupEncoding(target, context());

                    assertEquals(expectedEncoding, encoding);
                }

                private InjectionTarget target(String methodName) {
                    Method method = assertDoesNotThrow(
                            () -> AnnotatedClassWithTestFieldsAndMethods.class.getDeclaredMethod(methodName, String.class));
                    Parameter parameter = method.getParameters()[0];

                    ParameterContext context = mock(ParameterContext.class);
                    when(context.getParameter()).thenReturn(parameter);
                    when(context.getDeclaringExecutable()).thenReturn(method);
                    when(context.findAnnotation(any())).thenAnswer(i -> AnnotationSupport.findAnnotation(parameter, i.getArgument(0)));
                    when(context.findRepeatableAnnotations(any()))
                            .thenAnswer(i -> AnnotationSupport.findRepeatableAnnotations(parameter, i.getArgument(0)));

                    return InjectionTarget.forParameter(context);
                }
            }

            @Nested
            @DisplayName("class not annotated")
            class ClassNotAnnotated {

                @Nested
                @DisplayName("method with no annotations")
                class MethodWithNoAnnotations {

                    @Test
                    @DisplayName("no configuration parameter")
                    void testNoConfigurationParameter() {
                        testMethod(context(), "UTF-8");
                    }

                    @Test
                    @DisplayName("configuration parameter DEFAULT")
                    void testConfigurationParameterDefault() {
                        // Ideally this test would be run with @SetSystemProperty to test against multiple values,
                        // but Charset.defaultCharset() only reads the property once

                        testMethod("DEFAULT", Charset.defaultCharset().name());
                    }

                    @Nested
                    @DisplayName("configuration parameter SYSTEM")
                    class ConfigurationParameterSystem {

                        @Test
                        @SetSystemProperty(key = "file.encoding", value = "ASCII")
                        void testWithASCII() {
                            testMethod("SYSTEM", "ASCII");
                        }

                        @Test
                        @SetSystemProperty(key = "file.encoding", value = "ISO-8859-1")
                        void testWithISO88959() {
                            testMethod("SYSTEM", "ISO-8859-1");
                        }

                        @Test
                        @ClearSystemProperty(key = "file.encoding")
                        void testWithMissingProperty() {
                            InjectionTarget target = target("methodWithNoAnnotations");
                            ExtensionContext context = context(Encoding.DEFAULT_ENCODING_PROPERTY_NAME, "SYSTEM");

                            PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
                                    () -> lookupEncoding(target, context));
                            assertEquals("System property [file.encoding] not available", exception.getMessage());
                        }
                    }

                    @Nested
                    @DisplayName("configuration parameter NATIVE")
                    class ConfigurationParameterNative {

                        @Test
                        @SetSystemProperty(key = "native.encoding", value = "ASCII")
                        void testWithASCII() {
                            testMethod("NATIVE", "ASCII");
                        }

                        @Test
                        @SetSystemProperty(key = "native.encoding", value = "ISO-8859-1")
                        void testWithISO88959() {
                            testMethod("NATIVE", "ISO-8859-1");
                        }

                        @Test
                        @ClearSystemProperty(key = "native.encoding")
                        void testWithMissingProperty() {
                            InjectionTarget target = target("methodWithNoAnnotations");
                            ExtensionContext context = context(Encoding.DEFAULT_ENCODING_PROPERTY_NAME, "NATIVE");

                            PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
                                    () -> lookupEncoding(target, context));
                            assertEquals("System property [native.encoding] not available", exception.getMessage());
                        }
                    }

                    @Test
                    @DisplayName("custom configuration parameter")
                    void testCustomConfigurationParameter() {
                        testMethod("ASCII", "ASCII");
                    }

                    private void testMethod(String configurationParameterValue, String expectedEncoding) {
                        testMethod(context(Encoding.DEFAULT_ENCODING_PROPERTY_NAME, configurationParameterValue), expectedEncoding);
                    }

                    private void testMethod(ExtensionContext context, String expectedEncoding) {
                        ClassNotAnnotated.this.testMethod("methodWithNoAnnotations", context, expectedEncoding);
                    }
                }

                @Nested
                @DisplayName("parameter annotated")
                class ParameterAnnotated {

                    @Test
                    @DisplayName("parameter annotated with Encoding.DEFAULT")
                    void testParameterAnnotatedWithDefault() {
                        // Ideally this test would be run with @SetSystemProperty to test against multiple values,
                        // but Charset.defaultCharset() only reads the property once

                        testMethod("methodWithParameterWithDefaultEncoding", Charset.defaultCharset().name());
                    }

                    @Nested
                    @DisplayName("parameter annotated with Encoding.SYSTEM")
                    class ParameterAnnotatedWithSystem {

                        @Test
                        @SetSystemProperty(key = "file.encoding", value = "ASCII")
                        void testWithASCII() {
                            testMethod("methodWithParameterWithSystemEncoding", "ASCII");
                        }

                        @Test
                        @SetSystemProperty(key = "file.encoding", value = "ISO-8859-1")
                        void testWithISO88959() {
                            testMethod("methodWithParameterWithSystemEncoding", "ISO-8859-1");
                        }

                        @Test
                        @ClearSystemProperty(key = "file.encoding")
                        void testWithMissingProperty() {
                            InjectionTarget target = target("methodWithParameterWithSystemEncoding");
                            ExtensionContext context = context();

                            PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
                                    () -> lookupEncoding(target, context));
                            assertEquals("System property [file.encoding] not available", exception.getMessage());
                        }
                    }

                    @Nested
                    @DisplayName("parameter annotated with Encoding.NATIVE")
                    class ParameterAnnotatedWithNative {

                        @Test
                        @SetSystemProperty(key = "native.encoding", value = "ASCII")
                        void testWithASCII() {
                            testMethod("methodWithParameterWithNativeEncoding", "ASCII");
                        }

                        @Test
                        @SetSystemProperty(key = "native.encoding", value = "ISO-8859-1")
                        void testWithISO88959() {
                            testMethod("methodWithParameterWithNativeEncoding", "ISO-8859-1");
                        }

                        @Test
                        @ClearSystemProperty(key = "native.encoding")
                        void testWithMissingProperty() {
                            InjectionTarget target = target("methodWithParameterWithNativeEncoding");
                            ExtensionContext context = context();

                            PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
                                    () -> lookupEncoding(target, context));
                            assertEquals("System property [native.encoding] not available", exception.getMessage());
                        }
                    }

                    @Test
                    @DisplayName("parameter annotated with custom encoding")
                    void testParameterAnnotatedWithCustom() {
                        testMethod("methodWithParameterWithCustomEncoding", "ASCII");
                    }
                }

                @Nested
                @DisplayName("method annotated")
                class MethodAnnotated {

                    @Test
                    @DisplayName("method annotated with Encoding.DEFAULT")
                    void testMethodAnnotatedWithDefault() {
                        // Ideally this test would be run with @SetSystemProperty to test against multiple values,
                        // but Charset.defaultCharset() only reads the property once

                        testMethod("methodWithDefaultEncoding", Charset.defaultCharset().name());
                    }

                    @Nested
                    @DisplayName("method annotated with Encoding.SYSTEM")
                    class MethodAnnotatedWithSystem {

                        @Test
                        @SetSystemProperty(key = "file.encoding", value = "ASCII")
                        void testWithASCII() {
                            testMethod("methodWithSystemEncoding", "ASCII");
                        }

                        @Test
                        @SetSystemProperty(key = "file.encoding", value = "ISO-8859-1")
                        void testWithISO88959() {
                            testMethod("methodWithSystemEncoding", "ISO-8859-1");
                        }

                        @Test
                        @ClearSystemProperty(key = "file.encoding")
                        void testWithMissingProperty() {
                            InjectionTarget target = target("methodWithSystemEncoding");
                            ExtensionContext context = context();

                            PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
                                    () -> lookupEncoding(target, context));
                            assertEquals("System property [file.encoding] not available", exception.getMessage());
                        }
                    }

                    @Nested
                    @DisplayName("method annotated with Encoding.NATIVE")
                    class MethodAnnotatedWithNative {

                        @Test
                        @SetSystemProperty(key = "native.encoding", value = "ASCII")
                        void testWithASCII() {
                            testMethod("methodWithNativeEncoding", "ASCII");
                        }

                        @Test
                        @SetSystemProperty(key = "native.encoding", value = "ISO-8859-1")
                        void testWithISO88959() {
                            testMethod("methodWithNativeEncoding", "ISO-8859-1");
                        }

                        @Test
                        @ClearSystemProperty(key = "native.encoding")
                        void testWithMissingProperty() {
                            InjectionTarget target = target("methodWithNativeEncoding");
                            ExtensionContext context = context();

                            PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
                                    () -> lookupEncoding(target, context));
                            assertEquals("System property [native.encoding] not available", exception.getMessage());
                        }
                    }

                    @Test
                    @DisplayName("method annotated with custom encoding")
                    void testMethodAnnotatedWithCustom() {
                        testMethod("methodWithCustomEncoding", "ASCII");
                    }
                }

                private void testMethod(String methodName, String expectedEncoding) {
                    testMethod(methodName, context(), expectedEncoding);
                }

                private void testMethod(String methodName, ExtensionContext context, String expectedEncoding) {
                    InjectionTarget target = target(methodName);

                    String encoding = lookupEncoding(target, context);

                    assertEquals(expectedEncoding, encoding);
                }

                private InjectionTarget target(String methodName) {
                    Method method = assertDoesNotThrow(
                            () -> NonAnnotatedClassWithTestFieldsAndMethods.class.getDeclaredMethod(methodName, String.class));
                    Parameter parameter = method.getParameters()[0];

                    ParameterContext parameterContext = mock(ParameterContext.class);
                    when(parameterContext.getParameter()).thenReturn(parameter);
                    when(parameterContext.getDeclaringExecutable()).thenReturn(method);
                    when(parameterContext.findAnnotation(any())).thenAnswer(i -> AnnotationSupport.findAnnotation(parameter, i.getArgument(0)));
                    when(parameterContext.findRepeatableAnnotations(any()))
                            .thenAnswer(i -> AnnotationSupport.findRepeatableAnnotations(parameter, i.getArgument(0)));

                    return InjectionTarget.forParameter(parameterContext);
                }
            }
        }
    }

    private ExtensionContext context(String... configurationParameters) {
        ExtensionContext context = mock(ExtensionContext.class);
        when(context.getConfigurationParameter(any())).thenReturn(Optional.empty());
        for (int i = 0; i < configurationParameters.length; i += 2) {
            when(context.getConfigurationParameter(configurationParameters[i])).thenReturn(Optional.of(configurationParameters[i + 1]));
        }
        return context;
    }

    @EOL("--class--")
    @Encoding("ISO-8851")
    private static final class AnnotatedClassWithTestFieldsAndMethods {

        String fieldWithNoAnnotation;

        @EOL(EOL.LF)
        String fieldWithLfEOL;

        @EOL(EOL.CR)
        String fieldWithCrEOL;

        @EOL(EOL.CRLF)
        String fieldWithCrlfEOL;

        @EOL(EOL.SYSTEM)
        String fieldWithSystemEOL;

        @EOL(EOL.ORIGINAL)
        String fieldWithOriginalEOL;

        @EOL(EOL.NONE)
        String fieldWithEmptyEOL;

        @EOL("--")
        String fieldWithCustomEOL;

        @Encoding(Encoding.DEFAULT)
        String fieldWithDefaultEncoding;

        @Encoding(Encoding.SYSTEM)
        String fieldWithSystemEncoding;

        @Encoding(Encoding.NATIVE)
        String fieldWithNativeEncoding;

        @Encoding("ASCII")
        String fieldWithCustomEncoding;

        void methodWithNoAnnotations(String parameter) {
            // no body
        }

        void methodWithParameterWithLfEOL(@EOL(EOL.LF) String parameter) {
            // no body
        }

        void methodWithParameterWithCrEOL(@EOL(EOL.CR) String parameter) {
            // no body
        }

        void methodWithParameterWithCrlfEOL(@EOL(EOL.CRLF) String parameter) {
            // no body
        }

        void methodWithParameterWithSystemEOL(@EOL(EOL.SYSTEM) String parameter) {
            // no body
        }

        void methodWithParameterWithOriginalEOL(@EOL(EOL.ORIGINAL) String parameter) {
            // no body
        }

        void methodWithParameterWithEmptyEOL(@EOL(EOL.NONE) String parameter) {
            // no body
        }

        void methodWithParameterWithCustomEOL(@EOL("--") String parameter) {
            // no body
        }

        void methodWithParameterWithDefaultEncoding(@Encoding(Encoding.DEFAULT) String parameter) {
            // no body
        }

        void methodWithParameterWithSystemEncoding(@Encoding(Encoding.SYSTEM) String parameter) {
            // no body
        }

        void methodWithParameterWithNativeEncoding(@Encoding(Encoding.NATIVE) String parameter) {
            // no body
        }

        void methodWithParameterWithCustomEncoding(@Encoding("ASCII") String parameter) {
            // no body
        }

        @EOL(EOL.LF)
        void methodWithLfEOL(String parameter) {
            // no body
        }

        @EOL(EOL.CR)
        void methodWithCrEOL(String parameter) {
            // no body
        }

        @EOL(EOL.CRLF)
        void methodWithCrlfEOL(String parameter) {
            // no body
        }

        @EOL(EOL.SYSTEM)
        void methodWithSystemEOL(String parameter) {
            // no body
        }

        @EOL(EOL.ORIGINAL)
        void methodWithOriginalEOL(String parameter) {
            // no body
        }

        @EOL(EOL.NONE)
        void methodWithEmptyEOL(String parameter) {
            // no body
        }

        @EOL("--")
        void methodWithCustomEOL(String parameter) {
            // no body
        }

        @Encoding(Encoding.DEFAULT)
        void methodWithDefaultEncoding(String parameter) {
            // no body
        }

        @Encoding(Encoding.SYSTEM)
        void methodWithSystemEncoding(String parameter) {
            // no body
        }

        @Encoding(Encoding.NATIVE)
        void methodWithNativeEncoding(String parameter) {
            // no body
        }

        @Encoding("ASCII")
        void methodWithCustomEncoding(String parameter) {
            // no body
        }
    }

    private static final class NonAnnotatedClassWithTestFieldsAndMethods {

        String fieldWithNoAnnotation;

        @EOL(EOL.LF)
        String fieldWithLfEOL;

        @EOL(EOL.CR)
        String fieldWithCrEOL;

        @EOL(EOL.CRLF)
        String fieldWithCrlfEOL;

        @EOL(EOL.SYSTEM)
        String fieldWithSystemEOL;

        @EOL(EOL.ORIGINAL)
        String fieldWithOriginalEOL;

        @EOL(EOL.NONE)
        String fieldWithEmptyEOL;

        @EOL("--")
        String fieldWithCustomEOL;

        @Encoding(Encoding.DEFAULT)
        String fieldWithDefaultEncoding;

        @Encoding(Encoding.SYSTEM)
        String fieldWithSystemEncoding;

        @Encoding(Encoding.NATIVE)
        String fieldWithNativeEncoding;

        @Encoding("ASCII")
        String fieldWithCustomEncoding;

        void methodWithNoAnnotations(String parameter) {
            // no body
        }

        void methodWithParameterWithLfEOL(@EOL(EOL.LF) String parameter) {
            // no body
        }

        void methodWithParameterWithCrEOL(@EOL(EOL.CR) String parameter) {
            // no body
        }

        void methodWithParameterWithCrlfEOL(@EOL(EOL.CRLF) String parameter) {
            // no body
        }

        void methodWithParameterWithSystemEOL(@EOL(EOL.SYSTEM) String parameter) {
            // no body
        }

        void methodWithParameterWithOriginalEOL(@EOL(EOL.ORIGINAL) String parameter) {
            // no body
        }

        void methodWithParameterWithEmptyEOL(@EOL(EOL.NONE) String parameter) {
            // no body
        }

        void methodWithParameterWithCustomEOL(@EOL("--") String parameter) {
            // no body
        }

        void methodWithParameterWithDefaultEncoding(@Encoding(Encoding.DEFAULT) String parameter) {
            // no body
        }

        void methodWithParameterWithSystemEncoding(@Encoding(Encoding.SYSTEM) String parameter) {
            // no body
        }

        void methodWithParameterWithNativeEncoding(@Encoding(Encoding.NATIVE) String parameter) {
            // no body
        }

        void methodWithParameterWithCustomEncoding(@Encoding("ASCII") String parameter) {
            // no body
        }

        @EOL(EOL.LF)
        void methodWithLfEOL(String parameter) {
            // no body
        }

        @EOL(EOL.CR)
        void methodWithCrEOL(String parameter) {
            // no body
        }

        @EOL(EOL.CRLF)
        void methodWithCrlfEOL(String parameter) {
            // no body
        }

        @EOL(EOL.SYSTEM)
        void methodWithSystemEOL(String parameter) {
            // no body
        }

        @EOL(EOL.ORIGINAL)
        void methodWithOriginalEOL(String parameter) {
            // no body
        }

        @EOL(EOL.NONE)
        void methodWithEmptyEOL(String parameter) {
            // no body
        }

        @EOL("--")
        void methodWithCustomEOL(String parameter) {
            // no body
        }

        @Encoding(Encoding.DEFAULT)
        void methodWithDefaultEncoding(String parameter) {
            // no body
        }

        @Encoding(Encoding.SYSTEM)
        void methodWithSystemEncoding(String parameter) {
            // no body
        }

        @Encoding(Encoding.NATIVE)
        void methodWithNativeEncoding(String parameter) {
            // no body
        }

        @Encoding("ASCII")
        void methodWithCustomEncoding(String parameter) {
            // no body
        }
    }
}
