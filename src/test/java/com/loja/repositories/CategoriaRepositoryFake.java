package com.loja.repositories;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.loja.model.Categoria;
import com.loja.repositories.interfaces.ICategoriaRepository;

public class CategoriaRepositoryFake implements ICategoriaRepository {
    private final Map<String, Categoria> categorias = new HashMap<>();

    @Override
    public void salvar(Categoria categoria) {
        categorias.put(categoria.getId(), categoria);
    }

    @Override
    public Categoria buscar(String id) {
        return categorias.get(id);
    }

    @Override
    public Map<String, Categoria> listar() {
        return Collections.unmodifiableMap(this.categorias);
    }

    @Override
    public boolean atualizar(Categoria categoria) {
        if (this.categorias.containsKey(categoria.getId())) {
            categorias.put(categoria.getId(), categoria);
            return true;
        }
        return false;
    }

    @Override
    public boolean deletar(String id) {
        if (this.categorias.containsKey(id)) {
            categorias.remove(id);
            return true;
        }
        return false;
    }

    @Override
    public void carregarDados() {
        // Fake em memória usado apenas em testes: não há fonte externa para carregar.
    }

    @Override
    public void salvarDados() {
        // Fake em memória usado apenas em testes: não há necessidade de persistir em disco.
    }
}