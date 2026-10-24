
package com.loja.ui.controllers;

import com.loja.model.Cliente;
import com.loja.padrao.facade.interfaces.ILojaFacade;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NovoClienteFuncionarioController {

    private static final Logger logger =
            LoggerFactory.getLogger(NovoClienteFuncionarioController.class);

    @FXML
    private TextField idField;

    @FXML
    private TextField nomeField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField senhaField;

    @FXML
    private Label mensagemLabel;

    @FXML
    private Button cadastrarButton;

    private ILojaFacade facade;

    /**
     * Recebe a fachada utilizada para cadastrar clientes.
     */
    public void setFacade(ILojaFacade facade) {
        this.facade = facade;
    }

    @FXML
    private void handleCadastrar() {
        String id = idField.getText().trim();
        String nome = nomeField.getText().trim();
        String email = emailField.getText().trim();
        String senha = senhaField.getText();

        // Verifica se todos os campos foram preenchidos.
        if (id.isEmpty() || nome.isEmpty()
                || email.isEmpty() || senha.isBlank()) {
            exibirMensagem("Preencha todos os campos.", false);
            return;
        }

        // Verifica se a fachada foi configurada.
        if (facade == null) {
            exibirMensagem(
                    "Erro interno: serviço de cadastro não configurado.",
                    false
            );
            logger.error("ILojaFacade não foi configurada no controller.");
            return;
        }

        try {
            Cliente novoCliente = new Cliente(id, nome, email, senha);

            facade.cadastrarCliente(novoCliente);

            exibirMensagem("Cliente cadastrado com sucesso!", true);
            limparCampos();

        } catch (RuntimeException e) {
            logger.error(
                    "Falha ao cadastrar cliente '{}': {}",
                    id,
                    e.getMessage(),
                    e
            );

            String erro = e.getMessage();

            if (erro == null || erro.isBlank()) {
                erro = "Não foi possível cadastrar o cliente.";
            }

            exibirMensagem("Erro ao cadastrar: " + erro, false);
        }
    }

    @FXML
    private void handleLimpar() {
        limparCampos();
        mensagemLabel.setText("");
    }

    private void limparCampos() {
        idField.clear();
        nomeField.clear();
        emailField.clear();
        senhaField.clear();
    }

    private void exibirMensagem(String mensagem, boolean sucesso) {
        mensagemLabel.setText(mensagem);

        if (sucesso) {
            mensagemLabel.setStyle(
                    "-fx-font-size: 12px; -fx-text-fill: #16a34a;"
            );
        } else {
            mensagemLabel.setStyle(
                    "-fx-font-size: 12px; -fx-text-fill: #dc2626;"
            );
        }
    }
}

