package com.loja.business;

import com.loja.business.interfaces.IUsuarioBusiness;
import com.loja.exceptions.UsuarioException;
import com.loja.model.Administrador;
import com.loja.model.Cliente;
import com.loja.model.Funcionario;
import com.loja.model.Usuario;
import com.loja.repositories.interfaces.IUsuarioRepository;

import java.util.Map;
import java.util.Objects;

public class UsuarioBusiness implements IUsuarioBusiness {

    private final IUsuarioRepository usuarioRepository;

    private Usuario usuarioAtual;

    private int ultimoSequencial = 0;

    public UsuarioBusiness(IUsuarioRepository repository) {
        this.usuarioRepository = repository;
    }

    /* ----------------------------- cadastro ----------------------------- */

    @Override
    public void cadastrar(Usuario usuario) {
        // Qualquer pessoa pode criar a própria conta de cliente; os demais perfis exigem administrador.
        if (!(usuario instanceof Cliente)) {
            exigirAdministrador();
        }
        exigirNome(usuario.getNome());
        UsuarioValidador.validarEmail(usuario.getLogin());
        UsuarioValidador.validarSenha(usuario.getSenha());
        if (usuarioRepository.buscarPorEmail(usuario.getLogin()) != null) {
            throw new UsuarioException("Login já cadastrado: " + usuario.getLogin());
        }
        if (usuario.getId() == null || usuario.getId().isBlank()) {
            usuario.setId(gerarProximoId());
        }
        usuario.setAtivo(true);
        usuarioRepository.salvar(usuario);
    }

    @Override
    public Cliente cadastrarCliente(String nome, String email, String senha) {
        exigirNome(nome);
        Cliente cliente = new Cliente(null, nome.trim(), email == null ? null : email.trim(), senha);
        cadastrar(cliente);
        return cliente;
    }

    private synchronized String gerarProximoId() {
        int maiorExistente = usuarioRepository.listar().keySet().stream()
                .filter(id -> id != null && id.matches("(?i)USR\\d+"))
                .mapToInt(id -> Integer.parseInt(id.substring(3)))
                .max()
                .orElse(0);
        ultimoSequencial = Math.max(ultimoSequencial, maiorExistente) + 1;
        return String.format("USR%03d", ultimoSequencial);
    }

    /* ----------------------------- consultas ---------------------------- */

    @Override
    public Usuario buscarPorId(String id) {
        Usuario usuario = usuarioRepository.buscar(id);
        if (usuario == null) {
            throw new UsuarioException("Usuário não encontrado: " + id);
        }
        return usuario;
    }

    @Override
    public Usuario buscarPorEmail(String email) {
        Usuario usuario = usuarioRepository.buscarPorEmail(email);
        if (usuario == null) {
            throw new UsuarioException("Usuário não encontrado: " + email);
        }
        return usuario;
    }

    @Override
    public Map<String, Usuario> listar() {
        exigirAdministrador();
        return usuarioRepository.listar();
    }

    @Override
    public Map<String, Usuario> listarPorPerfil(String perfil) {
        exigirAdministrador();
        return usuarioRepository.listar(perfil);
    }

    @Override
    public void atualizar(Usuario dados) {
        if (dados.getId() == null) {
            throw new UsuarioException("Usuário não encontrado");
        }
        Usuario existente = usuarioRepository.buscar(dados.getId());
        if (existente == null) {
            throw new UsuarioException("Usuário não encontrado");
        }
        if (existente == dados) {
            throw new UsuarioException("Atualização inválida: informe uma cópia dos dados, não o objeto persistido.");
        }
        if (existente.getClass() != dados.getClass()) {
            throw new UsuarioException("O perfil do usuário não pode ser alterado.");
        }
        exigirAdministradorOuProprio(existente.getId());
        exigirNome(dados.getNome());

        if (!Objects.equals(existente.getLogin(), dados.getLogin())) {
            UsuarioValidador.validarEmail(dados.getLogin());
            Usuario outro = usuarioRepository.buscarPorEmail(dados.getLogin());
            if (outro != null && !outro.getId().equalsIgnoreCase(existente.getId())) {
                throw new UsuarioException("Login já cadastrado: " + dados.getLogin());
            }
        }
        if (!Objects.equals(existente.getSenha(), dados.getSenha())) {
            UsuarioValidador.validarSenha(dados.getSenha());
        }

        existente.setNome(dados.getNome());
        existente.setLogin(dados.getLogin());
        existente.setSenha(dados.getSenha());
        if (existente instanceof Funcionario f && dados instanceof Funcionario d) {
            f.setCargo(d.getCargo());
        }
        if (existente instanceof Administrador a && dados instanceof Administrador d) {
            a.setNivelAcesso(d.getNivelAcesso());
            a.setDepartamento(d.getDepartamento());
        }
        usuarioRepository.atualizar(existente);
    }

    @Override
    public void desativar(String id) {
        exigirAdministrador();
        Usuario usuario = buscarPorId(id);
        exigirOutroUsuario(usuario, "desativar");
        usuario.setAtivo(false);
        usuarioRepository.atualizar(usuario);
    }

    @Override
    public void deletar(String id) {
        exigirAdministrador();
        Usuario usuario = buscarPorId(id);
        exigirOutroUsuario(usuario, "remover");
        usuarioRepository.deletar(id);
    }

    @Override
    public void registrarHistorico(String clienteId) {
        Usuario usuario = buscarPorId(clienteId);
        if (usuario instanceof Cliente cliente) {
            cliente.setHistorico(true);
            usuarioRepository.atualizar(cliente);
        }
    }

    /* ------------------------------ sessão ------------------------------ */

    @Override
    public Usuario autenticar(String email, String senha) {
        Usuario usuario = usuarioRepository.buscarPorEmail(email);
        if (usuario == null || usuario.getSenha() == null || !usuario.getSenha().equals(senha)) {
            throw new UsuarioException("Email ou senha inválidos.");
        }
        if (!usuario.isAtivo()) {
            throw new UsuarioException("Conta desativada. Procure um administrador.");
        }
        usuarioAtual = usuario;
        return usuario;
    }

    @Override
    public Usuario getUsuarioAtual() {
        return usuarioAtual;
    }

    @Override
    public void encerrarSessao() {
        usuarioAtual = null;
    }

    public void salvarDados() {
        this.usuarioRepository.salvarDados();
    }

    /* --------------------------- autorização ---------------------------- */

    private void exigirAdministrador() {
        if (!(usuarioAtual instanceof Administrador)) {
            throw new UsuarioException("Acesso negado: operação restrita a administradores.");
        }
    }

    private void exigirAdministradorOuProprio(String idAlvo) {
        boolean proprio = usuarioAtual != null && usuarioAtual.getId().equalsIgnoreCase(idAlvo);
        if (!proprio) {
            exigirAdministrador();
        }
    }

    private void exigirOutroUsuario(Usuario alvo, String acao) {
        if (usuarioAtual != null && usuarioAtual.getId().equalsIgnoreCase(alvo.getId())) {
            throw new UsuarioException("Você não pode " + acao + " a própria conta.");
        }
    }

    private static void exigirNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new UsuarioException("Informe o nome.");
        }
    }
}