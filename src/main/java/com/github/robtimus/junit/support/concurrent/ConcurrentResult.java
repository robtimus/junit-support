/*
 * ConcurrentResult.java
 * Copyright 2024 Rob Spoor
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

package com.github.robtimus.junit.support.concurrent;

import java.util.function.BiFunction;

final class ConcurrentResult<T> {

    private final T result;
    private final Throwable throwable;

    ConcurrentResult(T result) {
        this.result = result;
        this.throwable = null;
    }

    ConcurrentResult(Throwable throwable) {
        this.result = null;
        this.throwable = throwable;
    }

    T getOrThrow() {
        if (throwable instanceof Error) {
            throw (Error) throwable;
        }
        if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        }
        if (throwable != null) {
            throw new ConcurrentException(throwable);
        }
        return result;
    }

    <R> R handle(BiFunction<? super T, ? super Throwable, ? extends R> handler) {
        return handler.apply(result, throwable);
    }
}
