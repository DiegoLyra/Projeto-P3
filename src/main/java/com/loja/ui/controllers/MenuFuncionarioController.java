package com.loja.ui.controllers;

import com.loja.model.Funcionario;
import com.loja.padrao.facade.interfaces.ILojaFacade;
import com.loja.ui.navigation.Navigator;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;

public class MenuFuncionarioController {

    private final ILojaFacade facade;
    private final Navigator navigator;

    @FXML
    private Label lblNomeFuncionario;

    @FXML
    private StackPane conteudoArea;

    public MenuFuncionarioController(ILojaFacade facade, Navigator navigator) {
        this.facade = facade;
        this.navigator = navigator;
    }

    @FXML
    public void initialize() {
        if (!(navigator.getUsuarioLogado() instanceof Funcionario funcionario)) {
            throw new IllegalStateException(
                    "A tela do funcionário exige um funcionário logado."
            );
        }

        if (funcionario.getNome() != null) {
            lblNomeFuncionario.setText(
                    "PAINEL DO FUNCIONÁRIO: "
                    + funcionario.getNome().toUpperCase()
            );
        }

        abrirAluguel();
    }

    @FXML
    private void abrirAluguel() {
        carregarTela("/fxml/funcionario/TelaAluguel.fxml");
    }

    @FXML
    private void abrirDevolucao() {
        carregarTela("/fxml/funcionario/TelaDevolucao.fxml");
    }

    @FXML
    private void abrirNovoCliente() {
        carregarTela("/fxml/funcionario/TelaNovoCliente.fxml");
    }

    @FXML
    private void abrirDisponiveis() {
        carregarTela("/fxml/funcionario/TelaDisponiveis.fxml");
    }

    @FXML
    private void abrirContratosAtivos() {
        carregarTela("/fxml/funcionario/TelaContratosAtivos.fxml");
    }

    @FXML
    private void abrirHistorico() {
        carregarTela("/fxml/funcionario/TelaHistorico.fxml");
    }

    @FXML
    private void abrirMultas() {
        carregarTela("/fxml/funcionario/TelaMultas.fxml");
    }

    @FXML
    public void handleSair() {
        navigator.showLogin();
    }

    private void carregarTela(String caminhoFXML) {
        try {
            URL url = getClass().getResource(caminhoFXML);

            if (url == null) {
                throw new IllegalStateException(
                        "FXML não encontrado: " + caminhoFXML
                );
            }

            FXMLLoader loader = new FXMLLoader(url);

            loader.setControllerFactory(tipo -> {
                try {
                    return tipo
                            .getConstructor(ILojaFacade.class, Navigator.class)
                            .newInstance(facade, navigator);
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException(
                            "O controller " + tipo.getSimpleName()
                            + " precisa ter um construtor público "
                            + "(ILojaFacade, Navigator).",
                            e
                    );
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException(
                            "Erro ao criar o controller: "
                            + tipo.getSimpleName(),
                            e
                    );
                }
            });

            Parent tela = loader.load();
            conteudoArea.getChildren().setAll(tela);

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }
    }
}