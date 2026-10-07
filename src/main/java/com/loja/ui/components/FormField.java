package com.loja.ui.components;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.VBox;

public class FormField extends VBox {

    private final Label label = new Label();
    private final TextInputControl inputField;

    public FormField(String labelText, TextInputControl inputControl) {
        super(6);
        this.inputField = inputControl;

        label.setText(labelText != null ? labelText.toUpperCase() : "");
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #4b5563;");

        inputField.setStyle(
                "-fx-background-color: #ffffff; " +
                        "-fx-border-color: #d1d5db; " +
                        "-fx-border-radius: 10px; " +
                        "-fx-background-radius: 10px; " +
                        "-fx-padding: 8px 12px; " +
                        "-fx-font-size: 13px;"
        );

        getChildren().addAll(label, inputField);
    }

    public TextInputControl getInputField() {
        return inputField;
    }

    public String getText() {
        return inputField.getText();
    }

    public void setText(String value) {
        inputField.setText(value);
    }
}