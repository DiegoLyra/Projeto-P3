package com.loja.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class PageHeader extends HBox {

    private final Label titleLabel = new Label();
    private final Label subtitleLabel = new Label();

    public PageHeader() {
        super();
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(0, 0, 16, 0));
        setStyle("-fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");

        VBox textContainer = new VBox(4);

        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        subtitleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280; -fx-font-weight: 600;");

        textContainer.getChildren().addAll(titleLabel, subtitleLabel);
        setHgrow(textContainer, Priority.ALWAYS);

        getChildren().add(textContainer);
    }

    public PageHeader(String title, String subtitle) {
        this();
        setTitle(title);
        setSubtitle(subtitle);
    }

    public String getTitle() {
        return titleLabel.getText();
    }

    public void setTitle(String title) {
        titleLabel.setText(title != null ? title.toUpperCase() : "");
    }

    public String getSubtitle() {
        return subtitleLabel.getText();
    }

    public void setSubtitle(String subtitle) {
        subtitleLabel.setText(subtitle != null ? subtitle.toUpperCase() : "");
    }
}