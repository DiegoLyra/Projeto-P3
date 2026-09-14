package com.loja.exceptions;

public class FornecedorException extends RuntimeException {

    public FornecedorException(String mensagem) {
        super(mensagem);
    }

    public FornecedorException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}