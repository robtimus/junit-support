/*
 * AnnotationBasedInjectingExtension.java
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
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.support.AnnotationSupport;

/**
 * An abstract base class for <a href="http://junit.org/">JUnit</a> extensions that can inject values in fields and/or parameters,
 * based on a specific annotation.
 * <p>
 * A compatible annotation should look like this, where {@code MyExtension} extends {@code AnnotationBasedInjectingExtension<MyAnnotation>}:
 * <pre><code>
 * &#64;ExtendWith(MyExtension.class)
 * &#64;Target({ ElementType.FIELD, ElementType.PARAMETER })
 * &#64;Retention(RetentionPolicy.RUNTIME)
 * public &#64;interface MyAnnotation {
 *     // add fields as needed
 * }
 * </code></pre>
 *
 * @author Rob Spoor
 * @param <A> The type of annotation to use for fields and/or parameters.
 * @since 2.0
 */
@SuppressWarnings("nls")
public abstract class AnnotationBasedInjectingExtension<A extends Annotation> extends InjectingExtension {

    private final Class<A> annotationType;

    /**
     * Creates a new extension.
     *
     * @param annotationType The annotation type to check for.
     * @throws NullPointerException If the given annotation type is {@code null}.
     */
    protected AnnotationBasedInjectingExtension(Class<A> annotationType) {
        super(field -> AnnotationSupport.isAnnotated(field, annotationType));
        this.annotationType = Objects.requireNonNull(annotationType);
    }

    @Override
    protected final Optional<JUnitException> validateTarget(InjectionTarget target, ExtensionContext context) {
        A annotation = target.findAnnotation(annotationType).orElse(null);
        if (annotation == null) {
            // Note: for fields, this will not occur due to the field predicate
            return Optional.of(target.createException("Target not annotated with @" + annotationType.getSimpleName()));
        }
        return validateTarget(target, annotation, context);
    }

    /**
     * Validates that a target is valid for an injection annotation.
     * <p>
     * This method will be called from {@link #validateTarget(InjectionTarget, ExtensionContext)} when the annotation is present on the injection
     * target.
     * <p>
     * For field injection, the field is found using only the annotation. If this method returns a non-empty {@link Optional}, the exception is
     * thrown. This is used to indicate the field is not properly configured, e.g. because it has an incorrect type.
     *
     * @param target The target to validate; never {@code null}.
     * @param annotation The injection annotation found on the target; never {@code null}.
     * @param context The current extension context; never {@code null}.
     * @return {@link Optional#empty()} if the given target is valid for the given annotation, or an {@link Optional} describing an exception that
     *         indicates why the target is invalid otherwise. In that case, the exception should have been created using
     *         {@link InjectionTarget#createException(String)} or {@link InjectionTarget#createException(String, Throwable)}.
     */
    protected abstract Optional<JUnitException> validateTarget(InjectionTarget target, A annotation, ExtensionContext context);

    @Override
    protected final Object resolveValue(InjectionTarget target, ExtensionContext context) throws Exception {
        A annotation = target.findAnnotation(annotationType).orElseThrow(IllegalStateException::new);

        return resolveValue(target, annotation, context);
    }

    /**
     * Resolves the value to inject.
     * <p>
     * When this method is called for parameter injection, {@link #supportsParameter(ParameterContext, ExtensionContext)} will have returned
     * {@code true}, which means that {@link #validateTarget(InjectionTarget, ExtensionContext)}, and by proxy
     * {@link #validateTarget(InjectionTarget, Annotation, ExtensionContext)}, will have returned an empty {@link Optional}.
     * <p>
     * When this method is called for field injection, {@link #validateTarget(InjectionTarget, ExtensionContext)}, and by proxy
     * {@link #validateTarget(InjectionTarget, Annotation, ExtensionContext)}, will have been called and verified to have returned an empty
     * {@link Optional}.
     *
     * @param target The target to inject the value in; never {@code null}.
     * @param annotation The injection annotation found on the target; never {@code null}.
     * @param context The current extension context; never {@code null}.
     * @return The value to inject; possibly {@code null}.
     * @throws Exception If the value could not be resolved.
     */
    protected abstract Object resolveValue(InjectionTarget target, A annotation, ExtensionContext context) throws Exception;
}
