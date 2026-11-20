package com.loja.repositories;

import com.loja.exceptions.CategoriaException;
import com.loja.model.Categoria;
import com.loja.repositories.interfaces.ICategoriaRepository;
import com.loja.repositories.jpa.JPAUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoriaPersistenciaJPA implements ICategoriaRepository {

    @Override
    public void salvar(Categoria categoria) {
        JPAUtil.write(em -> {
            String id = categoria.getId().toUpperCase();
            Categoria existente = em.find(Categoria.class, id);
            if (existente != null) {
                throw new CategoriaException("Categoria ja existe: " + id);
            }
            Categoria paraPersistir = new Categoria(id, categoria.getNome());
            paraPersistir.setHistorico(categoria.hasHistorico());
            em.persist(paraPersistir);
            return null;
        });
    }

    @Override
    public Categoria buscar(String id) {
        return JPAUtil.read(em -> em.find(Categoria.class, id.toUpperCase()));
    }

    @Override
    public Map<String, Categoria> listar() {
        return JPAUtil.read(em -> {
            TypedQuery<Categoria> query = em.createQuery("SELECT c FROM Categoria c", Categoria.class);
            List<Categoria> resultados = query.getResultList();
            Map<String, Categoria> categorias = new HashMap<>();
            for (Categoria c : resultados) {
                categorias.put(c.getId(), c);
            }
            return categorias;
        });
    }

    @Override
    public boolean atualizar(Categoria categoria) {
        return JPAUtil.write(em -> {
            String id = categoria.getId().toUpperCase();
            Categoria existente = em.find(Categoria.class, id);
            if (existente == null) {
                return false;
            }
            existente.setNome(categoria.getNome());
            existente.setHistorico(categoria.hasHistorico());
            return true;
        });
    }

    @Override
    public boolean deletar(String id) {
        return JPAUtil.write(em -> {
            Categoria categoria = em.find(Categoria.class, id.toUpperCase());
            if (categoria == null) {
                return false;
            }
            em.remove(categoria);
            return true;
        });
    }
}