package com.loja.ui.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loja.model.Cliente;
import com.loja.model.ContratoAluguel;
import com.loja.model.Item;
import com.loja.model.Multa;
import com.loja.padrao.facade.interfaces.ILojaFacade;
import com.loja.ui.navigation.Navigator;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MenuClienteController {

    private static final Logger logger = LoggerFactory.getLogger(MenuClienteController.class);
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int ITENS_POR_PAGINA = 5;

    private final ILojaFacade facade;
    private final Navigator navigator;
    private Cliente usuarioLogado;

    private final ObservableList<ContratoAluguel> listaContratos = FXCollections.observableArrayList();
    private FilteredList<ContratoAluguel> listaContratosFiltrada;
    private final ObservableList<Multa> listaMultas = FXCollections.observableArrayList();

    @FXML private Label lblBoasVindas;
    @FXML private Label lblTituloSecao;
    @FXML private TextField txtPesquisa;
    @FXML private Button btnNavItens;
    @FXML private Button btnNavAlugueis;
    @FXML private Button btnNavMultas;

    @FXML private VBox paneItens;
    @FXML private VBox paneAlugueis;
    @FXML private VBox paneMultas;

    @FXML private FlowPane containerCardsItens;

    @FXML private TableView<ContratoAluguel> tblAlugueis;
    @FXML private TableColumn<ContratoAluguel, String> colContratoId;
    @FXML private TableColumn<ContratoAluguel, String> colContratoItem;
    @FXML private TableColumn<ContratoAluguel, String> colContratoRetirada;
    @FXML private TableColumn<ContratoAluguel, String> colContratoDevolucao;
    @FXML private TableColumn<ContratoAluguel, String> colContratoValorTotal;
    @FXML private TableColumn<ContratoAluguel, String> colContratoStatus;
    @FXML private Pagination paginacaoAlugueis;

    @FXML private HBox bannerMultas;
    @FXML private Label lblBannerMulta;
    @FXML private TableView<Multa> tblMultas;
    @FXML private TableColumn<Multa, String> colMultaId;
    @FXML private TableColumn<Multa, String> colMultaMotivo;
    @FXML private TableColumn<Multa, String> colMultaDias;
    @FXML private TableColumn<Multa, String> colMultaValor;
    @FXML private TableColumn<Multa, String> colMultaStatus;
    @FXML private Pagination paginacaoMultas;

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

        if (cliente.getNome() != null) {
            lblBoasVindas.setText("ÁREA DO CLIENTE: " + cliente.getNome().toUpperCase());
        }

        configurarTabelas();
        configurarPesquisa();
        configurarPaginacao();
        mostrarAbaItens();
    }

    private void configurarTabelas() {
        colContratoId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));
        colContratoItem.setCellValueFactory(cellData -> {
            Item item = cellData.getValue().getItem();
            return new SimpleStringProperty(item != null ? item.getNome() : "Item N/I");
        });
        colContratoRetirada.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatarData(cellData.getValue().getDataRetirada(), "-")));
        colContratoDevolucao.setCellValueFactory(cellData -> {
            LocalDate data = cellData.getValue().getDataEfetivaDevolucao();
            if (data == null) {
                data = cellData.getValue().getDataPrevDevolucao();
            }
            return new SimpleStringProperty(formatarData(data, "Em Aberto"));
        });
        colContratoValorTotal.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatarMoeda(cellData.getValue().getValorTotal())));
        colContratoStatus.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.valueOf(cellData.getValue().getStatus())));

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
        colMultaDias.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDiasAtraso() + " dias"));
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

private void configurarPesquisa() {
        listaContratosFiltrada = new FilteredList<>(listaContratos, p -> true);

        txtPesquisa.textProperty().addListener((observable, oldValue, newValue) -> {
            listaContratosFiltrada.setPredicate(contrato ->
                    newValue == null || newValue.isBlank()
                            || correspondeABusca(contrato, newValue.toLowerCase().trim()));

            atualizarPaginacaoAlugueis();
        });
    }

    // Junta os campos pesquisáveis do contrato e verifica se o termo aparece em algum deles
    private boolean correspondeABusca(ContratoAluguel contrato, String termoBusca) {
        Item item = contrato.getItem();
        LocalDate dataDevolucao = contrato.getDataEfetivaDevolucao() != null
                ? contrato.getDataEfetivaDevolucao()
                : contrato.getDataPrevDevolucao();

        String textoPesquisavel = String.join("\n",
                item != null && item.getNome() != null ? item.getNome() : "",
                contrato.getValorTotal() != null ? contrato.getValorTotal().toString() : "",
                contrato.getStatus() != null ? contrato.getStatus().toString() : "",
                contrato.getId() != null ? contrato.getId() : "",
                formatarData(contrato.getDataRetirada(), ""),
                formatarData(dataDevolucao, ""));

        return textoPesquisavel.toLowerCase().contains(termoBusca);
    }

    private boolean filtrarContrato(ContratoAluguel contrato, String termo) {
        if (termo == null || termo.isBlank()) {
            return true;
        }

        String termoBusca = termo.toLowerCase().trim();
        return matchItem(contrato, termoBusca)
                || matchValor(contrato, termoBusca)
                || matchStatus(contrato, termoBusca)
                || matchId(contrato, termoBusca)
                || matchDatas(contrato, termoBusca);
    }

    private boolean matchItem(ContratoAluguel contrato, String termo) {
        Item item = contrato.getItem();
        return item != null && item.getNome() != null && item.getNome().toLowerCase().contains(termo);
    }

    private boolean matchValor(ContratoAluguel contrato, String termo) {
        return contrato.getValorTotal() != null && contrato.getValorTotal().toString().contains(termo);
    }

    private boolean matchStatus(ContratoAluguel contrato, String termo) {
        return contrato.getStatus() != null && contrato.getStatus().toString().toLowerCase().contains(termo);
    }

    private boolean matchId(ContratoAluguel contrato, String termo) {
        return contrato.getId() != null && contrato.getId().toLowerCase().contains(termo);
    }

    private boolean matchDatas(ContratoAluguel contrato, String termo) {
        if (contrato.getDataRetirada() != null && formatarData(contrato.getDataRetirada(), "").contains(termo)) {
            return true;
        }

        LocalDate dataDevolucao = contrato.getDataEfetivaDevolucao() != null
                ? contrato.getDataEfetivaDevolucao()
                : contrato.getDataPrevDevolucao();

        return dataDevolucao != null && formatarData(dataDevolucao, "").contains(termo);
    }

    private void configurarPaginacao() {
        paginacaoAlugueis.currentPageIndexProperty().addListener((obs, oldIdx, newIdx) -> 
            tblAlugueis.setItems(obterItensPaginaAlugueis(newIdx.intValue()))
        );

        paginacaoMultas.currentPageIndexProperty().addListener((obs, oldIdx, newIdx) -> 
            tblMultas.setItems(obterItensPaginaMultas(newIdx.intValue()))
        );
    }

    private ObservableList<ContratoAluguel> obterItensPaginaAlugueis(int pageIndex) {
        int deIndex = pageIndex * ITENS_POR_PAGINA;
        int paraIndex = Math.min(deIndex + ITENS_POR_PAGINA, listaContratosFiltrada.size());

        if (deIndex >= 0 && deIndex < listaContratosFiltrada.size()) {
            return FXCollections.observableArrayList(listaContratosFiltrada.subList(deIndex, paraIndex));
        }
        return FXCollections.emptyObservableList();
    }

    private ObservableList<Multa> obterItensPaginaMultas(int pageIndex) {
        int deIndex = pageIndex * ITENS_POR_PAGINA;
        int paraIndex = Math.min(deIndex + ITENS_POR_PAGINA, listaMultas.size());

        if (deIndex >= 0 && deIndex < listaMultas.size()) {
            return FXCollections.observableArrayList(listaMultas.subList(deIndex, paraIndex));
        }
        return FXCollections.emptyObservableList();
    }

    private void atualizarPaginacaoAlugueis() {
        int totalItens = listaContratosFiltrada.size();
        int totalPaginas = (int) Math.ceil((double) totalItens / ITENS_POR_PAGINA);
        
        paginacaoAlugueis.setPageCount(totalPaginas == 0 ? 1 : totalPaginas);
        
        int paginaAtual = Math.clamp(paginacaoAlugueis.getCurrentPageIndex(), 0, Math.max(0, totalPaginas - 1));
        paginacaoAlugueis.setCurrentPageIndex(paginaAtual);
        tblAlugueis.setItems(obterItensPaginaAlugueis(paginaAtual));
    }

    private void atualizarPaginacaoMultas() {
        int totalItens = listaMultas.size();
        int totalPaginas = (int) Math.ceil((double) totalItens / ITENS_POR_PAGINA);
        
        paginacaoMultas.setPageCount(totalPaginas == 0 ? 1 : totalPaginas);
        
        int paginaAtual = Math.clamp(paginacaoMultas.getCurrentPageIndex(), 0, Math.max(0, totalPaginas - 1));
        paginacaoMultas.setCurrentPageIndex(paginaAtual);
        tblMultas.setItems(obterItensPaginaMultas(paginaAtual));
    }

    @FXML
    public void mostrarAbaItens() {
        lblTituloSecao.setText("📦 Itens Disponíveis para Aluguel");
        txtPesquisa.setVisible(false);
        alternarPaineis(paneItens);
        destacarBotaoNav(btnNavItens, btnNavAlugueis, btnNavMultas);
        carregarItensDisponiveis();
    }

    @FXML
    public void mostrarAbaAlugueis() {
        lblTituloSecao.setText("📄 Meus Aluguéis (Histórico do Cliente)");
        txtPesquisa.setVisible(true);
        alternarPaineis(paneAlugueis);
        destacarBotaoNav(btnNavAlugueis, btnNavItens, btnNavMultas);
        carregarMeusAlugueis();
    }

    @FXML
    public void mostrarAbaMultas() {
        lblTituloSecao.setText("⚠️ Minhas Multas");
        txtPesquisa.setVisible(false);
        alternarPaineis(paneMultas);
        destacarBotaoNav(btnNavMultas, btnNavItens, btnNavAlugueis);
        carregarMultasPendentes();
    }

    private void alternarPaineis(VBox visivel) {
        for (VBox pane : new VBox[]{paneItens, paneAlugueis, paneMultas}) {
            pane.setVisible(pane == visivel);
        }
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
                containerCardsItens.getChildren().add(criarCardItem(item));
            }
        } catch (RuntimeException e) {
            logger.error("Falha ao listar itens disponíveis: {}", e.getMessage(), e);
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

        Label lblValor = new Label(formatarMoeda(item.getTaxaDiaria()));
        lblValor.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #059669;");

        precoBox.getChildren().addAll(lblValorTexto, spacer, lblValor);

        Button btnAlugar = new Button("Solicitar Aluguel");
        btnAlugar.setMaxWidth(Double.MAX_VALUE);
        btnAlugar.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
        btnAlugar.setOnAction(e -> abrirModalSolicitacaoAluguel(item));

        card.getChildren().addAll(lblId, lblNome, precoBox, btnAlugar);
        return card;
    }

    private void abrirModalSolicitacaoAluguel(Item item) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Solicitação de Aluguel - " + item.getNome());

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #ffffff;");

        Label lblTitulo = new Label("Alugar: " + item.getNome());
        lblTitulo.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1e293b;");

        DatePicker dpRetirada = new DatePicker(LocalDate.now(ZoneId.systemDefault()));
        DatePicker dpDevolucao = new DatePicker(LocalDate.now(ZoneId.systemDefault()).plusDays(1));

        Label lblDiarias = new Label("Duração: 1 dia(s)");
        Label lblValorTotal = new Label("Valor Total Previsto: " + formatarMoeda(item.getTaxaDiaria()));
        lblValorTotal.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #059669;");

        Label lblMultaAviso = new Label("⚠️ Taxa de juros por atraso: R$ 5,50/dia + Multa Fixa de R$ 20,00");
        lblMultaAviso.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11px;");

        Label lblStatusConflito = new Label();
        lblStatusConflito.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        Button btnConfirmar = new Button("Confirmar Aluguel");
        btnConfirmar.setMaxWidth(Double.MAX_VALUE);
        btnConfirmar.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");

        Runnable recalcular = () -> {
            LocalDate dtInicio = dpRetirada.getValue();
            LocalDate dtFim = dpDevolucao.getValue();

            if (dtInicio == null || dtFim == null || !dtFim.isAfter(dtInicio)) {
                lblDiarias.setText("Período inválido");
                lblValorTotal.setText("Valor Total Previsto: R$ 0,00");
                lblStatusConflito.setText("❌ A data de devolução deve ser posterior à data de retirada.");
                lblStatusConflito.setStyle("-fx-text-fill: #dc2626;");
                btnConfirmar.setDisable(true);
                return;
            }

            long dias = ChronoUnit.DAYS.between(dtInicio, dtFim);
            BigDecimal total = item.getTaxaDiaria().multiply(BigDecimal.valueOf(dias));

            lblDiarias.setText("Duração: " + dias + " dia(s)");
            lblValorTotal.setText("Valor Total Previsto: " + formatarMoeda(total));

            boolean possuiConflito = verificarConflitoDeDatas(item.getId(), dtInicio, dtFim);

            if (possuiConflito) {
                lblStatusConflito.setText("❌ Este item já possui reserva/aluguel para o período selecionado.");
                lblStatusConflito.setStyle("-fx-text-fill: #dc2626;");
                btnConfirmar.setDisable(true);
            } else {
                lblStatusConflito.setText("✅ Período disponível para aluguel!");
                lblStatusConflito.setStyle("-fx-text-fill: #166534;");
                btnConfirmar.setDisable(false);
            }
        };

        dpRetirada.valueProperty().addListener((obs, oldV, newV) -> recalcular.run());
        dpDevolucao.valueProperty().addListener((obs, oldV, newV) -> recalcular.run());
        recalcular.run();

        btnConfirmar.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sucesso");
            alert.setHeaderText(null);
            alert.setContentText("Aluguel solicitado com sucesso para o item " + item.getNome() + "!");
            alert.showAndWait();
            modal.close();
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Data Retirada:"), 0, 0);
        grid.add(dpRetirada, 1, 0);
        grid.add(new Label("Data Prevista Devolução:"), 0, 1);
        grid.add(dpDevolucao, 1, 1);

        root.getChildren().addAll(lblTitulo, grid, lblDiarias, lblValorTotal, lblMultaAviso, lblStatusConflito, btnConfirmar);

        Scene scene = new Scene(root, 420, 320);
        modal.setScene(scene);
        modal.showAndWait();
    }

    private boolean verificarConflitoDeDatas(String itemId, LocalDate inicioDesejado, LocalDate fimDesejado) {
        try {
            Map<String, ContratoAluguel> historico = facade.consultarHistoricoCliente(usuarioLogado.getId());
            if (historico == null) return false;

            for (ContratoAluguel c : historico.values()) {
                if (c.getItem() != null && itemId.equalsIgnoreCase(c.getItem().getId())) {
                    LocalDate inicioExistente = c.getDataRetirada();
                    LocalDate fimExistente = c.getDataEfetivaDevolucao() != null ? c.getDataEfetivaDevolucao() : c.getDataPrevDevolucao();

                    if (inicioExistente != null && fimExistente != null) {
                        boolean sobrepoe = !(fimDesejado.isBefore(inicioExistente) || inicioDesejado.isAfter(fimExistente));
                        if (sobrepoe && !"ENCERRADO".equalsIgnoreCase(String.valueOf(c.getStatus()))) {
                            return true;
                        }
                    }
                }
            }
        } catch (RuntimeException e) {
            logger.error("Erro ao verificar conflito de datas: {}", e.getMessage());
        }
        return false;
    }

    @FXML
    public void carregarMeusAlugueis() {
        try {
            if (usuarioLogado == null || usuarioLogado.getId() == null) return;

            Map<String, ContratoAluguel> contratos = facade.consultarHistoricoCliente(usuarioLogado.getId());
            listaContratos.clear();
            if (contratos != null && !contratos.isEmpty()) {
                listaContratos.addAll(contratos.values());
            }

            atualizarPaginacaoAlugueis();

        } catch (RuntimeException e) {
            logger.error("Falha ao consultar histórico do cliente '{}': {}", usuarioLogado.getId(), e.getMessage(), e);
            exibirAlertaErro("Erro", "Erro ao carregar histórico de aluguéis: " + e.getMessage());
        }
    }

    @FXML
    public void carregarMultasPendentes() {
        try {
            if (usuarioLogado == null || usuarioLogado.getId() == null) return;

            String clienteId = usuarioLogado.getId();

            if (facade.possuiMultaPendente(clienteId)) {
                bannerMultas.setStyle("-fx-background-color: #fef2f2; -fx-border-color: #fecaca; -fx-border-radius: 8; -fx-background-radius: 8;");
                lblBannerMulta.setText("⚠️ Atenção: Você possui multas pendentes de pagamento. Regularize a situação.");
                lblBannerMulta.setStyle("-fx-text-fill: #991b1b; -fx-font-weight: bold;");
            } else {
                bannerMultas.setStyle("-fx-background-color: #f0fdf4; -fx-border-color: #bbf7d0; -fx-border-radius: 8; -fx-background-radius: 8;");
                lblBannerMulta.setText("✅ Você não possui multas pendentes no momento.");
                lblBannerMulta.setStyle("-fx-text-fill: #166534; -fx-font-weight: bold;");
            }

            Map<String, Multa> multas = facade.listarMultaPorCliente(clienteId);
            listaMultas.clear();
            if (multas != null && !multas.isEmpty()) {
                listaMultas.addAll(multas.values());
            }

            atualizarPaginacaoMultas();

        } catch (RuntimeException e) {
            logger.error("Falha ao consultar multas do cliente '{}': {}", usuarioLogado.getId(), e.getMessage(), e);
            exibirAlertaErro("Erro", "Erro ao carregar multas: " + e.getMessage());
        }
    }

    @FXML
    public void handleSair() {
        navigator.showLogin();
    }

    private static String formatarMoeda(BigDecimal valor) {
        return valor == null ? "R$ 0,00" : String.format("R$ %.2f", valor);
    }

    private static String formatarData(LocalDate data, String padrao) {
        return data == null ? padrao : data.format(FORMATO_DATA);
    }

    private void exibirAlertaErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}