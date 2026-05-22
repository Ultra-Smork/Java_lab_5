package com;

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
        String jdbcUrl = null;
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
            } else if (args[i].equals("--jdbc") && i + 1 < args.length) {
                jdbcUrl = args[++i];
            } else {
                remaining.add(args[i]);
            }
        }

        String[] filteredArgs = remaining.toArray(new String[0]);

        if (filteredArgs.length == 0) {
            MainApplication.setServerHost(host);
            MainApplication.setServerPort(port);
            MainApplication.main(filteredArgs);
            return;
        }

        String mode = filteredArgs[0];

        switch (mode) {
            case "--dev": {
                final int devPort = port;
                final String devJdbc = jdbcUrl;
                Thread serverThread = new Thread(() -> {
                    try {
                        ServerApp.start(devPort, devJdbc);
                    } catch (Exception e) {
                        System.err.println("Server thread failed: " + e.getMessage());
                        e.printStackTrace();
                    }
                }, "server-daemon");
                serverThread.setDaemon(true);
                serverThread.start();

                try { Thread.sleep(2500); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                MainApplication.setServerHost(host);
                MainApplication.setServerPort(port);
                MainApplication.main(filteredArgs);
                break;
            }

            case "--server": {
                ServerApp.start(port, jdbcUrl);
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
                System.out.println("  java -jar app.jar                     Start GUI client (connects to localhost:8080)");
                System.out.println("  java -jar app.jar --dev [--port PORT] [--jdbc URL]  Start server + GUI client");
                System.out.println("  java -jar app.jar --server [--port PORT] [--jdbc URL]  Start server");
                System.out.println("  java -jar app.jar --console [--host HOST] [--port PORT]  Start CLI client");
                System.out.println("  java -jar app.jar --check-health [--host HOST] [--port PORT]  Check server health");
                System.out.println();
                System.out.println("JDBC URL format:");
                System.out.println("  jdbc:postgresql://HOST:PORT/DATABASE?user=USER&password=PASS");
                System.out.println("  Example: --jdbc \"jdbc:postgresql://localhost:5432/mydb?user=admin&password=secret\"");
                System.exit(1);
            }
        }
    }
}
