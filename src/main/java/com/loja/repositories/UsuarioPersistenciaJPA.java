package com.loja.repositories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.loja.exceptions.PersistenciaException;
import com.loja.model.Administrador;
import com.loja.model.Cliente;
import com.loja.model.Funcionario;
import com.loja.model.Usuario;
import com.loja.repositories.interfaces.IUsuarioRepository;
import com.loja.repositories.jpa.JPAUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

public class UsuarioPersistenciaJPA implements IUsuarioRepository {

    @Override
    public void salvar(Usuario usuario) {
        usuario.setId(usuario.getId().toUpperCase());
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(usuario);   // grava em "usuarios" + na tabela do perfil
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            desfazer(em);
            throw new PersistenciaException("Erro ao salvar usuário: " + usuario.getId(), e);
        } finally {
            em.close();
        }
    }

    @Override
    public Usuario buscar(String id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            // Devolve já o tipo certo (Administrador, Funcionario ou Cliente)
            return em.find(Usuario.class, id.toUpperCase());
        } finally {
            em.close();
        }
    }

    @Override
    public Usuario buscarPorEmail(String email) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<Usuario> resultado = em.createQuery(
                            "select u from Usuario u where lower(u.login) = lower(:login)", Usuario.class)
                    .setParameter("login", email)
                    .getResultList();
            return resultado.isEmpty() ? null : resultado.get(0);
        } finally {
            em.close();
        }
    }

    @Override
    public Map<String, Usuario> listar() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<Usuario> lista = em.createQuery("select u from Usuario u", Usuario.class)
                    .getResultList();
            return paraMapa(lista);
        } finally {
            em.close();
        }
    }

    @Override
    public Map<String, Usuario> listar(String perfil) {
        Class<? extends Usuario> tipo = classeDoPerfil(perfil);
        if (tipo == null) {
            return new HashMap<>();
        }
        EntityManager em = JPAUtil.getEntityManager();
        try {
            // TYPE(u) vai filtra pelo tipo da classe
            List<Usuario> lista = em.createQuery(
                            "select u from Usuario u where type(u) = :tipo", Usuario.class)
                    .setParameter("tipo", tipo)
                    .getResultList();
            return paraMapa(lista);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean atualizar(Usuario usuario) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            if (em.find(Usuario.class, usuario.getId().toUpperCase()) == null) {
                return false;
            }
            em.getTransaction().begin();
            em.merge(usuario);
            em.getTransaction().commit();
            return true;
        } catch (PersistenceException e) {
            desfazer(em);
            throw new PersistenciaException("Erro ao atualizar usuário: " + usuario.getId(), e);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean deletar(String id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Usuario usuario = em.find(Usuario.class, id.toUpperCase());
            if (usuario == null) {
                return false;
            }
            em.getTransaction().begin();
            em.remove(usuario);   // apaga das duas tabelas
            em.getTransaction().commit();
            return true;
        } catch (PersistenceException e) {
            desfazer(em);
            throw new PersistenciaException("Erro ao deletar usuário: " + id, e);
        } finally {
            em.close();
        }
    }

    private Class<? extends Usuario> classeDoPerfil(String perfil) {
        if (perfil == null) {
            return null;
        }
        switch (perfil.toUpperCase()) {
            case "ADMINISTRADOR":
                return Administrador.class;
            case "FUNCIONARIO":
                return Funcionario.class;
            case "CLIENTE":
                return Cliente.class;
            default:
                return null;
        }
    }

    private Map<String, Usuario> paraMapa(List<Usuario> lista) {
        Map<String, Usuario> usuarios = new HashMap<>();
        for (Usuario u : lista) {
            usuarios.put(u.getId(), u);
        }
        return usuarios;
    }

    private void desfazer(EntityManager em) {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }
}