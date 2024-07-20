/*
 * TestLoggerExtension.java
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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

    private static final Map<Class<? extends LoggerContext>, ContextFactory<?>> CONTEXT_FACTORIES = Map.of(
            JdkLoggerContext.class, new JdkLoggerContext.Factory(),
            Log4jLoggerContext.class, new Log4jLoggerContext.Factory(),
            LogbackLoggerContext.class, new LogbackLoggerContext.Factory(),
            Reload4jLoggerContext.class, new Reload4jLoggerContext.Factory());

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
        LoggerContext loggerContext = resolveContext(target, contextFactory, context);
        if (target.isAnnotated(DisableLogging.class)) {
            loggerContext.disable();
        }
        if (target.isAnnotated(LogOnFailure.class)) {
            loggerContext.startCapture();
        }
        return loggerContext;
    }

    private LoggerContext resolveContext(InjectionTarget target, ContextFactory<?> contextFactory, ExtensionContext context) {
        TestLogger testLogger = target.findAnnotation(TestLogger.class)
                .orElse(null);
        TestLogger.ForClass testLoggerForClass = target.findAnnotation(TestLogger.ForClass.class)
                .orElse(null);
        TestLogger.Root testLoggerRoot = target.findAnnotation(TestLogger.Root.class)
                .orElse(null);

        validateAnnotations(testLogger, testLoggerForClass, testLoggerRoot);

        if (testLogger != null) {
            String loggerName = testLogger.value();
            String key = contextFactory.key(loggerName);
            return context.getStore(NAMESPACE).getOrComputeIfAbsent(key, k -> contextFactory.newContext(loggerName, context), CloseableContext.class)
                    .loggerContext;
        }
        if (testLoggerForClass != null) {
            Class<?> loggerClass = testLoggerForClass.value();
            String key = contextFactory.key(loggerClass);
            return context.getStore(NAMESPACE).getOrComputeIfAbsent(key, k -> contextFactory.newContext(loggerClass, context), CloseableContext.class)
                    .loggerContext;
        }
        String key = contextFactory.rootKey();
        return context.getStore(NAMESPACE).getOrComputeIfAbsent(key, o -> contextFactory.newRootContext(context), CloseableContext.class)
                .loggerContext;
    }

    private void validateAnnotations(TestLogger testLogger, ForClass testLoggerForClass, Root testLoggerRoot) {
        long count = Stream.of(testLogger, testLoggerForClass, testLoggerRoot)
                .filter(Objects::nonNull)
                .count();
        if (count != 1) {
            throw new PreconditionViolationException("Exactly one of @TestLogger, @TestLogger.ForClass and @TestLogger.Root required");
        }
    }

    abstract static class ContextFactory<C extends LoggerContext> {

        private CloseableContext newContext(String loggerName, ExtensionContext context) {
            C loggerContext = newLoggerContext(loggerName);
            return new CloseableContext(loggerContext, context);
        }

        private CloseableContext newContext(Class<?> loggerClass, ExtensionContext context) {
            C loggerContext = newLoggerContext(loggerClass);
            return new CloseableContext(loggerContext, context);
        }

        private CloseableContext newRootContext(ExtensionContext context) {
            C loggerContext = newRootLoggerContext();
            return new CloseableContext(loggerContext, context);
        }

        abstract C newLoggerContext(String loggerName);

        abstract C newLoggerContext(Class<?> loggerClass);

        abstract C newRootLoggerContext();

        abstract String keyPrefix();

        abstract String loggerName(Class<?> loggerClass);

        abstract String rootLoggerName();

        private String key(String loggerName) {
            return keyPrefix() + "." + loggerName;
        }

        private String key(Class<?> loggerClass) {
            return key(loggerName(loggerClass));
        }

        private String rootKey() {
            return key(rootLoggerName());
        }
    }

    private static final class CloseableContext implements CloseableResource {

        private final LoggerContext loggerContext;
        private final ExtensionContext context;

        private CloseableContext(LoggerContext loggerContext, ExtensionContext context) {
            this.loggerContext = loggerContext;
            this.context = context;
            loggerContext.saveSettings();
        }

        @Override
        public void close() throws Throwable {
            loggerContext.restore();
            context.getExecutionException().ifPresent(t -> loggerContext.logCaptured());
        }
    }
}
