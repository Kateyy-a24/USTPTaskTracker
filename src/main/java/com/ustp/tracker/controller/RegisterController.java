package com.ustp.tracker.controller;

import com.ustp.tracker.App;
import com.ustp.tracker.Ui;
import com.ustp.tracker.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.sql.SQLException;

public class RegisterController extends BaseController {
    @FXML private StackPane leftPane, logoHolder;
    @FXML private TextField fullNameField, studentIdField, emailField, usernameField;
    @FXML private PasswordField passField, confirmField;
    @FXML private Label errorLbl;

    @FXML
    private void initialize() {
        URL bg = getClass().getResource("/images/campus.jpg");
        if (bg != null) {
            leftPane.setStyle("-fx-background-image:url('" + bg.toExternalForm() + "');"
                    + "-fx-background-size:cover;-fx-background-position:center;");
        }
        logoHolder.getChildren().add(Ui.logo(150));
    }

    @FXML
    private void onRegister() {
        String name = fullNameField.getText().trim();
        String sid = studentIdField.getText().trim();
        String email = emailField.getText().trim();
        String user = usernameField.getText().trim();
        String pass = passField.getText();

        if (name.isEmpty() || sid.isEmpty() || email.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            errorLbl.setText("Please fill in all fields.");
            return;
        }
        if (!email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            errorLbl.setText("Please enter a valid email address.");
            return;
        }
        if (user.length() < 4 || user.contains(" ")) {
            errorLbl.setText("Username must be at least 4 characters with no spaces.");
            return;
        }
        if (pass.length() < 8) {
            errorLbl.setText("Password must be at least 8 characters.");
            return;
        }
        if (!pass.equals(confirmField.getText())) {
            errorLbl.setText("Passwords do not match.");
            return;
        }
        try {
            if (userRepo.exists(user, email)) {
                errorLbl.setText("That username or email is already registered.");
                return;
            }
            User u = new User();
            u.setUsername(user);
            u.setFullName(name);
            u.setStudentId(sid);
            u.setEmail(email);
            userRepo.register(u, pass);
            Ui.info("Account Created", "Your account is ready. You can now log in.\nTip: complete your college, major and semester in the Profile page.");
            App.setRoot("Login", "Login");
        } catch (SQLException e) {
            Ui.error(e);
        }
    }

    @FXML
    private void onBack() {
        App.setRoot("Login", "Login");
    }
}
