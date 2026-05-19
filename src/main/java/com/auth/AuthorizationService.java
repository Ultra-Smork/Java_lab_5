package com.auth;

import com.server.DatabaseManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthorizationService {

    private static final ThreadLocal<String> serverLogin = new ThreadLocal<>();
    private static final ThreadLocal<String> serverPasswordHash = new ThreadLocal<>();

    private static String clientLogin;
    private static String clientPasswordHash;

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-384");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-384 not available", e);
        }
    }

    public static AuthResult login(String login, String password) {
        String passwordHash = hashPassword(password);
        if (DatabaseManager.validateUser(login, passwordHash)) {
            serverLogin.set(login);
            serverPasswordHash.set(passwordHash);
            return AuthResult.success("Login successful: " + login, login, passwordHash);
        }
        return AuthResult.error("Invalid login or password");
    }

    public static AuthResult register(String login, String password) {
        if (DatabaseManager.userExists(login)) {
            return AuthResult.error("User already exists: " + login);
        }
        String passwordHash = hashPassword(password);
        if (DatabaseManager.registerUser(login, passwordHash)) {
            serverLogin.set(login);
            serverPasswordHash.set(passwordHash);
            return AuthResult.success("User registered successfully: " + login, login, passwordHash);
        }
        return AuthResult.error("Failed to register user");
    }

    public static void clearSession() {
        serverLogin.remove();
        serverPasswordHash.remove();
    }

    public static String getCurrentLogin() {
        return serverLogin.get();
    }

    public static String getCurrentPasswordHash() {
        return serverPasswordHash.get();
    }

    public static boolean isAuthenticated() {
        return serverLogin.get() != null;
    }

    public static void setClientSession(String login, String passwordHash) {
        clientLogin = login;
        clientPasswordHash = passwordHash;
    }

    public static void clearClientSession() {
        clientLogin = null;
        clientPasswordHash = null;
    }

    public static String getClientLogin() {
        return clientLogin;
    }

    public static String getClientPasswordHash() {
        return clientPasswordHash;
    }

    public static boolean isClientAuthenticated() {
        return clientLogin != null;
    }
}