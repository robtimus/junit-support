/*
 * UnmodifiableIteratorTests.java
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

package com.github.robtimus.junit.support.collections;

import static com.github.robtimus.junit.support.collections.CollectionAssertions.assertHasElements;
import static com.github.robtimus.junit.support.collections.CollectionUtils.toList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Base interface for testing separate {@link Iterator} functionalities for unmodifiable iterators.
 *
 * @author Rob Spoor
 * @param <T> The element type of the iterator to test.
 */
public interface UnmodifiableIteratorTests<T> extends IteratorTests<T> {

    /**
     * Contains tests for {@link Iterator#remove()} for unmodifiable iterators.
     *
     * @author Rob Spoor
     * @param <T> The element type of the iterator to test.
     */
    @DisplayName("remove()")
    interface RemoveTests<T> extends UnmodifiableIteratorTests<T> {

        @Test
        @DisplayName("remove() throws UnsupportedOperationException")
        default void testRemove() {
            Iterable<T> iterable = iterable();
            Iterator<T> iterator = iterable.iterator();

            while (iterator.hasNext()) {
                iterator.next();

                assertThrows(UnsupportedOperationException.class, iterator::remove);
            }

            List<T> remaining = toList(iterable);
            assertHasElements(remaining, expectedElements(), fixedOrder());
        }
    }
}
