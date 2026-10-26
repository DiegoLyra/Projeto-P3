package com.loja.ui.controllers;

import com.loja.model.ContratoAluguel;
import com.loja.padrao.facade.interfaces.ILojaFacade;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ContratosAtivosController {

    private static final Logger logger = LoggerFactory.getLogger(ContratosAtivosController.class);

    @FXML private TextField txtFiltro;
    @FXML private TableView<ContratoAluguel> tabelaContratos;
    @FXML private TableColumn<ContratoAluguel, String> colIdContrato;
    @FXML private TableColumn<ContratoAluguel, String> colCliente;
    @FXML private TableColumn<ContratoAluguel, String> colItem;
    @FXML private TableColumn<ContratoAluguel, String> colDataInicio;
    @FXML private TableColumn<ContratoAluguel, String> colDataDevolucao;
    @FXML private TableColumn<ContratoAluguel, Double> colValor;
    @FXML private Label mensagemLabel;

    private ILojaFacade facade;
    private final ObservableList<ContratoAluguel> listaContratos = FXCollections.observableArrayList();

    public void setFacade(ILojaFacade facade) {
        this.facade = facade;
        carregarContratos();
    }

    @FXML
    private void initialize() {
        colIdContrato.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nomeCliente"));
        colItem.setCellValueFactory(new PropertyValueFactory<>("nomeItem"));
        colDataInicio.setCellValueFactory(new PropertyValueFactory<>("dataInicio"));
        colDataDevolucao.setCellValueFactory(new PropertyValueFactory<>("dataDevolucaoPrevista"));
        colValor.setCellValueFactory(new PropertyValueFactory<>("valorTotal"));

        tabelaContratos.setItems(listaContratos);
    }

    @FXML
    private void handleFiltrar() {
        String filtro = txtFiltro.getText().trim().toLowerCase();
        if (filtro.isEmpty()) {
            carregarContratos();
            return;
        }

        if (facade != null) {
            List<ContratoAluguel> filtrados = facade.listarContratosAtivos().values().stream()
                    .filter(c -> (c.getId() != null && c.getId().toLowerCase().contains(filtro))
                            || (c.getId() != null && c.getId().toLowerCase().contains(filtro)))
                    .toList();
            listaContratos.setAll(filtrados);
        }
    }

    @FXML
    private void handleAtualizar() {
        txtFiltro.clear();
        carregarContratos();
    }

    private void carregarContratos() {
        if (facade == null) return;
        try {
            List<ContratoAluguel> ativos = new ArrayList<>(facade.listarContratosAtivos().values());
            listaContratos.setAll(ativos);
            mensagemLabel.setText("");
        } catch (Exception e) {
            logger.error("Erro ao carregar contratos ativos", e);
            exibirMensagem("Erro ao carregar contratos: " + e.getMessage(), false);
        }
    }

    private void exibirMensagem(String msg, boolean sucesso) {
        mensagemLabel.setText(msg);
        mensagemLabel.setStyle(sucesso 
                ? "-fx-font-size: 12px; -fx-text-fill: #16a34a;" 
                : "-fx-font-size: 12px; -fx-text-fill: #dc2626;");
    }
}