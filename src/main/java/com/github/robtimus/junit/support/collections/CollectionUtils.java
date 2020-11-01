/*
 * CollectionUtils.java
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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

final class CollectionUtils {

    private CollectionUtils() {
        throw new IllegalStateException("cannot create instances of " + getClass().getName()); //$NON-NLS-1$
    }

    static <T> List<T> toList(Iterable<T> iterable) {
        return toList(iterable.iterator());
    }

    static <T> List<T> toList(Iterator<T> iterator) {
        List<T> result = new ArrayList<>();
        while (iterator.hasNext()) {
            T element = iterator.next();
            result.add(element);
        }
        return result;
    }

    static Class<?> commonType(Collection<?> collection) {
        if (collection.isEmpty()) {
            // doesn't really matter, use String
            return String.class;
        }
        Iterator<?> iterator = collection.iterator();
        Class<?> common = iterator.next().getClass();
        while (common != Object.class && iterator.hasNext()) {
            common = commonType(common, iterator.next().getClass());
        }
        if (common != Object.class && Modifier.isPublic(common.getModifiers())) {
            return common;
        }

        iterator = collection.iterator();
        Set<Class<?>> commonInterfaces = collectAllInterfaces(iterator.next().getClass());
        while (!commonInterfaces.isEmpty() && iterator.hasNext()) {
            commonInterfaces.retainAll(collectAllInterfaces(iterator.next().getClass()));
        }

        Set<Class<?>> publicCommonInterfaces = new HashSet<>(commonInterfaces);
        publicCommonInterfaces.removeIf(i -> !Modifier.isPublic(i.getModifiers()));
        if (!publicCommonInterfaces.isEmpty()) {
            return publicCommonInterfaces.iterator().next();
        }

        return commonInterfaces.isEmpty() ? common : commonInterfaces.iterator().next();
    }

    private static Class<?> commonType(Class<?> c1, Class<?> c2) {
        if (c1.isAssignableFrom(c2)) {
            return c1;
        }
        if (c2.isAssignableFrom(c1)) {
            return c2;
        }
        // if c1 or c2 is Object.class, then one of the above if statements would have been true
        return commonType(c1.getSuperclass(), c2.getSuperclass());
    }

    private static Set<Class<?>> collectAllInterfaces(Class<?> c) {
        Set<Class<?>> allInterfaces = new HashSet<>();
        while (c != null) {
            Collections.addAll(allInterfaces, c.getInterfaces());
            c = c.getSuperclass();
        }
        return allInterfaces;
    }
}
