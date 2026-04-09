package com.client.handlers.script;

import com.auth.AuthorizationService;
import com.client.network.AsyncClient;
import com.common.Command;
import com.common.RequestBuilder;
import com.common.Response;
import com.model.MusicBand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class ScriptCommandExecutor {
    
    private final ScriptBandParser bandParser;
    
    private final Map<Command, BiFunction<AsyncClient, List<String>, String>> commands;
    
    public ScriptCommandExecutor() {
        this.bandParser = new ScriptBandParser();
        this.commands = initCommands();
    }
    
    private Map<Command, BiFunction<AsyncClient, List<String>, String>> initCommands() {
        Map<Command, BiFunction<AsyncClient, List<String>, String>> map = new HashMap<>();
        
        map.put(Command.SHOW, (c, l) -> executeShow(c));
        map.put(Command.INFO, (c, l) -> executeInfo(c));
        map.put(Command.HISTORY, (c, l) -> executeHistory(c));
        map.put(Command.HELP, (c, l) -> executeHelp(c));
        map.put(Command.CLEAR, (c, l) -> executeClear(c));
        map.put(Command.SAVE, (c, l) -> executeSave(c));
        map.put(Command.AVERAGE_OF_NUMBER_OF_PARTICIPANTS, (c, l) -> executeAverage(c));
        map.put(Command.COUNT_BY_NUMBER_OF_PARTICIPANTS, (c, l) -> executeCount(c, l));
        map.put(Command.PARTICIPANTS_BY_ID, (c, l) -> executeParticipantsById(c, l));
        map.put(Command.REMOVE_BY_ID, (c, l) -> executeRemoveById(c, l));
        map.put(Command.REMOVE_GREATER, (c, l) -> executeRemoveGreater(c, l));
        map.put(Command.REMOVE_ANY_BY_BEST_ALBUM, (c, l) -> executeRemoveByBestAlbum(c, l));
        map.put(Command.ADD, (c, l) -> executeAdd(c, l));
        map.put(Command.ADD_IF_MIN, (c, l) -> executeAddIfMin(c, l));
        map.put(Command.UPDATE, (c, l) -> executeUpdate(c, l));
        
        return map;
    }
    
    public String execute(AsyncClient client, String commandStr, List<String> lines, int index) {
        Command command = Command.fromString(commandStr);
        if (command == null) {
            return "Unknown command: " + commandStr;
        }
        
        BiFunction<AsyncClient, List<String>, String> executor = commands.get(command);
        if (executor == null) {
            return "Unknown command: " + commandStr;
        }
        
        try {
            return executor.apply(client, lines);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    private void checkAuth() {
        if (AuthorizationService.getClientLogin() == null) {
            throw new IllegalStateException("Not authenticated");
        }
    }
    
    private String sendRequest(AsyncClient client, com.common.Request request) {
        try {
            Response resp = client.send(request);
            return resp.isSuccess() ? resp.getResult() : resp.getError();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    private String executeShow(AsyncClient client) {
        return sendRequest(client, RequestBuilder.command(Command.SHOW).build());
    }
    
    private String executeInfo(AsyncClient client) {
        String result = sendRequest(client, RequestBuilder.command(Command.INFO).build());
        if (result == null || result.equals("EMPTY_RESULT")) {
            return "Collection is empty";
        }
        return "Collection type: MusicBand\nDatabase: PostgreSQL\nNumber of elements: " + result;
    }
    
    private String executeHistory(AsyncClient client) {
        return sendRequest(client, RequestBuilder.command(Command.HISTORY).build());
    }
    
    private String executeHelp(AsyncClient client) {
        return sendRequest(client, RequestBuilder.command(Command.HELP).build());
    }
    
    private String executeClear(AsyncClient client) {
        checkAuth();
        return sendRequest(client, RequestBuilder.command(Command.CLEAR).withAuth().build());
    }
    
    private String executeSave(AsyncClient client) {
        return "Data is automatically persisted to PostgreSQL database";
    }
    
    private String executeAverage(AsyncClient client) {
        return sendRequest(client, RequestBuilder.command(Command.AVERAGE_OF_NUMBER_OF_PARTICIPANTS).build());
    }
    
    private String executeCount(AsyncClient client, List<String> lines) {
        int count = parseInt(lines, 1, "count_by_number_of_participants");
        return sendRequest(client, RequestBuilder.command(Command.COUNT_BY_NUMBER_OF_PARTICIPANTS)
            .withArg("count", count).build());
    }
    
    private String executeParticipantsById(AsyncClient client, List<String> lines) {
        long id = parseLong(lines, 1, "participants_by_id");
        return sendRequest(client, RequestBuilder.command(Command.PARTICIPANTS_BY_ID)
            .withArg("id", id).build());
    }
    
    private String executeRemoveById(AsyncClient client, List<String> lines) {
        checkAuth();
        long id = parseLong(lines, 1, "remove_by_id");
        return sendRequest(client, RequestBuilder.command(Command.REMOVE_BY_ID)
            .withArg("id", id).withAuth().build());
    }
    
    private String executeRemoveGreater(AsyncClient client, List<String> lines) {
        checkAuth();
        long id = parseLong(lines, 1, "remove_greater");
        return sendRequest(client, RequestBuilder.command(Command.REMOVE_GREATER)
            .withArg("id", id).withAuth().build());
    }
    
    private String executeRemoveByBestAlbum(AsyncClient client, List<String> lines) {
        checkAuth();
        String album = parseString(lines, 1, "remove_any_by_best_album");
        return sendRequest(client, RequestBuilder.command(Command.REMOVE_ANY_BY_BEST_ALBUM)
            .withArg("album", album).withAuth().build());
    }
    
    private String executeAdd(AsyncClient client, List<String> lines) {
        checkAuth();
        List<String> inputs = lines.subList(1, 1 + bandParser.getExpectedFieldCount());
        MusicBand band = bandParser.parse(inputs);
        return sendRequest(client, RequestBuilder.command(Command.ADD)
            .withData(band).withAuth().build());
    }
    
    private String executeAddIfMin(AsyncClient client, List<String> lines) {
        checkAuth();
        long id = parseLong(lines, 1, "add_if_min");
        List<String> inputs = lines.subList(2, 2 + bandParser.getExpectedFieldCount());
        MusicBand band = bandParser.parse(inputs);
        band.setId(id);
        return sendRequest(client, RequestBuilder.command(Command.ADD_IF_MIN)
            .withData(band).withAuth().build());
    }
    
    private String executeUpdate(AsyncClient client, List<String> lines) {
        checkAuth();
        if (lines.size() < 3 || !lines.get(1).equalsIgnoreCase("id")) {
            return "Error: update command requires 'id <id>' format";
        }
        long id = parseLong(lines, 2, "update");
        List<String> inputs = lines.subList(3, 3 + bandParser.getExpectedFieldCount());
        MusicBand band = bandParser.parse(inputs);
        band.setId(id);
        return sendRequest(client, RequestBuilder.command(Command.UPDATE)
            .withArg("id", id).withData(band).withAuth().build());
    }
    
    private int parseInt(List<String> lines, int index, String command) {
        if (index >= lines.size()) {
            throw new IllegalArgumentException(command + " requires an argument");
        }
        try {
            return Integer.parseInt(lines.get(index));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid argument for " + command);
        }
    }
    
    private long parseLong(List<String> lines, int index, String command) {
        if (index >= lines.size()) {
            throw new IllegalArgumentException(command + " requires an argument");
        }
        try {
            return Long.parseLong(lines.get(index));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid argument for " + command);
        }
    }
    
    private String parseString(List<String> lines, int index, String command) {
        if (index >= lines.size()) {
            throw new IllegalArgumentException(command + " requires an argument");
        }
        return lines.get(index);
    }
}