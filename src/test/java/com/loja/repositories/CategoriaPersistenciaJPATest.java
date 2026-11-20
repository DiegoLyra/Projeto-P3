package com.loja.repositories;

import com.loja.model.Categoria;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.Persistence;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CategoriaPersistenciaJPATest {

    @TempDir
    Path tempDir;

    private Path dbFile;
    private EntityManagerFactory emf;

    @BeforeEach
    void setUp() {
        dbFile = tempDir.resolve("test.db");
        Map<String, Object> props = new HashMap<>();
        props.put("jakarta.persistence.jdbc.driver", "org.sqlite.JDBC");
        props.put("jakarta.persistence.jdbc.url", "jdbc:sqlite:" + dbFile.toAbsolutePath() + "?foreign_keys=true");
        props.put("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect");
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.format_sql", "false");

        emf = Persistence.createEntityManagerFactory("loja", props);
    }

    @AfterEach
    void tearDown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    private void salvar(Categoria categoria) {
        EntityManager em = emf.createEntityManager();
        try {
            categoria.setId(categoria.getId().toUpperCase());
            em.getTransaction().begin();
            em.persist(categoria);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    private Categoria buscar(String id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Categoria.class, id.toUpperCase());
        } finally {
            em.close();
        }
    }

    private Map<String, Categoria> listar() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Categoria> query = em.createQuery("SELECT c FROM Categoria c", Categoria.class);
            List<Categoria> resultados = query.getResultList();
            Map<String, Categoria> categorias = new HashMap<>();
            for (Categoria c : resultados) {
                categorias.put(c.getId(), c);
            }
            return categorias;
        } finally {
            em.close();
        }
    }

    private boolean atualizar(Categoria categoria) {
        EntityManager em = emf.createEntityManager();
        try {
            String id = categoria.getId().toUpperCase();
            Categoria existente = em.find(Categoria.class, id);
            if (existente == null) {
                return false;
            }
            em.getTransaction().begin();
            existente.setNome(categoria.getNome());
            existente.setHistorico(categoria.hasHistorico());
            em.merge(existente);
            em.getTransaction().commit();
            return true;
        } finally {
            em.close();
        }
    }

    private boolean deletar(String id) {
        EntityManager em = emf.createEntityManager();
        try {
            Categoria categoria = em.find(Categoria.class, id.toUpperCase());
            if (categoria == null) {
                return false;
            }
            em.getTransaction().begin();
            em.remove(categoria);
            em.getTransaction().commit();
            return true;
        } finally {
            em.close();
        }
    }

    @Test
    void deveSalvarECarregarCategoria() {
        Categoria categoria = new Categoria("cat1", "Categoria Teste");

        salvar(categoria);

        Categoria resultado = buscar("cat1");
        assertNotNull(resultado);
        assertEquals("CAT1", resultado.getId());
        assertEquals("Categoria Teste", resultado.getNome());
        assertFalse(resultado.hasHistorico());
    }

    @Test
    void deveNormalizarIdParaMaiusculas() {
        salvar(new Categoria("cat1", "Categoria Teste"));

        assertNotNull(buscar("CAT1"));
        assertNotNull(buscar("cat1"));
        assertNotNull(buscar("CaT1"));
    }

    @Test
    void deveListarCategorias() {
        salvar(new Categoria("cat1", "Categoria 1"));
        salvar(new Categoria("cat2", "Categoria 2"));
        salvar(new Categoria("cat3", "Categoria 3"));

        Map<String, Categoria> resultado = listar();

        assertEquals(3, resultado.size());
        assertTrue(resultado.containsKey("CAT1"));
        assertTrue(resultado.containsKey("CAT2"));
        assertTrue(resultado.containsKey("CAT3"));
    }

    @Test
    void deveRetornarMapaVazioQuandoNaoHaCategorias() {
        Map<String, Categoria> resultado = listar();
        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveAtualizarCategoriaExistente() {
        salvar(new Categoria("cat1", "Nome Antigo"));

        Categoria atualizada = new Categoria("cat1", "Nome Novo");
        boolean resultado = atualizar(atualizada);

        assertTrue(resultado);
        assertEquals("Nome Novo", buscar("CAT1").getNome());
    }

    @Test
    void naoDeveAtualizarCategoriaInexistente() {
        Categoria categoria = new Categoria("inexistente", "Nome");

        boolean resultado = atualizar(categoria);

        assertFalse(resultado);
    }

    @Test
    void deveDeletarCategoriaExistente() {
        salvar(new Categoria("cat1", "Categoria Teste"));

        boolean resultado = deletar("cat1");

        assertTrue(resultado);
        assertNull(buscar("CAT1"));
    }

    @Test
    void naoDeveDeletarCategoriaInexistente() {
        boolean resultado = deletar("inexistente");

        assertFalse(resultado);
    }

    @Test
    void deveManterDadosEntreInstancias() {
        salvar(new Categoria("cat1", "Categoria Persistente"));

        // Simula reinício: busca com novo EntityManager
        Categoria resultado = buscar("CAT1");

        assertNotNull(resultado);
        assertEquals("Categoria Persistente", resultado.getNome());
    }

    @Test
    void deveBuscarRetornarNullParaIdInexistente() {
        assertNull(buscar("nao-existe"));
    }

    @Test
    void deveAtualizarCampoHistorico() {
        salvar(new Categoria("cat1", "Categoria Teste"));

        Categoria comHistorico = new Categoria("cat1", "Categoria Teste");
        comHistorico.setHistorico(true);
        atualizar(comHistorico);

        assertTrue(buscar("CAT1").hasHistorico());
    }
}
