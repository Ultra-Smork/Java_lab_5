package com.utils;

import java.io.InputStream;
import java.util.List;

public class ScriptInputStream extends InputStream {
    private final List<String> lines;
    private int currentLineIndex = 0;
    private String currentLine = "";
    private int currentCharIndex = 0;

    public ScriptInputStream(List<String> lines) {
        this.lines = lines;
        if (!lines.isEmpty()) {
            currentLine = lines.get(0);
        }
    }

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
