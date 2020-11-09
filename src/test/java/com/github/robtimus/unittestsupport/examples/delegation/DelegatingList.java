/*
 * DelegatingList.java
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

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

final class DelegatingList implements List<String> {

    private final List<String> delegate;

    DelegatingList(List<String> delegate) {
        this.delegate = delegate;
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @Override
    public Iterator<String> iterator() {
        return delegate.iterator();
    }

    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return delegate.toArray(a);
    }

    @Override
    public boolean add(String e) {
        return delegate.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends String> c) {
        return delegate.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends String> c) {
        return delegate.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return delegate.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return delegate.retainAll(c);
    }

    @Override
    public void replaceAll(UnaryOperator<String> operator) {
        delegate.replaceAll(operator);
    }

    @Override
    public boolean removeIf(Predicate<? super String> filter) {
        return delegate.removeIf(filter);
    }

    @Override
    public void sort(Comparator<? super String> c) {
        delegate.sort(c);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String get(int index) {
        return delegate.get(index);
    }

    @Override
    public String set(int index, String element) {
        return delegate.set(index, element);
    }

    @Override
    public void add(int index, String element) {
        delegate.add(index, element);
    }

    @Override
    public String remove(int index) {
        return delegate.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return delegate.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return delegate.lastIndexOf(o);
    }

    @Override
    public ListIterator<String> listIterator() {
        return delegate.listIterator();
    }

    @Override
    public ListIterator<String> listIterator(int index) {
        return delegate.listIterator(index);
    }

    @Override
    public List<String> subList(int fromIndex, int toIndex) {
        return delegate.subList(fromIndex, toIndex);
    }

    @Override
    public void forEach(Consumer<? super String> action) {
        delegate.forEach(action);
    }

    @Override
    public Spliterator<String> spliterator() {
        return delegate.spliterator();
    }

    @Override
    public Stream<String> stream() {
        return delegate.stream();
    }

    @Override
    public Stream<String> parallelStream() {
        return delegate.parallelStream();
    }
}
