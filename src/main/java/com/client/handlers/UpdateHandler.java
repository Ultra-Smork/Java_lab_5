package com.client.handlers;

import com.client.network.AsyncClient;
import com.common.Request;
import com.common.Request.RequestType;
import com.common.Response;
import com.model.MusicBand;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class UpdateHandler implements CommandHandler {
    @Override
    public Response handle(AsyncClient client, String[] parts, Scanner scanner) throws Exception {
        String currentLogin = LoginHandler.getCurrentLogin();
        String currentPasswordHash = LoginHandler.getCurrentPasswordHash();
        
        if (currentLogin == null) {
            return Response.error("Please login first using 'login <login> <password>'");
        }
        
        if (parts.length < 2 || !parts[1].equalsIgnoreCase("id") || parts.length < 3) {
            System.out.println("Usage: update id <id>");
            return Response.error("Usage: update id <id>");
        }
        
        long id;
        try {
            id = Long.parseLong(parts[2]);
        } catch (NumberFormatException e) {
            return Response.error("Invalid ID: " + parts[2] + ". Please enter a valid number.");
        }
        
        Request selectRequest = new Request(RequestType.COMMAND, "select");
        Map<String, Object> selectArgs = new HashMap<>();
        selectArgs.put("id", id);
        selectRequest.setArgs(selectArgs);
        
        Response selectResp = client.send(selectRequest);
        if (!selectResp.isSuccess()) {
            return selectResp;
        }
        
        if (selectResp.getData() == null) {
            return Response.error("Band with ID " + id + " not found");
        }
        
        MusicBand existingBand = (MusicBand) selectResp.getData();
        
        MusicBand updatedBand = ClientBandPrompt.promptForBand(scanner, existingBand);
        updatedBand.setId(id);
        
        Map<String, Object> args = new HashMap<>();
        args.put("id", id);
        args.put("band", updatedBand);
        args.put("login", currentLogin);
        args.put("passwordHash", currentPasswordHash);
        
        Request request = new Request(RequestType.COMMAND, "update");
        request.setData(updatedBand);
        request.setArgs(args);
        
        return client.send(request);
    }
}