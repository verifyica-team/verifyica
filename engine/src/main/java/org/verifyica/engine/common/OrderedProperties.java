/*
 * Copyright (C) 2024-present Verifyica project authors and contributors
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

package org.verifyica.engine.common;

import java.io.*;
import java.util.*;
import java.util.Properties;

/** Class to implement OrderedProperties */
public class OrderedProperties extends Properties {

    /** Ordered Map */
    private final LinkedHashMap<Object, Object> orderedMap;

    /** Constructor */
    public OrderedProperties() {
        this.orderedMap = new LinkedHashMap<>();
    }

    @Override
    public synchronized Object put(Object key, Object value) {
        orderedMap.put(key, value);
        return super.put(key, value);
    }

    @Override
    public synchronized Object remove(Object key) {
        orderedMap.remove(key);
        return super.remove(key);
    }

    @Override
    public synchronized void clear() {
        orderedMap.clear();
        super.clear();
    }

    @Override
    public synchronized Object get(Object key) {
        return orderedMap.get(key);
    }

    @Override
    public synchronized Set<Map.Entry<Object, Object>> entrySet() {
        return new LinkedHashSet<>(orderedMap.entrySet());
    }

    @Override
    public synchronized Enumeration<Object> keys() {
        return Collections.enumeration(orderedMap.keySet());
    }

    @Override
    public synchronized void load(InputStream inStream) throws IOException {
        super.load(inStream);
        orderedMap.clear();
        orderedMap.putAll(this);
    }

    @Override
    public synchronized void load(Reader reader) throws IOException {
        super.load(reader);
        orderedMap.clear();
        orderedMap.putAll(this);
    }

    @Override
    public synchronized void store(OutputStream out, String comments) throws IOException {
        Properties tempProperties = new Properties() {
            @Override
            public Set<Map.Entry<Object, Object>> entrySet() {
                return orderedMap.entrySet();
            }
        };
        tempProperties.store(out, comments);
    }

    @Override
    public synchronized void store(Writer writer, String comments) throws IOException {
        Properties tempProperties = new Properties() {
            @Override
            public Set<Map.Entry<Object, Object>> entrySet() {
                return orderedMap.entrySet();
            }
        };
        tempProperties.store(writer, comments);
    }

    @Override
    public synchronized Enumeration<Object> elements() {
        return Collections.enumeration(orderedMap.values());
    }

    @Override
    public synchronized Set<Object> keySet() {
        return orderedMap.keySet();
    }

    @Override
    public synchronized Collection<Object> values() {
        return orderedMap.values();
    }
}
