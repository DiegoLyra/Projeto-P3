package com.loja.ui.controllers;

import com.loja.model.Cliente;
import com.loja.padrao.facade.interfaces.ILojaFacade;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MenuClienteController {

    private static final Logger logger = LoggerFactory.getLogger(MenuClienteController.class);

    private ILojaFacade facade;
    private Cliente usuarioLogado;

    @FXML private Label lblBoasVindas;

    public void initData(ILojaFacade facade, Cliente usuarioLogado) {
        this.facade = facade;
        this.usuarioLogado = usuarioLogado;

        if (usuarioLogado != null) {
            this.lblBoasVindas.setText("ÁREA DO CLIENTE: " + usuarioLogado.getNome().toUpperCase());
        }
    }

    @FXML
    public void handleSair() {
        Stage stage = (Stage) lblBoasVindas.getScene().getWindow();
        stage.close();
    }
}
