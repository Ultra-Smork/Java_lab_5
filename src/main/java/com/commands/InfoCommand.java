package com.commands;

import com.common.Response;
import com.server.DatabaseManager;
import java.time.LocalDateTime;
import java.util.Map;

public class InfoCommand implements ServerCommand {
    @Override
    public Response execute(Map<String, Object> args) {
        int count = DatabaseManager.getBandCount();
        String info = String.format("Type: MusicBand (PostgreSQL)\nInitialization date: %s\nElements: %d",
            LocalDateTime.now().toString(),
            count);
        return Response.success(info);
    }
}