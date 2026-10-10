package com.loja.ui.controllers;

import com.loja.model.Cliente;
import com.loja.model.ContratoAluguel;
import com.loja.model.Item;
import com.loja.model.Multa;
import com.loja.padrao.facade.interfaces.ILojaFacade;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class MenuClienteController {

    private static final Logger logger = LoggerFactory.getLogger(MenuClienteController.class);

    private ILojaFacade facade;
    private Cliente usuarioLogado;

    // Componentes de Cabeçalho e Navegação
    @FXML private Label lblBoasVindas;
    @FXML private Label lblTituloSecao;
    @FXML private Button btnNavItens;
    @FXML private Button btnNavAlugueis;
    @FXML private Button btnNavMultas;

    // Painéis de Conteúdo
    @FXML private VBox paneItens;
    @FXML private VBox paneAlugueis;
    @FXML private VBox paneMultas;

    @FXML private FlowPane containerCardsItens;

    @FXML private TableView<ContratoAluguel> tblAlugueis;
    @FXML private TableColumn<ContratoAluguel, String> colContratoId;
    @FXML private TableColumn<ContratoAluguel, String> colContratoItem;
    @FXML private TableColumn<ContratoAluguel, Object> colContratoRetirada;
    @FXML private TableColumn<ContratoAluguel, Object> colContratoDevolucao;
    @FXML private TableColumn<ContratoAluguel, Object> colContratoValorTotal;
    @FXML private TableColumn<ContratoAluguel, String> colContratoStatus;

    @FXML private HBox bannerMultas;
    @FXML private Label lblBannerMulta;
    @FXML private TableView<Multa> tblMultas;
    @FXML private TableColumn<Multa, String> colMultaId;
    @FXML private TableColumn<Multa, String> colMultaMotivo;
    @FXML private TableColumn<Multa, Object> colMultaDias;
    @FXML private TableColumn<Multa, Object> colMultaValor;
    @FXML private TableColumn<Multa, String> colMultaStatus;

    public void initData(ILojaFacade facade, Cliente usuarioLogado) {
        this.facade = facade;
        this.usuarioLogado = usuarioLogado;

        if (usuarioLogado != null && usuarioLogado.getNome() != null) {
            this.lblBoasVindas.setText("ÁREA DO CLIENTE: " + usuarioLogado.getNome().toUpperCase());
        }

        configurarTabelas();
        mostrarAbaItens();
    }

    private void configurarTabelas() {
        colContratoId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));
        colContratoItem.setCellValueFactory(cellData -> {
            Item item = cellData.getValue().getItem();
            return new SimpleStringProperty(item != null ? item.getNome() : "Item N/I");
        });
        colContratoRetirada.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDataRetirada()));
        colContratoDevolucao.setCellValueFactory(cellData -> {
            Object devEfetiva = cellData.getValue().getDataEfetivaDevolucao();
            if (devEfetiva == null) {
                devEfetiva = cellData.getValue().getDataPrevDevolucao();
            }
            return new SimpleObjectProperty<>(devEfetiva != null ? devEfetiva : "Em Aberto");
        });
        colContratoValorTotal.setCellValueFactory(cellData -> new SimpleObjectProperty<>(String.format("R$ %.2f", cellData.getValue().getValorTotal())));
        colContratoStatus.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getStatus())));

        colContratoStatus.setCellFactory(column -> new TableCell<ContratoAluguel, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equalsIgnoreCase("ENCERRADO") || item.equalsIgnoreCase("CONCLUIDO")) {
                        setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #334155; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    } else {
                        setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    }
                }
            }
        });

        colMultaId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));
        colMultaMotivo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMotivo()));
        colMultaDias.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDiasAtraso() + " dias"));
        colMultaValor.setCellValueFactory(cellData -> new SimpleObjectProperty<>(String.format("R$ %.2f", cellData.getValue().getValorTotal())));
        colMultaStatus.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getStatus())));

        colMultaStatus.setCellFactory(column -> new TableCell<Multa, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String lower = item.toLowerCase();
                    if (lower.contains("pendente") || lower.contains("aberto")) {
                        setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #991b1b; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    } else if (lower.contains("quitada") || lower.contains("paga")) {
                        setStyle("-fx-background-color: #f0fdf4; -fx-text-fill: #166534; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    } else {
                        setStyle("-fx-alignment: CENTER;");
                    }
                }
            }
        });
    }

    @FXML
    public void mostrarAbaItens() {
        lblTituloSecao.setText("📦 Itens Disponíveis para Aluguel");
        paneItens.setVisible(true);
        paneAlugueis.setVisible(false);
        paneMultas.setVisible(false);

        destacarBotaoNav(btnNavItens, btnNavAlugueis, btnNavMultas);
        carregarItensDisponiveis();
    }

    @FXML
    public void mostrarAbaAlugueis() {
        lblTituloSecao.setText("📄 Meus Aluguéis (Histórico do Cliente)");
        paneItens.setVisible(false);
        paneAlugueis.setVisible(true);
        paneMultas.setVisible(false);

        destacarBotaoNav(btnNavAlugueis, btnNavItens, btnNavMultas);
        carregarMeusAlugueis();
    }

    @FXML
    public void mostrarAbaMultas() {
        lblTituloSecao.setText("⚠️ Minhas Multas");
        paneItens.setVisible(false);
        paneAlugueis.setVisible(false);
        paneMultas.setVisible(true);

        destacarBotaoNav(btnNavMultas, btnNavItens, btnNavAlugueis);
        carregarMultasPendentes();
    }

    private void destacarBotaoNav(Button ativo, Button... inativos) {
        ativo.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        for (Button btn : inativos) {
            btn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        }
    }

    @FXML
    public void carregarItensDisponiveis() {
        containerCardsItens.getChildren().clear();
        try {
            Map<String, Item> itens = facade.listarItensDisponiveis();
            if (itens == null || itens.isEmpty()) {
                Label lblVazio = new Label("Não há itens disponíveis para aluguel no momento.");
                lblVazio.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px;");
                containerCardsItens.getChildren().add(lblVazio);
                return;
            }

            for (Item item : itens.values()) {
                VBox card = criarCardItem(item);
                containerCardsItens.getChildren().add(card);
            }
        } catch (RuntimeException e) {
            logger.error("Falha ao listar itens do CSV: {}", e.getMessage(), e);
            exibirAlertaErro("Erro", "Falha ao carregar itens disponíveis: " + e.getMessage());
        }
    }

    private VBox criarCardItem(Item item) {
        VBox card = new VBox(10);
        card.setPrefSize(260, 155);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-background-radius: 10;");

        Label lblId = new Label("#" + item.getId());
        lblId.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #4f46e5;");

        Label lblNome = new Label(item.getNome());
        lblNome.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0f172a;");
        lblNome.setWrapText(true);

        HBox precoBox = new HBox();
        Label lblValorTexto = new Label("Valor Diário:");
        lblValorTexto.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblValor = new Label(String.format("R$ %.2f", item.getTaxaDiaria()));
        lblValor.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #059669;");

        precoBox.getChildren().addAll(lblValorTexto, spacer, lblValor);

        Button btnAlugar = new Button("Solicitar Aluguel");
        btnAlugar.setMaxWidth(Double.MAX_VALUE);
        btnAlugar.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
        
        btnAlugar.setOnAction(e -> solicitarAluguelItem(item));

        card.getChildren().addAll(lblId, lblNome, precoBox, btnAlugar);
        return card;
    }

    private void solicitarAluguelItem(Item item) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Solicitação de Aluguel");
        alert.setHeaderText("Item Selecionado: " + item.getNome());
        alert.setContentText("Solicitação enviada com sucesso para o item #" + item.getId() + " (Taxa: R$ " + item.getTaxaDiaria() + "/dia).");
        alert.showAndWait();
    }

    @FXML
    public void carregarMeusAlugueis() {
        try {
            if (usuarioLogado != null) {
                Map<String, ContratoAluguel> contratos = facade.consultarHistoricoCliente(usuarioLogado.getId());
                if (contratos != null) {
                    tblAlugueis.setItems(FXCollections.observableArrayList(contratos.values()));
                }
            }
        } catch (RuntimeException e) {
            logger.error("Falha ao consultar histórico do cliente '{}': {}", usuarioLogado.getId(), e.getMessage(), e);
            exibirAlertaErro("Erro", "Erro ao carregar histórico de aluguéis: " + e.getMessage());
        }
    }

    @FXML
    public void carregarMultasPendentes() {
        try {
            if (usuarioLogado != null) {
                boolean temMultaPendente = facade.possuiMultaPendente(usuarioLogado.getId());
                
                if (!temMultaPendente) {
                    bannerMultas.setStyle("-fx-background-color: #f0fdf4; -fx-border-color: #bbf7d0; -fx-border-radius: 8; -fx-background-radius: 8;");
                    lblBannerMulta.setText("✅ Você não possui multas pendentes no momento.");
                    lblBannerMulta.setStyle("-fx-text-fill: #166534; -fx-font-weight: bold;");
                } else {
                    bannerMultas.setStyle("-fx-background-color: #fef2f2; -fx-border-color: #fecaca; -fx-border-radius: 8; -fx-background-radius: 8;");
                    lblBannerMulta.setText("⚠️ Atenção: Você possui multas pendentes de pagamento. Regularize a situação.");
                    lblBannerMulta.setStyle("-fx-text-fill: #991b1b; -fx-font-weight: bold;");
                }

                Map<String, Multa> multas = facade.listarMultaPorCliente(usuarioLogado.getId());
                if (multas != null) {
                    tblMultas.setItems(FXCollections.observableArrayList(multas.values()));
                }
            }
        } catch (RuntimeException e) {
            logger.error("Falha ao consultar multas do cliente '{}': {}", usuarioLogado.getId(), e.getMessage(), e);
            exibirAlertaErro("Erro", "Erro ao carregar multas: " + e.getMessage());
        }
    }

    @FXML
    public void handleSair() {
        Stage stage = (Stage) lblBoasVindas.getScene().getWindow();
        stage.close();
    }

    private void exibirAlertaErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}