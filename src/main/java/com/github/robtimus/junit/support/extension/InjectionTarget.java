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
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
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
 *
 * @author Rob Spoor
 * @since 2.0
 */
public final class InjectionTarget {

    private final Class<?> declaringClass;
    private final Class<?> type;
    private final Type genericType;

    private final BiFunction<Class<? extends Annotation>, Boolean, Optional<? extends Annotation>> annotationFinder;
    private final BiFunction<Class<? extends Annotation>, Boolean, List<? extends Annotation>> repeatableAnnotationFinder;

    private final Function<String, JUnitException> exceptionWithMessage;
    private final BiFunction<String, Throwable, JUnitException> exceptionWithMessageAndCause;

    private InjectionTarget(Class<?> declaringClass, Class<?> type, Type genericType,
            BiFunction<Class<? extends Annotation>, Boolean, Optional<? extends Annotation>> annotationFinder,
            BiFunction<Class<? extends Annotation>, Boolean, List<? extends Annotation>> repeatableAnnotationFinder,
            Function<String, JUnitException> exceptionWithMessage,
            BiFunction<String, Throwable, JUnitException> exceptionWithMessageAndCause) {

        this.declaringClass = declaringClass;
        this.type = type;
        this.genericType = genericType;
        this.annotationFinder = annotationFinder;
        this.repeatableAnnotationFinder = repeatableAnnotationFinder;
        this.exceptionWithMessage = exceptionWithMessage;
        this.exceptionWithMessageAndCause = exceptionWithMessageAndCause;
    }

    /**
     * Returns the declaring class. For parameters, this is the declaring class of the constructor or method.
     *
     * @return The declaring class.
     * @see Field#getDeclaringClass()
     * @see Parameter#getDeclaringExecutable()
     * @see Executable#getDeclaringClass()
     */
    public Class<?> declaringClass() {
        return declaringClass;
    }

    /**
     * Returns the target type.
     *
     * @return The target type.
     * @see Field#getType()
     * @see Parameter#getType()
     */
    public Class<?> type() {
        return type;
    }

    /**
     * Returns the generic target type.
     *
     * @return The generic target type.
     * @see Field#getGenericType()
     * @see Parameter#getParameterizedType()
     */
    public Type genericType() {
        return genericType;
    }

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
    @SuppressWarnings("unchecked")
    public <A extends Annotation> Optional<A> findAnnotation(Class<A> annotationType, boolean includeDeclaringElements) {
        return (Optional<A>) annotationFinder.apply(annotationType, includeDeclaringElements);
    }

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
    @SuppressWarnings("unchecked")
    public <A extends Annotation> List<A> findRepeatableAnnotations(Class<A> annotationType, boolean includeDeclaringElements) {
        return (List<A>) repeatableAnnotationFinder.apply(annotationType, includeDeclaringElements);
    }

    /**
     * Creates a {@link JUnitException} with a message.
     *
     * @param message The message for the exception.
     * @return The created exception.
     */
    public JUnitException createException(String message) {
        return exceptionWithMessage.apply(message);
    }

    /**
     * Creates a {@link JUnitException} with a message and a cause.
     *
     * @param message The message for the exception.
     * @param cause The cause of the exception.
     * @return The created exception.
     */
    public JUnitException createException(String message, Throwable cause) {
        return exceptionWithMessageAndCause.apply(message, cause);
    }

    /**
     * Creates an injection target for a constructor or method parameter.
     *
     * @param parameterContext The parameter context describing the parameter.
     * @return The created injection target.
     * @throws NullPointerException If the given parameter context is {@code null}.
     */
    public static InjectionTarget forParameter(ParameterContext parameterContext) {
        Parameter parameter = parameterContext.getParameter();
        Class<?> declaringClass = parameterContext.getDeclaringExecutable().getDeclaringClass();
        return new InjectionTarget(declaringClass, parameter.getType(), parameter.getParameterizedType(),
                (annotationType, includeDeclaringElements) -> findAnnotation(parameterContext, annotationType, includeDeclaringElements),
                (annotationType, includeDeclaringElements) -> findRepeatableAnnotations(parameterContext, annotationType, includeDeclaringElements),
                ParameterResolutionException::new,
                ParameterResolutionException::new);
    }

    /**
     * Creates an injection target for a field.
     *
     * @param field The field.
     * @return The created injection target.
     * @throws NullPointerException If the given parameter context is {@code null}.
     */
    public static InjectionTarget forField(Field field) {
        return new InjectionTarget(field.getDeclaringClass(), field.getType(), field.getGenericType(),
                (annotationType, includeDeclaringElements) -> findAnnotation(field, annotationType, includeDeclaringElements),
                (annotationType, includeDeclaringElements) -> findRepeatableAnnotations(field, annotationType, includeDeclaringElements),
                ExtensionConfigurationException::new,
                ExtensionConfigurationException::new);
    }

    private static <A extends Annotation> Optional<A> findAnnotation(ParameterContext parameterContext, Class<A> annotationType,
            boolean includeDeclaringElements) {

        Optional<A> annotation = parameterContext.findAnnotation(annotationType);
        if (annotation.isPresent() || !includeDeclaringElements) {
            return annotation;
        }
        Executable executable = parameterContext.getDeclaringExecutable();
        annotation = AnnotationSupport.findAnnotation(executable, annotationType);
        if (annotation.isPresent()) {
            return annotation;
        }
        return findAnnotation(executable.getDeclaringClass(), annotationType);
    }

    private static <A extends Annotation> Optional<A> findAnnotation(Field field, Class<A> annotationType, boolean includeDeclaringElements) {
        Optional<A> annotation = AnnotationSupport.findAnnotation(field, annotationType);
        if (annotation.isPresent() || !includeDeclaringElements) {
            return annotation;
        }
        return findAnnotation(field.getDeclaringClass(), annotationType);
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

    private static <A extends Annotation> List<A> findRepeatableAnnotations(ParameterContext parameterContext, Class<A> annotationType,
            boolean includeDeclaringElements) {

        List<A> annotations = parameterContext.findRepeatableAnnotations(annotationType);
        if (!annotations.isEmpty() || !includeDeclaringElements) {
            return annotations;
        }
        Executable executable = parameterContext.getDeclaringExecutable();
        annotations = AnnotationSupport.findRepeatableAnnotations(executable, annotationType);
        if (!annotations.isEmpty()) {
            return annotations;
        }
        return findRepeatableAnnotations(executable.getDeclaringClass(), annotationType);
    }

    private static <A extends Annotation> List<A> findRepeatableAnnotations(Field field, Class<A> annotationType, boolean includeDeclaringElements) {
        List<A> annotations = AnnotationSupport.findRepeatableAnnotations(field, annotationType);
        if (!annotations.isEmpty() || !includeDeclaringElements) {
            return annotations;
        }
        return findRepeatableAnnotations(field.getDeclaringClass(), annotationType);
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
}
