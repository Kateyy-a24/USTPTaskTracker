package com.ustp.tracker.controller;

import com.ustp.tracker.Session;
import com.ustp.tracker.Ui;
import com.ustp.tracker.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileController extends BaseController {
    @FXML private TextField nameField, emailField, studentIdField, collegeField, programField, semesterField;
    @FXML private StackPane avatarHolder;
    @FXML private Label roleChip;
    @FXML private Button editBtn, photoBtn;
    @FXML private HBox actionBar;

    private final User u = Session.getUser();
    private File newPhoto;
    private List<TextField> fields;

    @FXML
    private void initialize() {
        fields = List.of(nameField, emailField, studentIdField, collegeField, programField, semesterField);
        fill();
        setEditing(false);
    }

    private void fill() {
        nameField.setText(u.getFullName());
        emailField.setText(u.getEmail());
        studentIdField.setText(u.getStudentId());
        collegeField.setText(u.getCollege());
        programField.setText(u.getProgram());
        semesterField.setText(u.getSemester());
        avatarHolder.getChildren().setAll(Ui.avatar(u, 110));
        Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(u.getProgram() == null ? "" : u.getProgram());
        roleChip.setText(m.find() ? m.group(1) + " Student" : "Student");
    }

    private void setEditing(boolean on) {
        for (TextField f : fields) {
            f.setEditable(on);
            f.setFocusTraversable(on);
            if (on) { if (!f.getStyleClass().contains("editing")) f.getStyleClass().add("editing"); }
            else f.getStyleClass().remove("editing");
        }
        editBtn.setVisible(!on);   editBtn.setManaged(!on);
        photoBtn.setVisible(on);   photoBtn.setManaged(on);
        actionBar.setVisible(on);  actionBar.setManaged(on);
    }

    @FXML private void onEdit() { setEditing(true); }

    @FXML
    private void onPhoto() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose profile picture");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File f = fc.showOpenDialog(avatarHolder.getScene().getWindow());
        if (f != null) {
            newPhoto = f;
            User preview = new User();
            preview.setAvatarPath(f.getAbsolutePath());
            avatarHolder.getChildren().setAll(Ui.avatar(preview, 110));
        }
    }

    @FXML
    private void onDone() {
        if (nameField.getText().trim().isEmpty()) {
            Ui.error("Name cannot be empty.");
            return;
        }
        String newEmail = emailField.getText().trim();
        if (!newEmail.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            Ui.error("Please enter a valid email address. This is where your password reset code is sent.");
            return;
        }
        try {
            if (userRepo.emailTakenByOther(newEmail, u.getId())) {
                Ui.error("That email is already used by another account.");
                return;
            }
            if (newPhoto != null) {
                Path dir = Path.of(System.getProperty("user.home"), "USTPTaskTracker", "avatars");
                Files.createDirectories(dir);
                String name = newPhoto.getName();
                String ext = name.contains(".") ? name.substring(name.lastIndexOf('.')) : ".png";
                Path target = dir.resolve("user_" + u.getId() + "_" + System.currentTimeMillis() + ext);
                Files.copy(newPhoto.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
                u.setAvatarPath(target.toString());
                newPhoto = null;
            }
            u.setFullName(nameField.getText().trim());
            u.setEmail(emailField.getText().trim());
            u.setStudentId(studentIdField.getText().trim());
            u.setCollege(collegeField.getText().trim());
            u.setProgram(programField.getText().trim());
            u.setSemester(semesterField.getText().trim());
            userRepo.updateProfile(u);
            Session.getMain().refreshUser();
            fill();
            setEditing(false);
        } catch (IOException | java.sql.SQLException e) {
            Ui.error(e);
        }
    }

    @FXML
    private void onCancel() {
        newPhoto = null;
        fill();
        setEditing(false);
    }
}
