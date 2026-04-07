package com.client.handlers;

import com.client.network.AsyncClient;
import com.common.Request;
import com.common.Request.RequestType;
import com.common.Response;
import com.model.Album;
import com.model.Coordinates;
import com.model.MusicBand;
import com.model.MusicGenre;
import com.utils.CollectionFileManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ExecuteScriptHandler implements CommandHandler {
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
        
        List<String> allLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(resolvedPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    allLines.add(trimmed);
                }
            }
        } catch (Exception e) {
            return Response.error("Script file not found: " + e.getMessage());
        }
        
        StringBuilder results = new StringBuilder();
        int i = 0;
        
        while (i < allLines.size()) {
            String commandLine = allLines.get(i);
            String[] cmdParts = commandLine.split("\\s+");
            String cmd = cmdParts[0].toLowerCase();
            boolean hasArgument = cmdParts.length > 1;
            
            try {
                switch (cmd) {
                    case "show":
                        results.append("Line ").append(i + 1).append(": ").append(executeShow(client)).append("\n");
                        i++;
                        break;
                        
                    case "add":
                        if (i + 8 > allLines.size()) {
                            results.append("Line ").append(i + 1).append(": ").append("Error: add command requires 8 input lines\n");
                        } else {
                            results.append(handleAddFromScript(client, allLines, i));
                        }
                        i += 9;
                        break;
                        
                    case "add_if_min":
                        if (i + 9 > allLines.size()) {
                            results.append("Line ").append(i + 1).append(": ").append("Error: add_if_min command requires 9 input lines\n");
                        } else {
                            results.append(handleAddIfMinFromScript(client, allLines, i));
                        }
                        i += 10;
                        break;
                        
                    case "update":
                        if (cmdParts.length >= 3 && cmdParts[1].equalsIgnoreCase("id")) {
                            if (i + 10 > allLines.size()) {
                                results.append("Line ").append(i + 1).append(": ").append("Error: update id command requires 10 input lines\n");
                            } else {
                                results.append(handleUpdateFromScript(client, allLines, i, cmdParts));
                            }
                        } else {
                            results.append("Line ").append(i + 1).append(": ").append("Error: update command requires 'id <id>' format.\n");
                            i++;
                        }
                        break;
                        
                    case "info":
                        results.append("Line ").append(i + 1).append(": ").append(executeInfo(client)).append("\n");
                        i++;
                        break;
                        
                    case "history":
                        results.append("Line ").append(i + 1).append(": ").append(executeHistory(client)).append("\n");
                        i++;
                        break;
                        
                    case "clear":
                        results.append("Line ").append(i + 1).append(": ").append(executeClear(client)).append("\n");
                        i++;
                        break;
                        
                    case "save":
                        results.append("Line ").append(i + 1).append(": ").append(executeSave(client)).append("\n");
                        i++;
                        break;
                        
                    case "remove_by_id":
                        results.append("Line ").append(i + 1).append(": ").append(
                            handleRemoveByIdFromScript(client, allLines, i, hasArgument, cmdParts)).append("\n");
                        i++;
                        break;
                        
                    case "remove_greater":
                        results.append("Line ").append(i + 1).append(": ").append(
                            handleRemoveGreaterFromScript(client, allLines, i, hasArgument, cmdParts)).append("\n");
                        i++;
                        break;
                        
                    case "remove_any_by_best_album":
                        results.append("Line ").append(i + 1).append(": ").append(
                            handleRemoveByBestAlbumFromScript(client, allLines, i, hasArgument, commandLine)).append("\n");
                        i++;
                        break;
                        
                    case "count_by_number_of_participants":
                        results.append("Line ").append(i + 1).append(": ").append(
                            handleCountByParticipantsFromScript(client, allLines, i, hasArgument, cmdParts)).append("\n");
                        i++;
                        break;
                        
                    case "average_of_number_of_participants":
                        results.append("Line ").append(i + 1).append(": ").append(executeAverageParticipants(client)).append("\n");
                        i++;
                        break;
                        
                    case "help":
                        results.append("Line ").append(i + 1).append(": ").append(executeHelp(client)).append("\n");
                        i++;
                        break;
                        
                    default:
                        results.append("Line ").append(i + 1).append(": ").append("Unknown command: " + cmd).append("\n");
                        i++;
                        break;
                }
            } catch (Exception e) {
                results.append("Line ").append(i + 1).append(": ").append("Error: " + e.getMessage()).append("\n");
                i++;
            }
        }
        
        return Response.success("Script executed. Results:\n" + results.toString());
    }
    
    private String handleAddFromScript(AsyncClient client, List<String> allLines, int i) {
        String currentLogin = LoginHandler.getCurrentLogin();
        String currentPasswordHash = LoginHandler.getCurrentPasswordHash();
        
        if (currentLogin == null) {
            return "Error: Please login first using 'login <login> <password>'\n";
        }
        
        List<String> addInputs = new ArrayList<>();
        for (int j = 0; j < 8; j++) {
            addInputs.add(allLines.get(i + 1 + j));
        }
        MusicBand band = parseBandFromInputs(addInputs);
        
        Map<String, Object> args = new java.util.HashMap<>();
        args.put("login", currentLogin);
        args.put("passwordHash", currentPasswordHash);
        Request addRequest = new Request(RequestType.COMMAND, "add");
        addRequest.setData(band);
        addRequest.setArgs(args);
        
        try {
            Response resp = client.send(addRequest);
            return resp.isSuccess() ? resp.getResult() : resp.getError();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    private String handleAddIfMinFromScript(AsyncClient client, List<String> allLines, int i) {
        String currentLogin = LoginHandler.getCurrentLogin();
        String currentPasswordHash = LoginHandler.getCurrentPasswordHash();
        
        if (currentLogin == null) {
            return "Error: Please login first\n";
        }
        
        try {
            long id = Long.parseLong(allLines.get(i + 1));
            List<String> addInputs = new ArrayList<>();
            for (int j = 0; j < 8; j++) {
                addInputs.add(allLines.get(i + 2 + j));
            }
            MusicBand band = parseBandFromInputs(addInputs);
            band.setId(id);
            
            Map<String, Object> args = new java.util.HashMap<>();
            args.put("login", currentLogin);
            args.put("passwordHash", currentPasswordHash);
            Request addRequest = new Request(RequestType.COMMAND, "add_if_min");
            addRequest.setData(band);
            addRequest.setArgs(args);
            
            Response resp = client.send(addRequest);
            return resp.isSuccess() ? resp.getResult() : resp.getError();
        } catch (NumberFormatException e) {
            return "Error: Invalid ID for add_if_min\n";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    private String handleUpdateFromScript(AsyncClient client, List<String> allLines, int i, String[] cmdParts) {
        String currentLogin = LoginHandler.getCurrentLogin();
        String currentPasswordHash = LoginHandler.getCurrentPasswordHash();
        
        if (currentLogin == null) {
            return "Error: Please login first\n";
        }
        
        try {
            Long id = Long.parseLong(cmdParts[2]);
            
            List<String> updateInputs = new ArrayList<>();
            for (int j = 0; j < 9; j++) {
                updateInputs.add(allLines.get(i + 3 + j));
            }
            MusicBand band = parseBandFromInputs(updateInputs);
            band.setId(id);
            
            Map<String, Object> args = new java.util.HashMap<>();
            args.put("id", id);
            args.put("band", band);
            args.put("login", currentLogin);
            args.put("passwordHash", currentPasswordHash);
            Request updateRequest = new Request(RequestType.COMMAND, "update");
            updateRequest.setData(band);
            updateRequest.setArgs(args);
            
            Response resp = client.send(updateRequest);
            return resp.isSuccess() ? resp.getResult() : resp.getError();
        } catch (NumberFormatException e) {
            return "Error: Invalid ID in update id command.\n";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    private String handleRemoveByIdFromScript(AsyncClient client, List<String> allLines, int i, boolean hasArgument, String[] cmdParts) throws Exception {
        try {
            Long id;
            if (hasArgument) {
                id = Long.parseLong(cmdParts[1]);
            } else if (i + 1 < allLines.size()) {
                id = Long.parseLong(allLines.get(i + 1));
            } else {
                return "Error: remove_by_id command requires an argument.\n";
            }
            return executeRemoveById(client, id);
        } catch (NumberFormatException e) {
            return "Error: Invalid ID argument for remove_by_id command.\n";
        }
    }
    
    private String handleRemoveGreaterFromScript(AsyncClient client, List<String> allLines, int i, boolean hasArgument, String[] cmdParts) throws Exception {
        try {
            Long id;
            if (hasArgument) {
                id = Long.parseLong(cmdParts[1]);
            } else if (i + 1 < allLines.size()) {
                id = Long.parseLong(allLines.get(i + 1));
            } else {
                return "Error: remove_greater command requires an argument.\n";
            }
            return executeRemoveGreater(client, id);
        } catch (NumberFormatException e) {
            return "Error: Invalid ID argument for remove_greater command.\n";
        }
    }
    
    private String handleRemoveByBestAlbumFromScript(AsyncClient client, List<String> allLines, int i, boolean hasArgument, String commandLine) throws Exception {
        try {
            String album;
            if (hasArgument) {
                album = commandLine.substring(commandLine.indexOf(" ") + 1);
            } else if (i + 1 < allLines.size()) {
                album = allLines.get(i + 1);
            } else {
                return "Error: remove_any_by_best_album command requires an argument.\n";
            }
            return executeRemoveByBestAlbum(client, album);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    private String handleCountByParticipantsFromScript(AsyncClient client, List<String> allLines, int i, boolean hasArgument, String[] cmdParts) throws Exception {
        try {
            Integer count;
            if (hasArgument) {
                count = Integer.parseInt(cmdParts[1]);
            } else if (i + 1 < allLines.size()) {
                count = Integer.parseInt(allLines.get(i + 1));
            } else {
                return "Error: count_by_number_of_participants command requires an argument.\n";
            }
            return executeCountByParticipants(client, count);
        } catch (NumberFormatException e) {
            return "Error: Invalid argument for count_by_number_of_participants command.\n";
        }
    }
    
    private MusicBand parseBandFromInputs(List<String> inputs) {
        MusicBand band = new MusicBand();
        
        band.setName(inputs.get(0));
        band.setNumberOfParticipants(Integer.parseInt(inputs.get(1)));
        
        String genreStr = inputs.get(2).trim();
        if (!genreStr.isEmpty()) {
            try {
                band.setGenre(MusicGenre.valueOf(genreStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                band.setGenre(null);
            }
        } else {
            band.setGenre(null);
        }
        
        long x = Long.parseLong(inputs.get(3));
        int y = Integer.parseInt(inputs.get(4));
        band.setCoordinates(new Coordinates(x, y));
        
        String description = inputs.get(5).trim();
        band.setDescription(description.isEmpty() ? null : description);
        
        String albumName = inputs.get(6);
        double sales = Double.parseDouble(inputs.get(7));
        band.setBestAlbum(new Album(albumName, sales));
        
        return band;
    }
    
    private String executeShow(AsyncClient client) throws Exception {
        String sql = "SELECT * FROM music_bands ORDER BY name";
        Map<String, Object> args = new java.util.HashMap<>();
        args.put("sql", sql);
        args.put("operation", "SELECT");
        args.put("command", "show");
        
        Request request = new Request(RequestType.COMMAND, "show");
        request.setArgs(args);
        
        Response resp = client.send(request);
        if (!resp.isSuccess()) {
            return resp.getError();
        }
        
        String result = resp.getResult();
        if (result == null || result.equals("EMPTY_RESULT")) {
            return "Collection is empty";
        }
        
        StringBuilder sb = new StringBuilder();
        String[] bands = result.split(";");
        for (String bandData : bands) {
            if (bandData.trim().isEmpty()) continue;
            MusicBand band = parseBandFromSqlResult(bandData);
            sb.append(band.toString());
        }
        return sb.toString();
    }
    
    private MusicBand parseBandFromSqlResult(String sqlResult) {
        MusicBand band = new MusicBand();
        String[] rows = sqlResult.split(";");
        for (String row : rows) {
            if (row.trim().isEmpty()) continue;
            String[] kv = row.split("\\|");
            for (String pair : kv) {
                String[] keyValue = pair.split("=");
                if (keyValue.length != 2) continue;
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                
                switch (key) {
                    case "id": band.setId(Long.parseLong(value)); break;
                    case "name": band.setName(value); break;
                    case "x": band.setCoordinates(new Coordinates(Long.parseLong(value), 0)); break;
                    case "y": if (band.getCoordinates() != null) band.setCoordinates(new Coordinates(band.getCoordinates().getX(), Integer.parseInt(value))); break;
                    case "number_of_participants": band.setNumberOfParticipants(Integer.parseInt(value)); break;
                    case "description": band.setDescription(value.equals("null") ? null : value); break;
                    case "genre": band.setGenre(value.equals("null") ? null : MusicGenre.valueOf(value)); break;
                    case "album_name": if (!value.equals("null")) band.setBestAlbum(new Album(value, 0.0)); break;
                    case "album_sales": if (band.getBestAlbum() != null) band.setBestAlbum(new Album(band.getBestAlbum().getName(), Double.parseDouble(value))); break;
                    case "creation_date": 
                        try {
                            if (!value.equals("null")) {
                                band.setCreationDate(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(value));
                            }
                        } catch (Exception e) {}
                        break;
                }
            }
        }
        return band;
    }
    
    private String executeInfo(AsyncClient client) throws Exception {
        String sql = "SELECT COUNT(*) as count FROM music_bands";
        Map<String, Object> args = new java.util.HashMap<>();
        args.put("sql", sql);
        args.put("operation", "SELECT");
        args.put("command", "info");
        
        Request request = new Request(RequestType.COMMAND, "info");
        request.setArgs(args);
        
        Response resp = client.send(request);
        if (!resp.isSuccess()) {
            return resp.getError();
        }
        
        String countStr = resp.getResult();
        return "Collection type: MusicBand\n" +
               "Database: PostgreSQL\n" +
               "Number of elements: " + countStr;
    }
    
    private String executeHistory(AsyncClient client) throws Exception {
        Request request = new Request(RequestType.COMMAND, "history");
        Response resp = client.send(request);
        return resp.isSuccess() ? resp.getResult() : resp.getError();
    }
    
    private String executeClear(AsyncClient client) throws Exception {
        String sql = "DELETE FROM music_bands";
        Map<String, Object> args = new java.util.HashMap<>();
        args.put("sql", sql);
        args.put("operation", "DELETE");
        args.put("command", "clear");
        
        Request request = new Request(RequestType.COMMAND, "clear");
        request.setArgs(args);
        
        Response resp = client.send(request);
        return resp.isSuccess() ? resp.getResult() : resp.getError();
    }
    
    private String executeSave(AsyncClient client) throws Exception {
        return "Data is automatically persisted to PostgreSQL database";
    }
    
    private String executeHelp(AsyncClient client) throws Exception {
        Request request = new Request(RequestType.COMMAND, "help");
        Response resp = client.send(request);
        return resp.isSuccess() ? resp.getResult() : resp.getError();
    }
    
    private String executeRemoveById(AsyncClient client, Long id) throws Exception {
        Map<String, Object> args = new java.util.HashMap<>();
        args.put("id", id);
        
        Request request = new Request(RequestType.COMMAND, "remove_by_id");
        request.setArgs(args);
        
        Response resp = client.send(request);
        return resp.isSuccess() ? resp.getResult() : resp.getError();
    }
    
    private String executeRemoveGreater(AsyncClient client, Long id) throws Exception {
        Map<String, Object> args = new java.util.HashMap<>();
        args.put("id", id);
        
        Request request = new Request(RequestType.COMMAND, "remove_greater");
        request.setArgs(args);
        
        Response resp = client.send(request);
        return resp.isSuccess() ? resp.getResult() : resp.getError();
    }
    
    private String executeRemoveByBestAlbum(AsyncClient client, String album) throws Exception {
        Map<String, Object> args = new java.util.HashMap<>();
        args.put("album", album);
        
        Request request = new Request(RequestType.COMMAND, "remove_any_by_best_album");
        request.setArgs(args);
        
        Response resp = client.send(request);
        return resp.isSuccess() ? resp.getResult() : resp.getError();
    }
    
    private String executeCountByParticipants(AsyncClient client, Integer count) throws Exception {
        Map<String, Object> args = new java.util.HashMap<>();
        args.put("count", count);
        
        Request request = new Request(RequestType.COMMAND, "count_by_number_of_participants");
        request.setArgs(args);
        
        Response resp = client.send(request);
        return resp.isSuccess() ? resp.getResult() : resp.getError();
    }
    
    private String executeAverageParticipants(AsyncClient client) throws Exception {
        Request request = new Request(RequestType.COMMAND, "average_of_number_of_participants");
        Response resp = client.send(request);
        return resp.isSuccess() ? resp.getResult() : resp.getError();
    }
}