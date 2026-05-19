package com.commands;

import com.common.Response;
import com.utils.CollectionFileManager;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

public class ExecuteScriptCommand implements ServerCommand {
    @Override
    public Response execute(Map<String, Object> args) {
        if (args == null || args.get("path") == null) {
            return Response.error("Missing file path for execute_script command");
        }
        String filePath = (String) args.get("path");
        String resolvedPath = CollectionFileManager.resolvePath(filePath);

        if (resolvedPath == null) {
            return Response.error("Invalid file path: " + filePath);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(resolvedPath))) {
            StringBuilder results = new StringBuilder();
            String line;
            int lineNum = 0;

            while ((line = reader.readLine()) != null) {
                lineNum++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                results.append("Line ").append(lineNum).append(": ").append(trimmed).append("\n");
            }

            return Response.success("Script executed. Results:\n" + results.toString());
        } catch (IOException e) {
            return Response.error("Script file not found");
        }
    }
}