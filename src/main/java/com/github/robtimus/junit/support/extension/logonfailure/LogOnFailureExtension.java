/*
 * LogOnFailureExtension.java
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

package com.github.robtimus.junit.support.extension.logonfailure;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

@SuppressWarnings("nls")
class LogOnFailureExtension implements BeforeEachCallback {

    private static final Namespace NAMESPACE = Namespace.create(LogOnFailureExtension.class);

    private static final List<LogCaptor.Factory> LOG_CAPTOR_FACTORIES = Stream.of(
            new JdkLogCaptorFactory(),
            new Log4jLogCaptorFactory(),
            new LogbackLogCaptorFactory(),
            new Reload4jLogCaptorFactory())
            .filter(LogCaptor.Factory::isAvailable)
            .collect(Collectors.toUnmodifiableList());

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        for (Field field : ReflectionSupport.findFields(context.getRequiredTestClass(), this::isAnnotated, HierarchyTraversalMode.TOP_DOWN)) {
            startCapture(field, context);
        }
    }

    private boolean isAnnotated(Field field) {
        return AnnotationSupport.isAnnotated(field, LogOnFailure.class);
    }

    private void startCapture(Field field, ExtensionContext context) throws ReflectiveOperationException {
        int modifiers = field.getModifiers();

        MethodHandles.Lookup lookup = Modifier.isPublic(modifiers)
                ? MethodHandles.lookup()
                : MethodHandles.privateLookupIn(field.getDeclaringClass(), MethodHandles.lookup());

        Object logger = Modifier.isStatic(modifiers)
                ? lookup.findStaticVarHandle(field.getDeclaringClass(), field.getName(), field.getType()).get()
                : lookup.findVarHandle(field.getDeclaringClass(), field.getName(), field.getType()).get(context.getRequiredTestInstance());

        context.getStore(NAMESPACE).getOrComputeIfAbsent(field, k -> getLogCaptor(logger, context), LogCaptor.class);
    }

    private LogCaptor getLogCaptor(Object logger, ExtensionContext context) {
        return LOG_CAPTOR_FACTORIES.stream()
                .map(factory -> factory.newLogCaptor(logger, context))
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
}
