package com.loja.ui.controllers;

import com.loja.model.Cliente;
import com.loja.model.ContratoAluguel;
import com.loja.model.Item;
import com.loja.model.Usuario;
import com.loja.padrao.facade.interfaces.ILojaFacade;
import com.loja.ui.navigation.Navigator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;

public class AluguelController {
    private static final ZoneId FUSO = ZoneId.of("America/Recife");

    @FXML private ComboBox<Cliente> clienteComboBox;
    @FXML private ComboBox<Item> itemComboBox;
    @FXML private DatePicker dataRetiradaPicker;
    @FXML private DatePicker dataDevolucaoPicker;
    @FXML private Label valorDiariaLabel;
    @FXML private Label quantidadeDiasLabel;
    @FXML private Label valorTotalLabel;
    @FXML private Label mensagemLabel;
    @FXML private Button registrarAluguelButton;

    private final ObservableList<Cliente> clientes = FXCollections.observableArrayList();
    private final ObservableList<Item> itens = FXCollections.observableArrayList();
    private final ObservableList<ContratoAluguel> alugados = FXCollections.observableArrayList();

    private final ILojaFacade facade;
    private final Navigator navigator;

    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public AluguelController(ILojaFacade facade, Navigator navigator) {
        this.facade = facade;
        this.navigator = navigator;
    }

    @FXML
    public void initialize() {
        clienteComboBox.setItems(clientes);
        itemComboBox.setItems(itens);

        // Configuração de exibição para Cliente
        clienteComboBox.setCellFactory(lv -> new ListCell<Cliente>() {
            @Override
            protected void updateItem(Cliente cliente, boolean empty) {
                super.updateItem(cliente, empty);
                setText((empty || cliente == null || cliente.getNome() == null) ? "" : cliente.getNome());
            }
        });
        clienteComboBox.setButtonCell(new ListCell<Cliente>() {
            @Override
            protected void updateItem(Cliente cliente, boolean empty) {
                super.updateItem(cliente, empty);
                setText((empty || cliente == null || cliente.getNome() == null) ? "" : cliente.getNome());
            }
        });

        // Configuração de exibição para Item
        itemComboBox.setCellFactory(lv -> new ListCell<Item>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null || item.getNome() == null) ? "" : item.getNome());
            }
        });
        itemComboBox.setButtonCell(new ListCell<Item>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null || item.getNome() == null) ? "" : item.getNome());
            }
        });

        // Configura restrições visuais nos DatePickers
        configurarRestricoesDeDatas();

        // Listeners
        dataRetiradaPicker.valueProperty().addListener((obs, antiga, nova) -> {
            atualizarRestricaoDevolucao();
            calcularTotal();
        });
        dataDevolucaoPicker.valueProperty().addListener((obs, antiga, nova) -> calcularTotal());
        itemComboBox.valueProperty().addListener((obs, antigo, novo) -> calcularTotal());

        carregarDados();
    }

    private void configurarRestricoesDeDatas() {
        dataRetiradaPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now(FUSO))) {
                    setDisable(true);
                    setStyle("-fx-background-color: #fee2e2;");
                }
            }
        });
    }

    private void atualizarRestricaoDevolucao() {
        LocalDate retirada = dataRetiradaPicker.getValue();
        dataDevolucaoPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (retirada != null && (date.isBefore(retirada) || date.isEqual(retirada))) {
                    setDisable(true);
                    setStyle("-fx-background-color: #fee2e2;");
                }
            }
        });

        if (dataDevolucaoPicker.getValue() != null && retirada != null && !dataDevolucaoPicker.getValue().isAfter(retirada)) {
            dataDevolucaoPicker.setValue(null);
        }
    }

    private void carregarDados() {
        try {
            clientes.clear();
            itens.clear();

            Map<String, Usuario> usuariosMap = facade.listarUsuario();
            if (usuariosMap != null) {
                for (Usuario usuario : usuariosMap.values()) {
                    if (usuario instanceof Cliente cliente) {
                        clientes.add(cliente);
                    }
                }
            }

            Map<String, Item> itensMap = facade.listarItensDisponiveis();
            if (itensMap != null) {
                itens.addAll(itensMap.values());
            }

            if (clientes.isEmpty()) {
                mensagemLabel.setText("Nenhum cliente encontrado.");
            } else if (itens.isEmpty()) {
                mensagemLabel.setText("Nenhum item disponível para aluguel.");
            } else {
                mensagemLabel.setText("");
            }

        } catch (Exception e) {
            e.printStackTrace();
            mensagemLabel.setText("Erro ao carregar dados: " + e.getMessage());
        }
    }

    private void calcularTotal() {
        Item item = itemComboBox.getValue();
        LocalDate retirada = dataRetiradaPicker.getValue();
        LocalDate devolucao = dataDevolucaoPicker.getValue();

        double diaria = 0;
        long dias = 0;
        double total = 0;

        if (item != null) {
            diaria = Double.parseDouble(String.valueOf(item.getTaxaDiaria()));
        }

        if (retirada != null && devolucao != null && devolucao.isAfter(retirada)) {
            dias = ChronoUnit.DAYS.between(retirada, devolucao);
            total = diaria * dias;
        }

        valorDiariaLabel.setText(moeda.format(diaria));
        quantidadeDiasLabel.setText(dias + " dia(s)");
        valorTotalLabel.setText(moeda.format(total));
    }

    @FXML
    private void handleRegistrarAluguel() {
        Cliente cliente = clienteComboBox.getValue();
        Item item = itemComboBox.getValue();
        LocalDate retirada = dataRetiradaPicker.getValue();
        LocalDate devolucao = dataDevolucaoPicker.getValue();

        if (cliente == null || item == null || retirada == null || devolucao == null) {
            mensagemLabel.setStyle("-fx-text-fill: #b91c1c;");
            mensagemLabel.setText("Selecione o cliente, o item e as duas datas.");
            return;
        }

        if (retirada.isBefore(LocalDate.now(FUSO))) {
            mensagemLabel.setStyle("-fx-text-fill: #b91c1c;");
            mensagemLabel.setText("A data de retirada não pode ser no passado.");
            return;
        }

        if (!devolucao.isAfter(retirada)) {
            mensagemLabel.setStyle("-fx-text-fill: #b91c1c;");
            mensagemLabel.setText("A data de devolução deve ser posterior à data de retirada (mínimo 1 dia).");
            return;
        }

        try {
            ContratoAluguel contrato = facade.registrarAluguel(
                    cliente.getId(),
                    item.getId(),
                    retirada,
                    devolucao
            );

            alugados.add(contrato);

            mensagemLabel.setStyle("-fx-text-fill: #15803d;");
            mensagemLabel.setText("Aluguel registrado! Contrato: " + contrato.getId());

            carregarDados();
            limparFormulario();

        } catch (Exception e) {
            e.printStackTrace();
            mensagemLabel.setStyle("-fx-text-fill: #b91c1c;");
            mensagemLabel.setText("Não foi possível registrar: " + e.getMessage());
        }
    }

    private void limparFormulario() {
        clienteComboBox.getSelectionModel().clearSelection();
        itemComboBox.getSelectionModel().clearSelection();
        dataRetiradaPicker.setValue(null);
        dataDevolucaoPicker.setValue(null);
        calcularTotal();
    }
}