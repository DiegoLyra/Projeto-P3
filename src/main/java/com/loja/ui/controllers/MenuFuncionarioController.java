package com.loja.ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class MenuFuncionarioController {

    @FXML
    private Label lblNomeFuncionario;

    @FXML
    private StackPane conteudoArea;

    @FXML
    public void initialize() {
        abrirAluguel();
    }

    public void setUsuarioLogado(String nomeFuncionario) {
        if (nomeFuncionario != null && !nomeFuncionario.isBlank()) {
            lblNomeFuncionario.setText("PAINEL DO FUNCIONÁRIO: " + nomeFuncionario.toUpperCase());
        }
    }

    // AJUSTE DOS CAMINHOS COM A PASTA CORRETA /fxml/funcionario/
    @FXML private void abrirAluguel() { carregarTela("/fxml/funcionario/TelaAluguel.fxml"); }
    @FXML private void abrirDevolucao() { carregarTela("/fxml/funcionario/TelaDevolucao.fxml"); }
    @FXML private void abrirNovoCliente() { carregarTela("/fxml/funcionario/TelaNovoCliente.fxml"); }
    @FXML private void abrirDisponiveis() { carregarTela("/fxml/funcionario/TelaDisponiveis.fxml"); }
    @FXML private void abrirContratosAtivos() { carregarTela("/fxml/funcionario/TelaContratosAtivos.fxml"); }
    @FXML private void abrirHistorico() { carregarTela("/fxml/funcionario/TelaHistorico.fxml"); }
    @FXML private void abrirMultas() { carregarTela("/fxml/funcionario/TelaMultas.fxml"); }

    private void carregarTela(String fxmlPath) {
        try {
            Parent tela = FXMLLoader.load(getClass().getResource(fxmlPath));
            conteudoArea.getChildren().setAll(tela);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}