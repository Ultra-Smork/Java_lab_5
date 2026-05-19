package com.client.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ResourceBundle;

public class MainApplication extends Application {
    private static Stage primaryStage;
    private static String serverHost = "localhost";
    private static int serverPort = 8080;

    public static void setServerHost(String host) {
        serverHost = host;
    }

    public static void setServerPort(int port) {
        serverPort = port;
    }

    public static String getServerHost() {
        return serverHost;
    }

    public static int getServerPort() {
        return serverPort;
    }

    @Override
    public void start(Stage stage) throws IOException {
        LocalizationManager.init();
        primaryStage = stage;
        showLogin();
    }

    public static void showLogin() throws IOException {
        FXMLLoader loader = new FXMLLoader(
            MainApplication.class.getResource("/com/client/gui/LoginView.fxml"),
            LocalizationManager.getResourceBundle());
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle(LocalizationManager.get("login.title"));
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void showMain(String username) throws IOException {
        FXMLLoader loader = new FXMLLoader(
            MainApplication.class.getResource("/com/client/gui/MainView.fxml"),
            LocalizationManager.getResourceBundle());
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            System.err.println("Failed to load MainView.fxml:");
            e.printStackTrace();
            if (e.getCause() != null) {
                System.err.println("Caused by:");
                e.getCause().printStackTrace();
            }
            throw e;
        }
        Scene scene = new Scene(root);
        primaryStage.setTitle(LocalizationManager.get("main.title", username));
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static Stage getStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
