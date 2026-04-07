package com.client.handlers;

import com.client.network.AsyncClient;
import com.common.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CommandDispatcher {
    private static final Map<String, CommandHandler> handlers = new HashMap<>();
    
    static {
        handlers.put("register", new RegisterHandler());
        handlers.put("login", new LoginHandler());
        handlers.put("add", new AddHandler());
        handlers.put("add_if_min", new AddIfMinHandler());
        handlers.put("update", new UpdateHandler());
        handlers.put("execute_script", new ExecuteScriptHandler());
    }
    
    public static Response dispatch(AsyncClient client, String[] parts, Scanner scanner) throws Exception {
        String cmd = parts[0].toLowerCase();
        
        CommandHandler handler = handlers.get(cmd);
        if (handler != null) {
            return handler.handle(client, parts, scanner);
        }
        
        return new CommandWithArgsHandler().handle(client, parts, scanner);
    }
}