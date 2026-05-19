package com.client.gui;

import com.model.MusicBand;
import com.model.MusicGenre;
import com.model.Coordinates;
import com.model.Album;
import com.client.network.AsyncClient;
import com.common.Command;
import com.common.RequestBuilder;
import com.common.Response;
import com.auth.AuthorizationService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;

public class BandEditController {
    @FXML private HBox idRow;
    @FXML private Label lblId;
    @FXML private TextField idField;
    @FXML private Label lblName, lblCoordX, lblCoordY, lblParticipants, lblGenre;
    @FXML private Label lblDescription, lblAlbumName, lblAlbumSales, errorLabel;
    @FXML private TextField nameField, coordXField, coordYField;
    @FXML private TextField participantsField, descriptionField;
    @FXML private TextField albumNameField, albumSalesField;
    @FXML private ComboBox<String> genreField;
    @FXML private Button saveBtn, cancelBtn;

    private MusicBand band;
    private AsyncClient client;
    private boolean addMode = false;
    private boolean isAddIfMin = false;

    @FXML
    public void initialize() {
        genreField.setItems(FXCollections.observableArrayList(
            "PSYCHEDELIC_ROCK", "MATH_ROCK", "POST_ROCK"
        ));
        updateLabels();
        LocalizationManager.addLocaleChangeListener(locale -> updateLabels());
    }

    private void updateLabels() {
        lblId.setText(LocalizationManager.get("edit.id"));
        lblName.setText(LocalizationManager.get("edit.name"));
        lblCoordX.setText(LocalizationManager.get("edit.coordX"));
        lblCoordY.setText(LocalizationManager.get("edit.coordY"));
        lblParticipants.setText(LocalizationManager.get("edit.participants"));
        lblGenre.setText(LocalizationManager.get("edit.genre"));
        lblDescription.setText(LocalizationManager.get("edit.description"));
        lblAlbumName.setText(LocalizationManager.get("edit.albumName"));
        lblAlbumSales.setText(LocalizationManager.get("edit.albumSales"));
        saveBtn.setText(LocalizationManager.get("edit.save"));
        cancelBtn.setText(LocalizationManager.get("edit.cancel"));
    }

    public void initData(MusicBand band, AsyncClient client) {
        this.band = band;
        this.client = client;
        this.addMode = false;
        nameField.setText(band.getName());
        if (band.getCoordinates() != null) {
            coordXField.setText(String.valueOf(band.getCoordinates().getX()));
            coordYField.setText(String.valueOf(band.getCoordinates().getY()));
        }
        if (band.getNumberOfParticipants() != null) {
            participantsField.setText(String.valueOf(band.getNumberOfParticipants()));
        }
        if (band.getGenre() != null) {
            genreField.setValue(band.getGenre().name());
        }
        descriptionField.setText(band.getDescription() != null ? band.getDescription() : "");
        if (band.getBestAlbum() != null) {
            albumNameField.setText(band.getBestAlbum().getName());
            albumSalesField.setText(String.valueOf(band.getBestAlbum().getSales()));
        }
    }

    public void initAddMode(AsyncClient client, boolean isAddIfMin) {
        this.band = new MusicBand();
        this.client = client;
        this.addMode = true;
        this.isAddIfMin = isAddIfMin;
        if (isAddIfMin) {
            idRow.setManaged(true);
            idRow.setVisible(true);
            idField.setText(String.valueOf(band.getId()));
        }
    }

    @FXML
    public void handleSave(ActionEvent event) {
        errorLabel.setText("");

        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            errorLabel.setText(LocalizationManager.get("edit.error.required", LocalizationManager.get("edit.name")));
            return;
        }
        band.setName(name);

        try {
            String xs = coordXField.getText().trim();
            long x = xs.isEmpty() ? 0 : Long.parseLong(xs);
            if (x > 554) {
                errorLabel.setText(LocalizationManager.get("validation.coordX.max"));
                return;
            }
            String ys = coordYField.getText().trim();
            int y = ys.isEmpty() ? 0 : Integer.parseInt(ys);
            if (y > 782) {
                errorLabel.setText(LocalizationManager.get("validation.coordY.max"));
                return;
            }
            band.setCoordinates(new Coordinates(x, y));
        } catch (NumberFormatException e) {
            errorLabel.setText(LocalizationManager.get("validation.coordinates.format"));
            return;
        }

        String parts = participantsField.getText().trim();
        if (parts.isEmpty()) {
            errorLabel.setText(LocalizationManager.get("validation.participants.positive"));
            return;
        }
        try {
            int n = Integer.parseInt(parts);
            if (n <= 0) {
                errorLabel.setText(LocalizationManager.get("validation.participants.positive"));
                return;
            }
            band.setNumberOfParticipants(n);
        } catch (NumberFormatException e) {
            errorLabel.setText(LocalizationManager.get("validation.participants.positive"));
            return;
        }

        String g = genreField.getValue();
        band.setGenre(g != null ? MusicGenre.valueOf(g) : null);

        band.setDescription(descriptionField.getText().trim().isEmpty() ? null : descriptionField.getText().trim());

        String albumName = albumNameField.getText().trim();
        if (albumName.isEmpty()) {
            errorLabel.setText(LocalizationManager.get("edit.error.required", LocalizationManager.get("edit.albumName")));
            return;
        }
        String albumSalesStr = albumSalesField.getText().trim();
        if (albumSalesStr.isEmpty()) {
            errorLabel.setText(LocalizationManager.get("validation.album.sales.positive"));
            return;
        }
        try {
            double albumSales = Double.parseDouble(albumSalesStr.replace(',', '.'));
            if (albumSales < 0) {
                errorLabel.setText(LocalizationManager.get("validation.album.sales.positive"));
                return;
            }
            band.setBestAlbum(new Album(albumName, albumSales));
        } catch (NumberFormatException e) {
            errorLabel.setText(LocalizationManager.get("validation.album.sales.positive"));
            return;
        }

        if (addMode && isAddIfMin) {
            String idText = idField.getText().trim();
            if (idText.isEmpty()) {
                errorLabel.setText(LocalizationManager.get("edit.error.required", LocalizationManager.get("edit.id")));
                return;
            }
            try {
                long manualId = Long.parseLong(idText);
                if (manualId <= 0) {
                    errorLabel.setText(LocalizationManager.get("validation.id.positive"));
                    return;
                }
                band.setId(manualId);
            } catch (NumberFormatException e) {
                errorLabel.setText(LocalizationManager.get("validation.id.format"));
                return;
            }
        }

        new Thread(() -> {
            try {
                if (addMode) {
                    Command cmd = isAddIfMin ? Command.ADD_IF_MIN : Command.ADD;
                    client.send(RequestBuilder.command(cmd)
                        .withData(band)
                        .withAuth()
                        .build());
                } else {
                    client.send(RequestBuilder.command(Command.UPDATE)
                        .withArg("id", band.getId())
                        .withData(band)
                        .withAuth()
                        .build());
                }
            } catch (Exception ignored) {}
        }).start();
        ((Stage) saveBtn.getScene().getWindow()).close();
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        ((Stage) cancelBtn.getScene().getWindow()).close();
    }
}
