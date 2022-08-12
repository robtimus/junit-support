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

import static com.github.robtimus.junit.support.AdditionalAssertions.assertIsPresent;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.ToIntFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.support.AnnotationSupport;
import com.github.robtimus.junit.support.extension.InjectionTargetTest.Annotation0;
import com.github.robtimus.junit.support.extension.InjectionTargetTest.RepeatableAnnotation0;

@Annotation0
@RepeatableAnnotation0(0)
@RepeatableAnnotation0(1)
@RepeatableAnnotation0(2)
@SuppressWarnings({ "nls", "unused" })
class InjectionTargetTest {

    @Nested
    @DisplayName("forParameter(ParameterContext)")
    class ForParameter {

        private InjectionTarget target;

        @BeforeEach
        void initTarget() {
            Method method = assertDoesNotThrow(() -> Level1.Level2.class.getDeclaredMethod("method", List.class));
            Parameter parameter = method.getParameters()[0];

            ParameterContext context = mock(ParameterContext.class);
            when(context.getParameter()).thenReturn(parameter);
            when(context.getDeclaringExecutable()).thenReturn(method);
            when(context.findAnnotation(any())).thenAnswer(i -> AnnotationSupport.findAnnotation(parameter, i.getArgument(0)));
            when(context.findRepeatableAnnotations(any())).thenAnswer(i -> AnnotationSupport.findRepeatableAnnotations(parameter, i.getArgument(0)));

            target = forParameter(context);
        }

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
            assertEquals(Optional.empty(), target.findAnnotation(Annotation0.class));
            assertEquals(Optional.empty(), target.findAnnotation(Annotation1.class));
            assertEquals(Optional.empty(), target.findAnnotation(Annotation2.class));
            assertEquals(Optional.empty(), target.findAnnotation(Annotation3.class));

            assertIsPresent(target.findAnnotation(Annotation4.class));

            assertEquals(Optional.empty(), target.findAnnotation(ExtendWith.class));
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
                assertEquals(Optional.empty(), target.findAnnotation(Annotation0.class, includeDeclaringElements));
                assertEquals(Optional.empty(), target.findAnnotation(Annotation1.class, includeDeclaringElements));
                assertEquals(Optional.empty(), target.findAnnotation(Annotation2.class, includeDeclaringElements));
                assertEquals(Optional.empty(), target.findAnnotation(Annotation3.class, includeDeclaringElements));
            }
            assertIsPresent(target.findAnnotation(Annotation4.class, includeDeclaringElements));

            assertEquals(Optional.empty(), target.findAnnotation(ExtendWith.class, includeDeclaringElements));
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
    }

    @Nested
    @DisplayName("forField(Field")
    class ForField {

        private InjectionTarget target;

        @BeforeEach
        void initTarget() {
            Field field = assertDoesNotThrow(() -> Level1.Level2.class.getDeclaredField("field"));

            target = forField(field);
        }

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
            assertEquals(Optional.empty(), target.findAnnotation(Annotation0.class));
            assertEquals(Optional.empty(), target.findAnnotation(Annotation1.class));
            assertEquals(Optional.empty(), target.findAnnotation(Annotation2.class));

            assertIsPresent(target.findAnnotation(Annotation3.class));

            assertEquals(Optional.empty(), target.findAnnotation(Annotation4.class));

            assertEquals(Optional.empty(), target.findAnnotation(ExtendWith.class));
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
                assertEquals(Optional.empty(), target.findAnnotation(Annotation0.class, includeDeclaringElements));
                assertEquals(Optional.empty(), target.findAnnotation(Annotation1.class, includeDeclaringElements));
                assertEquals(Optional.empty(), target.findAnnotation(Annotation2.class, includeDeclaringElements));
            }
            assertIsPresent(target.findAnnotation(Annotation3.class, includeDeclaringElements));

            assertEquals(Optional.empty(), target.findAnnotation(Annotation4.class, includeDeclaringElements));

            assertEquals(Optional.empty(), target.findAnnotation(ExtendWith.class, includeDeclaringElements));
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
        public @interface List {

            RepeatableAnnotation0[] value();
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(RepeatableAnnotation1.List.class)
    public @interface RepeatableAnnotation1 {

        int value();

        @Retention(RetentionPolicy.RUNTIME)
        public @interface List {

            RepeatableAnnotation1[] value();
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(RepeatableAnnotation2.List.class)
    public @interface RepeatableAnnotation2 {

        int value();

        @Retention(RetentionPolicy.RUNTIME)
        public @interface List {

            RepeatableAnnotation2[] value();
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(RepeatableAnnotation3.List.class)
    public @interface RepeatableAnnotation3 {

        int value();

        @Retention(RetentionPolicy.RUNTIME)
        public @interface List {

            RepeatableAnnotation3[] value();
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(RepeatableAnnotation4.List.class)
    public @interface RepeatableAnnotation4 {

        int value();

        @Retention(RetentionPolicy.RUNTIME)
        public @interface List {

            RepeatableAnnotation4[] value();
        }
    }
}
