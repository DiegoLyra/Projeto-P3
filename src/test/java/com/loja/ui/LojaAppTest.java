package com.loja.ui;

import com.loja.model.Categoria;
import com.loja.repositories.jpa.JPAUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LojaAppTest {

    @TempDir
    Path tempDir;

    private String previousDatabaseUrl;

    @BeforeEach
    void setUp() {
        previousDatabaseUrl = System.getProperty("loja.db.url");
        JPAUtil.close();
        System.setProperty("loja.db.url", "jdbc:sqlite:" + tempDir.resolve("loja.db").toAbsolutePath());
    }

    @AfterEach
    void tearDown() {
        JPAUtil.close();
        if (previousDatabaseUrl == null) {
            System.clearProperty("loja.db.url");
        } else {
            System.setProperty("loja.db.url", previousDatabaseUrl);
        }
    }

    @Test
    void montarFacade_deveCarregarCategoriasIniciaisAntesDeResolverDependencias() {
        Categoria categoria = LojaApp.montarFacade().buscarCategoria("CAT001");

        assertEquals("TELEVISAO", categoria.getNome());
    }
}