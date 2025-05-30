/*
 * LogbackLogResourceFactory.java
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtensionContext;
import com.github.robtimus.junit.support.extension.logging.capture.CapturingLogbackAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

final class LogbackLogResourceFactory extends LogResourceFactory {

    @Override
    Optional<LogCaptor> startCapture(Object logger, ExtensionContext context) {
        return Factory.startCapture(logger, context);
    }

    @Override
    Optional<LogDisabler> disableLogging(Object logger) {
        return Factory.disableLogging(logger);
    }

    // Use a separate nested class to prevent class loading errors if logback is not available.
    // This nested class is only loaded when newLogCaptor or newLogDisabler is called.

    private static final class Factory {

        private Factory() {
        }

        @SuppressWarnings("resource")
        private static Optional<LogCaptor> startCapture(Object logger, ExtensionContext context) {
            if (logger instanceof Logger) {
                return Optional.of(startCapture((Logger) logger, context));
            }
            return Optional.empty();
        }

        private static LogCaptor startCapture(Logger logger, ExtensionContext context) {
            List<Appender<ILoggingEvent>> originalAppenders = listAppenders(logger);
            boolean originalAdditive = logger.isAdditive();

            originalAppenders.forEach(logger::detachAppender);
            logger.setAdditive(false);

            CapturingLogbackAppender capturingAppender = new CapturingLogbackAppender();
            capturingAppender.start();
            logger.addAppender(capturingAppender);

            return () -> {
                restoreSettings(logger, originalAppenders, originalAdditive);
                context.getExecutionException().ifPresent(t -> logCaptured(logger, capturingAppender));
            };
        }

        private static List<Appender<ILoggingEvent>> listAppenders(Logger logger) {
            List<Appender<ILoggingEvent>> appenders = new ArrayList<>();
            for (Iterator<Appender<ILoggingEvent>> i = logger.iteratorForAppenders(); i.hasNext(); ) {
                appenders.add(i.next());
            }
            return appenders;
        }

        private static void restoreSettings(Logger logger, List<Appender<ILoggingEvent>> originalAppenders, boolean originalAdditive) {
            List<Appender<ILoggingEvent>> appenders = listAppenders(logger);
            appenders.forEach(logger::detachAppender);

            originalAppenders.forEach(logger::addAppender);
            logger.setAdditive(originalAdditive);
        }

        private static void logCaptured(Logger logger, CapturingLogbackAppender capturingAppender) {
            List<ILoggingEvent> events = capturingAppender.getEvents();
            // logger.log takes a different type of event
            events.stream()
                    .filter(event -> logger.isEnabledFor(event.getLevel()))
                    .forEach(logger::callAppenders);
        }

        @SuppressWarnings("resource")
        private static Optional<LogDisabler> disableLogging(Object logger) {
            if (logger instanceof Logger) {
                return Optional.of(disableLogging((Logger) logger));
            }
            return Optional.empty();
        }

        private static LogDisabler disableLogging(Logger logger) {
            Level originalLevel = logger.getLevel();
            logger.setLevel(Level.OFF);

            return () -> logger.setLevel(originalLevel);
        }
    }
}
