package com.loja.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class StatusBadge extends HBox {

    public enum StatusType {
        SUCCESS, WARNING, DANGER, INFO
    }

    private final Label label = new Label();

    public StatusBadge(String text, StatusType type) {
        super();
        setAlignment(Pos.CENTER);
        setPadding(new Insets(4, 10, 4, 10));

        label.setText(text != null ? text.toUpperCase() : "");
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: 700;");

        applyColors(type);
        getChildren().add(label);
    }

    private void applyColors(StatusType type) {
        switch (type) {
            case SUCCESS:
                this.setStyle("-fx-background-color: #d1fae5; -fx-background-radius: 20px;");
                label.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #065f46;");
                break;
            case WARNING:
                this.setStyle("-fx-background-color: #fef3c7; -fx-background-radius: 20px;");
                label.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #92400e;");
                break;
            case DANGER:
                this.setStyle("-fx-background-color: #fee2e2; -fx-background-radius: 20px;");
                label.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #991b1b;");
                break;
            case INFO:
            default:
                this.setStyle("-fx-background-color: #e0e7ff; -fx-background-radius: 20px;");
                label.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #3730a3;");
                break;
        }
    }
}