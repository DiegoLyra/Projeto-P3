package com.loja.repositories;

import com.loja.exceptions.PersistenciaException;
import com.loja.model.Categoria;
import com.loja.repositories.interfaces.ICategoriaRepository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CategoriaPersistenciaCSV implements ICategoriaRepository {
    private String caminhoArquivo;
    private Map<String, Categoria> categorias;

    public CategoriaPersistenciaCSV(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
        this.categorias = new HashMap<>();
        this.carregarDados();
    }

    public String getCaminhoArquivo() {
        return caminhoArquivo;
    }

    public void setCaminhoArquivo(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
    }

    @Override
    public void salvar(Categoria categoria) {
        categorias.put(categoria.getId().toUpperCase(), categoria);
    }

    @Override
    public Categoria buscar(String id) {
        return categorias.get(id.toUpperCase());
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
            this.categorias.remove(id);
            return true;
        }
        return false;
    }

    @Override
    public void carregarDados() {
        try (BufferedReader leitor = new BufferedReader(new FileReader(this.caminhoArquivo))) {
            String linha = leitor.readLine();

            if (linha != null && linha.toLowerCase().startsWith("id;nome")) {
                linha = leitor.readLine();
            }

            while (linha != null) {
                String[] dados = linha.split(";");

                if (dados.length >= 3) {
                    String id = dados[0].toUpperCase();
                    String nome = dados[1];
                    boolean historico = Boolean.parseBoolean(dados[2]);

                    Categoria categoria = new Categoria(id, nome);
                    categoria.setHistorico(historico);

                    this.categorias.put(categoria.getId(), categoria);
                }
                linha = leitor.readLine();
            }
        } catch (IOException e) {
            throw new PersistenciaException("Erro ao carregar dados do arquivo: " + this.caminhoArquivo, e);
        }
    }

    @Override
    public void salvarDados() {
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(this.caminhoArquivo))) {
            escritor.write("id;nome;historico");
            escritor.newLine();

            for (Categoria categoria : this.categorias.values()) {
                String linha = categoria.getId().toUpperCase() + ";" +
                        categoria.getNome() + ";" +
                        categoria.hasHistorico();
                escritor.write(linha);
                escritor.newLine();
            }
        } catch (IOException e) {
            throw new PersistenciaException("Erro ao salvar dados no arquivo: " + this.caminhoArquivo, e);
        }
    }
}