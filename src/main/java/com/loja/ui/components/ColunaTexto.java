package com.loja.ui.components;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

//nao vai permitir que a tabela da tela de funcionarios quebre por falta de algum dado
public abstract class ColunaTexto<T>
        implements Callback<TableColumn.CellDataFeatures<T, String>, ObservableValue<String>> {

    @Override
    public ObservableValue<String> call(TableColumn.CellDataFeatures<T, String> dados) {
        String texto;
        try {
            texto = valor(dados.getValue());
        } catch (RuntimeException e) {
            texto = "-";
        }
        return new SimpleStringProperty(texto == null ? "-" : texto);
    }

    protected abstract String valor(T item);
}