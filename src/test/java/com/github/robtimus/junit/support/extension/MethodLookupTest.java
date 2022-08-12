/*
 * MethodLookupTest.java
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

package com.github.robtimus.junit.support.extension;

import static com.github.robtimus.junit.support.extension.MethodLookup.METHOD_REFERENCE_PATTERN;
import static com.github.robtimus.junit.support.extension.MethodLookup.className;
import static com.github.robtimus.junit.support.extension.MethodLookup.findMethod;
import static com.github.robtimus.junit.support.extension.MethodLookup.methodArguments;
import static com.github.robtimus.junit.support.extension.MethodLookup.methodName;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;
import org.junitpioneer.jupiter.cartesian.CartesianTest;

@SuppressWarnings("nls")
final class MethodLookupTest {

    private MethodLookupTest() {
    }

    @Nested
    @DisplayName("METHOD_REFERENCE_PATTERN")
    class MethodReferencePattern {

        @Nested
        @DisplayName("valid input")
        class ValidInput {

            @ParameterizedTest(name = "{0}")
            @ValueSource(strings = { "init", "of", "UPPERCASE", "x", "x1", "_x", "$" })
            @DisplayName("with method name")
            void testWithMethodName(String methodName) {
                Matcher matcher = METHOD_REFERENCE_PATTERN.matcher(methodName);
                assertTrue(matcher.matches());

                assertNull(className(matcher));
                assertEquals(methodName, methodName(matcher));
                assertNull(methodArguments(matcher));
            }

            @CartesianTest(name = "{0}{1}")
            @DisplayName("with method name and parameters")
            void testWithMethodNameAndParameters(
                    @CartesianTest.Values(strings = { "init", "of", "UPPERCASE", "x", "x1", "_x", "$" }) String methodName,
                    @CartesianTest.Values(strings = { "()", "(int)", "(java.lang.String)", "(int, java.lang.String)" }) String arguments) {

                Matcher matcher = METHOD_REFERENCE_PATTERN.matcher(methodName + arguments);
                assertTrue(matcher.matches());

                assertNull(className(matcher));
                assertEquals(methodName, methodName(matcher));
                assertEquals(arguments, "(" + methodArguments(matcher) + ")");
            }

            @CartesianTest(name = "{0}#{1}")
            @DisplayName("with class name and method name")
            void testWithClassNameAndMethodName(
                    @CartesianTest.Values(strings = { "int", "java.lang.String" }) String className,
                    @CartesianTest.Values(strings = { "init", "of", "UPPERCASE", "x", "x1", "_x", "$" }) String methodName) {

                Matcher matcher = METHOD_REFERENCE_PATTERN.matcher(className + "#" + methodName);
                assertTrue(matcher.matches());

                assertEquals(className, className(matcher));
                assertEquals(methodName, methodName(matcher));
                assertNull(methodArguments(matcher));
            }

            @CartesianTest(name = "{0}#{1}{2}")
            @DisplayName("with class name, method name and parameters")
            void testWithClassNameAndMethodNameAndParameters(
                    @CartesianTest.Values(strings = { "int", "java.lang.String" }) String className,
                    @CartesianTest.Values(strings = { "init", "of", "UPPERCASE", "x", "x1", "_x", "$" }) String methodName,
                    @CartesianTest.Values(strings = { "()", "(int)", "(java.lang.String)", "(int, java.lang.String)" }) String arguments) {

                Matcher matcher = METHOD_REFERENCE_PATTERN.matcher(className + "#" + methodName + arguments);
                assertTrue(matcher.matches());

                assertEquals(className, className(matcher));
                assertEquals(methodName, methodName(matcher));
                assertEquals(arguments, "(" + methodArguments(matcher) + ")");
            }
        }

        @Nested
        @DisplayName("invalid input")
        class InvalidInput {

            @ParameterizedTest(name = "{0}")
            @ValueSource(strings = { "3init", "o-f", "method name" })
            @DisplayName("with invalid method name")
            void testWithInvalidMethodName(String methodName) {
                Matcher matcher = METHOD_REFERENCE_PATTERN.matcher(methodName);
                assertFalse(matcher.matches());
            }

            @CartesianTest(name = "{0}{1}")
            @DisplayName("with invalid method name and parameters")
            void testWithMethodNameAndParameters(
                    @CartesianTest.Values(strings = { "3init", "o-f", "method name" }) String methodName,
                    @CartesianTest.Values(strings = { "()", "(int)", "(java.lang.String)", "(int, java.lang.String)" }) String arguments) {

                Matcher matcher = METHOD_REFERENCE_PATTERN.matcher(methodName + arguments);
                assertFalse(matcher.matches());
            }

            @CartesianTest(name = "{0}{1}")
            @DisplayName("with method name and invalid parameters")
            void testWithMethodNameAndInvalidParameters(
                    @CartesianTest.Values(strings = { "init", "of", "UPPERCASE", "x", "x1", "_x", "$" }) String methodName,
                    @CartesianTest.Values(strings = { "(", "(3int)", "(java lang String)", "(int; java.lang.String)" }) String arguments) {

                Matcher matcher = METHOD_REFERENCE_PATTERN.matcher(methodName + arguments);
                assertFalse(matcher.matches());
            }

            @CartesianTest(name = "{0}#{1}")
            @DisplayName("with invalid class name and method name")
            void testWithInvalidClassNameAndMethodName(
                    @CartesianTest.Values(strings = { "3int", "java lang String" }) String className,
                    @CartesianTest.Values(strings = { "init", "of", "UPPERCASE", "x", "x1", "_x", "$" }) String methodName) {

                Matcher matcher = METHOD_REFERENCE_PATTERN.matcher(className + "#" + methodName);
                assertFalse(matcher.matches());
            }

            @CartesianTest(name = "{0}#{1}")
            @DisplayName("with class name and invalid method name")
            void testWithClassNameAndInvalidMethodName(
                    @CartesianTest.Values(strings = { "int", "java.lang.String" }) String className,
                    @CartesianTest.Values(strings = { "3init", "o-f", "method name" }) String methodName) {

                Matcher matcher = METHOD_REFERENCE_PATTERN.matcher(className + "#" + methodName);
                assertFalse(matcher.matches());
            }

            @CartesianTest(name = "{0}#{1}{2}")
            @DisplayName("with invalid class name, method name and parameters")
            void testWithInvalidClassNameAndMethodNameAndParameters(
                    @CartesianTest.Values(strings = { "3int", "java lang String" }) String className,
                    @CartesianTest.Values(strings = { "init", "of", "UPPERCASE", "x", "x1", "_x", "$" }) String methodName,
                    @CartesianTest.Values(strings = { "()", "(int)", "(java.lang.String)", "(int, java.lang.String)" }) String arguments) {

                Matcher matcher = METHOD_REFERENCE_PATTERN.matcher(className + "#" + methodName + arguments);
                assertFalse(matcher.matches());
            }

            @CartesianTest(name = "{0}#{1}{2}")
            @DisplayName("with class name, invalid method name and parameters")
            void testWithClassNameAndInvalidMethodNameAndParameters(
                    @CartesianTest.Values(strings = { "int", "java.lang.String" }) String className,
                    @CartesianTest.Values(strings = { "3init", "o-f", "method name" }) String methodName,
                    @CartesianTest.Values(strings = { "()", "(int)", "(java.lang.String)", "(int, java.lang.String)" }) String arguments) {

                Matcher matcher = METHOD_REFERENCE_PATTERN.matcher(className + "#" + methodName + arguments);
                assertFalse(matcher.matches());
            }

            @CartesianTest(name = "{0}#{1}{2}")
            @DisplayName("with class name, method name and invalid parameters")
            void testWithClassNameAndMethodNameAndParameters(
                    @CartesianTest.Values(strings = { "int", "java.lang.String" }) String className,
                    @CartesianTest.Values(strings = { "init", "of", "UPPERCASE", "x", "x1", "_x", "$" }) String methodName,
                    @CartesianTest.Values(strings = { "(", "(3int)", "(java lang String)", "(int; java.lang.String)" }) String arguments) {

                Matcher matcher = METHOD_REFERENCE_PATTERN.matcher(className + "#" + methodName + arguments);
                assertFalse(matcher.matches());
            }
        }
    }

    @Nested
    @DisplayName("find(String, ExtensionContext)")
    class Find {

        @Test
        @DisplayName("null method reference")
        void testNullMethodReference() {
            MethodLookup lookup = MethodLookup.withParameterTypes(String.class, int.class)
                    .orParameterTypes(String.class);

            ExtensionContext context = mock(ExtensionContext.class);

            PreconditionViolationException exception = assertThrows(PreconditionViolationException.class, () -> lookup.find(null, context));
            assertEquals("methodReference must not be null or blank", exception.getMessage());
        }

        @Test
        @DisplayName("blank method reference")
        void testBlankMethodReference() {
            MethodLookup lookup = MethodLookup.withParameterTypes(String.class, int.class)
                    .orParameterTypes(String.class);

            ExtensionContext context = mock(ExtensionContext.class);

            PreconditionViolationException exception = assertThrows(PreconditionViolationException.class, () -> lookup.find("  ", context));
            assertEquals("methodReference must not be null or blank", exception.getMessage());
        }

        @Test
        @DisplayName("invalid method reference")
        void testInvalidMethodReference() {
            MethodLookup lookup = MethodLookup.withParameterTypes(String.class, int.class)
                    .orParameterTypes(String.class);

            ExtensionContext context = mock(ExtensionContext.class);

            PreconditionViolationException exception = assertThrows(PreconditionViolationException.class, () -> lookup.find("1", context));
            assertEquals("[1] is not a valid method reference: "
                    + "it must be the method name, optionally preceded by a fully qualified class name followed by a '#', "
                    + "and optionally followed by a parameter list enclosed in parentheses.",
                    exception.getMessage());
        }

        @Nested
        @DisplayName("with method name")
        class WithMethodName {

            @Test
            @DisplayName("method found")
            void testMethodFound() {
                MethodLookup lookup = MethodLookup.withParameterTypes(String.class, int.class)
                        .orParameterTypes(String.class);

                ExtensionContext context = mock(ExtensionContext.class);
                doReturn(getClass()).when(context).getRequiredTestClass();

                MethodLookup.Result result = lookup.find("echo", context);
                Method expectedMethod = assertDoesNotThrow(() -> getClass().getDeclaredMethod("echo", String.class));
                assertEquals(expectedMethod, result.method());
                assertEquals(1, result.index());

                result = lookup.find("repeat", context);
                expectedMethod = assertDoesNotThrow(() -> getClass().getDeclaredMethod("repeat", String.class, int.class));
                assertEquals(expectedMethod, result.method());
                assertEquals(0, result.index());
            }

            @Nested
            @DisplayName("method not found")
            class MethodNotFound {

                @Test
                @DisplayName("for single parameter combination")
                void testSingleParameterCombination() {
                    MethodLookup lookup = MethodLookup.withParameterTypes(String.class, int.class);

                    ExtensionContext context = mock(ExtensionContext.class);
                    doReturn(getClass()).when(context).getRequiredTestClass();

                    PreconditionViolationException exception = assertThrows(PreconditionViolationException.class, () -> lookup.find("echo", context));
                    assertEquals(String.format("Could not find method [echo] in class [%s] with parameter combination (java.lang.String, int)",
                            getClass().getName()),
                            exception.getMessage());
                }

                @Test
                @DisplayName("for multple parameter combinations")
                void testMultpleParameterCombinations() {
                    MethodLookup lookup = MethodLookup.withParameterTypes(String.class, int.class)
                            .orParameterTypes(int.class);

                    ExtensionContext context = mock(ExtensionContext.class);
                    doReturn(getClass()).when(context).getRequiredTestClass();

                    PreconditionViolationException exception = assertThrows(PreconditionViolationException.class, () -> lookup.find("echo", context));
                    assertEquals(String.format("Could not find method [echo] in class [%s] with a parameter combination in [%s]",
                            getClass().getName(), "(java.lang.String, int), (int)"),
                            exception.getMessage());
                }
            }

            String echo(String value) {
                return value;
            }

            String repeat(String value, int count) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < count; i++) {
                    sb.append(value);
                }
                return sb.toString();
            }

            String repeat(String value) {
                return repeat(value, 3);
            }
        }

        @Nested
        @DisplayName("with method name and arguments")
        class WithMethodNameAndArguments {

            @Test
            @DisplayName("method found")
            void testMethodFound() {
                MethodLookup lookup = MethodLookup.withParameterTypes(String.class, int.class)
                        .orParameterTypes(String.class);

                ExtensionContext context = mock(ExtensionContext.class);
                doReturn(getClass()).when(context).getRequiredTestClass();

                MethodLookup.Result result = lookup.find("echo(java.lang.String)", context);
                Method expectedMethod = assertDoesNotThrow(() -> getClass().getDeclaredMethod("echo", String.class));
                assertEquals(expectedMethod, result.method());
                assertEquals(1, result.index());

                result = lookup.find("repeat(java.lang.String)", context);
                expectedMethod = assertDoesNotThrow(() -> getClass().getDeclaredMethod("repeat", String.class));
                assertEquals(expectedMethod, result.method());
                assertEquals(1, result.index());

                result = lookup.find("repeat(java.lang.String, int)", context);
                expectedMethod = assertDoesNotThrow(() -> getClass().getDeclaredMethod("repeat", String.class, int.class));
                assertEquals(expectedMethod, result.method());
                assertEquals(0, result.index());
            }

            @Test
            @DisplayName("method not found")
            void testMethodNotFound() {
                MethodLookup lookup = MethodLookup.withParameterTypes(String.class, int.class)
                        .orParameterTypes(int.class);

                ExtensionContext context = mock(ExtensionContext.class);
                doReturn(getClass()).when(context).getRequiredTestClass();

                PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
                        () -> lookup.find("echo(int)", context));
                assertEquals(String.format("Could not find method [echo(int)] in class [%s]", getClass().getName()), exception.getMessage());
            }

            @Nested
            @DisplayName("method mismatch")
            class MethodMismatch {

                @Test
                @DisplayName("for single parameter combinations")
                void testSingleParameterCombinations() {
                    MethodLookup lookup = MethodLookup.withParameterTypes(String.class, int.class);

                    ExtensionContext context = mock(ExtensionContext.class);
                    doReturn(getClass()).when(context).getRequiredTestClass();

                    PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
                            () -> lookup.find("echo(java.lang.String)", context));
                    assertEquals(String.format(
                            "Method [echo(java.lang.String)] in class [%s] does not have parameter combination (java.lang.String, int)",
                            getClass().getName()),
                            exception.getMessage());
                }

                @Test
                @DisplayName("for multple parameter combinations")
                void testMultpleParameterCombinations() {
                    MethodLookup lookup = MethodLookup.withParameterTypes(String.class, int.class)
                            .orParameterTypes(int.class);

                    ExtensionContext context = mock(ExtensionContext.class);
                    doReturn(getClass()).when(context).getRequiredTestClass();

                    PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
                            () -> lookup.find("echo(java.lang.String)", context));
                    assertEquals(String.format(
                            "Method [echo(java.lang.String)] in class [%s] does not have a parameter combination in [(java.lang.String, int), (int)]",
                            getClass().getName()),
                            exception.getMessage());
                }

                String echo(String value) {
                    return value;
                }
            }

            @Test
            @DisplayName("invalid argument type")
            void testInvalidArgumentType() {
                MethodLookup lookup = MethodLookup.withParameterTypes(String.class, int.class)
                        .orParameterTypes(int.class);

                ExtensionContext context = mock(ExtensionContext.class);
                doReturn(getClass()).when(context).getRequiredTestClass();

                JUnitException exception = assertThrows(JUnitException.class, () -> lookup.find("echo(String)", context));
                assertEquals(String.format("Failed to load parameter type [String] for method [echo] in class [%s].", getClass().getName()),
                        exception.getMessage());
            }

            String echo(String value) {
                return value;
            }

            String repeat(String value, int count) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < count; i++) {
                    sb.append(value);
                }
                return sb.toString();
            }

            String repeat(String value) {
                return repeat(value, 3);
            }
        }

        @Nested
        @DisplayName("with class name and method name")
        class WithClassNameAndMethodName {

            private final Class<?> declaringClass = MethodLookupTest.class;
            private final String className = declaringClass.getName();

            @Test
            @DisplayName("method found")
            void testMethodFound() {
                MethodLookup lookup = MethodLookup.withParameterTypes(String.class, int.class)
                        .orParameterTypes(String.class);

                ExtensionContext context = mock(ExtensionContext.class);
                doReturn(getClass()).when(context).getRequiredTestClass();

                MethodLookup.Result result = lookup.find(className + "#echo", context);
                Method expectedMethod = assertDoesNotThrow(() -> declaringClass.getDeclaredMethod("echo", String.class));
                assertEquals(expectedMethod, result.method());
                assertEquals(1, result.index());

                result = lookup.find(className + "#repeat", context);
                expectedMethod = assertDoesNotThrow(() -> declaringClass.getDeclaredMethod("repeat", String.class, int.class));
                assertEquals(expectedMethod, result.method());
                assertEquals(0, result.index());
            }

            @Nested
            @DisplayName("method not found")
            class MethodNotFound {

                @Test
                @DisplayName("for single parameter combination")
                void testSingleParameterCombination() {
                    MethodLookup lookup = MethodLookup.withParameterTypes(String.class, int.class);

                    ExtensionContext context = mock(ExtensionContext.class);
                    doReturn(getClass()).when(context).getRequiredTestClass();

                    PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
                            () -> lookup.find(className + "#echo", context));
                    assertEquals(String.format("Could not find method [echo] in class [%s] with parameter combination (java.lang.String, int)",
                            declaringClass.getName()),
                            exception.getMessage());
                }

                @Test
                @DisplayName("for multple parameter combinations")
                void testMultpleParameterCombinations() {
                    MethodLookup lookup = MethodLookup.withParameterTypes(String.class, int.class)
                            .orParameterTypes(int.class);

                    ExtensionContext context = mock(ExtensionContext.class);
                    doReturn(getClass()).when(context).getRequiredTestClass();

                    PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
                            () -> lookup.find(className + "#echo", context));
                    assertEquals(String.format("Could not find method [echo] in class [%s] with a parameter combination in [%s]",
                            declaringClass.getName(), "(java.lang.String, int), (int)"),
                            exception.getMessage());
                }
            }

            @Test
            @DisplayName("class not found")
            void testClassNotFound() {
                MethodLookup lookup = MethodLookup.withParameterTypes(String.class, int.class)
                        .orParameterTypes(int.class);

                ExtensionContext context = mock(ExtensionContext.class);
                doReturn(getClass()).when(context).getRequiredTestClass();

                JUnitException exception = assertThrows(JUnitException.class, () -> lookup.find(className + "1#echo", context));
                assertEquals(String.format("Could not load class [%s1]", className), exception.getMessage());
            }
        }

        @Nested
        @DisplayName("with class name, method name and arguments")
        class WithClassNameAndMethodNameAndArguments {

            private final Class<?> declaringClass = MethodLookupTest.class;
            private final String className = declaringClass.getName();

            @Test
            @DisplayName("method found")
            void testMethodFound() {
                MethodLookup lookup = MethodLookup.withParameterTypes(String.class, int.class)
                        .orParameterTypes(String.class);

                ExtensionContext context = mock(ExtensionContext.class);
                doReturn(getClass()).when(context).getRequiredTestClass();

                MethodLookup.Result result = lookup.find(className + "#echo(java.lang.String)", context);
                Method expectedMethod = assertDoesNotThrow(() -> declaringClass.getDeclaredMethod("echo", String.class));
                assertEquals(expectedMethod, result.method());
                assertEquals(1, result.index());

                result = lookup.find(className + "#repeat(java.lang.String)", context);
                expectedMethod = assertDoesNotThrow(() -> declaringClass.getDeclaredMethod("repeat", String.class));
                assertEquals(expectedMethod, result.method());
                assertEquals(1, result.index());

                result = lookup.find(className + "#repeat(java.lang.String, int)", context);
                expectedMethod = assertDoesNotThrow(() -> declaringClass.getDeclaredMethod("repeat", String.class, int.class));
                assertEquals(expectedMethod, result.method());
                assertEquals(0, result.index());
            }

            @Test
            @DisplayName("method not found")
            void testMethodNotFound() {
                MethodLookup lookup = MethodLookup.withParameterTypes(String.class, int.class)
                        .orParameterTypes(int.class);

                ExtensionContext context = mock(ExtensionContext.class);
                doReturn(getClass()).when(context).getRequiredTestClass();

                PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
                        () -> lookup.find(className + "#echo(int)", context));
                assertEquals(String.format("Could not find method [echo(int)] in class [%s]", declaringClass.getName()), exception.getMessage());
            }

            @Nested
            @DisplayName("method mismatch")
            class MethodMismatch {

                @Test
                @DisplayName("for single parameter combinations")
                void testSingleParameterCombinations() {
                    MethodLookup lookup = MethodLookup.withParameterTypes(String.class, int.class);

                    ExtensionContext context = mock(ExtensionContext.class);
                    doReturn(getClass()).when(context).getRequiredTestClass();

                    PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
                            () -> lookup.find(className + "#echo(java.lang.String)", context));
                    assertEquals(String.format(
                            "Method [echo(java.lang.String)] in class [%s] does not have parameter combination (java.lang.String, int)",
                            declaringClass.getName()),
                            exception.getMessage());
                }

                @Test
                @DisplayName("for multple parameter combinations")
                void testMultpleParameterCombinations() {
                    MethodLookup lookup = MethodLookup.withParameterTypes(String.class, int.class)
                            .orParameterTypes(int.class);

                    ExtensionContext context = mock(ExtensionContext.class);
                    doReturn(getClass()).when(context).getRequiredTestClass();

                    PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
                            () -> lookup.find(className + "#echo(java.lang.String)", context));
                    assertEquals(String.format(
                            "Method [echo(java.lang.String)] in class [%s] does not have a parameter combination in [(java.lang.String, int), (int)]",
                            declaringClass.getName()),
                            exception.getMessage());
                }
            }

            @Test
            @DisplayName("invalid argument type")
            void testInvalidArgumentType() {
                MethodLookup lookup = MethodLookup.withParameterTypes(String.class, int.class)
                        .orParameterTypes(int.class);

                ExtensionContext context = mock(ExtensionContext.class);
                doReturn(getClass()).when(context).getRequiredTestClass();

                JUnitException exception = assertThrows(JUnitException.class, () -> lookup.find(className + "#echo(String)", context));
                assertEquals(String.format("Failed to load parameter type [String] for method [echo] in class [%s].", declaringClass.getName()),
                        exception.getMessage());
            }

            @Test
            @DisplayName("class not found")
            void testClassNotFound() {
                MethodLookup lookup = MethodLookup.withParameterTypes(String.class, int.class)
                        .orParameterTypes(int.class);

                ExtensionContext context = mock(ExtensionContext.class);
                doReturn(getClass()).when(context).getRequiredTestClass();

                JUnitException exception = assertThrows(JUnitException.class, () -> lookup.find(className + "1#echo(String)", context));
                assertEquals(String.format("Could not load class [%s1]", className), exception.getMessage());
            }

            String echo(String value) {
                return value;
            }

            String repeat(String value, int count) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < count; i++) {
                    sb.append(value);
                }
                return sb.toString();
            }

            String repeat(String value) {
                return repeat(value, 3);
            }
        }
    }

    @Nested
    @DisplayName("findMethod(String, ExtensionContext)")
    class FindMethod {

        @Test
        @DisplayName("null method reference")
        void testNullMethodReference() {
            ExtensionContext context = mock(ExtensionContext.class);

            PreconditionViolationException exception = assertThrows(PreconditionViolationException.class, () -> findMethod(null, context));
            assertEquals("methodReference must not be null or blank", exception.getMessage());
        }

        @Test
        @DisplayName("blank method reference")
        void testBlankMethodReference() {
            ExtensionContext context = mock(ExtensionContext.class);

            PreconditionViolationException exception = assertThrows(PreconditionViolationException.class, () -> findMethod("  ", context));
            assertEquals("methodReference must not be null or blank", exception.getMessage());
        }

        @Test
        @DisplayName("invalid method reference")
        void testInvalidMethodReference() {
            ExtensionContext context = mock(ExtensionContext.class);

            PreconditionViolationException exception = assertThrows(PreconditionViolationException.class, () -> findMethod("1", context));
            assertEquals("[1] is not a valid method reference: "
                    + "it must be the method name, optionally preceded by a fully qualified class name followed by a '#', "
                    + "and optionally followed by a parameter list enclosed in parentheses.",
                    exception.getMessage());
        }

        @Nested
        @DisplayName("with method name")
        class WithMethodName {

            @Test
            @DisplayName("single method found")
            void testSingleMethodFound() {
                ExtensionContext context = mock(ExtensionContext.class);
                doReturn(getClass()).when(context).getRequiredTestClass();

                Method method = findMethod("echo", context);
                Method expectedMethod = assertDoesNotThrow(() -> getClass().getDeclaredMethod("echo", String.class));
                assertEquals(expectedMethod, method);
            }

            @Test
            @DisplayName("multiple methods found")
            void testMultpleMethodsFound() {
                ExtensionContext context = mock(ExtensionContext.class);
                doReturn(getClass()).when(context).getRequiredTestClass();

                PreconditionViolationException exception = assertThrows(PreconditionViolationException.class, () -> findMethod("repeat", context));
                assertEquals(String.format("Found several methods named [repeat] in class [%s]", getClass().getName()), exception.getMessage());
            }

            @Test
            @DisplayName("method not found")
            void testMethodNotFound() {
                ExtensionContext context = mock(ExtensionContext.class);
                doReturn(getClass()).when(context).getRequiredTestClass();

                PreconditionViolationException exception = assertThrows(PreconditionViolationException.class, () -> findMethod("foo", context));
                assertEquals(String.format("Could not find method [foo] in class [%s]", getClass().getName()), exception.getMessage());
            }

            String echo(String value) {
                return value;
            }

            String repeat(String value, int count) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < count; i++) {
                    sb.append(value);
                }
                return sb.toString();
            }

            String repeat(String value) {
                return repeat(value, 3);
            }
        }

        @Nested
        @DisplayName("with method name and arguments")
        class WithMethodNameAndArguments {

            @Test
            @DisplayName("method found")
            void testMethodFound() {
                ExtensionContext context = mock(ExtensionContext.class);
                doReturn(getClass()).when(context).getRequiredTestClass();

                Method method = findMethod("echo(java.lang.String)", context);
                Method expectedMethod = assertDoesNotThrow(() -> getClass().getDeclaredMethod("echo", String.class));
                assertEquals(expectedMethod, method);

                method = findMethod("repeat(java.lang.String)", context);
                expectedMethod = assertDoesNotThrow(() -> getClass().getDeclaredMethod("repeat", String.class));
                assertEquals(expectedMethod, method);

                method = findMethod("repeat(java.lang.String, int)", context);
                expectedMethod = assertDoesNotThrow(() -> getClass().getDeclaredMethod("repeat", String.class, int.class));
                assertEquals(expectedMethod, method);
            }

            @Test
            @DisplayName("method not found")
            void testMethodNotFound() {
                ExtensionContext context = mock(ExtensionContext.class);
                doReturn(getClass()).when(context).getRequiredTestClass();

                PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
                        () -> findMethod("echo(int)", context));
                assertEquals(String.format("Could not find method [echo(int)] in class [%s]", getClass().getName()), exception.getMessage());
            }

            @Test
            @DisplayName("invalid argument type")
            void testInvalidArgumentType() {
                ExtensionContext context = mock(ExtensionContext.class);
                doReturn(getClass()).when(context).getRequiredTestClass();

                JUnitException exception = assertThrows(JUnitException.class, () -> findMethod("echo(String)", context));
                assertEquals(String.format("Failed to load parameter type [String] for method [echo] in class [%s].", getClass().getName()),
                        exception.getMessage());
            }

            String echo(String value) {
                return value;
            }

            String repeat(String value, int count) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < count; i++) {
                    sb.append(value);
                }
                return sb.toString();
            }

            String repeat(String value) {
                return repeat(value, 3);
            }
        }

        @Nested
        @DisplayName("with class name and method name")
        class WithClassNameAndMethodName {

            private final Class<?> declaringClass = MethodLookupTest.class;
            private final String className = declaringClass.getName();

            @Test
            @DisplayName("single method found")
            void testSingleMethodFound() {
                ExtensionContext context = mock(ExtensionContext.class);
                doReturn(getClass()).when(context).getRequiredTestClass();

                Method method = findMethod(className + "#echo", context);
                Method expectedMethod = assertDoesNotThrow(() -> declaringClass.getDeclaredMethod("echo", String.class));
                assertEquals(expectedMethod, method);
            }

            @Test
            @DisplayName("multiple methods found")
            void testMultpleMethodsFound() {
                ExtensionContext context = mock(ExtensionContext.class);
                doReturn(getClass()).when(context).getRequiredTestClass();

                PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
                        () -> findMethod(className + "#repeat", context));
                assertEquals(String.format("Found several methods named [repeat] in class [%s]", declaringClass.getName()), exception.getMessage());
            }

            @Test
            @DisplayName("method not found")
            void testMethodNotFound() {
                ExtensionContext context = mock(ExtensionContext.class);
                doReturn(getClass()).when(context).getRequiredTestClass();

                PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
                        () -> findMethod(className + "#foo", context));
                assertEquals(String.format("Could not find method [foo] in class [%s]", declaringClass.getName()), exception.getMessage());
            }

            @Test
            @DisplayName("class not found")
            void testClassNotFound() {
                ExtensionContext context = mock(ExtensionContext.class);
                doReturn(getClass()).when(context).getRequiredTestClass();

                JUnitException exception = assertThrows(JUnitException.class, () -> findMethod(className + "1#echo", context));
                assertEquals(String.format("Could not load class [%s1]", className), exception.getMessage());
            }
        }

        @Nested
        @DisplayName("with class name, method name and arguments")
        class WithClassNameAndMethodNameAndArguments {

            private final Class<?> declaringClass = MethodLookupTest.class;
            private final String className = declaringClass.getName();

            @Test
            @DisplayName("method found")
            void testMethodFound() {
                ExtensionContext context = mock(ExtensionContext.class);
                doReturn(getClass()).when(context).getRequiredTestClass();

                Method method = findMethod(className + "#echo(java.lang.String)", context);
                Method expectedMethod = assertDoesNotThrow(() -> declaringClass.getDeclaredMethod("echo", String.class));
                assertEquals(expectedMethod, method);

                method = findMethod(className + "#repeat(java.lang.String)", context);
                expectedMethod = assertDoesNotThrow(() -> declaringClass.getDeclaredMethod("repeat", String.class));
                assertEquals(expectedMethod, method);

                method = findMethod(className + "#repeat(java.lang.String, int)", context);
                expectedMethod = assertDoesNotThrow(() -> declaringClass.getDeclaredMethod("repeat", String.class, int.class));
                assertEquals(expectedMethod, method);
            }

            @Test
            @DisplayName("method not found")
            void testMethodNotFound() {
                ExtensionContext context = mock(ExtensionContext.class);
                doReturn(getClass()).when(context).getRequiredTestClass();

                PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
                        () -> findMethod(className + "#echo(int)", context));
                assertEquals(String.format("Could not find method [echo(int)] in class [%s]", declaringClass.getName()), exception.getMessage());
            }

            @Test
            @DisplayName("invalid argument type")
            void testInvalidArgumentType() {
                ExtensionContext context = mock(ExtensionContext.class);
                doReturn(getClass()).when(context).getRequiredTestClass();

                JUnitException exception = assertThrows(JUnitException.class, () -> findMethod(className + "#echo(String)", context));
                assertEquals(String.format("Failed to load parameter type [String] for method [echo] in class [%s].", declaringClass.getName()),
                        exception.getMessage());
            }

            @Test
            @DisplayName("class not found")
            void testClassNotFound() {
                ExtensionContext context = mock(ExtensionContext.class);
                doReturn(getClass()).when(context).getRequiredTestClass();

                JUnitException exception = assertThrows(JUnitException.class, () -> findMethod(className + "1#echo(String)", context));
                assertEquals(String.format("Could not load class [%s1]", className), exception.getMessage());
            }

            String echo(String value) {
                return value;
            }

            String repeat(String value, int count) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < count; i++) {
                    sb.append(value);
                }
                return sb.toString();
            }

            String repeat(String value) {
                return repeat(value, 3);
            }
        }
    }

    static String echo(String value) {
        return value;
    }

    static String repeat(String value, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(value);
        }
        return sb.toString();
    }

    static String repeat(String value) {
        return repeat(value, 3);
    }
}
