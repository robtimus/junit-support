module com.github.robtimus.unittestsupport {
    requires transitive org.junit.jupiter.api;
    requires transitive org.junit.jupiter.params;
    requires transitive com.github.robtimus.io.functions;
    requires org.hamcrest;
    requires org.mockito;

    exports com.github.robtimus.unittestsupport;
    exports com.github.robtimus.unittestsupport.collections;
    exports com.github.robtimus.unittestsupport.collections.annotation;
    exports com.github.robtimus.unittestsupport.io;
}
