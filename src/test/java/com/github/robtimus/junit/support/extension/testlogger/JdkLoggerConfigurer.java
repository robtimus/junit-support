/*
 * JdkLoggerConfigurer.java
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings({ "nls", "javadoc" })
public class JdkLoggerConfigurer {

    private static final Logger LOGGER = Logger.getLogger(TestLogger.class.getName());
    private static final Logger ROOT_LOGGER = Logger.getLogger("");

    public JdkLoggerConfigurer() {
        LOGGER.setLevel(Level.INFO);
        assertArrayEquals(new Handler[0], LOGGER.getHandlers());
        LOGGER.addHandler(new JdkTestHandler());

        ROOT_LOGGER.setLevel(Level.WARNING);
        assertArrayEquals(new Handler[0], ROOT_LOGGER.getHandlers());
        ROOT_LOGGER.addHandler(new JdkTestHandler());
    }
}
