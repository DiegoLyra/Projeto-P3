package com.loja.ui.navigation;

import com.loja.model.Administrador;
import com.loja.model.Cliente;
import com.loja.model.Funcionario;
import com.loja.model.Usuario;
import com.loja.padrao.facade.interfaces.ILojaFacade;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;

public class Navigator {

    private static final String FXML_AUTH = "/fxml/autenticacao/auth.fxml";
    private static final String FXML_ADMIN = "/fxml/admin/admin.fxml";
    private static final String FXML_FUNCIONARIO = "/fxml/funcionario/funcionario.fxml";
    private static final String FXML_CLIENTE = "/fxml/cliente/cliente.fxml";

    private static final double LARGURA_INICIAL = 1200;
    private static final double ALTURA_INICIAL = 720;

    private final Stage stage;
    private final ILojaFacade facade;
    private Usuario usuarioLogado;

    public Navigator(Stage stage, ILojaFacade facade) {
        this.stage = stage;
        this.facade = facade;
    }

    public Usuario getUsuarioLogado() {
        return usuarioLogado;
    }

    public void showLogin() {
        usuarioLogado = null;
        show(FXML_AUTH);
    }

    public void showHome(Usuario usuario) {
        String fxml;
        if (usuario instanceof Administrador) {
            fxml = FXML_ADMIN;
        } else if (usuario instanceof Funcionario) {
            fxml = FXML_FUNCIONARIO;
        } else if (usuario instanceof Cliente) {
            fxml = FXML_CLIENTE;
        } else {
            throw new IllegalStateException("Perfil de usuário desconhecido.");
        }

        if (Navigator.class.getResource(fxml) == null) {
            throw new IllegalStateException("A tela do perfil " + usuario.getPerfil() + " ainda não foi implementada.");
        }

        usuarioLogado = usuario;
        try {
            show(fxml);
        } catch (RuntimeException e) {
            usuarioLogado = null;
            throw e;
        }
    }

    private void show(String caminho) {
        URL url = Navigator.class.getResource(caminho);
        if (url == null) {
            throw new IllegalStateException("FXML não encontrado: " + caminho);
        }
        FXMLLoader loader = new FXMLLoader(url);
        loader.setControllerFactory(this::criarController);
        try {
            Parent root = loader.load();
            if (stage.getScene() == null) {
                stage.setScene(new Scene(root, LARGURA_INICIAL, ALTURA_INICIAL));
                stage.centerOnScreen();
            } else {
                stage.getScene().setRoot(root);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao carregar " + caminho, e);
        }
    }

    private Object criarController(Class<?> tipo) {
        try {
            return tipo.getConstructor(ILojaFacade.class, Navigator.class).newInstance(facade, this);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Controller " + tipo.getSimpleName() + " precisa de um construtor público (ILojaFacade, Navigator).", e);
        }
    }
}
