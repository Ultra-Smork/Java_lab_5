package com.common;

import com.auth.AuthorizationService;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RequestBuilder {
    private final Request request;

    private RequestBuilder(Request.RequestType type, String command) {
        this.request = new Request(type, command);
    }

    private RequestBuilder(Request.RequestType type, Command command) {
        this.request = new Request(type, command);
    }

    public static RequestBuilder command(Command cmd) {
        return new RequestBuilder(Request.RequestType.COMMAND, cmd);
    }

    public static RequestBuilder command(String cmd) {
        return new RequestBuilder(Request.RequestType.COMMAND, cmd);
    }

    public static RequestBuilder health() {
        return new RequestBuilder(Request.RequestType.HEALTH, "health");
    }

    public RequestBuilder withArg(String key, Object value) {
        if (request.getArgs() == null) {
            request.setArgs(new HashMap<>());
        }
        request.getArgs().put(key, value);
        return this;
    }

    public RequestBuilder withArgs(Map<String, Object> args) {
        request.setArgs(args);
        return this;
    }

    public RequestBuilder withData(Serializable data) {
        request.setData(data);
        return this;
    }

    public RequestBuilder withAuth() {
        String login = AuthorizationService.getClientLogin();
        String passwordHash = AuthorizationService.getClientPasswordHash();

        if (login != null) {
            request.setLogin(login);
            request.setPassword(passwordHash);

            if (request.getArgs() == null) {
                request.setArgs(new HashMap<>());
            }
            request.getArgs().put("login", login);
            request.getArgs().put("passwordHash", passwordHash);
        }
        return this;
    }

    public Request build() {
        return request;
    }
}