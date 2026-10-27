package com.loja.ui.controllers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import com.loja.padrao.facade.interfaces.ILojaFacade;
import com.loja.ui.navigation.Navigator;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;

public class MenuFuncionarioController {

    @FXML
    private Label lblNomeFuncionario;

    @FXML
    private StackPane conteudoArea;

    @FXML private Button btnAluguel;
    @FXML private Button btnDevolucao;
    @FXML private Button btnNovoCliente;
    @FXML private Button btnDisponiveis;
    @FXML private Button btnContratosAtivos;
    @FXML private Button btnHistorico;
    @FXML private Button btnMultas;
    @FXML private Button btnRelatorios;

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
            marcarBotaoAtivo(btnAluguel);
        }
    }

    @FXML
    private void abrirDevolucao() {
        if (carregarTela(
                "/fxml/funcionario/DevolucaoFuncionario.fxml")) {
            marcarBotaoAtivo(btnDevolucao);
        }
    }

    @FXML
    private void abrirNovoCliente() {
        if (carregarTela(
                "/fxml/funcionario/NovoClienteFuncionario.fxml")) {
            marcarBotaoAtivo(btnNovoCliente);
        }
    }

    @FXML
    private void abrirDisponiveis() {
        if (carregarTela(
                "/fxml/funcionario/ItensDisponiveisFuncionario.fxml")) {
            marcarBotaoAtivo(btnDisponiveis);
        }
    }

    @FXML
    private void abrirContratosAtivos() {
        if (carregarTela(
                "/fxml/funcionario/TelaContratosAtivos.fxml")) {
            marcarBotaoAtivo(btnContratosAtivos);
        }
    }

    @FXML
    private void abrirHistorico() {
        if (carregarTela(
                "/fxml/funcionario/TelaHistorico.fxml")) {
            marcarBotaoAtivo(btnHistorico);
        }
    }

    @FXML
    private void abrirMultas() {
        if (carregarTela(
                "/fxml/funcionario/MultaFuncionario.fxml")) {
            marcarBotaoAtivo(btnMultas);
        }
    }

    @FXML
    private void abrirRelatorios() {
        if (carregarTela(
                "/fxml/funcionario/RelatorioFuncionario.fxml")){
            marcarBotaoAtivo(btnRelatorios);
        }
    }

    private void marcarBotaoAtivo(Button selecionado) {
        List<Button> botoes = List.of(
                btnAluguel,
                btnDevolucao,
                btnNovoCliente,
                btnDisponiveis,
                btnContratosAtivos,
                btnHistorico,
                btnMultas,
                btnRelatorios
        );

        for (Button botao : botoes) {
            if (botao == null) {
                continue;
            }

            if (botao == selecionado) {
                botao.setStyle(
                        "-fx-background-color: #2563eb;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 9 16;" +
                        "-fx-cursor: hand;"
                );
            } else {
                botao.setStyle(
                        "-fx-background-color: #f1f5f9;" +
                        "-fx-text-fill: #1e293b;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 9 16;" +
                        "-fx-cursor: hand;"
                );
            }
        }
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

            loader.setControllerFactory(this::criarControlador);

            Parent tela = loader.load();
            Object controlador = loader.getController();

            configurarFacade(controlador);

            ScrollPane scrollPane = new ScrollPane(tela);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(false);
            scrollPane.setMinHeight(0);
            scrollPane.setMaxHeight(Double.MAX_VALUE);

            scrollPane.setHbarPolicy(
                    ScrollPane.ScrollBarPolicy.NEVER);

            scrollPane.setVbarPolicy(
                    ScrollPane.ScrollBarPolicy.AS_NEEDED);

            scrollPane.setStyle(
                    "-fx-background: #f8fafc;" +
                    "-fx-background-color: #f8fafc;"
            );

            conteudoArea.getChildren().setAll(scrollPane);

            return true;

        } catch (Exception erro) {

            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Erro de navegação");
            alerta.setHeaderText(
                    "Não foi possível abrir esta tela.");

            alerta.setContentText(
                    "Arquivo: " + caminhoFXML
                    + "\n\nMotivo: " + obterMensagemErro(erro)
            );

            alerta.showAndWait();

            return false;
        }
    }

    private void configurarFacade(Object controlador) {
        if (controlador == null) {
            return;
        }

        try {
            Method metodo = controlador.getClass().getMethod(
                    "setFacade",
                    ILojaFacade.class
            );

            metodo.invoke(controlador, facade);

        } catch (NoSuchMethodException erro) {
            // Esse controlador não precisa receber a facade por meio de setFacade().
        } catch (IllegalAccessException
                | InvocationTargetException erro) {

            throw new IllegalStateException(
                    "Não foi possível configurar a facade no controlador "
                    + controlador.getClass().getName(),
                    erro
            );
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
                Constructor<?> construtorPadrão = tipo.getConstructor();
                return construtorPadrão.newInstance();

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

    @FXML
    private void sair() {
        Alert confirmacao =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirmacao.setTitle("Sair do sistema");
        confirmacao.setHeaderText(
                "Deseja voltar ao login?");
        confirmacao.setContentText(
                "Sua sessão será encerrada.");

        Optional<ButtonType> resposta =
                confirmacao.showAndWait();

        if (resposta.isPresent()
                && resposta.get() == ButtonType.OK) {
            navigator.showLogin();
        }
    }
}