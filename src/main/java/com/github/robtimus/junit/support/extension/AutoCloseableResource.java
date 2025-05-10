/*
 * AutoCloseableResource.java
 * Copyright 2025 Rob Spoor
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

package com.github.robtimus.junit.support.extension;

import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;

/**
 * An interface that extends both {@link AutoCloseable} and {@link CloseableResource}, allowing implementations to be used with
 * {@link Store} with JUnit 5.13 but also before.
 *
 * @author Rob Spoor
 * @since 3.1
 */
@FunctionalInterface
public interface AutoCloseableResource extends AutoCloseable, CloseableResource {

    // AutoCloseable defines void close() throws Exception
    // CloseableResource defines void close() throws Throwable
    // this interface therefore defines one method: void close() throws Exception
}
