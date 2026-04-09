package com.commands;

import com.auth.AuthorizationService;
import com.common.Response;
import com.utils.MinHeap;
import java.util.Map;

public class RemoveGreaterCommand implements ServerCommand {
    @Override
    public Response execute(Map<String, Object> args) {
        if (args == null || args.get("id") == null) {
            return Response.error("Missing ID for remove_greater command");
        }
        Long id = ((Number) args.get("id")).longValue();
        
        String login = (String) args.get("login");
        if (login == null) {
            login = AuthorizationService.getCurrentLogin();
        }
        
        if (login == null) {
            return Response.error("Authentication required. Please login first.");
        }
        
        MinHeap heap = MinHeap.getInstance();
        int count = heap.removeElementsGreaterThanIdOwned(id, login);
        return Response.success("Removed " + count + " elements with ID greater than " + id);
    }
}