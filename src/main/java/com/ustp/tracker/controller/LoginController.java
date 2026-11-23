package com.ustp.tracker.controller;

import com.ustp.tracker.App;
import com.ustp.tracker.Session;
import com.ustp.tracker.Ui;
import com.ustp.tracker.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.sql.SQLException;

public class LoginController extends BaseController {
    @FXML private StackPane leftPane, logoHolder;
    @FXML private TextField usernameField, passVisible;
    @FXML private PasswordField passField;
    @FXML private Label errorLbl;

    @FXML
    private void initialize() {
        // Campus photo background (optional): put it at src/main/resources/images/campus.jpg
        URL bg = getClass().getResource("/images/campus.jpg");
        if (bg != null) {
            leftPane.setStyle("-fx-background-image:url('" + bg.toExternalForm() + "');"
                    + "-fx-background-size:cover;-fx-background-position:center;");
        }
        logoHolder.getChildren().add(Ui.logo(150));
        passVisible.textProperty().bindBidirectional(passField.textProperty());
        passVisible.setVisible(false);
        passVisible.setManaged(false);
    }

    @FXML
    private void toggleEye() {
        boolean show = !passVisible.isVisible();
        passVisible.setVisible(show);
        passVisible.setManaged(show);
        passField.setVisible(!show);
        passField.setManaged(!show);
    }

    @FXML
    private void onLogin() {
        String user = usernameField.getText().trim();
        String pass = passField.getText();
        if (user.isEmpty() || pass.isEmpty()) {
            errorLbl.setText("Please enter your username and password.");
            return;
        }
        try {
            User u = userRepo.login(user, pass);
            if (u == null) {
                errorLbl.setText("Incorrect username or password.");
                return;
            }
            Session.setUser(u);
            App.setRoot("Main", "Dashboard");
        } catch (SQLException e) {
            Ui.error(e);
        }
    }

    @FXML
    private void onForgot() {
        ForgotPasswordController.setUsernameMode(false);
        App.setRoot("ForgotPassword", "Forgot Password");
    }

    @FXML
    private void onForgotUsername() {
        ForgotPasswordController.setUsernameMode(true);
        App.setRoot("ForgotPassword", "Forgot Username");
    }

    @FXML
    private void onSignUp() {
        App.setRoot("Register", "Sign Up");
    }
}
