package com.commands;

import com.common.Response;
import com.utils.MinHeap;
import java.util.Map;

public class CountByParticipantsCommand implements ServerCommand {
    @Override
    public Response execute(Map<String, Object> args) {
        if (args == null || args.get("count") == null) {
            return Response.error("Missing count for count_by_number_of_participants command");
        }
        int count = ((Number) args.get("count")).intValue();
        int result = MinHeap.getInstance().countByNumberOfParticipants(count);
        return Response.success("Number of bands with " + count + " participants: " + result);
    }
}