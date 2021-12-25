/*
 * ClassUtils.java
 * Copyright 2021 Rob Spoor
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.reflections.Reflections;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.Scanners;
import org.reflections.scanners.TypeElementsScanner;
import org.reflections.util.ConfigurationBuilder;

/**
 * A utility class for classes.
 *
 * @author Rob Spoor
 */
public final class ClassUtils {

    private ClassUtils() {
    }

    /**
     * Finds all classes in a specific package.
     * This can be useful to generate tests, e.g. using {@link DynamicContainer} or {@link DynamicTest}, for all classes in a project that match
     * certain criteria. These criteria can be applied as filters to the returned stream.
     * <p>
     * Note that classes in the JDK itself will not be found.
     *
     * @param packageName The name of the package to find all classes in.
     * @return A stream with all classes in the given package, as well as any sub packages.
     */
    public static Stream<Class<?>> findClassesInPackage(String packageName) {
        Scanner[] scanners = { new TypeElementsScanner().publicOnly(false), Scanners.SubTypes };

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackage(packageName)
                .addScanners(scanners));

        // The following intermediate variable is necessary to keep the compiler happy.
        // Without it, there's a "cannot convert from Stream<Class<capture#3-of ?>> to Stream<Class<?>>" error,
        // that is caused by the filter step
        Stream<Class<?>> classes = Arrays.stream(scanners)
                .flatMap(s -> getClassNames(reflections, s))
                .filter(s -> s.startsWith(packageName))
                .filter(s -> !s.contains("package-info")) //$NON-NLS-1$
                .distinct()
                .map(s -> toClass(s, reflections));
        return classes.filter(Objects::nonNull);
    }

    private static Stream<String> getClassNames(Reflections reflections, Scanner scanner) {
        // Taken from Reflections.getAll; prevent the unnecessary intermediate collect step
        Map<String, Set<String>> map = reflections.getStore().getOrDefault(scanner.index(), Collections.emptyMap());
        return Stream.concat(map.keySet().stream(), map.values().stream().flatMap(Collection::stream));
    }

    /**
     * Finds all classes in a specific package.
     * This can be useful to generate tests, e.g. using {@link DynamicContainer} or {@link DynamicTest}, for all classes in a project that match
     * certain criteria. These criteria can be applied as filters to the returned stream.
     * <p>
     * Note that classes in the JDK itself will not be found.
     *
     * @param classInPackage A class in the package to find all classes in. It will be returned as well.
     * @return A stream with all classes in the given package, as well as any sub packages.
     */
    public static Stream<Class<?>> findClassesInPackage(Class<?> classInPackage) {
        String packageName = classInPackage.getPackage().getName();
        return findClassesInPackage(packageName);
    }

    private static Class<?> toClass(String className, Reflections reflections) {
        return reflections.forName(className, Class.class, reflections.getConfiguration().getClassLoaders());
    }
}
