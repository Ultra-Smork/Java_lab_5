package com.commands;

import com.common.Response;
import com.model.MusicBand;
import com.utils.MinHeap;
import java.util.Map;

public class AverageParticipantsCommand implements ServerCommand {
    @Override
    public Response execute(Map<String, Object> args) {
        double avg = MinHeap.getInstance().getAllElements().stream()
            .mapToInt(MusicBand::getNumberOfParticipants)
            .average()
            .orElse(0.0);
        return Response.success(String.format("%.2f", avg));
    }
}