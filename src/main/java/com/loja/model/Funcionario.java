package com.loja.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "funcionarios")
@DiscriminatorValue("FUNCIONARIO")
public class Funcionario extends Usuario {

    @Column(name = "cargo")
    private String cargo;

    @Column(name = "historico", nullable = false)
    private boolean historico;

    public Funcionario(String id, String nome, String login, String senha, String cargo) {
        super(id, nome, login, senha);
        this.cargo = cargo;
        this.historico = false;
    }

    public Funcionario() {
        super();
    }

    public String getPerfil(){
        return "FUNCIONARIO";
    }
    public String getCargo() {
        return cargo;
    }
    public boolean hasHistorico(){
        return this.historico;
    }

    public void setCargo(String cargo){
        this.cargo = cargo;
    }
    public void setHistorico(boolean historico) {
        this.historico = historico;
    }
}