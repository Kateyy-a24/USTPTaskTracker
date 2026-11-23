package com.ustp.tracker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    private static Stage stage;

    @Override
    public void start(Stage s) throws IOException {
        stage = s;
        // Size relative to the user's screen so it fits Windows 10/11 at any scaling (not zoomed in).
        Rectangle2D vb = Screen.getPrimary().getVisualBounds();
        double w = Math.min(1280, vb.getWidth() * 0.9);
        double h = Math.min(760, vb.getHeight() * 0.9);
        Scene scene = new Scene(load("Login"), w, h);
        scene.getStylesheets().add(App.class.getResource("/css/style.css").toExternalForm());
        s.setScene(scene);
        s.setMinWidth(980);
        s.setMinHeight(620);
        s.setTitle("USTP-CDO Task Tracker - Login");
        s.show();
    }

    @Override
    public void stop() {
        AppContext.get().reminders().stop();   // removes the tray icon so the app closes completely
        System.exit(0);
    }

    public static Parent load(String fxml) throws IOException {
        return FXMLLoader.load(App.class.getResource("/fxml/" + fxml + ".fxml"));
    }

    public static void setRoot(String fxml, String title) {
        try {
            stage.getScene().setRoot(load(fxml));
            stage.setTitle("USTP-CDO Task Tracker - " + title);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Stage stage() { return stage; }

    public static void setWindowTitle(String title) {
        if (stage != null) stage.setTitle("USTP-CDO Task Tracker - " + title);
    }

    public static void main(String[] args) { launch(args); }
}
