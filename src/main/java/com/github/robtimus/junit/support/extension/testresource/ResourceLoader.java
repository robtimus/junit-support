/*
 * ResourceLoader.java
 * Copyright 2026 Rob Spoor
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

package com.github.robtimus.junit.support.extension.testresource;

import java.io.InputStream;

/**
 * A loader for resources.
 * <p>
 * The Java module system does not allow access to non-exported packages. That also includes access to resources in folders that don't match exported
 * packages. The {@link TestResource} annotation will fail to load such resources. This interface makes it possible to define a custom resource loader
 * that does have access to non-exported resources. In most cases, an implementation will simply delegate to {@link Class#getResourceAsStream(String)}
 * to access resources from its own module.
 *
 * @author Rob Spoor
 * @since 3.2
 */
public interface ResourceLoader {

    /**
     * Opens a resource if possible.
     * The resource will be loaded relative to the class where the field, constructor or method is defined.
     *
     * @param c The class that acts as base path for the resource to open.
     * @param path The path to the resource, relative to the given class.
     * @return An {@link InputStream} to the resource, or {@code null} if the resource could not be found.
     */
    InputStream loadResource(Class<?> c, String path);
}
