package com.loja.ui.controllers;

import com.loja.model.Cliente;
import com.loja.model.ContratoAluguel;
import com.loja.model.Item;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;

public class AluguelController implements Initializable {

    // --- Componentes FXML ---
    @FXML private ComboBox<Cliente> clienteComboBox;
    @FXML private ComboBox<Item> itemComboBox;
    @FXML private DatePicker dataRetiradaPicker;
    @FXML private DatePicker dataDevolucaoPicker;

    @FXML private Label valorDiariaLabel;
    @FXML private Label quantidadeDiasLabel;
    @FXML private Label valorTotalLabel;
    @FXML private Label mensagemLabel;

    @FXML private Button registrarAluguelButton;

    // Formatador para Real (R$)
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarFormatacaoComboBoxes();
        configurarListenersCalculo();
    }

    /**
     * Formatação visual dos itens dentro do ComboBox.
     */
    private void configurarFormatacaoComboBoxes() {
        clienteComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Cliente cliente) {
                return (cliente != null) ? cliente.getNome() : "";
            }

            @Override
            public Cliente fromString(String string) {
                return null;
            }
        });

        itemComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Item item) {
                if (item == null) return "";
                BigDecimal taxa = item.getTaxaDiaria() != null ? item.getTaxaDiaria() : BigDecimal.ZERO;
                return item.getNome() + " - " + currencyFormat.format(taxa) + "/dia";
            }

            @Override
            public Item fromString(String string) {
                return null;
            }
        });
    }

    /**
     * Adiciona ouvintes para recalcular os valores em tempo real ao alterar qualquer campo.
     */
    private void configurarListenersCalculo() {
        clienteComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validarClienteEAtualizar());
        itemComboBox.valueProperty().addListener((obs, oldVal, newVal) -> calcularEAtualizarPrevia());
        dataRetiradaPicker.valueProperty().addListener((obs, oldVal, newVal) -> calcularEAtualizarPrevia());
        dataDevolucaoPicker.valueProperty().addListener((obs, oldVal, newVal) -> calcularEAtualizarPrevia());
    }

    private void validarClienteEAtualizar() {
        limparMensagem();
        Cliente cliente = clienteComboBox.getValue();
        if (cliente != null && cliente.isInadimplente()) {
            exibirErro("Cliente selecionado possui pendências (inadimplente).");
        }
    }

    /**
     * Calcula e exibe na tela o valor da diária, a quantidade de dias e o total do aluguel.
     */
    private void calcularEAtualizarPrevia() {
        limparMensagem();

        Cliente cliente = clienteComboBox.getValue();
        if (cliente != null && cliente.isInadimplente()) {
            exibirErro("Cliente selecionado possui pendências (inadimplente).");
        }

        Item itemSelecionado = itemComboBox.getValue();
        LocalDate retirada = dataRetiradaPicker.getValue();
        LocalDate devolucao = dataDevolucaoPicker.getValue();

        // Atualiza a exibição da taxa diária usando item.getTaxaDiaria()
        if (itemSelecionado == null || itemSelecionado.getTaxaDiaria() == null) {
            valorDiariaLabel.setText("R$ 0,00");
        } else {
            valorDiariaLabel.setText(currencyFormat.format(itemSelecionado.getTaxaDiaria()));
        }

        if (retirada == null || devolucao == null || itemSelecionado == null) {
            quantidadeDiasLabel.setText("0 dia(s)");
            valorTotalLabel.setText("R$ 0,00");
            return;
        }

        if (devolucao.isBefore(retirada)) {
            exibirErro("A data de devolução não pode ser anterior à data de retirada.");
            quantidadeDiasLabel.setText("0 dia(s)");
            valorTotalLabel.setText("R$ 0,00");
            return;
        }

        long dias = ChronoUnit.DAYS.between(retirada, devolucao);
        if (dias == 0) {
            dias = 1; // Mínimo de 1 diária caso devolução no mesmo dia
        }

        BigDecimal total = itemSelecionado.getTaxaDiaria().multiply(BigDecimal.valueOf(dias));

        quantidadeDiasLabel.setText(dias + " dia(s)");
        valorTotalLabel.setText(currencyFormat.format(total));
    }

    /**
     * Ação disparada pelo botão "Firmar Contrato de Aluguel".
     */
    @FXML
    private void handleRegistrarAluguel(ActionEvent event) {
        limparMensagem();

        Cliente cliente = clienteComboBox.getValue();
        Item item = itemComboBox.getValue();
        LocalDate retirada = dataRetiradaPicker.getValue();
        LocalDate devolucao = dataDevolucaoPicker.getValue();

        // Validações de obrigatoriedade
        if (cliente == null) {
            exibirErro("Por favor, selecione um cliente.");
            return;
        }
        if (cliente.isInadimplente()) {
            exibirErro("Não é possível registrar aluguel para cliente inadimplente.");
            return;
        }
        if (item == null) {
            exibirErro("Por favor, selecione um item.");
            return;
        }
        if (item.getStatus() != null && !item.getStatus().equalsIgnoreCase("DISPONIVEL")) {
            exibirErro("O item selecionado não está disponível no momento.");
            return;
        }
        if (retirada == null) {
            exibirErro("Por favor, selecione a data de retirada.");
            return;
        }
        if (devolucao == null) {
            exibirErro("Por favor, selecione a previsão de devolução.");
            return;
        }
        if (devolucao.isBefore(retirada)) {
            exibirErro("A data de devolução deve ser igual ou posterior à data de retirada.");
            return;
        }

        long dias = ChronoUnit.DAYS.between(retirada, devolucao);
        if (dias == 0) dias = 1;

        // Cálculo usando getTaxaDiaria()
        BigDecimal valorTotal = item.getTaxaDiaria().multiply(BigDecimal.valueOf(dias));

        // Construção do objeto ContratoAluguel usando o Builder da sua classe
        ContratoAluguel contrato = new ContratoAluguel.Builder()
                .id(UUID.randomUUID().toString())
                .cliente(cliente)
                .item(item)
                .dataRetirada(retirada)
                .dataPrevDevolucao(devolucao)
                .valorTotal(valorTotal)
                .status("ATIVO")
                .build();

        // --- LÓGICA DE SALVAMENTO / PERSISTÊNCIA ---
        // Exemplo: contratoService.salvar(contrato);

        exibirSucesso("Contrato de aluguel registrado com sucesso!");
        limparFormulario();
    }

    private void exibirErro(String mensagem) {
        mensagemLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #b91c1c;");
        mensagemLabel.setText(mensagem);
    }

    private void exibirSucesso(String mensagem) {
        mensagemLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #059669;");
        mensagemLabel.setText(mensagem);
    }

    private void limparMensagem() {
        mensagemLabel.setText("");
    }

    private void limparFormulario() {
        clienteComboBox.setValue(null);
        itemComboBox.setValue(null);
        dataRetiradaPicker.setValue(null);
        dataDevolucaoPicker.setValue(null);
        valorDiariaLabel.setText("R$ 0,00");
        quantidadeDiasLabel.setText("0 dia(s)");
        valorTotalLabel.setText("R$ 0,00");
    }
}