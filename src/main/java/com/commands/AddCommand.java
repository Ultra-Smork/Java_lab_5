package com.commands;

import com.auth.AuthorizationService;
import com.common.Response;
import com.model.MusicBand;
import com.utils.MinHeap;
import java.util.Map;

public class AddCommand implements ServerCommand {
    @Override
    public Response execute(Map<String, Object> args) {
        if (args == null || args.get("band") == null) {
            return Response.error("Missing band data for add command");
        }
        MusicBand band = (MusicBand) args.get("band");
        
        String login = (String) args.get("login");
        String passwordHash = (String) args.get("passwordHash");
        
        if (login == null) {
            login = AuthorizationService.getCurrentLogin();
            passwordHash = AuthorizationService.getCurrentPasswordHash();
        }
        
        if (login == null) {
            return Response.error("Authentication required. Please login first.");
        }
        
        band.setOwnerLogin(login);
        if (passwordHash != null) {
            band.setOwnerPasswordHash(passwordHash);
        }
        
        MinHeap.getInstance().insert(band);
        return Response.success("Added new band:\n" + band);
    }
}