package com.loja.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class EmptyState extends VBox {

    public EmptyState(String message) {
        super(8);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(30));

        Label msgLabel = new Label(message != null ? message : "Nenhum registro encontrado.");
        msgLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #9ca3af; -fx-font-weight: 500;");

        getChildren().add(msgLabel);
    }
}