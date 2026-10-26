
package com.loja.ui.controllers;

import com.loja.model.ContratoAluguel;
import com.loja.padrao.facade.interfaces.ILojaFacade;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

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

    @FXML
    public void initialize() {
        configurarColunas();
        configurarBotaoDevolucao();
    }

    public void setFacade(ILojaFacade facade) {
        this.facade = facade;
        carregarContratos();
    }

    private void configurarColunas() {
        colId.setCellValueFactory(dado ->
                new SimpleStringProperty(
                        dado.getValue().getId()));

        colCliente.setCellValueFactory(dado ->
                new SimpleStringProperty(
                        dado.getValue().getCliente().getNome()));

        colItem.setCellValueFactory(dado ->
                new SimpleStringProperty(
                        dado.getValue().getItem().getNome()));

        colRetirada.setCellValueFactory(dado ->
                new SimpleStringProperty(
                        dado.getValue().getDataRetirada()
                                .format(formatoData)));

        colPrevDevolucao.setCellValueFactory(dado ->
                new SimpleStringProperty(
                        dado.getValue().getDataPrevDevolucao()
                                .format(formatoData)));

        colSituacao.setCellValueFactory(dado ->
                new SimpleStringProperty(
                        String.valueOf(
                                dado.getValue().getStatus())));

        colAcao.setCellValueFactory(dado ->
                new SimpleStringProperty(""));
    }

    private void configurarBotaoDevolucao() {
        colAcao.setCellFactory(coluna -> new TableCell<>() {

            private final Button btnDevolver =
                    new Button("Devolver");

            {
                btnDevolver.setOnAction(event -> {
                    ContratoAluguel contrato =
                            getTableView().getItems().get(getIndex());

                    confirmarDevolucao(contrato);
                });
            }

            @Override
            protected void updateItem(String item, boolean vazio) {
                super.updateItem(item, vazio);

                if (vazio) {
                    setGraphic(null);
                } else {
                    setGraphic(btnDevolver);
                }
            }
        });
    }

    @FXML
    public void carregarContratos() {
        if (facade == null) {
            mensagemLabel.setText(
                    "Erro: a facade não foi configurada.");
            return;
        }

        try {
            Map<String, ContratoAluguel> contratos =
                    facade.listarContratosAtivos();

            if (contratos == null) {
                tblContratos.getItems().clear();
                mensagemLabel.setText(
                        "Erro: a lista de contratos retornou nula.");
                return;
            }

            tblContratos.getItems().setAll(contratos.values());

            if (contratos.isEmpty()) {
                mensagemLabel.setText(
                        "Nenhum contrato ativo encontrado.");
            } else {
                mensagemLabel.setText(
                        "Contratos ativos encontrados: "
                                + contratos.size());
            }

        } catch (RuntimeException e) {
            e.printStackTrace();

            mensagemLabel.setText(
                    "Erro ao carregar contratos: "
                            + e.getMessage());
        }
    }

    private void confirmarDevolucao(ContratoAluguel contrato) {
        if (facade == null) {
            mensagemLabel.setText(
                    "Erro: a facade não foi configurada.");
            return;
        }

        Alert confirmacao = new Alert(
                Alert.AlertType.CONFIRMATION);

        confirmacao.setTitle("Registrar devolução");
        confirmacao.setHeaderText("Confirmar devolução");
        confirmacao.setContentText(
                "Deseja registrar a devolução do contrato "
                        + contrato.getId() + "?");

        Optional<ButtonType> resposta =
                confirmacao.showAndWait();

        if (resposta.isPresent()
                && resposta.get() == ButtonType.OK) {
            processarDevolucao(contrato);
        }
    }

    private void processarDevolucao(
            ContratoAluguel selecionado) {

        try {
            ContratoAluguel contratoDevolvido =
                    facade.processarDevolucao(
                            selecionado.getId());

            boolean possuiMulta =
                    facade.possuiMultaPendente(
                            contratoDevolvido
                                    .getCliente().getId());

            Alert resultado;

            if (possuiMulta) {
                resultado = new Alert(
                        Alert.AlertType.WARNING);
                resultado.setTitle("Devolução registrada");
                resultado.setHeaderText(
                        "Devolução processada com sucesso");
                resultado.setContentText(
                        "O cliente possui multa pendente.");
            } else {
                resultado = new Alert(
                        Alert.AlertType.INFORMATION);
                resultado.setTitle("Devolução registrada");
                resultado.setHeaderText("Sucesso");
                resultado.setContentText(
                        "Devolução registrada com sucesso!");
            }

            resultado.showAndWait();

            mensagemLabel.setText(
                    "Devolução processada para o contrato "
                            + contratoDevolvido.getId());

            carregarContratos();

        } catch (RuntimeException e) {
            e.printStackTrace();

            Alert erro = new Alert(Alert.AlertType.ERROR);
            erro.setTitle("Erro na devolução");
            erro.setHeaderText(
                    "Não foi possível processar a devolução");
            erro.setContentText(e.getMessage());
            erro.showAndWait();

            mensagemLabel.setText(
                    "Erro ao processar devolução: "
                            + e.getMessage());
        }
    }

    @FXML
    public void processarDevolucao() {
        ContratoAluguel selecionado =
                tblContratos.getSelectionModel()
                        .getSelectedItem();

        if (selecionado == null) {
            mensagemLabel.setText(
                    "Selecione um contrato para devolver.");
            return;
        }

        confirmarDevolucao(selecionado);
    }
}
