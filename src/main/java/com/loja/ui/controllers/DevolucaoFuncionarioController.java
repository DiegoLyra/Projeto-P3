
package com.loja.ui.controllers;

import com.loja.model.ContratoAluguel;
import com.loja.padrao.facade.interfaces.ILojaFacade;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.format.DateTimeFormatter;

public class DevolucaoFuncionarioController {

    @FXML
    private TableView<ContratoAluguel> tblContratos;

    @FXML
    private TableColumn<ContratoAluguel, String> colId;

    @FXML
    private TableColumn<ContratoAluguel, String> colCliente;

    @FXML
    private TableColumn<ContratoAluguel, String> colItem;

    @FXML
    private TableColumn<ContratoAluguel, String> colRetirada;

    @FXML
    private TableColumn<ContratoAluguel, String> colPrevDevolucao;

    @FXML
    private TableColumn<ContratoAluguel, String> colSituacao;

    @FXML
    private TableColumn<ContratoAluguel, String> colAcao;

    @FXML
    private Label mensagemLabel;

    private ILojaFacade facade;

    private final DateTimeFormatter formatoData =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void setFacade(ILojaFacade facade) {
        this.facade = facade;
        configurarColunas();
        carregarContratos();
    }

    @FXML
    public void initialize() {
        configurarColunas();
    }

    private void configurarColunas() {
        colId.setCellValueFactory(dado ->
                new SimpleStringProperty(dado.getValue().getId()));

        colCliente.setCellValueFactory(dado ->
                new SimpleStringProperty(
                        dado.getValue().getCliente().getNome()));

        colItem.setCellValueFactory(dado ->
                new SimpleStringProperty(
                        dado.getValue().getItem().getNome()));

        colPrevDevolucao.setCellValueFactory(dado ->
                new SimpleStringProperty(
                        dado.getValue().getDataPrevDevolucao()
                                .format(formatoData)));

        colSituacao.setCellValueFactory(dado ->
                new SimpleStringProperty(
                        String.valueOf(dado.getValue().getStatus())));

        colRetirada.setCellValueFactory(dado ->
                new SimpleStringProperty(
                        dado.getValue().getDataRetirada()
                                .format(formatoData)));

        colAcao.setCellValueFactory(dado ->
                new SimpleStringProperty("Devolver"));
    }

    @FXML
    public void carregarContratos() {
        if (facade == null) {
            return;
        }

        try {
            /*
             * ATENÇÃO:
             * Confirme se listarContratosAtivos() existe na sua ILojaFacade.
             */
            tblContratos.getItems().setAll(
                    facade.listarContratosAtivos().values()
            );

            mensagemLabel.setText(
                    tblContratos.getItems().isEmpty()
                            ? "Nenhum contrato ativo encontrado."
                            : "Contratos carregados com sucesso."
            );

        } catch (RuntimeException e) {
            mensagemLabel.setText(
                    "Erro ao carregar contratos: " + e.getMessage()
            );
        }
    }

    @FXML
    public void processarDevolucao() {
        ContratoAluguel selecionado =
                tblContratos.getSelectionModel().getSelectedItem();

        if (selecionado == null) {
            mensagemLabel.setText(
                    "Selecione um contrato para processar a devolução."
            );
            return;
        }

        try {
            ContratoAluguel contratoDevolvido =
                    facade.processarDevolucao(selecionado.getId());

            boolean possuiMulta = facade.possuiMultaPendente(
                    contratoDevolvido.getCliente().getId()
            );

            mensagemLabel.setText(
                    possuiMulta
                            ? "Devolução processada. O cliente possui multa pendente."
                            : "Devolução processada com sucesso!"
            );

            carregarContratos();

        } catch (RuntimeException e) {
            mensagemLabel.setText(
                    "Erro ao processar devolução: " + e.getMessage()
            );
        }
    }
}
