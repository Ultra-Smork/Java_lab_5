package com.client.gui;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.text.MessageFormat;
import java.util.*;

public class LocalizationManager {
    private static final Map<Locale, ResourceBundle> bundles = new HashMap<>();
    private static final ReadOnlyObjectWrapper<Locale> currentLocale = new ReadOnlyObjectWrapper<>(Locale.getDefault());
    private static final ObservableList<Locale> availableLocales = FXCollections.observableArrayList();
    private static Locale lastServerLocale = null;

    public static final Locale ENGLISH = new Locale("en");
    public static final Locale RUSSIAN = new Locale("ru");
    public static final Locale SERBIAN = new Locale("sr");
    public static final Locale ITALIAN = new Locale("it");
    public static final Locale SPANISH_NICARAGUA = new Locale("es", "NI");

    static {
        availableLocales.addAll(ENGLISH, RUSSIAN, SERBIAN, ITALIAN, SPANISH_NICARAGUA);
    }

    public static void init() {
        for (Locale locale : availableLocales) {
            try {
                ResourceBundle bundle = ResourceBundle.getBundle(
                    "com.client.gui.messages", locale);
                bundles.put(locale, bundle);
            } catch (Exception e) {
                System.err.println("Failed to load bundle for " + locale + ": " + e.getMessage());
            }
        }
        currentLocale.set(findBestMatch(Locale.getDefault()));
    }

    public static void setLocale(Locale locale) {
        Locale finalLocale = findBestMatch(locale);
        currentLocale.set(finalLocale);
        lastServerLocale = finalLocale;
        Platform.runLater(() -> {
            for (LocaleChangeListener listener : listeners) {
                listener.onLocaleChanged(finalLocale);
            }
        });
    }

    private static Locale findBestMatch(Locale locale) {
        if (locale == null) return RUSSIAN;
        if (bundles.containsKey(locale)) return locale;
        for (Locale key : bundles.keySet()) {
            if (key.getLanguage().equals(locale.getLanguage())
                && !key.getCountry().isEmpty()
                && key.getCountry().equals(locale.getCountry())) {
                return key;
            }
        }
        for (Locale key : bundles.keySet()) {
            if (key.getLanguage().equals(locale.getLanguage())) {
                return key;
            }
        }
        return RUSSIAN;
    }

    public static Locale getLocale() {
        return currentLocale.get();
    }

    public static ReadOnlyObjectProperty<Locale> localeProperty() {
        return currentLocale.getReadOnlyProperty();
    }

    public static String get(String key) {
        ResourceBundle bundle = bundles.get(currentLocale.get());
        if (bundle == null) {
            return key;
        }
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return key;
        }
    }

    public static String get(String key, Object... args) {
        String pattern = get(key);
        if (args == null || args.length == 0) return pattern;
        try {
            return MessageFormat.format(pattern, args);
        } catch (Exception e) {
            return pattern;
        }
    }

    public static ObservableList<Locale> getAvailableLocales() {
        return availableLocales;
    }

    public static Locale getLastServerLocale() {
        return lastServerLocale;
    }

    private static final ObservableList<LocaleChangeListener> listeners = FXCollections.observableArrayList();

    public interface LocaleChangeListener {
        void onLocaleChanged(Locale locale);
    }

    public static void addLocaleChangeListener(LocaleChangeListener listener) {
        listeners.add(listener);
    }

    public static void removeLocaleChangeListener(LocaleChangeListener listener) {
        listeners.remove(listener);
    }

    public static ResourceBundle getResourceBundle() {
        ResourceBundle bundle = bundles.get(currentLocale.get());
        if (bundle != null) return bundle;
        return new ResourceBundle() {
            @Override
            protected Object handleGetObject(String key) { return key; }
            @Override
            public Enumeration<String> getKeys() { return Collections.emptyEnumeration(); }
        };
    }
}
