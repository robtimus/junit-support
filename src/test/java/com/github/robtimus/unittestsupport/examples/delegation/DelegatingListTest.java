/*
 * DelegatingListTest.java
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

package com.github.robtimus.unittestsupport.examples.delegation;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import com.github.robtimus.unittestsupport.DelegateTests;

class DelegatingListTest implements DelegateTests<List<String>> {

    @Override
    @SuppressWarnings("unchecked")
    public Class<List<String>> delegateType() {
        return (Class<List<String>>) (Class<?>) List.class;
    }

    @Override
    public List<String> wrap(List<String> delegate) {
        return new DelegatingList(delegate);
    }

    @Override
    @SuppressWarnings("nls")
    public Stream<DelegateMethod<List<String>>> methods() {
        return Stream.of(
                method("size"),
                method("isEmpty"),
                method("contains", Object.class),
                method("iterator"),
                method("toArray"),
                method("toArray", parameter(Object[].class, new String[0])),
                method("add", Object.class),
                method("remove", Object.class),
                method("containsAll", parameter(Collection.class, Collections.emptyList())),
                method("addAll", parameter(Collection.class, Collections.emptyList())),
                method("addAll", parameter(0), parameter(Collection.class, Collections.emptyList())),
                method("removeAll", parameter(Collection.class, Collections.emptyList())),
                method("retainAll", parameter(Collection.class, Collections.emptyList())),
                method("replaceAll", parameter(UnaryOperator.class, t -> t.toString() + t)),
                method("removeIf", parameter(Predicate.class, t -> true)),
                method("sort", Comparator.class),
                method("clear"),
                // equals and hashCode cannot be stubbed / verified
                method("get", int.class),
                method("set", int.class, Object.class),
                method("add", int.class, Object.class),
                method("remove", int.class),
                method("indexOf", Object.class),
                method("lastIndexOf", Object.class),
                method("listIterator"),
                method("listIterator", int.class),
                method("subList", int.class, int.class),
                method("forEach", parameter(Consumer.class, t -> { /* do nothing */ })),
                method("spliterator"),
                method("stream"),
                method("parallelStream"));
    }
}
