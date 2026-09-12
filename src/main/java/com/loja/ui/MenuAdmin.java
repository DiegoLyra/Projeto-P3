package com.loja.ui;

import com.loja.model.*;
import com.loja.padraoFacade.interfaces.ILojaFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Scanner;

public class MenuAdmin {

    private static final Logger logger = LoggerFactory.getLogger(MenuAdmin.class);
    private static final String OPCAO_PROMPT = "Opção: ";

    private final ILojaFacade facade;
    private final Administrador usuarioLogado;
    private final Scanner scanner;

    public MenuAdmin(ILojaFacade facade, Administrador usuarioLogado, Scanner scanner) {
        this.facade = facade;
        this.usuarioLogado = usuarioLogado;
        this.scanner = scanner;
    }

    public void exibir() {
        boolean ativo = true;
        while (ativo) {
            System.out.println("\n=== PAINEL ADMINISTRATIVO: " + usuarioLogado.getNome().toUpperCase() + " ==="); // NOSONAR
            System.out.println("1 - Gerenciar Usuários"); // NOSONAR
            System.out.println("2 - Gerenciar Itens"); // NOSONAR
            System.out.println("3 - Gerenciar Categorias"); // NOSONAR
            System.out.println("4 - Gerenciar Fornecedores"); // NOSONAR
            System.out.println("5 - Emitir Relatórios"); // NOSONAR
            System.out.println("0 - Sair"); // NOSONAR
            System.out.print("Escolha uma opção: "); // NOSONAR

            String opcao = scanner.nextLine();

            switch (opcao) {
                case "1" -> gerenciarUsuarios();
                case "2" -> gerenciarItens();
                case "3" -> gerenciarCategorias();
                case "4" -> gerenciarFornecedores();
                case "5" -> emitirRelatorios();
                case "0" -> {
                    System.out.println("Saindo do painel administrativo..."); // NOSONAR
                    ativo = false;
                }
                default -> System.out.println("Opção inválida!"); // NOSONAR
            }
        }
    }

    private void gerenciarUsuarios() {
        System.out.println("\nGERENCIAR USUÁRIOS"); // NOSONAR
        System.out.println("1 - Cadastrar Usuário (Cliente/Func/Adm)"); // NOSONAR
        System.out.println("2 - Listar Usuários"); // NOSONAR
        System.out.println("3 - Atualizar Usuário"); // NOSONAR
        System.out.println("4 - Desativar Usuário"); // NOSONAR
        System.out.print(OPCAO_PROMPT); // NOSONAR
        String subOpcao = scanner.nextLine();

        try {
            switch (subOpcao) {
                case "1" -> cadastrarUsuario();
                case "2" -> listarUsuarios();
                case "3" -> atualizarUsuario();
                case "4" -> desativarUsuario();
                default -> System.out.println("Opção inválida."); // NOSONAR
            }
        } catch (RuntimeException e) {
            logger.error("Falha ao gerenciar usuários: {}", e.getMessage(), e);
            System.out.println("Erro: " + e.getMessage()); // NOSONAR
        }
    }

    private void cadastrarUsuario() {
        System.out.println("Tipo: 1-Cliente | 2-Funcionário | 3-Administrador"); // NOSONAR
        System.out.print("Escolha o tipo: "); // NOSONAR
        String tipo = scanner.nextLine();

        System.out.print("ID: "); // NOSONAR
        String id = scanner.nextLine();

        System.out.print("Nome: "); // NOSONAR
        String nome = scanner.nextLine();

        System.out.print("Email/Login: "); // NOSONAR
        String email = scanner.nextLine();

        System.out.print("Senha: "); // NOSONAR
        String senha = scanner.nextLine();

        if (tipo.equals("1")) {
            facade.cadastrarCliente(new Cliente(id, nome, email, senha));
            System.out.println("Cliente cadastrado com sucesso!"); // NOSONAR
        } else if (tipo.equals("2")) {
            System.out.print("Cargo do Funcionário: "); // NOSONAR
            String cargo = scanner.nextLine();
            facade.cadastrarFuncionario(new Funcionario(id, nome, email, senha, cargo));
            System.out.println("Funcionário cadastrado com sucesso!"); // NOSONAR
        } else if (tipo.equals("3")) {
            facade.cadastrarAdm(new Administrador(id, nome, email, senha));
            System.out.println("Administrador cadastrado com sucesso!"); // NOSONAR
        } else {
            System.out.println("Tipo de usuário inválido!"); // NOSONAR
        }
    }

    private void listarUsuarios() {
        System.out.println("1-Todos | 2-Por Perfil (CLIENTE/FUNCIONARIO/ADMINISTRADOR)"); // NOSONAR
        System.out.print(OPCAO_PROMPT); // NOSONAR
        String listOpt = scanner.nextLine();

        if (listOpt.equals("1")) {
            facade.listarUsuario().values().forEach(u -> System.out.println("ID: " + u.getId() + " | Nome: " + u.getNome() + " | Perfil: " + u.getPerfil())); // NOSONAR
        } else if (listOpt.equals("2")) {
            System.out.print("Perfil desejado: "); // NOSONAR
            String perfil = scanner.nextLine().toUpperCase();
            if (facade.listarUsuarioPorPerfil(perfil).isEmpty()) {
                throw new RuntimeException("Nehum usuário de perfil " + perfil);
            }
            facade.listarUsuarioPorPerfil(perfil).values().forEach(u -> System.out.println("ID: " + u.getId() + " | Nome: " + u.getNome())); // NOSONAR
        } else {
            System.out.println("Digite uma opção válida!"); // NOSONAR
        }
    }

    private void atualizarUsuario() {
        System.out.print("ID do usuário a atualizar: "); // NOSONAR
        String id = scanner.nextLine();

        Usuario u = facade.buscarUsuario(id);

        System.out.println("O que você deseja atualizar?"); // NOSONAR
        System.out.println("1 - Nome"); // NOSONAR
        System.out.println("2 - Email/Login"); // NOSONAR
        System.out.println("3 - Senha"); // NOSONAR
        System.out.println("4 - Cargo (quando aplicavel)"); // NOSONAR
        String escolha = scanner.nextLine();

        if (escolha.equals("1")) {
            System.out.print("Novo Nome (" + u.getNome() + "): "); // NOSONAR
            String novoNome = scanner.nextLine();
            if (novoNome.isBlank()) throw new RuntimeException("nome inválido!");
            u.setNome(novoNome);
        } else if (escolha.equals("2")) {
            System.out.print("Novo Email/Login (" + u.getLogin() + "): "); // NOSONAR
            String novoLogin = scanner.nextLine();
            if (novoLogin.isBlank()) throw new RuntimeException("login inválido!");
            u.setLogin(novoLogin);
        } else if (escolha.equals("3")) {
            System.out.print("Nova senha: "); // NOSONAR
            String novaSenha = scanner.nextLine();
            if (novaSenha.isBlank() || novaSenha.length() < 3) throw new RuntimeException("Senha inválida!");
            u.setSenha(novaSenha);
        } else if (escolha.equals("4") && !(u instanceof Funcionario)) {
            throw new RuntimeException("O usuário não é funcionário!");
        } else if (escolha.equals("4")) {
            System.out.print("Novo cargo (" + ((Funcionario) u).getCargo() + "): "); // NOSONAR
            String novoCargo = scanner.nextLine();
            if (novoCargo.isBlank()) throw new RuntimeException("Cargo inválido!");
            ((Funcionario) u).setCargo(novoCargo);
        } else {
            throw new RuntimeException("Opção inválida!");
        }

        facade.atualizarUsuario(id, u);
        System.out.println("Usuário atualizado com sucesso!"); // NOSONAR
    }

    private void desativarUsuario() {
        System.out.print("ID do usuário a desativar: "); // NOSONAR
        String id = scanner.nextLine();
        facade.desativarUsuario(id);
        System.out.println("Usuário desativado com sucesso."); // NOSONAR
    }

    private void gerenciarItens() {
        System.out.println("\nGERENCIAR ITENS"); // NOSONAR
        System.out.println("1 - Cadastrar Item"); // NOSONAR
        System.out.println("2 - Listar Itens"); // NOSONAR
        System.out.println("3 - Atualizar Item"); // NOSONAR
        System.out.println("4 - Deletar Item"); // NOSONAR
        System.out.print("Escolha uma opção: "); // NOSONAR
        String subOpcao = scanner.nextLine();

        try {
            switch (subOpcao) {
                case "1" -> cadastrarItem();
                case "2" -> listarItens();
                case "3" -> atualizarItem();
                case "4" -> deletarItem();
                default -> System.out.println("Opção inválida."); // NOSONAR
            }
        } catch (RuntimeException e) {
            logger.error("Falha ao gerenciar itens: {}", e.getMessage(), e);
            System.out.println("Erro: " + e.getMessage()); // NOSONAR
        }
    }

    private void cadastrarItem() {
        Item item = new Item();

        System.out.print("ID: "); // NOSONAR
        item.setId(scanner.nextLine());

        System.out.print("Nome: "); // NOSONAR
        item.setNome(scanner.nextLine());

        item.setStatus("DISPONIVEL");

        System.out.print("ID Categoria: "); // NOSONAR
        item.setCategoria(facade.buscarCategoria(scanner.nextLine()));

        System.out.print("ID Fornecedor: "); // NOSONAR
        item.setFornecedor(facade.buscarFornecedor(scanner.nextLine()));

        item.setTaxaDiaria(lerValorMonetario("Taxa Diária (XX.xx):  R$ ", "taxa diária"));
        item.setValorReposicao(lerValorMonetario("Valor de reposição (XX.xx): R$ ", "valor de reposição"));

        facade.cadastrarItem(item);
        System.out.println("Item cadastrado!"); // NOSONAR
    }

    private BigDecimal lerValorMonetario(String prompt, String rotulo) {
        try {
            System.out.print(prompt); // NOSONAR
            BigDecimal valor = new BigDecimal(scanner.nextLine());
            if (valor.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("O valor não pode ser negativo!");
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Valor inválido para " + rotulo + ".", e);
        }
    }

    private void listarItens() {
        System.out.println("1-Todos | 2-Por Status | 3-Por Categoria | 4-Por Fornecedor"); // NOSONAR
        System.out.print(OPCAO_PROMPT); // NOSONAR
        String opt = scanner.nextLine();

        if (opt.equals("1")) {
            facade.listarItem().values().forEach(i -> System.out.println("ID: " + i.getId() + " | Nome: " + i.getNome() + " | Status: " + i.getStatus())); // NOSONAR
        } else if (opt.equals("2")) {
            System.out.print("Status (DISPONIVEL/ALUGADO): "); // NOSONAR
            String status = scanner.nextLine().toUpperCase();
            facade.listarItemPorStatus(status).values().forEach(i -> System.out.println("ID: " + i.getId() + " | Nome: " + i.getNome())); // NOSONAR
        } else if (opt.equals("3")) {
            System.out.print("ID Categoria: "); // NOSONAR
            Categoria cat = facade.buscarCategoria(scanner.nextLine());
            facade.listarItemPorCategoria(cat).values().forEach(i -> System.out.println("ID: " + i.getId() + " | Nome: " + i.getNome())); // NOSONAR
        } else if (opt.equals("4")) {
            System.out.print("ID Fornecedor: "); // NOSONAR
            Fornecedor forn = facade.buscarFornecedor(scanner.nextLine());
            facade.listarItemPorFornecedor(forn).values().forEach(i -> System.out.println("ID: " + i.getId() + " | Nome: " + i.getNome())); // NOSONAR
        }
    }

    private void atualizarItem() {
        System.out.print("ID do Item: "); // NOSONAR
        Item item = facade.buscarItem(scanner.nextLine());

        System.out.println("O que você deseja atualizar?"); // NOSONAR
        System.out.println("1 - Nome"); // NOSONAR
        System.out.println("2 - Taxa diária"); // NOSONAR
        System.out.println("3 - Valor de reposição"); // NOSONAR
        System.out.println("4 - Categoria"); // NOSONAR
        System.out.println("5 - Fornecedor"); // NOSONAR
        String escolha = scanner.nextLine();

        if (escolha.equals("1")) {
            System.out.print("Novo Nome (" + item.getNome() + "): "); // NOSONAR
            String novoNome = scanner.nextLine();
            if (novoNome.isBlank()) throw new RuntimeException("nome inválido!");
            item.setNome(novoNome);
        } else if (escolha.equals("2")) {
            item.setTaxaDiaria(lerValorMonetario("Nova taxa diária (" + item.getTaxaDiaria() + ")(XX.xx):  R$ ", "taxa diária"));
        } else if (escolha.equals("3")) {
            item.setValorReposicao(lerValorMonetario("Valor de reposição (XX.xx): R$ ", "valor de reposição"));
        } else if (escolha.equals("4")) {
            System.out.print("Digite o id da categoria (" + item.getCategoria().getId() + "): "); // NOSONAR
            item.setCategoria(facade.buscarCategoria(scanner.nextLine()));
        } else if (escolha.equals("5")) {
            System.out.print("Digite o id do fornecedor (" + item.getFornecedor().getId() + "): "); // NOSONAR
            item.setFornecedor(facade.buscarFornecedor(scanner.nextLine()));
        } else {
            throw new RuntimeException("Opção inválida!");
        }

        facade.atualizarItem(item);
        System.out.println("Item atualizado com sucesso!"); // NOSONAR
    }

    private void deletarItem() {
        System.out.print("ID do Item a deletar: "); // NOSONAR
        facade.deletarItem(scanner.nextLine());
        System.out.println("Item deletado do repositório."); // NOSONAR
    }

    private void gerenciarCategorias() {
        System.out.println("\nGERENCIAR CATEGORIAS"); // NOSONAR
        System.out.println("1 - Cadastrar"); // NOSONAR
        System.out.println("2 - Listar"); // NOSONAR
        System.out.println("3 - Atualizar"); // NOSONAR
        System.out.println("4 - Deletar"); // NOSONAR
        System.out.print(OPCAO_PROMPT); // NOSONAR
        String subOpcao = scanner.nextLine();

        try {
            if (subOpcao.equals("1")) {
                System.out.print("ID: "); // NOSONAR
                String id = scanner.nextLine();
                System.out.print("Nome: "); // NOSONAR
                String nome = scanner.nextLine();
                facade.cadastrarCategoria(new Categoria(id, nome));
                System.out.println("Categoria criada!"); // NOSONAR

            } else if (subOpcao.equals("2")) {
                facade.listarCategoria().values().forEach(c -> System.out.println("ID: " + c.getId() + " | Nome: " + c.getNome())); // NOSONAR

            } else if (subOpcao.equals("3")) {
                System.out.print("ID: "); // NOSONAR
                Categoria c = facade.buscarCategoria(scanner.nextLine());
                System.out.print("Novo Nome: "); // NOSONAR
                c.setNome(scanner.nextLine());
                facade.atualizarCategoria(c);
                System.out.println("Categoria atualizada!"); // NOSONAR

            } else if (subOpcao.equals("4")) {
                System.out.print("ID a deletar: "); // NOSONAR
                facade.deletarCategoria(scanner.nextLine());
                System.out.println("Categoria removida."); // NOSONAR
            }
        } catch (RuntimeException e) {
            logger.error("Falha ao gerenciar categorias: {}", e.getMessage(), e);
            System.out.println("Erro: " + e.getMessage()); // NOSONAR
        }
    }

    private void gerenciarFornecedores() {
        System.out.println("\nGERENCIAR FORNECEDORES"); // NOSONAR
        System.out.println("1 - Cadastrar"); // NOSONAR
        System.out.println("2 - Listar"); // NOSONAR
        System.out.println("3 - Atualizar"); // NOSONAR
        System.out.println("4 - Deletar"); // NOSONAR
        System.out.print(OPCAO_PROMPT); // NOSONAR
        String subOpcao = scanner.nextLine();

        try {
            if (subOpcao.equals("1")) {
                cadastrarFornecedor();
            } else if (subOpcao.equals("2")) {
                facade.listarFornecedor().values().forEach(f -> System.out.println("ID: " + f.getId() + " | Nome: " + f.getNome() + " | CNPJ: " + f.getCnpj() + " | Telefone: " + f.getTelefone())); // NOSONAR
            } else if (subOpcao.equals("3")) {
                atualizarFornecedor();
            } else if (subOpcao.equals("4")) {
                System.out.print("ID a deletar: "); // NOSONAR
                facade.deletarFornecedor(scanner.nextLine());
                System.out.println("Fornecedor removido."); // NOSONAR
            }
        } catch (RuntimeException e) {
            logger.error("Falha ao gerenciar fornecedores: {}", e.getMessage(), e);
            System.out.println("Erro: " + e.getMessage()); // NOSONAR
        }
    }

    private void cadastrarFornecedor() {
        System.out.print("ID: "); // NOSONAR
        String id = scanner.nextLine();
        System.out.print("Nome: "); // NOSONAR
        String nome = scanner.nextLine();
        System.out.print("CNPJ: "); // NOSONAR
        String cnpj = scanner.nextLine();
        System.out.print("Telefone: "); // NOSONAR
        String telefone = scanner.nextLine();

        facade.cadastrarFornecedor(new Fornecedor(id, nome, cnpj, telefone));
        System.out.println("Fornecedor criado!"); // NOSONAR
    }

    private void atualizarFornecedor() {
        System.out.print("ID: "); // NOSONAR
        Fornecedor f = facade.buscarFornecedor(scanner.nextLine());

        System.out.println("O que você deseja atualizar?"); // NOSONAR
        System.out.println("1 - Nome"); // NOSONAR
        System.out.println("2 - CNPJ"); // NOSONAR
        System.out.println("3 - Telefone"); // NOSONAR
        System.out.print(OPCAO_PROMPT); // NOSONAR
        String escolha = scanner.nextLine();

        if (escolha.equals("1")) {
            System.out.print("Novo Nome (" + f.getNome() + "): "); // NOSONAR
            String novoNome = scanner.nextLine();
            if (novoNome.isBlank()) throw new RuntimeException("Nome inválido!");
            f.setNome(novoNome);
        } else if (escolha.equals("2")) {
            System.out.print("Novo CNPJ (" + f.getCnpj() + "): "); // NOSONAR
            String novoCnpj = scanner.nextLine();
            if (novoCnpj.isBlank()) throw new RuntimeException("CNPJ inválido!");
            f.setCnpj(novoCnpj);
        } else if (escolha.equals("3")) {
            System.out.print("Novo Telefone (" + f.getTelefone() + "): "); // NOSONAR
            String novoTelefone = scanner.nextLine();
            if (novoTelefone.isBlank()) throw new RuntimeException("Telefone inválido!");
            f.setTelefone(novoTelefone);
        } else {
            throw new RuntimeException("Opção inválida!");
        }

        facade.atualizarFornecedor(f);
        System.out.println("Fornecedor atualizado!"); // NOSONAR
    }

    private void emitirRelatorios() {
        System.out.println("\nEMITIR RELATÓRIOS"); // NOSONAR
        System.out.println("1 - Itens Disponíveis"); // NOSONAR
        System.out.println("2 - Aluguéis Atuais (Ativos)"); // NOSONAR
        System.out.println("3 - Aluguel de um Cliente (Histórico)"); // NOSONAR
        System.out.println("4 - Financeiro (Faturamento)"); // NOSONAR
        System.out.print(OPCAO_PROMPT); // NOSONAR
        String subOpcao = scanner.nextLine();

        switch (subOpcao) {
            case "1" -> relatorioItensDisponiveis();
            case "2" -> relatorioAlugueisAtivos();
            case "3" -> relatorioContratosPorCliente();
            case "4" -> relatorioFaturamento();
            default -> System.out.println("Opção inválida!"); // NOSONAR
        }
    }

    private void relatorioItensDisponiveis() {
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

    private void relatorioAlugueisAtivos() {
        System.out.println("\nRELATÓRIO DE CONTRATOS ATIVOS"); // NOSONAR
        try {
            String relatorio = facade.gerarRelatorioItensAlugados();
            System.out.println(relatorio); // NOSONAR
        } catch (RuntimeException e) {
            logger.error("Falha ao gerar relatório de itens alugados: {}", e.getMessage(), e);
            System.out.println("Erro ao gerar relatório: " + e.getMessage()); // NOSONAR
        }
    }

    private void relatorioContratosPorCliente() {
        System.out.println("\nRELATÓRIO DE CONTRATOS POR CLIENTE"); // NOSONAR
        System.out.print("ID do Cliente: "); // NOSONAR
        String clienteId = scanner.nextLine();
        try {
            Map<String, ContratoAluguel> contratos = facade.consultarHistoricoCliente(clienteId);
            if (contratos.isEmpty()) {
                System.out.println("Não há histórico de contratos para esse cliente."); // NOSONAR
            } else {
                for (ContratoAluguel c : contratos.values()) {
                    System.out.println("ID: " + c.getId() + " | Item: " + c.getItem().getNome() + " | Valor total: " + c.getValorTotal() + " | Status: " + c.getStatus() + " | Devolução prevista: " + c.getDataPrevDevolucao()); // NOSONAR
                }
            }
        } catch (RuntimeException e) {
            logger.error("Falha ao listar contratos do cliente '{}': {}", clienteId, e.getMessage(), e);
            System.out.println("Erro ao listar contratos: " + e.getMessage()); // NOSONAR
        }
    }

    private void relatorioFaturamento() {
        System.out.println("\nRELATÓRIO FINANCEIRO"); // NOSONAR
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate ini = null;
        LocalDate fim = null;

        while (ini == null) {
            try {
                System.out.print("Data Inicial (dd/MM/yyyy): "); // NOSONAR
                ini = LocalDate.parse(scanner.nextLine(), formatter);
            } catch (DateTimeParseException e) {
                System.out.println("Data inválida, tente novamente."); // NOSONAR
            }
        }

        while (fim == null) {
            try {
                System.out.print("Data Final (dd/MM/yyyy): "); // NOSONAR
                fim = LocalDate.parse(scanner.nextLine(), formatter);
                if (fim.isBefore(ini)) {
                    System.out.println("Data final não pode ser anterior à data inicial."); // NOSONAR
                    fim = null;
                }
            } catch (DateTimeParseException e) {
                System.out.println("Data inválida, tente novamente."); // NOSONAR
            }
        }

        try {
            System.out.println(facade.gerarRelatorioFaturamento(ini, fim)); // NOSONAR
        } catch (RuntimeException e) {
            logger.error("Falha ao gerar relatório de faturamento ({} a {}): {}", ini, fim, e.getMessage(), e);
            System.out.println("Erro ao gerar relatório: " + e.getMessage()); // NOSONAR
        }
    }
}