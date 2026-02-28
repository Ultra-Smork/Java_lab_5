package com.utils;

import java.io.InputStream;
import java.util.List;

/**
 * Custom InputStream implementation that reads from a list of strings.
 * This class is used to simulate user input during script execution,
 * allowing commands in a script file to receive input as if from the console.
 */
public class ScriptInputStream extends InputStream {
    /** The list of input lines to read from */
    private final List<String> lines;
    /** The index of the current line being processed */
    private int currentLineIndex = 0;
    /** The current line being read */
    private String currentLine = "";
    /** The index of the current character within the current line */
    private int currentCharIndex = 0;

    /**
     * Constructs a new ScriptInputStream with the provided list of input lines.
     *
     * @param lines the list of strings to use as input
     */
    public ScriptInputStream(List<String> lines) {
        this.lines = lines;
        if (!lines.isEmpty()) {
            currentLine = lines.get(0);
        }
    }

    /**
     * Reads a single character from the input stream.
     * Returns the next character from the current line, or a newline character
     * when transitioning between lines, or -1 when all lines have been read.
     *
     * @return the next character as an integer (0-255), or -1 if end of stream is reached
     */
    @Override
    public int read() {
        if (currentLineIndex >= lines.size()) {
            return -1;
        }

        if (currentCharIndex < currentLine.length()) {
            return currentLine.charAt(currentCharIndex++);
        }

        if (currentLineIndex < lines.size() - 1) {
            currentLineIndex++;
            currentCharIndex = 0;
            currentLine = lines.get(currentLineIndex);
            return '\n';
        }

        currentLineIndex++;
        return -1;
    }

    /**
     * Returns an estimate of the number of bytes that can be read
     * from the input stream without blocking.
     *
     * @return the estimated number of remaining characters
     */
    @Override
    public int available() {
        if (currentLineIndex >= lines.size()) {
            return 0;
        }
        int available = 0;
        for (int i = currentLineIndex; i < lines.size(); i++) {
            available += lines.get(i).length();
            available++;
        }
        return available;
    }
}
