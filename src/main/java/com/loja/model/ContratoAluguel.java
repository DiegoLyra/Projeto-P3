
package com.loja.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;

@Entity
@Table(name = "contratos_aluguel")
public class ContratoAluguel {

    @Id
    @Column(name = "id")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // Apenas o ID do Item será persistido pelo JPA.
    @Column(name = "item_id", nullable = false)
    private String itemId;

    // O objeto Item continua disponível para a aplicação,
    // mas não será gerenciado nem persistido pelo JPA.
    @Transient
    private Item item;

    private LocalDate dataRetirada;
    private LocalDate dataPrevDevolucao;
    private LocalDate dataEfetivaDevolucao;

    @Column(precision = 10, scale = 2)
    private BigDecimal valorTotal;

    private String status;
    private Boolean historico;

    private ContratoAluguel(Builder builder) {
        this.id = builder.id;
        this.cliente = builder.cliente;
        this.item = builder.item;

        this.itemId = builder.itemId != null
                ? builder.itemId
                : builder.item != null ? builder.item.getId() : null;

        this.dataRetirada = builder.dataRetirada;
        this.dataPrevDevolucao = builder.dataPrevDevolucao;
        this.dataEfetivaDevolucao = builder.dataEfetivaDevolucao;
        this.valorTotal = builder.valorTotal;
        this.status = builder.status;
        this.historico = false;
    }

    public ContratoAluguel() {
        // Construtor vazio exigido pelo JPA.
    }

    // Getters

    public String getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Item getItem() {
        return item;
    }

    public String getItemId() {
        return itemId;
    }

    public LocalDate getDataRetirada() {
        return dataRetirada;
    }

    public LocalDate getDataPrevDevolucao() {
        return dataPrevDevolucao;
    }

    public LocalDate getDataEfetivaDevolucao() {
        return dataEfetivaDevolucao;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public String getStatus() {
        return status;
    }

    public Boolean getHistorico() {
        return historico;
    }

    // Setters

    public void setId(String id) {
        this.id = id;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public void setItem(Item item) {
        this.item = item;
        this.itemId = item != null ? item.getId() : null;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public void setDataRetirada(LocalDate dataRetirada) {
        this.dataRetirada = dataRetirada;
    }

    public void setDataPrevDevolucao(LocalDate dataPrevDevolucao) {
        this.dataPrevDevolucao = dataPrevDevolucao;
    }

    public void setDataEfetivaDevolucao(LocalDate dataEfetivaDevolucao) {
        this.dataEfetivaDevolucao = dataEfetivaDevolucao;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setHistorico(Boolean historico) {
        this.historico = historico;
    }

    // Builder

    public static class Builder {

        private String id;
        private Cliente cliente;
        private Item item;
        private String itemId;
        private LocalDate dataRetirada;
        private LocalDate dataPrevDevolucao;
        private LocalDate dataEfetivaDevolucao;
        private BigDecimal valorTotal;
        private String status;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder cliente(Cliente cliente) {
            this.cliente = cliente;
            return this;
        }

        public Builder item(Item item) {
            this.item = item;
            this.itemId = item != null ? item.getId() : null;
            return this;
        }

        public Builder itemId(String itemId) {
            this.itemId = itemId;
            return this;
        }

        public Builder dataRetirada(LocalDate dataRetirada) {
            this.dataRetirada = dataRetirada;
            return this;
        }

        public Builder dataPrevDevolucao(LocalDate dataPrevDevolucao) {
            this.dataPrevDevolucao = dataPrevDevolucao;
            return this;
        }

        public Builder dataEfetivaDevolucao(LocalDate dataEfetivaDevolucao) {
            this.dataEfetivaDevolucao = dataEfetivaDevolucao;
            return this;
        }

        public Builder valorTotal(BigDecimal valorTotal) {
            this.valorTotal = valorTotal;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public ContratoAluguel build() {
            return new ContratoAluguel(this);
        }
    }
}
