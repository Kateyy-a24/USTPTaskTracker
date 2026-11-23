package com.ustp.tracker.controller;

import com.ustp.tracker.App;
import com.ustp.tracker.Ui;
import com.ustp.tracker.model.User;
import com.ustp.tracker.service.OtpService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Forgot Password / Forgot Username.
 * The code is ALWAYS sent to the email saved in the account (never to an email typed by the user),
 * so only the real owner of the account can recover it.
 *
 * Password: 1) username or email  2) OTP  3) new password
 * Username: 1) email              2) OTP  3) show username(s) (+ option to reset password too)
 */
public class ForgotPasswordController extends BaseController {
    /** Set by LoginController before opening this screen. */
    private static boolean usernameMode = false;

    public static void setUsernameMode(boolean value) { usernameMode = value; }

    @FXML private StackPane leftPane, logoHolder;
    @FXML private VBox stepId, stepOtp, stepReset, stepResult;
    @FXML private Label titleLbl, introLbl, idLabel, otpInfoLbl, resultLbl, errorLbl;
    @FXML private TextField idField, otpField;
    @FXML private PasswordField newPassField, confirmField;
    @FXML private Button sendBtn, verifyBtn, resendBtn;

    private final boolean usernameFlow = usernameMode;
    private User account;          // the account being recovered
    private boolean verified;      // OTP already verified in this session
    private Timeline countdown;

    @FXML
    private void initialize() {
        URL bg = getClass().getResource("/images/campus.jpg");
        if (bg != null) {
            leftPane.setStyle("-fx-background-image:url('" + bg.toExternalForm() + "');"
                    + "-fx-background-size:cover;-fx-background-position:center;");
        }
        logoHolder.getChildren().add(Ui.logo(150));

        if (usernameFlow) {
            titleLbl.setText("Forgot Username");
            introLbl.setText("Enter the email you registered with. We will send a 6-digit code to that email.");
            idLabel.setText("REGISTERED EMAIL");
            idField.setPromptText("name@gmail.com");
        } else {
            titleLbl.setText("Forgot Password");
            introLbl.setText("Enter your username or email. We will send a 6-digit code to the email saved in your account.");
            idLabel.setText("USERNAME / EMAIL");
            idField.setPromptText("Enter your username or email");
        }
        // only 6 digits in the OTP box
        otpField.textProperty().addListener((o, a, b) -> {
            String d = b.replaceAll("\\D", "");
            if (d.length() > 6) d = d.substring(0, 6);
            if (!d.equals(b)) otpField.setText(d);
        });
        showStep(1);
    }

    private void showStep(int n) {
        set(stepId, n == 1);
        set(stepOtp, n == 2);
        set(stepReset, n == 3);
        set(stepResult, n == 4);
        message("", false);
    }

    private static void set(VBox b, boolean on) { b.setVisible(on); b.setManaged(on); }

    private void message(String msg, boolean ok) {
        errorLbl.getStyleClass().removeAll("error-text", "success-text");
        errorLbl.getStyleClass().add(ok ? "success-text" : "error-text");
        errorLbl.setText(msg);
    }

    private static String mask(String email) {
        int at = email.indexOf('@');
        if (at < 1) return email;
        String name = email.substring(0, at);
        String shown = name.length() <= 2 ? name.substring(0, 1) : name.substring(0, 2);
        return shown + "*".repeat(Math.max(3, name.length() - shown.length())) + email.substring(at);
    }

    // ---------- Step 1: find account, send code ----------
    @FXML
    private void onSendCode() {
        String id = idField.getText().trim();
        if (id.isEmpty()) {
            message(usernameFlow ? "Please enter your registered email." : "Please enter your username or email.", false);
            return;
        }
        if (!context.mailer().isConfigured()) {
            message("The email service is not set up yet. Please contact the system administrator.", false);
            System.err.println("Fill in gmail.user and gmail.app.password in mail.properties (see README).");
            return;
        }
        try {
            if (usernameFlow) {
                List<User> found = userRepo.findByEmail(id);
                account = found.isEmpty() ? null : found.get(0);
            } else {
                account = userRepo.findByUsernameOrEmail(id);
            }
        } catch (SQLException e) {
            Ui.error(e);
            return;
        }
        if (account == null) {
            message(usernameFlow ? "No account is registered with that email."
                                 : "No account found with that username or email.", false);
            return;
        }
        if (!account.hasEmail()) {
            message("This account has no email on file, so a code cannot be sent. Please contact your administrator.", false);
            return;
        }
        long wait = context.otp().cooldownLeft(String.valueOf(account.getId()));
        if (wait > 0) {              // code was just sent: continue straight to the OTP step
            goToOtpStep();
            message("A code was already sent. You can request another in " + wait + "s.", true);
            return;
        }
        sendCode();
    }

    @FXML
    private void onResend() {
        if (account == null) return;
        long wait = context.otp().cooldownLeft(String.valueOf(account.getId()));
        if (wait > 0) {
            message("Please wait " + wait + "s before requesting a new code.", false);
            return;
        }
        sendCode();
    }

    private void goToOtpStep() {
        otpInfoLbl.setText("We sent a 6-digit code to " + mask(account.getEmail()) + ". It expires in "
                + OtpService.VALID_MINUTES + " minutes.");
        showStep(2);
        otpField.clear();
        otpField.requestFocus();
        startCooldown();
    }

    private void sendCode() {
        final User acc = account;
        final String code = context.otp().generate(String.valueOf(acc.getId()));
        final String purpose = usernameFlow ? "find your username" : "reset your password";
        sendBtn.setDisable(true);
        resendBtn.setDisable(true);
        message("Sending code...", true);

        Thread t = new Thread(() -> {
            try {
                context.mailer().sendOtp(acc.getEmail(), acc.getFullName(), code, OtpService.VALID_MINUTES, purpose);
                Platform.runLater(() -> {
                    sendBtn.setDisable(false);
                    goToOtpStep();
                    message("Code sent! Check your inbox (and Spam folder).", true);
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                context.otp().discard(String.valueOf(acc.getId()));
                Platform.runLater(() -> {
                    sendBtn.setDisable(false);
                    resendBtn.setDisable(false);
                    message(friendly(ex), false);
                });
            }
        }, "otp-mail");
        t.setDaemon(true);
        t.start();
    }

    private static String friendly(Exception ex) {
        String m = String.valueOf(ex.getMessage()).toLowerCase();
        if (m.contains("535") || m.contains("authentication") || m.contains("username and password"))
            return "The system email login was rejected. Please contact the administrator (check the App Password in mail.properties).";
        if (m.contains("unknownhost") || m.contains("connect") || m.contains("timeout"))
            return "Cannot reach the email server. Please check your internet connection and try again.";
        return "Could not send the email. Please try again.";
    }

    private void startCooldown() {
        if (countdown != null) countdown.stop();
        long[] left = {Math.max(1, context.otp().cooldownLeft(String.valueOf(account.getId())))};
        resendBtn.setDisable(true);
        resendBtn.setText("Resend code (" + left[0] + "s)");
        countdown = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            left[0]--;
            if (left[0] <= 0) {
                resendBtn.setText("Resend code");
                resendBtn.setDisable(false);
                countdown.stop();
            } else {
                resendBtn.setText("Resend code (" + left[0] + "s)");
            }
        }));
        countdown.setCycleCount((int) left[0]);
        countdown.play();
    }

    // ---------- Step 2: verify ----------
    @FXML
    private void onVerify() {
        String code = otpField.getText().trim();
        if (code.length() != 6) {
            message("Enter the 6-digit code.", false);
            return;
        }
        switch (context.otp().verify(String.valueOf(account.getId()), code)) {
            case OK -> {
                verified = true;
                if (countdown != null) countdown.stop();
                context.otp().discard(String.valueOf(account.getId()));
                if (usernameFlow) showUsername(); else { showStep(3); newPassField.requestFocus(); }
            }
            case WRONG -> message("Incorrect code. Please check the email and try again.", false);
            case EXPIRED -> message("That code has expired. Click \"Resend code\" to get a new one.", false);
            case TOO_MANY -> message("Too many wrong attempts. Click \"Resend code\" to get a new code.", false);
            case NONE -> message("No active code. Click \"Resend code\" to get a new one.", false);
        }
    }

    // ---------- Step 3 (username flow): show username(s) ----------
    private void showUsername() {
        String names;
        try {
            names = userRepo.findByEmail(account.getEmail()).stream()
                    .map(u -> u.getUsername()).collect(Collectors.joining(", "));
        } catch (SQLException e) {
            names = account.getUsername();
        }
        resultLbl.setText(names);
        showStep(4);
    }

    @FXML
    private void onAlsoResetPassword() {      // from the username result: continue without a second code
        if (!verified) return;
        showStep(3);
        newPassField.requestFocus();
    }

    // ---------- Step 3: new password ----------
    @FXML
    private void onReset() {
        if (!verified || account == null) {
            message("Please verify the code first.", false);
            return;
        }
        String p = newPassField.getText();
        if (p.length() < 8) {
            message("Password must be at least 8 characters.", false);
            return;
        }
        if (!p.equals(confirmField.getText())) {
            message("Passwords do not match.", false);
            return;
        }
        try {
            if (!userRepo.resetPassword(account.getId(), p)) {
                message("Could not update the password. Please try again.", false);
                return;
            }
            Ui.info("Password Changed", "Your password was updated.\nYour username: " + account.getUsername()
                    + "\nYou can now log in.");
            App.setRoot("Login", "Login");
        } catch (SQLException e) {
            Ui.error(e);
        }
    }

    // ---------- navigation (never loses what was typed) ----------
    @FXML
    private void onBackToStart() {         // from OTP step: back to step 1 with the same text still filled in
        if (countdown != null) countdown.stop();
        showStep(1);
        idField.requestFocus();
    }

    @FXML
    private void onBack() {
        if (countdown != null) countdown.stop();
        App.setRoot("Login", "Login");
    }
}
