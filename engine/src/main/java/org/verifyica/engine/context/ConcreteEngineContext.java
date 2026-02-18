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

package org.verifyica.engine.context;

import java.util.Objects;
import org.verifyica.api.Configuration;
import org.verifyica.api.EngineContext;
import org.verifyica.engine.common.Precondition;
import org.verifyica.engine.configuration.ImmutableConfiguration;

/**
 * Class to implement ConcreteEngineContext
 */
public class ConcreteEngineContext extends AbstractContext implements EngineContext {

    private final Configuration configuration;
    private final String version;

    /**
     * Constructor
     *
     * @param configuration configuration
     * @param version version
     */
    public ConcreteEngineContext(Configuration configuration, String version) {
        Precondition.notNull(configuration, "configuration is null");
        Precondition.notNull(version, "version is null");

        this.configuration = new ImmutableConfiguration(configuration);
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public String toString() {
        return "ConcreteEngineContext{" + "version='" + version + '\'' + '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        ConcreteEngineContext that = (ConcreteEngineContext) object;
        return Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), version);
    }
}
