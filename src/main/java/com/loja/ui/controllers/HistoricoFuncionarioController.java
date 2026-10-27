package com.loja.ui.controllers;

import com.loja.model.ContratoAluguel;
import com.loja.padrao.facade.interfaces.ILojaFacade;
import com.loja.model.Usuario;
import com.loja.ui.components.ColunaTexto;
import com.loja.ui.relatorios.GeradorRelatorios;
import com.loja.ui.relatorios.PainelRelatorio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class HistoricoFuncionarioController {

    private static final Logger logger = LoggerFactory.getLogger(HistoricoFuncionarioController.class);

    @FXML private TextField txtClienteId;
    @FXML private TableView<ContratoAluguel> tabelaHistorico;
    @FXML private TableColumn<ContratoAluguel, String> colContratoId;
    @FXML private TableColumn<ContratoAluguel, String> colItem;
    @FXML private TableColumn<ContratoAluguel, String> colDataRetirada;
    @FXML private TableColumn<ContratoAluguel, String> colDataDevolucao;
    @FXML private TableColumn<ContratoAluguel, String> colValorPago;
    @FXML private TableColumn<ContratoAluguel, String> colStatus;
    @FXML private PainelRelatorio painelHistorico;
    private GeradorRelatorios relatorios;    
    @FXML private Label mensagemLabel;

    private ILojaFacade facade;
    private final ObservableList<ContratoAluguel> listaHistorico = FXCollections.observableArrayList();

    public void setFacade(ILojaFacade facade) {
        this.facade = facade;
        this.relatorios = new GeradorRelatorios(facade);
    }

        @FXML
    private void initialize() {
        colContratoId.setCellValueFactory(new ColunaTexto<ContratoAluguel>() {
            @Override
            protected String valor(ContratoAluguel c) {
                return c.getId();
            }
        });
        colItem.setCellValueFactory(new ColunaTexto<ContratoAluguel>() {
            @Override
            protected String valor(ContratoAluguel c) {
                return c.getItem().getNome();
            }
        });
        colDataRetirada.setCellValueFactory(new ColunaTexto<ContratoAluguel>() {
            @Override
            protected String valor(ContratoAluguel c) {
                return GeradorRelatorios.formatarData(c.getDataRetirada());
            }
        });
        colDataDevolucao.setCellValueFactory(new ColunaTexto<ContratoAluguel>() {
            @Override
            protected String valor(ContratoAluguel c) {
                if (c.getDataEfetivaDevolucao() != null) {
                    return GeradorRelatorios.formatarData(c.getDataEfetivaDevolucao());
                }
                return "Prev. " + GeradorRelatorios.formatarData(c.getDataPrevDevolucao());
            }
        });
        colStatus.setCellValueFactory(new ColunaTexto<ContratoAluguel>() {
            @Override
            protected String valor(ContratoAluguel c) {
                return c.getStatus();
            }
        });
        colValorPago.setCellValueFactory(new ColunaTexto<ContratoAluguel>() {
            @Override
            protected String valor(ContratoAluguel c) {
                return GeradorRelatorios.formatarMoeda(c.getValorTotal());
            }
        });

        tabelaHistorico.setItems(listaHistorico);
    }

    @FXML
    private void handleBuscarHistorico() {
        String clienteId = txtClienteId.getText().trim();
        if (clienteId.isEmpty()) {
            exibirMensagem("Informe o ID do cliente para realizar a consulta.", false);
            return;
        }

        if (facade == null) {
            exibirMensagem("Erro interno: Serviço de histórico não configurado.", false);
            return;
        }

                try {
            Usuario cliente = facade.buscarUsuario(clienteId);
            if (cliente == null) {
                listaHistorico.clear();
                painelHistorico.limpar();
                exibirMensagem("Cliente não encontrado.", false);
                return;
            }

            Map<String, ContratoAluguel> mapaHistorico = facade.consultarHistoricoCliente(clienteId);
            listaHistorico.setAll(mapaHistorico.values());
            painelHistorico.mostrar(relatorios.historicoCliente(cliente));

            exibirMensagem(mapaHistorico.isEmpty()
                    ? "Nenhum aluguel registrado para este cliente."
                    : "Histórico carregado com sucesso.", !mapaHistorico.isEmpty());
        } catch (Exception e) {
            logger.error("Erro ao buscar histórico do cliente '{}'", clienteId, e);
            painelHistorico.limpar();
            exibirMensagem("Erro ao buscar histórico: " + e.getMessage(), false);
        }
    }

    private void exibirMensagem(String msg, boolean sucesso) {
        mensagemLabel.setText(msg);
        mensagemLabel.setStyle(sucesso 
                ? "-fx-font-size: 12px; -fx-text-fill: #16a34a;" 
                : "-fx-font-size: 12px; -fx-text-fill: #dc2626;");
    }
}