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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.sql.SQLException;
import java.util.List;

public class UpcomingController extends BaseController {
    @FXML private VBox list;

    @FXML
    private void initialize() {
        try {
            List<Task> pending = taskRepo.findAll(Session.getUser().getId()).stream()
                    .filter(t -> t.status().equals("Pending")).toList();
            if (pending.isEmpty()) {
                Label l = new Label("No upcoming deadlines. 🎉");
                l.getStyleClass().add("muted");
                list.getChildren().add(l);
            }
            for (Task t : pending) {
                Circle dot = new Circle(4, Color.web(Ui.dotColor(t.deadline())));
                Label name = new Label(t.name());
                name.getStyleClass().add("row-title");
                Label due = new Label(Ui.dueText(t.deadline()));
                due.getStyleClass().add("muted-small");
                VBox text = new VBox(2, name, due);
                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);
                Label date = new Label(Ui.FMT.format(t.deadline()));
                date.getStyleClass().add("date-label");
                HBox row = new HBox(12, dot, text, sp, date);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(10, 0, 10, 0));
                row.getStyleClass().add("list-row");
                list.getChildren().add(row);
            }
        } catch (SQLException e) {
            Ui.error(e);
        }
    }
}
