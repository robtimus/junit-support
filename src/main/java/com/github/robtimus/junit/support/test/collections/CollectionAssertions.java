/*
 * CollectionAssertions.java
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

package com.github.robtimus.junit.support.test.collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyArray;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * A collection of utility methods that support asserting conditions for collections and related types.
 *
 * @author Rob Spoor
 */
final class CollectionAssertions {

    private CollectionAssertions() {
    }

    static void assertHasElements(Collection<?> collection, Collection<?> expected, boolean fixedOrder) {
        if (expected.isEmpty()) {
            assertThat(collection, empty());
        } else if (fixedOrder) {
            assertEquals(asList(expected), asList(collection));
        } else if (collection instanceof Set<?> && expected instanceof Set<?>) {
            assertEquals(expected, collection);
        } else {
            assertThat(collection, containsInAnyOrder(expected.toArray()));
        }
    }

    private static List<?> asList(Collection<?> collection) {
        return collection instanceof List<?> ? (List<?>) collection : new ArrayList<>(collection);
    }

    static void assertHasElements(Object[] array, Collection<?> expected, boolean fixedOrder) {
        if (expected.isEmpty()) {
            assertThat(array, emptyArray());
        } else if (fixedOrder) {
            assertArrayEquals(expected.toArray(), array);
        } else {
            assertThat(array, arrayContainingInAnyOrder(expected.toArray()));
        }
    }
}
