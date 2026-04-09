package com.client.handlers;

import com.auth.AuthorizationService;
import com.client.network.AsyncClient;
import com.common.Command;
import com.common.RequestBuilder;
import com.common.Response;
import com.model.MusicBand;

import java.util.Scanner;

public class UpdateHandler implements CommandHandler {
    @Override
    public Response handle(AsyncClient client, String[] parts, Scanner scanner) throws Exception {
        if (AuthorizationService.getClientLogin() == null) {
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
        
        Response selectResp = client.send(
            RequestBuilder.command(Command.SELECT)
                .withArg("id", id)
                .build()
        );
        
        if (!selectResp.isSuccess()) {
            return selectResp;
        }
        
        if (selectResp.getData() == null) {
            return Response.error("Band with ID " + id + " not found");
        }
        
        MusicBand existingBand = (MusicBand) selectResp.getData();
        
        MusicBand updatedBand = ClientBandPrompt.promptForBand(scanner, existingBand);
        updatedBand.setId(id);
        
        return client.send(RequestBuilder.command(Command.UPDATE)
            .withArg("id", id)
            .withData(updatedBand)
            .withAuth()
            .build());
    }
}