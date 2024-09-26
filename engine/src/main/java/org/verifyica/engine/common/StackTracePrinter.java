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

package org.verifyica.engine.common;

import java.io.PrintStream;

/** Class to implement StackTracePrinter */
public class StackTracePrinter {

    /** Constructor */
    private StackTracePrinter() {
        // INTENTIONALLY BLANK
    }

    /**
     * Method to print the stacktrace using a specific AnsiColor
     *
     * @param throwable throwable
     * @param ansiColor ansiColor
     * @param printStream printStream
     */
    public static void printStackTrace(Throwable throwable, AnsiColor ansiColor, PrintStream printStream) {
        synchronized (printStream) {
            printStream.print(ansiColor);
            throwable.printStackTrace(printStream);
            printStream.print(AnsiColor.NONE);
        }
    }
}
