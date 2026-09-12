package com.loja.ui;

import com.loja.padraoFacade.interfaces.ILojaFacade;
import com.loja.model.Cliente;
import com.loja.model.ContratoAluguel;
import com.loja.model.Item;
import com.loja.model.Multa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Scanner;

public class MenuCliente {

    private static final Logger logger = LoggerFactory.getLogger(MenuCliente.class);

    private final ILojaFacade facade;
    private final Cliente usuarioLogado;
    private final Scanner scanner;

    public MenuCliente(ILojaFacade facade, Cliente usuarioLogado, Scanner scanner) {
        this.facade = facade;
        this.usuarioLogado = usuarioLogado;
        this.scanner = scanner;
    }

    public void exibir() {
        boolean logado = true;
        while (logado) {
            System.out.println("\nÁREA DO CLIENTE: " + usuarioLogado.getNome().toUpperCase()); // NOSONAR
            System.out.println("1 - Verificar Itens Disponíveis para Aluguel"); // NOSONAR
            System.out.println("2 - Ver Meus Aluguéis (Histórico)"); // NOSONAR
            System.out.println("3 - Verificar Minhas Multas Pendentes"); // NOSONAR
            System.out.println("0 - Sair"); // NOSONAR
            System.out.print("Escolha uma opção: "); // NOSONAR

            String opcao = scanner.nextLine();

            switch (opcao) {
                case "1" -> verItensDisponiveis();
                case "2" -> verMeusAlugueis();
                case "3" -> verMultasPendentes();
                case "0" -> {
                    System.out.println("Saindo..."); // NOSONAR
                    logado = false;
                }
                default -> System.out.println("Opção inválida!"); // NOSONAR
            }

        }
    }

    public void verItensDisponiveis() {
        System.out.println("\nITENS DISPONÍVEIS"); // NOSONAR
        try {
            Map<String, Item> itens = facade.listarItensDisponiveis();
            if (itens.isEmpty()) {
                System.out.println("Não há itens disponíveis para aluguel no momento."); // NOSONAR
            } else {
                for (Item item : itens.values()) {
                    System.out.println("ID: " + item.getId() + " | Nome: " + item.getNome() + " | Valor Diário: " + item.getTaxaDiaria()); // NOSONAR
                }
            }
        } catch (RuntimeException e) {
            logger.error("Falha ao listar itens disponíveis: {}", e.getMessage(), e);
            System.out.println("Erro ao listar itens: " + e.getMessage()); // NOSONAR
        }
    }

    public void verMeusAlugueis() {
        System.out.println("\nHISTÓRICO DE ALUGUÉIS"); // NOSONAR
        try {
            Map<String, ContratoAluguel> contratos = facade.consultarHistoricoCliente(usuarioLogado.getId());
            if (contratos.isEmpty()) {
                System.out.println("Você não possui registros de aluguéis."); // NOSONAR
            } else {
                for (ContratoAluguel c : contratos.values()) {
                    String itemNome = (c.getItem() != null) ? c.getItem().getNome() : "Item não identificado";
                    System.out.println("Contrato ID: " + c.getId() + " | Item: " + itemNome + " | Retirada: " + c.getDataRetirada() + " | Prev. Devolução: " + c.getDataPrevDevolucao()); // NOSONAR
                }
            }
        } catch (RuntimeException e) {
            logger.error("Falha ao buscar histórico do cliente '{}': {}", usuarioLogado.getId(), e.getMessage(), e);
            System.out.println("Erro ao buscar histórico: " + e.getMessage()); // NOSONAR
        }
    }

    public void verMultasPendentes() {
        System.out.println("\nMINHAS MULTAS"); // NOSONAR
        try {
            boolean temMulta = facade.possuiMultaPendente(usuarioLogado.getId());
            if (!temMulta) {
                System.out.println("Você não possui multas pendentes no momento."); // NOSONAR
            } else {
                Map<String, Multa> multas = facade.listarMultaPorCliente(usuarioLogado.getId());
                for (Multa m : multas.values()) {
                    System.out.println("Multa ID: " + m.getId() + " | Motivo: " + m.getMotivo() + " | Total: R$ " + m.getValorTotal() + " | Status: " + m.getStatus()); // NOSONAR
                }
            }
        } catch (RuntimeException e) {
            logger.error("Falha ao verificar multas do cliente '{}': {}", usuarioLogado.getId(), e.getMessage(), e);
            System.out.println("Erro ao processar verificação de multas: " + e.getMessage()); // NOSONAR
        }
    }
}