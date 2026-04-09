package com.commands;

import com.auth.AuthorizationService;
import com.common.Response;
import com.utils.MinHeap;
import java.util.Map;

public class ClearCommand implements ServerCommand {
    @Override
    public Response execute(Map<String, Object> args) {
        String login = (String) args.get("login");
        if (login == null) {
            login = AuthorizationService.getCurrentLogin();
        }
        
        if (login == null) {
            return Response.error("Authentication required to clear collection");
        }
        
        int count = MinHeap.getInstance().clearOwned(login);
        return Response.success("Cleared " + count + " of your bands");
    }
}