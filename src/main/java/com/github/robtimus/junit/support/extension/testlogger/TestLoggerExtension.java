/*
 * LoggingContextExtension.java
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

package com.github.robtimus.junit.support.extension.testlogger;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.support.AnnotationSupport;
import com.github.robtimus.junit.support.extension.InjectingExtension;
import com.github.robtimus.junit.support.extension.InjectionTarget;
import com.github.robtimus.junit.support.extension.testlogger.TestLogger.ForClass;
import com.github.robtimus.junit.support.extension.testlogger.TestLogger.Root;

@SuppressWarnings("nls")
class TestLoggerExtension extends InjectingExtension {

    private static final Namespace NAMESPACE = Namespace.create(TestLoggerExtension.class);
    private static final Object ROOT_KEY = true;

    private static final Map<Class<? extends LoggerContext<?, ?>>, ContextFactory<?>> CONTEXT_FACTORIES;

    static {
        Map<Class<? extends LoggerContext<?, ?>>, ContextFactory<?>> contextFactories = new HashMap<>();
        contextFactories.put(JdkLoggerContext.class,
                new ContextFactory<>(JdkLoggerContext::forLogger, JdkLoggerContext::forLogger, JdkLoggerContext::forRootLogger));
        contextFactories.put(Log4jLoggerContext.class,
                new ContextFactory<>(Log4jLoggerContext::forLogger, Log4jLoggerContext::forLogger, Log4jLoggerContext::forRootLogger));
        contextFactories.put(LogbackLoggerContext.class,
                new ContextFactory<>(LogbackLoggerContext::forLogger, LogbackLoggerContext::forLogger, LogbackLoggerContext::forRootLogger));
        contextFactories.put(Reload4jLoggerContext.class,
                new ContextFactory<>(Reload4jLoggerContext::forLogger, Reload4jLoggerContext::forLogger, Reload4jLoggerContext::forRootLogger));
        CONTEXT_FACTORIES = Collections.unmodifiableMap(contextFactories);
    }

    TestLoggerExtension() {
        super(TestLoggerExtension::hasSupportedAnnotation);
    }

    private static boolean hasSupportedAnnotation(Field field) {
        return AnnotationSupport.isAnnotated(field, TestLogger.class)
                || AnnotationSupport.isAnnotated(field, TestLogger.ForClass.class)
                || AnnotationSupport.isAnnotated(field, TestLogger.Root.class);
    }

    @Override
    protected Optional<JUnitException> validateTarget(InjectionTarget target, ExtensionContext context) {
        // No need to check the type - this extension is package private, and can only be triggered in combination with
        // @TestLogger, @TestLogger.ForClass and/or TestLogger.Root.class
        // don't validate the number of annotations yet
        Class<?> targetType = target.type();
        return CONTEXT_FACTORIES.containsKey(targetType)
                ? Optional.empty()
                : Optional.of(target.createException("Target type not supported: " + targetType));
    }

    @Override
    protected Object resolveValue(InjectionTarget target, ExtensionContext context) throws Exception {
        Class<?> targetType = target.type();
        ContextFactory<?> contextFactory = CONTEXT_FACTORIES.get(targetType);
        return resolveContext(target, contextFactory, context);
    }

    private LoggerContext<?, ?> resolveContext(InjectionTarget target, ContextFactory<?> contextFactory, ExtensionContext context) {
        TestLogger testLogger = target.findAnnotation(TestLogger.class)
                .orElse(null);
        TestLogger.ForClass testLoggerForClass = target.findAnnotation(TestLogger.ForClass.class)
                .orElse(null);
        TestLogger.Root testLoggerRoot = target.findAnnotation(TestLogger.Root.class)
                .orElse(null);

        validateAnnotations(testLogger, testLoggerForClass, testLoggerRoot);

        if (testLogger != null) {
            String loggerName = testLogger.value();
            return context.getStore(NAMESPACE).getOrComputeIfAbsent(loggerName, contextFactory::newContext, CloseableContext.class).context;
        }
        if (testLoggerForClass != null) {
            Class<?> loggerClass = testLoggerForClass.value();
            return context.getStore(NAMESPACE).getOrComputeIfAbsent(loggerClass, contextFactory::newContext, CloseableContext.class).context;
        }
        return context.getStore(NAMESPACE).getOrComputeIfAbsent(ROOT_KEY, o -> contextFactory.newRootContext(), CloseableContext.class).context;
    }

    private void validateAnnotations(TestLogger testLogger, ForClass testLoggerForClass, Root testLoggerRoot) {
        long count = Stream.of(testLogger, testLoggerForClass, testLoggerRoot)
                .filter(Objects::nonNull)
                .count();
        if (count != 1) {
            throw new PreconditionViolationException("Exactly one of @TestLogger, @TestLogger.ForClass and @TestLogger.Root required");
        }
    }

    private static final class ContextFactory<C extends LoggerContext<?, ?>> {

        private final Function<String, C> newContextFromName;
        private final Function<Class<?>, C> newContextFromClass;
        private final Supplier<C> newRootContext;

        private ContextFactory(Function<String, C> newContextFromName, Function<Class<?>, C> newContextFromClass, Supplier<C> newRootContext) {
            this.newContextFromName = newContextFromName;
            this.newContextFromClass = newContextFromClass;
            this.newRootContext = newRootContext;
        }

        private CloseableContext newContext(String loggerName) {
            C context = newContextFromName.apply(loggerName);
            return new CloseableContext(context);
        }

        private CloseableContext newContext(Class<?> loggerClass) {
            C context = newContextFromClass.apply(loggerClass);
            return new CloseableContext(context);
        }

        private CloseableContext newRootContext() {
            C context = newRootContext.get();
            return new CloseableContext(context);
        }
    }

    private static final class CloseableContext implements CloseableResource {

        private final LoggerContext<?, ?> context;

        private CloseableContext(LoggerContext<?, ?> context) {
            this.context = context;
            context.saveSettings();
        }

        @Override
        public void close() throws Throwable {
            context.restore();
        }
    }
}
