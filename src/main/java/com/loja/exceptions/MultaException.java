package com.loja.exceptions;

public class MultaException extends RuntimeException {

    public MultaException(String mensagem) {
        super(mensagem);
    }

    public MultaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}