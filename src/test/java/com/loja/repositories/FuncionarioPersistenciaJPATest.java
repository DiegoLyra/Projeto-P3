package com.loja.repositories;

import com.loja.model.Funcionario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FuncionarioPersistenciaJPATest {

    @TempDir
    Path tempDir;

    private EntityManagerFactory emf;

    @BeforeEach
    void setUp() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.driver", "org.sqlite.JDBC");
        properties.put("jakarta.persistence.jdbc.url", "jdbc:sqlite:" + tempDir.resolve("funcionarios.db").toAbsolutePath() + "?foreign_keys=true");
        properties.put("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect");
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        properties.put("hibernate.show_sql", "false");
        emf = Persistence.createEntityManagerFactory("loja", properties);
    }

    @AfterEach
    void tearDown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @Test
    void devePersistirFuncionarioComCamposHerdadosEEspecificos() {
        EntityManager em = emf.createEntityManager();
        try {
            Funcionario funcionario = new Funcionario("FUN001", "Ana", "ana", "senha", "Caixa");
            funcionario.setAtivo(true);
            funcionario.setHistorico(true);

            em.getTransaction().begin();
            em.persist(funcionario);
            em.getTransaction().commit();
        } finally {
            em.close();
        }

        em = emf.createEntityManager();
        try {
            Funcionario funcionario = em.find(Funcionario.class, "FUN001");

            assertNotNull(funcionario);
            assertEquals("Ana", funcionario.getNome());
            assertEquals("ana", funcionario.getLogin());
            assertEquals("Caixa", funcionario.getCargo());
            assertTrue(funcionario.hasHistorico());
            assertTrue(funcionario.isAtivo());
            assertEquals("FUNCIONARIO", em.createNativeQuery("select perfil from usuarios where id = :id")
                    .setParameter("id", "FUN001")
                    .getSingleResult());
        } finally {
            em.close();
        }
    }
}