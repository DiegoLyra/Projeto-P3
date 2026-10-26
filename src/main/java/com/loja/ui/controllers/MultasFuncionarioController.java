package com.loja.ui.controllers;

import com.loja.model.Multa;
import com.loja.padrao.facade.interfaces.ILojaFacade;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class MultasFuncionarioController {

    @FXML private TableView<Multa> tabelaMultas;

    @FXML private TableColumn<Multa, String> colId;
    @FXML private TableColumn<Multa, String> colContrato;
    @FXML private TableColumn<Multa, String> colCliente;
    @FXML private TableColumn<Multa, String> colMotivo;
    @FXML private TableColumn<Multa, String> colValor;
    @FXML private TableColumn<Multa, String> colSituacao;
    @FXML private TableColumn<Multa, Void> colAcao;

    @FXML private Label lblTotalMultas;
    @FXML private Label lblValorTotal;
    @FXML private Label lblMensagem;

    private ILojaFacade facade;

    private final NumberFormat formatoMoeda =
            NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public void setFacade(ILojaFacade facade) {
        this.facade = facade;
        configurarTabela();
        atualizarMultas();
    }

    @FXML
    private void initialize() {
        configurarTabela();
    }

    private void configurarTabela() {
        colId.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getId()));

        colContrato.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getContrato().getId()));

        colCliente.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getContrato()
                                .getCliente().getNome()));

        colMotivo.setCellValueFactory(d ->
                new SimpleStringProperty("Atraso na devolução"));

        colValor.setCellValueFactory(d ->
                new SimpleStringProperty(
                        formatoMoeda.format(
                                d.getValue().getValorTotal())));

        colSituacao.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getStatus()));

        configurarBotaoQuitar();
    }

    private void configurarBotaoQuitar() {
        colAcao.setCellFactory(coluna -> new TableCell<>() {

            private final Button btnQuitar = new Button("Quitar");

            {
                btnQuitar.setStyle(
                        "-fx-background-color: #059669;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
                );

                btnQuitar.setOnAction(event -> {
                    Multa multa = getTableView()
                            .getItems().get(getIndex());

                    quitarMulta(multa);
                });
            }

            @Override
            protected void updateItem(Void item, boolean vazio) {
                super.updateItem(item, vazio);

                if (vazio) {
                    setGraphic(null);
                    return;
                }

                Multa multa = getTableView()
                        .getItems().get(getIndex());

                boolean pendente =
                        "PENDENTE".equalsIgnoreCase(multa.getStatus());

                btnQuitar.setDisable(!pendente);
                btnQuitar.setText(pendente ? "Quitar" : "Quitada");

                setGraphic(btnQuitar);
            }
        });
    }

    @FXML
    public void atualizarMultas() {
        if (facade == null) {
            return;
        }

        try {
            tabelaMultas.getItems().clear();

            Map<String, Multa> multas = facade.listarMulta();

            BigDecimal total = BigDecimal.ZERO;
            int quantidade = 0;

            for (Multa multa : multas.values()) {
                if ("PENDENTE".equalsIgnoreCase(multa.getStatus())) {
                    tabelaMultas.getItems().add(multa);
                    total = total.add(multa.getValorTotal());
                    quantidade++;
                }
            }

            lblTotalMultas.setText(
                    "Multas pendentes: " + quantidade);

            lblValorTotal.setText(
                    "Valor total: " + formatoMoeda.format(total));

            lblMensagem.setText(
                    quantidade == 0
                            ? "Não existem multas pendentes."
                            : "");

        } catch (RuntimeException e) {
            lblMensagem.setText(
                    "Erro ao carregar multas: " + e.getMessage());
        }
    }

    private void quitarMulta(Multa multa) {
        try {
            facade.quitarMulta(multa.getId());

            lblMensagem.setText(
                    "Multa " + multa.getId()
                            + " quitada com sucesso.");

            atualizarMultas();

        } catch (RuntimeException e) {
            lblMensagem.setText(
                    "Erro ao quitar multa: " + e.getMessage());
        }
    }
}