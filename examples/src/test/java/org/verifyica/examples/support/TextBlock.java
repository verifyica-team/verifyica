package org.verifyica.examples.support;

import java.util.ArrayList;
import java.util.List;

public class TextBlock {

    private final List<String> lines;

    public TextBlock() {
        this.lines = new ArrayList<>();
    }

    public TextBlock line(String line) {
        this.lines.add(line);
        return this;
    }

    public TextBlock line() {
        return line("");
    }

    public String toString() {
        return String.join(System.lineSeparator(), lines);
    }
}
