package com.loja.ui.relatorios;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javax.print.PrintServiceLookup;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Janela de pré-visualização de um relatório, com os botões "Salvar PDF" e "Imprimir".
 * Usada pelas telas de Admin e de Funcionário.
 */

@SuppressWarnings({"java:S9357", "java:S1604"})

public final class JanelaRelatorios {

    private static final Logger logger = LoggerFactory.getLogger(JanelaRelatorios.class);

    private JanelaRelatorios() {
    }

        public static void mostrar(final Relatorio relatorio) {
        TextArea area = new TextArea(relatorio.comoTexto());
        area.setEditable(false);
        area.setWrapText(false);
        area.setStyle("-fx-font-family: monospace; -fx-font-size: 14px;");

        final Stage janela = new Stage();
        janela.initModality(Modality.APPLICATION_MODAL); // bloqueia a tela de trás, como o Dialog fazia
        janela.setTitle(relatorio.getTitulo());

        Label titulo = new Label(relatorio.getTitulo());
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        titulo.setPadding(new Insets(12, 12, 8, 12));

        Button botaoPdf = new Button("Salvar PDF");
        botaoPdf.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent evento) {
                salvarPdf(relatorio, janela);
            }
        });

        Button botaoImprimir = new Button("Imprimir");
        botaoImprimir.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent evento) {
                imprimir(relatorio);
            }
        });

        Button botaoFechar = new Button("Fechar");
        botaoFechar.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent evento) {
                janela.close();
            }
        });

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        HBox botoes = new HBox(8, botaoPdf, botaoImprimir, espaco, botaoFechar);
        botoes.setPadding(new Insets(10, 12, 12, 12));

        BorderPane raiz = new BorderPane();
        raiz.setTop(titulo);
        raiz.setCenter(area);
        raiz.setBottom(botoes);
        BorderPane.setMargin(area, new Insets(0, 12, 0, 12));

        // Tamanho definido na própria Scene: é respeitado também no Linux
        Rectangle2D tela = Screen.getPrimary().getVisualBounds();
        double largura = Math.min(1100, tela.getWidth() * 0.8);
        double altura = Math.min(760, tela.getHeight() * 0.8);
        janela.setScene(new Scene(raiz, largura, altura));
        janela.setMinWidth(600);
        janela.setMinHeight(400);
        janela.showAndWait();
    }

    public static void salvarPdf(Relatorio relatorio, Window dono) {
        FileChooser seletor = new FileChooser();
        seletor.setTitle("Salvar relatório em PDF");
        seletor.setInitialFileName(relatorio.getNomeArquivo() + "-" + GeradorRelatorios.hoje() + ".pdf");
        seletor.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo PDF (*.pdf)", "*.pdf"));

        File arquivo = seletor.showSaveDialog(dono);
        if (arquivo == null) {
            return; // o usuário cancelou
        }
        if (!arquivo.getName().toLowerCase().endsWith(".pdf")) {
            arquivo = new File(arquivo.getParentFile(), arquivo.getName() + ".pdf");
        }

        try {
            RelatorioPDF.gerar(relatorio, arquivo);
            mensagem(Alert.AlertType.INFORMATION, "Relatório salvo", "PDF salvo em:\n" + arquivo.getAbsolutePath());
        } catch (IOException | RuntimeException e) {
            logger.error("Falha ao salvar PDF do relatório: {}", e.getMessage(), e);
            mensagem(Alert.AlertType.ERROR, "Erro",
                    "Não foi possível salvar o PDF. Verifique se o arquivo não está aberto em outro programa.");
        }
    }

    // Gera o PDF e abre a janela de impressão do sistema (escolha de impressora, cópias, páginas)
    public static void imprimir(final Relatorio relatorio) {
        // Sem impressora instalada, não adianta abrir a janela de impressão
        if (PrintServiceLookup.lookupPrintServices(null, null).length == 0) {
            mensagem(Alert.AlertType.INFORMATION, "Impressão",
                    "Nenhuma impressora encontrada neste computador.\nUse \"Salvar PDF\" para guardar o relatório.");
            return;
        }
        final File temporario;
        try {
            java.nio.file.Path pastaTemporaria = Files.createTempDirectory("relatorios-");

            temporario = Files.createTempFile(
                    pastaTemporaria,
                    relatorio.getNomeArquivo() + "-",
                    ".pdf"
            ).toFile();

            pastaTemporaria.toFile().deleteOnExit();
            temporario.deleteOnExit();

            RelatorioPDF.gerar(relatorio, temporario);

        } catch (IOException | RuntimeException e) {
            logger.error("Falha ao preparar impressão do relatório: {}", e.getMessage(), e);
            mensagem(Alert.AlertType.ERROR, "Erro", "Não foi possível preparar a impressão.");
            return;
        }

        // A janela de impressão é do AWT: roda em outra thread para não travar a tela do JavaFX
        Thread impressao = new Thread(new Runnable() {
            @Override
            public void run() {
                try (PDDocument pdf = Loader.loadPDF(temporario)) {
                    PrinterJob trabalho = PrinterJob.getPrinterJob();
                    trabalho.setJobName(relatorio.getTitulo());
                    trabalho.setPageable(new PDFPageable(pdf));

                    if (trabalho.printDialog()) {
                        trabalho.print();
                    }
                } catch (IOException | PrinterException | RuntimeException e) {
                    logger.error("Falha ao imprimir o relatório: {}", e.getMessage(), e);
                    avisarErro("Não foi possível imprimir. Verifique se há uma impressora configurada no computador.");
                }
            }
        });
        impressao.start();
    }

    // Chamado de outra thread: o alerta precisa ser mostrado na thread do JavaFX
    private static void avisarErro(final String texto) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                mensagem(Alert.AlertType.ERROR, "Impressão", texto);
            }
        });
    }

    private static void mensagem(Alert.AlertType tipo, String titulo, String texto) {
        Alert alerta = new Alert(tipo, texto);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}