/*
 * Reload4jLogResourceFactory.java
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
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.jupiter.api.extension.ExtensionContext;
import com.github.robtimus.junit.support.extension.logging.capture.CapturingReload4jAppender;

final class Reload4jLogResourceFactory extends LogResourceFactory {

    @Override
    Optional<LogCaptor> startCapture(Object logger, ExtensionContext context) {
        return Factory.startCapture(logger, context);
    }

    @Override
    Optional<LogDisabler> disableLogging(Object logger) {
        return Factory.disableLogging(logger);
    }

    // Use a separate nested class to prevent class loading errors if reload4j is not available.
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
            List<Appender> originalAppenders = listAppenders(logger);
            boolean originalAdditivity = logger.getAdditivity();

            logger.removeAllAppenders();
            logger.setAdditivity(false);

            CapturingReload4jAppender capturingAppender = new CapturingReload4jAppender();
            logger.addAppender(capturingAppender);

            return () -> {
                restoreSettings(logger, originalAppenders, originalAdditivity);
                context.getExecutionException().ifPresent(t -> logCaptured(logger, capturingAppender));
            };
        }

        private static List<Appender> listAppenders(Logger logger) {
            List<Appender> appenders = new ArrayList<>();
            // getAllAppenders uses raw types...
            for (Enumeration<?> e = logger.getAllAppenders(); e.hasMoreElements(); ) {
                appenders.add((Appender) e.nextElement());
            }
            return appenders;
        }

        private static void restoreSettings(Logger logger, List<Appender> originalAppenders, boolean originalAdditivity) {
            logger.removeAllAppenders();

            originalAppenders.forEach(logger::addAppender);
            logger.setAdditivity(originalAdditivity);
        }

        private static void logCaptured(Logger logger, CapturingReload4jAppender capturingAppender) {
            List<LoggingEvent> events = capturingAppender.getEvents();
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
