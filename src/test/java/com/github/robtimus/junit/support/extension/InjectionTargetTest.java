/*
 * InjectionTargetTest.java
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

import static com.github.robtimus.junit.support.OptionalAssertions.assertIsEmpty;
import static com.github.robtimus.junit.support.OptionalAssertions.assertIsPresent;
import static com.github.robtimus.junit.support.extension.InjectionTarget.forField;
import static com.github.robtimus.junit.support.extension.InjectionTarget.forParameter;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.ToIntFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.support.AnnotationSupport;
import com.github.robtimus.junit.support.extension.InjectionTargetTest.Annotation0;
import com.github.robtimus.junit.support.extension.InjectionTargetTest.RepeatableAnnotation0;

@Annotation0
@RepeatableAnnotation0(0)
@RepeatableAnnotation0(1)
@RepeatableAnnotation0(2)
@SuppressWarnings({ "nls", "unused", "exports" })
class InjectionTargetTest {

    @Nested
    @DisplayName("forParameter(ParameterContext)")
    @TestInstance(Lifecycle.PER_CLASS)
    class ForParameter {

        private Parameter parameter = assertDoesNotThrow(() -> Level1.Level2.class.getDeclaredMethod("method", List.class)).getParameters()[0];
        private InjectionTarget target = forParameter(mockParameterContext(parameter));

        @Test
        @DisplayName("declaringClass()")
        void testDeclaringClass() {
            assertEquals(Level1.Level2.class, target.declaringClass());
        }

        @Test
        @DisplayName("type()")
        void testType() {
            assertEquals(List.class, target.type());
        }

        @Test
        @DisplayName("genericType()")
        void testGenericType() {
            Type genericType = target.genericType();
            ParameterizedType parameterizedType = assertInstanceOf(ParameterizedType.class, genericType);
            assertEquals(List.class, parameterizedType.getRawType());
            assertArrayEquals(new Class<?>[] { String.class }, parameterizedType.getActualTypeArguments());
        }

        @Test
        @DisplayName("isAnnotated(Class)")
        void testIsAnnotated() {
            assertFalse(target.isAnnotated(Annotation0.class));
            assertFalse(target.isAnnotated(Annotation1.class));
            assertFalse(target.isAnnotated(Annotation2.class));
            assertFalse(target.isAnnotated(Annotation3.class));

            assertTrue(target.isAnnotated(Annotation4.class));
        }

        @ParameterizedTest(name = "includeDeclaringElements: {0}")
        @ValueSource(booleans = { true, false })
        @DisplayName("isAnnotated(Class, boolean)")
        void testIsAnnotated(boolean includeDeclaringElements) {
            assertEquals(includeDeclaringElements, target.isAnnotated(Annotation0.class, includeDeclaringElements));
            assertEquals(includeDeclaringElements, target.isAnnotated(Annotation1.class, includeDeclaringElements));
            assertEquals(includeDeclaringElements, target.isAnnotated(Annotation2.class, includeDeclaringElements));
            assertEquals(includeDeclaringElements, target.isAnnotated(Annotation3.class, includeDeclaringElements));

            assertTrue(target.isAnnotated(Annotation4.class, includeDeclaringElements));

            assertFalse(target.isAnnotated(ExtendWith.class, includeDeclaringElements));
        }

        @Test
        @DisplayName("findAnnotation(Class)")
        void testFindAnnotation() {
            assertIsEmpty(target.findAnnotation(Annotation0.class));
            assertIsEmpty(target.findAnnotation(Annotation1.class));
            assertIsEmpty(target.findAnnotation(Annotation2.class));
            assertIsEmpty(target.findAnnotation(Annotation3.class));

            assertIsPresent(target.findAnnotation(Annotation4.class));

            assertIsEmpty(target.findAnnotation(ExtendWith.class));
        }

        @ParameterizedTest(name = "includeDeclaringElements: {0}")
        @ValueSource(booleans = { true, false })
        @DisplayName("findAnnotation(Class, boolean)")
        void testFindAnnotationAnnotated(boolean includeDeclaringElements) {
            if (includeDeclaringElements) {
                assertIsPresent(target.findAnnotation(Annotation0.class, includeDeclaringElements));
                assertIsPresent(target.findAnnotation(Annotation1.class, includeDeclaringElements));
                assertIsPresent(target.findAnnotation(Annotation2.class, includeDeclaringElements));
                assertIsPresent(target.findAnnotation(Annotation3.class, includeDeclaringElements));
            } else {
                assertIsEmpty(target.findAnnotation(Annotation0.class, includeDeclaringElements));
                assertIsEmpty(target.findAnnotation(Annotation1.class, includeDeclaringElements));
                assertIsEmpty(target.findAnnotation(Annotation2.class, includeDeclaringElements));
                assertIsEmpty(target.findAnnotation(Annotation3.class, includeDeclaringElements));
            }
            assertIsPresent(target.findAnnotation(Annotation4.class, includeDeclaringElements));

            assertIsEmpty(target.findAnnotation(ExtendWith.class, includeDeclaringElements));
        }

        @Test
        @DisplayName("findRepeatableAnnotations(Class)")
        void testFindRepeatableAnnotations() {
            assertEquals(Collections.emptyList(), target.findRepeatableAnnotations(RepeatableAnnotation0.class));
            assertEquals(Collections.emptyList(), target.findRepeatableAnnotations(RepeatableAnnotation1.class));
            assertEquals(Collections.emptyList(), target.findRepeatableAnnotations(RepeatableAnnotation2.class));
            assertEquals(Collections.emptyList(), target.findRepeatableAnnotations(RepeatableAnnotation3.class));

            assertHasValues(target.findRepeatableAnnotations(RepeatableAnnotation4.class), RepeatableAnnotation4::value, 0, 1, 2);

            assertEquals(Collections.emptyList(), target.findRepeatableAnnotations(ExtendWith.class));
        }

        @ParameterizedTest(name = "includeDeclaringElements: {0}")
        @ValueSource(booleans = { true, false })
        @DisplayName("findRepeatableAnnotations(Class, boolean)")
        void testFindRepeatableAnnotationsAnnotated(boolean includeDeclaringElements) {
            if (includeDeclaringElements) {
                assertHasValues(target.findRepeatableAnnotations(RepeatableAnnotation0.class, includeDeclaringElements), RepeatableAnnotation0::value,
                        0, 1, 2);
                assertHasValues(target.findRepeatableAnnotations(RepeatableAnnotation1.class, includeDeclaringElements), RepeatableAnnotation1::value,
                        0, 1, 2);
                assertHasValues(target.findRepeatableAnnotations(RepeatableAnnotation2.class, includeDeclaringElements), RepeatableAnnotation2::value,
                        0, 1, 2);
                assertHasValues(target.findRepeatableAnnotations(RepeatableAnnotation3.class, includeDeclaringElements), RepeatableAnnotation3::value,
                        0, 1, 2);
            } else {
                assertEquals(Collections.emptyList(), target.findRepeatableAnnotations(RepeatableAnnotation0.class, includeDeclaringElements));
                assertEquals(Collections.emptyList(), target.findRepeatableAnnotations(RepeatableAnnotation1.class, includeDeclaringElements));
                assertEquals(Collections.emptyList(), target.findRepeatableAnnotations(RepeatableAnnotation2.class, includeDeclaringElements));
                assertEquals(Collections.emptyList(), target.findRepeatableAnnotations(RepeatableAnnotation3.class, includeDeclaringElements));
            }
            assertHasValues(target.findRepeatableAnnotations(RepeatableAnnotation4.class, includeDeclaringElements), RepeatableAnnotation4::value,
                    0, 1, 2);

            assertEquals(Collections.emptyList(), target.findRepeatableAnnotations(ExtendWith.class, includeDeclaringElements));
        }

        @Test
        @DisplayName("createException(String)")
        void testCreateException() {
            JUnitException exception = target.createException("foo");

            assertInstanceOf(ParameterResolutionException.class, exception);
            assertEquals("foo", exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("createException(String, Throwable)")
        void testCreateExceptionWithCause() {
            NullPointerException cause = new NullPointerException();

            JUnitException exception = target.createException("foo", cause);

            assertInstanceOf(ParameterResolutionException.class, exception);
            assertEquals("foo", exception.getMessage());
            assertSame(cause, exception.getCause());
        }

        @ParameterizedTest
        @DisplayName("equals({0})")
        @MethodSource("equalsArguments")
        void testEquals(Object o, boolean expected) {
            assertEquals(expected, target.equals(o));
            if (o != null) {
                assertEquals(expected, o.equals(target));
            }
        }

        Arguments[] equalsArguments() {
            Parameter equalParameter = assertDoesNotThrow(() -> Level1.Level2.class.getDeclaredMethod("method", List.class)).getParameters()[0];
            Parameter unEqualParameter = assertDoesNotThrow(() -> getClass().getDeclaredMethod("testEquals", Object.class, boolean.class))
                    .getParameters()[0];
            Field field = assertDoesNotThrow(() -> Level1.Level2.class.getDeclaredField("field"));

            return new Arguments[] {
                    arguments(target, true),
                    arguments(InjectionTarget.forParameter(mockParameterContext(equalParameter)), true),
                    arguments(InjectionTarget.forParameter(mockParameterContext(unEqualParameter)), false),
                    arguments(InjectionTarget.forField(field), false),
                    arguments(null, false),
            };
        }

        @Test
        @DisplayName("hashCode()")
        void testHashCode() {
            assertEquals(parameter.hashCode(), target.hashCode());
        }

        @Nested
        @DisplayName("toString()")
        class ToString {

            @Test
            @DisplayName("for method()")
            void testForMethod() {
                assertEquals(String.format("%s#method(List)[%s]", Level1.Level2.class.getName(), parameter.getName()), target.toString());
            }

            @Test
            @DisplayName("for constructor()")
            void testForConstructor() {
                Parameter constructorParameteer = assertDoesNotThrow(() -> Level1.Level2.class.getDeclaredConstructor(Object.class))
                        .getParameters()[0];

                assertEquals(String.format("%s(Object)[%s]", Level1.Level2.class.getName(), constructorParameteer.getName()),
                        InjectionTarget.forParameter(mockParameterContext(constructorParameteer)).toString());
            }
        }
    }

    @Nested
    @DisplayName("forField(Field")
    @TestInstance(Lifecycle.PER_CLASS)
    class ForField {

        private Field field = assertDoesNotThrow(() -> Level1.Level2.class.getDeclaredField("field"));
        private InjectionTarget target = forField(field);

        @Test
        @DisplayName("declaringClass()")
        void testDeclaringClass() {
            assertEquals(Level1.Level2.class, target.declaringClass());
        }

        @Test
        @DisplayName("type()")
        void testType() {
            assertEquals(Set.class, target.type());
        }

        @Test
        @DisplayName("genericType()")
        void testGenericType() {
            Type genericType = target.genericType();
            ParameterizedType parameterizedType = assertInstanceOf(ParameterizedType.class, genericType);
            assertEquals(Set.class, parameterizedType.getRawType());
            assertArrayEquals(new Class<?>[] { Integer.class }, parameterizedType.getActualTypeArguments());
        }

        @Test
        @DisplayName("isAnnotated(Class)")
        void testIsAnnotated() {
            assertFalse(target.isAnnotated(Annotation0.class));
            assertFalse(target.isAnnotated(Annotation1.class));
            assertFalse(target.isAnnotated(Annotation2.class));

            assertTrue(target.isAnnotated(Annotation3.class));

            assertFalse(target.isAnnotated(Annotation4.class));
        }

        @ParameterizedTest(name = "includeDeclaringElements: {0}")
        @ValueSource(booleans = { true, false })
        @DisplayName("isAnnotated(Class, boolean)")
        void testIsAnnotated(boolean includeDeclaringElements) {
            assertEquals(includeDeclaringElements, target.isAnnotated(Annotation0.class, includeDeclaringElements));
            assertEquals(includeDeclaringElements, target.isAnnotated(Annotation1.class, includeDeclaringElements));
            assertEquals(includeDeclaringElements, target.isAnnotated(Annotation2.class, includeDeclaringElements));

            assertTrue(target.isAnnotated(Annotation3.class, includeDeclaringElements));

            assertFalse(target.isAnnotated(Annotation4.class, includeDeclaringElements));

            assertFalse(target.isAnnotated(ExtendWith.class, includeDeclaringElements));
        }

        @Test
        @DisplayName("findAnnotation(Class)")
        void testFindAnnotation() {
            assertIsEmpty(target.findAnnotation(Annotation0.class));
            assertIsEmpty(target.findAnnotation(Annotation1.class));
            assertIsEmpty(target.findAnnotation(Annotation2.class));

            assertIsPresent(target.findAnnotation(Annotation3.class));

            assertIsEmpty(target.findAnnotation(Annotation4.class));

            assertIsEmpty(target.findAnnotation(ExtendWith.class));
        }

        @ParameterizedTest(name = "includeDeclaringElements: {0}")
        @ValueSource(booleans = { true, false })
        @DisplayName("findAnnotation(Class, boolean)")
        void testFindAnnotationAnnotated(boolean includeDeclaringElements) {
            if (includeDeclaringElements) {
                assertIsPresent(target.findAnnotation(Annotation0.class, includeDeclaringElements));
                assertIsPresent(target.findAnnotation(Annotation1.class, includeDeclaringElements));
                assertIsPresent(target.findAnnotation(Annotation2.class, includeDeclaringElements));
            } else {
                assertIsEmpty(target.findAnnotation(Annotation0.class, includeDeclaringElements));
                assertIsEmpty(target.findAnnotation(Annotation1.class, includeDeclaringElements));
                assertIsEmpty(target.findAnnotation(Annotation2.class, includeDeclaringElements));
            }
            assertIsPresent(target.findAnnotation(Annotation3.class, includeDeclaringElements));

            assertIsEmpty(target.findAnnotation(Annotation4.class, includeDeclaringElements));

            assertIsEmpty(target.findAnnotation(ExtendWith.class, includeDeclaringElements));
        }

        @Test
        @DisplayName("findRepeatableAnnotations(Class)")
        void testFindRepeatableAnnotations() {
            assertEquals(Collections.emptyList(), target.findRepeatableAnnotations(RepeatableAnnotation0.class));
            assertEquals(Collections.emptyList(), target.findRepeatableAnnotations(RepeatableAnnotation1.class));
            assertEquals(Collections.emptyList(), target.findRepeatableAnnotations(RepeatableAnnotation2.class));

            assertHasValues(target.findRepeatableAnnotations(RepeatableAnnotation3.class), RepeatableAnnotation3::value, 0, 1, 2);

            assertEquals(Collections.emptyList(), target.findRepeatableAnnotations(RepeatableAnnotation4.class));

            assertEquals(Collections.emptyList(), target.findRepeatableAnnotations(ExtendWith.class));
        }

        @ParameterizedTest(name = "includeDeclaringElements: {0}")
        @ValueSource(booleans = { true, false })
        @DisplayName("findRepeatableAnnotations(Class, boolean)")
        void testFindRepeatableAnnotationsAnnotated(boolean includeDeclaringElements) {
            if (includeDeclaringElements) {
                assertHasValues(target.findRepeatableAnnotations(RepeatableAnnotation0.class, includeDeclaringElements), RepeatableAnnotation0::value,
                        0, 1, 2);
                assertHasValues(target.findRepeatableAnnotations(RepeatableAnnotation1.class, includeDeclaringElements), RepeatableAnnotation1::value,
                        0, 1, 2);
                assertHasValues(target.findRepeatableAnnotations(RepeatableAnnotation2.class, includeDeclaringElements), RepeatableAnnotation2::value,
                        0, 1, 2);
            } else {
                assertEquals(Collections.emptyList(), target.findRepeatableAnnotations(RepeatableAnnotation0.class, includeDeclaringElements));
                assertEquals(Collections.emptyList(), target.findRepeatableAnnotations(RepeatableAnnotation1.class, includeDeclaringElements));
                assertEquals(Collections.emptyList(), target.findRepeatableAnnotations(RepeatableAnnotation2.class, includeDeclaringElements));
            }
            assertHasValues(target.findRepeatableAnnotations(RepeatableAnnotation3.class, includeDeclaringElements), RepeatableAnnotation3::value,
                    0, 1, 2);

            assertEquals(Collections.emptyList(), target.findRepeatableAnnotations(RepeatableAnnotation4.class, includeDeclaringElements));

            assertEquals(Collections.emptyList(), target.findRepeatableAnnotations(ExtendWith.class, includeDeclaringElements));
        }

        @Test
        @DisplayName("createException(String)")
        void testCreateException() {
            JUnitException exception = target.createException("foo");

            assertInstanceOf(ExtensionConfigurationException.class, exception);
            assertEquals("foo", exception.getMessage());
            assertNull(exception.getCause());
        }

        @Test
        @DisplayName("createException(String, Throwable)")
        void testCreateExceptionWithCause() {
            NullPointerException cause = new NullPointerException();

            JUnitException exception = target.createException("foo", cause);

            assertInstanceOf(ExtensionConfigurationException.class, exception);
            assertEquals("foo", exception.getMessage());
            assertSame(cause, exception.getCause());
        }

        @ParameterizedTest
        @DisplayName("equals({0})")
        @MethodSource("equalsArguments")
        void testEquals(Object o, boolean expected) {
            assertEquals(expected, target.equals(o));
            if (o != null) {
                assertEquals(expected, o.equals(target));
            }
        }

        Arguments[] equalsArguments() {
            Field equalField = assertDoesNotThrow(() -> Level1.Level2.class.getDeclaredField("field"));
            Field unEqualField = assertDoesNotThrow(() -> getClass().getDeclaredField("target"));
            Parameter parameter = assertDoesNotThrow(() -> Level1.Level2.class.getDeclaredMethod("method", List.class)).getParameters()[0];

            return new Arguments[] {
                    arguments(target, true),
                    arguments(InjectionTarget.forField(equalField), true),
                    arguments(InjectionTarget.forField(unEqualField), false),
                    arguments(InjectionTarget.forParameter(mockParameterContext(parameter)), false),
                    arguments(null, false),
            };
        }

        @Test
        @DisplayName("hashCode()")
        void testHashCode() {
            assertEquals(field.hashCode(), target.hashCode());
        }

        @Test
        @DisplayName("toString()")
        void testToString() {
            assertEquals(String.format("%s#field", Level1.Level2.class.getName()), target.toString());
        }
    }

    private static ParameterContext mockParameterContext(Parameter parameter) {
        ParameterContext context = mock(ParameterContext.class);
        when(context.getParameter()).thenReturn(parameter);
        when(context.getDeclaringExecutable()).thenReturn(parameter.getDeclaringExecutable());
        when(context.findAnnotation(any())).thenAnswer(i -> AnnotationSupport.findAnnotation(parameter, i.getArgument(0)));
        when(context.findRepeatableAnnotations(any())).thenAnswer(i -> AnnotationSupport.findRepeatableAnnotations(parameter, i.getArgument(0)));
        return context;
    }

    private static <A extends Annotation> void assertHasValues(List<A> annotations, ToIntFunction<A> value, int... expected) {
        int[] values = annotations.stream()
                .mapToInt(value)
                .toArray();
        assertArrayEquals(expected, values);
    }

    @Annotation1
    @RepeatableAnnotation1(0)
    @RepeatableAnnotation1(1)
    @RepeatableAnnotation1(2)
    private static final class Level1 {

        @Annotation2
        @RepeatableAnnotation2(0)
        @RepeatableAnnotation2(1)
        @RepeatableAnnotation2(2)
        private static final class Level2 {

            @Annotation3
            @RepeatableAnnotation3(0)
            @RepeatableAnnotation3(1)
            @RepeatableAnnotation3(2)
            private Set<Integer> field;

            private Level2(Object ignored) { // NOSONAR
                // does nothing
            }

            @Annotation3
            @RepeatableAnnotation3(0)
            @RepeatableAnnotation3(1)
            @RepeatableAnnotation3(2)
            private void method(
                    @Annotation4
                    @RepeatableAnnotation4(0)
                    @RepeatableAnnotation4(1)
                    @RepeatableAnnotation4(2)
                    List<String> argument) {

                // no body
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Annotation0 {
        // no content
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Annotation1 {
        // no content
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Annotation2 {
        // no content
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Annotation3 {
        // no content
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Annotation4 {
        // no content
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(RepeatableAnnotation0.List.class)
    public @interface RepeatableAnnotation0 {

        int value();

        @Retention(RetentionPolicy.RUNTIME)
        @interface List {

            RepeatableAnnotation0[] value();
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(RepeatableAnnotation1.List.class)
    public @interface RepeatableAnnotation1 {

        int value();

        @Retention(RetentionPolicy.RUNTIME)
        @interface List {

            RepeatableAnnotation1[] value();
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(RepeatableAnnotation2.List.class)
    public @interface RepeatableAnnotation2 {

        int value();

        @Retention(RetentionPolicy.RUNTIME)
        @interface List {

            RepeatableAnnotation2[] value();
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(RepeatableAnnotation3.List.class)
    public @interface RepeatableAnnotation3 {

        int value();

        @Retention(RetentionPolicy.RUNTIME)
        @interface List {

            RepeatableAnnotation3[] value();
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(RepeatableAnnotation4.List.class)
    public @interface RepeatableAnnotation4 {

        int value();

        @Retention(RetentionPolicy.RUNTIME)
        @interface List {

            RepeatableAnnotation4[] value();
        }
    }
}
