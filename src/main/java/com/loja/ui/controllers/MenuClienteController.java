package com.loja.ui.controllers;

import com.loja.model.Cliente;
import com.loja.model.ContratoAluguel;
import com.loja.model.Item;
import com.loja.model.Multa;
import com.loja.padrao.facade.interfaces.ILojaFacade;
import com.loja.ui.navigation.Navigator;

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
import javafx.scene.layout.VBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class MenuClienteController {

    private static final Logger logger = LoggerFactory.getLogger(MenuClienteController.class);
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ILojaFacade facade;
    private final Navigator navigator;
    private Cliente usuarioLogado;

    @FXML private Label lblBoasVindas;

    // ITENS DISPONÍVEIS
    @FXML private FlowPane containerCardsItens;

    // ALUGUÉIS
    @FXML private TableView<ContratoAluguel> tblAlugueis;
    @FXML private TableColumn<ContratoAluguel, String> colContratoId;
    @FXML private TableColumn<ContratoAluguel, String> colContratoItem;
    @FXML private TableColumn<ContratoAluguel, String> colContratoStatus;
    @FXML private TableColumn<ContratoAluguel, String> colContratoValorTotal;
    @FXML private TableColumn<ContratoAluguel, String> colContratoDevolucaoEfetiva;

    // MULTAS
    @FXML private HBox bannerMultas;
    @FXML private Label lblBannerMulta;
    @FXML private TableView<Multa> tblMultas;
    @FXML private TableColumn<Multa, String> colMultaId;
    @FXML private TableColumn<Multa, String> colMultaMotivo;
    @FXML private TableColumn<Multa, String> colMultaValor;
    @FXML private TableColumn<Multa, String> colMultaStatus;

    public MenuClienteController(ILojaFacade facade, Navigator navigator) {
        this.facade = facade;
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        if (!(navigator.getUsuarioLogado() instanceof Cliente cliente)) {
            throw new IllegalStateException("Usuário logado não é um cliente.");
        }
        this.usuarioLogado = cliente;
        lblBoasVindas.setText("ÁREA DO CLIENTE: " + cliente.getNome().toUpperCase());

        configurarTabelas();
        carregarDadosIniciais();
    }

    private void configurarTabelas() {
        colContratoId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));
        colContratoItem.setCellValueFactory(cellData -> {
            Item item = cellData.getValue().getItem();
            return new SimpleStringProperty(item != null ? item.getNome() : "N/I");
        });
        colContratoStatus.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus()));
        colContratoValorTotal.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatarMoeda(cellData.getValue().getValorTotal())));
        colContratoDevolucaoEfetiva.setCellValueFactory(cellData -> {
            LocalDate devEfetiva = cellData.getValue().getDataEfetivaDevolucao();
            return new SimpleStringProperty(devEfetiva != null
                    ? devEfetiva.format(FORMATO_DATA)
                    : "Pendente / Em Aberto");
        });

        colMultaId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));
        colMultaMotivo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMotivo()));
        colMultaValor.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatarMoeda(cellData.getValue().getValorTotal())));
        colMultaStatus.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getStatus())));

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
                        setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    } else if (lower.contains("paga") || lower.contains("quitada")) {
                        setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    } else {
                        setStyle("-fx-alignment: CENTER;");
                    }
                }
            }
        });
    }

    private void carregarDadosIniciais() {
        carregarItensDisponiveis();
        carregarMeusAlugueis();
        carregarMultasPendentes();
    }

    @FXML
    public void carregarItensDisponiveis() {
        containerCardsItens.getChildren().clear();
        try {
            Map<String, Item> itens = facade.listarItensDisponiveis();
            if (itens == null || itens.isEmpty()) {
                Label lblVazio = new Label("Não há itens disponíveis para aluguel no momento.");
                containerCardsItens.getChildren().add(lblVazio);
                return;
            }

            for (Item item : itens.values()) {
                containerCardsItens.getChildren().add(criarCardItem(item));
            }
        } catch (RuntimeException e) {
            logger.error("Falha ao listar itens disponíveis: {}", e.getMessage(), e);
            exibirAlertaErro("Erro", "Erro ao listar itens: " + e.getMessage());
        }
    }

    private VBox criarCardItem(Item item) {
        VBox card = new VBox(8);
        card.setPrefSize(200, 140);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dcdcdc; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        Label lblNome = new Label(item.getNome());
        lblNome.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        lblNome.setWrapText(true);

        Label lblId = new Label("ID: " + item.getId());
        lblId.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");

        Label lblValor = new Label(formatarMoeda(item.getTaxaDiaria()) + " / dia");
        lblValor.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #27ae60;");

        Button btnAlugar = new Button("Alugar");
        btnAlugar.setMaxWidth(Double.MAX_VALUE);
        btnAlugar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        btnAlugar.setOnAction(e -> solicitarAluguel(item));

        card.getChildren().addAll(lblNome, lblId, lblValor, btnAlugar);
        return card;
    }

    private void solicitarAluguel(Item item) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Aluguel");
        alert.setHeaderText(null);
        alert.setContentText("O aluguel de \"" + item.getNome() + "\" ainda não está disponível pela interface.");
        alert.showAndWait();
    }

    @FXML
    public void carregarMeusAlugueis() {
        try {
            Map<String, ContratoAluguel> contratos = facade.consultarHistoricoCliente(usuarioLogado.getId());
            tblAlugueis.setItems(contratos == null
                    ? FXCollections.emptyObservableList()
                    : FXCollections.observableArrayList(contratos.values()));
        } catch (RuntimeException e) {
            logger.error("Falha ao buscar histórico do cliente '{}': {}", usuarioLogado.getId(), e.getMessage(), e);
            exibirAlertaErro("Erro", "Erro ao buscar histórico: " + e.getMessage());
        }
    }

    @FXML
    public void carregarMultasPendentes() {
        try {
            String clienteId = usuarioLogado.getId();

            if (facade.possuiMultaPendente(clienteId)) {
                bannerMultas.setStyle("-fx-background-color: #f8d7da; -fx-border-color: #f5c6cb; -fx-border-radius: 5; -fx-background-radius: 5;");
                lblBannerMulta.setText("🚨 Você possui multas pendentes! Regularize a sua situação.");
                lblBannerMulta.setStyle("-fx-text-fill: #721c24; -fx-font-weight: bold;");
            } else {
                bannerMultas.setStyle("-fx-background-color: #d4edda; -fx-border-color: #c3e6cb; -fx-border-radius: 5; -fx-background-radius: 5;");
                lblBannerMulta.setText("✅ Você não possui multas pendentes no momento.");
                lblBannerMulta.setStyle("-fx-text-fill: #155724; -fx-font-weight: bold;");
            }

            Map<String, Multa> multas = facade.listarMultaPorCliente(clienteId);
            tblMultas.setItems(multas == null
                    ? FXCollections.emptyObservableList()
                    : FXCollections.observableArrayList(multas.values()));
        } catch (RuntimeException e) {
            logger.error("Falha ao verificar multas do cliente '{}': {}", usuarioLogado.getId(), e.getMessage(), e);
            exibirAlertaErro("Erro", "Erro ao verificar multas: " + e.getMessage());
        }
    }

    @FXML
    public void handleSair() {
        navigator.showLogin();
    }

    private static String formatarMoeda(BigDecimal valor) {
        return valor == null ? "R$ 0,00" : String.format("R$ %.2f", valor);
    }

    private void exibirAlertaErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}