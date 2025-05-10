/*
 * LoggingExtension.java
 * Copyright 2024 Rob Spoor
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

package com.github.robtimus.junit.support.extension.logging;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import com.github.robtimus.junit.support.extension.AutoCloseableResource;

@SuppressWarnings("nls")
abstract class LoggingExtension<R extends AutoCloseableResource> implements BeforeEachCallback {

    private static final Namespace NAMESPACE = Namespace.create(LoggingExtension.class);

    private static final List<LogResourceFactory> LOG_RESOURCE_FACTORIES = Stream.of(
            new JdkLogResourceFactory(),
            new Log4jLogResourceFactory(),
            new LogbackLogResourceFactory(),
            new Reload4jLogResourceFactory())
            .filter(LogResourceFactory::isAvailable)
            .collect(Collectors.toUnmodifiableList());

    private final Predicate<Field> fieldPredicate;
    private final Class<R> resourceType;
    private final ResourceFactory<R> resourceFactory;

    private final MethodHandles.Lookup lookup;

    LoggingExtension(Predicate<Field> fieldPredicate, Class<R> resourceType, ResourceFactory<R> resourceFactory) {
        this.fieldPredicate = fieldPredicate;
        this.resourceType = resourceType;
        this.resourceFactory = resourceFactory;

        this.lookup = MethodHandles.lookup();
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        for (Field field : ReflectionSupport.findFields(context.getRequiredTestClass(), fieldPredicate, HierarchyTraversalMode.TOP_DOWN)) {
            configureLogging(field, context);
        }
    }

    @SuppressWarnings("resource")
    private void configureLogging(Field field, ExtensionContext context) throws ReflectiveOperationException {
        Object logger = getLogger(field, context);

        context.getStore(NAMESPACE).getOrComputeIfAbsent(field, k -> newResource(logger, context), resourceType);
    }

    private Object getLogger(Field field, ExtensionContext context) throws ReflectiveOperationException {
        // addReads is necessary to allow accessing the class using var handles
        getClass().getModule().addReads(field.getDeclaringClass().getModule());

        if (Modifier.isStatic(field.getModifiers())) {
            return getLookup(field, null)
                    .findStaticVarHandle(field.getDeclaringClass(), field.getName(), field.getType())
                    .get();
        }
        Object target = context.getRequiredTestInstance();
        return getLookup(field, target)
                .findVarHandle(field.getDeclaringClass(), field.getName(), field.getType())
                .get(target);
    }

    private MethodHandles.Lookup getLookup(Field field, Object target) throws IllegalAccessException {
        return field.canAccess(target)
                ? lookup
                : MethodHandles.privateLookupIn(field.getDeclaringClass(), lookup);
    }

    private R newResource(Object logger, ExtensionContext context) {
        return LOG_RESOURCE_FACTORIES.stream()
                .map(factory -> resourceFactory.newResource(factory, logger, context))
                .filter(Optional::isPresent)
                .map(Optional::orElseThrow)
                .findAny()
                .orElseThrow(() -> unsupportedLoggerException(logger));
    }

    private JUnitException unsupportedLoggerException(Object logger) {
        if (logger == null) {
            return new PreconditionViolationException("null not supported");
        }
        return new PreconditionViolationException("Object type not supported: " + logger.getClass().getName());
    }

    interface ResourceFactory<R extends AutoCloseableResource> {

        Optional<R> newResource(LogResourceFactory resourceFactory, Object logger, ExtensionContext context);
    }
}
