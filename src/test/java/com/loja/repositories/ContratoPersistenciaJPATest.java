package com.loja.repositories;

import com.loja.model.Cliente;
import com.loja.model.ContratoAluguel;
import com.loja.model.Item;
import com.loja.repositories.jpa.JPAUtil;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ContratoPersistenciaJPATest {

    private ContratoPersistenciaJPA repositorio;

    private Cliente cliente;
    private Cliente outroCliente;
    private Item item;

    private LocalDate dataRetirada;
    private LocalDate dataPrevDevolucao;

    private String prefixoContrato;

    @BeforeEach
    void setUp() {
        repositorio = new ContratoPersistenciaJPA();

        String identificador = UUID.randomUUID().toString()
                .replace("-", "").toUpperCase();

        prefixoContrato = "CT" + identificador;

        cliente = new Cliente(
                "C" + identificador,
                "João",
                "joao@email.com",
                "123"
        );

        outroCliente = new Cliente(
                "C2" + identificador,
                "Maria",
                "maria@email.com",
                "456"
        );

        item = new Item(
                "I1",
                "Furadeira",
                new BigDecimal("50.00"),
                new BigDecimal("500.00"),
                "DISPONIVEL",
                null,
                null
        );

        dataRetirada = LocalDate.of(2025, 1, 10);
        dataPrevDevolucao = LocalDate.of(2025, 1, 15);

        // Os clientes são entidades JPA e precisam existir no banco.
        JPAUtil.write(em -> {
            em.persist(cliente);
            em.persist(outroCliente);
            return null;
        });
    }

    @AfterEach
    void tearDown() {
        JPAUtil.write(em -> {
            // Remove os contratos criados para os clientes deste teste.
            em.createQuery(
                    "DELETE FROM ContratoAluguel c " +
                    "WHERE c.cliente.id IN (:id1, :id2)"
            )
            .setParameter("id1", cliente.getId())
            .setParameter("id2", outroCliente.getId())
            .executeUpdate();

            Cliente clienteBanco =
                    em.find(Cliente.class, cliente.getId());

            Cliente outroClienteBanco =
                    em.find(Cliente.class, outroCliente.getId());

            if (clienteBanco != null) {
                em.remove(clienteBanco);
            }

            if (outroClienteBanco != null) {
                em.remove(outroClienteBanco);
            }

            return null;
        });
    }

    private ContratoAluguel criarContrato(
            String id,
            Cliente cliente,
            Item item,
            LocalDate retirada,
            LocalDate prevDevolucao,
            LocalDate efetivaDevolucao,
            BigDecimal valorTotal,
            String status) {

        ContratoAluguel contrato =
                new ContratoAluguel.Builder()
                        .id(id)
                        .cliente(cliente)
                        .item(item)
                        .dataRetirada(retirada)
                        .dataPrevDevolucao(prevDevolucao)
                        .dataEfetivaDevolucao(efetivaDevolucao)
                        .valorTotal(valorTotal)
                        .status(status)
                        .build();

    
        contrato.setItemId(item.getId());

        return contrato;
    }

    private String idContrato(String sufixo) {
        return prefixoContrato + sufixo;
    }

    // Salvar e buscar

    @Test
    @DisplayName("salvar e buscar: deve recuperar contrato salvo pelo id")
    void salvar_devePersistir_e_buscarDeveRecuperar() {
        ContratoAluguel contrato = criarContrato(
                idContrato("01"), cliente, item,
                dataRetirada, dataPrevDevolucao,
                null, new BigDecimal("250.00"), "ATIVO"
        );

        repositorio.salvar(contrato);

        ContratoAluguel resultado =
                repositorio.buscar(idContrato("01"));

        assertNotNull(resultado);
        assertEquals(idContrato("01"), resultado.getId());
        assertEquals("ATIVO", resultado.getStatus());
        assertEquals("I1", resultado.getItemId());
        assertEquals(cliente.getId(), resultado.getCliente().getId());
    }

    @Test
    @DisplayName("buscar: deve retornar null quando id não existe")
    void buscar_deveRetornarNull_quandoIdNaoExiste() {
        assertNull(repositorio.buscar(idContrato("INEXISTENTE")));
    }

    @Test
    @DisplayName("salvar: deve normalizar id para maiúsculo")
    void salvar_deveNormalizarId_paraMaiusculo() {
        ContratoAluguel contrato = criarContrato(
                idContrato("01").toLowerCase(),
                cliente, item,
                dataRetirada, dataPrevDevolucao,
                null, new BigDecimal("250.00"), "ATIVO"
        );

        repositorio.salvar(contrato);

        assertNotNull(repositorio.buscar(idContrato("01")));
    }

    // Listar

    @Test
    @DisplayName("listar: deve retornar todos os contratos salvos")
    void listar_deveRetornarContratosSalvos() {
        repositorio.salvar(criarContrato(
                idContrato("01"), cliente, item,
                dataRetirada, dataPrevDevolucao,
                null, new BigDecimal("250.00"), "ATIVO"
        ));

        repositorio.salvar(criarContrato(
                idContrato("02"), cliente, item,
                dataRetirada, dataPrevDevolucao,
                null, new BigDecimal("150.00"), "ENCERRADO"
        ));

        Map<String, ContratoAluguel> resultado =
                repositorio.listar();

        assertTrue(resultado.containsKey(idContrato("01")));
        assertTrue(resultado.containsKey(idContrato("02")));
    }

    @Test
    @DisplayName("listar por status: deve retornar apenas contratos ATIVOS")
    void listarPorStatus_deveRetornarApenasAtivos() {
        repositorio.salvar(criarContrato(
                idContrato("01"), cliente, item,
                dataRetirada, dataPrevDevolucao,
                null, new BigDecimal("250.00"), "ATIVO"
        ));

        repositorio.salvar(criarContrato(
                idContrato("02"), cliente, item,
                dataRetirada, dataPrevDevolucao,
                null, new BigDecimal("150.00"), "ENCERRADO"
        ));

        Map<String, ContratoAluguel> ativos =
                repositorio.listar("ATIVO");

        assertEquals(1, ativos.size());
        assertTrue(ativos.containsKey(idContrato("01")));
    }

    @Test
    @DisplayName("listar por cliente: deve retornar contratos do cliente informado")
    void listarPorCliente_deveRetornarContratosDoClienteCorreto() {
        repositorio.salvar(criarContrato(
                idContrato("01"), cliente, item,
                dataRetirada, dataPrevDevolucao,
                null, new BigDecimal("250.00"), "ATIVO"
        ));

        repositorio.salvar(criarContrato(
                idContrato("02"), outroCliente, item,
                dataRetirada, dataPrevDevolucao,
                null, new BigDecimal("150.00"), "ATIVO"
        ));

        Map<String, ContratoAluguel> resultado =
                repositorio.listar(cliente);

        assertEquals(1, resultado.size());
        assertTrue(resultado.containsKey(idContrato("01")));
    }

    // Atualizar

    @Test
    @DisplayName("atualizar: deve atualizar contrato existente")
    void atualizar_deveRetornarTrue_quandoContratoExiste() {
        ContratoAluguel contrato = criarContrato(
                idContrato("01"), cliente, item,
                dataRetirada, dataPrevDevolucao,
                null, new BigDecimal("250.00"), "ATIVO"
        );

        repositorio.salvar(contrato);

        contrato.setStatus("ENCERRADO");

        boolean resultado = repositorio.atualizar(contrato);

        assertTrue(resultado);
        assertEquals(
                "ENCERRADO",
                repositorio.buscar(idContrato("01")).getStatus()
        );
    }

    @Test
    @DisplayName("atualizar: deve retornar false quando contrato não existe")
    void atualizar_deveRetornarFalse_quandoContratoNaoExiste() {
        ContratoAluguel contrato = criarContrato(
                idContrato("INEXISTENTE"), cliente, item,
                dataRetirada, dataPrevDevolucao,
                null, new BigDecimal("250.00"), "ATIVO"
        );

        assertFalse(repositorio.atualizar(contrato));
    }

    // Deletar

    @Test
    @DisplayName("deletar: deve remover contrato existente")
    void deletar_deveRetornarTrue_e_removerContrato() {
        repositorio.salvar(criarContrato(
                idContrato("01"), cliente, item,
                dataRetirada, dataPrevDevolucao,
                null, new BigDecimal("250.00"), "ATIVO"
        ));

        boolean resultado =
                repositorio.deletar(idContrato("01"));

        assertTrue(resultado);
        assertNull(repositorio.buscar(idContrato("01")));
    }

    @Test
    @DisplayName("deletar: deve retornar false quando contrato não existe")
    void deletar_deveRetornarFalse_quandoContratoNaoExiste() {
        assertFalse(repositorio.deletar(idContrato("INEXISTENTE")));
    }

    // Datas e valores

    @Test
    @DisplayName("buscar: deve recuperar datas e valor total corretamente")
    void buscar_deveRecuperarDatasEValorTotal() {
        LocalDate dataEfetiva =
                LocalDate.of(2025, 1, 20);

        ContratoAluguel contrato = criarContrato(
                idContrato("01"), cliente, item,
                dataRetirada, dataPrevDevolucao,
                dataEfetiva, new BigDecimal("250.00"), "ENCERRADO"
        );

        repositorio.salvar(contrato);

        ContratoAluguel resultado =
                repositorio.buscar(idContrato("01"));

        assertNotNull(resultado);
        assertEquals(dataRetirada, resultado.getDataRetirada());
        assertEquals(dataPrevDevolucao, resultado.getDataPrevDevolucao());
        assertEquals(dataEfetiva, resultado.getDataEfetivaDevolucao());
        assertEquals(
                0,
                new BigDecimal("250.00").compareTo(resultado.getValorTotal())
        );
    }
}