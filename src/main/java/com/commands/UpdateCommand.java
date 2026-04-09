package com.commands;

import com.auth.AuthorizationService;
import com.common.Response;
import com.model.MusicBand;
import com.utils.MinHeap;
import java.util.Map;

public class UpdateCommand implements ServerCommand {
    @Override
    public Response execute(Map<String, Object> args) {
        MinHeap heap = MinHeap.getInstance();

        if (args == null || args.get("band") == null) {
            if (args == null || args.get("id") == null) {
                return Response.error("Missing ID for update command");
            }
            Long id = ((Number) args.get("id")).longValue();
            MusicBand existing = heap.findById(id);
            if (existing == null) {
                return Response.error("No MusicBand found with id " + id);
            }
            return Response.withData(existing);
        }

        MusicBand band = (MusicBand) args.get("band");
        if (args.get("id") != null) {
            Long id = ((Number) args.get("id")).longValue();
            band.setId(id);
        }
        
        String login = (String) args.get("login");
        String passwordHash = (String) args.get("passwordHash");
        
        if (login == null) {
            login = AuthorizationService.getCurrentLogin();
            passwordHash = AuthorizationService.getCurrentPasswordHash();
        }
        
        if (login == null) {
            return Response.error("Authentication required. Please login first.");
        }
        
        MusicBand existing = heap.findById(band.getId());
        if (existing != null && existing.getOwnerLogin() != null) {
            if (!login.equals(existing.getOwnerLogin())) {
                return Response.error("You can only update your own bands");
            }
        }
        
        band.setOwnerLogin(login);
        if (passwordHash != null) {
            band.setOwnerPasswordHash(passwordHash);
        }
        
        heap.updateElement(band);
        return Response.success("MusicBand updated successfully!\n" + band);
    }
}