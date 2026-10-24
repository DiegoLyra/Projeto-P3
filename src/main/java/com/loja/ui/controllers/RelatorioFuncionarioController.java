
package com.loja.ui.controllers;

import com.loja.padrao.facade.interfaces.ILojaFacade;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RelatorioFuncionarioController {

    @FXML
    private DatePicker dpFaturamentoInicio;

    @FXML
    private DatePicker dpFaturamentoFim;

    @FXML
    private VBox feedbackRelatorios;

    @FXML
    private TextArea txtFaturamento;

    private ILojaFacade facade;

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        dpFaturamentoInicio.setValue(
                LocalDate.now().withDayOfMonth(1)
        );

        dpFaturamentoFim.setValue(LocalDate.now());
    }

    public void setFacade(ILojaFacade facade) {
        this.facade = facade;
    }

    @FXML
    private void gerarFaturamento() {
        if (facade == null) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Erro",
                    "A fachada do sistema não foi configurada."
            );
            return;
        }

        LocalDate inicio = dpFaturamentoInicio.getValue();
        LocalDate fim = dpFaturamentoFim.getValue();

        if (inicio == null || fim == null) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Datas obrigatórias",
                    "Selecione a data inicial e a data final."
            );
            return;
        }

        if (inicio.isAfter(fim)) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Período inválido",
                    "A data inicial não pode ser posterior à data final."
            );
            return;
        }

        try {
            String relatorio =
                    facade.gerarRelatorioFaturamento(inicio, fim);

            if (relatorio == null || relatorio.isBlank()) {
                txtFaturamento.setText(
                        "Nenhum dado de faturamento foi retornado para o período."
                );
            } else {
                txtFaturamento.setText(
                        "FATURAMENTO POR PERÍODO\n"
                        + "Data inicial: " + inicio.format(FORMATO_DATA) + "\n"
                        + "Data final: " + fim.format(FORMATO_DATA) + "\n\n"
                        + relatorio
                );
            }

        } catch (RuntimeException e) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Erro ao gerar relatório",
                    "Não foi possível consultar o faturamento: "
                            + e.getMessage()
            );
        }
    }

    @FXML
    private void exportarPDF() {
        String conteudo = txtFaturamento.getText();

        if (conteudo == null || conteudo.isBlank()) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Relatório vazio",
                    "Gere o relatório de faturamento antes de exportar."
            );
            return;
        }

        FileChooser seletor = new FileChooser();
        seletor.setTitle("Salvar relatório de faturamento");
        seletor.setInitialFileName("relatorio-faturamento.pdf");

        seletor.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Arquivo PDF", "*.pdf")
        );

        Window janela = txtFaturamento.getScene().getWindow();
        File arquivo = seletor.showSaveDialog(janela);

        if (arquivo == null) {
            return;
        }

        if (!arquivo.getName().toLowerCase().endsWith(".pdf")) {
            arquivo = new File(arquivo.getAbsolutePath() + ".pdf");
        }

        try {
            criarPDF(arquivo, conteudo);

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "PDF exportado",
                    "Relatório salvo em:\n" + arquivo.getAbsolutePath()
            );

        } catch (IOException e) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Erro ao exportar PDF",
                    "Não foi possível salvar o arquivo: " + e.getMessage()
            );
        }
    }

    private void criarPDF(File arquivo, String conteudo)
            throws IOException {

        try (PDDocument documento = new PDDocument()) {
            PDType0Font fonte = carregarFonte(documento);

            float tamanhoFonte = 10;
            float margem = 50;
            float alturaLinha = 15;
            float larguraUtil = PDRectangle.A4.getWidth() - 2 * margem;

            PDPage pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);

            PDPageContentStream stream =
                    new PDPageContentStream(documento, pagina);

            float y = pagina.getMediaBox().getHeight() - margem;

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
                                documento, pagina
                        );

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
                "Nenhuma fonte compatível foi encontrada no computador."
        );
    }

    private String[] quebrarLinha(
            String linha,
            PDType0Font fonte,
            float tamanhoFonte,
            float larguraMaxima
    ) throws IOException {

        if (linha.isEmpty()) {
            return new String[]{""};
        }

        java.util.List<String> partes = new java.util.ArrayList<>();
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

    private void mostrarAlerta(
            Alert.AlertType tipo,
            String titulo,
            String mensagem
    ) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}
