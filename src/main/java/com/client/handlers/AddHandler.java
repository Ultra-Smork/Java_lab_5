package com.client.handlers;

import com.auth.AuthorizationService;
import com.client.network.AsyncClient;
import com.common.Command;
import com.common.RequestBuilder;
import com.common.Response;
import com.model.MusicBand;

import java.util.Scanner;

public class AddHandler implements CommandHandler {
    @Override
    public Response handle(AsyncClient client, String[] parts, Scanner scanner) throws Exception {
        if (AuthorizationService.getClientLogin() == null) {
            return Response.error("Please login first using 'login <login> <password>'");
        }
        
        MusicBand band = ClientBandPrompt.promptForBand(scanner, null);
        
        RequestBuilder.command(Command.ADD)
            .withData(band)
            .withAuth()
            .build();
        
        return client.send(RequestBuilder.command(Command.ADD)
            .withData(band)
            .withAuth()
            .build());
    }
}