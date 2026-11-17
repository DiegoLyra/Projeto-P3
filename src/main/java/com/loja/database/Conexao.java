package com.loja.database;

import com.loja.exceptions.PersistenciaException;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Conexao {
    // Salva na mesma pasta "dados/" que vocês já usam para os CSVs
    private static final String URL = "jdbc:sqlite:dados/loja.db";
    private static Connection conexao = null;

    private Conexao() {}

    public static Connection getConexao() {
        try {
            if (conexao == null || conexao.isClosed()) {
                // Garante que a pasta "dados" exista antes de criar o .db
                File pastaDados = new File("dados");
                if (!pastaDados.exists()) {
                    pastaDados.mkdirs();
                }

                conexao = DriverManager.getConnection(URL);
                inicializarBanco(conexao);
            }
            return conexao;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro crítico ao conectar com o banco SQLite.", e);
        }
    }

    private static void inicializarBanco(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS categorias (
                    id TEXT PRIMARY KEY,
                    nome TEXT NOT NULL,
                    historico INTEGER NOT NULL
                );
                """);
        }
    }

    public static void fecharConexao() {
        try {
            if (conexao != null && !conexao.isClosed()) {
                conexao.close();
                System.out.println("Conexão com SQLite encerrada com segurança.");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao fechar conexão SQLite: " + e.getMessage());
        }
    }
}