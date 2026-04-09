package com.commands;

import com.auth.AuthorizationService;
import com.common.Response;
import java.util.Map;

public class RegisterCommand implements ServerCommand {
    @Override
    public Response execute(Map<String, Object> args) {
        if (args == null || args.get("login") == null || args.get("password") == null) {
            return Response.error("Usage: register <login> <password>");
        }
        String login = (String) args.get("login");
        String password = (String) args.get("password");
        
        var result = AuthorizationService.register(login, password);
        if (result.success()) {
            return Response.success(result.message());
        } else {
            return Response.error(result.message());
        }
    }
}