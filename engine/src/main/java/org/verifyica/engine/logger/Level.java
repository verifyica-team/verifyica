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

package org.verifyica.engine.logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * An enumeration representing logging levels in the Verifyica logging framework.
 *
 * <p>Logging levels are ordered from most severe to least severe:
 * ERROR &gt; WARN &gt; INFO &gt; DEBUG &gt; TRACE. Each level has an associated
 * integer value used for comparisons.
 *
 * <p>The Level class provides methods to convert between string representations
 * and Level instances, as well as integer values for efficient level checking.
 */
public final class Level {

    /**
     * ERROR level - indicates a serious problem that has caused a failure.
     */
    public static final Level ERROR = new Level(100, "ERROR");

    /**
     * WARN level - indicates a potentially harmful situation.
     */
    public static final Level WARN = new Level(200, "WARN");

    /**
     * INFO level - indicates informational messages about the progress of the application.
     */
    public static final Level INFO = new Level(300, "INFO");

    /**
     * DEBUG level - indicates fine-grained informational events useful for debugging.
     */
    public static final Level DEBUG = new Level(400, "DEBUG");

    /**
     * TRACE level - indicates even more detailed informational events than DEBUG.
     */
    public static final Level TRACE = new Level(500, "TRACE");

    /**
     * ALL level - indicates that all messages should be logged.
     */
    public static final Level ALL = new Level(Integer.MAX_VALUE, "ALL");

    private static final Map<String, Level> LEVEL_MAP = new HashMap<>();

    static {
        LEVEL_MAP.put(ERROR.toString(), ERROR);
        LEVEL_MAP.put(WARN.toString(), WARN);
        LEVEL_MAP.put(INFO.toString(), INFO);
        LEVEL_MAP.put(DEBUG.toString(), DEBUG);
        LEVEL_MAP.put(TRACE.toString(), TRACE);
        LEVEL_MAP.put(ALL.toString(), ALL);
    }

    private final int level;
    private final String string;

    /**
     * Private constructor to create a Level instance.
     *
     * @param level the integer value of the level
     * @param string the string representation of the level
     */
    private Level(int level, String string) {
        this.level = level;
        this.string = string;
    }

    /**
     * Returns the integer value of this level.
     *
     * @return the integer value of this level
     */
    public int toInt() {
        return level;
    }

    /**
     * Returns the string representation of this level.
     *
     * @return the string representation of this level
     */
    @Override
    public String toString() {
        return string;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Level level1 = (Level) o;
        return level == level1.level && Objects.equals(string, level1.string);
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, string);
    }

    /**
     * Decodes a string into a Level.
     *
     * @param string the string to decode (case-insensitive)
     * @return the corresponding Level, or INFO if the string is not recognized
     */
    public static Level decode(String string) {
        Level level = null;

        if (string != null && !string.trim().isEmpty()) {
            level = LEVEL_MAP.get(string.trim());
        }

        return level != null ? level : INFO;
    }
}
