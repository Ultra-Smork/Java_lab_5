package com.commands;

import com.common.Response;
import com.model.MusicBand;
import com.server.DatabaseManager;

import java.util.Map;

public class ShowCommand implements ServerCommand {
    @Override
    public Response execute(Map<String, Object> args) {
        var heap = com.utils.MinHeap.getInstance();
        var result = heap.getAllElements().stream()
            .sorted(java.util.Comparator.comparing(MusicBand::getName, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())))
            .map(MusicBand::toString)
            .collect(java.util.stream.Collectors.joining("\n"));
        return Response.success(result);
    }
}