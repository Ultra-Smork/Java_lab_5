package com.client.gui;

import com.model.MusicBand;
import com.model.MusicGenre;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;

public class BandTableManager {
    private final ObservableList<MusicBand> observableBands = FXCollections.observableArrayList();
    private final ObservableList<MusicBand> displayBands = FXCollections.observableArrayList();

    private final Map<TableColumn<MusicBand, ?>, TextField> filterFields = new LinkedHashMap<>();
    private final Map<TableColumn<MusicBand, ?>, Boolean> sortDirections = new HashMap<>();
    private TableView<MusicBand> tableView;

    public BandTableManager() {
    }

    public void attachTable(TableView<MusicBand> tableView) {
        this.tableView = tableView;
        tableView.setItems(displayBands);
        tableView.setEditable(false);
        
        // Listen to table sort events to trigger our stream API sort
        tableView.sortPolicyProperty().set(tv -> {
            applyStreamFilterAndSort();
            return true;
        });
    }

    public void addFilterField(TableColumn<MusicBand, ?> column, TextField filterField) {
        filterFields.put(column, filterField);
        sortDirections.put(column, false);

        filterField.textProperty().addListener((obs, oldVal, newVal) -> {
            applyStreamFilterAndSort();
        });

        column.setSortType(TableColumn.SortType.ASCENDING);
        column.sortableProperty().set(true);

        column.widthProperty().addListener((obs, oldVal, newVal) -> {
            applyStreamFilterAndSort();
        });
    }

    public void addColumn(String titleKey, String property, Class<?> type,
                         TableColumn<MusicBand, ?> col, TextField filterField, double width) {
        col.setText(LocalizationManager.get(titleKey));
        col.setCellValueFactory(cd -> {
            MusicBand band = cd.getValue();
            return new ReadOnlyObjectWrapper(getPropertyValue(band, property));
        });
        col.getProperties().put("propertyName", property);
        col.setPrefWidth(width);
        col.setResizable(true);

        if (type == String.class) {
            col.setCellFactory(tc -> new TextFieldTableCell<>());
        } else if (type == Long.class) {
            col.setCellFactory(tc -> new TextFieldTableCell<>());
        } else if (type == Integer.class) {
            col.setCellFactory(tc -> new TextFieldTableCell<>());
        } else if (type == Double.class) {
            col.setCellFactory(tc -> new TextFieldTableCell<>());
        } else if (type == Date.class) {
            col.setCellFactory(tc -> new TextFieldTableCell<>());
        } else if (type == MusicGenre.class) {
            col.setCellFactory(tc -> new TextFieldTableCell<>());
        }

        filterField.promptTextProperty().set(LocalizationManager.get("table.filter"));

        addFilterField(col, filterField);
    }

    private Object getPropertyValue(MusicBand band, String property) {
        return switch (property) {
            case "id" -> band.getId();
            case "name" -> band.getName();
            case "x" -> band.getCoordinates() != null ? band.getCoordinates().getX() : null;
            case "y" -> band.getCoordinates() != null ? band.getCoordinates().getY() : null;
            case "numberOfParticipants" -> band.getNumberOfParticipants();
            case "genre" -> band.getGenre();
            case "albumName" -> band.getBestAlbum() != null ? band.getBestAlbum().getName() : null;
            case "albumSales" -> band.getBestAlbum() != null ? band.getBestAlbum().getSales() : null;
            case "ownerLogin" -> band.getOwnerLogin();
            case "creationDate" -> band.getCreationDate();
            default -> null;
        };
    }

    private void applyStreamFilterAndSort() {
        if (tableView == null) return;
        List<MusicBand> filteredList = observableBands.stream()
            .filter(band -> {
                for (Map.Entry<TableColumn<MusicBand, ?>, TextField> entry : filterFields.entrySet()) {
                    TextField tf = entry.getValue();
                    String filterText = tf.getText();
                    if (filterText != null && !filterText.isEmpty()) {
                        TableColumn<MusicBand, ?> col = entry.getKey();
                        String property = getPropertyName(col);
                        if (property == null) continue;
                        Object value = getPropertyValue(band, property);
                        if (value == null) {
                            if (!filterText.equalsIgnoreCase("null")) return false;
                        } else {
                            String strVal = formatValue(value);
                            if (!strVal.toLowerCase().contains(filterText.toLowerCase())) return false;
                        }
                    }
                }
                return true;
            })
            .sorted((a, b) -> {
                for (TableColumn<MusicBand, ?> col : tableView.getSortOrder()) {
                    String property = getPropertyName(col);
                    if (property == null) continue;
                    Object valA = getPropertyValue(a, property);
                    Object valB = getPropertyValue(b, property);
                    int cmp = compareNulls(valA, valB);
                    if (cmp != 0) {
                        return col.getSortType() == TableColumn.SortType.DESCENDING ? -cmp : cmp;
                    }
                }
                return 0;
            })
            .collect(Collectors.toList());

        displayBands.setAll(filteredList);
    }

    private String getPropertyName(TableColumn<MusicBand, ?> col) {
        return (String) col.getProperties().get("propertyName");
    }

    private String formatValue(Object value) {
        if (value instanceof Date date) {
            Locale locale = LocalizationManager.getLocale();
            var formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                .withLocale(locale)
                .withZone(ZoneId.systemDefault());
            return formatter.format(date.toInstant());
        }
        return String.valueOf(value);
    }


    private int compareNulls(Object a, Object b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        if (a instanceof Comparable && b instanceof Comparable) {
            return ((Comparable) a).compareTo(b);
        }
        return String.valueOf(a).compareTo(String.valueOf(b));
    }

    public ObservableList<MusicBand> getObservableBands() {
        return observableBands;
    }

    public void updateBands(List<MusicBand> newBands) {
        observableBands.setAll(newBands);
        applyStreamFilterAndSort();
    }

    public void updateLocale() {
        for (Map.Entry<TableColumn<MusicBand, ?>, TextField> entry : filterFields.entrySet()) {
            TableColumn<MusicBand, ?> col = entry.getKey();
            col.setText(LocalizationManager.get(getColumnTitleKey(col)));
            entry.getValue().promptTextProperty().set(LocalizationManager.get("table.filter"));
        }
    }

    private String getColumnTitleKey(TableColumn<MusicBand, ?> col) {
        String prop = getPropertyName(col);
        return switch (prop) {
            case "id" -> "table.id";
            case "name" -> "table.name";
            case "x" -> "table.x";
            case "y" -> "table.y";
            case "numberOfParticipants" -> "table.participants";
            case "genre" -> "table.genre";
            case "albumName" -> "table.album";
            case "albumSales" -> "table.sales";
            case "ownerLogin" -> "table.owner";
            case "creationDate" -> "table.created";
            default -> "";
        };
    }
}
