package com.ustp.tracker.controller;

import com.ustp.tracker.App;
import com.ustp.tracker.Session;
import com.ustp.tracker.Ui;
import com.ustp.tracker.model.Task;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

import java.io.IOException;
import java.util.List;

public class MainController extends BaseController {
    @FXML private Label titleLbl, subtitleLbl, userNameLbl;
    @FXML private Button soundBtn, emailBtn;
    @FXML private StackPane pageHolder, avatarHolder, sidebarLogo;
    @FXML private Button navDashboard, navAdd, navView, navUpcoming, navSearch, navProfile, navLogout;
    private List<Button> navs;

    @FXML
    private void initialize() {
        Session.setMain(this);
        navs = List.of(navDashboard, navAdd, navView, navUpcoming, navSearch, navProfile);
        sidebarLogo.getChildren().add(Ui.logo(64));
        setIcon(navDashboard, "M3 3h7v7H3z M14 3h7v7h-7z M14 14h7v7h-7z M3 14h7v7H3z");
        setIcon(navAdd, "M4 4h16v16H4z M12 8v8 M8 12h8");
        setIcon(navView, "M4 6h16 M4 12h16 M4 18h10");
        setIcon(navUpcoming, "M4 5h16v15H4z M4 10h16 M8 3v4 M16 3v4");
        setIcon(navSearch, "M10.5 3a7.5 7.5 0 1 0 0 15a7.5 7.5 0 0 0 0-15z M16 16l5 5");
        setIcon(navProfile, "M12 3a9 9 0 1 0 0 18a9 9 0 0 0 0-18z M9 9l6 6 M15 9l-6 6");
        setIcon(navLogout, "M9 4H5v16h4 M16 8l4 4-4 4 M20 12H9");
        refreshUser();
        updateSoundBtn();
        updateEmailBtn();
        showDashboard();
        context.reminders().start();   // deadline pop-up + alarm + notifications
    }

    private void updateSoundBtn() {
        boolean on = context.settings().isSoundEnabled(currentUser().getId());
        soundBtn.setText(on ? "\uD83D\uDD14 Alarm On" : "\uD83D\uDD15 Alarm Off");
        soundBtn.setTooltip(new Tooltip("Deadline alarm sound is " + (on ? "ON" : "OFF") + ". Click to turn it " + (on ? "off." : "on.")));
    }

    private void updateEmailBtn() {
        boolean on = context.settings().isEmailEnabled(currentUser().getId());
        emailBtn.setText(on ? "\u2709 Email On" : "\u2709 Email Off");
        emailBtn.setTooltip(new Tooltip("Deadline reminders by email are " + (on ? "ON" : "OFF")
                + ". Click to turn them " + (on ? "off." : "on.")));
    }

    @FXML
    private void onToggleEmail() {
        int uid = currentUser().getId();
        if (context.settings().isEmailEnabled(uid)) {          // turn OFF
            context.settings().setEmailEnabled(uid, false);
            updateEmailBtn();
            return;
        }
        String email = Session.getUser().getEmail();               // turn ON (and send a confirmation email)
        if (email == null || email.isBlank()) {
            Ui.error("Your account has no email yet. Add one in your Profile first.");
            return;
        }
        if (!context.mailer().isConfigured()) {
            Ui.error("The email service is not set up yet. Please contact the system administrator.");
            return;
        }
        emailBtn.setDisable(true);
        var user = Session.getUser();
        Thread t = new Thread(() -> {
            try {
                context.emailChannel().sendConfirmation(user);
                Platform.runLater(() -> {
                    context.settings().setEmailEnabled(uid, true);
                    emailBtn.setDisable(false);
                    updateEmailBtn();
                    Ui.info("Email Reminders On", "A confirmation email was sent to " + email
                            + ". You will get deadline reminders there.");
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    emailBtn.setDisable(false);
                    Ui.error("Could not send the email. Please check your internet connection or ask the administrator to check mail.properties.");
                });
            }
        }, "email-test");
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void onToggleSound() {
        boolean on = !context.settings().isSoundEnabled(currentUser().getId());
        context.settings().setSoundEnabled(currentUser().getId(), on);
        updateSoundBtn();
        if (on) context.alarm().play();   // quick preview of the ringtone
        else context.alarm().stop();
    }

    private void setIcon(Button b, String path) {
        SVGPath p = new SVGPath();
        p.setContent(path);
        p.getStyleClass().add("nav-icon");
        p.setScaleX(0.75);
        p.setScaleY(0.75);
        StackPane box = new StackPane(p);
        box.setMinSize(18, 18);
        box.setPrefSize(18, 18);
        box.setMaxSize(18, 18);
        b.setGraphic(box);
        b.setGraphicTextGap(12);
    }

    /** Call after the profile changes so the top-right name/photo updates. */
    public void refreshUser() {
        userNameLbl.setText(currentUser().getDisplayName());
        avatarHolder.getChildren().setAll(Ui.avatar(Session.getUser(), 30));
    }

    private void go(String page, String title, String subtitle, Button active) {
        try {
            Node n = App.load(page);
            pageHolder.getChildren().setAll(n);
            titleLbl.setText(title);
            App.setWindowTitle(switch (title) {
                case "Dashboard Overview" -> "Dashboard";
                case "My Tasks" -> "View Tasks";
                case "Upcoming Academic Deadlines" -> "Upcoming Deadlines";
                case "Search & Filter Workspace" -> "Search & Filter";
                case "My University Profile" -> "Profile";
                default -> title;
            });
            subtitleLbl.setText(subtitle);
            for (Button b : navs) b.getStyleClass().remove("nav-active");
            if (active != null) active.getStyleClass().add("nav-active");
        } catch (IOException e) {
            Ui.error("Could not load page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML public void showDashboard() {
        String full = Session.getUser().getFullName();
        String first = (full == null || full.isBlank()) ? "Student" : full.trim().split("\\s+")[0];
        go("Dashboard", "Dashboard Overview", "Hi, " + first + "! Track and manage your academic tasks and deadlines here.", navDashboard);
    }

    @FXML public void showAddTask() {
        Session.setEditTask(null);
        go("AddTask", "Add Academic Task", "Add your school tasks by entering the task name, subject, description, and deadline.", navAdd);
    }

    public void showEditTask(Task t) {
        Session.setEditTask(t);
        go("AddTask", "Edit Academic Task", "Edit the information of your existing academic task.", navView);
    }

    public void showUpdateStatus(Task t) {
        Session.setStatusTask(t);
        go("UpdateStatus", "Update Task Status", "Update the status of your selected academic task.", navView);
    }

    @FXML public void showViewTasks() {
        go("ViewTasks", "My Tasks", "View your recorded academic tasks along with their deadlines and status.", navView);
    }

    @FXML public void showUpcoming() {
        go("Upcoming", "Upcoming Academic Deadlines", "View your upcoming academic deadlines sorted by date.", navUpcoming);
    }

    @FXML public void showSearch() {
        go("Search", "Search & Filter Workspace", "Easily find your academic workloads, filter by subject codes, or segment based on status.", navSearch);
    }

    @FXML public void showProfile() {
        go("Profile", "My University Profile", "Manage your student profile and account information.", navProfile);
    }

    @FXML
    private void onLogout() {
        if (Ui.confirmLogout()) {
            context.reminders().stop();
            Session.setUser(null);
            App.setRoot("Login", "Login");
        }
    }
}
