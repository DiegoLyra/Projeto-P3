package com.loja.ui.components;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class Card extends VBox {

    public Card(Node... children) {
        super(children);
        initializeStyle();
    }

    public Card() {
        super();
        initializeStyle();
    }

    private void initializeStyle() {
        this.setSpacing(12);
        this.setPadding(new Insets(20));
        this.setStyle(
                "-fx-background-color: #ffffff; " +
                        "-fx-background-radius: 16px; " +
                        "-fx-border-color: #e5e7eb; " +
                        "-fx-border-radius: 16px; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.03), 6, 0, 0, 2);"
        );
    }
}