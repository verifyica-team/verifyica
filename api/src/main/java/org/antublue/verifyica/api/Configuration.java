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

import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;

public interface Configuration {

    String put(String key, String value);

    String get(String key);

    String getOrDefault(String key, String defaultValue);

    String computeIfAbsent(String key, Function<String, String> function);

    boolean containsKey(String key);

    String remove(String key);

    int size();

    boolean isEmpty();

    void clear();

    /**
     * Returns a copy of the keySet
     *
     * @return a copy of the keySet
     */
    Set<String> keySet();

    ReadWriteLock getLock();
}
