module com.github.robtimus.junit.support {
    requires transitive org.junit.jupiter.api;
    requires transitive org.junit.jupiter.params;
    requires transitive com.github.robtimus.io.functions;
    requires transitive org.hamcrest;
    requires org.mockito;

    exports com.github.robtimus.junit.support;
    exports com.github.robtimus.junit.support.collections;
    exports com.github.robtimus.junit.support.collections.annotation;
    exports com.github.robtimus.junit.support.io;
    exports com.github.robtimus.junit.support.matchers.nio.file;
}
