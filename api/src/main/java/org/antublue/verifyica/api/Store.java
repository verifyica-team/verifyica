/*
 * Copyright (C) 2024 The Verifyica project authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.antublue.verifyica.api;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;

public interface Store {

    <T> T put(Object key, Object value);

    <T> T computeIfAbsent(Object key, Function<Object, Object> function);

    Store merge(Store store);

    Store merge(Map<Object, Object> map);

    <T> T get(Object key);

    <T> T get(Object key, Class<T> type);

    boolean containsKey(Object key);

    <T> T remove(Object key);

    <T> T remove(Object key, Class<T> type);

    void clear();

    int size();

    boolean isEmpty();

    Set<Object> keySet();

    Store duplicate();

    ReadWriteLock getLock();
}
