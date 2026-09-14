package com.loja.ui;

import com.loja.model.Item;
import com.loja.padraoFacade.interfaces.ILojaFacade;
import com.loja.model.Funcionario;
import com.loja.model.Cliente;
import com.loja.model.ContratoAluguel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Scanner;

public class MenuFuncionario {

    private static final Logger logger = LoggerFactory.getLogger(MenuFuncionario.class);

    private final ILojaFacade facade;
    private final Funcionario usuarioLogado;
    private final Scanner scanner;

    public MenuFuncionario(ILojaFacade facade, Funcionario usuarioLogado, Scanner scanner) {
        this.facade = facade;
        this.usuarioLogado = usuarioLogado;
        this.scanner = scanner;
    }

    public void exibir() {
        boolean ativo = true;
        while (ativo) {
            System.out.println("\nPAINEL DO FUNCIONÁRIO: " + usuarioLogado.getNome().toUpperCase()); // NOSONAR
            System.out.println("1 - Registrar Novo Aluguel"); // NOSONAR
            System.out.println("2 - Processar Devolução de Item"); // NOSONAR
            System.out.println("3 - Cadastrar Novo Cliente"); // NOSONAR
            System.out.println("4 - Emitir Relatórios de itens disponíveis"); // NOSONAR
            System.out.println("5 - Emitir Relatórios de contratos ativos"); // NOSONAR
            System.out.println("6 - Emitir Relatórios de contratos por cliente"); // NOSONAR
            System.out.println("7 - Quitar uma Multa de Cliente"); // NOSONAR
            System.out.println("0 - Sair"); // NOSONAR
            System.out.print("Escolha uma opção: "); // NOSONAR

            String opcao = scanner.nextLine();

            switch (opcao) {
                case "1" -> registrarAluguel();
                case "2" -> processarDevolucao();
                case "3" -> cadastrarCliente();
                case "4" -> emitirRelatorioDisponiveis();
                case "5" -> emitirRelatorioAlugados();
                case "6" -> emitirRelatorioContratosCliente();
                case "7" -> quitarMulta();
                case "0" -> {
                    System.out.println("Saindo..."); // NOSONAR
                    ativo = false;
                }
                default -> System.out.println("Opção inválida!"); // NOSONAR
            }
        }
    }

    private void registrarAluguel() {
        System.out.println("\nREGISTRAR NOVO ALUGUEL"); // NOSONAR

        System.out.print("ID do Cliente: "); // NOSONAR
        String clienteId = scanner.nextLine();

        System.out.print("ID do Item: "); // NOSONAR
        String itemId = scanner.nextLine();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate dataRetirada = null;
        LocalDate dataPrevDevolucao = null;

        while (dataRetirada == null) {
            try {
                System.out.print("Data de Retirada (dd/MM/yyyy): "); // NOSONAR
                dataRetirada = LocalDate.parse(scanner.nextLine(), formatter);
            } catch (DateTimeParseException e) {
                System.out.println("Data inválida, tente novamente."); // NOSONAR
            }
        }

        while (dataPrevDevolucao == null) {
            try {
                System.out.print("Data Prevista de Devolução (dd/MM/yyyy): "); // NOSONAR
                dataPrevDevolucao = LocalDate.parse(scanner.nextLine(), formatter);
                if (dataPrevDevolucao.isBefore(dataRetirada)) {
                    System.out.println("Data de devolução não pode ser anterior à data de retirada."); // NOSONAR
                    dataPrevDevolucao = null;
                }
            } catch (DateTimeParseException e) {
                System.out.println("Data inválida, tente novamente."); // NOSONAR
            }
        }

        try {
            ContratoAluguel contrato = facade.registrarAluguel(clienteId, itemId, dataRetirada, dataPrevDevolucao);
            System.out.println("Sucesso! Contrato firmado com o ID: " + contrato.getId()); // NOSONAR
        } catch (RuntimeException e) {
            logger.error("Falha ao registrar aluguel (cliente='{}', item='{}'): {}", clienteId, itemId, e.getMessage(), e);
            System.out.println("Erro ao abrir aluguel: " + e.getMessage()); // NOSONAR
        }
    }

    private void processarDevolucao() {
        System.out.println("\nPROCESSAR DEVOLUÇÃO"); // NOSONAR
        System.out.print("Digite o ID do Contrato de Aluguel: "); // NOSONAR
        String contratoId = scanner.nextLine();
        try {
            ContratoAluguel contrato = facade.processarDevolucao(contratoId);
            System.out.println("Devolução processada com sucesso! Contrato ID: " + contrato.getId() + " finalizado."); // NOSONAR
            if (facade.possuiMultaPendente(contrato.getCliente().getId())) {
                System.out.println("Atenção: devolução em atraso, multa aplicada ao cliente."); // NOSONAR
            }
        } catch (RuntimeException e) {
            logger.error("Falha ao processar devolução do contrato '{}': {}", contratoId, e.getMessage(), e);
            System.out.println("Erro ao processar encerramento de contrato: " + e.getMessage()); // NOSONAR
        }
    }

    private void cadastrarCliente() {
        System.out.println("\nCADASTRO DE NOVO CLIENTE"); // NOSONAR

        System.out.print("ID: "); // NOSONAR
        String id = scanner.nextLine();

        System.out.print("Nome Completo: "); // NOSONAR
        String nome = scanner.nextLine();

        System.out.print("E-mail: "); // NOSONAR
        String email = scanner.nextLine();

        System.out.print("Senha de Acesso: "); // NOSONAR
        String senha = scanner.nextLine();

        Cliente novoCliente = new Cliente(id, nome, email, senha);
        try {
            facade.cadastrarCliente(novoCliente);
            System.out.println("Cliente cadastrado com sucesso!"); // NOSONAR
        } catch (RuntimeException e) {
            logger.error("Falha ao cadastrar cliente '{}': {}", id, e.getMessage(), e);
            System.out.println("Falha ao salvar cliente: " + e.getMessage()); // NOSONAR
        }
    }

    private void emitirRelatorioDisponiveis() {
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

    private void emitirRelatorioAlugados() {
        System.out.println("\nRELATÓRIO DE CONTRATOS ATIVOS"); // NOSONAR
        try {
            String relatorio = facade.gerarRelatorioItensAlugados();
            System.out.println(relatorio); // NOSONAR
        } catch (RuntimeException e) {
            logger.error("Falha ao gerar relatório de itens alugados: {}", e.getMessage(), e);
            System.out.println("Erro ao gerar relatório: " + e.getMessage()); // NOSONAR
        }
    }

    private void emitirRelatorioContratosCliente() {
        System.out.println("\nRELATÓRIO DE CONTRATOS POR CLIENTE"); // NOSONAR
        System.out.print("Digite o ID do cliente: "); // NOSONAR
        String id = scanner.nextLine();
        try {
            Map<String, ContratoAluguel> contratos = facade.consultarHistoricoCliente(id);
            if (contratos.isEmpty()) {
                System.out.println("Não há histórico de contratos para esse cliente."); // NOSONAR
            } else {
                for (ContratoAluguel con : contratos.values()) {
                    System.out.println("ID: " + con.getId() + " | Item: " + con.getItem().getNome() + " | Valor total: " + con.getValorTotal() + " | Status: " + con.getStatus() + " | Devolução prevista: " + con.getDataPrevDevolucao()); // NOSONAR
                }
            }
        } catch (RuntimeException e) {
            logger.error("Falha ao listar contratos do cliente '{}': {}", id, e.getMessage(), e);
            System.out.println("Erro ao listar contratos: " + e.getMessage()); // NOSONAR
        }
    }

    private void quitarMulta() {
        System.out.println("\nQUITAR MULTA FINANCEIRA"); // NOSONAR
        System.out.print("Digite o ID da Multa a ser quitada: "); // NOSONAR
        String multaId = scanner.nextLine();
        try {
            facade.quitarMulta(multaId);
            System.out.println("Sucesso! A multa foi alterada para QUITADA."); // NOSONAR
        } catch (RuntimeException e) {
            logger.error("Falha ao quitar multa '{}': {}", multaId, e.getMessage(), e);
            System.out.println("Erro ao dar baixa na multa: " + e.getMessage()); // NOSONAR
        }
    }
}