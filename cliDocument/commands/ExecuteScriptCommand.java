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

/**
 * Command implementation for executing commands from a script file.
 * This command reads a file containing commands and executes them sequentially.
 * Supports nested script execution up to a maximum depth of 5 levels.
 */
public class ExecuteScriptCommand implements Command {
    /** Current script recursion depth */
    private static int scriptDepth = 0;
    /** Maximum allowed script recursion depth */
    private static final int MAX_DEPTH = 5;

    /** Path to the script file to execute */
    private final String filePath;
    /** Reference to the invoker for executing commands */
    private Invoker invoker;

    /**
     * Constructs an ExecuteScriptCommand with the specified file path.
     *
     * @param filePath the path to the script file to execute
     */
    public ExecuteScriptCommand(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Sets the invoker reference for this command.
     *
     * @param invoker the Invoker instance to use for command execution
     */
    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }

    /**
     * Gets the invoker instance, creating one if not set.
     *
     * @return the Invoker instance
     */
    private Invoker getInvoker() {
        if (invoker == null) {
            invoker = new Invoker();
        }
        return invoker;
    }

    /**
     * Executes the script file command.
     * Checks recursion depth, reads the file, and executes all commands.
     */
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

    /**
     * Reads all lines from the script file.
     *
     * @param path the path to the script file
     * @return a list of lines from the file, or null if the file cannot be read
     */
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

    /**
     * Resolves the full path to a script file by searching in common locations.
     * Searches in scripts directories relative to the current working directory.
     *
     * @param path the file name or relative path to resolve
     * @return the full path to the script file, or null if not found
     */
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

    /**
     * Executes commands from a list of script lines.
     * Parses each line as a command and delegates to the invoker.
     *
     * @param allLines the list of lines to execute as commands
     */
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
            String cmd = parts[0].toLowerCase();
            boolean hasArgument = parts.length > 1;
            String argument = hasArgument ? parts[1] : null;

            switch (cmd) {
                case "show":
                    getInvoker().ListAll();
                    i++;
                    break;
                case "add":
                    List<String> addInputs = new ArrayList<>();
                    for (int j = 0; j < 8 && (i + 1 + j) < nonEmptyLines.size(); j++) {
                        addInputs.add(nonEmptyLines.get(i + 1 + j));
                    }
                    executeAddWithInputs(addInputs);
                    i += 1 + 8;
                    break;
                case "update":
                    if (parts.length >= 3 && parts[1].equalsIgnoreCase("id")) {
                        try {
                            Long id = Long.parseLong(parts[2]);
                            List<String> updateInputs = new ArrayList<>();
                            updateInputs.add(parts[2]);
                            for (int j = 3; j < 9 && (i + 1 + (j - 2)) < nonEmptyLines.size(); j++) {
                                updateInputs.add(nonEmptyLines.get(i + 1 + (j - 2)));
                            }
                            executeUpdateWithInputs(updateInputs);
                            i += 1 + updateInputs.size();
                        } catch (NumberFormatException e) {
                            System.out.println("Error: Invalid ID in update id command.");
                            i++;
                        }
                    } else {
                        System.out.println("Error: update command requires 'id <id>' format.");
                        i++;
                    }
                    break;
                case "info":
                    getInvoker().ListInfo();
                    i++;
                    break;
                case "history":
                    getInvoker().PrintHistory();
                    i++;
                    break;
                case "exit":
                    getInvoker().Exit();
                    i++;
                    break;
                case "help":
                    getInvoker().PrintHelp();
                    i++;
                    break;
                case "clear":
                    getInvoker().Clear();
                    i++;
                    break;
                case "remove_by_id":
                    if (hasArgument) {
                        try {
                            Long id = Long.parseLong(argument);
                            getInvoker().RemoveById(id);
                        } catch (NumberFormatException e) {
                            System.out.println("Error: Invalid ID argument for remove_by_id command.");
                        }
                    } else if (i + 1 < nonEmptyLines.size()) {
                        try {
                            Long id = Long.parseLong(nonEmptyLines.get(i + 1));
                            getInvoker().RemoveById(id);
                        } catch (NumberFormatException e) {
                            System.out.println("Error: Invalid ID argument for remove_by_id command.");
                        }
                        i++;
                    } else {
                        System.out.println("Error: remove_by_id command requires an argument.");
                    }
                    i++;
                    break;
                case "remove_any_by_best_album":
                    if (hasArgument) {
                        getInvoker().RemoveByBestAlbum(argument);
                    } else if (i + 1 < nonEmptyLines.size()) {
                        getInvoker().RemoveByBestAlbum(nonEmptyLines.get(i + 1));
                        i++;
                    } else {
                        System.out.println("Error: remove_any_by_best_album command requires an argument.");
                    }
                    i++;
                    break;
                case "remove_greater":
                    if (hasArgument) {
                        try {
                            Long id = Long.parseLong(argument);
                            getInvoker().RemoveGreaterThanId(id);
                        } catch (NumberFormatException e) {
                            System.out.println("Error: Invalid ID argument for remove_greater command.");
                        }
                    } else if (i + 1 < nonEmptyLines.size()) {
                        try {
                            Long id = Long.parseLong(nonEmptyLines.get(i + 1));
                            getInvoker().RemoveGreaterThanId(id);
                        } catch (NumberFormatException e) {
                            System.out.println("Error: Invalid ID argument for remove_greater command.");
                        }
                        i++;
                    } else {
                        System.out.println("Error: remove_greater command requires an argument.");
                    }
                    i++;
                    break;
                case "execute_script":
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
                        System.out.println("Error: execute_script command requires a file path argument.");
                    }
                    i++;
                    break;
                case "add_if_min":
                    getInvoker().AddIfMin();
                    i++;
                    break;
                case "average_of_number_of_participants":
                    getInvoker().AverageParticipants();
                    i++;
                    break;
                case "count_by_number_of_participants":
                    if (hasArgument) {
                        try {
                            Integer count = Integer.parseInt(argument);
                            getInvoker().CountByParticipants(count);
                        } catch (NumberFormatException e) {
                            System.out.println("Error: Invalid argument for count_by_number_of_participants command.");
                        }
                    } else if (i + 1 < nonEmptyLines.size()) {
                        try {
                            Integer count = Integer.parseInt(nonEmptyLines.get(i + 1));
                            getInvoker().CountByParticipants(count);
                        } catch (NumberFormatException e) {
                            System.out.println("Error: Invalid argument for count_by_number_of_participants command.");
                        }
                        i++;
                    } else {
                        System.out.println("Error: count_by_number_of_participants command requires an argument.");
                    }
                    i++;
                    break;
                case "save":
                    getInvoker().Save();
                    i++;
                    break;
                default:
                    System.out.println("Unknown command in script: " + cmd);
                    i++;
                    break;
            }
        }
    }

    /**
     * Executes the add command with predefined inputs from the script.
     * Temporarily redirects System.in to provide inputs programmatically.
     *
     * @param inputs the list of input values for the add command
     */
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

    /**
     * Executes the update command with predefined inputs from the script.
     * Temporarily redirects System.in to provide inputs programmatically.
     *
     * @param inputs the list of input values for the update command
     */
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
