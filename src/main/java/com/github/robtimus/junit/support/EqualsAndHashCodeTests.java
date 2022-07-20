/*
 * EqualsAndHashCodeTests.java
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

package com.github.robtimus.junit.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Base interface for testing {@link Object#equals(Object)} and {@link Object#hashCode()} implementations.
 * This interface supports testing a single object only. To test multiple objects, use {@link Nested @Nested} to create multiple implementations.
 * <p>
 * Note that equality tests are missing transitivity tests. Symmetry is tested in {@link #testEqual(Object)}.
 *
 * @author Rob Spoor
 * @param <T> The type of object to test.
 * @since 2.0
 */
@TestInstance(Lifecycle.PER_CLASS)
public interface EqualsAndHashCodeTests<T> {

    /**
     * Returns the object to test.
     *
     * @return The object to test.
     */
    T object();

    /**
     * Returns a stream of objects that are equal to the object to test. This default implementation returns a stream containing only
     * {@link #object()}}.
     *
     * @return A stream of objects that are equal to the object to test
     */
    default Stream<T> equalObjects() {
        return Stream.of(object());
    }

    /**
     * Returns a stream of objects that are not equal to the object to test.
     *
     * @return A stream of objects that are not equal to the object to test.
     */
    Stream<T> unequalObjects();

    /**
     * Returns a stream of objects of a different type. These should not be equal to the object to test.
     * This default implementation returns a stream containing a string and an int.
     *
     * @return A stream of objects of a different type
     */
    @SuppressWarnings("nls")
    default Stream<?> otherTypedObjects() {
        return Stream.of("foo", 13);
    }

    /**
     * Returns a stream of objects that have a hash code different from the object to test. This default implementation returns
     * {@link #unequalObjects()}. If some fields are used for {@link Object#equals(Object)} and not for {@link Object#hashCode()}, this method should
     * most likely be overridden.
     *
     * @return A stream of objects that have a hash code different from the object to test
     */
    default Stream<T> unequalHashCodeObjects() {
        return unequalObjects();
    }

    @Test
    @DisplayName("object.equals(object)")
    @SuppressWarnings("javadoc")
    default void testReflexiveEquals() {
        T object = object();
        assertEquals(object, object);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("equalObjects")
    @DisplayName("object.equals(other)")
    @SuppressWarnings("javadoc")
    default void testEqual(Object other) {
        T object = object();
        assertEquals(object, other);
        assertEquals(other, object);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("unequalObjects")
    @DisplayName("!object.equals(other)")
    @SuppressWarnings("javadoc")
    default void testUnequal(Object other) {
        T object = object();
        assertNotEquals(object, other);
        assertNotEquals(other, object);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("otherTypedObjects")
    @DisplayName("!object.equals(other) (different type)")
    @SuppressWarnings("javadoc")
    default void testUnequalToOtherTypes(Object other) {
        T object = object();
        assertNotEquals(object, other);
        assertNotEquals(other, object);
    }

    @Test
    @DisplayName("!object.equals(null)")
    @SuppressWarnings("javadoc")
    default void testUnEqualToNull() {
        T object = object();
        assertNotEquals(null, object);
    }

    @Test
    @DisplayName("hashCode() is consistent")
    @SuppressWarnings("javadoc")
    default void testHashCodeConsistent() {
        T object = object();
        assertEquals(object.hashCode(), object.hashCode());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("equalObjects")
    @DisplayName("object.hashCode() == other.hashCode()")
    @SuppressWarnings("javadoc")
    default void testHashCodeOfEqualObject(Object other) {
        T object = object();
        assertEquals(other.hashCode(), object.hashCode());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("unequalHashCodeObjects")
    @DisplayName("object.hashCode() != other.hashCode()")
    @SuppressWarnings("javadoc")
    default void testHashCodeOfUnequalObject(Object other) {
        T object = object();
        assertNotEquals(other.hashCode(), object.hashCode());
    }
}
