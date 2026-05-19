package com.auth;

public class AuthResult {
    private final boolean success;
    private final String message;
    private final String login;
    private final String passwordHash;

    public AuthResult(boolean success, String message, String login, String passwordHash) {
        this.success = success;
        this.message = message;
        this.login = login;
        this.passwordHash = passwordHash;
    }

    public boolean success() {
        return success;
    }

    public String message() {
        return message;
    }

    public String login() {
        return login;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public static AuthResult success(String login, String passwordHash) {
        return new AuthResult(true, "Success", login, passwordHash);
    }

    public static AuthResult error(String message) {
        return new AuthResult(false, message, null, null);
    }

    public static AuthResult success(String message, String login, String passwordHash) {
        return new AuthResult(true, message, login, passwordHash);
    }
}