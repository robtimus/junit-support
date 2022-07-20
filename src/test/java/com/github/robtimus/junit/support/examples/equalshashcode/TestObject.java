/*
 * TestObject.java
 * Copyright 2022 Rob Spoor
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

package com.github.robtimus.junit.support.examples.equalshashcode;

import java.util.Objects;

class TestObject {

    private final int i;
    private final String s;
    private final boolean b;

    TestObject(int i, String s, boolean b) {
        this.i = i;
        this.s = s;
        this.b = b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != getClass()) {
            return false;
        }
        TestObject other = (TestObject) o;
        return i == other.i
                && Objects.equals(s, other.s)
                && b == other.b;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + i;
        result = prime * result + Objects.hashCode(s);
        result = prime * result + Boolean.hashCode(b);
        return result;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return getClass().getSimpleName() + ": i=" + i + ", s=" + s + ", b=" + b;
    }
}
