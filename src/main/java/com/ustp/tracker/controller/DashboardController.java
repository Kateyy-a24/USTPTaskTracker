package com.ustp.tracker.controller;

import com.ustp.tracker.Session;
import com.ustp.tracker.Ui;
import com.ustp.tracker.model.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class DashboardController extends BaseController {
    @FXML private Label pendingLbl, completedLbl, upcomingLbl;
    @FXML private VBox highlights;

    @FXML
    private void initialize() {
        try {
            List<Task> tasks = taskRepo.findAll(Session.getUser().getId());
            LocalDate today = LocalDate.now();
            long pending = tasks.stream().filter(t -> t.status().equals("Pending")).count();
            long done = tasks.size() - pending;
            long upcoming = tasks.stream().filter(t -> t.status().equals("Pending") && !t.deadline().isBefore(today)).count();
            pendingLbl.setText(String.valueOf(pending));
            completedLbl.setText(String.valueOf(done));
            upcomingLbl.setText(String.valueOf(upcoming));

            if (tasks.isEmpty()) {
                Label l = new Label("No tasks yet. Add your first task!");
                l.getStyleClass().add("muted");
                highlights.getChildren().add(l);
            }
            tasks.stream().limit(5).forEach(t -> {
                VBox text = new VBox(2);
                Label name = new Label(t.name());
                name.getStyleClass().add("row-title");
                Label due = new Label(Ui.dueText(t.deadline()) + " : " + Ui.FMT.format(t.deadline()));
                due.getStyleClass().add("muted-small");
                text.getChildren().addAll(name, due);
                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);
                HBox row = new HBox(text, sp, Ui.pill(t.status()));
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(10, 0, 10, 0));
                row.getStyleClass().add("list-row");
                highlights.getChildren().add(row);
            });
        } catch (SQLException e) {
            Ui.error(e);
        }
    }

    @FXML
    private void showAll() { Session.getMain().showViewTasks(); }
}
