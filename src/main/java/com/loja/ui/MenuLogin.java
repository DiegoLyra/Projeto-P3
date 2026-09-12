package com.loja.ui;

import com.loja.model.Administrador;
import com.loja.model.Cliente;
import com.loja.model.Funcionario;
import com.loja.model.Usuario;
import com.loja.padraoFacade.interfaces.ILojaFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class MenuLogin {

    private static final Logger logger = LoggerFactory.getLogger(MenuLogin.class);

    private final ILojaFacade facade;
    private final Scanner scanner;

    public MenuLogin(ILojaFacade facade){
        this.facade = facade;
        this.scanner = new Scanner(System.in);
    }

    public void iniciar(){
        boolean rodando = true;
        while (rodando) {
            System.out.println("\n=== BEM-VINDO À LOJA QUE ALUGA DE UM TUDO ==="); // NOSONAR
            System.out.println("1 - Login"); // NOSONAR
            System.out.println("0 - Sair"); // NOSONAR
            System.out.print("Escolha uma opção: "); // NOSONAR

            String opcao = scanner.nextLine();

            switch (opcao) {
                case "1" -> exibirMenuLogin();
                case "0" -> {
                    scanner.close();
                    facade.salvarTudo();
                    System.out.println("Encerrando o sistema..."); // NOSONAR
                    rodando = false;
                }
                default -> System.out.println("Opção inválida!"); // NOSONAR
            }
        }
    }

    private void exibirMenuLogin() {
        System.out.println("\n=== LOGIN ==="); // NOSONAR
        System.out.print("E-mail: "); // NOSONAR
        String email = scanner.nextLine();
        System.out.print("Senha: "); // NOSONAR
        String senha = scanner.nextLine();

        try {
            Usuario usuario = facade.autenticarUsuario(email, senha);
            if (usuario == null) {
                System.out.println("E-mail ou senha incorretos."); // NOSONAR
            } else {
                redirecionar(usuario);
            }
        } catch (RuntimeException e) {
            logger.error("Falha ao autenticar usuário com e-mail '{}': {}", email, e.getMessage(), e);
            System.out.println("Erro ao autenticar: " + e.getMessage()); // NOSONAR
        }
    }

    private void redirecionar(Usuario usuario) {
        if (usuario instanceof Administrador adm) {
            new MenuAdmin(facade, adm, scanner).exibir();
        } else if (usuario instanceof Funcionario func) {
            new MenuFuncionario(facade, func, scanner).exibir();
        } else if (usuario instanceof Cliente cliente) {
            new MenuCliente(facade, cliente, scanner).exibir();
        }
    }
}