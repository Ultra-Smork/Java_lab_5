package com.client.gui;

import com.auth.AuthorizationService;
import com.client.handlers.script.ScriptCommandExecutor;
import com.client.network.AsyncClient;
import com.common.Command;
import com.common.RequestBuilder;
import com.common.Response;
import com.model.MusicBand;
import com.utils.CollectionFileManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainController {
    @FXML private Label userLabel;
    @FXML private ComboBox<String> languageCombo;
    @FXML private TableView<MusicBand> bandTable;
    @FXML private Label statusLabel;
    @FXML private Button logoutBtn;

    // Columns
    @FXML private TableColumn<MusicBand, Long> colId;
    @FXML private TableColumn<MusicBand, String> colName;
    @FXML private TableColumn<MusicBand, Long> colX;
    @FXML private TableColumn<MusicBand, Integer> colY;
    @FXML private TableColumn<MusicBand, Long> colParticipants;
    @FXML private TableColumn<MusicBand, String> colGenre;
    @FXML private TableColumn<MusicBand, String> colAlbum;
    @FXML private TableColumn<MusicBand, Integer> colSales;
    @FXML private TableColumn<MusicBand, String> colOwner;
    @FXML private TableColumn<MusicBand, java.util.Date> colCreated;
    
    // Filters
    @FXML private TextField filterId;
    @FXML private TextField filterName;
    @FXML private TextField filterX;
    @FXML private TextField filterY;
    @FXML private TextField filterParticipants;
    @FXML private TextField filterGenre;
    @FXML private TextField filterAlbum;
    @FXML private TextField filterSales;
    @FXML private TextField filterOwner;
    @FXML private TextField filterCreated;

    @FXML private CollectionCanvas collectionCanvas;
    @FXML private StackPane canvasContainer;
    @FXML private SplitPane splitPane;

    private BandTableManager tableManager;
    private ScheduledExecutorService scheduler;
    private AsyncClient client;

    @FXML
    public void initialize() {
        String username = AuthorizationService.getClientLogin();
        if (username != null) {
            userLabel.setText(LocalizationManager.get("main.user", username));
        }

        // Setup i18n
        languageCombo.setItems(FXCollections.observableArrayList(
            "English", "Русский", "Српски", "Italiano", "Español (NI)"
        ));
        languageCombo.setValue(getLanguageName(LocalizationManager.getLocale().getLanguage()));
        languageCombo.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) setText(null);
                    else setText(item);
                    setStyle("-fx-text-fill: white; -fx-background-color: #3d3d3d;");
                }
            };
            return cell;
        });
        languageCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item);
                setStyle("-fx-text-fill: white; -fx-background-color: #3d3d3d;");
            }
        });
        LocalizationManager.addLocaleChangeListener(l -> updateLabels());

        // Setup Table
        tableManager = new BandTableManager();
        tableManager.attachTable(bandTable);
        
        tableManager.addColumn("table.id", "id", Long.class, colId, filterId, 70);
        tableManager.addColumn("table.name", "name", String.class, colName, filterName, 100);
        tableManager.addColumn("table.x", "x", Long.class, colX, filterX, 50);
        tableManager.addColumn("table.y", "y", Integer.class, colY, filterY, 50);
        tableManager.addColumn("table.participants", "numberOfParticipants", Long.class, colParticipants, filterParticipants, 70);
        tableManager.addColumn("table.genre", "genre", com.model.MusicGenre.class, colGenre, filterGenre, 80);
        tableManager.addColumn("table.album", "albumName", String.class, colAlbum, filterAlbum, 100);
        tableManager.addColumn("table.sales", "albumSales", Integer.class, colSales, filterSales, 60);
        tableManager.addColumn("table.owner", "ownerLogin", String.class, colOwner, filterOwner, 80);
        tableManager.addColumn("table.created", "creationDate", java.util.Date.class, colCreated, filterCreated, 100);

        bandTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                MusicBand selected = bandTable.getSelectionModel().getSelectedItem();
                if (selected != null) executeRemove(selected.getId());
            }
        });

        // Bind canvas to container size
        collectionCanvas.widthProperty().bind(canvasContainer.widthProperty());
        collectionCanvas.heightProperty().bind(canvasContainer.heightProperty());

        // Keep split pane divider at 50% on any resize
        splitPane.widthProperty().addListener((obs, ov, nv) ->
            splitPane.setDividerPositions(0.5));

        // Start polling
        client = new AsyncClient(MainApplication.getServerHost(), MainApplication.getServerPort());
        startPolling();
        updateLabels();
    }

    private void startPolling() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (!client.isConnected()) {
                    client.connect();
                }
                
                Response response = client.send(RequestBuilder.command(Command.SHOW)
                    .withAuth()
                    .build());
                    
                if (response.isSuccess() && response.getData() instanceof List) {
                    List<MusicBand> bands = (List<MusicBand>) response.getData();
                    Platform.runLater(() -> {
                        tableManager.updateBands(bands);
                        collectionCanvas.updateBands(bands);
                        statusLabel.setText(LocalizationManager.get("main.connected", bands.size()));
                    });
                }
            } catch (Exception e) {
                String msg = toUserMessage(e);
                Platform.runLater(() -> statusLabel.setText(msg));
            }
        }, 0, 3, TimeUnit.SECONDS);
    }

    @FXML
    private void onLanguageChange(ActionEvent event) {
        String lang = languageCombo.getValue();
        switch (lang) {
            case "English": LocalizationManager.setLocale(LocalizationManager.ENGLISH); break;
            case "Русский": LocalizationManager.setLocale(LocalizationManager.RUSSIAN); break;
            case "Српски": LocalizationManager.setLocale(LocalizationManager.SERBIAN); break;
            case "Italiano": LocalizationManager.setLocale(LocalizationManager.ITALIAN); break;
            case "Español (NI)": LocalizationManager.setLocale(LocalizationManager.SPANISH_NICARAGUA); break;
        }
    }

    private String getLanguageName(String langCode) {
        switch (langCode) {
            case "en": return "English";
            case "sr": return "Српски";
            case "it": return "Italiano";
            case "es": return "Español (NI)";
            default: return "Русский";
        }
    }

    @FXML private Button btnAdd;
    @FXML private Button btnAddIfMin;
    @FXML private Button btnUpdate;
    @FXML private Button btnRemoveById;
    @FXML private Button btnRemoveGreater;
    @FXML private Button btnRemoveBestAlbum;
    @FXML private Button btnClear;
    @FXML private Button btnInfo;
    @FXML private Button btnHelp;
    @FXML private Button btnHistory;

    @FXML private Button btnExecuteScript;

    private void updateLabels() {
        String username = AuthorizationService.getClientLogin();
        if (username != null) {
            userLabel.setText(LocalizationManager.get("main.user", username));
            MainApplication.getStage().setTitle(LocalizationManager.get("main.title", username));
        }
        logoutBtn.setText(LocalizationManager.get("main.logout"));
        tableManager.updateLocale();
        
        btnAdd.setText(LocalizationManager.get("commands.add"));
        btnAddIfMin.setText(LocalizationManager.get("commands.addIfMin"));
        btnUpdate.setText(LocalizationManager.get("commands.update"));
        btnRemoveById.setText(LocalizationManager.get("commands.removeById"));
        btnRemoveGreater.setText(LocalizationManager.get("commands.removeGreater"));
        btnRemoveBestAlbum.setText(LocalizationManager.get("commands.removeBestAlbum"));
        btnClear.setText(LocalizationManager.get("commands.clear"));
        btnInfo.setText(LocalizationManager.get("commands.info"));
        btnHelp.setText(LocalizationManager.get("commands.help"));
        btnHistory.setText(LocalizationManager.get("commands.history"));
        btnExecuteScript.setText(LocalizationManager.get("commands.executeScript"));
    }

    @FXML
    private void onLogout(ActionEvent event) {
        if (scheduler != null) scheduler.shutdownNow();
        AuthorizationService.clearClientSession();
        try {
            MainApplication.showLogin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onTableClick(MouseEvent event) {
        if (event.getClickCount() == 2 && !bandTable.getSelectionModel().isEmpty()) {
            MusicBand selected = bandTable.getSelectionModel().getSelectedItem();
            openEditDialog(selected);
        }
    }

    private void openEditDialog(MusicBand band) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/com/client/gui/EditView.fxml"));
            Parent root = loader.load();
            BandEditController controller = loader.getController();
            controller.initData(band, client);
            
            Stage stage = new Stage();
            stage.setTitle(LocalizationManager.get("edit.title", band.getName()));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(MainApplication.getStage());
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openAddDialog(boolean isAddIfMin) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/com/client/gui/EditView.fxml"));
            Parent root = loader.load();
            BandEditController controller = loader.getController();
            controller.initAddMode(client, isAddIfMin);

            Stage stage = new Stage();
            stage.setTitle(LocalizationManager.get(isAddIfMin ? "dialog.addIfMin.title" : "dialog.add.title"));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(MainApplication.getStage());
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Command handlers
    @FXML private void onAdd(ActionEvent event) {
        openAddDialog(false);
    }
    @FXML private void onAddIfMin(ActionEvent event) {
        openAddDialog(true);
    }
    @FXML private void onUpdate(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(LocalizationManager.get("dialog.update.title"));
        dialog.setHeaderText(LocalizationManager.get("dialog.update.header"));
        dialog.setContentText(LocalizationManager.get("dialog.update.content"));
        dialog.showAndWait().ifPresent(input -> {
            String trimmed = input.trim();
            try {
                long id = Long.parseLong(trimmed);
                if (id <= 0) {
                    showAlert(Alert.AlertType.WARNING, LocalizationManager.get("validation.id.positive"));
                    return;
                }
                new Thread(() -> {
                    try {
                        Response r = client.send(RequestBuilder.command(Command.SELECT)
                            .withArg("id", id)
                            .withAuth()
                            .build());
                        if (r.isSuccess() && r.getData() instanceof MusicBand mb) {
                            Platform.runLater(() -> openEditDialog(mb));
                        } else {
                            Platform.runLater(() -> {
                                Alert a = new Alert(Alert.AlertType.ERROR,
                                    r.getError() != null ? r.getError() : LocalizationManager.get("dialog.update.notFound"));
                                a.show();
                            });
                        }
                    } catch (Exception e) {
                        String err = toUserMessage(e);
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, err));
                    }
                }).start();
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, LocalizationManager.get("validation.id.format"));
            }
        });
    }
    @FXML private void onRemoveById(ActionEvent event) {
        MusicBand selected = bandTable.getSelectionModel().getSelectedItem();
        if (selected != null) executeRemove(selected.getId());
    }
    @FXML private void onRemoveGreater(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(LocalizationManager.get("dialog.removeGreater.title"));
        dialog.setHeaderText(LocalizationManager.get("dialog.removeGreater.header"));
        dialog.setContentText(LocalizationManager.get("dialog.removeGreater.content"));
        dialog.showAndWait().ifPresent(input -> {
            String trimmed = input.trim();
            try {
                long id = Long.parseLong(trimmed);
                if (id <= 0) {
                    showAlert(Alert.AlertType.WARNING, LocalizationManager.get("validation.id.positive"));
                    return;
                }
                new Thread(() -> {
                    try {
                        Response r = client.send(RequestBuilder.command(Command.REMOVE_GREATER)
                            .withArg("id", id)
                            .withAuth()
                            .build());
                        if (!r.isSuccess()) {
                            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, r.getError()));
                        }
                    } catch (Exception e) {
                        String err = toUserMessage(e);
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, err));
                    }
                }).start();
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, LocalizationManager.get("validation.id.format"));
            }
        });
    }
    @FXML private void onRemoveBestAlbum(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(LocalizationManager.get("dialog.removeAlbum.title"));
        dialog.setHeaderText(LocalizationManager.get("dialog.removeAlbum.header"));
        dialog.setContentText(LocalizationManager.get("dialog.removeAlbum.content"));
        dialog.showAndWait().ifPresent(input -> {
            String name = input.trim();
            if (name.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, LocalizationManager.get("validation.album.name.empty"));
                return;
            }
            new Thread(() -> {
                try {
                    Response r = client.send(RequestBuilder.command(Command.REMOVE_ANY_BY_BEST_ALBUM)
                        .withArg("album", name)
                        .withAuth()
                        .build());
                    if (!r.isSuccess()) {
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, r.getError()));
                    }
                } catch (Exception e) {
                    String err = toUserMessage(e);
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, err));
                }
            }).start();
        });
    }
    @FXML private void onClear(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, LocalizationManager.get("delete.confirm"));
        confirm.showAndWait().ifPresent(r -> {
            if (r != ButtonType.OK) return;
            new Thread(() -> {
                try {
                    Response resp = client.send(RequestBuilder.command(Command.CLEAR)
                        .withAuth()
                        .build());
                    if (!resp.isSuccess()) {
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, resp.getError()));
                    }
                } catch (Exception e) {
                    String err = toUserMessage(e);
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, err));
                }
            }).start();
        });
    }
    @FXML private void onInfo(ActionEvent event) {
        new Thread(() -> {
            try {
                Response r = client.send(RequestBuilder.command(Command.INFO)
                    .withAuth()
                    .build());
                Platform.runLater(() -> {
                    String content = r.isSuccess() ? r.getResult() : r.getError();
                    Alert a = new Alert(r.isSuccess() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                        content != null ? content : LocalizationManager.get("dialog.info.result"));
                    a.show();
                });
            } catch (Exception e) {
                String err = toUserMessage(e);
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, err));
            }
        }).start();
    }
    @FXML private void onHelp(ActionEvent event) {
        new Thread(() -> {
            try {
                Response r = client.send(RequestBuilder.command(Command.HELP)
                    .withAuth()
                    .build());
                Platform.runLater(() -> {
                    String content = r.isSuccess() ? r.getResult() : r.getError();
                    Alert a = new Alert(r.isSuccess() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                        content != null ? content : LocalizationManager.get("help.title"));
                    a.setTitle(LocalizationManager.get("help.title"));
                    a.show();
                });
            } catch (Exception e) {
                String err = toUserMessage(e);
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, err));
            }
        }).start();
    }
    @FXML private void onHistory(ActionEvent event) {
        new Thread(() -> {
            try {
                Response r = client.send(RequestBuilder.command(Command.HISTORY)
                    .withAuth()
                    .build());
                Platform.runLater(() -> {
                    String content = r.isSuccess() ? r.getResult() : r.getError();
                    Alert a = new Alert(r.isSuccess() ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                        content != null ? content : LocalizationManager.get("dialog.history.result"));
                    a.setTitle(LocalizationManager.get("dialog.history.title"));
                    a.show();
                });
            } catch (Exception e) {
                String err = toUserMessage(e);
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, err));
            }
        }).start();
    }

    private void executeRemove(long id) {
        new Thread(() -> {
            try {
                Response r = client.send(RequestBuilder.command(Command.REMOVE_BY_ID)
                    .withArg("id", id)
                    .withAuth()
                    .build());
                if (!r.isSuccess()) {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, r.getError()));
                }
            } catch (Exception e) {
                String err = toUserMessage(e);
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, err));
            }
        }).start();
    }

    @FXML
    private void onExecuteScript(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(LocalizationManager.get("dialog.script.title"));
        dialog.setHeaderText(LocalizationManager.get("dialog.script.header"));
        dialog.setContentText(LocalizationManager.get("dialog.script.content"));
        dialog.showAndWait().ifPresent(input -> {
            String path = input.trim();
            if (path.isEmpty()) return;
            String resolved = CollectionFileManager.resolvePath(path);
            if (resolved == null) {
                showAlert(Alert.AlertType.ERROR, LocalizationManager.get("dialog.script.invalidPath"));
                return;
            }
            List<String> lines = loadScriptLines(resolved);
            if (lines == null) {
                showAlert(Alert.AlertType.ERROR, LocalizationManager.get("dialog.script.notFound", path));
                return;
            }
            if (lines.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, LocalizationManager.get("dialog.script.empty"));
                return;
            }

            List<String> results = new ArrayList<>();
            new Thread(() -> {
                ScriptCommandExecutor executor = new ScriptCommandExecutor();
                int i = 0;
                while (i < lines.size()) {
                    String cmdLine = lines.get(i);
                    String[] parts = cmdLine.split("\\s+");
                    String cmd = parts[0].toLowerCase();
                    int skip = getScriptSkipCount(cmd);
                    if (i + skip > lines.size()) {
                        results.add(LocalizationManager.get("dialog.script.resultMissing", i + 1, cmd, skip));
                        i++;
                        continue;
                    }
                    try {
                        String result = executor.execute(client, cmd, lines.subList(i, i + skip), 0);
                        results.add(LocalizationManager.get("dialog.script.resultLine", i + 1, result));
                    } catch (Exception e) {
                        results.add(LocalizationManager.get("dialog.script.resultError", i + 1, e.getMessage()));
                    }
                    i += Math.max(skip, 1);
                }
                Platform.runLater(() -> showScriptResults(results));
            }).start();
        });
    }

    private int getScriptSkipCount(String cmd) {
        return switch (cmd) {
            case "add" -> 9;
            case "add_if_min", "update" -> 10;
            case "remove_by_id", "remove_greater", "count_by_number_of_participants", "participants_by_id" -> 2;
            default -> 1;
        };
    }

    private List<String> loadScriptLines(String path) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    lines.add(trimmed);
                }
            }
            return lines;
        } catch (Exception e) {
            return null;
        }
    }

    private void showScriptResults(List<String> results) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(LocalizationManager.get("dialog.script.results"));
        TextArea textArea = new TextArea(String.join("\n", results));
        textArea.setEditable(false);
        textArea.setWrapText(false);
        dialog.getDialogPane().setContent(textArea);
        dialog.getDialogPane().setPrefSize(600, 400);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type, msg);
        a.show();
    }

    private String toUserMessage(Exception e) {
        if (e instanceof ClosedChannelException) {
            return LocalizationManager.get("server.disconnected");
        }
        String msg = e.getMessage();
        if (msg == null) return LocalizationManager.get("server.error");
        String lower = msg.toLowerCase();
        if (lower.contains("not connected") || lower.contains("connection refused")
            || lower.contains("connect refused") || lower.contains("server closed")
            || lower.contains("failed to connect") || lower.contains("timeout")
            || lower.contains("connection reset") || lower.contains("broken pipe")) {
            return LocalizationManager.get("server.disconnected");
        }
        return LocalizationManager.get("server.error");
    }
}
