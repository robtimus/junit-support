/*
 * Log4jTestAppender.java
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

@SuppressWarnings("javadoc")
@Plugin(name = "Log4jTest", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class Log4jTestAppender extends AbstractAppender {

    private final List<LogEvent> events = new ArrayList<>();

    public Log4jTestAppender(String name) {
        this(name, null, PatternLayout.createDefaultLayout(), false);
    }

    public Log4jTestAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions, null);
    }

    @PluginFactory
    public static Log4jTestAppender createAppender(@PluginAttribute("name") String name) {
        return new Log4jTestAppender(name);
    }

    @Override
    public void append(LogEvent event) {
        if (!isFiltered(event)) {
            events.add(event.toImmutable());
        }
    }

    List<LogEvent> getEvents() {
        return events;
    }

    void clearEvents() {
        events.clear();
    }
}
