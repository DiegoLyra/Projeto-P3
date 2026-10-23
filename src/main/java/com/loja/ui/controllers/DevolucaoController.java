
package com.loja.ui.controllers;

import com.loja.model.ContratoAluguel;
import com.loja.padrao.facade.interfaces.ILojaFacade;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class DevolucaoController {

    @FXML
    private TextField txtContratoId;

    private final ILojaFacade facade;

    public DevolucaoController(ILojaFacade facade) {
        this.facade = facade;
    }

    @FXML
    private void finalizarDevolucao() {
        String contratoId = txtContratoId.getText().trim();

        if (contratoId.isEmpty()) {
            mostrarAlerta(
                Alert.AlertType.WARNING,
                "Campo obrigatório",
                "Digite o ID do contrato de aluguel."
            );
            return;
        }

        try {
            ContratoAluguel contrato =
                facade.processarDevolucao(contratoId);

            mostrarAlerta(
                Alert.AlertType.INFORMATION,
                "Devolução concluída",
                "Contrato " + contrato.getId()
                    + " encerrado com sucesso."
            );

            txtContratoId.clear();

        } catch (RuntimeException e) {
            mostrarAlerta(
                Alert.AlertType.ERROR,
                "Erro na devolução",
                e.getMessage() != null
                    ? e.getMessage()
                    : "Não foi possível processar a devolução."
            );
        }
    }

    private void mostrarAlerta(
            Alert.AlertType tipo,
            String titulo,
            String mensagem) {

        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}
