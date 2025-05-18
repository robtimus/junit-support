/*
 * InjectionTarget.java
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.support.AnnotationSupport;

/**
 * A representation of an injection target. This can be a field or a parameter.
 * <p>
 * When method {@link #isAnnotated(Class, boolean)}, {@link #findAnnotation(Class, boolean)} or {@link #findRepeatableAnnotations(Class, boolean)} is
 * called with {@code true} for the {@code includeDeclaringElements} argument, the declaring elements are checked if the target itself is not
 * annotated. The declaring elements are the constructor or method (for parameters only) and the class the field, constructor or method is declared
 * in. If the class is a nested class, its declaring class is also checked; this continues until a top-level class is found.
 * <p>
 * Since version 3.1, injection targets implement {@link Object#equals(Object)} and {@link Object#hashCode()} and can therefore be used as keys to
 * {@link Store}. They also implement {@link Object#toString()} to return a unique representation for the injection target. This can be used as basis
 * for keys to {@link Store}.
 *
 * @author Rob Spoor
 * @since 2.0
 */
public abstract class InjectionTarget {

    private InjectionTarget() {
    }

    /**
     * Returns the declaring class. For parameters, this is the declaring class of the constructor or method.
     *
     * @return The declaring class.
     * @see Field#getDeclaringClass()
     * @see Parameter#getDeclaringExecutable()
     * @see Executable#getDeclaringClass()
     */
    public abstract Class<?> declaringClass();

    /**
     * Returns the target type.
     *
     * @return The target type.
     * @see Field#getType()
     * @see Parameter#getType()
     */
    public abstract Class<?> type();

    /**
     * Returns the generic target type.
     *
     * @return The generic target type.
     * @see Field#getGenericType()
     * @see Parameter#getParameterizedType()
     */
    public abstract Type genericType();

    /**
     * Checks whether or not an annotation of a specific type is either <em>present</em> or <em>meta-present</em> on the injection target.
     *
     * @param annotationType The type to check.
     * @return {@code true} if the an annotation of the given type is <em>present</em> or <em>meta-present</em>, or {@code false} otherwise.
     */
    public boolean isAnnotated(Class<? extends Annotation> annotationType) {
        return isAnnotated(annotationType, false);
    }

    /**
     * Checks whether or not an annotation of a specific type is either <em>present</em> or <em>meta-present</em> on the injection target.
     *
     * @param annotationType The type to check.
     * @param includeDeclaringElements If {@code true}, the injection targets declaring elements are checked if the target itself is not annotated.
     * @return {@code true} if the an annotation of the given type is <em>present</em> or <em>meta-present</em>, or {@code false} otherwise.
     */
    public boolean isAnnotated(Class<? extends Annotation> annotationType, boolean includeDeclaringElements) {
        return findAnnotation(annotationType, includeDeclaringElements).isPresent();
    }

    /**
     * Finds the first annotation of a specific type that is either <em>present</em> or <em>meta-present</em> on the injection target.
     *
     * @param <A> The type of annotation.
     * @param annotationType The type to find an annotation for.
     * @return An {@link Optional} describing the first annotation of the given type, or {@link Optional#empty()} if the annotation is not present.
     */
    public <A extends Annotation> Optional<A> findAnnotation(Class<A> annotationType) {
        return findAnnotation(annotationType, false);
    }

    /**
     * Finds the first annotation of a specific type that is either <em>present</em> or <em>meta-present</em> on the injection target.
     *
     * @param <A> The type of annotation.
     * @param annotationType The type to find an annotation for.
     * @param includeDeclaringElements If {@code true}, the injection targets declaring elements are checked if the target itself is not annotated.
     * @return An {@link Optional} describing the first annotation of the given type, or {@link Optional#empty()} if the annotation is not present.
     */
    public abstract <A extends Annotation> Optional<A> findAnnotation(Class<A> annotationType, boolean includeDeclaringElements);

    /**
     * Finds all <em>repeatable</em> annotations of a specific type that are either <em>present</em> or <em>meta-present</em> on the injection target.
     *
     * @param <A> The type of annotation.
     * @param annotationType The type to find annotations for.
     * @return A list with all annotations of the given type; possibly empty but never {@code null}.
     */
    public <A extends Annotation> List<A> findRepeatableAnnotations(Class<A> annotationType) {
        return findRepeatableAnnotations(annotationType, false);
    }

    /**
     * Finds all <em>repeatable</em> annotations of a specific type that are either <em>present</em> or <em>meta-present</em> on the injection target.
     *
     * @param <A> The type of annotation.
     * @param annotationType The type to find annotations for.
     * @param includeDeclaringElements If {@code true}, the injection targets declaring elements are checked if the target itself is not annotated.
     * @return A list with all annotations of the given type; possibly empty but never {@code null}.
     */
    public abstract <A extends Annotation> List<A> findRepeatableAnnotations(Class<A> annotationType, boolean includeDeclaringElements);

    /**
     * Creates a {@link JUnitException} with a message.
     *
     * @param message The message for the exception.
     * @return The created exception.
     */
    public abstract JUnitException createException(String message);

    /**
     * Creates a {@link JUnitException} with a message and a cause.
     *
     * @param message The message for the exception.
     * @param cause The cause of the exception.
     * @return The created exception.
     */
    public abstract JUnitException createException(String message, Throwable cause);

    /**
     * Creates an injection target for a constructor or method parameter.
     *
     * @param parameterContext The parameter context describing the parameter.
     * @return The created injection target.
     * @throws NullPointerException If the given parameter context is {@code null}.
     */
    public static InjectionTarget forParameter(ParameterContext parameterContext) {
        return new ParameterInjectionTarget(parameterContext);
    }

    /**
     * Creates an injection target for a field.
     *
     * @param field The field.
     * @return The created injection target.
     * @throws NullPointerException If the given parameter context is {@code null}.
     */
    public static InjectionTarget forField(Field field) {
        return new FieldInjectionTarget(field);
    }

    private static <A extends Annotation> Optional<A> findAnnotation(Class<?> clazz, Class<A> annotationType) {
        Class<?> iterator = clazz;
        while (iterator != null) {
            Optional<A> annotation = AnnotationSupport.findAnnotation(iterator, annotationType);
            if (annotation.isPresent()) {
                return annotation;
            }
            iterator = iterator.getDeclaringClass();
        }
        return Optional.empty();
    }

    private static <A extends Annotation> List<A> findRepeatableAnnotations(Class<?> clazz, Class<A> annotationType) {
        Class<?> iterator = clazz;
        while (iterator != null) {
            List<A> annotations = AnnotationSupport.findRepeatableAnnotations(iterator, annotationType);
            if (!annotations.isEmpty()) {
                return annotations;
            }
            iterator = iterator.getDeclaringClass();
        }
        return Collections.emptyList();
    }

    private static final class ParameterInjectionTarget extends InjectionTarget {

        private final ParameterContext parameterContext;
        private final Parameter parameter;

        private ParameterInjectionTarget(ParameterContext parameterContext) {
            this.parameterContext = parameterContext;
            this.parameter = parameterContext.getParameter();
        }

        @Override
        public Class<?> declaringClass() {
            return parameter.getDeclaringExecutable().getDeclaringClass();
        }

        @Override
        public Class<?> type() {
            return parameter.getType();
        }

        @Override
        public Type genericType() {
            return parameter.getParameterizedType();
        }

        @Override
        public <A extends Annotation> Optional<A> findAnnotation(Class<A> annotationType, boolean includeDeclaringElements) {
            Optional<A> annotation = parameterContext.findAnnotation(annotationType);
            if (annotation.isPresent() || !includeDeclaringElements) {
                return annotation;
            }
            Executable executable = parameterContext.getDeclaringExecutable();
            annotation = AnnotationSupport.findAnnotation(executable, annotationType);
            if (annotation.isPresent()) {
                return annotation;
            }
            return InjectionTarget.findAnnotation(executable.getDeclaringClass(), annotationType);
        }

        @Override
        public <A extends Annotation> List<A> findRepeatableAnnotations(Class<A> annotationType, boolean includeDeclaringElements) {
            List<A> annotations = parameterContext.findRepeatableAnnotations(annotationType);
            if (!annotations.isEmpty() || !includeDeclaringElements) {
                return annotations;
            }
            Executable executable = parameterContext.getDeclaringExecutable();
            annotations = AnnotationSupport.findRepeatableAnnotations(executable, annotationType);
            if (!annotations.isEmpty()) {
                return annotations;
            }
            return InjectionTarget.findRepeatableAnnotations(executable.getDeclaringClass(), annotationType);
        }

        @Override
        public JUnitException createException(String message) {
            return new ParameterResolutionException(message);
        }

        @Override
        public JUnitException createException(String message, Throwable cause) {
            return new ParameterResolutionException(message, cause);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || o.getClass() != getClass()) {
                return false;
            }
            ParameterInjectionTarget other = (ParameterInjectionTarget) o;
            return parameter.equals(other.parameter);
        }

        @Override
        public int hashCode() {
            return parameter.hashCode();
        }

        @Override
        @SuppressWarnings("nls")
        public String toString() {
            Executable executable = parameter.getDeclaringExecutable();
            Class<?> declaringClass = executable.getDeclaringClass();
            String parameterString = Arrays.stream(executable.getParameterTypes())
                    .map(Class::getSimpleName)
                    .collect(Collectors.joining(", ")); //$NON-NLS-1$
            return executable instanceof Constructor<?>
                    ? String.format("%s(%s)[%s]", declaringClass.getName(), parameterString, parameter.getName())
                    : String.format("%s#%s(%s)[%s]", declaringClass.getName(), executable.getName(), parameterString, parameter.getName());
        }
    }

    private static final class FieldInjectionTarget extends InjectionTarget {

        private final Field field;

        private FieldInjectionTarget(Field field) {
            this.field = field;
        }

        @Override
        public Class<?> declaringClass() {
            return field.getDeclaringClass();
        }

        @Override
        public Class<?> type() {
            return field.getType();
        }

        @Override
        public Type genericType() {
            return field.getGenericType();
        }

        @Override
        public <A extends Annotation> Optional<A> findAnnotation(Class<A> annotationType, boolean includeDeclaringElements) {
            Optional<A> annotation = AnnotationSupport.findAnnotation(field, annotationType);
            if (annotation.isPresent() || !includeDeclaringElements) {
                return annotation;
            }
            return InjectionTarget.findAnnotation(field.getDeclaringClass(), annotationType);
        }

        @Override
        public <A extends Annotation> List<A> findRepeatableAnnotations(Class<A> annotationType, boolean includeDeclaringElements) {
            List<A> annotations = AnnotationSupport.findRepeatableAnnotations(field, annotationType);
            if (!annotations.isEmpty() || !includeDeclaringElements) {
                return annotations;
            }
            return InjectionTarget.findRepeatableAnnotations(field.getDeclaringClass(), annotationType);
        }

        @Override
        public JUnitException createException(String message) {
            return new ExtensionConfigurationException(message);
        }

        @Override
        public JUnitException createException(String message, Throwable cause) {
            return new ExtensionConfigurationException(message, cause);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || o.getClass() != getClass()) {
                return false;
            }
            FieldInjectionTarget other = (FieldInjectionTarget) o;
            return field.equals(other.field);
        }

        @Override
        public int hashCode() {
            return field.hashCode();
        }

        @Override
        @SuppressWarnings("nls")
        public String toString() {
            return field.getDeclaringClass().getName() + "#" + field.getName();
        }
    }
}
