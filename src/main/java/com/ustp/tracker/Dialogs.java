package com.ustp.tracker;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/** Custom dialog boxes that match the prototype (white rounded card over a dimmed window). */
public final class Dialogs {
    public enum Kind { DANGER, INFO, SUCCESS }

    public static final String LOGOUT = "M9 4H5v16h4 M16 8l4 4-4 4 M20 12H9";
    public static final String TRASH = "M4 7h16 M10 11v6 M14 11v6 M6 7l1 13h10l1-13 M9 7V4h6v3";
    public static final String ALERT = "M12 3a9 9 0 1 0 0 18a9 9 0 0 0 0-18z M12 8v5 M12 16v.01";
    public static final String CHECK = "M5 13l4 4L19 7";
    public static final String INFO = "M12 3a9 9 0 1 0 0 18a9 9 0 0 0 0-18z M12 11v6 M12 7.5v.01";

    private Dialogs() {}

    /** Shows the dialog and returns true when the main (right) button was pressed. cancelText = null -> single button. */
    public static boolean show(Kind kind, String icon, String title, String message, String okText, String cancelText) {
        Stage owner = App.stage();
        Stage st = new Stage(StageStyle.TRANSPARENT);
        st.initModality(Modality.APPLICATION_MODAL);
        double w = 700, h = 450;
        if (owner != null) {
            st.initOwner(owner);
            st.setX(owner.getX());
            st.setY(owner.getY());
            w = owner.getWidth();
            h = owner.getHeight();
            st.setWidth(w);
            st.setHeight(h);
        }
        boolean[] result = {false};

        SVGPath p = new SVGPath();
        p.setContent(icon);
        p.getStyleClass().add("dlg-icon");
        p.setScaleX(1.25);
        p.setScaleY(1.25);
        StackPane circle = new StackPane(p);
        circle.getStyleClass().addAll("dlg-circle", "dlg-circle-" + kind.name().toLowerCase());
        circle.setMinSize(64, 64);
        circle.setPrefSize(64, 64);
        circle.setMaxSize(64, 64);

        Label t = new Label(title);
        t.getStyleClass().add("dlg-title");
        Label m = new Label(message);
        m.getStyleClass().add("dlg-msg");
        m.setWrapText(true);
        m.setTextAlignment(TextAlignment.CENTER);
        m.setAlignment(Pos.CENTER);
        m.setMaxWidth(400);

        Button ok = new Button(okText);
        ok.setDefaultButton(true);
        ok.getStyleClass().add(kind == Kind.DANGER && cancelText != null ? "dlg-btn-danger" : "primary-btn");
        ok.setMaxWidth(Double.MAX_VALUE);
        ok.setOnAction(e -> { result[0] = true; st.close(); });

        HBox buttons = new HBox(14);
        buttons.setAlignment(Pos.CENTER);
        if (cancelText != null) {
            Button cancel = new Button(cancelText);
            cancel.setCancelButton(true);
            cancel.getStyleClass().add("secondary-btn");
            cancel.setMaxWidth(Double.MAX_VALUE);
            cancel.setOnAction(e -> st.close());
            HBox.setHgrow(cancel, Priority.ALWAYS);
            HBox.setHgrow(ok, Priority.ALWAYS);
            buttons.getChildren().addAll(cancel, ok);
        } else {
            ok.setPrefWidth(180);
            buttons.getChildren().add(ok);
        }

        VBox card = new VBox(12, circle, t, m, buttons);
        VBox.setMargin(buttons, new Insets(14, 0, 0, 0));
        card.getStyleClass().add("dlg-card");
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(500);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        StackPane root = new StackPane(card);
        root.getStyleClass().add("dlg-overlay");
        Scene sc = new Scene(root, w, h);
        sc.setFill(Color.TRANSPARENT);
        sc.getStylesheets().add(Dialogs.class.getResource("/css/style.css").toExternalForm());
        sc.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ESCAPE) st.close(); });
        st.setScene(sc);
        st.showAndWait();
        return result[0];
    }
}
