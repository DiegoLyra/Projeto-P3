package com.loja.repositories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.loja.model.Cliente;
import com.loja.model.ContratoAluguel;
import com.loja.repositories.interfaces.IContratoRepository;
import com.loja.repositories.jpa.JPAUtil;

public class ContratoPersistenciaJPA implements IContratoRepository {

    @Override
    public void salvar(ContratoAluguel contrato) {
        JPAUtil.write(em -> {
            contrato.setId(contrato.getId().toUpperCase());

            Cliente cliente = em.getReference(
                    Cliente.class, contrato.getCliente().getId());

            contrato.setCliente(cliente);

            em.persist(contrato);
            return null;
        });
    }

    @Override
    public ContratoAluguel buscar(String id) {
        return JPAUtil.read(em -> em.createQuery(
                "SELECT c FROM ContratoAluguel c " +
                "LEFT JOIN FETCH c.cliente " +
                "WHERE c.id = :id",
                ContratoAluguel.class)
                .setParameter("id", id.toUpperCase())
                .getResultStream()
                .findFirst()
                .orElse(null));
    }

    @Override
    public Map<String, ContratoAluguel> listar() {
        return JPAUtil.read(em -> {
            List<ContratoAluguel> contratos = em.createQuery(
                    "SELECT DISTINCT c FROM ContratoAluguel c " +
                    "LEFT JOIN FETCH c.cliente",
                    ContratoAluguel.class)
                    .getResultList();

            return paraMapa(contratos);
        });
    }

    @Override
    public Map<String, ContratoAluguel> listar(Cliente cliente) {
        return JPAUtil.read(em -> {
            List<ContratoAluguel> contratos = em.createQuery(
                    "SELECT DISTINCT c FROM ContratoAluguel c " +
                    "LEFT JOIN FETCH c.cliente " +
                    "WHERE c.cliente.id = :clienteId",
                    ContratoAluguel.class)
                    .setParameter("clienteId", cliente.getId())
                    .getResultList();

            return paraMapa(contratos);
        });
    }

    @Override
    public Map<String, ContratoAluguel> listar(String status) {
        return JPAUtil.read(em -> {
            List<ContratoAluguel> contratos = em.createQuery(
                    "SELECT DISTINCT c FROM ContratoAluguel c " +
                    "LEFT JOIN FETCH c.cliente " +
                    "WHERE UPPER(c.status) = UPPER(:status)",
                    ContratoAluguel.class)
                    .setParameter("status", status)
                    .getResultList();

            return paraMapa(contratos);
        });
    }

    @Override
    public boolean atualizar(ContratoAluguel contrato) {
        return JPAUtil.write(em -> {
            String id = contrato.getId().toUpperCase();

            ContratoAluguel existente =
                    em.find(ContratoAluguel.class, id);

            if (existente == null) {
                return false;
            }

            contrato.setId(id);

            if (contrato.getCliente() != null) {
                contrato.setCliente(em.getReference(
                        Cliente.class, contrato.getCliente().getId()));
            }

            em.merge(contrato);
            return true;
        });
    }

    @Override
    public boolean deletar(String id) {
        return JPAUtil.write(em -> {
            ContratoAluguel contrato =
                    em.find(ContratoAluguel.class, id.toUpperCase());

            if (contrato == null) {
                return false;
            }

            em.remove(contrato);
            return true;
        });
    }

    private Map<String, ContratoAluguel> paraMapa(
            List<ContratoAluguel> lista) {

        Map<String, ContratoAluguel> contratos = new HashMap<>();

        for (ContratoAluguel contrato : lista) {
            contratos.put(contrato.getId(), contrato);
        }

        return contratos;
    }
}