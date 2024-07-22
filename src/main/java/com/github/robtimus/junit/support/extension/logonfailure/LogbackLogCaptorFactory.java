/*
 * LogbackLogCaptorFactory.java
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

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.ArgumentCaptor;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

final class LogbackLogCaptorFactory extends LogCaptor.Factory {

    @Override
    Optional<LogCaptor> newLogCaptor(Object logger, ExtensionContext context) {
        return Factory.newLogCaptor(logger, context);
    }

    // Use a separate nested class to prevent class loading errors if logback is not available.
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
            List<Appender<ILoggingEvent>> originalAppenders = listAppenders(logger);
            boolean originalAdditive = logger.isAdditive();

            originalAppenders.forEach(logger::detachAppender);
            logger.setAdditive(false);

            @SuppressWarnings("unchecked")
            Appender<ILoggingEvent> capturingAppender = mock(Appender.class);
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

        private static void logCaptured(Logger logger, Appender<ILoggingEvent> capturingAppender) {
            ArgumentCaptor<ILoggingEvent> eventCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
            verify(capturingAppender, atLeast(0)).doAppend(eventCaptor.capture());
            List<ILoggingEvent> events = eventCaptor.getAllValues();
            // logger.log takes a different type of event
            events.stream()
                    .filter(event -> logger.isEnabledFor(event.getLevel()))
                    .forEach(logger::callAppenders);
        }
    }
}
