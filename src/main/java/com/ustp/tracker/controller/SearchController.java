package com.ustp.tracker.controller;

import com.ustp.tracker.Session;
import com.ustp.tracker.Ui;
import com.ustp.tracker.model.Task;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.List;
import java.util.TreeSet;

public class SearchController extends BaseController {
    @FXML private TableView<Task> table;
    @FXML private TableColumn<Task, String> colName, colSubject, colDeadline;
    @FXML private TableColumn<Task, Task> colStatus;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> subjectBox, statusBox;
    private FilteredList<Task> filtered;

    @FXML
    private void initialize() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No matching tasks."));
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name()));
        colSubject.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().subject()));
        colDeadline.setCellValueFactory(d -> new SimpleStringProperty(Ui.FMT.format(d.getValue().deadline())));
        colStatus.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue()));
        colStatus.setCellFactory(c -> new TableCell<Task, Task>() {
            @Override protected void updateItem(Task t, boolean empty) {
                super.updateItem(t, empty);
                setGraphic(empty || t == null ? null : Ui.pill(t.status()));
            }
        });
        try {
            List<Task> all = taskRepo.findAll(Session.getUser().getId());
            filtered = new FilteredList<>(FXCollections.observableArrayList(all), t -> true);
            table.setItems(filtered);
            subjectBox.getItems().add("All Subjects");
            subjectBox.getItems().addAll(new TreeSet<>(all.stream().map(Task::subject).toList()));
        } catch (SQLException e) {
            Ui.error(e);
            filtered = new FilteredList<>(FXCollections.observableArrayList(), t -> true);
        }
        subjectBox.setValue("All Subjects");
        statusBox.getItems().addAll("All Status", "Pending", "Completed");
        statusBox.setValue("All Status");
        searchField.textProperty().addListener((o, a, b) -> apply());
        subjectBox.valueProperty().addListener((o, a, b) -> apply());
        statusBox.valueProperty().addListener((o, a, b) -> apply());
    }

    private void apply() {
        String q = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String sub = subjectBox.getValue(), st = statusBox.getValue();
        filtered.setPredicate(t ->
                (q.isEmpty() || t.name().toLowerCase().contains(q) || t.subject().toLowerCase().contains(q))
                        && (sub == null || sub.equals("All Subjects") || t.subject().equals(sub))
                        && (st == null || st.equals("All Status") || t.status().equals(st)));
    }
}
