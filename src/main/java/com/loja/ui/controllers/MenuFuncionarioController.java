package com.loja.ui.controllers;

import com.loja.padrao.facade.interfaces.ILojaFacade;
import com.loja.ui.navigation.Navigator;

import java.lang.reflect.Constructor;
import java.net.URL;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class MenuFuncionarioController {

    @FXML
    private Label lblNomeFuncionario;

    @FXML
    private StackPane conteudoArea;

    private final ILojaFacade facade;
    private final Navigator navigator;

    public MenuFuncionarioController(
            ILojaFacade facade,
            Navigator navigator) {
        this.facade = facade;
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        abrirAluguel();
    }

    public void setUsuarioLogado(String nomeFuncionario) {
        if (lblNomeFuncionario != null
                && nomeFuncionario != null
                && !nomeFuncionario.isBlank()) {

            lblNomeFuncionario.setText(
                    "PAINEL DO FUNCIONÁRIO: "
                    + nomeFuncionario.toUpperCase()
            );
        }
    }

    @FXML
    private void abrirAluguel() {
        carregarTela("/fxml/funcionario/Aluguel.fxml");
    }

    @FXML
    private void abrirDevolucao() {
        carregarTela("/fxml/funcionario/DevolucaoFuncionario.fxml");
    }

    @FXML
    private void abrirNovoCliente() {
        carregarTela("/fxml/funcionario/NovoClienteFuncionario.fxml");
    }

    @FXML
    private void abrirDisponiveis() {
        carregarTela("/fxml/funcionario/ItensDisponiveisFuncionario.fxml");
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
    private void abrirRelatorios() {
        carregarTela("/fxml/funcionario/RelatorioFuncionario.fxml");
    }

    private void carregarTela(String caminhoFXML) {
        try {
            URL recurso = getClass().getResource(caminhoFXML);

            if (recurso == null) {
                throw new IllegalStateException(
                        "Arquivo FXML não encontrado: " + caminhoFXML
                );
            }

            FXMLLoader loader = new FXMLLoader(recurso);

            loader.setControllerFactory(tipo ->
                    criarControlador(tipo)
            );

            Parent tela = loader.load();

            Object controlador = loader.getController();

            // Passa a fachada para a tela de relatórios.
            if (controlador instanceof RelatorioFuncionarioController) {
                ((RelatorioFuncionarioController) controlador)
                        .setFacade(facade);
            }

            // Passa a fachada para a tela de itens disponíveis.
            if (controlador instanceof ItensDisponiveisFuncionarioController) {
                ((ItensDisponiveisFuncionarioController) controlador)
                        .setFacade(facade);
            }


            // Passa a fachada para a tela de cadastro de clientes.
            if (controlador instanceof NovoClienteFuncionarioController) {
                ((NovoClienteFuncionarioController) controlador)
                        .setFacade(facade);
            }

            // Exibe a tela no painel central.
            conteudoArea.getChildren().setAll(tela);

        } catch (Exception erro) {
            erro.printStackTrace();

            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Erro de navegação");
            alerta.setHeaderText("Não foi possível abrir esta tela.");
            alerta.setContentText(
                    "Arquivo: " + caminhoFXML
                    + "\n\nMotivo: " + obterMensagemErro(erro)
            );
            alerta.showAndWait();
        }
    }

    private Object criarControlador(Class<?> tipo) {
        try {
            Constructor<?> construtor = tipo.getConstructor(
                    ILojaFacade.class,
                    Navigator.class
            );

            return construtor.newInstance(facade, navigator);

        } catch (NoSuchMethodException erro) {
            try {
                Constructor<?> construtor =
                        tipo.getDeclaredConstructor();

                if (!construtor.canAccess(null)) {
                    construtor.setAccessible(true);
                }

                return construtor.newInstance();

            } catch (ReflectiveOperationException erroInterno) {
                throw new IllegalStateException(
                        "O controlador " + tipo.getName()
                        + " não possui um construtor compatível.",
                        erroInterno
                );
            }

        } catch (ReflectiveOperationException erro) {
            throw new IllegalStateException(
                    "Não foi possível criar o controlador "
                    + tipo.getName(),
                    erro
            );
        }
    }

    private String obterMensagemErro(Throwable erro) {
        Throwable causa = erro;

        while (causa.getCause() != null) {
            causa = causa.getCause();
        }

        String mensagem = causa.getMessage();

        return mensagem != null
                ? mensagem
                : causa.getClass().getSimpleName();
    }
}