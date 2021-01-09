/*
 * ProxyInputStreamTest.java
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

import java.io.InputStream;
import java.util.stream.Stream;
import org.apache.commons.io.input.ProxyInputStream;
import com.github.robtimus.unittestsupport.DelegateTests;

class ProxyInputStreamTest implements DelegateTests<InputStream> {

    @Override
    public Class<InputStream> delegateType() {
        return InputStream.class;
    }

    @Override
    public InputStream wrap(InputStream delegate) {
        return new ProxyInputStream(delegate) {
            // no new content
        };
    }

    @Override
    @SuppressWarnings("nls")
    public Stream<DelegateMethod<InputStream>> methods() {
        return Stream.of(
                method("read"),
                method("read", parameter(new byte[3])),
                method("read", parameter(new byte[3]), parameter(0), parameter(3)),
                method("skip", long.class),
                method("available"),
                method("close"),
                method("mark", int.class),
                method("reset"),
                method("markSupported"));
    }
}
