package com.loja.repositories;

import com.loja.database.Conexao;
import com.loja.exceptions.PersistenciaException;
import com.loja.model.Categoria;
import com.loja.repositories.interfaces.ICategoriaRepository;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class CategoriaPersistenciaJPA implements ICategoriaRepository {

    @Override
    public void salvar(Categoria categoria) {
        String sql = "INSERT INTO categorias (id, nome, historico) VALUES (?, ?, ?)";
        Connection conn = Conexao.getConexao();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, categoria.getId().toUpperCase());
            pstmt.setString(2, categoria.getNome());
            pstmt.setInt(3, categoria.hasHistorico() ? 1 : 0);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao salvar categoria: " + categoria.getId(), e);
        }
    }

    @Override
    public Categoria buscar(String id) {
        String sql = "SELECT id, nome, historico FROM categorias WHERE id = ?";
        Connection conn = Conexao.getConexao();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id.toUpperCase());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearCategoria(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao buscar categoria: " + id, e);
        }
    }

    @Override
    public Map<String, Categoria> listar() {
        String sql = "SELECT id, nome, historico FROM categorias";
        Map<String, Categoria> categorias = new HashMap<>();
        Connection conn = Conexao.getConexao();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Categoria categoria = mapearCategoria(rs);
                categorias.put(categoria.getId(), categoria);
            }
            return categorias;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao listar categorias.", e);
        }
    }

    @Override
    public boolean atualizar(Categoria categoria) {
        String sql = "UPDATE categorias SET nome = ?, historico = ? WHERE id = ?";
        Connection conn = Conexao.getConexao();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, categoria.getNome());
            pstmt.setInt(2, categoria.hasHistorico() ? 1 : 0);
            pstmt.setString(3, categoria.getId().toUpperCase());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao atualizar categoria: " + categoria.getId(), e);
        }
    }

    @Override
    public boolean deletar(String id) {
        String sql = "DELETE FROM categorias WHERE id = ?";
        Connection conn = Conexao.getConexao();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id.toUpperCase());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao deletar categoria: " + id, e);
        }
    }

    private Categoria mapearCategoria(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria(rs.getString("id"), rs.getString("nome"));
        categoria.setHistorico(rs.getInt("historico") == 1);
        return categoria;
    }
}