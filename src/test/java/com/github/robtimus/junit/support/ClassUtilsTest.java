/*
 * ClassUtilsTest.java
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

import static com.github.robtimus.junit.support.ClassUtils.findClassesInPackage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.input.BrokenInputStream;
import org.apache.commons.io.output.BrokenOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtendWith;
import com.github.robtimus.junit.support.collections.CollectionTests;
import com.github.robtimus.junit.support.collections.annotation.StoreNullKeyNotSupported;
import com.github.robtimus.junit.support.io.IOAssertions;

@SuppressWarnings("nls")
class ClassUtilsTest {

    @Nested
    @DisplayName("findClassesInPackage")
    class FindClassesInPackage {

        // Just a few classes for the current package, JUnit API and Commons IO

        private final Class<?>[] packageClasses = {
                AdditionalAssertions.class,
                ClassUtils.class,
                getClass(),
                TraitTest.class,
                CollectionTests.class,
                CollectionTests.ClearTests.class,
                StoreNullKeyNotSupported.class,
                IOAssertions.class,
        };

        private final Class<?>[] junitApiClasses = {
                Assertions.class,
                DisplayName.class,
                Test.class,
                EnabledIf.class,
                ExtendWith.class,
        };

        private final Class<?>[] commonsIOClasses = {
                Charsets.class,
                BrokenInputStream.class,
                BrokenOutputStream.class,
        };

        @Test
        @DisplayName("current package")
        void testCurrentPackage() {
            List<Class<?>> classes = findClassesInPackage(getClass()).collect(Collectors.toList());
            assertThat(classes, hasItems(packageClasses));
            assertThat(classes, not(hasItems(junitApiClasses)));
            assertThat(classes, not(hasItems(commonsIOClasses)));

            // Retrieve all class names from classes and test-classes and verify that classes contains all of these
            List<String> classNamesOnDisk = collectClassNames();
            List<String> classNames = classes.stream()
                    .map(Class::getName)
                    .sorted()
                    .collect(Collectors.toList());
            assertEquals(classNamesOnDisk, classNames);
        }

        private List<String> collectClassNames() {
            Path projectBase = Paths.get(".");
            List<String> classFiles = collectClassNames(projectBase.resolve("target/classes"));
            List<String> testClassFiles = collectClassNames(projectBase.resolve("target/test-classes"));
            List<String> classNamesOnDisk = new ArrayList<>();
            classNamesOnDisk.addAll(classFiles);
            classNamesOnDisk.addAll(testClassFiles);
            classNamesOnDisk.sort(null);
            return classNamesOnDisk;
        }

        private List<String> collectClassNames(Path folder) {
            try (Stream<Path> stream = Files.walk(folder)) {
                return stream
                        .map(p -> folder.relativize(p))
                        .map(p -> p.toString())
                        .filter(s -> s.endsWith(".class") && !s.endsWith("package-info.class"))
                        .map(s -> s.replaceFirst("\\.class$", ""))
                        .map(s -> s.replace(File.separatorChar, '.'))
                        .collect(Collectors.toList());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Test
        @DisplayName("JUnit API")
        void testJUnitAPI() {
            Set<Class<?>> classes = findClassesInPackage(Test.class).collect(Collectors.toSet());
            assertThat(classes, hasItems(junitApiClasses));
            assertThat(classes, not(hasItems(packageClasses)));
            assertThat(classes, not(hasItems(commonsIOClasses)));
        }

        @Test
        @DisplayName("Commons IO")
        void testCommonsIO() {
            Set<Class<?>> classes = findClassesInPackage(Charsets.class).collect(Collectors.toSet());
            assertThat(classes, hasItems(commonsIOClasses));
            assertThat(classes, not(hasItems(packageClasses)));
            assertThat(classes, not(hasItems(junitApiClasses)));
        }
    }
}
