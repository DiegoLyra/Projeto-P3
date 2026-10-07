package com.loja.ui.components;

import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;

public class PillNav extends HBox {

    private Button activeBtn = null;

    public PillNav() {
        super(8);
        setPadding(new Insets(6));
        setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 12px; -fx-border-color: #e5e7eb; -fx-border-radius: 12px;");
    }

    public Button addTab(String text, boolean active, Runnable onClick) {
        Button btn = new Button(text);
        setStyleButton(btn, active);
        if (active) {
            activeBtn = btn;
        }

        btn.setOnAction(e -> {
            if (activeBtn != null) {
                setStyleButton(activeBtn, false);
            }
            setStyleButton(btn, true);
            activeBtn = btn;
            if (onClick != null) {
                onClick.run();
            }
        });

        getChildren().add(btn);
        return btn;
    }

    private void setStyleButton(Button btn, boolean active) {
        btn.setStyle(
                "-fx-font-size: 12px; " +
                        "-fx-font-weight: 600; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 8px 14px; " +
                        (active ?
                                "-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 2, 0, 0, 1);" :
                                "-fx-background-color: transparent; -fx-text-fill: #4b5563;")
        );
    }
}