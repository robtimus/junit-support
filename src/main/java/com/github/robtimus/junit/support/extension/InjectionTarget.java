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
 *
 * @author Rob Spoor
 * @since 2.0
 */
public final class InjectionTarget {

    private final Class<?> declaringClass;
    private final Class<?> type;
    private final Type genericType;

    private final Function<Class<? extends Annotation>, Optional<? extends Annotation>> annotationFinder;
    private final Function<Class<? extends Annotation>, List<? extends Annotation>> repeatableAnnotationFinder;

    private final Function<String, JUnitException> exceptionWithMessage;
    private final BiFunction<String, Throwable, JUnitException> exceptionWithMessageAndCause;

    private InjectionTarget(Class<?> declaringClass, Class<?> type, Type genericType,
            Function<Class<? extends Annotation>, Optional<? extends Annotation>> annotationFinder,
            Function<Class<? extends Annotation>, List<? extends Annotation>> repeatableAnnotationFinder,
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
        return annotationFinder.apply(annotationType).isPresent();
    }

    /**
     * Finds the first annotation of a specific type that is either <em>present</em> or <em>meta-present</em> on the injection target.
     *
     * @param <A> The type of annotation.
     * @param annotationType The type to find an annotation for.
     * @return An {@link Optional} describing the first annotation of the given type, or {@link Optional#empty()} if the annotation is not present.
     */
    @SuppressWarnings("unchecked")
    public <A extends Annotation> Optional<A> findAnnotation(Class<A> annotationType) {
        return (Optional<A>) annotationFinder.apply(annotationType);
    }

    /**
     * Finds all <em>repeatable</em> annotations of a specific type that are either <em>present</em> or <em>meta-present</em> on the injection target.
     *
     * @param <A> The type of annotation.
     * @param annotationType The type to find annotations for.
     * @return A list with all annotations of the given type; possibly empty but never {@code null}.
     */
    @SuppressWarnings("unchecked")
    public <A extends Annotation> List<A> findRepeatableAnnotations(Class<A> annotationType) {
        return (List<A>) repeatableAnnotationFinder.apply(annotationType);
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

    static InjectionTarget forParameter(ParameterContext parameterContext) {
        Parameter parameter = parameterContext.getParameter();
        Class<?> declaringClass = parameterContext.getDeclaringExecutable().getDeclaringClass();
        return new InjectionTarget(declaringClass, parameter.getType(), parameter.getParameterizedType(),
                parameterContext::findAnnotation,
                parameterContext::findRepeatableAnnotations,
                ParameterResolutionException::new,
                ParameterResolutionException::new);
    }

    static InjectionTarget forField(Field field) {
        return new InjectionTarget(field.getDeclaringClass(), field.getType(), field.getGenericType(),
                type -> AnnotationSupport.findAnnotation(field, type),
                type -> AnnotationSupport.findRepeatableAnnotations(field, type),
                ExtensionConfigurationException::new,
                ExtensionConfigurationException::new);
    }
}
