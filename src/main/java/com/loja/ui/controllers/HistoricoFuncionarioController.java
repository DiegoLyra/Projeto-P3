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
import java.util.Map;

public class HistoricoFuncionarioController {

    private static final Logger logger = LoggerFactory.getLogger(HistoricoFuncionarioController.class);

    @FXML private TextField txtClienteId;
    @FXML private TableView<ContratoAluguel> tabelaHistorico;
    @FXML private TableColumn<ContratoAluguel, String> colContratoId;
    @FXML private TableColumn<ContratoAluguel, String> colItem;
    @FXML private TableColumn<ContratoAluguel, String> colDataRetirada;
    @FXML private TableColumn<ContratoAluguel, String> colDataDevolucao;
    @FXML private TableColumn<ContratoAluguel, Object> colValorPago;
    @FXML private Label mensagemLabel;

    private ILojaFacade facade;
    private final ObservableList<ContratoAluguel> listaHistorico = FXCollections.observableArrayList();

    public void setFacade(ILojaFacade facade) {
        this.facade = facade;
    }

    @FXML
    private void initialize() {
        colContratoId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colItem.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        colDataRetirada.setCellValueFactory(new PropertyValueFactory<>("dataRetirada"));
        colDataDevolucao.setCellValueFactory(new PropertyValueFactory<>("dataPrevDevolucao"));
        colValorPago.setCellValueFactory(new PropertyValueFactory<>("valorTotal"));

        tabelaHistorico.setItems(listaHistorico);
    }

    @FXML
    private void handleBuscarHistorico() {
        String clienteId = txtClienteId.getText().trim();
        if (clienteId.isEmpty()) {
            exibirMensagem("Informe o ID do cliente para realizar a consulta.", false);
            return;
        }

        if (facade == null) {
            exibirMensagem("Erro interno: Serviço de histórico não configurado.", false);
            return;
        }

        try {
            // Chama o método da fachada que você definiu: consultarHistoricoCliente
            Map<String, ContratoAluguel> mapaHistorico = facade.consultarHistoricoCliente(clienteId);
            List<ContratoAluguel> historico = new ArrayList<>(mapaHistorico.values());
            
            listaHistorico.setAll(historico);

            if (historico.isEmpty()) {
                exibirMensagem("Nenhum histórico encontrado para este cliente.", false);
            } else {
                exibirMensagem("Histórico carregado com sucesso.", true);
            }
        } catch (Exception e) {
            logger.error("Erro ao buscar histórico do cliente '{}'", clienteId, e);
            exibirMensagem("Erro ao buscar histórico: " + e.getMessage(), false);
        }
    }

    private void exibirMensagem(String msg, boolean sucesso) {
        mensagemLabel.setText(msg);
        mensagemLabel.setStyle(sucesso 
                ? "-fx-font-size: 12px; -fx-text-fill: #16a34a;" 
                : "-fx-font-size: 12px; -fx-text-fill: #dc2626;");
    }
}