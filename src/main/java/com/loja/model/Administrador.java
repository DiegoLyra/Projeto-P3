package com.loja.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "administradores")
@DiscriminatorValue("ADMINISTRADOR")
public class Administrador extends Usuario {

    @Column(name = "nivel_acesso", nullable = false)
    private int nivelAcesso;

    @Column(name = "departamento", nullable = false)
    private String departamento;

    public Administrador(String id, String nome, String login, String senha, int nivelAcesso, String departamento) {
        super(id, nome, login, senha);
        this.nivelAcesso = nivelAcesso;
        this.departamento = departamento;
    }

    public Administrador(String id, String nome, String login, String senha) {
        super(id, nome, login, senha);
        this.nivelAcesso = 1;
        this.departamento = "Geral";
    }

    // Construtor vazio obrigatório para o JPA
    public Administrador() {
        super();
    }

    @Override
    public String getPerfil() {
        return "ADMINISTRADOR";
    }

    public int getNivelAcesso() {
        return nivelAcesso;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setNivelAcesso(int nivelAcesso) {
        this.nivelAcesso = nivelAcesso;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }
}