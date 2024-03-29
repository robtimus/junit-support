/*
 * ReaderDelegateTests.java
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

package com.github.robtimus.junit.support.test.io;

import static com.github.robtimus.junit.support.ThrowableAssertions.assertDoesNotThrowCheckedException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.io.Reader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Base interface for testing that {@link Reader} implementations correctly delegate to another {@link Reader}.
 *
 * @author Rob Spoor
 */
public interface ReaderDelegateTests {

    /**
     * Creates the reader to test.
     * <p>
     * This method will be called only once for each test. This makes it possible to capture the reader to test and its delegate, and perform
     * additional tests after the pre-defined test has finished.
     *
     * @param delegate The delegate to test against.
     * @return The created reader.
     */
    Reader wrapReader(Reader delegate);

    /**
     * Contains tests for {@link Reader#close()}.
     *
     * @author Rob Spoor
     */
    @DisplayName("close()")
    interface CloseTests extends ReaderDelegateTests {

        @Test
        @DisplayName("close() delegates")
        default void testCloseDelegates() {
            assertDoesNotThrowCheckedException(() -> {
                @SuppressWarnings("resource")
                Reader delegate = mock(Reader.class);
                try (Reader reader = wrapReader(delegate)) {
                    // no code necessary
                }
                verify(delegate).close();
            });
        }
    }
}
