package com.client.handlers.script;

import com.auth.AuthorizationService;

public class ScriptAuthChecker {
    
    public boolean isAuthenticated() {
        return AuthorizationService.getClientLogin() != null;
    }
    
    public String requireAuth() {
        if (!isAuthenticated()) {
            return "Error: Please login first using 'login <login> <password>'";
        }
        return null;
    }
}