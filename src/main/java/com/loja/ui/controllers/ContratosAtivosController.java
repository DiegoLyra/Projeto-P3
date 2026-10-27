package com.loja.ui.controllers;

import com.loja.model.ContratoAluguel;
import com.loja.padrao.facade.interfaces.ILojaFacade;
import com.loja.ui.components.ColunaTexto;
import com.loja.ui.relatorios.GeradorRelatorios;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

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
    @FXML private TableColumn<ContratoAluguel, String> colValor;    
    @FXML private Label mensagemLabel;

    private ILojaFacade facade;
    private final ObservableList<ContratoAluguel> listaContratos = FXCollections.observableArrayList();

    public void setFacade(ILojaFacade facade) {
        this.facade = facade;
        carregarContratos();
    }

        @FXML
    private void initialize() {
        colIdContrato.setCellValueFactory(new ColunaTexto<ContratoAluguel>() {
            @Override
            protected String valor(ContratoAluguel c) {
                return c.getId();
            }
        });
        colCliente.setCellValueFactory(new ColunaTexto<ContratoAluguel>() {
            @Override
            protected String valor(ContratoAluguel c) {
                return c.getCliente().getNome();
            }
        });
        colItem.setCellValueFactory(new ColunaTexto<ContratoAluguel>() {
            @Override
            protected String valor(ContratoAluguel c) {
                return c.getItem().getNome();
            }
        });
        colDataInicio.setCellValueFactory(new ColunaTexto<ContratoAluguel>() {
            @Override
            protected String valor(ContratoAluguel c) {
                return GeradorRelatorios.formatarData(c.getDataRetirada());
            }
        });
        colDataDevolucao.setCellValueFactory(new ColunaTexto<ContratoAluguel>() {
            @Override
            protected String valor(ContratoAluguel c) {
                return GeradorRelatorios.formatarData(c.getDataPrevDevolucao());
            }
        });
        colValor.setCellValueFactory(new ColunaTexto<ContratoAluguel>() {
            @Override
            protected String valor(ContratoAluguel c) {
                return GeradorRelatorios.formatarMoeda(c.getValorTotal());
            }
        });

        tabelaContratos.setItems(listaContratos);
    }

        @FXML
    private void handleFiltrar() {
        String filtro = txtFiltro.getText().trim().toLowerCase();
        if (filtro.isEmpty() || facade == null) {
            carregarContratos();
            return;
        }

        List<ContratoAluguel> filtrados = new ArrayList<>();
        for (ContratoAluguel c : facade.listarContratosAtivos().values()) {
            String clienteId = c.getCliente() != null ? c.getCliente().getId() : "";
            String clienteNome = c.getCliente() != null ? c.getCliente().getNome() : "";
            String texto = (c.getId() + " " + clienteId + " " + clienteNome).toLowerCase();
            if (texto.contains(filtro)) {
                filtrados.add(c);
            }
        }

        listaContratos.setAll(filtrados);
        exibirMensagem(filtrados.isEmpty()
                ? "Nenhum contrato encontrado."
                : filtrados.size() + " contrato(s) encontrado(s).", !filtrados.isEmpty());
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
            logger.error("Erro ao carregar contratos ativos do CSV", e);
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