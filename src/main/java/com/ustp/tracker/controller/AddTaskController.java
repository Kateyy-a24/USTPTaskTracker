package com.ustp.tracker.controller;

import com.ustp.tracker.Session;
import com.ustp.tracker.Ui;
import com.ustp.tracker.model.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.time.LocalDate;

/** Used for both "Add Task" and "Edit Task" (when Session.getEditTask() is set). */
public class AddTaskController extends BaseController {
    @FXML private TextField nameField;
    @FXML private ComboBox<String> subjectBox, statusBox;
    @FXML private TextArea descArea;
    @FXML private DatePicker deadlinePicker;
    @FXML private Button saveBtn;
    private Task editing;

    @FXML
    private void initialize() {
        subjectBox.getItems().addAll("IT211", "IT212", "IT213", "IT214", "IT215", "IT216");
        statusBox.getItems().addAll("Pending", "Completed");
        statusBox.setValue("Pending");
        editing = Session.getEditTask();
        if (editing != null) {
            nameField.setText(editing.name());
            subjectBox.setValue(editing.subject());
            descArea.setText(editing.description());
            deadlinePicker.setValue(editing.deadline());
            statusBox.setValue(editing.status());
            saveBtn.setText("Save Changes");
        }
    }

    @FXML
    private void onSave() {
        String name = nameField.getText().trim();
        String subject = subjectBox.getEditor().getText() == null ? "" : subjectBox.getEditor().getText().trim();
        LocalDate deadline = deadlinePicker.getValue();
        if (name.isEmpty() || subject.isEmpty() || deadline == null) {
            Ui.error("Please fill in the Task Name, Subject and Deadline.");
            return;
        }
        String desc = descArea.getText() == null ? "" : descArea.getText().trim();
        try {
            if (editing == null) {
                taskRepo.add(Session.getUser().getId(), new Task(0, name, subject, desc, deadline, statusBox.getValue()));
            } else {
                taskRepo.update(new Task(editing.id(), name, subject, desc, deadline, statusBox.getValue()));
            }
            Session.setEditTask(null);
            Session.getMain().showViewTasks();
        } catch (SQLException e) {
            Ui.error(e);
        }
    }

    @FXML
    private void onCancel() {
        Session.setEditTask(null);
        Session.getMain().showViewTasks();
    }
}
