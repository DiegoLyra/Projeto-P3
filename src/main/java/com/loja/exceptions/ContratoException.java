package com.loja.exceptions;

public class ContratoException extends RuntimeException {

    public ContratoException(String mensagem) {
        super(mensagem);
    }

    public ContratoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}