package com.loja.ui.components;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UiComponentTest extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Teste de Componentes - UI JavaFX");

        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f3f4f6;");

        // 1. Testando PageHeader
        PageHeader header = new PageHeader("Painel de Controle", "Gerenciamento e Locações");

        // 2. Testando PillNav (Abas)
        PillNav nav = new PillNav();
        nav.addTab("Visão Geral", true, () -> System.out.println("Aba Visão Geral selecionada"));
        nav.addTab("Itens", false, () -> System.out.println("Aba Itens selecionada"));

        // 3. Testando Banner de Alerta
        Banner banner = new Banner("Atenção: Existem pendências financeiras registradas.", Banner.BannerType.WARNING);

        // 4. Testando FormField com TextField nativo
        TextField txtNome = new TextField();
        FormField formField = new FormField("Nome do Cliente", txtNome);

        // 5. Testando StatusBadge
        StatusBadge badge = new StatusBadge("Ativo", StatusBadge.StatusType.SUCCESS);

        // 6. Testando ItemCard
        ItemCard itemCard = new ItemCard("001", "Furadeira de Impacto Profissional", 45.00, () -> {
            System.out.println("Botão do ItemCard acionado!");
        });

        // 7. Testando EmptyState
        EmptyState emptyState = new EmptyState("Nenhum histórico recente encontrado.");

        // Adicionando tudo ao container principal
        root.getChildren().addAll(
                header,
                nav,
                banner,
                formField,
                badge,
                itemCard,
                emptyState
        );

        Scene scene = new Scene(root, 500, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}