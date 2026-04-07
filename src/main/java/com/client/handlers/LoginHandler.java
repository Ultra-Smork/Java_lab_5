package com.client.handlers;

import com.client.network.AsyncClient;
import com.common.Request;
import com.common.Request.RequestType;
import com.common.Response;
import com.server.DatabaseManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class LoginHandler implements CommandHandler {
    private static String currentLogin;
    private static String currentPasswordHash;

    public static String getCurrentLogin() {
        return currentLogin;
    }

    public static String getCurrentPasswordHash() {
        return currentPasswordHash;
    }

    public static void setCurrentLogin(String login) {
        currentLogin = login;
    }

    public static void setCurrentPasswordHash(String passwordHash) {
        currentPasswordHash = passwordHash;
    }

    @Override
    public Response handle(AsyncClient client, String[] parts, Scanner scanner) throws Exception {
        if (parts.length < 3) {
            return Response.error("Usage: login <login> <password>");
        }
        String login = parts[1];
        String password = parts[2];
        
        Request request = new Request(RequestType.COMMAND, "login");
        request.setLogin(login);
        request.setPassword(password);
        
        Map<String, Object> args = new HashMap<>();
        args.put("login", login);
        args.put("password", password);
        request.setArgs(args);
        
        Response resp = client.send(request);
        if (resp.isSuccess()) {
            currentLogin = login;
            currentPasswordHash = DatabaseManager.hashPassword(password);
        }
        return resp;
    }
}