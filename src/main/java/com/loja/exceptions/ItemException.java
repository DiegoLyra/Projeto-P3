package com.loja.exceptions;

public class ItemException extends RuntimeException {

    public ItemException(String mensagem) {
        super(mensagem);
    }

    public ItemException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}