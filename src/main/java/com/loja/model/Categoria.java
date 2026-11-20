package com.loja.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    private String id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "historico")
    private boolean historico;

    public Categoria(String id, String nome){
        this.id = id;
        this.nome = nome;
        this.historico = false;
    }

    public Categoria() {}

    public String getId() {
        return id;
    }
    public String getNome() {
        return nome;
    }
    public boolean hasHistorico(){
        return this.historico;
    }

    public void setId(String id) {
        this.id = id;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public void setHistorico(boolean historico) {
        this.historico = historico;
    }
}