/*
 * module-info.java
 * Copyright 2020 Rob Spoor
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

/**
 * Contains interfaces and classes that make it easier to write tests with JUnit
 */
module com.github.robtimus.junit.support {
    requires transitive org.junit.jupiter.api;
    requires transitive org.junit.jupiter.params;
    requires transitive com.github.robtimus.io.functions;
    requires transitive org.hamcrest;
    requires org.junit.platform.commons;
    requires org.mockito;
    requires static java.logging;
    requires static org.apache.logging.log4j;
    requires static org.apache.logging.log4j.core;
    requires static ch.qos.logback.classic;
    requires static ch.qos.logback.core;
    requires static ch.qos.reload4j;
    requires static org.slf4j;

    exports com.github.robtimus.junit.support;
    exports com.github.robtimus.junit.support.concurrent;
    exports com.github.robtimus.junit.support.extension;
    exports com.github.robtimus.junit.support.extension.logonfailure;
    exports com.github.robtimus.junit.support.extension.testlogger;
    exports com.github.robtimus.junit.support.extension.testresource;
    exports com.github.robtimus.junit.support.params;
    exports com.github.robtimus.junit.support.test;
    exports com.github.robtimus.junit.support.test.collections;
    exports com.github.robtimus.junit.support.test.collections.annotation;
    exports com.github.robtimus.junit.support.test.io;
    exports com.github.robtimus.junit.support.util;
}
