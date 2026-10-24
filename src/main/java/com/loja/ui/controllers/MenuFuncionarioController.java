package com.loja.ui.controllers;

import com.loja.padrao.facade.interfaces.ILojaFacade;
import com.loja.ui.navigation.Navigator;

import java.lang.reflect.Constructor;
import java.net.URL;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class MenuFuncionarioController {

    @FXML
    private Label lblNomeFuncionario;

    @FXML
    private StackPane conteudoArea;

    @FXML
    private Button btnAluguel;

    @FXML
    private Button btnDevolucao;

    @FXML
    private Button btnNovoCliente;

    @FXML
    private Button btnDisponiveis;

    @FXML
    private Button btnContratosAtivos;

    @FXML
    private Button btnHistorico;

    @FXML
    private Button btnMultas;

    @FXML
    private Button btnRelatorios;

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
        if (carregarTela("/fxml/funcionario/Aluguel.fxml")) {
            atualizarBotaoSelecionado(btnAluguel);
        }
    }

    @FXML
    private void abrirDevolucao() {
        if (carregarTela(
                "/fxml/funcionario/DevolucaoFuncionario.fxml")) {
            atualizarBotaoSelecionado(btnDevolucao);
        }
    }

    @FXML
    private void abrirNovoCliente() {
        if (carregarTela(
                "/fxml/funcionario/NovoClienteFuncionario.fxml")) {
            atualizarBotaoSelecionado(btnNovoCliente);
        }
    }

    @FXML
    private void abrirDisponiveis() {
        if (carregarTela(
                "/fxml/funcionario/ItensDisponiveisFuncionario.fxml")) {
            atualizarBotaoSelecionado(btnDisponiveis);
        }
    }

    @FXML
    private void abrirContratosAtivos() {
        if (carregarTela(
                "/fxml/funcionario/TelaContratosAtivos.fxml")) {
            atualizarBotaoSelecionado(btnContratosAtivos);
        }
    }

    @FXML
    private void abrirHistorico() {
        if (carregarTela(
                "/fxml/funcionario/TelaHistorico.fxml")) {
            atualizarBotaoSelecionado(btnHistorico);
        }
    }

    @FXML
    private void abrirMultas() {
        if (carregarTela(
                "/fxml/funcionario/TelaMultas.fxml")) {
            atualizarBotaoSelecionado(btnMultas);
        }
    }

    @FXML
    private void abrirRelatorios() {
        if (carregarTela(
                "/fxml/funcionario/RelatorioFuncionario.fxml")) {
            atualizarBotaoSelecionado(btnRelatorios);
        }
    }

    private void atualizarBotaoSelecionado(Button selecionado) {

        Button[] botoes = {
            btnAluguel,
            btnDevolucao,
            btnNovoCliente,
            btnDisponiveis,
            btnContratosAtivos,
            btnHistorico,
            btnMultas,
            btnRelatorios
        };

        String estiloNormal =
                "-fx-background-color: #f1f5f9;"
                + "-fx-text-fill: #1e293b;"
                + "-fx-background-radius: 8;"
                + "-fx-padding: 9 16;"
                + "-fx-cursor: hand;";

        String estiloSelecionado =
                "-fx-background-color: #2563eb;"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 8;"
                + "-fx-padding: 9 16;"
                + "-fx-cursor: hand;";

        for (Button botao : botoes) {
            botao.setStyle(estiloNormal);
        }

        selecionado.setStyle(estiloSelecionado);
    }

    private boolean carregarTela(String caminhoFXML) {
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

            // Passa a fachada para o cadastro de clientes.
            if (controlador instanceof NovoClienteFuncionarioController) {
                ((NovoClienteFuncionarioController) controlador)
                        .setFacade(facade);
            }

            // Exibe a tela no painel central.
            conteudoArea.getChildren().setAll(tela);

            return true;

        } catch (Exception erro) {
            erro.printStackTrace();

            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Erro de navegação");
            alerta.setHeaderText(
                    "Não foi possível abrir esta tela."
            );
            alerta.setContentText(
                    "Arquivo: " + caminhoFXML
                    + "\n\nMotivo: " + obterMensagemErro(erro)
            );
            alerta.showAndWait();

            return false;
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