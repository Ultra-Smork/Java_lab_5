package com.commands;

import com.auth.AuthorizationService;
import com.common.Response;
import java.util.Map;

public class LogoutCommand implements ServerCommand {
    @Override
    public Response execute(Map<String, Object> args) {
        String currentLogin = AuthorizationService.getCurrentLogin();
        
        if (currentLogin == null) {
            return Response.error("No active session to logout");
        }
        
        AuthorizationService.clearSession();
        return Response.success("Logged out successfully");
    }
}