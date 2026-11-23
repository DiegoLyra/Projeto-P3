package com.loja.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "perfil")   // coluna que guarda ADMINISTRADOR / FUNCIONARIO / CLIENTE

public abstract class Usuario {

    @Id 
    private String id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "login", nullable = false, unique = true)
    private String login;

    @Column(name = "senha", nullable = false)
    private String senha;

    @Column(name = "ativo", nullable = false)
    private boolean ativo;

    protected Usuario(String id, String nome, String login, String senha) {
        this.id = id;
        this.nome = nome;
        this.login = login;
        this.senha = senha;
    }

    protected Usuario() {}

    public abstract String getPerfil();

    public String getId() {
        return id;
    }
    public String getNome() {
        return nome;
    }
    public String getLogin() {
        return login;
    }
    public String getSenha() {
        return senha;
    }
    public boolean isAtivo() {
        return ativo;
    }

    public void setId(String id){
        this.id = id;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public void setLogin(String login) {
        this.login = login;
    }
    public void setSenha(String senha) {
        this.senha = senha;
    }
    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}