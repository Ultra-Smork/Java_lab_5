package com.client.handlers;

import com.client.network.AsyncClient;
import com.client.handlers.script.ScriptAuthChecker;
import com.client.handlers.script.ScriptCommandExecutor;
import com.common.Response;
import com.utils.CollectionFileManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ExecuteScriptHandler implements CommandHandler {
    
    private final ScriptAuthChecker authChecker;
    private final ScriptCommandExecutor executor;
    
    public ExecuteScriptHandler() {
        this.authChecker = new ScriptAuthChecker();
        this.executor = new ScriptCommandExecutor();
    }
    
    @Override
    public Response handle(AsyncClient client, String[] parts, Scanner scanner) throws Exception {
        if (parts.length < 2) {
            return Response.error("Usage: execute_script <file_path>");
        }
        
        String filePath = String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length));
        String resolvedPath = CollectionFileManager.resolvePath(filePath);
        
        if (resolvedPath == null) {
            return Response.error("Invalid file path: " + filePath);
        }
        
        List<String> lines = loadScriptLines(resolvedPath);
        if (lines == null) {
            return Response.error("Script file not found");
        }
        
        return executeScript(client, lines);
    }
    
    private List<String> loadScriptLines(String path) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    lines.add(trimmed);
                }
            }
        } catch (Exception e) {
            return null;
        }
        return lines;
    }
    
    private Response executeScript(AsyncClient client, List<String> lines) {
        String authError = authChecker.requireAuth();
        if (authError != null) {
            return Response.error(authError);
        }
        
        StringBuilder results = new StringBuilder();
        int i = 0;
        
        while (i < lines.size()) {
            String commandLine = lines.get(i);
            String[] cmdParts = commandLine.split("\\s+");
            String cmd = cmdParts[0].toLowerCase();
            
            int skip = getSkipCount(cmd);
            if (skip > 0 && i + skip > lines.size()) {
                results.append("Line ").append(i + 1)
                    .append(": Error: ").append(cmd).append(" requires ")
                    .append(skip).append(" lines\n");
                i++;
                continue;
            }
            
            List<String> args = (skip > 0) ? lines.subList(i, i + skip) : List.of();
            String result = executor.execute(client, cmd, args, i);
            
            results.append("Line ").append(i + 1).append(": ").append(result).append("\n");
            i += Math.max(skip, 1);
        }
        
        return Response.success("Script executed. Results:\n" + results.toString());
    }
    
    private int getSkipCount(String cmd) {
        return switch (cmd) {
            case "add" -> 9;
            case "add_if_min", "update" -> 10;
            default -> 1;
        };
    }
}