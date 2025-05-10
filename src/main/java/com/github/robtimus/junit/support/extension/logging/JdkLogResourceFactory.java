/*
 * JdkLogResourceFactory.java
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.extension.ExtensionContext;
import com.github.robtimus.junit.support.extension.logging.capture.CapturingJdkHandler;

final class JdkLogResourceFactory extends LogResourceFactory {

    @Override
    Optional<LogCaptor> startCapture(Object logger, ExtensionContext context) {
        return Factory.startCapture(logger, context);
    }

    @Override
    Optional<LogDisabler> disableLogging(Object logger) {
        return Factory.disableLogging(logger);
    }

    // Use a separate nested class to prevent class loading errors if the java.logging module is not available.
    // This nested class is only loaded when newLogCaptor or newLogDisabler is called.

    private static final class Factory {

        private Factory() {
        }

        private static Optional<LogCaptor> startCapture(Object logger, ExtensionContext context) {
            if (logger instanceof Logger) {
                return Optional.of(startCapture((Logger) logger, context));
            }
            return Optional.empty();
        }

        private static LogCaptor startCapture(Logger logger, ExtensionContext context) {
            List<Handler> originalHandlers = listHandlers(logger);
            boolean originalUseParentHandlers = logger.getUseParentHandlers();

            originalHandlers.forEach(logger::removeHandler);
            logger.setUseParentHandlers(false);

            CapturingJdkHandler capturingHandler = new CapturingJdkHandler();
            logger.addHandler(capturingHandler);

            return () -> {
                restoreSettings(logger, originalHandlers, originalUseParentHandlers);
                context.getExecutionException().ifPresent(t -> logCaptured(logger, capturingHandler));
            };
        }

        private static List<Handler> listHandlers(Logger logger) {
            return new ArrayList<>(Arrays.asList(logger.getHandlers()));
        }

        private static void restoreSettings(Logger logger, List<Handler> originalHandlers, boolean originalUseParentHandlers) {
            List<Handler> handlers = listHandlers(logger);
            handlers.forEach(logger::removeHandler);

            originalHandlers.forEach(logger::addHandler);
            logger.setUseParentHandlers(originalUseParentHandlers);
        }

        private static void logCaptured(Logger logger, CapturingJdkHandler capturingHandler) {
            List<LogRecord> logRecords = capturingHandler.getRecords();
            logRecords.forEach(logger::log);
        }

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
