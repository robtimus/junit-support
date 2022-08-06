/*
 * AbstractInjectExtension.java
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
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ModifierSupport;

/**
 * An abstract base class for <a href="http://junit.org/">JUnit</a> extensions that can inject values in fields and/or parameters,
 * based on a specific annotation.
 * <p>
 * A compatible annotation should look like this, where {@code MyExtension} extends {@code AbstractInjectExtension<MyAnnotation>}:
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
public abstract class AbstractInjectExtension<A extends Annotation> implements BeforeAllCallback, BeforeEachCallback, ParameterResolver {

    private final Class<A> annotationType;

    /**
     * Creates a new extension.
     *
     * @param annotationType The annotation type to check for.
     */
    protected AbstractInjectExtension(Class<A> annotationType) {
        this.annotationType = Objects.requireNonNull(annotationType);
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        injectFields(null, context.getRequiredTestClass(), ModifierSupport::isStatic, context);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        for (Object testInstance : context.getRequiredTestInstances().getAllInstances()) {
            injectFields(testInstance, testInstance.getClass(), ModifierSupport::isNotStatic, context);
        }
    }

    private void injectFields(Object testInstance, Class<?> testClass, Predicate<Field> predicate, ExtensionContext context) {
        for (Field field : AnnotationSupport.findAnnotatedFields(testClass, annotationType, predicate, HierarchyTraversalMode.TOP_DOWN)) {
            setValue(field, testInstance, context);
        }
    }

    private void setValue(Field field, Object testInstance, ExtensionContext context) {
        InjectionTarget target = InjectionTarget.forField(field);

        A annotation = target.findAnnotation(annotationType).orElseThrow(IllegalStateException::new);

        validateTarget(target, annotation, context).ifPresent(e -> {
            throw e;
        });

        Object value = resolveValue(annotation, target, context);

        try {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            field.set(testInstance, value);
        } catch (Exception e) {
            throwAsUncheckedException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void throwAsUncheckedException(Throwable t) throws T {
        throw (T) t;
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        A annotation = parameterContext.findAnnotation(annotationType).orElse(null);
        if (annotation == null) {
            return false;
        }
        InjectionTarget target = InjectionTarget.forParameter(parameterContext);
        return !validateTarget(target, annotation, extensionContext).isPresent();
    }

    /**
     * Validates that a target is valid for an injection annotation.
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
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        A annotation = parameterContext.findAnnotation(annotationType).orElseThrow(IllegalStateException::new);
        InjectionTarget target = InjectionTarget.forParameter(parameterContext);

        return resolveValue(annotation, target, extensionContext);
    }

    /**
     * Resolves the value to inject.
     *
     * @param annotation The injection annotation found on the target; never {@code null}.
     * @param target The target to inject the value in; never {@code null}.
     * @param context The current extension context; never {@code null}.
     * @return The value to inject; possibly {@code null}.
     */
    protected abstract Object resolveValue(A annotation, InjectionTarget target, ExtensionContext context);
}
