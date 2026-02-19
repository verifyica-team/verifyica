/*
 * Copyright (C) Verifyica project authors and contributors. All rights reserved.
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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Class to implement ImmutableProperties
 */
public class ImmutableProperties extends Properties {

    private static final long serialVersionUID = 1L;

    /**
     * Unmodifiable entry set
     */
    private Set<Map.Entry<Object, Object>> unmodifiableEntrySet;

    /**
     * Constructor
     *
     * @param properties properties
     */
    public ImmutableProperties(Properties properties) {
        super();
        // Copy all properties from source using the parent class method directly
        // This must be done during construction before the object is fully initialized
        if (properties != null) {
            super.putAll(properties);
        }
    }

    @Override
    public synchronized Object put(Object key, Object value) {
        throw new UnsupportedOperationException("Cannot modify an immutable properties instance.");
    }

    @Override
    public synchronized Object remove(Object key) {
        throw new UnsupportedOperationException("Cannot modify an immutable properties instance.");
    }

    @Override
    public synchronized void clear() {
        throw new UnsupportedOperationException("Cannot modify an immutable properties instance.");
    }

    @Override
    public synchronized void putAll(Map<?, ?> t) {
        throw new UnsupportedOperationException("Cannot modify an immutable properties instance.");
    }

    @Override
    public synchronized void load(Reader reader) {
        throw new UnsupportedOperationException("Cannot modify an immutable properties instance.");
    }

    @Override
    public synchronized void load(InputStream inStream) {
        throw new UnsupportedOperationException("Cannot modify an immutable properties instance.");
    }

    @Override
    public synchronized void store(OutputStream out, String comments) {
        throw new UnsupportedOperationException("Cannot modify an immutable properties instance.");
    }

    @Override
    public synchronized void store(Writer writer, String comments) {
        throw new UnsupportedOperationException("Cannot modify an immutable properties instance.");
    }

    @Override
    public synchronized Set<String> stringPropertyNames() {
        return Collections.unmodifiableSet(super.stringPropertyNames());
    }

    @Override
    public synchronized Enumeration<?> propertyNames() {
        return Collections.enumeration(Collections.list(super.propertyNames()));
    }

    @Override
    public synchronized Set<Map.Entry<Object, Object>> entrySet() {
        if (unmodifiableEntrySet == null) {
            unmodifiableEntrySet = Collections.unmodifiableSet(super.entrySet());
        }
        return unmodifiableEntrySet;
    }

    @Override
    public synchronized Enumeration<Object> elements() {
        return Collections.enumeration(Collections.list(super.elements()));
    }
}
