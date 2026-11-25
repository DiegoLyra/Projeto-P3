package com.loja.ui;

import com.loja.model.Categoria;
import com.loja.model.Usuario;
import com.loja.repositories.CategoriaPersistenciaJPA;
import com.loja.repositories.UsuarioPersistenciaCSV;
import com.loja.repositories.UsuarioPersistenciaJPA;
import com.loja.repositories.interfaces.ICategoriaRepository;
import com.loja.repositories.interfaces.IUsuarioRepository;
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

        // 1. Insere diretamente via JPA as categorias referenciadas por dados/itens.csv
        ICategoriaRepository categoriaRepo = new CategoriaPersistenciaJPA();
        categoriaRepo.salvar(new Categoria("CAT001", "TELEVISAO"));
        categoriaRepo.salvar(new Categoria("CAT002", "FERRAMENTAS"));
        categoriaRepo.salvar(new Categoria("CAT003", "ELETRODOMESTICOS"));
        categoriaRepo.salvar(new Categoria("CAT004", "INFORMATICA"));
        categoriaRepo.salvar(new Categoria("CAT005", "EVENTOS"));
        // (Confira no arquivo dados/itens.csv se há algum outro CATxxx e adicione aqui se necessário)

        // 2. Enquanto Contrato ainda ler dados/contratos.csv, popula os usuários no banco de teste
        IUsuarioRepository usuarioRepo = new UsuarioPersistenciaJPA();
        for (Usuario u : new UsuarioPersistenciaCSV("dados/usuarios.csv").listar().values()) {
            usuarioRepo.salvar(u);
        }
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