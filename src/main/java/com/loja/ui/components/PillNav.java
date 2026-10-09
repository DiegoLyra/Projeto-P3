package com.loja.ui.components;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class PillNav extends StackPane {

    private static final Duration DURACAO = Duration.millis(260);
    private static final String COR_INDICADOR = "#0b8ee8";

    private final HBox tabs = new HBox(8);
    private final Region indicador = new Region();
    private final List<Button> botoes = new ArrayList<>();
    private final List<Runnable> acoes = new ArrayList<>();

    private final DoubleProperty indX = new SimpleDoubleProperty();
    private final DoubleProperty indW = new SimpleDoubleProperty();

    private Button ativo;
    private Timeline animacao;

    public PillNav() {
        super();
        setPadding(new Insets(6));
        setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 12px; -fx-border-color: #e5e7eb; -fx-border-radius: 12px;");

        indicador.setManaged(false);
        indicador.setMouseTransparent(true);
        indicador.setStyle("-fx-background-color: " + COR_INDICADOR + "; -fx-background-radius: 8px; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.12), 4, 0, 0, 1);");

        getChildren().addAll(indicador, tabs);

        indX.addListener((obs, antigo, novo) -> posicionarIndicador());
        indW.addListener((obs, antigo, novo) -> posicionarIndicador());
        tabs.layoutXProperty().addListener((obs, antigo, novo) -> encaixar());
    }

    public Button addTab(String text, boolean active, Runnable onClick) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btn, Priority.ALWAYS);

        botoes.add(btn);
        acoes.add(onClick);

        if (active) {
            ativo = btn;
        }
        estilizar(btn, active);

        btn.setOnAction(e -> selecionar(btn, true));
        btn.layoutXProperty().addListener((obs, antigo, novo) -> encaixar());
        btn.widthProperty().addListener((obs, antigo, novo) -> encaixar());
        btn.heightProperty().addListener((obs, antigo, novo) -> encaixar());

        tabs.getChildren().add(btn);
        return btn;
    }

    public void selectTab(int index) {
        selecionar(botoes.get(index), true);
    }

    private void selecionar(Button btn, boolean disparar) {
        if (btn == ativo) {
            return;
        }
        Button anterior = ativo;
        ativo = btn;

        if (anterior != null) {
            estilizar(anterior, false);
        }
        estilizar(btn, true);
        animarIndicador();

        Runnable acao = acoes.get(botoes.indexOf(btn));
        if (disparar && acao != null) {
            acao.run();
        }
    }

    private void animarIndicador() {
        if (animacao != null) {
            animacao.stop();
        }
        double x = tabs.getLayoutX() + ativo.getLayoutX();
        double w = ativo.getWidth();

        animacao = new Timeline(new KeyFrame(DURACAO,
                new KeyValue(indX, x, Interpolator.EASE_BOTH),
                new KeyValue(indW, w, Interpolator.EASE_BOTH)));
        animacao.setOnFinished(e -> animacao = null);
        animacao.play();
    }

    private void encaixar() {
        if (ativo == null || animacao != null) {
            return;
        }
        indX.set(tabs.getLayoutX() + ativo.getLayoutX());
        indW.set(ativo.getWidth());
        posicionarIndicador();
    }

    private void posicionarIndicador() {
        if (ativo == null) {
            return;
        }
        double y = tabs.getLayoutY() + ativo.getLayoutY();
        indicador.resizeRelocate(indX.get(), y, indW.get(), ativo.getHeight());
    }

    private void estilizar(Button btn, boolean active) {
        btn.setStyle(
                "-fx-font-size: 12px; " +
                        "-fx-font-weight: 600; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 8px 14px; " +
                        "-fx-background-color: transparent; " +
                        "-fx-text-fill: " + (active ? "white" : "#4b5563") + ";");
    }
}