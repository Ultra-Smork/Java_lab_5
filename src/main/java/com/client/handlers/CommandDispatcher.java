package com.client.handlers;

import com.auth.AuthorizationService;
import com.client.network.AsyncClient;
import com.common.Command;
import com.common.RequestBuilder;
import com.common.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CommandDispatcher {
    private static final Map<String, CommandHandler> handlers = new HashMap<>();
    
    static {
        handlers.put("add", new AddHandler());
        handlers.put("add_if_min", new AddIfMinHandler());
        handlers.put("update", new UpdateHandler());
        handlers.put("execute_script", new ExecuteScriptHandler());
    }
    
    public static Response dispatch(AsyncClient client, String[] parts, Scanner scanner) throws Exception {
        String cmd = parts[0].toLowerCase();
        
        if (cmd.equals("login")) {
            return handleLogin(client, parts);
        }
        
        if (cmd.equals("register")) {
            return handleRegister(client, parts);
        }
        
        if (cmd.equals("logout")) {
            return handleLogout(client);
        }
        
        CommandHandler handler = handlers.get(cmd);
        if (handler != null) {
            return handler.handle(client, parts, scanner);
        }
        
        return new CommandWithArgsHandler().handle(client, parts, scanner);
    }
    
    private static Response handleLogout(AsyncClient client) throws Exception {
        Response resp = client.send(RequestBuilder.command(Command.LOGOUT).build());
        AuthorizationService.clearClientSession();
        return resp;
    }
    
    private static Response handleLogin(AsyncClient client, String[] parts) throws Exception {
        if (parts.length < 3) {
            return Response.error("Usage: login <login> <password>");
        }
        String login = parts[1];
        String password = parts[2];
        
        Response resp = client.send(RequestBuilder.command(Command.LOGIN)
            .withArg("login", login)
            .withArg("password", password)
            .build());
        
        if (resp.isSuccess()) {
            String passwordHash = AuthorizationService.hashPassword(password);
            AuthorizationService.setClientSession(login, passwordHash);
        }
        return resp;
    }
    
    private static Response handleRegister(AsyncClient client, String[] parts) throws Exception {
        if (parts.length < 3) {
            return Response.error("Usage: register <login> <password>");
        }
        String login = parts[1];
        String password = parts[2];
        
        Response resp = client.send(RequestBuilder.command(Command.REGISTER)
            .withArg("login", login)
            .withArg("password", password)
            .build());
        
        if (resp.isSuccess()) {
            String passwordHash = AuthorizationService.hashPassword(password);
            AuthorizationService.setClientSession(login, passwordHash);
        }
        return resp;
    }
}