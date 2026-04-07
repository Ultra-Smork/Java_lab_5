package com.client.handlers;

import com.client.network.AsyncClient;
import com.common.Request;
import com.common.Request.RequestType;
import com.common.Response;
import com.model.MusicBand;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class AddIfMinHandler implements CommandHandler {
    @Override
    public Response handle(AsyncClient client, String[] parts, Scanner scanner) throws Exception {
        String currentLogin = LoginHandler.getCurrentLogin();
        String currentPasswordHash = LoginHandler.getCurrentPasswordHash();
        
        if (currentLogin == null) {
            return Response.error("Please login first using 'login <login> <password>'");
        }
        
        long id = 0;
        while (true) {
            System.out.print("Enter ID (must be positive and less than minimum in collection): ");
            String idStr = scanner.nextLine().trim();
            if (idStr.isEmpty()) {
                System.out.println("Error: ID cannot be empty. Please try again.");
                continue;
            }
            try {
                id = Long.parseLong(idStr);
                if (id > 0) {
                    break;
                } else {
                    System.out.println("Error: ID must be greater than 0. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid ID format. Please enter a valid number.");
            }
        }
        
        MusicBand band = ClientBandPrompt.promptForBand(scanner, null);
        band.setId(id);
        
        Request request = new Request(RequestType.COMMAND, "add_if_min");
        request.setData(band);
        request.setLogin(currentLogin);
        request.setPassword(currentPasswordHash);
        
        Map<String, Object> args = new HashMap<>();
        args.put("login", currentLogin);
        args.put("passwordHash", currentPasswordHash);
        request.setArgs(args);
        
        return client.send(request);
    }
}