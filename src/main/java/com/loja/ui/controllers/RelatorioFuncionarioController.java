package com.loja.ui.controllers;

import com.loja.model.ContratoAluguel;
import com.loja.model.Item;
import com.loja.padrao.facade.interfaces.ILojaFacade;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;

import java.util.Map;
import java.util.Optional;

public class RelatorioFuncionarioController {

    @FXML
    private TextArea txtRelatorio;

    private ILojaFacade facade;

    public void setFacade(ILojaFacade facade) {
        this.facade = facade;
    }

    @FXML
    private void gerarDisponiveis() {
        if (!verificarFacade()) {
            return;
        }

        try {
            Map<String, Item> itens = facade.listarItensDisponiveis();

            if (itens.isEmpty()) {
                txtRelatorio.setText(
                    "Não há itens disponíveis para aluguel no momento."
                );
                return;
            }

            StringBuilder texto = new StringBuilder();
            texto.append("RELATÓRIO DE ITENS DISPONÍVEIS\n\n");

            for (Item item : itens.values()) {
                texto.append("ID: ")
                    .append(item.getId())
                    .append("\nNome: ")
                    .append(item.getNome())
                    .append("\nValor diário: R$ ")
                    .append(item.getTaxaDiaria())
                    .append("\n\n");
            }

            txtRelatorio.setText(texto.toString());

        } catch (RuntimeException e) {
            mostrarErro("Erro ao listar itens: " + e.getMessage());
        }
    }

    @FXML
    private void gerarContratosAtivos() {
        if (!verificarFacade()) {
            return;
        }

        try {
            String relatorio = facade.gerarRelatorioItensAlugados();
            txtRelatorio.setText(relatorio);

        } catch (RuntimeException e) {
            mostrarErro("Erro ao gerar relatório: " + e.getMessage());
        }
    }

    @FXML
    private void gerarHistoricoCliente() {
        if (!verificarFacade()) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Histórico do cliente");
        dialog.setHeaderText("Consultar contratos do cliente");
        dialog.setContentText("ID do cliente:");

        Optional<String> resultado = dialog.showAndWait();

        resultado.ifPresent(id -> {
            if (id.trim().isEmpty()) {
                mostrarErro("Informe o ID do cliente.");
                return;
            }

            try {
                Map<String, ContratoAluguel> contratos =
                    facade.consultarHistoricoCliente(id.trim());

                if (contratos.isEmpty()) {
                    txtRelatorio.setText(
                        "Não há histórico de contratos para esse cliente."
                    );
                    return;
                }

                StringBuilder texto = new StringBuilder();
                texto.append("HISTÓRICO DE CONTRATOS DO CLIENTE\n\n");

                for (ContratoAluguel contrato : contratos.values()) {
                    texto.append("Contrato: ")
                        .append(contrato.getId())
                        .append("\nItem: ")
                        .append(contrato.getItem().getNome())
                        .append("\nValor total: R$ ")
                        .append(contrato.getValorTotal())
                        .append("\nStatus: ")
                        .append(contrato.getStatus())
                        .append("\nDevolução prevista: ")
                        .append(contrato.getDataPrevDevolucao())
                        .append("\n\n");
                }

                txtRelatorio.setText(texto.toString());

            } catch (RuntimeException e) {
                mostrarErro(
                    "Erro ao consultar histórico: " + e.getMessage()
                );
            }
        });
    }

    private boolean verificarFacade() {
        if (facade == null) {
            mostrarErro(
                "A fachada da loja não foi configurada."
            );
            return false;
        }

        return true;
    }

    private void mostrarErro(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Erro");
        alerta.setHeaderText("Não foi possível gerar o relatório");
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}
