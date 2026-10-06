package com.loja.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ItemCard extends Card {

    public ItemCard(String id, String nome, double taxaDiaria, Runnable onAction) {
        super();
        this.setStyle(
                this.getStyle() +
                        "-fx-background-color: #f9fafb; " +
                        "-fx-border-color: #e5e7eb;"
        );

        Label idLabel = new Label("#" + id);
        idLabel.setStyle("-fx-font-size: 11px; -fx-font-family: monospace; -fx-text-fill: #4f46e5; -fx-font-weight: bold;");

        Label nameLabel = new Label(nome);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        VBox topBox = new VBox(2, idLabel, nameLabel);

        Label lblTaxaDesc = new Label("Valor Diário:");
        lblTaxaDesc.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");

        Label lblTaxaVal = new Label(String.format("R$ %.2f", taxaDiaria));
        lblTaxaVal.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #059669;");

        HBox footerBox = new HBox(lblTaxaDesc, lblTaxaVal);
        footerBox.setAlignment(Pos.CENTER_LEFT);
        footerBox.setSpacing(8);
        footerBox.setPadding(new Insets(8, 0, 0, 0));
        footerBox.setStyle("-fx-border-color: #e5e7eb; -fx-border-width: 1 0 0 0;");

        Button actionBtn = new Button("Solicitar Aluguel");
        actionBtn.setMaxWidth(Double.MAX_VALUE);
        actionBtn.setStyle(
                "-fx-background-color: #4f46e5; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: 600; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 8px;"
        );
        actionBtn.setOnAction(e -> {
            if (onAction != null) onAction.run();
        });

        this.getChildren().addAll(topBox, footerBox, actionBtn);
    }
}