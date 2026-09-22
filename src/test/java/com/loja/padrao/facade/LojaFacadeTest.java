package com.loja.padrao.facade;

import com.loja.business.interfaces.*;
import com.loja.exceptions.ItemException;
import com.loja.exceptions.PersistenciaException;
import com.loja.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LojaFacadeTest {

    @Mock private IUsuarioBusiness usuarioBusiness;
    @Mock private IItemBusiness itemBusiness;
    @Mock private ICategoriaBusiness categoriaBusiness;
    @Mock private IFornecedorBusiness fornecedorBusiness;
    @Mock private IContratoBusiness contratoBusiness;
    @Mock private IMultaBusiness multaBusiness;

    private LojaFacade facade;

    @BeforeEach
    void setUp() {
        when(itemBusiness.listar()).thenReturn(Collections.emptyMap());
        when(contratoBusiness.listar()).thenReturn(Collections.emptyMap());
        when(multaBusiness.listar()).thenReturn(Collections.emptyMap());

        facade = new LojaFacade(usuarioBusiness, itemBusiness, categoriaBusiness,
                fornecedorBusiness, contratoBusiness, multaBusiness);

        // O construtor de LojaFacade chama resolverDependencias(), que já interage
        // com esses mocks (ex.: contratoBusiness.listar(), multaBusiness.listar()).
        // Limpamos o histórico de invocações (sem remover os stubs feitos acima)
        // para que os testes de verifyNoInteractions considerem apenas as
        // interações geradas pelo próprio corpo do teste.
        clearInvocations(usuarioBusiness, itemBusiness, categoriaBusiness,
                fornecedorBusiness, contratoBusiness, multaBusiness);
    }

    @Test
    @DisplayName("cadastrarCliente: deve delegar para usuarioBusiness quando cliente não é nulo")
    void cadastrarCliente_deveDelegar_quandoClienteValido() {
        Cliente cliente = new Cliente("1", "João", "joao@email.com", "123");

        facade.cadastrarCliente(cliente);

        verify(usuarioBusiness).cadastrar(cliente);
    }

    @Test
    @DisplayName("cadastrarCliente: deve lançar exceção quando cliente é nulo")
    void cadastrarCliente_deveLancarExcecao_quandoNulo() {
        assertThrows(IllegalArgumentException.class, () -> facade.cadastrarCliente(null));
        verifyNoInteractions(usuarioBusiness);
    }

    @Test
    @DisplayName("cadastrarFuncionario: deve lançar exceção quando funcionário é nulo")
    void cadastrarFuncionario_deveLancarExcecao_quandoNulo() {
        assertThrows(IllegalArgumentException.class, () -> facade.cadastrarFuncionario(null));
        verifyNoInteractions(usuarioBusiness);
    }

    @Test
    @DisplayName("cadastrarAdm: deve lançar exceção quando administrador é nulo")
    void cadastrarAdm_deveLancarExcecao_quandoNulo() {
        assertThrows(IllegalArgumentException.class, () -> facade.cadastrarAdm(null));
        verifyNoInteractions(usuarioBusiness);
    }

    @Test
    @DisplayName("buscarUsuario: deve delegar quando id é válido")
    void buscarUsuario_deveDelegar_quandoIdValido() {
        Cliente cliente = new Cliente("1", "João", "joao@email.com", "123");
        when(usuarioBusiness.buscarPorId("1")).thenReturn(cliente);

        Usuario resultado = facade.buscarUsuario("1");

        assertEquals(cliente, resultado);
        verify(usuarioBusiness).buscarPorId("1");
    }

    @Test
    @DisplayName("buscarUsuario: deve lançar exceção quando id é nulo ou vazio")
    void buscarUsuario_deveLancarExcecao_quandoIdInvalido() {
        assertThrows(IllegalArgumentException.class, () -> facade.buscarUsuario(null));
        assertThrows(IllegalArgumentException.class, () -> facade.buscarUsuario("  "));
        verifyNoInteractions(usuarioBusiness);
    }

    @Test
    @DisplayName("autenticarUsuario: deve lançar exceção quando email ou senha são nulos")
    void autenticarUsuario_deveLancarExcecao_quandoEmailOuSenhaNulos() {
        assertThrows(IllegalArgumentException.class, () -> facade.autenticarUsuario(null, "123"));
        assertThrows(IllegalArgumentException.class, () -> facade.autenticarUsuario("a@a.com", null));
        verifyNoInteractions(usuarioBusiness);
    }

    @Test
    @DisplayName("autenticarUsuario: deve delegar quando email e senha são válidos")
    void autenticarUsuario_deveDelegar_quandoValido() {
        Cliente cliente = new Cliente("1", "João", "joao@email.com", "123");
        when(usuarioBusiness.autenticar("joao@email.com", "123")).thenReturn(cliente);

        Usuario resultado = facade.autenticarUsuario("joao@email.com", "123");

        assertEquals(cliente, resultado);
    }

    @Test
    @DisplayName("listarUsuarioPorPerfil: deve lançar exceção quando perfil é inválido")
    void listarUsuarioPorPerfil_deveLancarExcecao_quandoPerfilInvalido() {
        assertThrows(IllegalArgumentException.class, () -> facade.listarUsuarioPorPerfil(null));
        assertThrows(IllegalArgumentException.class, () -> facade.listarUsuarioPorPerfil(""));
    }

    @Test
    @DisplayName("atualizarUsuario: deve lançar exceção quando usuário é nulo")
    void atualizarUsuario_deveLancarExcecao_quandoNulo() {
        assertThrows(IllegalArgumentException.class, () -> facade.atualizarUsuario("1", null));
    }

    @Test
    @DisplayName("desativarUsuario: deve buscar, desativar e atualizar o usuário")
    void desativarUsuario_deveDesativarEAtualizar() {
        Cliente cliente = new Cliente("1", "João", "joao@email.com", "123");
        cliente.setAtivo(true);
        when(usuarioBusiness.buscarPorId("1")).thenReturn(cliente);

        facade.desativarUsuario("1");

        assertFalse(cliente.isAtivo());
        verify(usuarioBusiness).atualizar(cliente);
    }

    @Test
    @DisplayName("desativarUsuario: deve lançar exceção quando id é inválido")
    void desativarUsuario_deveLancarExcecao_quandoIdInvalido() {
        assertThrows(IllegalArgumentException.class, () -> facade.desativarUsuario(" "));
        verifyNoInteractions(usuarioBusiness);
    }

    @Test
    @DisplayName("buscarContrato: deve lançar exceção quando id é inválido")
    void buscarContrato_deveLancarExcecao_quandoIdInvalido() {
        assertThrows(IllegalArgumentException.class, () -> facade.buscarContrato(null));
        verifyNoInteractions(contratoBusiness);
    }

    @Test
    @DisplayName("processarDevolucao: deve aplicar multa quando há atraso")
    void processarDevolucao_deveAplicarMulta_quandoHaAtraso() {
        ContratoAluguel contrato = new ContratoAluguel();
        contrato.setId("CT1");
        when(contratoBusiness.processarDevolucao("CT1")).thenReturn(contrato);
        when(multaBusiness.calcularAtraso(contrato)).thenReturn(new BigDecimal("30.00"));

        ContratoAluguel resultado = facade.processarDevolucao("CT1");

        assertEquals(contrato, resultado);
        verify(multaBusiness).aplicar(contrato);
    }

    @Test
    @DisplayName("processarDevolucao: não deve aplicar multa quando não há atraso")
    void processarDevolucao_naoDeveAplicarMulta_quandoSemAtraso() {
        ContratoAluguel contrato = new ContratoAluguel();
        contrato.setId("CT1");
        when(contratoBusiness.processarDevolucao("CT1")).thenReturn(contrato);
        when(multaBusiness.calcularAtraso(contrato)).thenReturn(BigDecimal.ZERO);

        facade.processarDevolucao("CT1");

        verify(multaBusiness, never()).aplicar(any());
    }

    @Test
    @DisplayName("processarDevolucao: deve lançar exceção quando id é inválido")
    void processarDevolucao_deveLancarExcecao_quandoIdInvalido() {
        assertThrows(IllegalArgumentException.class, () -> facade.processarDevolucao(""));
        verifyNoInteractions(contratoBusiness, multaBusiness);
    }

    @Test
    @DisplayName("consultarHistoricoCliente: deve lançar exceção quando id do cliente é inválido")
    void consultarHistoricoCliente_deveLancarExcecao_quandoIdInvalido() {
        assertThrows(IllegalArgumentException.class, () -> facade.consultarHistoricoCliente(null));
        verifyNoInteractions(contratoBusiness);
    }

    @Test
    @DisplayName("atualizarItem: deve delegar quando status não muda")
    void atualizarItem_deveDelegar_quandoStatusIgual() {
        Item existente = new Item();
        existente.setId("I1");
        existente.setStatus("DISPONIVEL");
        when(itemBusiness.buscar("I1")).thenReturn(existente);

        Item atualizado = new Item();
        atualizado.setId("I1");
        atualizado.setStatus("DISPONIVEL");

        facade.atualizarItem(atualizado);

        verify(itemBusiness).atualizar(atualizado);
    }

    @Test
    @DisplayName("atualizarItem: deve lançar exceção quando status é alterado diretamente")
    void atualizarItem_deveLancarExcecao_quandoStatusMuda() {
        Item existente = new Item();
        existente.setId("I1");
        existente.setStatus("DISPONIVEL");
        when(itemBusiness.buscar("I1")).thenReturn(existente);

        Item atualizado = new Item();
        atualizado.setId("I1");
        atualizado.setStatus("ALUGADO");

        assertThrows(ItemException.class, () -> facade.atualizarItem(atualizado));
        verify(itemBusiness, never()).atualizar(any());
    }

    @Test
    @DisplayName("aplicarMulta: deve lançar exceção quando contrato é nulo")
    void aplicarMulta_deveLancarExcecao_quandoContratoNulo() {
        assertThrows(IllegalArgumentException.class, () -> facade.aplicarMulta(null));
        verifyNoInteractions(multaBusiness);
    }

    @Test
    @DisplayName("quitarMulta: deve lançar exceção quando id é inválido")
    void quitarMulta_deveLancarExcecao_quandoIdInvalido() {
        assertThrows(IllegalArgumentException.class, () -> facade.quitarMulta(" "));
        verifyNoInteractions(multaBusiness);
    }

    @Test
    @DisplayName("possuiMultaPendente: deve retornar false quando id do cliente é inválido, sem consultar o repositório")
    void possuiMultaPendente_deveRetornarFalse_quandoIdInvalido() {
        assertFalse(facade.possuiMultaPendente(null));
        assertFalse(facade.possuiMultaPendente(""));
        verifyNoInteractions(multaBusiness);
    }

    @Test
    @DisplayName("possuiMultaPendente: deve delegar quando id é válido")
    void possuiMultaPendente_deveDelegar_quandoIdValido() {
        when(multaBusiness.possuiMultaPendente("C1")).thenReturn(true);

        assertTrue(facade.possuiMultaPendente("C1"));
    }

    @Test
    @DisplayName("listarMultaPorCliente: deve lançar exceção quando id do cliente é inválido")
    void listarMultaPorCliente_deveLancarExcecao_quandoIdInvalido() {
        assertThrows(IllegalArgumentException.class, () -> facade.listarMultaPorCliente(null));
    }

    @Test
    @DisplayName("deletarMulta: deve lançar exceção quando id é inválido")
    void deletarMulta_deveLancarExcecao_quandoIdInvalido() {
        assertThrows(IllegalArgumentException.class, () -> facade.deletarMulta(""));
        verifyNoInteractions(multaBusiness);
    }

    @Test
    @DisplayName("gerarRelatorioItensAlugados: deve retornar mensagem padrão quando não há ativos")
    void gerarRelatorioItensAlugados_deveRetornarMensagemPadrao_quandoVazio() {
        when(contratoBusiness.listarAtivos()).thenReturn(Collections.emptyMap());

        String relatorio = facade.gerarRelatorioItensAlugados();

        assertEquals("Nenhum item alugado no momento.", relatorio);
    }

    @Test
    @DisplayName("gerarRelatorioFaturamento: deve lançar exceção quando datas são inválidas")
    void gerarRelatorioFaturamento_deveLancarExcecao_quandoDataInicialAposFinal() {
        LocalDate inicio = LocalDate.of(2025, 2, 1);
        LocalDate fim = LocalDate.of(2025, 1, 1);

        assertThrows(IllegalArgumentException.class,
                () -> facade.gerarRelatorioFaturamento(inicio, fim));
    }

    @Test
    @DisplayName("gerarRelatorioFaturamento: deve lançar exceção quando datas são nulas")
    void gerarRelatorioFaturamento_deveLancarExcecao_quandoDatasNulas() {
        assertThrows(IllegalArgumentException.class,
                () -> facade.gerarRelatorioFaturamento(null, LocalDate.now()));
    }

    @Test
    @DisplayName("salvarTudo: deve chamar salvarDados de todas as camadas de negócio")
    void salvarTudo_deveChamarSalvarDadosDeTodasAsBusiness() {
        facade.salvarTudo();

        verify(usuarioBusiness).salvarDados();
        verify(itemBusiness).salvarDados();
        verify(categoriaBusiness).salvarDados();
        verify(fornecedorBusiness).salvarDados();
        verify(contratoBusiness).salvarDados();
        verify(multaBusiness).salvarDados();
    }

    @Test
    @DisplayName("salvarTudo: não deve propagar exceção quando uma camada falha ao salvar")
    void salvarTudo_naoDevePropagarExcecao_quandoUmaCamadaFalha() {
        // salvarComTratamento só captura PersistenciaException (ver LojaFacade),
        // então o teste precisa lançar esse tipo específico de exceção.
        doThrow(new PersistenciaException("Falha ao salvar")).when(itemBusiness).salvarDados();

        assertDoesNotThrow(() -> facade.salvarTudo());
        verify(usuarioBusiness).salvarDados();
        verify(categoriaBusiness).salvarDados();
        verify(fornecedorBusiness).salvarDados();
        verify(contratoBusiness).salvarDados();
        verify(multaBusiness).salvarDados();
    }

    @Test
    @DisplayName("listarItensDisponiveis: deve delegar para itemBusiness com o status DISPONIVEL")
    void listarItensDisponiveis_deveDelegarComStatusDisponivel() {
        Map<String, Item> disponiveis = Map.of("I1", new Item());
        when(itemBusiness.listarPorStatus("DISPONIVEL")).thenReturn(disponiveis);

        Map<String, Item> resultado = facade.listarItensDisponiveis();

        assertEquals(disponiveis, resultado);
    }

    @Test
    @DisplayName("listarContratosAtivos: deve delegar para contratoBusiness")
    void listarContratosAtivos_deveDelegar() {
        facade.listarContratosAtivos();

        verify(contratoBusiness).listarAtivos();
    }
}