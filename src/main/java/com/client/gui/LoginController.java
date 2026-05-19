package com.client.gui;

import com.auth.AuthorizationService;
import com.client.network.AsyncClient;
import com.common.Command;
import com.common.RequestBuilder;
import com.common.Response;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label titleLabel;
    @FXML private Label lblUsername;
    @FXML private Label lblPassword;
    @FXML private Label errorLabel;
    @FXML private Button loginBtn;
    @FXML private Button registerBtn;

    private AsyncClient client;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @FXML
    public void initialize() {
        applyLabels();
        LocalizationManager.addLocaleChangeListener(locale -> applyLabels());
    }

    private void applyLabels() {
        titleLabel.setText(LocalizationManager.get("login.title"));
        lblUsername.setText(LocalizationManager.get("login.username"));
        lblPassword.setText(LocalizationManager.get("login.password"));
        usernameField.setPromptText(LocalizationManager.get("login.usernamePrompt"));
        passwordField.setPromptText(LocalizationManager.get("login.passwordPrompt"));
        loginBtn.setText(LocalizationManager.get("login.loginBtn"));
        registerBtn.setText(LocalizationManager.get("login.registerBtn"));
    }

    @FXML
    private void onLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError(LocalizationManager.get("login.error"));
            return;
        }

        setButtonsEnabled(false);
        errorLabel.setText("");

        executor.submit(() -> {
            try {
                if (client == null || !client.isConnected()) {
                    client = new AsyncClient(MainApplication.getServerHost(), MainApplication.getServerPort());
                    if (!client.connect()) {
                        Platform.runLater(() -> showError(LocalizationManager.get("login.connectionError")));
                        return;
                    }
                }

                Response resp = client.send(RequestBuilder.command(Command.LOGIN)
                    .withArg("login", username)
                    .withArg("password", password)
                    .build());

                Platform.runLater(() -> {
                    if (resp.isSuccess()) {
                        String hash = AuthorizationService.hashPassword(password);
                        AuthorizationService.setClientSession(username, hash);
                        try {
                            MainApplication.showMain(username);
                            client.disconnect();
                        } catch (IOException e) {
                            showError(LocalizationManager.get("login.windowError"));
                        }
                    } else {
                        showError(resp.getError() != null ? resp.getError() : LocalizationManager.get("login.error"));
                        setButtonsEnabled(true);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError(LocalizationManager.get("login.error"));
                    setButtonsEnabled(true);
                });
            }
        });
    }

    @FXML
    private void onRegister(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError(LocalizationManager.get("login.register.error"));
            return;
        }

        setButtonsEnabled(false);
        errorLabel.setText("");

        executor.submit(() -> {
            try {
                if (client == null || !client.isConnected()) {
                    client = new AsyncClient(MainApplication.getServerHost(), MainApplication.getServerPort());
                    if (!client.connect()) {
                        Platform.runLater(() -> showError(LocalizationManager.get("login.connectionError")));
                        return;
                    }
                }

                Response resp = client.send(RequestBuilder.command(Command.REGISTER)
                    .withArg("login", username)
                    .withArg("password", password)
                    .build());

                Platform.runLater(() -> {
                    if (resp.isSuccess()) {
                        String hash = AuthorizationService.hashPassword(password);
                        AuthorizationService.setClientSession(username, hash);
                        try {
                            MainApplication.showMain(username);
                            client.disconnect();
                        } catch (IOException e) {
                            showError(LocalizationManager.get("login.windowError"));
                        }
                    } else {
                        showError(resp.getError() != null ? resp.getError() : LocalizationManager.get("login.register.error"));
                        setButtonsEnabled(true);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError(LocalizationManager.get("login.register.error"));
                    setButtonsEnabled(true);
                });
            }
        });
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
    }

    private void setButtonsEnabled(boolean enabled) {
        usernameField.setDisable(!enabled);
        passwordField.setDisable(!enabled);
    }

    public AsyncClient getClient() {
        return client;
    }
}
