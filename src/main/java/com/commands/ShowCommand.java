package com.commands;

import com.common.Response;
import com.model.MusicBand;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ShowCommand implements ServerCommand {
    @Override
    public Response execute(Map<String, Object> args) {
        var heap = com.utils.MinHeap.getInstance();
        List<MusicBand> bands = heap.getAllElements().stream()
            .sorted(java.util.Comparator.comparing(MusicBand::getName, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())))
            .collect(Collectors.toList());
        String result = bands.stream()
            .map(MusicBand::toString)
            .collect(Collectors.joining("\n"));
        Response response = Response.success(result);
        response.setData((Serializable) bands);
        return response;
    }
}