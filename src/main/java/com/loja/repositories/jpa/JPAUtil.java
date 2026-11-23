package com.loja.repositories.jpa;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.loja.exceptions.PersistenciaException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class JPAUtil {

    private static volatile EntityManagerFactory emf;
    // Se a propriedade "loja.db.url" existir (nos testes), usa esse banco no lugar do persistence.xml
    private static final String PROPRIEDADE_URL = "loja.db.url";

    private JPAUtil() {
    }

    private static EntityManagerFactory criarFactory() {
        Map<String, String> config = new HashMap<>();
        String url = System.getProperty(PROPRIEDADE_URL);
        if (url != null && !url.isBlank()) {
            config.put("jakarta.persistence.jdbc.url", url);
        }
        return Persistence.createEntityManagerFactory("loja", config);
    }

    public static EntityManager getEntityManager() {
        EntityManagerFactory factory = emf;
        if (factory == null) {
            synchronized (JPAUtil.class) {
                factory = emf;
                if (factory == null) {
                    factory = criarFactory();                    
                    emf = factory;
                }
            }
        }
        return factory.createEntityManager();
    }

    /**
     * Executa uma operacao de leitura com EntityManager dedicado.
     * Nao inicia transacao; o EntityManager e fechado automaticamente.
     * Excecoes de dominio (com.loja.exceptions, exceto PersistenciaException) sao propagadas diretamente.
     */
    public static <T> T read(Function<EntityManager, T> work) {
        EntityManager em = getEntityManager();
        try {
            return work.apply(em);
        } catch (Exception e) {
            if (isDominioException(e)) {
                throw e;
            }
            throw new PersistenciaException("Erro na operacao JPA.", e);
        } finally {
            em.close();
        }
    }

    /**
     * Executa uma operacao de escrita com transacao.
     * Inicia transacao antes do trabalho e commit/rollback apos.
     * O EntityManager e fechado automaticamente.
     * Retorna o valor produzido pela funcao de trabalho.
     * Excecoes de dominio (com.loja.exceptions, exceto PersistenciaException) sao propagadas diretamente.
     */
    public static <T> T write(Function<EntityManager, T> work) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            T result = work.apply(em);
            em.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (isDominioException(e)) {
                throw e;
            }
            throw new PersistenciaException("Erro na operacao JPA.", e);
        } finally {
            em.close();
        }
    }

    /**
     * Verifica se uma excecao e do dominio (pacote com.loja.exceptions),
     * excluindo a propria PersistenciaException.
     */
    private static boolean isDominioException(Exception e) {
        String className = e.getClass().getName();
        return className.startsWith("com.loja.exceptions")
                && !className.equals("com.loja.exceptions.PersistenciaException");
    }

    public static void close() {
        synchronized (JPAUtil.class) {
            if (emf != null) {
                emf.close();
                emf = null;
            }
        }
    }
}