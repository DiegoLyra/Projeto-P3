package com.loja.ui.controllers;

import com.loja.model.Item;
import com.loja.padrao.facade.interfaces.ILojaFacade;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ItensDisponiveisFuncionarioController {

    private static final Logger logger =
            LoggerFactory.getLogger(ItensDisponiveisFuncionarioController.class);

    @FXML
    private TableView<Item> tabelaItens;

    @FXML
    private TableColumn<Item, String> colunaId;

    @FXML
    private TableColumn<Item, String> colunaNome;

    @FXML
    private TableColumn<Item, String> colunaValorDiario;

    @FXML
    private TableColumn<Item, String> colunaStatus;

    @FXML
    private Label mensagemLabel;

    private ILojaFacade facade;

    private final ObservableList<Item> itens =
            FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colunaId.setCellValueFactory(
                new PropertyValueFactory<>("id"));

        colunaNome.setCellValueFactory(
                new PropertyValueFactory<>("nome"));

        colunaValorDiario.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        String.valueOf(cell.getValue().getTaxaDiaria())
                ));

        colunaStatus.setCellValueFactory(cell ->
                new SimpleStringProperty("DISPONIVEL"));

        tabelaItens.setItems(itens);
    }

    public void setFacade(ILojaFacade facade) {
        this.facade = facade;
        carregarItens();
    }

    @FXML
    private void handleAtualizar() {
        carregarItens();
    }

    private void carregarItens() {
        if (facade == null) {
            mensagemLabel.setText(
                    "Serviço de consulta não configurado."
            );
            return;
        }

        try {
            Map<String, Item> itensDisponiveis =
                    facade.listarItensDisponiveis();

            itens.setAll(itensDisponiveis.values());

            if (itens.isEmpty()) {
                mensagemLabel.setText(
                        "Não há itens disponíveis para aluguel."
                );
            } else {
                mensagemLabel.setText(
                        "Total de itens disponíveis: " + itens.size()
                );
            }

        } catch (RuntimeException e) {
            logger.error(
                    "Erro ao carregar itens disponíveis: {}",
                    e.getMessage(),
                    e
            );

            itens.clear();
            mensagemLabel.setText(
                    "Erro ao carregar os itens disponíveis."
            );
        }
    }
}
