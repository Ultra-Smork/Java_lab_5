package com.client.handlers;

import com.auth.AuthorizationService;
import com.client.network.AsyncClient;
import com.common.Command;
import com.common.RequestBuilder;
import com.common.Response;
import com.model.MusicBand;

import java.util.Scanner;

public class AddIfMinHandler implements CommandHandler {
    @Override
    public Response handle(AsyncClient client, String[] parts, Scanner scanner) throws Exception {
        if (AuthorizationService.getClientLogin() == null) {
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
        
        return client.send(RequestBuilder.command(Command.ADD_IF_MIN)
            .withData(band)
            .withAuth()
            .build());
    }
}