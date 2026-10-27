
package com.loja.ui.controllers;

import com.loja.model.Multa;
import com.loja.padrao.facade.interfaces.ILojaFacade;
import com.loja.ui.components.ColunaTexto;
import com.loja.ui.relatorios.GeradorRelatorios;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class MultaFuncionarioController {

    @FXML
    private TableView<Multa> tblMultas;

    @FXML
    private TableColumn<Multa, String> colId;

    @FXML
    private TableColumn<Multa, String> colCliente;

    @FXML
    private TableColumn<Multa, String> colContrato;

    @FXML
    private TableColumn<Multa, String> colValor;

    @FXML
    private TableColumn<Multa, String> colSituacao;

    @FXML
    private TableColumn<Multa, Void> colAcao;

    @FXML
    private Label mensagemLabel;

    private ILojaFacade facade;

    private final NumberFormat formatoMoeda =
            NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @FXML
    public void initialize() {
        configurarColunas();
        configurarBotaoQuitar();
    }

    public void setFacade(ILojaFacade facade) {
        this.facade = facade;
        carregarMultas();
    }

        private void configurarColunas() {
        colId.setCellValueFactory(new ColunaTexto<Multa>() {
            @Override
            protected String valor(Multa m) {
                return m.getId();
            }
        });
        colCliente.setCellValueFactory(new ColunaTexto<Multa>() {
            @Override
            protected String valor(Multa m) {
                return m.getContrato().getCliente().getNome();
            }
        });
        colContrato.setCellValueFactory(new ColunaTexto<Multa>() {
            @Override
            protected String valor(Multa m) {
                return m.getContrato().getId();
            }
        });
        colValor.setCellValueFactory(new ColunaTexto<Multa>() {
            @Override
            protected String valor(Multa m) {
                return GeradorRelatorios.formatarMoeda(m.getValorTotal());
            }
        });
        colSituacao.setCellValueFactory(new ColunaTexto<Multa>() {
            @Override
            protected String valor(Multa m) {
                return m.getStatus();
            }
        });
    }

    private void configurarBotaoQuitar() {
        colAcao.setCellFactory(coluna -> new TableCell<>() {

            private final Button btnQuitar = new Button("Quitar");
            private final HBox container = new HBox(btnQuitar);

            {
                container.setStyle("-fx-alignment: center;");

                btnQuitar.setStyle(
                        "-fx-background-color: #16a34a;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;"
                );

                btnQuitar.setOnAction(event -> {
                    Multa multa = getTableView()
                            .getItems()
                            .get(getIndex());

                    confirmarQuitacao(multa);
                });
            }

            @Override
            protected void updateItem(Void item, boolean vazio) {
                super.updateItem(item, vazio);

                if (vazio) {
                    setGraphic(null);
                } else {
                    Multa multa = getTableView()
                            .getItems()
                            .get(getIndex());

                    boolean quitada = multa.getStatus()
                            .equalsIgnoreCase("QUITADA");

                    btnQuitar.setDisable(quitada);
                    btnQuitar.setText(quitada ? "Quitada" : "Quitar");

                    if (quitada) {
                        btnQuitar.setStyle(
                                "-fx-background-color: #9ca3af;" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 5;"
                        );
                    } else {
                        btnQuitar.setStyle(
                                "-fx-background-color: #16a34a;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 5;" +
                                "-fx-cursor: hand;"
                        );
                    }

                    setGraphic(container);
                }
            }
        });
    }

    @FXML
    public void carregarMultas() {
        if (facade == null) {
            mensagemLabel.setText(
                    "Erro: a conexão com o sistema não foi configurada."
            );
            return;
        }

        try {
            Map<String, Multa> multas = facade.listarMulta();

            if (multas == null) {
                tblMultas.getItems().clear();
                mensagemLabel.setText(
                        "Erro: a lista de multas retornou vazia."
                );
                return;
            }

            tblMultas.getItems().setAll(multas.values());

            if (multas.isEmpty()) {
                mensagemLabel.setText("Nenhuma multa cadastrada.");
            } else {
                mensagemLabel.setText(
                        "Total de multas encontradas: " + multas.size()
                );
            }

        } catch (RuntimeException e) {

            mensagemLabel.setText(
                    "Erro ao carregar multas: " + e.getMessage()
            );
        }
    }

    private void confirmarQuitacao(Multa multa) {
        if (facade == null || multa == null) {
            mensagemLabel.setText(
                    "Não foi possível identificar a multa."
            );
            return;
        }

        if ("QUITADA".equalsIgnoreCase(multa.getStatus())) {
            mensagemLabel.setText("Esta multa já foi quitada.");
            return;
        }

        Alert confirmacao = new Alert(AlertType.CONFIRMATION);

        confirmacao.setTitle("Confirmar quitação");
        confirmacao.setHeaderText("Quitar multa " + multa.getId());
        confirmacao.setContentText(
                "Deseja realmente quitar esta multa?\n\n" +
                "Cliente: " + multa.getContrato().getCliente().getNome() +
                "\nValor: " +
                formatoMoeda.format(multa.getValorTotal())
        );

        confirmacao.showAndWait().ifPresent(resposta -> {
            if (resposta == ButtonType.OK) {
                quitarMulta(multa);
            }
        });
    }

    private void quitarMulta(Multa multa) {
        try {
            facade.quitarMulta(multa.getId());

            mensagemLabel.setText(
                    "Multa " + multa.getId() +
                    " quitada com sucesso!"
            );

            carregarMultas();

        } catch (RuntimeException e) {

            Alert erro = new Alert(AlertType.ERROR);
            erro.setTitle("Erro ao quitar multa");
            erro.setHeaderText("Não foi possível quitar a multa.");
            erro.setContentText(e.getMessage());
            erro.showAndWait();

            mensagemLabel.setText(
                    "Erro ao quitar multa: " + e.getMessage()
            );
        }
    }
}