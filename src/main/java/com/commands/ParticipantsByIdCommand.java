package com.commands;

import com.common.Response;
import com.model.MusicBand;
import com.utils.MinHeap;
import java.util.Map;

public class ParticipantsByIdCommand implements ServerCommand {
    @Override
    public Response execute(Map<String, Object> args) {
        if (args == null || args.get("id") == null) {
            return Response.error("Missing ID for participants_by_id command");
        }
        Long id = ((Number) args.get("id")).longValue();
        MusicBand band = MinHeap.getInstance().findById(id);
        if (band == null) {
            return Response.error("No MusicBand found with id " + id);
        }
        return Response.success("Band: " + band.getName() + ", Participants: " + band.getNumberOfParticipants());
    }
}