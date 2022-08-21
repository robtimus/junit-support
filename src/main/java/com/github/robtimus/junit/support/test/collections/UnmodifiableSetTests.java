/*
 * UnmodifiableSetTests.java
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

import java.util.Set;

/**
 * Base interface for testing separate {@link Set} functionalities for unmodifiable sets.
 *
 * @author Rob Spoor
 * @param <T> The element type of the set to test.
 */
public interface UnmodifiableSetTests<T> extends SetTests<T>, UnmodifiableCollectionTests<T> {
    // no additional methods or classes needed
}
