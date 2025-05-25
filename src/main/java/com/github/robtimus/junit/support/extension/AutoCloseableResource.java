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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
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

    /**
     * A wrapper that allows any object type to be used as {@link AutoCloseable} and/or {@link CloseableResource}.
     *
     * @author Rob Spoor
     * @param <T> The type of the wrapped object.
     * @since 3.1
     */
    final class Wrapper<T> implements AutoCloseableResource {

        private final T wrapped;
        private final AutoCloseable delegate;

        private Wrapper(T wrapped, AutoCloseable delegate) {
            this.wrapped = wrapped;
            this.delegate = delegate;
        }

        /**
         * Returns the wrapped object.
         *
         * @return The wrapped object.
         */
        public T unwrap() {
            return wrapped;
        }

        @Override
        public void close() throws Exception {
            delegate.close();
        }

        /**
         * Creates a wrapper for an {@link AutoCloseable} object.
         * <p>
         * This method should not be necessary for JUnit 5.13, as from that version onward {@link AutoCloseable} can be used with {@link Store}
         * directly.
         *
         * @param <T> The object type.
         * @param closeable The object to create a wrapper for.
         * @return A wrapper for the given object.
         */
        @SuppressWarnings("resource")
        public static <T extends AutoCloseable> Wrapper<T> forAutoCloseable(T closeable) {
            Objects.requireNonNull(closeable);
            return new Wrapper<>(closeable, closeable::close);
        }

        /**
         * Creates a wrapper for an object.
         *
         * @param <T> The object type.
         * @param object The object to create a wrapper for.
         * @param onClose An action to execute when the wrapper is closed, as an {@link AutoCloseable}. It will be called at most once.
         * @return A wrapper for the given object.
         */
        @SuppressWarnings("resource")
        public static <T> Wrapper<T> forObject(T object, AutoCloseable onClose) {
            Objects.requireNonNull(object);
            Objects.requireNonNull(onClose);
            AtomicBoolean closed = new AtomicBoolean(false);
            return new Wrapper<>(object, () -> {
                if (closed.compareAndSet(false, true)) {
                    onClose.close();
                }
            });
        }
    }
}
