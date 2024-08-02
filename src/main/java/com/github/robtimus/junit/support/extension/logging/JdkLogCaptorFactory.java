/*
 * JdkLogCaptorFactory.java
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.ArgumentCaptor;

final class JdkLogCaptorFactory extends LogCaptor.Factory {

    @Override
    Optional<LogCaptor> newLogCaptor(Object logger, ExtensionContext context) {
        return Factory.newLogCaptor(logger, context);
    }

    // Use a separate nested class to prevent class loading errors if the java.logging module is not available.
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
            List<Handler> originalHandlers = listHandlers(logger);
            boolean originalUseParentHandlers = logger.getUseParentHandlers();

            originalHandlers.forEach(logger::removeHandler);
            logger.setUseParentHandlers(false);

            Handler capturingHandler = mock(Handler.class);
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

        private static void logCaptured(Logger logger, Handler capturingHandler) {
            ArgumentCaptor<LogRecord> recordCaptor = ArgumentCaptor.forClass(LogRecord.class);
            verify(capturingHandler, atLeast(0)).publish(recordCaptor.capture());
            List<LogRecord> logRecords = recordCaptor.getAllValues();
            logRecords.forEach(logger::log);
        }
    }
}
