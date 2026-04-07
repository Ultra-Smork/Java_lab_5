package com.client.handlers;

import com.client.network.AsyncClient;
import com.common.Request;
import com.common.Request.RequestType;
import com.common.Response;
import com.model.MusicBand;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class AddHandler implements CommandHandler {
    @Override
    public Response handle(AsyncClient client, String[] parts, Scanner scanner) throws Exception {
        String currentLogin = LoginHandler.getCurrentLogin();
        String currentPasswordHash = LoginHandler.getCurrentPasswordHash();
        
        if (currentLogin == null) {
            return Response.error("Please login first using 'login <login> <password>'");
        }
        
        MusicBand band = ClientBandPrompt.promptForBand(scanner, null);
        
        Request request = new Request(RequestType.COMMAND, "add");
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