/*
 * LogResourceFactory.java
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

import java.util.Optional;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;

abstract class LogResourceFactory {

    final boolean isAvailable() {
        try {
            startCapture(0, null);
            disableLogging(null);
            return true;
        } catch (@SuppressWarnings("unused") NoClassDefFoundError e) {
            // Ideally we would use JUnit's logging mechanism here, but that's not visible externally.
            // Using a specific logging framework is also not desired, so no logging is performed.
            return false;
        }
    }

    abstract Optional<LogCaptor> startCapture(Object logger, ExtensionContext context);

    abstract Optional<LogDisabler> disableLogging(Object logger);

    interface LogCaptor extends CloseableResource {
        // no additional methods necessary
    }

    interface LogDisabler extends CloseableResource {
        // no additional methods necessary
    }
}
