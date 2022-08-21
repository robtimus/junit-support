/*
 * CollectionFactory.java
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

package com.github.robtimus.junit.support.examples.collections;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

final class CollectionFactory {

    private CollectionFactory() {
        throw new IllegalStateException("cannot create instances of " + getClass().getName()); //$NON-NLS-1$
    }

    static <C extends Collection<String>> C createCollection(Supplier<C> constructor, int from, int to) {
        C collection = constructor.get();
        for (int i = from; i < to; i++) {
            collection.add("string" + i); //$NON-NLS-1$
        }
        return collection;
    }

    static <M extends Map<Integer, String>> M createMap(Supplier<M> constructor, int from, int to) {
        M map = constructor.get();
        for (int i = from; i < to; i++) {
            map.put(i, "string" + i); //$NON-NLS-1$
        }
        return map;
    }
}
