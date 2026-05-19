package com.commands;

import com.auth.AuthorizationService;
import com.common.Response;
import com.model.MusicBand;
import com.utils.MinHeap;
import java.util.Map;

public class RemoveByIdCommand implements ServerCommand {
    @Override
    public Response execute(Map<String, Object> args) {
        if (args == null || args.get("id") == null) {
            return Response.error("Missing ID for remove_by_id command");
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
        MusicBand band = heap.findById(id);
        if (band != null && band.getOwnerLogin() != null && !login.equals(band.getOwnerLogin())) {
            return Response.error("You can only remove your own bands");
        }
        
        boolean removed = heap.removeElById(id);
        if (removed) {
            return Response.success("MusicBand with id " + id + " has been removed.");
        } else {
            return Response.error("No MusicBand found with id " + id);
        }
    }
}