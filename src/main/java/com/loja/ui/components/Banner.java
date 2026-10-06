package com.loja.ui.components;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class Banner extends HBox {

    public enum BannerType {
        SUCCESS, WARNING, DANGER
    }

    public Banner(String message, BannerType type) {
        super(10);
        setPadding(new Insets(14));
        setStyle("-fx-background-radius: 12px; -fx-border-radius: 12px;");

        Label msgLabel = new Label(message);
        msgLabel.setWrapText(true);
        msgLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600;");

        applyStyleAndText(type, msgLabel);
        getChildren().add(msgLabel);
    }

    private void applyStyleAndText(BannerType type, Label label) {
        switch (type) {
            case SUCCESS:
                this.setStyle(this.getStyle() + "-fx-background-color: #ecfdf5; -fx-border-color: #a7f3d0;");
                label.setStyle(label.getStyle() + "-fx-text-fill: #047857;");
                break;
            case WARNING:
                this.setStyle(this.getStyle() + "-fx-background-color: #fffbeb; -fx-border-color: #fde68a;");
                label.setStyle(label.getStyle() + "-fx-text-fill: #b45309;");
                break;
            case DANGER:
                this.setStyle(this.getStyle() + "-fx-background-color: #fef2f2; -fx-border-color: #fecaca;");
                label.setStyle(label.getStyle() + "-fx-text-fill: #b91c1c;");
                break;
        }
    }
}