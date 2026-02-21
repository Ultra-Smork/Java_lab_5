package com.commands;

import com.utils.Command;
import com.utils.Invoker;
import com.utils.ScriptInputStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ExecuteScriptCommand implements Command {
    private static int scriptDepth = 0;
    private static final int MAX_DEPTH = 5;

    private final String filePath;
    private Invoker invoker;

    public ExecuteScriptCommand(String filePath) {
        this.filePath = filePath;
    }

    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }

    private Invoker getInvoker() {
        if (invoker == null) {
            invoker = new Invoker();
        }
        return invoker;
    }

    @Override
    public void execute() {
        if (scriptDepth >= MAX_DEPTH) {
            System.out.println("Error: Maximum script recursion depth (" + MAX_DEPTH + ") exceeded.");
            return;
        }

        List<String> allLines = readScriptFile(filePath);
        if (allLines == null) {
            return;
        }

        scriptDepth++;
        try {
            executeScriptLines(allLines);
        } finally {
            scriptDepth--;
        }
    }

    private List<String> readScriptFile(String path) {
        String resolvedPath = resolveScriptPath(path);
        if (resolvedPath == null) {
            System.out.println("Error: Script file not found: " + path);
            return null;
        }

        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(resolvedPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (Exception e) {
            System.out.println("Error: Could not read script file: " + e.getMessage());
            return null;
        }
        return lines;
    }

    private String resolveScriptPath(String path) {
        String userDir = System.getProperty("user.dir");
        String[] possiblePaths = {
            "scripts",
            "src/main/java/com/scripts",
            userDir + "/scripts",
            userDir + "/src/main/java/com/scripts",
            "../scripts",
            "../src/main/java/com/scripts",
            "../../scripts",
            "../../src/main/java/com/scripts"
        };

        String fileName = path;
        if (path.contains("/") || path.contains("\\")) {
            fileName = path.substring(path.lastIndexOf('/') + 1);
            if (fileName.isEmpty()) {
                fileName = path.substring(path.lastIndexOf('\\') + 1);
            }
        }

        for (String basePath : possiblePaths) {
            File dir = new File(basePath);
            if (!dir.exists() || !dir.isDirectory()) {
                continue;
            }

            final String searchName = fileName;
            File[] matches = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.equalsIgnoreCase(searchName);
                }
            });

            if (matches != null && matches.length > 0) {
                return matches[0].getAbsolutePath();
            }
        }

        return null;
    }

    private void executeScriptLines(List<String> allLines) {
        List<String> nonEmptyLines = new ArrayList<>();
        for (String line : allLines) {
            if (line != null && !line.trim().isEmpty()) {
                nonEmptyLines.add(line.trim());
            }
        }

        int i = 0;
        while (i < nonEmptyLines.size()) {
            String commandLine = nonEmptyLines.get(i);
            String[] parts = commandLine.split("\\s+");
            String cmd = parts[0].toUpperCase();
            boolean hasArgument = parts.length > 1;
            String argument = hasArgument ? parts[1] : null;

            switch (cmd) {
                case "V":
                    getInvoker().ListAll();
                    i++;
                    break;
                case "A":
                    List<String> addInputs = new ArrayList<>();
                    for (int j = 0; j < 8 && (i + 1 + j) < nonEmptyLines.size(); j++) {
                        addInputs.add(nonEmptyLines.get(i + 1 + j));
                    }
                    executeAddWithInputs(addInputs);
                    i += 1 + 8;
                    break;
                case "U":
                    List<String> updateInputs = new ArrayList<>();
                    for (int j = 0; j < 9 && (i + 1 + j) < nonEmptyLines.size(); j++) {
                        updateInputs.add(nonEmptyLines.get(i + 1 + j));
                    }
                    executeUpdateWithInputs(updateInputs);
                    i += 1 + 9;
                    break;
                case "S":
                    getInvoker().ListInfo();
                    i++;
                    break;
                case "H":
                    getInvoker().PrintHistory();
                    i++;
                    break;
                case "X":
                    getInvoker().Exit();
                    i++;
                    break;
                case "HELP":
                    getInvoker().PrintHelp();
                    i++;
                    break;
                case "L":
                    getInvoker().Clear();
                    i++;
                    break;
                case "R":
                    if (hasArgument) {
                        try {
                            Long id = Long.parseLong(argument);
                            getInvoker().RemoveById(id);
                        } catch (NumberFormatException e) {
                            System.out.println("Error: Invalid ID argument for R command.");
                        }
                    } else if (i + 1 < nonEmptyLines.size()) {
                        try {
                            Long id = Long.parseLong(nonEmptyLines.get(i + 1));
                            getInvoker().RemoveById(id);
                        } catch (NumberFormatException e) {
                            System.out.println("Error: Invalid ID argument for R command.");
                        }
                        i++;
                    } else {
                        System.out.println("Error: R command requires an argument.");
                    }
                    i++;
                    break;
                case "RB":
                    if (hasArgument) {
                        getInvoker().RemoveByBestAlbum(argument);
                    } else if (i + 1 < nonEmptyLines.size()) {
                        getInvoker().RemoveByBestAlbum(nonEmptyLines.get(i + 1));
                        i++;
                    } else {
                        System.out.println("Error: RB command requires an argument.");
                    }
                    i++;
                    break;
                case "REMOVE_GREATER":
                    if (hasArgument) {
                        try {
                            Long id = Long.parseLong(argument);
                            getInvoker().RemoveGreaterThanId(id);
                        } catch (NumberFormatException e) {
                            System.out.println("Error: Invalid ID argument for REMOVE_GREATER command.");
                        }
                    } else if (i + 1 < nonEmptyLines.size()) {
                        try {
                            Long id = Long.parseLong(nonEmptyLines.get(i + 1));
                            getInvoker().RemoveGreaterThanId(id);
                        } catch (NumberFormatException e) {
                            System.out.println("Error: Invalid ID argument for REMOVE_GREATER command.");
                        }
                        i++;
                    } else {
                        System.out.println("Error: REMOVE_GREATER command requires an argument.");
                    }
                    i++;
                    break;
                case "M":
                    if (hasArgument) {
                        ExecuteScriptCommand nestedScript = new ExecuteScriptCommand(argument);
                        nestedScript.setInvoker(getInvoker());
                        nestedScript.execute();
                    } else if (i + 1 < nonEmptyLines.size()) {
                        ExecuteScriptCommand nestedScript = new ExecuteScriptCommand(nonEmptyLines.get(i + 1));
                        nestedScript.setInvoker(getInvoker());
                        nestedScript.execute();
                        i++;
                    } else {
                        System.out.println("Error: M command requires a file path argument.");
                    }
                    i++;
                    break;
                default:
                    System.out.println("Unknown command in script: " + cmd);
                    i++;
                    break;
            }
        }
    }

    private void executeAddWithInputs(List<String> inputs) {
        if (inputs.size() < 8) {
            System.out.println("Error: A command requires 8 input lines but got " + inputs.size());
            return;
        }

        InputStream originalIn = System.in;
        try {
            ScriptInputStream scriptInputStream = new ScriptInputStream(inputs);
            System.setIn(scriptInputStream);
            getInvoker().AddElement();
        } finally {
            System.setIn(originalIn);
        }
    }

    private void executeUpdateWithInputs(List<String> inputs) {
        if (inputs.size() < 9) {
            System.out.println("Error: U command requires 9 input lines but got " + inputs.size());
            return;
        }

        InputStream originalIn = System.in;
        try {
            ScriptInputStream scriptInputStream = new ScriptInputStream(inputs);
            System.setIn(scriptInputStream);
            getInvoker().UpdateElementWithId(Long.parseLong(inputs.get(0)));
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid ID in U command: " + inputs.get(0));
        } finally {
            System.setIn(originalIn);
        }
    }
}
