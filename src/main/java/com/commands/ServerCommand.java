package com.commands;

import com.common.Response;
import java.util.Map;

public interface ServerCommand {
    Response execute(Map<String, Object> args);
}