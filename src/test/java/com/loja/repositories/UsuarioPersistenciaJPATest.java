package com.loja.repositories;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.loja.exceptions.PersistenciaException;
import com.loja.model.Administrador;
import com.loja.model.Cliente;
import com.loja.model.Funcionario;
import com.loja.model.Usuario;

@Disabled("Aguardando as anotações JPA de Cliente, Funcionario e Administrador")
class UsuarioPersistenciaJPATest {

    private UsuarioPersistenciaJPA repo;

    @BeforeAll
    static void usarBancoDeTeste() {
        // Garante o banco de teste também quando o teste roda pelo VS Code (sem o surefire)
        System.setProperty("loja.db.url", "jdbc:sqlite:target/teste-loja.db?foreign_keys=true");    }

    @BeforeEach
    void limparBanco() {
        repo = new UsuarioPersistenciaJPA();
        // Cada teste começa com o banco vazio
        for (String id : repo.listar().keySet()) {
            repo.deletar(id);
        }
    }

    @Test
    @DisplayName("salvar: deve gravar no banco (outra instância do repositório encontra)")
    void salvar_deveGravarNoBanco() {
        repo.salvar(new Cliente("USR001", "João", "joao@email.com", "123"));

        UsuarioPersistenciaJPA outroRepo = new UsuarioPersistenciaJPA();
        Usuario encontrado = outroRepo.buscar("USR001");

        assertNotNull(encontrado);
        assertEquals("João", encontrado.getNome());
    }

    @Test
    @DisplayName("salvar: deve guardar o id em maiúsculas")
    void salvar_deveGuardarIdEmMaiusculas() {
        repo.salvar(new Cliente("usr009", "João", "joao@email.com", "123"));

        assertNotNull(repo.buscar("USR009"));
        assertTrue(repo.listar().containsKey("USR009"));
    }

    @Test
    @DisplayName("salvar: deve lançar PersistenciaException quando o id já existe")
    void salvar_deveLancarExcecao_quandoIdJaExiste() {
        repo.salvar(new Cliente("USR001", "João", "joao@email.com", "123"));
        Cliente duplicado = new Cliente("USR001", "Outro", "outro@email.com", "456");

        try {
            repo.salvar(duplicado);
            fail("Deveria lançar PersistenciaException para id duplicado");
        } catch (PersistenciaException e) {
            assertEquals("João", repo.buscar("USR001").getNome());
        }
    }

    @Test
    @DisplayName("buscar: deve retornar null quando id não existe")
    void buscar_deveRetornarNull_quandoIdNaoExiste() {
        assertNull(repo.buscar("INEXISTENTE"));
    }

    @Test
    @DisplayName("buscar: deve devolver o tipo certo de cada perfil")
    void buscar_deveDevolverTipoCerto() {
        repo.salvar(new Cliente("USR001", "João", "joao@email.com", "123"));
        repo.salvar(new Administrador("USR002", "Carlos", "carlos@email.com", "abc", 2, "TI"));
        repo.salvar(new Funcionario("USR003", "Ana", "ana@email.com", "456", "Caixa"));

        assertInstanceOf(Cliente.class, repo.buscar("USR001"));
        assertInstanceOf(Administrador.class, repo.buscar("USR002"));
        assertInstanceOf(Funcionario.class, repo.buscar("USR003"));
    }

    @Test
    @DisplayName("buscarPorEmail: deve encontrar usuário ignorando maiúsculas/minúsculas")
    void buscarPorEmail_deveIgnorarCase() {
        repo.salvar(new Cliente("USR001", "João", "joao@email.com", "123"));

        Usuario resultado = repo.buscarPorEmail("JOAO@EMAIL.COM");

        assertNotNull(resultado);
        assertEquals("USR001", resultado.getId());
    }

    @Test
    @DisplayName("buscarPorEmail: deve retornar null quando email não existe")
    void buscarPorEmail_deveRetornarNull_quandoEmailNaoExiste() {
        assertNull(repo.buscarPorEmail("naoexiste@email.com"));
    }

    @Test
    @DisplayName("atualizar: deve gravar as alterações no banco e retornar true")
    void atualizar_deveGravar_quandoUsuarioExiste() {
        repo.salvar(new Cliente("USR001", "João", "joao@email.com", "123"));

        boolean resultado = repo.atualizar(new Cliente("USR001", "João Novo", "novo@email.com", "456"));

        Usuario atualizado = new UsuarioPersistenciaJPA().buscar("USR001");
        assertTrue(resultado);
        assertEquals("João Novo", atualizado.getNome());
        assertEquals("novo@email.com", atualizado.getLogin());
    }

    @Test
    @DisplayName("atualizar: deve retornar false quando usuário não existe")
    void atualizar_deveRetornarFalse_quandoUsuarioNaoExiste() {
        assertFalse(repo.atualizar(new Cliente("USR099", "X", "x@email.com", "x")));
        assertNull(repo.buscar("USR099"));
    }

    @Test
    @DisplayName("deletar: deve remover usuário existente e retornar true")
    void deletar_deveRemover_quandoUsuarioExiste() {
        repo.salvar(new Administrador("USR001", "Carlos", "carlos@email.com", "abc", 1, "TI"));

        assertTrue(repo.deletar("USR001"));
        assertNull(repo.buscar("USR001"));
    }

    @Test
    @DisplayName("deletar: deve retornar false quando usuário não existe")
    void deletar_deveRetornarFalse_quandoUsuarioNaoExiste() {
        assertFalse(repo.deletar("INEXISTENTE"));
    }

    @Test
    @DisplayName("listar: deve retornar todos os usuários, de todos os perfis")
    void listar_deveRetornarTodos() {
        repo.salvar(new Cliente("USR001", "João", "joao@email.com", "123"));
        repo.salvar(new Administrador("USR002", "Carlos", "carlos@email.com", "abc", 1, "TI"));
        repo.salvar(new Funcionario("USR003", "Ana", "ana@email.com", "456", "Caixa"));

        assertEquals(3, repo.listar().size());
    }

    @Test
    @DisplayName("listar(perfil): deve filtrar apenas o perfil solicitado")
    void listarPorPerfil_deveFiltrarCorretamente() {
        repo.salvar(new Cliente("USR001", "João", "joao@email.com", "123"));
        repo.salvar(new Funcionario("USR002", "Ana", "ana@email.com", "456", "Caixa"));

        Map<String, Usuario> resultado = repo.listar("CLIENTE");

        assertEquals(1, resultado.size());
        assertTrue(resultado.containsKey("USR001"));
    }

    @Test
    @DisplayName("listar(perfil): deve aceitar o perfil em minúsculas")
    void listarPorPerfil_deveIgnorarCase() {
        repo.salvar(new Funcionario("USR001", "Ana", "ana@email.com", "456", "Caixa"));

        assertEquals(1, repo.listar("funcionario").size());
    }

    @Test
    @DisplayName("listar(perfil): deve retornar vazio para perfil desconhecido")
    void listarPorPerfil_deveRetornarVazio_quandoPerfilDesconhecido() {
        repo.salvar(new Cliente("USR001", "João", "joao@email.com", "123"));

        assertTrue(repo.listar("GERENTE").isEmpty());
    }

    @Test
    @DisplayName("Cliente: deve preservar inadimplente e ativo no banco")
    void cliente_devePreservarCampos() {
        Cliente cliente = new Cliente("USR001", "João", "joao@email.com", "123");
        cliente.setInadimplente(true);
        cliente.setAtivo(true);
        repo.salvar(cliente);

        Cliente recarregado = assertInstanceOf(Cliente.class, new UsuarioPersistenciaJPA().buscar("USR001"));

        assertTrue(recarregado.isInadimplente());
        assertTrue(recarregado.isAtivo());
    }

    @Test
    @DisplayName("Administrador: deve preservar nivelAcesso e departamento no banco")
    void administrador_devePreservarCampos() {
        repo.salvar(new Administrador("USR001", "Carlos", "carlos@email.com", "abc", 5, "Financeiro"));

        Administrador recarregado =
                assertInstanceOf(Administrador.class, new UsuarioPersistenciaJPA().buscar("USR001"));

        assertEquals(5, recarregado.getNivelAcesso());
        assertEquals("Financeiro", recarregado.getDepartamento());
    }

    @Test
    @DisplayName("Funcionario: deve preservar cargo no banco")
    void funcionario_devePreservarCargo() {
        repo.salvar(new Funcionario("USR001", "Ana", "ana@email.com", "456", "Gerente"));

        Funcionario recarregado =
                assertInstanceOf(Funcionario.class, new UsuarioPersistenciaJPA().buscar("USR001"));

        assertEquals("Gerente", recarregado.getCargo());
    }
}