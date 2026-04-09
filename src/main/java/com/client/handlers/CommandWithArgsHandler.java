package com.client.handlers;

import com.client.network.AsyncClient;
import com.common.Command;
import com.common.RequestBuilder;
import com.common.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CommandWithArgsHandler implements CommandHandler {
    @Override
    public Response handle(AsyncClient client, String[] parts, Scanner scanner) throws Exception {
        String cmd = parts[0].toLowerCase();
        Command command = mapStringToCommand(cmd);
        
        if (command == null) {
            return Response.error("Unknown command: " + cmd);
        }
        
        Map<String, Object> args = new HashMap<>();
        
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
        
        return client.send(RequestBuilder.command(command)
            .withArgs(args)
            .withAuth()
            .build());
    }
    
    private Command mapStringToCommand(String cmd) {
        return switch (cmd) {
            case "show" -> Command.SHOW;
            case "select" -> Command.SELECT;
            case "info" -> Command.INFO;
            case "help" -> Command.HELP;
            case "history" -> Command.HISTORY;
            case "register" -> Command.REGISTER;
            case "login" -> Command.LOGIN;
            case "logout" -> Command.LOGOUT;
            case "clear" -> Command.CLEAR;
            case "add" -> Command.ADD;
            case "add_if_min" -> Command.ADD_IF_MIN;
            case "update" -> Command.UPDATE;
            case "remove_by_id" -> Command.REMOVE_BY_ID;
            case "remove_greater" -> Command.REMOVE_GREATER;
            case "remove_any_by_best_album" -> Command.REMOVE_ANY_BY_BEST_ALBUM;
            case "count_by_number_of_participants" -> Command.COUNT_BY_NUMBER_OF_PARTICIPANTS;
            case "participants_by_id" -> Command.PARTICIPANTS_BY_ID;
            case "average_of_number_of_participants" -> Command.AVERAGE_OF_NUMBER_OF_PARTICIPANTS;
            case "execute_script" -> Command.EXECUTE_SCRIPT;
            default -> null;
        };
    }
}