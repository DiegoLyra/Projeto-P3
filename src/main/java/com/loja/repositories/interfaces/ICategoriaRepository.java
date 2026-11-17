package com.loja.repositories.interfaces;
import com.loja.model.Categoria;
import java.util.Map;

public interface ICategoriaRepository {
    void salvar(Categoria categoria);
    Categoria buscar(String id);

    // O conjunto chave-valor vai ser String-Objeto, sendo a String o ID para o hashmap
    Map<String, Categoria> listar();

    boolean atualizar(Categoria categoria);
    boolean deletar(String id);
}