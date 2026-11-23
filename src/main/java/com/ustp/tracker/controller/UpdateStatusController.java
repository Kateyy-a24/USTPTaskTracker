package com.ustp.tracker.controller;

import com.ustp.tracker.Session;
import com.ustp.tracker.Ui;
import com.ustp.tracker.model.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class UpdateStatusController extends BaseController {
    @FXML private TextField nameField;
    @FXML private Label currentLbl;
    @FXML private ComboBox<String> newBox;
    private Task task;

    @FXML
    private void initialize() {
        task = Session.getStatusTask();
        nameField.setText(task.name());
        currentLbl.setText(task.status());
        currentLbl.getStyleClass().add(task.status().equals("Completed") ? "text-green" : "text-orange");
        newBox.getItems().addAll("Pending", "Completed");
        newBox.setValue(task.status().equals("Pending") ? "Completed" : "Pending");
    }

    @FXML
    private void onUpdate() {
        try {
            taskRepo.setStatus(task.id(), newBox.getValue());
            Session.getMain().showViewTasks();
        } catch (SQLException e) {
            Ui.error(e);
        }
    }

    @FXML
    private void onCancel() { Session.getMain().showViewTasks(); }
}
