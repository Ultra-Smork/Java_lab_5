package com.commands;

import com.auth.AuthorizationService;
import com.common.Response;
import com.utils.MinHeap;
import java.util.Map;

public class RemoveByBestAlbumCommand implements ServerCommand {
    @Override
    public Response execute(Map<String, Object> args) {
        if (args == null || args.get("album") == null) {
            return Response.error("Missing album name for remove_any_by_best_album command");
        }
        String albumName = (String) args.get("album");
        
        String login = (String) args.get("login");
        if (login == null) {
            login = AuthorizationService.getCurrentLogin();
        }
        
        if (login == null) {
            return Response.error("Authentication required. Please login first.");
        }
        
        MinHeap heap = MinHeap.getInstance();
        int count = heap.removeElByBestAlbumOwned(albumName, login);
        
        if (count > 0) {
            return Response.success("Removed " + count + " MusicBand(s) with best album: " + albumName);
        } else {
            return Response.error("No MusicBand found with best album: " + albumName);
        }
    }
}