package com.loja.ui.controllers;

import com.loja.model.Cliente;
import com.loja.model.Usuario;
import com.loja.padrao.facade.interfaces.ILojaFacade;
import com.loja.ui.components.Banner;
import com.loja.ui.components.PageHeader;
import com.loja.ui.components.PillNav;
import com.loja.ui.navigation.Navigator;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class AuthController {

    private static final double LARGURA_MINIMA_PAINEL_MARCA = 760;

    private static final double DESLOCAMENTO = 28;
    private static final Duration DURACAO_SAIDA = Duration.millis(120);
    private static final Duration DURACAO_ENTRADA = Duration.millis(180);

    private final ILojaFacade facade;
    private final Navigator navigator;

    private boolean modoRegistro = false;
    private ParallelTransition animacao;

    @FXML private ScrollPane scroll;
    @FXML private VBox brandPanel;
    @FXML private PageHeader header;
    @FXML private PillNav nav;
    @FXML private VBox bannerSlot;
    @FXML private StackPane formsStack;
    @FXML private VBox loginForm;
    @FXML private VBox registerForm;

    @FXML private TextField txtLoginEmail;
    @FXML private PasswordField txtLoginSenha;

    @FXML private TextField txtRegNome;
    @FXML private TextField txtRegEmail;
    @FXML private PasswordField txtRegSenha;
    @FXML private PasswordField txtRegConfirmar;

    public AuthController(ILojaFacade facade, Navigator navigator) {
        this.facade = facade;
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        nav.addTab("Entrar", true, () -> mostrarFormulario(false));
        nav.addTab("Criar conta", false, () -> mostrarFormulario(true));

        formsStack.setMinHeight(Region.USE_PREF_SIZE);
        formsStack.setMaxHeight(Region.USE_PREF_SIZE);
        aplicarEstadoFinal(false);
        txtLoginEmail.setOnAction(e -> txtLoginSenha.requestFocus());
        txtRegNome.setOnAction(e -> txtRegEmail.requestFocus());
        txtRegEmail.setOnAction(e -> txtRegSenha.requestFocus());
        txtRegSenha.setOnAction(e -> txtRegConfirmar.requestFocus());

        scroll.widthProperty().addListener((obs, antigo, novo) -> ajustarLayout(novo.doubleValue()));
        ajustarLayout(scroll.getWidth());
    }

    private void ajustarLayout(double largura) {
        if (largura <= 0) {
            return;
        }
        boolean mostrar = largura >= LARGURA_MINIMA_PAINEL_MARCA;
        brandPanel.setVisible(mostrar);
        brandPanel.setManaged(mostrar);
    }

    private void mostrarFormulario(boolean registro) {
        if (registro == modoRegistro) {
            return;
        }

        if (animacao != null) {
            animacao.stop();
            aplicarEstadoFinal(modoRegistro);
        }

        VBox sai = modoRegistro ? registerForm : loginForm;
        VBox entra = registro ? registerForm : loginForm;
        double direcao = registro ? 1 : -1;
        modoRegistro = registro;

        limparBanner();
        header.setSubtitle(registro ? "Crie sua conta de cliente" : "Acesse sua conta");

        double alturaDe = formsStack.getHeight();
        formsStack.setPrefHeight(alturaDe);

        entra.setOpacity(0);
        entra.setTranslateX(direcao * DESLOCAMENTO);
        entra.setVisible(true);
        entra.setManaged(true);
        entra.applyCss();
        double alturaPara = entra.prefHeight(formsStack.getWidth());

        Timeline altura = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(formsStack.prefHeightProperty(), alturaDe)),
                new KeyFrame(DURACAO_SAIDA.add(DURACAO_ENTRADA),
                        new KeyValue(formsStack.prefHeightProperty(), alturaPara, Interpolator.EASE_BOTH)));

        FadeTransition fadeSai = new FadeTransition(DURACAO_SAIDA, sai);
        fadeSai.setToValue(0);
        TranslateTransition slideSai = new TranslateTransition(DURACAO_SAIDA, sai);
        slideSai.setToX(-direcao * DESLOCAMENTO);
        slideSai.setInterpolator(Interpolator.EASE_IN);
        ParallelTransition saida = new ParallelTransition(fadeSai, slideSai);
        saida.setOnFinished(e -> {
            sai.setVisible(false);
            sai.setManaged(false);
        });

        FadeTransition fadeEntra = new FadeTransition(DURACAO_ENTRADA, entra);
        fadeEntra.setFromValue(0);
        fadeEntra.setToValue(1);
        TranslateTransition slideEntra = new TranslateTransition(DURACAO_ENTRADA, entra);
        slideEntra.setFromX(direcao * DESLOCAMENTO);
        slideEntra.setToX(0);
        slideEntra.setInterpolator(Interpolator.EASE_OUT);
        ParallelTransition entrada = new ParallelTransition(fadeEntra, slideEntra);

        animacao = new ParallelTransition(altura, new SequentialTransition(saida, entrada));
        animacao.setOnFinished(e -> {
            aplicarEstadoFinal(registro);
            animacao = null;
            (registro ? txtRegNome : txtLoginEmail).requestFocus();
        });
        animacao.play();
    }

    private void aplicarEstadoFinal(boolean registro) {
        for (VBox form : new VBox[]{loginForm, registerForm}) {
            boolean ativo = form == (registro ? registerForm : loginForm);
            form.setVisible(ativo);
            form.setManaged(ativo);
            form.setOpacity(1);
            form.setTranslateX(0);
        }
        formsStack.setPrefHeight(Region.USE_COMPUTED_SIZE);
    }

    @FXML
    private void entrar() {
        String email = textoOuVazio(txtLoginEmail.getText()).trim();
        String senha = textoOuVazio(txtLoginSenha.getText());

        if (email.isEmpty() || senha.isEmpty()) {
            mostrarBanner("Informe e-mail e senha.", Banner.BannerType.WARNING);
            return;
        }

        try {
            Usuario usuario = facade.autenticarUsuario(email, senha);
            navigator.showHome(usuario);
        } catch (RuntimeException e) {
            txtLoginSenha.clear();
            mostrarBanner(e.getMessage(), Banner.BannerType.DANGER);
        }
    }

    @FXML
    private void cadastrar() {
        String nome = textoOuVazio(txtRegNome.getText()).trim();
        String email = textoOuVazio(txtRegEmail.getText()).trim();
        String senha = textoOuVazio(txtRegSenha.getText());
        String confirmar = textoOuVazio(txtRegConfirmar.getText());

        String erro = validar(nome, email, senha, confirmar);
        if (erro != null) {
            mostrarBanner(erro, Banner.BannerType.WARNING);
            return;
        }

        try {
            Cliente cliente = new Cliente(gerarProximoId(), nome, email, senha);
            cliente.setAtivo(true);
            facade.cadastrarCliente(cliente);
            txtRegNome.clear();
            txtRegEmail.clear();
            txtRegSenha.clear();
            txtRegConfirmar.clear();
            txtLoginEmail.setText(email);
            txtLoginSenha.clear();

            nav.selectTab(0);
            mostrarBanner("Conta criada com sucesso! Faça login para continuar.", Banner.BannerType.SUCCESS);
        } catch (RuntimeException e) {
            mostrarBanner(e.getMessage(), Banner.BannerType.DANGER);
        }
    }

    private String validar(String nome, String email, String senha, String confirmar) {
        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            return "Preencha todos os campos.";
        }
        if (!senha.equals(confirmar)) {
            return "As senhas não conferem.";
        }
        return null;
    }

    private String gerarProximoId() {
        int maior = facade.listarUsuario().keySet().stream()
                .filter(id -> id != null && id.matches("(?i)USR\\d+"))
                .mapToInt(id -> Integer.parseInt(id.substring(3)))
                .max()
                .orElse(0);
        return String.format("USR%03d", maior + 1);
    }

    private static String textoOuVazio(String valor) {
        return valor == null ? "" : valor;
    }

    private void mostrarBanner(String mensagem, Banner.BannerType tipo) {
        bannerSlot.getChildren().setAll(new Banner(mensagem, tipo));
    }

    private void limparBanner() {
        bannerSlot.getChildren().clear();
    }
}