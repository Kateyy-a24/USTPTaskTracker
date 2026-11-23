package com.ustp.tracker;

import com.ustp.tracker.model.User;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public final class Ui {
    public static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);

    private Ui() {}

    public static void error(String msg) {
        Dialogs.show(Dialogs.Kind.DANGER, Dialogs.ALERT, "Oops!", msg, "OK", null);
    }

    public static void error(Exception e) {
        e.printStackTrace();
        error("Database error: " + e.getMessage() + "\n\nMake sure XAMPP MySQL is running and schema.sql was imported.");
    }

    public static void info(String title, String msg) {
        Dialogs.show(Dialogs.Kind.SUCCESS, Dialogs.CHECK, title, msg, "OK", null);
    }

    public static void info(String msg) { info("Success", msg); }

    public static boolean confirm(String msg) {
        return Dialogs.show(Dialogs.Kind.INFO, Dialogs.INFO, "Please Confirm", msg, "Yes", "Cancel");
    }

    public static boolean confirmLogout() {
        return Dialogs.show(Dialogs.Kind.DANGER, Dialogs.LOGOUT, "Logout Confirmation",
                "Are you sure you want to sign out of USTP-CDO Task Tracker? Your active progress will be securely saved.",
                "Log Out", "Cancel");
    }

    public static boolean confirmDelete(String taskName) {
        return Dialogs.show(Dialogs.Kind.DANGER, Dialogs.TRASH, "Delete Task",
                "Are you sure you want to delete \"" + taskName + "\"? This action cannot be undone.",
                "Delete", "Cancel");
    }

    public static Label pill(String status) {
        Label l = new Label(status);
        l.getStyleClass().addAll("pill", "Completed".equals(status) ? "pill-done" : "pill-pending");
        return l;
    }

    public static String dueText(LocalDate d) {
        long days = ChronoUnit.DAYS.between(LocalDate.now(), d);
        if (days < 0) return "Overdue by " + (-days) + (days == -1 ? " day" : " days");
        if (days == 0) return "Due Today";
        if (days == 1) return "Due Tomorrow";
        if (days < 30) return "Due in " + days + " days";
        long m = days / 30;
        return "Due in " + m + (m == 1 ? " month" : " months");
    }

    public static String dotColor(LocalDate d) {
        long days = ChronoUnit.DAYS.between(LocalDate.now(), d);
        if (days < 0) return "#DC2626";
        if (days <= 3) return "#F59E0B";
        if (days <= 30) return "#16A34A";
        return "#DC2626";
    }

    /** Round USTP logo. Uses images/logo.png if present, otherwise the built-in copy. */
    public static Node logo(double size) {
        StackPane sp = new StackPane();
        sp.setMinSize(size, size);
        sp.setMaxSize(size, size);
        Circle bg = new Circle(size / 2, Color.WHITE);
        Image img;
        URL url = Ui.class.getResource("/images/logo.png");
        if (url != null) {
            img = new Image(url.toExternalForm());
        } else {
            img = new Image(new java.io.ByteArrayInputStream(LogoData.bytes()));
        }
        ImageView iv = new ImageView(img);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        iv.setFitWidth(size);
        iv.setFitHeight(size);
        iv.setClip(new Circle(size / 2, size / 2, size / 2));
        sp.getChildren().addAll(bg, iv);
        return sp;
    }

    /** Round profile picture; shows the default blue person icon when the user has no photo. */
    public static Node avatar(User u, double size) {
        StackPane sp = new StackPane();
        sp.setMinSize(size, size);
        sp.setMaxSize(size, size);
        if (u != null && u.getAvatarPath() != null && !u.getAvatarPath().isBlank() && new File(u.getAvatarPath()).exists()) {
            Image img = new Image(new File(u.getAvatarPath()).toURI().toString());
            double w = img.getWidth(), h = img.getHeight(), s = Math.min(w, h);
            ImageView iv = new ImageView(img);
            iv.setViewport(new Rectangle2D((w - s) / 2, (h - s) / 2, s, s));
            iv.setFitWidth(size);
            iv.setFitHeight(size);
            iv.setClip(new Circle(size / 2, size / 2, size / 2));
            sp.getChildren().add(iv);
        } else {
            Circle bg = new Circle(size / 2, Color.web("#EEF2F7"));
            SVGPath p = new SVGPath();
            p.setContent("M12 12c2.7 0 4.9-2.2 4.9-4.9S14.7 2.2 12 2.2 7.1 4.4 7.1 7.1 9.3 12 12 12zm0 2.4c-3.3 0-9.8 1.6-9.8 4.9v2.5h19.6v-2.5c0-3.3-6.5-4.9-9.8-4.9z");
            p.setFill(Color.web("#1E88FF"));
            double sc = size * 0.7 / 24;
            p.setScaleX(sc);
            p.setScaleY(sc);
            sp.getChildren().addAll(bg, p);
        }
        return sp;
    }
}
