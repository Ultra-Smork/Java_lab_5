package com;

import com.server.DatabaseManager;
import com.server.ServerApp;
import com.client.ClientApp;
import com.client.gui.MainApplication;
import com.client.network.HealthChecker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class App {
    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_HOST = "localhost";

    private static Process serverProcess;

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

        try {
            startServerProcess(port);
        } catch (Exception e) {
            System.err.println("Failed to start server process: " + e.getMessage());
            return;
        }

        installSigintHandler();

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

        if (serverProcess != null && serverProcess.isAlive()) {
            serverProcess.destroy();
        }
    }

    private static void startServerProcess(int port) throws Exception {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");

        List<String> cmd = new ArrayList<>();
        cmd.add(javaBin);
        cmd.add("-cp");
        cmd.add(classpath);
        cmd.add("-Dapp.embedded=true");
        cmd.add("com.server.ServerApp");
        cmd.add(String.valueOf(port));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        pb.redirectInput(ProcessBuilder.Redirect.from(new File("/dev/null")));

        serverProcess = pb.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (serverProcess != null && serverProcess.isAlive()) {
                serverProcess.destroy();
            }
        }));
    }

    private static volatile long lastSigintTime = 0;
    private static final long FORCE_EXIT_THRESHOLD_MS = 3000;

    private static void installSigintHandler() {
        try {
            Class<?> signalClass = Class.forName("sun.misc.Signal");
            Class<?> handlerClass = Class.forName("sun.misc.SignalHandler");
            Object intSignal = signalClass.getConstructor(String.class).newInstance("INT");
            Object handler = java.lang.reflect.Proxy.newProxyInstance(
                handlerClass.getClassLoader(),
                new Class[]{handlerClass},
                (proxy, method, methodArgs) -> {
                    if (method.getName().equals("handle")) {
                        long now = System.currentTimeMillis();
                        if (now - lastSigintTime < FORCE_EXIT_THRESHOLD_MS) {
                            System.out.println("\nForce exiting...");
                            if (serverProcess != null && serverProcess.isAlive()) {
                                serverProcess.destroy();
                            }
                            System.exit(0);
                        }
                        lastSigintTime = now;
                        System.out.println("\n[Server stopped. GUI continues in offline mode.]");
                        System.out.println("[Close the window to exit, or press Ctrl+C again to force quit.]");
                    }
                    return null;
                }
            );
            signalClass.getMethod("handle", signalClass, handlerClass)
                .invoke(null, intSignal, handler);
        } catch (Exception e) {
            System.err.println("Warning: Could not install Ctrl+C handler: " + e.getMessage());
        }
    }
}
