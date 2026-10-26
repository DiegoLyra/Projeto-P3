package com.loja.business;

import com.loja.exceptions.UsuarioException;
import com.loja.model.Administrador;
import com.loja.model.Cliente;
import com.loja.model.Funcionario;
import com.loja.model.Usuario;
import com.loja.repositories.UsuarioRepositoryFake;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioBusinessTest {

    private static final String SENHA_OK = "Senha@123";
    private static final String SENHA_NOVA = "Outra@456";

    private UsuarioRepositoryFake repositorio;
    private UsuarioBusiness business;
    private Administrador admin;

    @BeforeEach
    void setUp() {
        repositorio = new UsuarioRepositoryFake();
        business = new UsuarioBusiness(repositorio);

        // Sessão padrão dos testes: administrador autenticado (ID fora do padrão USR###)
        admin = new Administrador("ADM01", "Admin", "admin@loja.com", SENHA_OK);
        admin.setAtivo(true);
        repositorio.salvar(admin);
        business.autenticar("admin@loja.com", SENHA_OK);
    }

    @Test
    @DisplayName("cadastrar: deve salvar usuário quando login não existe")
    void cadastrar_deveSalvar_quandoLoginNaoExiste() {
        Cliente cliente = new Cliente("1", "João", "joao@email.com", SENHA_OK);

        business.cadastrar(cliente);

        assertNotNull(repositorio.buscar("1"));
    }

    @Test
    @DisplayName("cadastrar: deve lançar exceção quando login já está cadastrado")
    void cadastrar_deveLancarExcecao_quandoLoginJaExiste() {
        business.cadastrar(new Cliente("1", "João", "joao@email.com", SENHA_OK));
        Cliente duplicado = new Cliente("2", "João 2", "joao@email.com", SENHA_NOVA);

        assertThrows(RuntimeException.class, () -> business.cadastrar(duplicado));
        assertNull(repositorio.buscar("2"));
    }

    @Test
    @DisplayName("cadastrar: deve permitir logins diferentes")
    void cadastrar_devePermitir_loginsDiferentes() {
        business.cadastrar(new Cliente("1", "João", "joao@email.com", SENHA_OK));
        business.cadastrar(new Cliente("2", "Maria", "maria@email.com", SENHA_NOVA));

        assertEquals(3, business.listar().size()); // 2 clientes + admin da sessão
    }

    @Test
    @DisplayName("buscarPorId: deve retornar usuário quando id existe")
    void buscarPorId_deveRetornarUsuario_quandoIdExiste() {
        business.cadastrar(new Cliente("1", "João", "joao@email.com", SENHA_OK));

        Usuario resultado = business.buscarPorId("1");

        assertEquals("João", resultado.getNome());
    }

    @Test
    @DisplayName("buscarPorId: deve lançar exceção quando id não existe")
    void buscarPorId_deveLancarExcecao_quandoIdNaoExiste() {
        assertThrows(RuntimeException.class, () -> business.buscarPorId("99"));
    }

    @Test
    @DisplayName("buscarPorEmail: deve retornar usuário quando email existe")
    void buscarPorEmail_deveRetornarUsuario_quandoEmailExiste() {
        business.cadastrar(new Cliente("1", "João", "joao@email.com", SENHA_OK));

        Usuario resultado = business.buscarPorEmail("joao@email.com");

        assertNotNull(resultado);
        assertEquals("1", resultado.getId());
    }

    @Test
    @DisplayName("buscarPorEmail: deve lançar exceção quando email não existe")
    void buscarPorEmail_deveLancarExcecao_quandoEmailNaoExiste() {
        assertThrows(RuntimeException.class,
                () -> business.buscarPorEmail("naoexiste@email.com"));
    }

    @Test
    @DisplayName("autenticar: deve retornar usuário quando credenciais corretas")
    void autenticar_deveRetornarUsuario_quandoCredenciaisCorretas() {
        business.cadastrar(new Cliente("1", "João", "joao@email.com", SENHA_OK));

        Usuario resultado = business.autenticar("joao@email.com", SENHA_OK);

        assertNotNull(resultado);
        assertEquals("João", resultado.getNome());
    }

    @Test
    @DisplayName("autenticar: deve lançar exceção quando senha está errada")
    void autenticar_deveLancarExcecao_quandoSenhaErrada() {
        business.cadastrar(new Cliente("1", "João", "joao@email.com", SENHA_OK));

        assertThrows(RuntimeException.class,
                () -> business.autenticar("joao@email.com", "senhaErrada"));
    }

    @Test
    @DisplayName("autenticar: deve lançar exceção quando email não existe")
    void autenticar_deveLancarExcecao_quandoEmailNaoExiste() {
        assertThrows(RuntimeException.class,
                () -> business.autenticar("naoexiste@email.com", SENHA_OK));
    }

    @Test
    @DisplayName("autenticar: deve ser case-insensitive no email")
    void autenticar_deveFuncionar_comEmailEmCaseDiferente() {
        business.cadastrar(new Cliente("1", "João", "joao@email.com", SENHA_OK));

        Usuario resultado = business.autenticar("JOAO@EMAIL.COM", SENHA_OK);

        assertNotNull(resultado);
    }

    @Test
    @DisplayName("atualizar: deve alterar nome, login e senha")
    void atualizar_deveAlterarDados_quandoUsuarioExiste() {
        business.cadastrar(new Cliente("1", "João", "joao@email.com", SENHA_OK));

        business.atualizar(new Cliente("1", "João Novo", "novo@email.com", SENHA_NOVA));

        Usuario atualizado = repositorio.buscar("1");
        assertEquals("João Novo", atualizado.getNome());
        assertEquals("novo@email.com", atualizado.getLogin());
        assertEquals(SENHA_NOVA, atualizado.getSenha());
    }

    @Test
    @DisplayName("atualizar: deve lançar exceção quando usuário não existe")
    void atualizar_deveLancarExcecao_quandoUsuarioNaoExiste() {
        Cliente clienteInexistente = new Cliente("99", "X", "x@x.com", "x");

        assertThrows(RuntimeException.class, () -> business.atualizar(clienteInexistente));
    }

    @Test
    @DisplayName("deletar: deve remover usuário existente")
    void deletar_deveRemoverUsuario_quandoExiste() {
        business.cadastrar(new Cliente("1", "João", "joao@email.com", SENHA_OK));

        business.deletar("1");

        assertNull(repositorio.buscar("1"));
    }

    @Test
    @DisplayName("deletar: deve lançar exceção quando usuário não existe")
    void deletar_deveLancarExcecao_quandoUsuarioNaoExiste() {
        assertThrows(RuntimeException.class, () -> business.deletar("99"));
    }

    @Test
    @DisplayName("listar: deve retornar todos os usuários cadastrados")
    void listar_deveRetornarTodosOsUsuarios() {
        business.cadastrar(new Cliente("1", "João", "joao@email.com", SENHA_OK));
        business.cadastrar(new Administrador("2", "Carlos", "carlos@email.com", SENHA_OK, 1, "TI"));

        Map<String, Usuario> resultado = business.listar();

        assertEquals(3, resultado.size()); // cliente + administrador + admin da sessão
    }

    @Test
    @DisplayName("listar: deve conter apenas o administrador da sessão quando nada foi cadastrado")
    void listar_deveRetornarSoAdminDaSessao_quandoNenhumCadastrado() {
        assertEquals(1, business.listar().size());
    }

    @Test
    @DisplayName("listarPorPerfil: deve retornar apenas clientes")
    void listarPorPerfil_deveRetornarApenasClientes() {
        business.cadastrar(new Cliente("1", "João", "joao@email.com", SENHA_OK));
        business.cadastrar(new Cliente("2", "Maria", "maria@email.com", SENHA_NOVA));
        business.cadastrar(new Administrador("3", "Carlos", "carlos@email.com", SENHA_OK, 1, "TI"));

        Map<String, Usuario> clientes = business.listarPorPerfil("CLIENTE");

        assertEquals(2, clientes.size());
        assertTrue(clientes.containsKey("1"));
        assertTrue(clientes.containsKey("2"));
        assertFalse(clientes.containsKey("3"));
    }

    @Test
    @DisplayName("listarPorPerfil: deve retornar apenas funcionários")
    void listarPorPerfil_deveRetornarApenasFuncionarios() {
        business.cadastrar(new Funcionario("1", "Ana", "ana@email.com", SENHA_OK, "Caixa"));
        business.cadastrar(new Cliente("2", "João", "joao@email.com", SENHA_NOVA));

        Map<String, Usuario> funcionarios = business.listarPorPerfil("FUNCIONARIO");

        assertEquals(1, funcionarios.size());
        assertTrue(funcionarios.containsKey("1"));
    }

    @Test
    @DisplayName("listarPorPerfil: deve retornar vazio quando perfil não tem usuários")
    void listarPorPerfil_deveRetornarVazio_quandoNenhumNoPerfil() {
        business.cadastrar(new Cliente("1", "João", "joao@email.com", SENHA_OK));

        Map<String, Usuario> funcionarios = business.listarPorPerfil("FUNCIONARIO");

        assertTrue(funcionarios.isEmpty());
    }

    @Test
    
    @DisplayName("cadastrar: deve rejeitar e-mail inválido e senha fraca")

    
    void cadastrar_deveRejeitar_emailInvalidoOuSenhaFraca() {
        Cliente emailInvalido =
                new Cliente("1", "A", "invalido", SENHA_OK);

        Cliente senhaFraca =
                new Cliente("2", "A", "a@b.com", "fraca");

        assertThrows(RuntimeException.class,
                () -> business.cadastrar(emailInvalido));

        assertThrows(RuntimeException.class,
                () -> business.cadastrar(senhaFraca));

        assertNull(repositorio.buscar("1"));
        assertNull(repositorio.buscar("2"));
    }

    @Test
    @DisplayName("cadastrar: deve ativar o usuário e gerar id quando ausente")
    void cadastrar_deveAtivarEGerarId() {
        Cliente c = new Cliente(null, "João", "joao@email.com", SENHA_OK);
        business.cadastrar(c);

        assertTrue(c.isAtivo());
        assertEquals("USR001", c.getId());
    }

    @Test
    @DisplayName("cadastrarCliente: deve criar cliente ativo com próximo id sequencial")
    void cadastrarCliente_deveCriarClienteAtivo() {
        business.cadastrar(new Cliente("USR005", "X", "x@email.com", SENHA_OK));

        Cliente c = business.cadastrarCliente(" Maria ", " maria@email.com ", SENHA_OK);

        assertEquals("USR006", c.getId());
        assertEquals("Maria", c.getNome());
        assertEquals("maria@email.com", c.getLogin());
        assertTrue(c.isAtivo());
        assertNotNull(repositorio.buscar("USR006"));
    }

    @Test
    @DisplayName("cadastrarCliente: deve rejeitar nome em branco")
    void cadastrarCliente_deveRejeitarNomeEmBranco() {
        assertThrows(RuntimeException.class, () -> business.cadastrarCliente("  ", "a@b.com", SENHA_OK));
        assertThrows(RuntimeException.class, () -> business.cadastrarCliente(null, "a@b.com", SENHA_OK));
    }

    @Test
    @DisplayName("desativar: deve marcar usuário como inativo")
    void desativar_deveMarcarInativo() {
        business.cadastrar(new Cliente("1", "João", "joao@email.com", SENHA_OK));

        business.desativar("1");

        assertFalse(repositorio.buscar("1").isAtivo());
    }

    @Test
    @DisplayName("desativar: deve lançar exceção quando usuário não existe")
    void desativar_deveLancarExcecao_quandoNaoExiste() {
        assertThrows(RuntimeException.class, () -> business.desativar("999"));
    }

    @Test
    @DisplayName("atualizar: deve rejeitar login já usado por outro usuário")
    void atualizar_deveRejeitarLoginDuplicado() {
        business.cadastrar(new Cliente("1", "João", "joao@email.com", SENHA_OK));
        business.cadastrar(new Cliente("2", "Maria", "maria@email.com", SENHA_OK));

        Cliente clienteDuplicado =
                new Cliente("2", "Maria", "joao@email.com", SENHA_OK);

        assertThrows(RuntimeException.class,
                () -> business.atualizar(clienteDuplicado));

        assertEquals("maria@email.com",
                repositorio.buscar("2").getLogin());
    }

    @Test
    @DisplayName("atualizar: não deve revalidar senha legada quando ela não mudou")
    void atualizar_naoRevalidaSenhaLegada() {
        Cliente legado = new Cliente("1", "João", "joao@email.com", "123");
        repositorio.salvar(legado);

        business.atualizar(new Cliente("1", "João Novo", "joao@email.com", "123"));

        assertEquals("João Novo", repositorio.buscar("1").getNome());
    }

    /* ------------------------ autorização e sessão ------------------------ */

    @Test
    @DisplayName("autenticar: deve recusar conta desativada, só depois de validar a senha")
    void autenticar_deveRecusarContaDesativada() {
        business.cadastrar(new Cliente("1", "João", "joao@email.com", SENHA_OK));
        business.desativar("1");
        business.encerrarSessao();

        UsuarioException semSenha = assertThrows(UsuarioException.class,
                () -> business.autenticar("joao@email.com", "errada"));
        assertEquals("Email ou senha inválidos.", semSenha.getMessage());

        UsuarioException desativada = assertThrows(UsuarioException.class,
                () -> business.autenticar("joao@email.com", SENHA_OK));
        assertTrue(desativada.getMessage().contains("desativada"));
        assertNull(business.getUsuarioAtual());
    }

    @Test
    @DisplayName("autenticar/encerrarSessao: deve guardar e limpar o usuário da sessão")
    void sessao_deveGuardarELimpar() {
        assertSame(admin, business.getUsuarioAtual());

        business.encerrarSessao();

        assertNull(business.getUsuarioAtual());
    }

    @Test
    @DisplayName("cadastrar: funcionário e administrador exigem administrador logado; cliente não")
    void cadastrar_deveExigirAdminParaPerfisPrivilegiados() {
        business.encerrarSessao();

        assertDoesNotThrow(() -> business.cadastrar(new Cliente("1", "João", "joao@email.com", SENHA_OK)));
        assertThrows(UsuarioException.class,
                () -> business.cadastrar(new Funcionario("2", "Ana", "ana@email.com", SENHA_OK, "Caixa")));
        assertThrows(UsuarioException.class,
                () -> business.cadastrar(new Administrador("3", "Eve", "eve@email.com", SENHA_OK)));
        assertNull(repositorio.buscar("2"));
        assertNull(repositorio.buscar("3"));
    }

    @Test
    @DisplayName("listar, listarPorPerfil, desativar e deletar: negados sem administrador logado")
    void operacoesAdministrativas_negadasSemAdmin() {
        business.cadastrar(new Cliente("1", "João", "joao@email.com", SENHA_OK));
        business.encerrarSessao();

        assertThrows(UsuarioException.class, () -> business.listar());
        assertThrows(UsuarioException.class, () -> business.listarPorPerfil("CLIENTE"));
        assertThrows(UsuarioException.class, () -> business.desativar("1"));
        assertThrows(UsuarioException.class, () -> business.deletar("1"));
        assertTrue(repositorio.buscar("1").isAtivo());
    }

    @Test
    @DisplayName("desativar e deletar: administrador não pode agir sobre a própria conta")
    void administradorNaoPodeDesativarNemRemoverASiMesmo() {
        assertThrows(UsuarioException.class, () -> business.desativar("ADM01"));
        assertThrows(UsuarioException.class, () -> business.deletar("ADM01"));
        assertTrue(repositorio.buscar("ADM01").isAtivo());
    }

    @Test
    @DisplayName("atualizar: cliente pode editar a si mesmo, mas não outro usuário")
    void atualizar_clienteSoEditaASiMesmo() {
        business.cadastrar(new Cliente("1", "João", "joao@email.com", SENHA_OK));
        business.cadastrar(new Cliente("2", "Maria", "maria@email.com", SENHA_OK));
        business.autenticar("joao@email.com", SENHA_OK);

        assertDoesNotThrow(() -> business.atualizar(new Cliente("1", "João Novo", "joao@email.com", SENHA_OK)));
        assertThrows(UsuarioException.class,
                () -> business.atualizar(new Cliente("2", "Hack", "maria@email.com", SENHA_OK)));
        assertEquals("Maria", repositorio.buscar("2").getNome());
    }

    /* ------------------------- atualizar sem atalhos ------------------------- */

    @Test
    @DisplayName("atualizar: deve recusar o próprio objeto persistido (evita pular validações)")
    void atualizar_deveRecusarObjetoPersistido() {
        business.cadastrar(new Cliente("1", "João", "joao@email.com", SENHA_OK));
        Usuario vivo = business.buscarPorId("1");
        vivo.setLogin("invalido"); // alteração direta, como a tela fazia antes

        assertThrows(UsuarioException.class, () -> business.atualizar(vivo));
    }

    @Test
    @DisplayName("atualizar: deve validar e-mail e senha novos quando recebe uma cópia")
    void atualizar_deveValidarCopia() {
            business.cadastrar(
                    new Cliente("1", "João", "joao@email.com", SENHA_OK));

            Cliente emailInvalido =
                    new Cliente("1", "João", "invalido", SENHA_OK);

            Cliente senhaFraca =
                    new Cliente("1", "João", "joao@email.com", "fraca");

            assertThrows(UsuarioException.class,
                    () -> business.atualizar(emailInvalido));

            assertThrows(UsuarioException.class,
                    () -> business.atualizar(senhaFraca));

            assertEquals("joao@email.com",
                    repositorio.buscar("1").getLogin());

            assertEquals(SENHA_OK,
                    repositorio.buscar("1").getSenha());
        }
    

    @Test
    @DisplayName("atualizar: deve copiar cargo do funcionário e recusar troca de perfil")
    void atualizar_deveCopiarCargoERecusarTrocaDePerfil() {
        business.cadastrar(new Funcionario("1", "Ana", "ana@email.com", SENHA_OK, "Caixa"));

        business.atualizar(new Funcionario("1", "Ana", "ana@email.com", SENHA_OK, "Gerente"));

        assertEquals("Gerente", ((Funcionario) repositorio.buscar("1")).getCargo());
        assertThrows(UsuarioException.class,
                () -> business.atualizar(new Cliente("1", "Ana", "ana@email.com", SENHA_OK)));
    }

    @Test
    @DisplayName("atualizar: deve rejeitar nome em branco")
    void atualizar_deveRejeitarNomeEmBranco() {
        business.cadastrar(new Cliente("1", "João", "joao@email.com", SENHA_OK));

        assertThrows(UsuarioException.class,
                () -> business.atualizar(new Cliente("1", "  ", "joao@email.com", SENHA_OK)));
    }

    /* ------------------------------- IDs ------------------------------- */

    @Test
    @DisplayName("gerar id: não deve reaproveitar o id de um usuário removido")
    void id_naoDeveSerReaproveitado() {
        Cliente a = business.cadastrarCliente("A", "a@email.com", SENHA_OK);
        Cliente b = business.cadastrarCliente("B", "b@email.com", SENHA_OK);

        business.deletar(b.getId());
        Cliente c = business.cadastrarCliente("C", "c@email.com", SENHA_OK);

        assertEquals("USR001", a.getId());
        assertEquals("USR002", b.getId());
        assertEquals("USR003", c.getId());
    }

    @Test
    @DisplayName("registrarHistorico: deve marcar o cliente, sem exigir sessão")
    void registrarHistorico_deveMarcarCliente() {
        Cliente c = business.cadastrarCliente("A", "a@email.com", SENHA_OK);
        business.encerrarSessao();

        business.registrarHistorico(c.getId());

        assertTrue(c.hasHistorico());
    }
}