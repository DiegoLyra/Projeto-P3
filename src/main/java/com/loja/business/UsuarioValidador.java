package com.loja.business;

import com.loja.exceptions.UsuarioException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class UsuarioValidador {

    public static final int SENHA_MIN = 8;
    public static final int SENHA_MAX = 64;
    public static final int EMAIL_MAX = 254;

    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");

    private static final Pattern MAIUSCULA = Pattern.compile("\\p{Lu}");
    private static final Pattern ESPECIAL = Pattern.compile("[^\\p{L}\\p{N}\\s]");
    private static final Pattern ESPACO = Pattern.compile("\\s");

    private UsuarioValidador() {
    }

    public static void validarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new UsuarioException("Informe o e-mail.");
        }
        if (email.length() > EMAIL_MAX || !EMAIL.matcher(email).matches()) {
            throw new UsuarioException("E-mail inválido. Use o formato nome@dominio.com.");
        }
    }

    public static void validarSenha(String senha) {
        if (senha == null || senha.isEmpty()) {
            throw new UsuarioException("Informe a senha.");
        }
        if (senha.length() > SENHA_MAX) {
            throw new UsuarioException("A senha deve ter no máximo " + SENHA_MAX + " caracteres.");
        }
        if (ESPACO.matcher(senha).find()) {
            throw new UsuarioException("A senha não pode conter espaços.");
        }

        List<String> faltando = new ArrayList<>();
        if (senha.length() < SENHA_MIN) {
            faltando.add("pelo menos " + SENHA_MIN + " caracteres");
        }
        if (!MAIUSCULA.matcher(senha).find()) {
            faltando.add("uma letra maiúscula");
        }
        if (!ESPECIAL.matcher(senha).find()) {
            faltando.add("um caractere especial (ex.: ! @ # $ %)");
        }
        if (!faltando.isEmpty()) {
            throw new UsuarioException("A senha deve ter " + String.join(", ", faltando) + ".");
        }
    }
}