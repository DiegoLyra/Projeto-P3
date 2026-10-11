package com.loja.business;

import com.loja.exceptions.UsuarioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioValidadorTest {

    @Test
    void emailValido() {
        assertDoesNotThrow(() -> UsuarioValidador.validarEmail("maria@email.com"));
        assertDoesNotThrow(() -> UsuarioValidador.validarEmail("joao.silva+loja@mail.com.br"));
    }

    @Test
    void emailInvalido() {
        for (String email : new String[]{"maria", "maria@", "@email.com", "maria@email", "maria @email.com", ""}) {
            assertThrows(UsuarioException.class, () -> UsuarioValidador.validarEmail(email), email);
        }
    }

    @Test
    void senhaValida() {
        assertDoesNotThrow(() -> UsuarioValidador.validarSenha("Senha@123"));
    }

    @Test
    void senhaSemMaiuscula() {
        UsuarioException e = assertThrows(UsuarioException.class, () -> UsuarioValidador.validarSenha("senha@123"));
        assertTrue(e.getMessage().contains("maiúscula"));
    }

    @Test
    void senhaSemEspecial() {
        UsuarioException e = assertThrows(UsuarioException.class, () -> UsuarioValidador.validarSenha("Senha1234"));
        assertTrue(e.getMessage().contains("especial"));
    }

    @Test
    void senhaCurta() {
        UsuarioException e = assertThrows(UsuarioException.class, () -> UsuarioValidador.validarSenha("S@1"));
        assertTrue(e.getMessage().contains("8 caracteres"));
    }

    @Test
    void senhaComEspaco() {
        assertThrows(UsuarioException.class, () -> UsuarioValidador.validarSenha("Senha @123"));
    }
}