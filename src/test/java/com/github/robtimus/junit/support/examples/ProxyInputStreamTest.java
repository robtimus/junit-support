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

package com.github.robtimus.junit.support.examples;

import static com.github.robtimus.junit.support.DelegateTests.intParameter;
import static com.github.robtimus.junit.support.DelegateTests.method;
import static com.github.robtimus.junit.support.DelegateTests.parameter;
import java.io.InputStream;
import org.apache.commons.io.input.ProxyInputStream;
import com.github.robtimus.junit.support.DelegateTests;

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
    public MethodFinder methods() {
        // methodsDeclaredByType will only work with Java 8; Java 9 and beyond add additional methods not yet found in ProxyInputStream
        // Besides, this shows how to use method parameters
        return method("read")
                .and(method("read", parameter(new byte[3])))
                .and(method("read", parameter(new byte[3]), intParameter(0), intParameter(3)))
                .and(method("skip", long.class))
                .and(method("available"))
                .and(method("close"))
                .and(method("mark", int.class))
                .and(method("reset"))
                .and(method("markSupported"));
    }
}
