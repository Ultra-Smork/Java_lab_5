package com.client.gui;

import com.model.MusicBand;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.util.*;
import java.util.stream.Collectors;

public class CollectionCanvas extends Canvas {
    private List<MusicBand> bands = new ArrayList<>();
    private MusicBand hoveredBand;
    private MusicBand selectedBand;

    private static final double PADDING = 50;
    private static final double AXIS_MAX_X = 560;
    private static final double AXIS_MAX_Y = 790;
    private static final double GRID_STEP = 50;
    private static final double MIN_RADIUS = 4;
    private static final double MAX_RADIUS = 20;
    private static final double RADIUS_OFFSET_HOVER = 3;
    private static final double RADIUS_OFFSET_SELECTED = 5;

    private int minParticipants = 0;
    private int maxParticipants = 1;

    private static final Color BG_COLOR = Color.web("#1e1e1e");
    private static final Color GRID_COLOR = Color.web("#3d3d3d");
    private static final Color AXIS_COLOR = Color.web("#888888");
    private static final Color LABEL_COLOR = Color.web("#aaaaaa");
    private static final Color HOVER_STROKE = Color.web("#ffffff");

    private final Map<String, Color> ownerColors = new HashMap<>();
    private static final Color[] PALETTE = {
        Color.web("#4CAF50"), Color.web("#2196F3"), Color.web("#FF9800"),
        Color.web("#E91E63"), Color.web("#9C27B0"), Color.web("#00BCD4"),
        Color.web("#FF5722"), Color.web("#795548"), Color.web("#607D8B"),
        Color.web("#CDDC39"), Color.web("#03A9F4"), Color.web("#F44336")
    };

    private static class Nudge {
        final double dx, dy;
        Nudge(double dx, double dy) { this.dx = dx; this.dy = dy; }
    }
    private Map<MusicBand, Nudge> nudgeMap = new HashMap<>();

    public CollectionCanvas() {
        widthProperty().addListener(e -> draw());
        heightProperty().addListener(e -> draw());

        setOnMouseMoved(e -> {
            MusicBand prev = hoveredBand;
            hoveredBand = findBandAt(e.getX(), e.getY());
            if (prev != hoveredBand) draw();
        });

        setOnMouseClicked(e -> {
            MusicBand band = findBandAt(e.getX(), e.getY());
            if (band != null) {
                selectedBand = band;
                draw();
                showBandInfo(band);
            }
        });

        setOnMouseExited(e -> {
            if (hoveredBand != null || selectedBand != null) {
                hoveredBand = null;
                draw();
            }
        });
    }

    public void updateBands(List<MusicBand> newBands) {
        bands = new ArrayList<>(newBands);
        minParticipants = bands.stream()
            .mapToInt(b -> b.getNumberOfParticipants() != null ? b.getNumberOfParticipants() : 0)
            .min().orElse(0);
        maxParticipants = bands.stream()
            .mapToInt(b -> b.getNumberOfParticipants() != null ? b.getNumberOfParticipants() : 0)
            .max().orElse(1);
        if (maxParticipants == minParticipants) maxParticipants = minParticipants + 1;
        draw();
    }

    public void clearSelection() {
        selectedBand = null;
        draw();
    }

    private Color getOwnerColor(String owner) {
        if (owner == null) owner = "unknown";
        return ownerColors.computeIfAbsent(owner, k -> PALETTE[ownerColors.size() % PALETTE.length]);
    }

    private double getRadius(MusicBand band) {
        int p = band.getNumberOfParticipants() != null ? band.getNumberOfParticipants() : 0;
        double ratio = (double)(p - minParticipants) / (maxParticipants - minParticipants);
        return MIN_RADIUS + ratio * (MAX_RADIUS - MIN_RADIUS);
    }

    private double[] bandPosition(MusicBand band, double scaleX, double scaleY) {
        Nudge n = nudgeMap.get(band);
        double nx = n != null ? n.dx : 0;
        double ny = n != null ? n.dy : 0;
        double x = PADDING + band.getCoordinates().getX() * scaleX + nx;
        double y = PADDING + (AXIS_MAX_Y - band.getCoordinates().getY()) * scaleY + ny;
        return new double[]{x, y};
    }

    private MusicBand findBandAt(double px, double py) {
        double plotW = getWidth() - 2 * PADDING;
        double plotH = getHeight() - 2 * PADDING;
        double scaleX = plotW / AXIS_MAX_X;
        double scaleY = plotH / AXIS_MAX_Y;

        for (MusicBand band : bands) {
            if (band.getCoordinates() == null) continue;
            double[] pos = bandPosition(band, scaleX, scaleY);
            double dx = px - pos[0];
            double dy = py - pos[1];
            double hitR = getRadius(band) + RADIUS_OFFSET_SELECTED + 2;
            if (dx * dx + dy * dy <= hitR * hitR) {
                return band;
            }
        }
        return null;
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        if (w <= 0 || h <= 0) return;

        gc.clearRect(0, 0, w, h);

        double plotW = w - 2 * PADDING;
        double plotH = h - 2 * PADDING;
        double scaleX = plotW / AXIS_MAX_X;
        double scaleY = plotH / AXIS_MAX_Y;

        computeNudges(scaleX, scaleY);

        drawBackground(gc, w, h);
        drawGrid(gc, plotW, plotH, scaleX, scaleY);
        drawAxes(gc, plotW, plotH);
        drawOwnerColors(gc, plotW, plotH, scaleX, scaleY);

        List<MusicBand> sorted = new ArrayList<>(bands);
        sorted.sort(Comparator.comparingDouble(b -> getRadius(b)));
        for (MusicBand band : sorted) {
            drawBand(gc, band, scaleX, scaleY);
        }

        if (hoveredBand != null) {
            drawHoverTooltip(gc, hoveredBand, scaleX, scaleY);
        }
    }

    private void computeNudges(double scaleX, double scaleY) {
        nudgeMap.clear();
        Map<String, List<MusicBand>> groups = new HashMap<>();
        for (MusicBand band : bands) {
            if (band.getCoordinates() == null) continue;
            String key = band.getCoordinates().getX() + "," + band.getCoordinates().getY();
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(band);
        }
        for (List<MusicBand> group : groups.values()) {
            int n = group.size();
            if (n <= 1) {
                for (MusicBand b : group) nudgeMap.put(b, new Nudge(0, 0));
            } else {
                double spreadR = Math.max(MAX_RADIUS + 2, n * 4);
                for (int i = 0; i < n; i++) {
                    double angle = 2 * Math.PI * i / n;
                    double dx = Math.cos(angle) * spreadR;
                    double dy = -Math.sin(angle) * spreadR;
                    nudgeMap.put(group.get(i), new Nudge(dx, dy));
                }
            }
        }
    }

    private void drawBackground(GraphicsContext gc, double w, double h) {
        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, w, h);
    }

    private void drawGrid(GraphicsContext gc, double plotW, double plotH, double scaleX, double scaleY) {
        gc.setStroke(GRID_COLOR);
        gc.setLineWidth(0.5);

        for (double v = 0; v <= AXIS_MAX_X; v += GRID_STEP) {
            double x = PADDING + v * scaleX;
            gc.strokeLine(x, PADDING, x, PADDING + plotH);
        }
        for (double v = 0; v <= AXIS_MAX_Y; v += GRID_STEP) {
            double y = PADDING + (AXIS_MAX_Y - v) * scaleY;
            gc.strokeLine(PADDING, y, PADDING + plotW, y);
        }
    }

    private void drawAxes(GraphicsContext gc, double plotW, double plotH) {
        gc.setStroke(AXIS_COLOR);
        gc.setLineWidth(2);
        gc.strokeLine(PADDING, PADDING, PADDING, PADDING + plotH);
        gc.strokeLine(PADDING, PADDING + plotH, PADDING + plotW, PADDING + plotH);

        gc.setFill(LABEL_COLOR);
        gc.setTextAlign(TextAlignment.CENTER);

        for (double v = 0; v <= AXIS_MAX_X; v += GRID_STEP) {
            double x = PADDING + v * (plotW / AXIS_MAX_X);
            gc.fillText(String.valueOf((int) v), x, PADDING + plotH + 15);
        }

        gc.setTextAlign(TextAlignment.RIGHT);
        for (double v = 0; v <= AXIS_MAX_Y; v += GRID_STEP) {
            double y = PADDING + (AXIS_MAX_Y - v) * (plotH / AXIS_MAX_Y);
            gc.fillText(String.valueOf((int) v), PADDING - 5, y + 4);
        }

        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("X", PADDING + plotW / 2, PADDING + plotH + 35);
        gc.save();
        gc.translate(PADDING - 30, PADDING + plotH / 2);
        gc.rotate(-90);
        gc.fillText("Y", 0, 0);
        gc.restore();
    }

    private void drawOwnerColors(GraphicsContext gc, double plotW, double plotH, double scaleX, double scaleY) {
        if (ownerColors.isEmpty()) return;

        double legendX = PADDING + plotW - 120;
        double legendY = PADDING + 10;
        gc.setFill(Color.web("#00000080"));
        gc.fillRoundRect(legendX - 5, legendY - 5, 130, ownerColors.size() * 20 + 15, 5, 5);

        gc.setFill(LABEL_COLOR);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Owners:", legendX, legendY + 12);

        int i = 0;
        for (Map.Entry<String, Color> entry : ownerColors.entrySet()) {
            double y = legendY + 25 + i * 18;
            gc.setFill(entry.getValue());
            gc.fillOval(legendX, y, 10, 10);
            gc.setFill(LABEL_COLOR);
            gc.fillText(entry.getKey(), legendX + 15, y + 10);
            i++;
        }
    }

    private void drawBand(GraphicsContext gc, MusicBand band, double scaleX, double scaleY) {
        if (band.getCoordinates() == null) return;

        double[] pos = bandPosition(band, scaleX, scaleY);
        double x = pos[0];
        double y = pos[1];

        Color color = getOwnerColor(band.getOwnerLogin());
        boolean isHover = band == hoveredBand;
        boolean isSelected = band == selectedBand;

        gc.setFill(color);
        gc.setStroke(isSelected ? HOVER_STROKE : Color.TRANSPARENT);
        gc.setLineWidth(isSelected ? 2.5 : 0);
        double r = getRadius(band);
        if (isSelected) r += RADIUS_OFFSET_SELECTED;
        else if (isHover) r += RADIUS_OFFSET_HOVER;
        gc.fillOval(x - r, y - r, r * 2, r * 2);
        if (isSelected) gc.strokeOval(x - r, y - r, r * 2, r * 2);
    }

    private void drawHoverTooltip(GraphicsContext gc, MusicBand band, double scaleX, double scaleY) {
        if (band.getCoordinates() == null) return;

        double[] pos = bandPosition(band, scaleX, scaleY);
        double x = pos[0];
        double y = pos[1];

        String text = band.getName() + " (ID: " + band.getId() + ")";
        gc.setFill(Color.web("#000000cc"));
        double tw = gc.getFont().getSize() * text.length() * 0.55;
        double tx = Math.min(x + 15, getWidth() - tw - 10);
        double ty = y - 25;
        gc.fillRoundRect(tx - 5, ty - 15, tw + 10, 22, 4, 4);
        gc.setFill(HOVER_STROKE);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(text, tx, ty);
    }

    private void showBandInfo(MusicBand band) {
        String info = String.format("ID: %d\nName: %s\nX: %d, Y: %d\nParticipants: %d\nGenre: %s\nOwner: %s",
            band.getId(), band.getName(),
            band.getCoordinates() != null ? band.getCoordinates().getX() : 0,
            band.getCoordinates() != null ? band.getCoordinates().getY() : 0,
            band.getNumberOfParticipants() != null ? band.getNumberOfParticipants() : 0,
            band.getGenre() != null ? band.getGenre() : "-",
            band.getOwnerLogin() != null ? band.getOwnerLogin() : "-");

        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle(LocalizationManager.get("info.title"));
            alert.setHeaderText(band.getName());
            alert.setContentText(info);
            alert.show();
        });
    }
}
