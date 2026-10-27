package com.loja.ui.relatorios;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Relatorio {

    private final String titulo;
    private final String subtitulo;
    private final List<Secao> secoes = new ArrayList<>();
    private final List<String> resumo = new ArrayList<>();
    private boolean paisagem;

    public Relatorio(String titulo, String subtitulo) {
        this.titulo = titulo;
        this.subtitulo = subtitulo;
    }

    // Cria uma seção (tabela) com as colunas informadas e a adiciona ao relatório
    public Secao novaSecao(String tituloSecao, String... colunas) {
        Secao secao = new Secao(tituloSecao, colunas);
        secoes.add(secao);
        return secao;
    }

    //Linha de texto mostrada no final do relatório (totais, avisos)
    public void adicionarResumo(String linha) {
        resumo.add(linha);
    }

    public String getTitulo() {
        return titulo;
    }

    public String getSubtitulo() {
        return subtitulo;
    }

    public List<Secao> getSecoes() {
        return Collections.unmodifiableList(secoes);
    }

    public List<String> getResumo() {
        return Collections.unmodifiableList(resumo);
    }

    //Página deitada, para tabelas com muitas colunas
    public boolean isPaisagem() {
        return paisagem;
    }

    public void setPaisagem(boolean paisagem) {
        this.paisagem = paisagem;
    }

    //Nome sugerido para o arquivo: "Histórico do Cliente" vira "historico-do-cliente"
    public String getNomeArquivo() {
        String semAcento = Normalizer.normalize(titulo, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String nome = semAcento.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        return nome.isEmpty() ? "relatorio" : nome;
    }

    //Versão em texto, usada na janela de pré-visualização
    public String comoTexto() {
        StringBuilder sb = new StringBuilder();
        sb.append(titulo.toUpperCase()).append('\n');
        if (subtitulo != null && !subtitulo.isEmpty()) {
            sb.append(subtitulo).append('\n');
        }
        sb.append('\n');
        for (Secao secao : secoes) {
            secao.escreverTexto(sb);
        }
        for (String linha : resumo) {
            sb.append(linha).append('\n');
        }
        return sb.toString();
    }

    //Uma tabela do relatório
    public static class Secao {
        private final String titulo;
        private final String[] colunas;
        private final List<String[]> linhas = new ArrayList<>();
        private String textoVazio = "Nenhum registro.";

        private Secao(String titulo, String[] colunas) {
            this.titulo = titulo;
            this.colunas = colunas.clone();
        }

        //Adiciona uma linha. Valores nulos aparecem como "-"
        public Secao linha(Object... valores) {
            String[] textos = new String[colunas.length];
            for (int i = 0; i < colunas.length; i++) {
                Object valor = i < valores.length ? valores[i] : null;
                textos[i] = valor == null ? "-" : String.valueOf(valor);
            }
            linhas.add(textos);
            return this;
        }

        //Texto mostrado quando a seção não tem nenhuma linha
        public Secao seVazia(String texto) {
            this.textoVazio = texto;
            return this;
        }

        public String getTitulo() {
            return titulo;
        }

        public String[] getColunas() {
            return colunas.clone();
        }

        public List<String[]> getLinhas() {
            return Collections.unmodifiableList(linhas);
        }

        public String getTextoVazio() {
            return textoVazio;
        }

        private void escreverTexto(StringBuilder sb) {
            sb.append(titulo).append('\n');
            if (linhas.isEmpty()) {
                sb.append("   ").append(textoVazio).append("\n\n");
                return;
            }

            // Largura de cada coluna = maior texto entre o cabeçalho e as linhas
            int[] larguras = new int[colunas.length];
            for (int i = 0; i < colunas.length; i++) {
                larguras[i] = colunas[i].length();
            }
            for (String[] linha : linhas) {
                for (int i = 0; i < linha.length; i++) {
                    larguras[i] = Math.max(larguras[i], linha[i].length());
                }
            }

            String[] separador = new String[colunas.length];
            for (int i = 0; i < colunas.length; i++) {
                separador[i] = "-".repeat(larguras[i]);
            }

            escreverLinha(sb, colunas, larguras);
            escreverLinha(sb, separador, larguras);
            for (String[] linha : linhas) {
                escreverLinha(sb, linha, larguras);
            }
            sb.append('\n');
        }

        private static void escreverLinha(StringBuilder sb, String[] valores, int[] larguras) {
            sb.append("   ");
            for (int i = 0; i < valores.length; i++) {
                sb.append(valores[i]);
                if (i < valores.length - 1) {
                    for (int k = valores[i].length(); k < larguras[i] + 2; k++) {
                        sb.append(' ');
                    }
                }
            }
            sb.append('\n');
        }
    }
}