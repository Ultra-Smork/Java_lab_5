package com;

import com.server.DatabaseManager;
import com.server.ServerApp;
import com.client.ClientApp;
import com.client.gui.MainApplication;
import com.client.network.HealthChecker;

import java.util.ArrayList;
import java.util.List;

public class App {
    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_HOST = "localhost";

    public static void main(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;
        boolean localMode = false;
        List<String> remaining = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--host") && i + 1 < args.length) {
                host = args[++i];
            } else if (args[i].equals("--port") && i + 1 < args.length) {
                try {
                    port = Integer.parseInt(args[++i]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid port: " + args[i]);
                    System.exit(1);
                }
            } else if (args[i].equals("--local")) {
                localMode = true;
            } else {
                remaining.add(args[i]);
            }
        }

        String[] filteredArgs = remaining.toArray(new String[0]);

        if (localMode) {
            startLocal(port, filteredArgs);
            return;
        }

        if (filteredArgs.length == 0) {
            MainApplication.setServerHost(host);
            MainApplication.setServerPort(port);
            MainApplication.main(filteredArgs);
            return;
        }

        String mode = filteredArgs[0];

        switch (mode) {
            case "--server": {
                ServerApp.start(port);
                break;
            }

            case "--console": {
                ClientApp.start(host, port);
                break;
            }

            case "--check-health": {
                HealthChecker.main(new String[]{"--host", host, "--port", String.valueOf(port)});
                break;
            }

            default: {
                System.out.println("Usage:");
                System.out.println("  java -jar app.jar [--host HOST] [--port PORT]");
                System.out.println("  java -jar app.jar --console [--host HOST] [--port PORT]");
                System.out.println("  java -jar app.jar --server [--port PORT]");
                System.out.println("  java -jar app.jar --check-health [--host HOST] [--port PORT]");
                System.out.println("  java -jar app.jar --local [--port PORT] [--console]");
                System.exit(1);
            }
        }
    }

    private static void startLocal(int port, String[] args) {
        System.out.println("Starting in local mode with embedded H2 database...");

        DatabaseManager.setEmbeddedMode(true);

        Thread serverThread = new Thread(() -> {
            try {
                ServerApp.start(port);
            } catch (Exception e) {
                System.err.println("Local server failed: " + e.getMessage());
                System.exit(1);
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean consoleMode = args.length > 0 && args[0].equals("--console");

        if (consoleMode) {
            ClientApp.start("localhost", port);
        } else {
            MainApplication.setServerHost("localhost");
            MainApplication.setServerPort(port);
            MainApplication.main(new String[0]);
        }
    }
}
