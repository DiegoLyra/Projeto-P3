package com.loja.ui.components;

import javafx.beans.DefaultProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.VBox;

@DefaultProperty("inputControl")
public class FormField extends VBox {

    private final Label label = new Label();
    private TextInputControl inputField;

    public FormField() {
        super(6);
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #4b5563;");
        getChildren().add(label);
    }

    public FormField(String labelText, TextInputControl inputControl) {
        this();
        setLabelText(labelText);
        setInputControl(inputControl);
    }

    public String getLabelText() { return label.getText(); }

    public void setLabelText(String labelText) {
        label.setText(labelText != null ? labelText.toUpperCase() : "");
    }

    public TextInputControl getInputControl() { return inputField; }

    public void setInputControl(TextInputControl inputControl) {
        if (inputField != null) {
            getChildren().remove(inputField);
        }
        inputField = inputControl;
        if (inputField != null) {
            inputField.setStyle(
                    "-fx-background-color: #ffffff; -fx-border-color: #d1d5db; " +
                            "-fx-border-radius: 10px; -fx-background-radius: 10px; " +
                            "-fx-padding: 8px 12px; -fx-font-size: 13px;");
            getChildren().add(inputField);
        }
    }

    public TextInputControl getInputField() { return inputField; }

    public String getText() { return inputField != null ? inputField.getText() : ""; }

    public void setText(String value) {
        if (inputField != null) {
            inputField.setText(value);
        }
    }
}