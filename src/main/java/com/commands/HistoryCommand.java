package com.commands;

import com.common.Response;
import com.server.CommandHistory;
import com.utils.MinHeap;
import java.util.Map;
import java.util.stream.Collectors;

public class HistoryCommand implements ServerCommand {
    private final CommandHistory commandHistory;

    public HistoryCommand(CommandHistory commandHistory) {
        this.commandHistory = commandHistory;
    }

    @Override
    public Response execute(Map<String, Object> args) {
        if (commandHistory != null) {
            return Response.success(commandHistory.getHistoryString());
        }
        return Response.success(MinHeap.getInstance().getMetadataHistory().stream()
            .collect(Collectors.joining("\n")));
    }
}