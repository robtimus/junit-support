/*
 * InjectingExtension.java
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

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ModifierSupport;
import org.junit.platform.commons.support.ReflectionSupport;

/**
 * An abstract base class for <a href="http://junit.org/">JUnit</a> extensions that can inject values in fields and/or parameters.
 *
 * @author Rob Spoor
 * @since 2.0
 */
public abstract class InjectingExtension implements BeforeAllCallback, BeforeEachCallback, ParameterResolver {

    private final Predicate<Field> fieldPredicate;
    private final MethodHandles.Lookup lookup;

    /**
     * Creates a new extension.
     * <p>
     * Concrete sub classes should call {@link MethodHandles#lookup()} and pass the result to this constructor.
     * This prevents extensions piggy-backing on access granted to this class' module.
     *
     * @param fieldPredicate A predicate that determines which fields are eligible for injection.
     * @param lookup The object to use for looking up the objects that are necessary for setting field values.
     * @throws NullPointerException If the given predicate or lookup is {@code null}.
     */
    protected InjectingExtension(Predicate<Field> fieldPredicate, MethodHandles.Lookup lookup) {
        this.fieldPredicate = Objects.requireNonNull(fieldPredicate);
        this.lookup = Objects.requireNonNull(lookup);
    }

    @Override
    public final void beforeAll(ExtensionContext context) throws Exception {
        injectFields(null, context.getRequiredTestClass(), ModifierSupport::isStatic, context);
    }

    @Override
    public final void beforeEach(ExtensionContext context) throws Exception {
        for (Object testInstance : context.getRequiredTestInstances().getAllInstances()) {
            injectFields(testInstance, testInstance.getClass(), ModifierSupport::isNotStatic, context);
        }
    }

    private void injectFields(Object testInstance, Class<?> testClass, Predicate<Field> predicate, ExtensionContext context) {
        for (Field field : ReflectionSupport.findFields(testClass, fieldPredicate.and(predicate), HierarchyTraversalMode.TOP_DOWN)) {
            setValue(field, testInstance, context);
        }
    }

    private void setValue(Field field, Object testInstance, ExtensionContext context) {
        InjectionTarget target = InjectionTarget.forField(field);

        validateTarget(target, context).ifPresent(e -> {
            throw e;
        });

        // addReads is necessary to allow accessing the class using var handles
        InjectingExtension.class.getModule().addReads(field.getDeclaringClass().getModule());

        try {
            Object value = resolveValue(target, context);

            if (Modifier.isStatic(field.getModifiers())) {
                getLookup(field, null).findStaticVarHandle(field.getDeclaringClass(), field.getName(), field.getType()).set(value);
            } else {
                getLookup(field, testInstance).findVarHandle(field.getDeclaringClass(), field.getName(), field.getType()).set(testInstance, value);
            }
        } catch (Exception e) {
            throwAsUncheckedException(e);
        }
    }

    private MethodHandles.Lookup getLookup(Field field, Object target) throws IllegalAccessException {
        return field.canAccess(target)
                ? lookup
                : MethodHandles.privateLookupIn(field.getDeclaringClass(), lookup);
    }

    @Override
    public final boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        InjectionTarget target = InjectionTarget.forParameter(parameterContext);
        return !validateTarget(target, extensionContext).isPresent();
    }

    /**
     * Validates that a target is valid for injecting.
     * <p>
     * If this method returns a non-empty {@link Optional} for parameter injection, {@link #supportsParameter(ParameterContext, ExtensionContext)}
     * will return {@code false}, and JUnit will fail if no other extension supports the parameter.
     * <p>
     * If this method returns a non-empty {@link Optional} for field injection, the exception is thrown. This situation may or may not be prevented
     * using the field predicate used to create this extension. In some cases the predicate may not test all aspects that are used to inject a value
     * into fields; if that's the case, throwing an error may be an appropriate action.
     *
     * @param target The target to validate; never {@code null}.
     * @param context The current extension context; never {@code null}.
     * @return {@link Optional#empty()} if the given target is valid for injecting, or an {@link Optional} describing an exception that indicates why
     *         the target is invalid otherwise. In that case, the exception should have been created using
     *         {@link InjectionTarget#createException(String)} or {@link InjectionTarget#createException(String, Throwable)}.
     */
    protected abstract Optional<JUnitException> validateTarget(InjectionTarget target, ExtensionContext context);

    @Override
    public final Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        InjectionTarget target = InjectionTarget.forParameter(parameterContext);

        try {
            return resolveValue(target, extensionContext);
        } catch (Exception e) {
            return throwAsUncheckedException(e);
        }
    }

    /**
     * Resolves the value to inject.
     * <p>
     * When this method is called for parameter injection, {@link #supportsParameter(ParameterContext, ExtensionContext)} will have returned
     * {@code true}, which means that {@link #validateTarget(InjectionTarget, ExtensionContext)} will have returned an empty {@link Optional}.
     * <p>
     * When this method is called for field injection, {@link #validateTarget(InjectionTarget, ExtensionContext)} will have been called and verified
     * to have returned an empty {@link Optional}.
     *
     * @param target The target to inject the value in; never {@code null}.
     * @param context The current extension context; never {@code null}.
     * @return The value to inject; possibly {@code null}.
     * @throws Exception If the value could not be resolved.
     */
    protected abstract Object resolveValue(InjectionTarget target, ExtensionContext context) throws Exception;

    @SuppressWarnings("unchecked")
    private static <T extends Throwable, R> R throwAsUncheckedException(Throwable t) throws T {
        throw (T) t;
    }
}
