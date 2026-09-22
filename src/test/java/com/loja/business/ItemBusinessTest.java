package com.loja.business;

import com.loja.exceptions.ItemException;
import com.loja.model.Categoria;
import com.loja.model.Fornecedor;
import com.loja.model.Item;
import com.loja.repositories.CategoriaRepositoryFake;
import com.loja.repositories.FornecedorRepositoryFake;
import com.loja.repositories.ItemRepositoryFake;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ItemBusinessTest {

    private ItemRepositoryFake itemRepo;
    private CategoriaRepositoryFake categoriaRepo;
    private FornecedorRepositoryFake fornecedorRepo;
    private ItemBusiness business;

    private Categoria categoria;
    private Fornecedor fornecedor;

    @BeforeEach
    void setUp() {
        itemRepo = new ItemRepositoryFake();
        categoriaRepo = new CategoriaRepositoryFake();
        fornecedorRepo = new FornecedorRepositoryFake();
        business = new ItemBusiness(itemRepo, categoriaRepo, fornecedorRepo);

        categoria = new Categoria("CAT1", "Ferramentas");
        categoriaRepo.salvar(categoria);

        fornecedor = new Fornecedor("FORN1", "Fornecedor A", "12345678000190", "81999999999");
        fornecedorRepo.salvar(fornecedor);
    }

    private Item criarItem(String id, String status) {
        return new Item(id, "Furadeira", new BigDecimal("10.00"), new BigDecimal("100.00"),
                status, categoria, fornecedor);
    }

    @Test
    @DisplayName("cadastrar: deve salvar item quando categoria e fornecedor existem")
    void cadastrar_deveSalvar_quandoDadosValidos() {
        Item item = criarItem("I1", "DISPONIVEL");

        business.cadastrar(item);

        assertEquals(item, itemRepo.buscar("I1"));
    }

    @Test
    @DisplayName("cadastrar: deve definir status DISPONIVEL quando não informado")
    void cadastrar_deveDefinirStatusPadrao_quandoNaoInformado() {
        Item item = criarItem("I1", null);

        business.cadastrar(item);

        assertEquals("DISPONIVEL", itemRepo.buscar("I1").getStatus());
    }

    @Test
    @DisplayName("cadastrar: deve definir status DISPONIVEL quando status está em branco")
    void cadastrar_deveDefinirStatusPadrao_quandoStatusEmBranco() {
        Item item = criarItem("I1", "   ");

        business.cadastrar(item);

        assertEquals("DISPONIVEL", itemRepo.buscar("I1").getStatus());
    }

    @Test
    @DisplayName("cadastrar: deve lançar exceção quando item já está cadastrado")
    void cadastrar_deveLancarExcecao_quandoItemJaCadastrado() {
        Item item = criarItem("I1", "DISPONIVEL");
        itemRepo.salvar(item);

        Item duplicado = criarItem("I1", "DISPONIVEL");

        assertThrows(ItemException.class, () -> business.cadastrar(duplicado));
    }

    @Test
    @DisplayName("cadastrar: deve lançar exceção quando categoria não está cadastrada")
    void cadastrar_deveLancarExcecao_quandoCategoriaNaoCadastrada() {
        Categoria categoriaInexistente = new Categoria("NAOEXISTE", "Inexistente");
        Item item = new Item("I1", "Furadeira", new BigDecimal("10.00"), new BigDecimal("100.00"),
                "DISPONIVEL", categoriaInexistente, fornecedor);

        assertThrows(ItemException.class, () -> business.cadastrar(item));
    }

    @Test
    @DisplayName("cadastrar: deve lançar exceção quando fornecedor não está cadastrado")
    void cadastrar_deveLancarExcecao_quandoFornecedorNaoCadastrado() {
        Fornecedor fornecedorInexistente = new Fornecedor("NAOEXISTE", "Inexistente", "0", "0");
        Item item = new Item("I1", "Furadeira", new BigDecimal("10.00"), new BigDecimal("100.00"),
                "DISPONIVEL", categoria, fornecedorInexistente);

        assertThrows(ItemException.class, () -> business.cadastrar(item));
    }

    @Test
    @DisplayName("buscar: deve retornar item quando id existe")
    void buscar_deveRetornarItem_quandoIdExiste() {
        Item item = criarItem("I1", "DISPONIVEL");
        itemRepo.salvar(item);

        assertEquals(item, business.buscar("I1"));
    }

    @Test
    @DisplayName("buscar: deve lançar exceção quando item não existe")
    void buscar_deveLancarExcecao_quandoItemNaoExiste() {
        assertThrows(ItemException.class, () -> business.buscar("INEXISTENTE"));
    }

    @Test
    @DisplayName("listar: deve retornar todos os itens cadastrados")
    void listar_deveRetornarTodosOsItens() {
        itemRepo.salvar(criarItem("I1", "DISPONIVEL"));
        itemRepo.salvar(criarItem("I2", "ALUGADO"));

        assertEquals(2, business.listar().size());
    }

    @Test
    @DisplayName("listarPorStatus: deve filtrar apenas o status solicitado")
    void listarPorStatus_deveFiltrarCorretamente() {
        itemRepo.salvar(criarItem("I1", "DISPONIVEL"));
        itemRepo.salvar(criarItem("I2", "ALUGADO"));

        Map<String, Item> resultado = business.listarPorStatus("DISPONIVEL");

        assertEquals(1, resultado.size());
        assertTrue(resultado.containsKey("I1"));
    }

    @Test
    @DisplayName("listarPorCategoria: deve retornar apenas itens da categoria informada")
    void listarPorCategoria_deveFiltrarCorretamente() {
        Categoria outraCategoria = new Categoria("CAT2", "Eletrônicos");
        categoriaRepo.salvar(outraCategoria);

        itemRepo.salvar(criarItem("I1", "DISPONIVEL"));
        Item itemOutraCategoria = new Item("I2", "TV", new BigDecimal("20.00"), new BigDecimal("200.00"),
                "DISPONIVEL", outraCategoria, fornecedor);
        itemRepo.salvar(itemOutraCategoria);

        Map<String, Item> resultado = business.listarPorCategoria(categoria);

        assertEquals(1, resultado.size());
        assertTrue(resultado.containsKey("I1"));
    }

    @Test
    @DisplayName("listarPorCategoria: deve lançar exceção quando categoria é nula")
    void listarPorCategoria_deveLancarExcecao_quandoCategoriaNula() {
        assertThrows(ItemException.class, () -> business.listarPorCategoria(null));
    }

    @Test
    @DisplayName("listarPorCategoria: deve lançar exceção quando nome da categoria é inválido")
    void listarPorCategoria_deveLancarExcecao_quandoNomeInvalido() {
        Categoria semNome = new Categoria("CAT9", " ");

        assertThrows(ItemException.class, () -> business.listarPorCategoria(semNome));
    }

    @Test
    @DisplayName("listarPorCategoria: deve lançar exceção quando categoria não está cadastrada")
    void listarPorCategoria_deveLancarExcecao_quandoCategoriaNaoCadastrada() {
        Categoria naoCadastrada = new Categoria("NAOEXISTE", "Inexistente");

        assertThrows(ItemException.class, () -> business.listarPorCategoria(naoCadastrada));
    }

    @Test
    @DisplayName("listarPorFornecedor: deve retornar apenas itens do fornecedor informado")
    void listarPorFornecedor_deveFiltrarCorretamente() {
        Fornecedor outroFornecedor = new Fornecedor("FORN2", "Fornecedor B", "0", "0");
        fornecedorRepo.salvar(outroFornecedor);

        itemRepo.salvar(criarItem("I1", "DISPONIVEL"));
        Item itemOutroFornecedor = new Item("I2", "TV", new BigDecimal("20.00"), new BigDecimal("200.00"),
                "DISPONIVEL", categoria, outroFornecedor);
        itemRepo.salvar(itemOutroFornecedor);

        Map<String, Item> resultado = business.listarPorFornecedor(fornecedor);

        assertEquals(1, resultado.size());
        assertTrue(resultado.containsKey("I1"));
    }

    @Test
    @DisplayName("listarPorFornecedor: deve lançar exceção quando fornecedor é nulo")
    void listarPorFornecedor_deveLancarExcecao_quandoFornecedorNulo() {
        assertThrows(ItemException.class, () -> business.listarPorFornecedor(null));
    }

    @Test
    @DisplayName("listarPorFornecedor: deve lançar exceção quando nome do fornecedor é inválido")
    void listarPorFornecedor_deveLancarExcecao_quandoNomeInvalido() {
        Fornecedor semNome = new Fornecedor("FORN9", " ", "0", "0");

        assertThrows(ItemException.class, () -> business.listarPorFornecedor(semNome));
    }

    @Test
    @DisplayName("listarPorFornecedor: deve lançar exceção quando fornecedor não está cadastrado")
    void listarPorFornecedor_deveLancarExcecao_quandoFornecedorNaoCadastrado() {
        Fornecedor naoCadastrado = new Fornecedor("NAOEXISTE", "Inexistente", "0", "0");

        assertThrows(ItemException.class, () -> business.listarPorFornecedor(naoCadastrado));
    }

    @Test
    @DisplayName("atualizar: deve atualizar item existente")
    void atualizar_deveAtualizar_quandoItemExiste() {
        itemRepo.salvar(criarItem("I1", "DISPONIVEL"));

        Item atualizado = criarItem("I1", "ALUGADO");
        business.atualizar(atualizado);

        assertEquals("ALUGADO", itemRepo.buscar("I1").getStatus());
    }

    @Test
    @DisplayName("atualizar: deve lançar exceção quando item é nulo")
    void atualizar_deveLancarExcecao_quandoItemNulo() {
        assertThrows(ItemException.class, () -> business.atualizar(null));
    }

    @Test
    @DisplayName("atualizar: deve lançar exceção quando item não existe")
    void atualizar_deveLancarExcecao_quandoItemNaoExiste() {
        Item inexistente = criarItem("NAOEXISTE", "DISPONIVEL");

        assertThrows(ItemException.class, () -> business.atualizar(inexistente));
    }

    @Test
    @DisplayName("deletar: deve remover item sem histórico")
    void deletar_deveRemover_quandoSemHistorico() {
        itemRepo.salvar(criarItem("I1", "DISPONIVEL"));

        business.deletar("I1");

        assertNull(itemRepo.buscar("I1"));
    }

    @Test
    @DisplayName("deletar: deve lançar exceção quando item não existe")
    void deletar_deveLancarExcecao_quandoItemNaoExiste() {
        assertThrows(ItemException.class, () -> business.deletar("NAOEXISTE"));
    }

    @Test
    @DisplayName("deletar: deve lançar exceção quando item possui histórico")
    void deletar_deveLancarExcecao_quandoTemHistorico() {
        Item item = criarItem("I1", "DISPONIVEL");
        item.setHistorico(true);
        itemRepo.salvar(item);

        assertThrows(ItemException.class, () -> business.deletar("I1"));
        assertNotNull(itemRepo.buscar("I1"));
    }
}