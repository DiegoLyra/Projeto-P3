package com.loja.ui;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loja.model.Administrador;
import com.loja.model.Categoria;
import com.loja.model.Cliente;
import com.loja.model.ContratoAluguel;
import com.loja.model.Fornecedor;
import com.loja.model.Funcionario;
import com.loja.model.Item;
import com.loja.model.Usuario;
import com.loja.padrao.facade.interfaces.ILojaFacade;


@SuppressWarnings("java:S106")
public class MenuAdmin {

    private static final String MSG_ATUALIZAR = "O que você deseja atualizar?";
    private static final Logger logger = LoggerFactory.getLogger(MenuAdmin.class);
    private static final String OPCAO_PROMPT = "Opção: ";
    private static final String NOME_PROMPT = "Novo Nome (";
    private static final String OPCAO_NOME = "1 - Nome";
    private static final String LABEL_NOME = "Nome: ";
    private static final String MSG_OPCAO_INVALIDA = "Opção inválida.";
    private static final String SEPARADOR_NOME = " | Nome: ";
    private static final String MSG_ERRO = "Erro: ";

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
            System.out.println("\n=== PAINEL ADMINISTRATIVO: " + usuarioLogado.getNome().toUpperCase() + " ===");
            System.out.println("1 - Gerenciar Usuários");
            System.out.println("2 - Gerenciar Itens");
            System.out.println("3 - Gerenciar Categorias");
            System.out.println("4 - Gerenciar Fornecedores");
            System.out.println("5 - Emitir Relatórios");
            System.out.println("0 - Sair");
            System.out.print("Escolha uma opção: ");

            String opcao = scanner.nextLine();

            switch (opcao) {
                case "1" -> gerenciarUsuarios();
                case "2" -> gerenciarItens();
                case "3" -> gerenciarCategorias();
                case "4" -> gerenciarFornecedores();
                case "5" -> emitirRelatorios();
                case "0" -> {
                    System.out.println("Saindo do painel administrativo...");
                    ativo = false;
                }
                default -> System.out.println(MSG_OPCAO_INVALIDA);
            }
        }
    }


    private void gerenciarUsuarios() {
        System.out.println("\nGERENCIAR USUÁRIOS");
        System.out.println("1 - Cadastrar Usuário (Cliente/Func/Adm)");
        System.out.println("2 - Listar Usuários");
        System.out.println("3 - Atualizar Usuário");
        System.out.println("4 - Desativar Usuário");
        System.out.print(OPCAO_PROMPT);
        String subOpcao = scanner.nextLine();

        try {
            switch (subOpcao) {
                case "1" -> cadastrarUsuario();
                case "2" -> listarUsuarios();
                case "3" -> atualizarUsuario();
                case "4" -> desativarUsuario();
                default -> System.out.println(MSG_OPCAO_INVALIDA);
            }
        } catch (RuntimeException e) {
            logger.error("Falha ao gerenciar usuários: {}", e.getMessage(), e);
            System.out.println(MSG_ERRO + e.getMessage());
        }
    }

    private void cadastrarUsuario() {
        System.out.println("Tipo: 1-Cliente | 2-Funcionário | 3-Administrador");
        System.out.print("Escolha o tipo: ");
        String tipo = scanner.nextLine();

        System.out.print("ID: ");
        String id = scanner.nextLine();

        System.out.print(LABEL_NOME);
        String nome = scanner.nextLine();

        System.out.print("Email/Login: ");
        String email = scanner.nextLine();

        System.out.print("Senha: ");
        String senha = scanner.nextLine();

        switch (tipo) {
            case "1" -> cadastrarCliente(id, nome, email, senha);
            case "2" -> cadastrarFuncionarioNovo(id, nome, email, senha);
            case "3" -> cadastrarAdministrador(id, nome, email, senha);
            default -> System.out.println("Tipo de usuário inválido!");
        }
    }

    private void cadastrarCliente(String id, String nome, String email, String senha) {
        facade.cadastrarCliente(new Cliente(id, nome, email, senha));
        System.out.println("Cliente cadastrado com sucesso!");
    }

    private void cadastrarFuncionarioNovo(String id, String nome, String email, String senha) {
        System.out.print("Cargo do Funcionário: ");
        String cargo = scanner.nextLine();
        facade.cadastrarFuncionario(new Funcionario(id, nome, email, senha, cargo));
        System.out.println("Funcionário cadastrado com sucesso!");
    }

    private void cadastrarAdministrador(String id, String nome, String email, String senha) {
        facade.cadastrarAdm(new Administrador(id, nome, email, senha));
        System.out.println("Administrador cadastrado com sucesso!");
    }

    private void listarUsuarios() {
        System.out.println("1-Todos | 2-Por Perfil (CLIENTE/FUNCIONARIO/ADMINISTRADOR)");
        System.out.print(OPCAO_PROMPT);
        String listOpt = scanner.nextLine();

        if (listOpt.equals("1")) {
            listarTodosUsuarios();
        } else if (listOpt.equals("2")) {
            listarUsuariosPorPerfil();
        } else {
            System.out.println("Digite uma opção válida!");
        }
    }

    private void listarTodosUsuarios() {
        facade.listarUsuario().values()
                .forEach(u -> System.out.println("ID: " + u.getId() + SEPARADOR_NOME + u.getNome() + " | Perfil: " + u.getPerfil()));
    }

    private void listarUsuariosPorPerfil() {
        System.out.print("Perfil desejado: ");
        String perfil = scanner.nextLine().toUpperCase();
        Map<String, Usuario> usuarios = facade.listarUsuarioPorPerfil(perfil);
        if (usuarios.isEmpty()) {
            throw new IllegalArgumentException("Nehum usuário de perfil " + perfil);
        }
        usuarios.values().forEach(u -> System.out.println("ID: " + u.getId() + SEPARADOR_NOME + u.getNome()));
    }

    private void atualizarUsuario() {
        System.out.print("ID do usuário a atualizar: ");
        String id = scanner.nextLine();

        Usuario u = facade.buscarUsuario(id);

        System.out.println(MSG_ATUALIZAR);
        System.out.println(OPCAO_NOME);
        System.out.println("2 - Email/Login");
        System.out.println("3 - Senha");
        System.out.println("4 - Cargo (quando aplicavel)");
        String escolha = scanner.nextLine();

        switch (escolha) {
            case "1" -> atualizarNomeUsuario(u);
            case "2" -> atualizarLoginUsuario(u);
            case "3" -> atualizarSenhaUsuario(u);
            case "4" -> atualizarCargoFuncionario(u);
            default -> throw new IllegalArgumentException("Opção inválida!");
        }

        facade.atualizarUsuario(id, u);
        System.out.println("Usuário atualizado com sucesso!");
    }

    private void atualizarNomeUsuario(Usuario u) {
        System.out.print(NOME_PROMPT + u.getNome() + "): ");
        String novoNome = scanner.nextLine();
        if (novoNome.isBlank()) {
            throw new IllegalArgumentException("nome inválido!");
        }
        u.setNome(novoNome);
    }

    private void atualizarLoginUsuario(Usuario u) {
        System.out.print("Novo Email/Login (" + u.getLogin() + "): ");
        String novoLogin = scanner.nextLine();
        if (novoLogin.isBlank()) {
            throw new IllegalArgumentException("login inválido!");
        }
        u.setLogin(novoLogin);
    }

    private void atualizarSenhaUsuario(Usuario u) {
        System.out.print("Nova senha: ");
        String novaSenha = scanner.nextLine();
        if (novaSenha.isBlank() || novaSenha.length() < 3) {
            throw new IllegalArgumentException("Senha inválida!");
        }
        u.setSenha(novaSenha);
    }

    private void atualizarCargoFuncionario(Usuario u) {
        if (!(u instanceof Funcionario func)) {
            throw new IllegalArgumentException("O usuário não é funcionário!");
        }
        System.out.print("Novo cargo (" + func.getCargo() + "): ");
        String novoCargo = scanner.nextLine();
        if (novoCargo.isBlank()) {
            throw new IllegalArgumentException("Cargo inválido!");
        }
        func.setCargo(novoCargo);
    }

    private void desativarUsuario() {
        System.out.print("ID do usuário a desativar: ");
        String id = scanner.nextLine();
        facade.desativarUsuario(id);
        System.out.println("Usuário desativado com sucesso.");
    }


    private void gerenciarItens() {
        System.out.println("\nGERENCIAR ITENS");
        System.out.println("1 - Cadastrar Item");
        System.out.println("2 - Listar Itens");
        System.out.println("3 - Atualizar Item");
        System.out.println("4 - Deletar Item");
        System.out.print("Escolha uma opção: ");
        String subOpcao = scanner.nextLine();

        try {
            switch (subOpcao) {
                case "1" -> cadastrarItem();
                case "2" -> listarItens();
                case "3" -> atualizarItem();
                case "4" -> deletarItem();
                default -> System.out.println(MSG_OPCAO_INVALIDA);
            }
        } catch (RuntimeException e) {
            logger.error("Falha ao gerenciar itens: {}", e.getMessage(), e);
            System.out.println(MSG_ERRO + e.getMessage());
        }
    }

    private void cadastrarItem() {
        Item item = new Item();

        System.out.print("ID: ");
        item.setId(scanner.nextLine());

        System.out.print(LABEL_NOME);
        item.setNome(scanner.nextLine());

        item.setStatus("DISPONIVEL");

        System.out.print("ID Categoria: ");
        item.setCategoria(facade.buscarCategoria(scanner.nextLine()));

        System.out.print("ID Fornecedor: ");
        item.setFornecedor(facade.buscarFornecedor(scanner.nextLine()));

        item.setTaxaDiaria(lerValorMonetario("Taxa Diária (XX.xx):  R$ ", "taxa diária"));
        item.setValorReposicao(lerValorMonetario("Valor de reposição (XX.xx): R$ ", "valor de reposição"));

        facade.cadastrarItem(item);
        System.out.println("Item cadastrado!");
    }

    private BigDecimal lerValorMonetario(String prompt, String rotulo) {
        try {
            System.out.print(prompt);
            BigDecimal valor = new BigDecimal(scanner.nextLine());
            if (valor.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("O valor não pode ser negativo!");
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valor inválido para " + rotulo + ".", e);
        }
    }

    private void listarItens() {
        System.out.println("1-Todos | 2-Por Status | 3-Por Categoria | 4-Por Fornecedor");
        System.out.print(OPCAO_PROMPT);
        String opt = scanner.nextLine();

        switch (opt) {
            case "1" -> listarTodosItens();
            case "2" -> listarItensPorStatus();
            case "3" -> listarItensPorCategoria();
            case "4" -> listarItensPorFornecedor();
            default -> System.out.println(MSG_OPCAO_INVALIDA);
        }
    }

    private void listarTodosItens() {
        facade.listarItem().values()
                .forEach(i -> System.out.println("ID: " + i.getId() + SEPARADOR_NOME + i.getNome() + " | Status: " + i.getStatus()));
    }

    private void listarItensPorStatus() {
        System.out.print("Status (DISPONIVEL/ALUGADO): ");
        String status = scanner.nextLine().toUpperCase();
        facade.listarItemPorStatus(status).values()
                .forEach(i -> System.out.println("ID: " + i.getId() + SEPARADOR_NOME + i.getNome()));
    }

    private void listarItensPorCategoria() {
        System.out.print("ID Categoria: ");
        Categoria cat = facade.buscarCategoria(scanner.nextLine());
        facade.listarItemPorCategoria(cat).values()
                .forEach(i -> System.out.println("ID: " + i.getId() + SEPARADOR_NOME + i.getNome()));
    }

    private void listarItensPorFornecedor() {
        System.out.print("ID Fornecedor: ");
        Fornecedor forn = facade.buscarFornecedor(scanner.nextLine());
        facade.listarItemPorFornecedor(forn).values()
                .forEach(i -> System.out.println("ID: " + i.getId() + SEPARADOR_NOME + i.getNome()));
    }

    private void atualizarItem() {
        System.out.print("ID do Item: ");
        Item item = facade.buscarItem(scanner.nextLine());

        System.out.println(MSG_ATUALIZAR);
        System.out.println(OPCAO_NOME);
        System.out.println("2 - Taxa diária");
        System.out.println("3 - Valor de reposição");
        System.out.println("4 - Categoria");
        System.out.println("5 - Fornecedor");
        String escolha = scanner.nextLine();

        switch (escolha) {
            case "1" -> atualizarNomeItem(item);
            case "2" -> item.setTaxaDiaria(lerValorMonetario(
                    "Nova taxa diária (" + item.getTaxaDiaria() + ")(XX.xx):  R$ ", "taxa diária"));
            case "3" -> item.setValorReposicao(lerValorMonetario(
                    "Valor de reposição (XX.xx): R$ ", "valor de reposição"));
            case "4" -> atualizarCategoriaItem(item);
            case "5" -> atualizarFornecedorItem(item);
            default -> throw new IllegalArgumentException("Opção inválida!");
        }

        facade.atualizarItem(item);
        System.out.println("Item atualizado com sucesso!");
    }

    private void atualizarNomeItem(Item item) {
        System.out.print(NOME_PROMPT + item.getNome() + "): ");
        String novoNome = scanner.nextLine();
        if (novoNome.isBlank()) {
            throw new IllegalArgumentException("nome inválido!");
        }
        item.setNome(novoNome);
    }

    private void atualizarCategoriaItem(Item item) {
        System.out.print("Digite o id da categoria (" + item.getCategoria().getId() + "): ");
        item.setCategoria(facade.buscarCategoria(scanner.nextLine()));
    }

    private void atualizarFornecedorItem(Item item) {
        System.out.print("Digite o id do fornecedor (" + item.getFornecedor().getId() + "): ");
        item.setFornecedor(facade.buscarFornecedor(scanner.nextLine()));
    }

    private void deletarItem() {
        System.out.print("ID do Item a deletar: ");
        facade.deletarItem(scanner.nextLine());
        System.out.println("Item deletado do repositório.");
    }


    private void gerenciarCategorias() {
        System.out.println("\nGERENCIAR CATEGORIAS");
        System.out.println("1 - Cadastrar");
        System.out.println("2 - Listar");
        System.out.println("3 - Atualizar");
        System.out.println("4 - Deletar");
        System.out.print(OPCAO_PROMPT);
        String subOpcao = scanner.nextLine();

        try {
            switch (subOpcao) {
                case "1" -> cadastrarCategoria();
                case "2" -> listarCategorias();
                case "3" -> atualizarCategoria();
                case "4" -> deletarCategoria();
                default -> System.out.println(MSG_OPCAO_INVALIDA);
            }
        } catch (RuntimeException e) {
            logger.error("Falha ao gerenciar categorias: {}", e.getMessage(), e);
            System.out.println(MSG_ERRO + e.getMessage());
        }
    }

    private void cadastrarCategoria() {
        System.out.print("ID: ");
        String id = scanner.nextLine();
        System.out.print(LABEL_NOME);
        String nome = scanner.nextLine();
        facade.cadastrarCategoria(new Categoria(id, nome));
        System.out.println("Categoria criada!");
    }

    private void listarCategorias() {
        facade.listarCategoria().values()
                .forEach(c -> System.out.println("ID: " + c.getId() + SEPARADOR_NOME + c.getNome()));
    }

    private void atualizarCategoria() {
        System.out.print("ID: ");
        Categoria c = facade.buscarCategoria(scanner.nextLine());
        System.out.print("Novo Nome: ");
        c.setNome(scanner.nextLine());
        facade.atualizarCategoria(c);
        System.out.println("Categoria atualizada!");
    }

    private void deletarCategoria() {
        System.out.print("ID a deletar: ");
        facade.deletarCategoria(scanner.nextLine());
        System.out.println("Categoria removida.");
    }


    private void gerenciarFornecedores() {
        System.out.println("\nGERENCIAR FORNECEDORES");
        System.out.println("1 - Cadastrar");
        System.out.println("2 - Listar");
        System.out.println("3 - Atualizar");
        System.out.println("4 - Deletar");
        System.out.print(OPCAO_PROMPT);
        String subOpcao = scanner.nextLine();

        try {
            switch (subOpcao) {
                case "1" -> cadastrarFornecedor();
                case "2" -> listarFornecedores();
                case "3" -> atualizarFornecedor();
                case "4" -> deletarFornecedor();
                default -> System.out.println(MSG_OPCAO_INVALIDA);
            }
        } catch (RuntimeException e) {
            logger.error("Falha ao gerenciar fornecedores: {}", e.getMessage(), e);
            System.out.println(MSG_ERRO + e.getMessage());
        }
    }

    private void cadastrarFornecedor() {
        System.out.print("ID: ");
        String id = scanner.nextLine();
        System.out.print(LABEL_NOME);
        String nome = scanner.nextLine();
        System.out.print("CNPJ: ");
        String cnpj = scanner.nextLine();
        System.out.print("Telefone: ");
        String telefone = scanner.nextLine();

        facade.cadastrarFornecedor(new Fornecedor(id, nome, cnpj, telefone));
        System.out.println("Fornecedor criado!");
    }

    private void listarFornecedores() {
        facade.listarFornecedor().values()
                .forEach(f -> System.out.println("ID: " + f.getId() + SEPARADOR_NOME + f.getNome()
                        + " | CNPJ: " + f.getCnpj() + " | Telefone: " + f.getTelefone()));
    }

    private void atualizarFornecedor() {
        System.out.print("ID: ");
        Fornecedor f = facade.buscarFornecedor(scanner.nextLine());

        System.out.println(MSG_ATUALIZAR);
        System.out.println(OPCAO_NOME);
        System.out.println("2 - CNPJ");
        System.out.println("3 - Telefone");
        System.out.print(OPCAO_PROMPT);
        String escolha = scanner.nextLine();

        switch (escolha) {
            case "1" -> atualizarNomeFornecedor(f);
            case "2" -> atualizarCnpjFornecedor(f);
            case "3" -> atualizarTelefoneFornecedor(f);
            default -> throw new IllegalArgumentException("Opção inválida!");
        }

        facade.atualizarFornecedor(f);
        System.out.println("Fornecedor atualizado!");
    }

    private void atualizarNomeFornecedor(Fornecedor f) {
        System.out.print(NOME_PROMPT + f.getNome() + "): ");
        String novoNome = scanner.nextLine();
        if (novoNome.isBlank()) {
            throw new IllegalArgumentException("Nome inválido!");
        }
        f.setNome(novoNome);
    }

    private void atualizarCnpjFornecedor(Fornecedor f) {
        System.out.print("Novo CNPJ (" + f.getCnpj() + "): ");
        String novoCnpj = scanner.nextLine();
        if (novoCnpj.isBlank()) {
            throw new IllegalArgumentException("CNPJ inválido!");
        }
        f.setCnpj(novoCnpj);
    }

    private void atualizarTelefoneFornecedor(Fornecedor f) {
        System.out.print("Novo Telefone (" + f.getTelefone() + "): ");
        String novoTelefone = scanner.nextLine();
        if (novoTelefone.isBlank()) {
            throw new IllegalArgumentException("Telefone inválido!");
        }
        f.setTelefone(novoTelefone);
    }

    private void deletarFornecedor() {
        System.out.print("ID a deletar: ");
        facade.deletarFornecedor(scanner.nextLine());
        System.out.println("Fornecedor removido.");
    }


    private void emitirRelatorios() {
        System.out.println("\nEMITIR RELATÓRIOS");
        System.out.println("1 - Itens Disponíveis");
        System.out.println("2 - Aluguéis Atuais (Ativos)");
        System.out.println("3 - Aluguel de um Cliente (Histórico)");
        System.out.println("4 - Financeiro (Faturamento)");
        System.out.print(OPCAO_PROMPT);
        String subOpcao = scanner.nextLine();

        switch (subOpcao) {
            case "1" -> relatorioItensDisponiveis();
            case "2" -> relatorioAlugueisAtivos();
            case "3" -> relatorioContratosPorCliente();
            case "4" -> relatorioFaturamento();
            default -> System.out.println(MSG_OPCAO_INVALIDA);
        }
    }

    private void relatorioItensDisponiveis() {
        System.out.println("\nITENS DISPONÍVEIS");
        try {
            Map<String, Item> itens = facade.listarItensDisponiveis();
            if (itens.isEmpty()) {
                System.out.println("Não há itens disponíveis para aluguel no momento.");
            } else {
                for (Item item : itens.values()) {
                    System.out.println("ID: " + item.getId() + SEPARADOR_NOME + item.getNome() + " | Valor Diário: " + item.getTaxaDiaria());
                }
            }
        } catch (RuntimeException e) {
            logger.error("Falha ao listar itens disponíveis: {}", e.getMessage(), e);
            System.out.println("Erro ao listar itens: " + e.getMessage());
        }
    }

    private void relatorioAlugueisAtivos() {
        System.out.println("\nRELATÓRIO DE CONTRATOS ATIVOS");
        try {
            String relatorio = facade.gerarRelatorioItensAlugados();
            System.out.println(relatorio);
        } catch (RuntimeException e) {
            logger.error("Falha ao gerar relatório de itens alugados: {}", e.getMessage(), e);
            System.out.println("Erro ao gerar relatório: " + e.getMessage());
        }
    }

    private void relatorioContratosPorCliente() {
        System.out.println("\nRELATÓRIO DE CONTRATOS POR CLIENTE");
        System.out.print("ID do Cliente: ");
        String clienteId = scanner.nextLine();
        try {
            Map<String, ContratoAluguel> contratos = facade.consultarHistoricoCliente(clienteId);
            if (contratos.isEmpty()) {
                System.out.println("Não há histórico de contratos para esse cliente.");
            } else {
                for (ContratoAluguel c : contratos.values()) {
                    System.out.println("ID: " + c.getId() + " | Item: " + c.getItem().getNome() + " | Valor total: " + c.getValorTotal() + " | Status: " + c.getStatus() + " | Devolução prevista: " + c.getDataPrevDevolucao());
                }
            }
        } catch (RuntimeException e) {
            logger.error("Falha ao listar contratos do cliente '{}': {}", clienteId, e.getMessage(), e);
            System.out.println("Erro ao listar contratos: " + e.getMessage());
        }
    }

    private void relatorioFaturamento() {
        System.out.println("\nRELATÓRIO FINANCEIRO");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        LocalDate ini = lerDataValida("Data Inicial (dd/MM/yyyy): ", formatter);
        LocalDate fim = lerDataValida("Data Final (dd/MM/yyyy): ", formatter);

        while (fim.isBefore(ini)) {
            System.out.println("Data final não pode ser anterior à data inicial.");
            fim = lerDataValida("Data Final (dd/MM/yyyy): ", formatter);
        }

        try {
            System.out.println(facade.gerarRelatorioFaturamento(ini, fim));
        } catch (RuntimeException e) {
            logger.error("Falha ao gerar relatório de faturamento ({} a {}): {}", ini, fim, e.getMessage(), e);
            System.out.println("Erro ao gerar relatório: " + e.getMessage());
        }
    }

    private LocalDate lerDataValida(String prompt, DateTimeFormatter formatter) {
        while (true) {
            try {
                System.out.print(prompt);
                return LocalDate.parse(scanner.nextLine(), formatter);
            } catch (DateTimeParseException e) {
                System.out.println("Data inválida, tente novamente.");
            }
        }
    }
}