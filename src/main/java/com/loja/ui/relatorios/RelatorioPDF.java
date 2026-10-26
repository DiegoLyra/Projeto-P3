package com.loja.ui.relatorios;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Gera o PDF de um {@link Relatorio}: texto preto, título, subtítulo,
 * uma tabela por seção (cabeçalho repetido em cada página) e rodapé com a página.
 */
public final class RelatorioPDF {

    private static final Font FONTE_TITULO = new Font(Font.HELVETICA, 16, Font.BOLD, Color.BLACK);
    private static final Font FONTE_SUBTITULO = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
    private static final Font FONTE_SECAO = new Font(Font.HELVETICA, 12, Font.BOLD, Color.BLACK);
    private static final Font FONTE_CABECALHO = new Font(Font.HELVETICA, 9, Font.BOLD, Color.BLACK);
    private static final Font FONTE_CELULA = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
    private static final Font FONTE_VAZIO = new Font(Font.HELVETICA, 9, Font.ITALIC, Color.BLACK);
    private static final Font FONTE_RESUMO = new Font(Font.HELVETICA, 10, Font.BOLD, Color.BLACK);
    private static final Font FONTE_RODAPE = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.BLACK);

    private static final Color FUNDO_CABECALHO = new Color(230, 230, 230);
    private static final Color COR_BORDA = new Color(170, 170, 170);

    private static final DateTimeFormatter FORMATO_EMISSAO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private RelatorioPDF() {
    }

    public static void gerar(Relatorio relatorio, File arquivo) throws IOException {
        Rectangle tamanho = relatorio.isPaisagem() ? PageSize.A4.rotate() : PageSize.A4;
        Document documento = new Document(tamanho, 40, 40, 40, 50);
        String emissao = "Emitido em " + LocalDateTime.now(ZoneId.of("America/Recife")).format(FORMATO_EMISSAO);

        // O Document é dono do arquivo: ao fechar o documento, ele grava o final do PDF e fecha o arquivo.
        // (Não usar try-with-resources aqui: ele fecharia o arquivo ANTES do documento terminar de gravar.)
        FileOutputStream saida = new FileOutputStream(arquivo);
        try {
            PdfWriter escritor = PdfWriter.getInstance(documento, saida);
            escritor.setPageEvent(new Rodape(emissao));
            documento.open();

            Paragraph titulo = new Paragraph(relatorio.getTitulo(), FONTE_TITULO);
            titulo.setSpacingAfter(4);
            documento.add(titulo);

            if (relatorio.getSubtitulo() != null && !relatorio.getSubtitulo().isEmpty()) {
                Paragraph subtitulo = new Paragraph(relatorio.getSubtitulo(), FONTE_SUBTITULO);
                subtitulo.setSpacingAfter(6);
                documento.add(subtitulo);
            }

            for (Relatorio.Secao secao : relatorio.getSecoes()) {
                adicionarSecao(documento, secao);
            }

            if (!relatorio.getResumo().isEmpty()) {
                Paragraph espaco = new Paragraph(" ", FONTE_RESUMO);
                documento.add(espaco);
                for (String linha : relatorio.getResumo()) {
                    documento.add(new Paragraph(linha, FONTE_RESUMO));
                }
            }
        } finally {
            if (documento.isOpen()) {
                documento.close(); // grava o final do PDF e fecha o arquivo
            } else {
                saida.close();     // deu erro antes de abrir o documento: só fecha o arquivo
            }
        }
    }

    private static void adicionarSecao(Document documento, Relatorio.Secao secao) {
        Paragraph titulo = new Paragraph(secao.getTitulo(), FONTE_SECAO);
        titulo.setSpacingBefore(14);
        titulo.setSpacingAfter(6);
        documento.add(titulo);

        if (secao.getLinhas().isEmpty()) {
            documento.add(new Paragraph(secao.getTextoVazio(), FONTE_VAZIO));
            return;
        }

        String[] colunas = secao.getColunas();
        PdfPTable tabela = new PdfPTable(colunas.length);
        tabela.setWidthPercentage(100);
        tabela.setHeaderRows(1); // repete o cabeçalho se a tabela continuar na próxima página

        for (String coluna : colunas) {
            tabela.addCell(celula(coluna, FONTE_CABECALHO, FUNDO_CABECALHO));
        }
        for (String[] linha : secao.getLinhas()) {
            for (String valor : linha) {
                tabela.addCell(celula(valor, FONTE_CELULA, null));
            }
        }
        documento.add(tabela);
    }

    private static PdfPCell celula(String texto, Font fonte, Color fundo) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, fonte));
        celula.setPadding(5);
        celula.setBorderWidth(0.5f);
        celula.setBorderColor(COR_BORDA);
        if (fundo != null) {
            celula.setBackgroundColor(fundo);
        }
        return celula;
    }

    /** Escreve "Emitido em ... | Página N" no pé de cada página. */
    private static class Rodape extends PdfPageEventHelper {
        private final String emissao;

        Rodape(String emissao) {
            this.emissao = emissao;
        }

        @Override
        public void onEndPage(PdfWriter escritor, Document documento) {
            Phrase texto = new Phrase(emissao + "   |   Página " + escritor.getPageNumber(), FONTE_RODAPE);
            float x = (documento.left() + documento.right()) / 2;
            float y = documento.bottom() - 25;
            ColumnText.showTextAligned(escritor.getDirectContent(), Element.ALIGN_CENTER, texto, x, y, 0);
        }
    }
}