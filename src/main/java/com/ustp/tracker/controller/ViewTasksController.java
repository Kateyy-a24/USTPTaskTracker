package com.ustp.tracker.controller;

import com.ustp.tracker.Session;
import com.ustp.tracker.Ui;
import com.ustp.tracker.model.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;

import java.sql.SQLException;
import java.util.List;

/** "My Tasks" list. Click a task row to show its description right below it (click again to hide). */
public class ViewTasksController extends BaseController {
    @FXML private VBox tableCard, rowsBox;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterBox;

    // Task Name | Subject | Deadline | Status | Action  (percent widths, must add up to 100)
    private static final double[] COLS = {32, 14, 20, 17, 17};

    private List<Task> all = List.of();
    private int openId = -1;   // id of the task whose description is showing

    @FXML
    private void initialize() {
        tableCard.getChildren().add(0, header());
        filterBox.getItems().addAll("All", "Pending", "Completed");
        filterBox.setPromptText("Filter");
        searchField.textProperty().addListener((o, a, b) -> render());
        filterBox.valueProperty().addListener((o, a, b) -> render());
        load();
    }

    private GridPane grid() {
        GridPane g = new GridPane();
        for (double c : COLS) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(c);
            cc.setMinWidth(0);          // columns may shrink, so content never pushes the icons out of view
            cc.setHgrow(Priority.SOMETIMES);
            g.getColumnConstraints().add(cc);
        }
        g.getColumnConstraints().get(COLS.length - 1).setMinWidth(118);   // Action icons always fully visible
        g.setMaxWidth(Double.MAX_VALUE);
        return g;
    }

    private Node header() {
        GridPane g = grid();
        g.getStyleClass().add("tbl-header");
        String[] h = {"Task Name", "Subject", "Deadline", "Status", "Action"};
        for (int i = 0; i < h.length; i++) {
            Label l = new Label(h[i]);
            l.getStyleClass().add("tbl-head-label");
            GridPane.setHalignment(l, i == 4 ? HPos.CENTER : HPos.LEFT);
            g.add(l, i, 0);
        }
        return g;
    }

    private void load() {
        try {
            all = taskRepo.findAll(Session.getUser().getId());
        } catch (SQLException e) {
            Ui.error(e);
        }
        render();
    }

    private void render() {
        rowsBox.getChildren().clear();
        String q = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String st = filterBox.getValue();
        boolean any = false;
        for (Task t : all) {
            boolean match = (q.isEmpty() || t.name().toLowerCase().contains(q) || t.subject().toLowerCase().contains(q))
                    && (st == null || st.equals("All") || t.status().equals(st));
            if (!match) continue;
            any = true;
            rowsBox.getChildren().add(block(t));
        }
        if (!any) {
            Label l = new Label("No tasks found.");
            l.getStyleClass().add("muted");
            l.setPadding(new Insets(24));
            rowsBox.getChildren().add(l);
        }
    }

    private Node block(Task t) {
        GridPane g = grid();
        g.getStyleClass().add("tbl-row");

        Label name = new Label(t.name());
        name.getStyleClass().add("tbl-name");
        // never cut off / hide the task name: wrap long names instead of showing "..."
        name.setWrapText(true);
        name.setMinWidth(0);
        name.setMaxWidth(Double.MAX_VALUE);
        name.setTooltip(new Tooltip(t.name() + "\n(Click to show/hide description)"));
        Label subject = new Label(t.subject());
        subject.getStyleClass().add("tbl-cell");
        subject.setMinWidth(0);
        subject.setMaxWidth(Double.MAX_VALUE);
        subject.setWrapText(true);
        subject.setTextOverrun(OverrunStyle.ELLIPSIS);
        subject.setTooltip(new Tooltip(t.subject()));
        Label date = new Label(Ui.FMT.format(t.deadline()));
        date.getStyleClass().add("tbl-cell");
        date.setMinWidth(0);
        Label pill = Ui.pill(t.status());
        GridPane.setFillWidth(pill, false);
        GridPane.setHalignment(pill, HPos.LEFT);

        Button status = icon("M7 8h13 M16 4l4 4-4 4 M17 16H4 M8 12l-4 4 4 4", "icon-btn-gray", "Update status");
        Button edit = icon("M4 20h4L19 9l-4-4L4 16z M13.5 6.5l4 4", "icon-btn-blue", "Edit task");
        Button del = icon("M4 7h16 M10 11v6 M14 11v6 M6 7l1 13h10l1-13 M9 7V4h6v3", "icon-btn-red", "Delete task");
        status.setOnAction(e -> Session.getMain().showUpdateStatus(t));
        edit.setOnAction(e -> Session.getMain().showEditTask(t));
        del.setOnAction(e -> delete(t));
        HBox actions = new HBox(6, status, edit, del);
        actions.setAlignment(Pos.CENTER);
        actions.setMinWidth(Region.USE_PREF_SIZE);

        Node[] cells = {name, subject, date, pill, actions};
        for (int i = 0; i < cells.length; i++) {
            GridPane.setValignment(cells[i], VPos.CENTER);
            g.add(cells[i], i, 0);
        }
        g.setOnMouseClicked(e -> {
            openId = (openId == t.id()) ? -1 : t.id();
            render();
        });

        VBox block = new VBox(g);
        block.getStyleClass().add("tbl-block");
        if (openId == t.id()) {
            block.getStyleClass().add("tbl-open");
            boolean blank = t.description() == null || t.description().isBlank();
            Label d = new Label(blank ? "No description provided." : t.description().trim());
            d.setWrapText(true);
            d.setMaxWidth(Double.MAX_VALUE);
            d.getStyleClass().add(blank ? "tbl-desc-empty" : "tbl-desc");
            block.getChildren().add(d);
        }
        return block;
    }

    private Button icon(String path, String style, String tip) {
        SVGPath p = new SVGPath();
        p.setContent(path);
        p.getStyleClass().add("act-icon");
        p.setScaleX(0.7);
        p.setScaleY(0.7);
        StackPane box = new StackPane(p);
        box.setMinSize(16, 16);
        box.setPrefSize(16, 16);
        box.setMaxSize(16, 16);
        Button b = new Button();
        b.setGraphic(box);
        b.getStyleClass().addAll("icon-btn", style);
        b.setTooltip(new Tooltip(tip));
        // clicking an action icon must not also expand/collapse the row
        b.addEventFilter(MouseEvent.MOUSE_CLICKED, Event::consume);
        return b;
    }

    private void delete(Task t) {
        if (!Ui.confirmDelete(t.name())) return;
        try {
            taskRepo.delete(t.id());
            if (openId == t.id()) openId = -1;
            load();
        } catch (SQLException e) {
            Ui.error(e);
        }
    }
}
