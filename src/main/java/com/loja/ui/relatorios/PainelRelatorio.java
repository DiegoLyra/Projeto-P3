package com.loja.ui.relatorios;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Mostra um relatório dentro da própria tela, com os botões "Salvar PDF" e "Imprimir" logo abaixo.
 * Pode ser usado direto no FXML: <PainelRelatorio fx:id="..." />
 * Os botões ficam desativados até algum relatório ser mostrado.
 */
public class PainelRelatorio extends VBox {

    private static final String ESTILO_BOTAO =
            "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 18; -fx-cursor: hand; ";

    private final TextArea area = new TextArea();
    private final Button botaoPdf = new Button("Salvar PDF");
    private final Button botaoImprimir = new Button("Imprimir");
    private Relatorio relatorio;

    public PainelRelatorio() {
        super(10);

        area.setEditable(false);
        area.setWrapText(false);
        area.setPrefHeight(260);
        area.setPromptText("O relatório aparecerá aqui.");
        area.setStyle("-fx-font-family: monospace; -fx-font-size: 13px;");
        VBox.setVgrow(area, Priority.ALWAYS);

        botaoPdf.setStyle(ESTILO_BOTAO + "-fx-background-color: #059669; -fx-text-fill: white;");
        botaoPdf.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent evento) {
                if (relatorio != null) {
                    JanelaRelatorios.salvarPdf(relatorio, getScene().getWindow());
                }
            }
        });

        botaoImprimir.setStyle(ESTILO_BOTAO + "-fx-background-color: #eef2ff; -fx-text-fill: #4338ca;");
        botaoImprimir.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent evento) {
                if (relatorio != null) {
                    JanelaRelatorios.imprimir(relatorio);
                }
            }
        });

        HBox botoes = new HBox(8, botaoPdf, botaoImprimir);
        botoes.setAlignment(Pos.CENTER_RIGHT);

        getChildren().addAll(area, botoes);
        habilitarBotoes(false);
    }

    /** Mostra o relatório no painel e libera os botões de PDF e impressão. */
    public void mostrar(Relatorio novoRelatorio) {
        this.relatorio = novoRelatorio;
        area.setText(novoRelatorio.comoTexto());
        habilitarBotoes(true);
    }

    /** Volta ao estado inicial (vazio, botões desativados). */
    public void limpar() {
        this.relatorio = null;
        area.clear();
        habilitarBotoes(false);
    }

    private void habilitarBotoes(boolean habilitar) {
        botaoPdf.setDisable(!habilitar);
        botaoImprimir.setDisable(!habilitar);
    }
}