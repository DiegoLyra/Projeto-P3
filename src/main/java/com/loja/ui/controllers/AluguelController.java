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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.util.StringConverter;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;

public class AluguelController {

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

        // Converter para Cliente (Botão e Células da Lista)
        StringConverter<Cliente> clienteConverter = new StringConverter<>() {
            @Override
            public String toString(Cliente cliente) {
                return (cliente == null || cliente.getNome() == null) ? "" : cliente.getNome();
            }

            @Override
            public Cliente fromString(String texto) {
                return null;
            }
        };
        clienteComboBox.setConverter(clienteConverter);
        clienteComboBox.setCellFactory(ComboBoxListCell.forListView(clienteConverter));

        // Converter para Item (Botão e Células da Lista)
        StringConverter<Item> itemConverter = new StringConverter<>() {
            @Override
            public String toString(Item item) {
                return (item == null || item.getNome() == null) ? "" : item.getNome();
            }

            @Override
            public Item fromString(String texto) {
                return null;
            }
        };
        itemComboBox.setConverter(itemConverter);
        itemComboBox.setCellFactory(ComboBoxListCell.forListView(itemConverter));

        // Listeners
        dataRetiradaPicker.valueProperty().addListener((obs, antiga, nova) -> calcularTotal());
        dataDevolucaoPicker.valueProperty().addListener((obs, antiga, nova) -> calcularTotal());
        itemComboBox.valueProperty().addListener((obs, antigo, novo) -> calcularTotal());

        carregarDados();
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

            System.out.println("Clientes carregados: " + clientes.size());
            System.out.println("Itens disponíveis: " + itens.size());

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

        if (retirada != null && devolucao != null && !devolucao.isBefore(retirada)) {
            dias = ChronoUnit.DAYS.between(retirada, devolucao);
            dias = Math.max(1, dias);
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

        if (devolucao.isBefore(retirada)) {
            mensagemLabel.setStyle("-fx-text-fill: #b91c1c;");
            mensagemLabel.setText("A devolução não pode ser anterior à retirada.");
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