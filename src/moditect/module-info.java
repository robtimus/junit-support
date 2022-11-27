module com.github.robtimus.junit.support {
    requires transitive org.junit.jupiter.api;
    requires transitive org.junit.jupiter.params;
    requires transitive com.github.robtimus.io.functions;
    requires transitive org.hamcrest;
    requires org.junit.platform.commons;
    requires org.mockito;
    requires static java.logging;
    requires static ch.qos.reload4j;
    requires static org.slf4j;
    requires static ch.qos.logback.classic;
    requires static ch.qos.logback.core;
    requires static org.apache.logging.log4j;
    requires static org.apache.logging.log4j.core;

    exports com.github.robtimus.junit.support;
    exports com.github.robtimus.junit.support.extension;
    exports com.github.robtimus.junit.support.extension.testresource;
    exports com.github.robtimus.junit.support.params;
    exports com.github.robtimus.junit.support.test;
    exports com.github.robtimus.junit.support.test.collections;
    exports com.github.robtimus.junit.support.test.collections.annotation;
    exports com.github.robtimus.junit.support.test.io;
    exports com.github.robtimus.junit.support.util;
}
