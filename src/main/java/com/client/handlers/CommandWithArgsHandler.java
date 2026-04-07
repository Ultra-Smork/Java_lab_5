package com.client.handlers;

import com.client.network.AsyncClient;
import com.common.Request;
import com.common.Request.RequestType;
import com.common.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CommandWithArgsHandler implements CommandHandler {
    @Override
    public Response handle(AsyncClient client, String[] parts, Scanner scanner) throws Exception {
        String cmd = parts[0].toLowerCase();
        Map<String, Object> args = new HashMap<>();
        
        String currentLogin = LoginHandler.getCurrentLogin();
        String currentPasswordHash = LoginHandler.getCurrentPasswordHash();
        
        switch (cmd) {
            case "remove_by_id":
                if (parts.length < 2) {
                    return Response.error("Usage: remove_by_id <id>");
                }
                try {
                    args.put("id", Long.parseLong(parts[1]));
                } catch (NumberFormatException e) {
                    return Response.error("Invalid ID: " + parts[1]);
                }
                break;
                
            case "remove_greater":
                if (parts.length < 2) {
                    return Response.error("Usage: remove_greater <id>");
                }
                try {
                    args.put("id", Long.parseLong(parts[1]));
                } catch (NumberFormatException e) {
                    return Response.error("Invalid ID: " + parts[1]);
                }
                break;
                
            case "remove_any_by_best_album":
                if (parts.length < 2) {
                    return Response.error("Usage: remove_any_by_best_album <album_name>");
                }
                String albumName = String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length));
                args.put("album", albumName);
                break;
                
            case "count_by_number_of_participants":
                if (parts.length < 2) {
                    return Response.error("Usage: count_by_number_of_participants <count>");
                }
                try {
                    args.put("count", Integer.parseInt(parts[1]));
                } catch (NumberFormatException e) {
                    return Response.error("Invalid count: " + parts[1]);
                }
                break;
                
            case "participants_by_id":
                if (parts.length < 2) {
                    return Response.error("Usage: participants_by_id <id>");
                }
                try {
                    args.put("id", Long.parseLong(parts[1]));
                } catch (NumberFormatException e) {
                    return Response.error("Invalid ID: " + parts[1]);
                }
                break;
                
            default:
                break;
        }
        
        Request request = new Request(RequestType.COMMAND, cmd);
        request.setArgs(args);
        
        if (currentLogin != null && currentPasswordHash != null) {
            request.setLogin(currentLogin);
            request.setPassword(currentPasswordHash);
            args.put("login", currentLogin);
            args.put("passwordHash", currentPasswordHash);
        }
        
        return client.send(request);
    }
}