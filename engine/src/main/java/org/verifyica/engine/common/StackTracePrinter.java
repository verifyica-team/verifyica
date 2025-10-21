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

import static java.util.Optional.ofNullable;

import java.io.PrintStream;
import java.util.Arrays;
import org.verifyica.engine.configuration.ConcreteConfiguration;
import org.verifyica.engine.configuration.Constants;

/**
 * Class to implement StackTracePrinter
 */
public class StackTracePrinter {

    private static boolean pruneStackTraces = true;

    static {
        ofNullable(ConcreteConfiguration.getInstance().getProperties().getProperty(Constants.ENGINE_PRUNE_STACK_TRACE))
                .ifPresent(value -> {
                    if (Constants.FALSE.equals(value)) {
                        pruneStackTraces = false;
                    }
                });
    }

    /**
     * Constructor
     */
    private StackTracePrinter() {
        // INTENTIONALLY EMPTY
    }

    /**
     * Method to print the stacktrace using a specific AnsiColor
     *
     * @param throwable throwable
     * @param ansiColor ansiColor
     * @param printStream printStream
     */
    public static void printStackTrace(Throwable throwable, AnsiColor ansiColor, PrintStream printStream) {
        if (pruneStackTraces) {
            pruneStackTrace(throwable);
        }

        synchronized (printStream) {
            printStream.print(ansiColor);
            throwable.printStackTrace(printStream);
            printStream.print(AnsiColor.NONE);
        }
    }

    /**
     * Method to prune a StackTrace
     *
     * @param throwable throwable
     */
    private static void pruneStackTrace(Throwable throwable) {
        Throwable cause = throwable;
        while (cause != null) {
            trimStackTrace(cause);
            cause = cause.getCause();
        }
    }

    /**
     * Method to trim a StackTrace
     * @param throwable throwable
     */
    private static void trimStackTrace(Throwable throwable) {
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        throwable.setStackTrace(Arrays.copyOf(stackTrace, length(stackTrace)));
    }

    /**
     * Method to determine StackTrace elements to copy
     *
     * @param stackTrace stackTrace
     * @return the number of StackTrace elements to copy
     */
    private static int length(StackTraceElement[] stackTrace) {
        for (int i = 0; i < stackTrace.length; i++) {
            StackTraceElement element = stackTrace[i];
            if (element.getClassName().startsWith("org.verifyica.engine.")) {
                return i;
            }
        }
        return stackTrace.length;
    }
}
