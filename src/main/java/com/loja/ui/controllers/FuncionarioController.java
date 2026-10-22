package com.loja.ui.controllers;

import com.loja.model.Cliente;
import com.loja.model.ContratoAluguel;
import com.loja.padrao.facade.interfaces.ILojaFacade;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class FuncionarioController {

    @FXML private TextField txtContratoId;
    @FXML private TextField txtClienteId;
    @FXML private TextField txtNomeCliente;
    @FXML private TextField txtClienteEmail;
    @FXML private PasswordField txtClienteSenha;
    @FXML private TextField txtMultaId;

    private ILojaFacade facade;

    // pelo que eu entendi isso vai ser chamado em outro lugar
    public void setFacade(ILojaFacade facade) {
        this.facade = facade;
    }

    // devolução
    @FXML
    private void finalizarDevolucao() {
        try {
            ContratoAluguel contrato = facade.processarDevolucao(txtContratoId.getText().trim());
            mostrar(AlertType.INFORMATION, "Devolução concluída",
                    "Contrato " + contrato.getId() + " encerrado.");
            txtContratoId.clear();
        } catch (Exception e) {
            mostrar(AlertType.ERROR, "Erro na devolução", e.getMessage());
        }
    }

    // novo cliente
    @FXML
    private void cadastrarCliente() {
        try {
            Cliente novo = facade.cadastrarCliente(
                    txtNomeCliente.getText().trim(),
                    txtClienteEmail.getText().trim(),
                    txtClienteSenha.getText());
            mostrar(AlertType.INFORMATION, "Cliente cadastrado",
                    "Cliente " + novo.getNome() + " salvo.");
            txtClienteId.clear();
            txtNomeCliente.clear();
            txtClienteEmail.clear();
            txtClienteSenha.clear();
        } catch (Exception e) {
            mostrar(AlertType.ERROR, "Erro no cadastro", e.getMessage());
        }
    }

    // multa
    @FXML
    private void quitarMulta() {
        try {
            facade.quitarMulta(txtMultaId.getText().trim());
            mostrar(AlertType.INFORMATION, "Multa quitada", "Baixa realizada com sucesso.");
            txtMultaId.clear();
        } catch (Exception e) {
            mostrar(AlertType.ERROR, "Erro ao quitar multa", e.getMessage());
        }
    }

    private void mostrar(AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
