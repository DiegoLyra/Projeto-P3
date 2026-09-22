package com.loja.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class ExceptionsTest {

    static Stream<Arguments> exceptions() {
        return Stream.of(
                Arguments.of("CategoriaException",
                        (Function<String, RuntimeException>) CategoriaException::new,
                        (BiFunction<String, Throwable, RuntimeException>) CategoriaException::new),
                Arguments.of("ContratoException",
                        (Function<String, RuntimeException>) ContratoException::new,
                        (BiFunction<String, Throwable, RuntimeException>) ContratoException::new),
                Arguments.of("FornecedorException",
                        (Function<String, RuntimeException>) FornecedorException::new,
                        (BiFunction<String, Throwable, RuntimeException>) FornecedorException::new),
                Arguments.of("ItemException",
                        (Function<String, RuntimeException>) ItemException::new,
                        (BiFunction<String, Throwable, RuntimeException>) ItemException::new),
                Arguments.of("MultaException",
                        (Function<String, RuntimeException>) MultaException::new,
                        (BiFunction<String, Throwable, RuntimeException>) MultaException::new),
                Arguments.of("PersistenciaException",
                        (Function<String, RuntimeException>) PersistenciaException::new,
                        (BiFunction<String, Throwable, RuntimeException>) PersistenciaException::new),
                Arguments.of("UsuarioException",
                        (Function<String, RuntimeException>) UsuarioException::new,
                        (BiFunction<String, Throwable, RuntimeException>) UsuarioException::new)
        );
    }

    @ParameterizedTest(name = "{0}: construtor com mensagem deve preservar a mensagem e não ter causa")
    @MethodSource("exceptions")
    @DisplayName("Construtor(mensagem)")
    void construtorComMensagem_devePreservarMensagem(
            String nome,
            Function<String, RuntimeException> comMensagem,
            BiFunction<String, Throwable, RuntimeException> comMensagemECausa) {

        RuntimeException excecao = comMensagem.apply("erro: " + nome);

        assertInstanceOf(RuntimeException.class, excecao);
        assertEquals("erro: " + nome, excecao.getMessage());
        assertNull(excecao.getCause());
    }

    @ParameterizedTest(name = "{0}: construtor com mensagem e causa deve preservar ambos")
    @MethodSource("exceptions")
    @DisplayName("Construtor(mensagem, causa)")
    void construtorComMensagemECausa_devePreservarAmbos(
            String nome,
            Function<String, RuntimeException> comMensagem,
            BiFunction<String, Throwable, RuntimeException> comMensagemECausa) {

        Throwable causaOriginal = new IllegalStateException("causa raiz de " + nome);

        RuntimeException excecao = comMensagemECausa.apply("erro: " + nome, causaOriginal);

        assertEquals("erro: " + nome, excecao.getMessage());
        assertEquals(causaOriginal, excecao.getCause());
    }
}