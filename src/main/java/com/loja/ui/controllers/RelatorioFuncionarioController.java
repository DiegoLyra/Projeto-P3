package com.loja.ui.controllers;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import com.loja.model.ContratoAluguel;
import com.loja.padrao.facade.interfaces.ILojaFacade;
import com.loja.ui.relatorios.GeradorRelatorios;
import com.loja.ui.relatorios.JanelaRelatorios;
import com.loja.ui.relatorios.PainelRelatorio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class RelatorioFuncionarioController {

    @FXML private DatePicker dpFaturamentoInicio;
    @FXML private DatePicker dpFaturamentoFim;
    @FXML private VBox feedbackRelatorios;
    @FXML private PainelRelatorio painelFaturamento;

    @FXML private TableView<LinhaAluguel> tabelaAlugados;
    @FXML private TableColumn<LinhaAluguel, String> colContrato;
    @FXML private TableColumn<LinhaAluguel, String> colItem;
    @FXML private TableColumn<LinhaAluguel, String> colCliente;
    @FXML private TableColumn<LinhaAluguel, String> colRetirada;
    @FXML private TableColumn<LinhaAluguel, String> colDevolucao;
    @FXML private TableColumn<LinhaAluguel, String> colSituacao;
    @FXML private TableColumn<LinhaAluguel, String> colDiasAtraso;
    @FXML private TableColumn<LinhaAluguel, String> colValor;

    @FXML private Label lblTotalAlugados;
    @FXML private Label lblTotalAtrasados;

    private final ObservableList<LinhaAluguel> alugueis =
            FXCollections.observableArrayList();

    private ILojaFacade facade;
    
    private GeradorRelatorios relatorios;

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        dpFaturamentoInicio.setValue(
                LocalDate.now().withDayOfMonth(1));

        dpFaturamentoFim.setValue(LocalDate.now());

        colContrato.setCellValueFactory(
                new PropertyValueFactory<>("contrato"));
        colItem.setCellValueFactory(
                new PropertyValueFactory<>("item"));
        colCliente.setCellValueFactory(
                new PropertyValueFactory<>("cliente"));
        colRetirada.setCellValueFactory(
                new PropertyValueFactory<>("retirada"));
        colDevolucao.setCellValueFactory(
                new PropertyValueFactory<>("devolucao"));
        colSituacao.setCellValueFactory(
                new PropertyValueFactory<>("situacao"));
        colDiasAtraso.setCellValueFactory(
                new PropertyValueFactory<>("diasAtraso"));
        colValor.setCellValueFactory(
                new PropertyValueFactory<>("valor"));

        tabelaAlugados.setItems(alugueis);
    }

    public void setFacade(ILojaFacade facade) {
        this.facade = facade;
        this.relatorios = new GeradorRelatorios(facade);
        atualizarAlugados();
    }

    @FXML
    private void gerarFaturamento() {
        if (facade == null) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Erro",
                    "A fachada do sistema não foi configurada.");
            return;
        }

        LocalDate inicio = dpFaturamentoInicio.getValue();
        LocalDate fim = dpFaturamentoFim.getValue();

        if (inicio == null || fim == null) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Datas obrigatórias",
                    "Selecione a data inicial e a data final.");
            return;
        }

        if (inicio.isAfter(fim)) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Período inválido",
                    "A data inicial não pode ser posterior à data final.");
            return;
        }

        try {
            painelFaturamento.mostrar(relatorios.faturamento(
                    dpFaturamentoInicio.getValue(), dpFaturamentoFim.getValue()));
        } catch (RuntimeException e) {
            painelFaturamento.limpar();
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao gerar relatório", e.getMessage());
        }
    }

    @FXML
    private void atualizarAlugados() {
        if (facade == null) {
            return;
        }

        try {
            alugueis.clear();

            Map<String, ContratoAluguel> contratos =
                    facade.listarContratosAtivos();

            LocalDate hoje =
                    LocalDate.now(ZoneId.of("America/Recife"));

            int atrasados = 0;

            if (contratos != null) {
                for (ContratoAluguel contrato : contratos.values()) {

                    LocalDate prevista =
                            contrato.getDataPrevDevolucao();

                    boolean atrasado =
                            prevista != null && hoje.isAfter(prevista);

                    long dias = atrasado
                            ? ChronoUnit.DAYS.between(prevista, hoje)
                            : 0;

                    if (atrasado) {
                        atrasados++;
                    }

                    alugueis.add(new LinhaAluguel(
                            contrato.getId(),

                            contrato.getItem() == null
                                    ? "—"
                                    : contrato.getItem().getNome(),

                            contrato.getCliente() == null
                                    ? "—"
                                    : contrato.getCliente().getNome(),

                            formatarData(contrato.getDataRetirada()),
                            formatarData(prevista),

                            atrasado ? "Em atraso" : "Em dia",

                            String.valueOf(dias),

                            contrato.getValorTotal() == null
                                    ? "—"
                                    : "R$ " + contrato.getValorTotal()
                    ));
                }
            }

            lblTotalAlugados.setText(
                    "Total: " + alugueis.size());

            lblTotalAtrasados.setText(
                    "Em atraso: " + atrasados);

        } catch (RuntimeException e) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Erro ao atualizar",
                    "Não foi possível carregar os aluguéis: "
                            + e.getMessage());
        }
    }

        @FXML
    private void salvarAlugadosPDF() {
        if (semFachada()) {
            return;
        }
        try {
            JanelaRelatorios.salvarPdf(relatorios.itensAlugados(), tabelaAlugados.getScene().getWindow());
        } catch (RuntimeException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao gerar relatório", e.getMessage());
        }
    }

    @FXML
    private void imprimirAlugados() {
        if (semFachada()) {
            return;
        }
        try {
            JanelaRelatorios.imprimir(relatorios.itensAlugados());
        } catch (RuntimeException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao gerar relatório", e.getMessage());
        }
    }

    private File escolherArquivoPDF(
            String titulo,
            String nome,
            Window janela) {

        FileChooser seletor = new FileChooser();

        seletor.setTitle(titulo);
        seletor.setInitialFileName(nome);

        seletor.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Arquivo PDF", "*.pdf"));

        File arquivo = seletor.showSaveDialog(janela);

        if (arquivo != null
                && !arquivo.getName().toLowerCase().endsWith(".pdf")) {
            arquivo = new File(
                    arquivo.getAbsolutePath() + ".pdf");
        }

        return arquivo;
    }

    private void criarPDF(File arquivo, String conteudo)
            throws IOException {

        try (PDDocument documento = new PDDocument()) {
            PDType0Font fonte = carregarFonte(documento);

            float tamanhoFonte = 10;
            float margem = 50;
            float alturaLinha = 15;

            float larguraUtil =
                    PDRectangle.A4.getWidth() - 2 * margem;

            PDPage pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);

            PDPageContentStream stream =
                    new PDPageContentStream(documento, pagina);

            float y =
                    pagina.getMediaBox().getHeight() - margem;

            stream.beginText();
            stream.setFont(fonte, tamanhoFonte);
            stream.newLineAtOffset(margem, y);

            for (String linha : conteudo.split("\\R", -1)) {
                for (String parte : quebrarLinha(
                        linha, fonte, tamanhoFonte, larguraUtil)) {

                    if (y < margem) {
                        stream.endText();
                        stream.close();

                        pagina = new PDPage(PDRectangle.A4);
                        documento.addPage(pagina);

                        stream = new PDPageContentStream(
                                documento, pagina);

                        y = pagina.getMediaBox().getHeight() - margem;

                        stream.beginText();
                        stream.setFont(fonte, tamanhoFonte);
                        stream.newLineAtOffset(margem, y);
                    }

                    stream.showText(parte);
                    stream.newLineAtOffset(0, -alturaLinha);
                    y -= alturaLinha;
                }
            }

            stream.endText();
            stream.close();

            documento.save(arquivo);
        }
    }

    private PDType0Font carregarFonte(PDDocument documento)
            throws IOException {

        String[] caminhos = {
                "C:/Windows/Fonts/arial.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/usr/share/fonts/truetype/liberation2/LiberationSans-Regular.ttf",
                "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf"
        };

        for (String caminho : caminhos) {
            File arquivoFonte = new File(caminho);

            if (arquivoFonte.isFile()) {
                return PDType0Font.load(documento, arquivoFonte);
            }
        }

        throw new IOException(
                "Nenhuma fonte compatível foi encontrada no computador.");
    }

    private String[] quebrarLinha(
            String linha,
            PDType0Font fonte,
            float tamanhoFonte,
            float larguraMaxima) throws IOException {

        if (linha.isEmpty()) {
            return new String[]{""};
        }

        java.util.List<String> partes =
                new java.util.ArrayList<>();

        StringBuilder atual = new StringBuilder();

        for (String palavra : linha.split(" ")) {
            String candidata = atual.isEmpty()
                    ? palavra
                    : atual + " " + palavra;

            float largura = fonte.getStringWidth(candidata)
                    / 1000 * tamanhoFonte;

            if (largura > larguraMaxima && !atual.isEmpty()) {
                partes.add(atual.toString());
                atual = new StringBuilder(palavra);
            } else {
                atual = new StringBuilder(candidata);
            }
        }

        partes.add(atual.toString());

        return partes.toArray(new String[0]);
    }

    private String formatarData(LocalDate data) {
        return data == null
                ? "—"
                : data.format(FORMATO_DATA);
    }

    private boolean semFachada() {
        if (relatorios == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "A fachada do sistema não foi configurada.");
            return true;
        }
        return false;
    }

    private void mostrarAlerta(
            Alert.AlertType tipo,
            String titulo,
            String mensagem) {

        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    public static class LinhaAluguel {

        private final String contrato;
        private final String item;
        private final String cliente;
        private final String retirada;
        private final String devolucao;
        private final String situacao;
        private final String diasAtraso;
        private final String valor;

        public LinhaAluguel(
                String contrato,
                String item,
                String cliente,
                String retirada,
                String devolucao,
                String situacao,
                String diasAtraso,
                String valor) {

            this.contrato = contrato;
            this.item = item;
            this.cliente = cliente;
            this.retirada = retirada;
            this.devolucao = devolucao;
            this.situacao = situacao;
            this.diasAtraso = diasAtraso;
            this.valor = valor;
        }

        public String getContrato() { return contrato; }
        public String getItem() { return item; }
        public String getCliente() { return cliente; }
        public String getRetirada() { return retirada; }
        public String getDevolucao() { return devolucao; }
        public String getSituacao() { return situacao; }
        public String getDiasAtraso() { return diasAtraso; }
        public String getValor() { return valor; }

        @Override
        public String toString() {
            return contrato + " | "
                    + item + " | "
                    + cliente + " | "
                    + retirada + " | "
                    + devolucao + " | "
                    + situacao + " | "
                    + diasAtraso + " | "
                    + valor;
        }
    }
}