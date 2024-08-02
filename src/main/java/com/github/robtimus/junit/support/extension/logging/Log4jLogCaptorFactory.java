/*
 * Log4jLogCaptorFactory.java
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

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.ArgumentCaptor;
import com.github.robtimus.junit.support.extension.testlogger.Log4jNullAppender;

final class Log4jLogCaptorFactory extends LogCaptor.Factory {

    @Override
    Optional<LogCaptor> newLogCaptor(Object logger, ExtensionContext context) {
        return Factory.newLogCaptor(logger, context);
    }

    // Use a separate nested class to prevent class loading errors if Log4j is not available.
    // This nested class is only loaded when newLogCaptor is called.

    private static final class Factory {

        private Factory() {
        }

        private static Optional<LogCaptor> newLogCaptor(Object logger, ExtensionContext context) {
            if (logger instanceof Logger) {
                return Optional.of(newLogCaptor((Logger) logger, context));
            }
            return Optional.empty();
        }

        private static LogCaptor newLogCaptor(Logger logger, ExtensionContext context) {
            List<Appender> originalAppenders = listAppenders(logger);
            boolean originalAdditive = logger.isAdditive();

            originalAppenders.forEach(logger::removeAppender);
            logger.setAdditive(false);

            Log4jNullAppender capturingAppender = spy(Log4jNullAppender.create("LogOnFailure-" + UUID.randomUUID().toString())); //$NON-NLS-1$
            logger.addAppender(capturingAppender);

            return () -> {
                restoreSettings(logger, originalAppenders, originalAdditive);
                context.getExecutionException().ifPresent(t -> logCaptured(logger, capturingAppender));
            };
        }

        private static List<Appender> listAppenders(Logger logger) {
            return new ArrayList<>(logger.getAppenders().values());
        }

        private static void restoreSettings(Logger logger, List<Appender> originalAppenders, boolean originalAdditive) {
            List<Appender> appenders = listAppenders(logger);
            appenders.forEach(logger::removeAppender);

            originalAppenders.forEach(logger::addAppender);
            logger.setAdditive(originalAdditive);
        }

        private static void logCaptured(Logger logger, Log4jNullAppender capturingAppender) {
            ArgumentCaptor<LogEvent> eventCaptor = ArgumentCaptor.forClass(LogEvent.class);
            verify(capturingAppender, atLeast(0)).ignore(eventCaptor.capture());
            List<LogEvent> events = eventCaptor.getAllValues();
            LoggerConfig loggerConfig = logger.get();
            events.forEach(loggerConfig::log);
        }
    }
}
