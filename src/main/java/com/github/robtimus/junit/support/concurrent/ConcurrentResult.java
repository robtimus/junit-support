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

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

@SuppressWarnings("nls")
final class ConcurrentResult<T> {

    private final T result;
    private final Throwable failure;

    ConcurrentResult(T result) {
        this.result = result;
        this.failure = null;
    }

    ConcurrentResult(Throwable failure) {
        this.result = null;
        this.failure = failure;
    }

    T getOrThrow() {
        if (failure != null) {
            throwUnchecked(failure);
        }
        return result;
    }

    Throwable failure() {
        return failure;
    }

    static void throwUnchecked(Throwable failure) {
        if (failure instanceof Error) {
            throw (Error) failure;
        }
        if (failure instanceof RuntimeException) {
            throw (RuntimeException) failure;
        }
        throw assertionFailure()
                .reason("Unexpected exception thrown: " + failure)
                .cause(failure)
                .build();
    }
}
