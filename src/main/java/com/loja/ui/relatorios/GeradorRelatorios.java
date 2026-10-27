package com.loja.ui.relatorios;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import com.loja.model.Categoria;
import com.loja.model.ContratoAluguel;
import com.loja.model.Item;
import com.loja.model.Multa;
import com.loja.model.Usuario;
import com.loja.padrao.facade.interfaces.ILojaFacade;

/*
 Monta os relatórios da loja a partir da facade.
 Usado pelas telas de Admin e de Funcionário, para que as duas mostrem exatamente o mesmo relatório.
 Só fala com a fachada: não acessa business nem repositório.
 */
public class GeradorRelatorios {

    private static final ZoneId FUSO = ZoneId.of("America/Recife");
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String CONTRATO = "Contrato";
    private static final String VALOR = "Valor";
    private static final String RETIRADA = "Retirada";

    private final ILojaFacade facade;

    public GeradorRelatorios(ILojaFacade facade) {
        this.facade = facade;
    }

    //Itens disponíveis, agrupados pelas categorias cadastradas
    public Relatorio disponiveisPorCategoria() {
        Map<String, Item> disponiveis = facade.listarItemPorStatus("DISPONIVEL");
        Map<String, Categoria> categorias = facade.listarCategoria();

        Relatorio relatorio = new Relatorio("Itens Disponíveis por Categoria",
                "Situação em " + formatarData(hoje()));

        for (Categoria categoria : categorias.values()) {
            Relatorio.Secao secao = relatorio.novaSecao(categoria.getNome(), "ID", "Item", "Taxa diária")
                    .seVazia("Nenhum item disponível nesta categoria.");
            for (Item item : disponiveis.values()) {
                if (item.getCategoria() != null && categoria.getId().equals(item.getCategoria().getId())) {
                    secao.linha(item.getId(), item.getNome(), formatarMoeda(item.getTaxaDiaria()));
                }
            }
        }

        // Itens disponíveis cuja categoria não está cadastrada
        Relatorio.Secao semCategoria = null;
        for (Item item : disponiveis.values()) {
            if (!categoriaCadastrada(item, categorias)) {
                if (semCategoria == null) {
                    semCategoria = relatorio.novaSecao("Sem categoria", "ID", "Item", "Taxa diária");
                }
                semCategoria.linha(item.getId(), item.getNome(), formatarMoeda(item.getTaxaDiaria()));
            }
        }

        relatorio.adicionarResumo("Total de itens disponíveis: " + disponiveis.size());
        return relatorio;
    }

    //Aluguéis atuais, encerrados e irregularidades de um cliente
    public Relatorio historicoCliente(Usuario cliente) {
        Map<String, ContratoAluguel> contratos = facade.consultarHistoricoCliente(cliente.getId());
        Map<String, Multa> multas = facade.listarMultaPorCliente(cliente.getId());
        LocalDate dataHoje = hoje();

        Relatorio relatorio = new Relatorio("Histórico do Cliente",
                cliente.getNome() + " (#" + cliente.getId() + ")   -   Login: " + cliente.getLogin());

        Relatorio.Secao atuais = relatorio.novaSecao("Aluguéis atuais",
                CONTRATO, "Item", RETIRADA, "Prev. devolução", VALOR)
                .seVazia("Nenhum aluguel em andamento.");
        Relatorio.Secao encerrados = relatorio.novaSecao("Aluguéis encerrados",
                CONTRATO, "Item", RETIRADA, "Devolução", VALOR)
                .seVazia("Nenhum aluguel encerrado.");
        Relatorio.Secao atrasos = relatorio.novaSecao("Atrasos",
                CONTRATO, "Item", "Situação", "Dias de atraso")
                .seVazia("Nenhum atraso registrado.");
        Relatorio.Secao secaoMultas = relatorio.novaSecao("Multas",
                "Multa", "Motivo", VALOR, "Status")
                .seVazia("Nenhuma multa registrada.");

        for (ContratoAluguel c : contratos.values()) {
            String item = nomeDoItem(c);
            LocalDate previsao = c.getDataPrevDevolucao();
            LocalDate devolucao = c.getDataEfetivaDevolucao();

            if (devolucao == null) {
                atuais.linha(c.getId(), item, formatarData(c.getDataRetirada()),
                        formatarData(previsao), formatarMoeda(c.getValorTotal()));
                if (previsao != null && dataHoje.isAfter(previsao)) {
                    atrasos.linha(c.getId(), item, "Em atraso (não devolvido)",
                            ChronoUnit.DAYS.between(previsao, dataHoje));
                }
            } else {
                encerrados.linha(c.getId(), item, formatarData(c.getDataRetirada()),
                        formatarData(devolucao), formatarMoeda(c.getValorTotal()));
                if (previsao != null && devolucao.isAfter(previsao)) {
                    atrasos.linha(c.getId(), item, "Devolvido com atraso",
                            ChronoUnit.DAYS.between(previsao, devolucao));
                }
            }
        }

        for (Multa m : multas.values()) {
            secaoMultas.linha(m.getId(), m.getMotivo(), formatarMoeda(m.getValorTotal()), m.getStatus());
        }

        relatorio.adicionarResumo("Total de aluguéis: " + contratos.size());
        if (facade.possuiMultaPendente(cliente.getId())) {
            relatorio.adicionarResumo("Atenção: este cliente possui multa pendente.");
        }
        return relatorio;
    }

    /*
     Faturamento (aluguéis + multas) de um período.
     A fachada valida as datas e lança IllegalArgumentException se forem inválidas.
     */
    public Relatorio faturamento(LocalDate inicio, LocalDate fim) {
        String texto = facade.gerarRelatorioFaturamento(inicio, fim);

        Relatorio relatorio = new Relatorio("Relatório de Faturamento",
                "Período: " + formatarData(inicio) + " a " + formatarData(fim));
        Relatorio.Secao receitas = relatorio.novaSecao("Receitas do período", "Descrição", VALOR);

        // A fachada devolve o relatório em texto; aqui separamos as linhas "Descrição : R$ valor"
        for (String linha : texto.split("\n")) {
            int separador = linha.indexOf(':');
            if (separador > 0 && linha.contains("R$")) {
                receitas.linha(linha.substring(0, separador).trim(), linha.substring(separador + 1).trim());
            }
        }

        relatorio.adicionarResumo("Considera os aluguéis encerrados e as multas quitadas no período.");
        return relatorio;
    }

    //Contratos ativos: cliente, datas, situação (em dia/atrasado) e valor
    public Relatorio itensAlugados() {
        Map<String, ContratoAluguel> ativos = facade.listarContratosAtivos();

        Relatorio relatorio = new Relatorio("Itens Alugados no Momento", "Situação em " + formatarData(hoje()));
        relatorio.setPaisagem(true); // 8 colunas: a página deitada fica mais legível

        Relatorio.Secao secao = relatorio.novaSecao("Contratos ativos",
                CONTRATO, "Item", "Cliente", RETIRADA, "Prev. devolução", "Situação", "Dias de atraso", VALOR)
                .seVazia("Nenhum item alugado no momento.");

        int atrasados = 0;
        for (ContratoAluguel c : ativos.values()) {
            boolean atrasado = estaAtrasado(c);
            if (atrasado) {
                atrasados++;
            }
            secao.linha(c.getId(),
                    nomeDoItem(c),
                    c.getCliente() != null ? c.getCliente().getNome() : "-",
                    formatarData(c.getDataRetirada()),
                    formatarData(c.getDataPrevDevolucao()),
                    atrasado ? "Atrasado" : "Em dia",
                    atrasado ? String.valueOf(diasDeAtraso(c)) : "-",
                    formatarMoeda(c.getValorTotal()));
        }

        relatorio.adicionarResumo("Total de itens alugados: " + ativos.size());
        relatorio.adicionarResumo("Em atraso: " + atrasados);
        return relatorio;
    }

   //formatação

    public static LocalDate hoje() {
        return LocalDate.now(FUSO);
    }

    public static String formatarData(LocalDate data) {
        return data == null ? "-" : data.format(FORMATO_DATA);
    }

    public static String formatarMoeda(BigDecimal valor) {
        return valor == null ? "-" : String.format("R$ %.2f", valor);
    }

    public static boolean estaAtrasado(ContratoAluguel c) {
        return c.getDataPrevDevolucao() != null && hoje().isAfter(c.getDataPrevDevolucao());
    }

    public static long diasDeAtraso(ContratoAluguel c) {
        return estaAtrasado(c) ? ChronoUnit.DAYS.between(c.getDataPrevDevolucao(), hoje()) : 0;
    }

    //outros

    private static String nomeDoItem(ContratoAluguel c) {
        return c.getItem() != null ? c.getItem().getNome() : "-";
    }

    private static boolean categoriaCadastrada(Item item, Map<String, Categoria> categorias) {
        if (item.getCategoria() == null) {
            return false;
        }
        for (Categoria c : categorias.values()) {
            if (c.getId().equals(item.getCategoria().getId())) {
                return true;
            }
        }
        return false;
    }
}