package com.loja.repositories.interfaces;

import java.util.Map;

import com.loja.model.Usuario;

public interface IUsuarioRepository {

    void salvar(Usuario usuario);

    Usuario buscar(String id);

    Usuario buscarPorEmail(String email);

    Map<String, Usuario> listar();

    Map<String, Usuario> listar(String perfil);

    boolean atualizar(Usuario usuario);

    boolean deletar(String id);

}
