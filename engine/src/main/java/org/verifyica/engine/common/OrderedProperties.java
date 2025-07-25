/*
 * Copyright (C) Verifyica project authors and contributors
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

/**
 * Class to implement OrderedProperties
 */
public class OrderedProperties extends Properties {

    /**
     * Ordered Map
     */
    private final TreeMap<Object, Object> orderedMap;

    /**
     * Constructor
     */
    public OrderedProperties() {
        this.orderedMap = new TreeMap<>();
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
    public synchronized void load(InputStream inputStream) throws IOException {
        super.load(inputStream);
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
        // TODO store in order
        super.store(out, comments);
    }

    @Override
    public synchronized void store(Writer writer, String comments) throws IOException {
        // TODO store in order
        super.store(writer, comments);
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
