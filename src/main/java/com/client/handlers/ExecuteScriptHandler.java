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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
            return Response.error("Usage: execute_script <file_path> [<file_path>...]");
        }
        
        List<String> filePaths = new ArrayList<>();
        for (int i = 1; i < parts.length; i++) {
            String resolvedPath = CollectionFileManager.resolvePath(parts[i]);
            if (resolvedPath == null) {
                return Response.error("Invalid file path: " + parts[i]);
            }
            filePaths.add(resolvedPath);
        }
        
        return executeConcurrentScripts(client, filePaths);
    }
    
    private Response executeConcurrentScripts(AsyncClient client, List<String> filePaths) {
        String authError = authChecker.requireAuth();
        if (authError != null) {
            return Response.error(authError);
        }
        
        StringBuilder results = new StringBuilder();
        List<CompletableFuture<Void>> allFutures = new ArrayList<>();
        
        for (String filePath : filePaths) {
            List<String> lines = loadScriptLines(filePath);
            if (lines == null) {
                results.append("File not found: ").append(filePath).append("\n");
                continue;
            }
            
            results.append("=== Script: ").append(filePath).append(" ===\n");
            
            int i = 0;
            while (i < lines.size()) {
                String commandLine = lines.get(i);
                String[] cmdParts = commandLine.split("\\s+");
                String cmd = cmdParts[0].toLowerCase();
                
                int skip = getSkipCount(cmd);
                if (i + skip > lines.size()) {
                    results.append("Line ").append(i + 1).append(": Error: ").append(cmd).append(" requires ").append(skip).append(" lines\n");
                    i++;
                    continue;
                }
                
                List<String> args = (skip > 0) ? lines.subList(i, i + skip) : List.of();
                final int lineIdx = i;
                final String file = filePath;
                
                CompletableFuture<Void> f = executor.executeAsync(client, cmd, args)
                    .thenAccept(response -> {
                        synchronized (results) {
                            results.append("[Script=").append(file)
                                .append(" Line=").append(lineIdx + 1)
                                .append(" Cmd=").append(cmd)
                                .append("] ").append(response).append("\n");
                        }
                    })
                    .exceptionally(e -> {
                        synchronized (results) {
                            results.append("[Script=").append(file)
                                .append(" Line=").append(lineIdx + 1)
                                .append(" Cmd=").append(cmd)
                                .append("] Error: ").append(e.getCause() != null ? (e.getCause().getMessage() != null ? e.getCause().getMessage() : e.getCause().getClass().getSimpleName()) : (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName())).append("\n");
                        }
                        return null;
                    });
                allFutures.add(f);
                i += Math.max(skip, 1);
            }
        }
        
        for (CompletableFuture<Void> f : allFutures) {
            try {
                f.get(120, TimeUnit.SECONDS);
            } catch (Exception e) {
                results.append("Wait error: ").append(e.getMessage()).append("\n");
            }
        }
        
        return Response.success("Scripts executed. Results:\n" + results.toString());
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
    
    private int getSkipCount(String cmd) {
        return switch (cmd) {
            case "add" -> 9;
            case "add_if_min", "update" -> 10;
            case "remove_by_id", "remove_greater", "count_by_number_of_participants", "participants_by_id" -> 2;
            default -> 1;
        };
    }
}