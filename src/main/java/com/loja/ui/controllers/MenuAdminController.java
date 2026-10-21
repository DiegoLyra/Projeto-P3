package com.loja.ui.controllers;


import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loja.model.Administrador;
import com.loja.model.Categoria;
import com.loja.model.Cliente;
import com.loja.model.Fornecedor;
import com.loja.model.Funcionario;
import com.loja.model.Item;
import com.loja.model.Usuario;
import com.loja.padrao.facade.interfaces.ILojaFacade;
import com.loja.ui.components.Banner;
import com.loja.ui.components.EmptyState;
import com.loja.ui.components.FormField;
import com.loja.ui.components.PillNav;
import com.loja.ui.components.StatusBadge;
import com.loja.ui.navigation.Navigator;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;


public class MenuAdminController {


    private static final Logger logger = LoggerFactory.getLogger(MenuAdminController.class);

    private static final String PERFIL_CLIENTE = "CLIENTE";
    private static final String PERFIL_FUNCIONARIO = "FUNCIONARIO";
    private static final String PERFIL_ADMINISTRADOR = "ADMINISTRADOR";
    private static final String ACAO_REMOVER = "Remover";

    private final ILojaFacade facade;
    private final Navigator navigator;
    private Administrador usuarioLogado;


    // Listas que alimentam as tabelas e os ComboBox
    private final ObservableList<Usuario> usuarios = FXCollections.observableArrayList();
    private final ObservableList<Item> itens = FXCollections.observableArrayList();
    private final ObservableList<Categoria> categorias = FXCollections.observableArrayList();
    private final ObservableList<Fornecedor> fornecedores = FXCollections.observableArrayList();


    // Cabeçalho e Navegação
    @FXML private Label lblBoasVindas;
    @FXML private PillNav navModulos;


    // Painéis de Conteúdo
    @FXML private VBox paneUsuarios;
    @FXML private VBox paneItens;
    @FXML private VBox paneCategorias;
    @FXML private VBox paneFornecedores;


    // Usuários
    @FXML private Label lblFormUsuario;
    @FXML private HBox badgeUsuario;
    @FXML private FormField fUsuarioId;
    @FXML private ComboBox<String> cbUsuarioPerfil;
    @FXML private FormField fUsuarioNome;
    @FXML private FormField fUsuarioLogin;
    @FXML private FormField fUsuarioSenha;
    @FXML private FormField fUsuarioCargo;
    @FXML private Button btnSalvarUsuario;
    @FXML private Button btnCancelarUsuario;
    @FXML private VBox feedbackUsuario;


    @FXML private TableView<Usuario> tblUsuarios;
    @FXML private TableColumn<Usuario, String> colUsuarioId;
    @FXML private TableColumn<Usuario, String> colUsuarioPerfil;
    @FXML private TableColumn<Usuario, String> colUsuarioNome;
    @FXML private TableColumn<Usuario, String> colUsuarioLogin;
    @FXML private TableColumn<Usuario, String> colUsuarioCargo;
    @FXML private TableColumn<Usuario, String> colUsuarioSituacao;
    @FXML private TableColumn<Usuario, Void> colUsuarioAcoes;


    // Itens
    @FXML private Label lblFormItem;
    @FXML private HBox badgeItem;
    @FXML private FormField fItemId;
    @FXML private FormField fItemNome;
    @FXML private ComboBox<Categoria> cbItemCategoria;
    @FXML private ComboBox<Fornecedor> cbItemFornecedor;
    @FXML private FormField fItemTaxa;
    @FXML private FormField fItemReposicao;
    @FXML private Button btnSalvarItem;
    @FXML private Button btnCancelarItem;
    @FXML private VBox feedbackItem;


    @FXML private TableView<Item> tblItens;
    @FXML private TableColumn<Item, String> colItemId;
    @FXML private TableColumn<Item, String> colItemNome;
    @FXML private TableColumn<Item, String> colItemStatus;
    @FXML private TableColumn<Item, String> colItemCategoria;
    @FXML private TableColumn<Item, String> colItemFornecedor;
    @FXML private TableColumn<Item, String> colItemTaxa;
    @FXML private TableColumn<Item, Void> colItemAcoes;


    // Categorias
    @FXML private Label lblFormCategoria;
    @FXML private HBox badgeCategoria;
    @FXML private FormField fCategoriaId;
    @FXML private FormField fCategoriaNome;
    @FXML private Button btnSalvarCategoria;
    @FXML private Button btnCancelarCategoria;
    @FXML private VBox feedbackCategoria;


    @FXML private TableView<Categoria> tblCategorias;
    @FXML private TableColumn<Categoria, String> colCategoriaId;
    @FXML private TableColumn<Categoria, String> colCategoriaNome;
    @FXML private TableColumn<Categoria, Void> colCategoriaAcoes;


    // Fornecedores
    @FXML private Label lblFormFornecedor;
    @FXML private HBox badgeFornecedor;
    @FXML private FormField fFornecedorId;
    @FXML private FormField fFornecedorNome;
    @FXML private FormField fFornecedorCnpj;
    @FXML private FormField fFornecedorTelefone;
    @FXML private Button btnSalvarFornecedor;
    @FXML private Button btnCancelarFornecedor;
    @FXML private VBox feedbackFornecedor;


    @FXML private TableView<Fornecedor> tblFornecedores;
    @FXML private TableColumn<Fornecedor, String> colFornecedorId;
    @FXML private TableColumn<Fornecedor, String> colFornecedorNome;
    @FXML private TableColumn<Fornecedor, String> colFornecedorCnpj;
    @FXML private TableColumn<Fornecedor, String> colFornecedorTelefone;
    @FXML private TableColumn<Fornecedor, Void> colFornecedorAcoes;


    // Controle "Cadastrar <-> Modo de Edição" de cada formulário
    private FormState formUsuario;
    private FormState formItem;
    private FormState formCategoria;
    private FormState formFornecedor;


    // Construtor exigido pelo Navigator (controllerFactory)
    public MenuAdminController(ILojaFacade facade, Navigator navigator) {
        this.facade = facade;
        this.navigator = navigator;
    }


    @FXML
    public void initialize() {
        navModulos.addTab("Usuários", true, new Runnable() {
            @Override
            public void run() {
                mostrarPainel(paneUsuarios);
            }
        });
        navModulos.addTab("Itens", false, new Runnable() {
            @Override
            public void run() {
                mostrarPainel(paneItens);
            }
        });
        navModulos.addTab("Categorias", false, new Runnable() {
            @Override
            public void run() {
                mostrarPainel(paneCategorias);
            }
        });
        navModulos.addTab("Fornecedores", false, new Runnable() {
            @Override
            public void run() {
                mostrarPainel(paneFornecedores);
            }
        });

        cbUsuarioPerfil.setItems(FXCollections.observableArrayList(PERFIL_CLIENTE, PERFIL_FUNCIONARIO, PERFIL_ADMINISTRADOR));
        cbItemCategoria.setItems(categorias);
        cbItemCategoria.setConverter(new StringConverter<Categoria>() {
            @Override
            public String toString(Categoria c) {
                return c == null ? "" : c.getId() + " - " + c.getNome();
            }

            @Override
            public Categoria fromString(String texto) {
                return null; // ComboBox não editável
            }
        });

        cbItemFornecedor.setItems(fornecedores);
        cbItemFornecedor.setConverter(new StringConverter<Fornecedor>() {
            @Override
            public String toString(Fornecedor f) {
                return f == null ? "" : f.getId() + " - " + f.getNome();
            }

            @Override
            public Fornecedor fromString(String texto) {
                return null; // ComboBox não editável
            }
        });

        formUsuario = new FormState(lblFormUsuario, badgeUsuario, btnSalvarUsuario, btnCancelarUsuario, feedbackUsuario,
                fUsuarioId, "Cadastrar Novo Usuário", "Atualizar Usuário", "Cadastrar Usuário");
        formItem = new FormState(lblFormItem, badgeItem, btnSalvarItem, btnCancelarItem, feedbackItem,
                fItemId, "Cadastrar Novo Item", "Atualizar Item", "Cadastrar Item");
        formCategoria = new FormState(lblFormCategoria, badgeCategoria, btnSalvarCategoria, btnCancelarCategoria, feedbackCategoria,
                fCategoriaId, "Cadastrar Nova Categoria", "Atualizar Categoria", "Cadastrar Categoria");
        formFornecedor = new FormState(lblFormFornecedor, badgeFornecedor, btnSalvarFornecedor, btnCancelarFornecedor, feedbackFornecedor,
                fFornecedorId, "Cadastrar Novo Fornecedor", "Atualizar Fornecedor", "Cadastrar Fornecedor");

        configurarTabelas();
        limparUsuario();
        carregarTudo();
    }

        private void mostrarPainel(VBox painel) {
        VBox[] paineis = {paneUsuarios, paneItens, paneCategorias, paneFornecedores};
        for (VBox p : paineis) {
            p.setVisible(p == painel);
            p.setManaged(p == painel);
        }
    }


    //tabelas


    private void configurarTabelas() {

         //Usuários
        tblUsuarios.setItems(usuarios);
        tblUsuarios.setPlaceholder(new EmptyState("Nenhum usuário cadastrado."));
        colUsuarioId.setCellValueFactory(new PropertyValueFactory<Usuario, String>("id"));
        colUsuarioPerfil.setCellValueFactory(new PropertyValueFactory<Usuario, String>("perfil"));
        colUsuarioPerfil.setCellFactory(new BadgeCellFactory<Usuario>());
        colUsuarioNome.setCellValueFactory(new PropertyValueFactory<Usuario, String>("nome"));
        colUsuarioLogin.setCellValueFactory(new PropertyValueFactory<Usuario, String>("login"));
        colUsuarioCargo.setCellValueFactory(new ColunaTexto<Usuario>() {
            @Override
            protected String valor(Usuario u) {
                if (u instanceof Funcionario) {
                    String cargo = ((Funcionario) u).getCargo();
                    if (cargo != null && !cargo.isBlank()) {
                        return cargo;
                    }
                }
                return "-";
            }
        });
        colUsuarioSituacao.setCellValueFactory(new ColunaTexto<Usuario>() {
            @Override
            protected String valor(Usuario u) {
                return u.isAtivo() ? "ATIVO" : "INATIVO";
            }
        });
        colUsuarioSituacao.setCellFactory(new BadgeCellFactory<Usuario>());
        colUsuarioAcoes.setCellFactory(new AcoesCellFactory<Usuario>("Desativar", new AcoesTabela<Usuario>() {
            @Override
            public void editar(Usuario u) {
                editarUsuario(u);
            }

            @Override
            public void remover(Usuario u) {
                desativarUsuario(u);
            }
        }));

        //Categorias
        tblCategorias.setItems(categorias);
        tblCategorias.setPlaceholder(new EmptyState("Nenhuma categoria cadastrada."));
        colCategoriaId.setCellValueFactory(new PropertyValueFactory<Categoria, String>("id"));
        colCategoriaNome.setCellValueFactory(new PropertyValueFactory<Categoria, String>("nome"));
        colCategoriaAcoes.setCellFactory(new AcoesCellFactory<Categoria>(ACAO_REMOVER, new AcoesTabela<Categoria>() {
            @Override
            public void editar(Categoria c) {
                editarCategoria(c);
            }

            @Override
            public void remover(Categoria c) {
                removerCategoria(c);
            }
        }));

        //Fornecedores
        tblFornecedores.setItems(fornecedores);
        tblFornecedores.setPlaceholder(new EmptyState("Nenhum fornecedor cadastrado."));
        colFornecedorId.setCellValueFactory(new PropertyValueFactory<Fornecedor, String>("id"));
        colFornecedorNome.setCellValueFactory(new PropertyValueFactory<Fornecedor, String>("nome"));
        colFornecedorCnpj.setCellValueFactory(new PropertyValueFactory<Fornecedor, String>("cnpj"));
        colFornecedorTelefone.setCellValueFactory(new PropertyValueFactory<Fornecedor, String>("telefone"));
        colFornecedorAcoes.setCellFactory(new AcoesCellFactory<Fornecedor>(ACAO_REMOVER, new AcoesTabela<Fornecedor>() {
            @Override
            public void editar(Fornecedor f) {
                editarFornecedor(f);
            }

            @Override
            public void remover(Fornecedor f) {
                removerFornecedor(f);
            }
        }));

        //Itens
        tblItens.setItems(itens);
        tblItens.setPlaceholder(new EmptyState("Nenhum item cadastrado."));
        colItemId.setCellValueFactory(new PropertyValueFactory<Item, String>("id"));
        colItemNome.setCellValueFactory(new PropertyValueFactory<Item, String>("nome"));
        colItemStatus.setCellValueFactory(new PropertyValueFactory<Item, String>("status"));
        colItemStatus.setCellFactory(new BadgeCellFactory<Item>());
        colItemCategoria.setCellValueFactory(new ColunaTexto<Item>() {
            @Override
            protected String valor(Item i) {
                return i.getCategoria() != null ? i.getCategoria().getNome() : "-";
            }
        });
        colItemFornecedor.setCellValueFactory(new ColunaTexto<Item>() {
            @Override
            protected String valor(Item i) {
                return i.getFornecedor() != null ? i.getFornecedor().getNome() : "-";
            }
        });
        colItemTaxa.setCellValueFactory(new ColunaTexto<Item>() {
            @Override
            protected String valor(Item i) {
                return String.format("R$ %.2f", i.getTaxaDiaria());
            }
        });
        colItemAcoes.setCellFactory(new AcoesCellFactory<Item>(ACAO_REMOVER, new AcoesTabela<Item>() {
            @Override
            public void editar(Item i) {
                editarItem(i);
            }

            @Override
            public void remover(Item i) {
                removerItem(i);
            }
        }));
    }




    private void carregarTudo() {
        try {
            usuarios.setAll(facade.listarUsuario().values());
            categorias.setAll(facade.listarCategoria().values());
            fornecedores.setAll(facade.listarFornecedor().values());
            itens.setAll(facade.listarItem().values());
        } catch (RuntimeException e) {
            logger.error("Falha ao carregar dados do painel administrativo: {}", e.getMessage(), e);
            exibirAlertaErro("Erro", "Não foi possível carregar os dados. Consulte o log.");
        }
    }


    @FXML public void alterarPerfil() {

        boolean funcionario = PERFIL_FUNCIONARIO.equals(cbUsuarioPerfil.getValue());
        fUsuarioCargo.setVisible(funcionario);
        fUsuarioCargo.setManaged(funcionario);
        if (!funcionario) fUsuarioCargo.setText("");

    }

    @FXML public void salvarUsuario() {

        String nome = fUsuarioNome.getText().trim();
        String login = fUsuarioLogin.getText().trim();
        String senha = fUsuarioSenha.getText();
        String cargo = fUsuarioCargo.getText().trim();

        if (nome.isEmpty() || login.isEmpty() || senha.isEmpty()) {
            formUsuario.aviso("Preencha os campos obrigatórios", Banner.BannerType.DANGER);
            return;
        }

        boolean edicao = formUsuario.emEdicao();
        try {
            if (edicao) {
                // Nunca altera o objeto persistido: envia uma cópia para o Business validar e aplicar
                Usuario atual = facade.buscarUsuario(formUsuario.idEditando);
                Usuario novo = copiaComNovosDados(atual, nome, login, senha, cargo);
                facade.atualizarUsuario(atual.getId(), novo);
            } else if (PERFIL_FUNCIONARIO.equals(cbUsuarioPerfil.getValue())) {
                // O ID é gerado pela camada de negócio
                facade.cadastrarFuncionario(new Funcionario(null, nome, login, senha, cargo));
            } else if (PERFIL_ADMINISTRADOR.equals(cbUsuarioPerfil.getValue())) {
                facade.cadastrarAdm(new Administrador(null, nome, login, senha));
            } else {
                facade.cadastrarCliente(nome, login, senha);
            }
            usuarios.setAll(facade.listarUsuario().values());
            limparUsuario();
            formUsuario.aviso(edicao ? "Usuário atualizado!" : "Usuário cadastrado!", Banner.BannerType.SUCCESS);
        } catch (RuntimeException e) {
            mostrarErro(formUsuario, e);
        }

    }

    @FXML public void cancelarUsuario() {

        limparUsuario();
        formUsuario.limparAviso();

    }

    private Usuario copiaComNovosDados(Usuario atual, String nome, String login, String senha, String cargo) {
        String id = atual.getId();
        Usuario copia;
        if (atual instanceof Funcionario) {
            copia = new Funcionario(id, nome, login, senha, cargo);
        } else if (atual instanceof Administrador a) {
            copia = new Administrador(id, nome, login, senha, a.getNivelAcesso(), a.getDepartamento());
        } else {
            copia = new Cliente(id, nome, login, senha);
        }
        copia.setAtivo(atual.isAtivo());
        return copia;
    }

    private void limparUsuario() {
        formUsuario.sairEdicao();
        fUsuarioId.getInputField().setDisable(true); // ID automático no cadastro
        fUsuarioNome.setText("");
        fUsuarioLogin.setText("");
        fUsuarioSenha.setText("");
        cbUsuarioPerfil.setDisable(false);
        cbUsuarioPerfil.setValue(PERFIL_CLIENTE);
        alterarPerfil();
    }

    private void editarUsuario(Usuario u) {
        formUsuario.entrarEdicao(u.getId());
        cbUsuarioPerfil.setValue(u.getPerfil());
        cbUsuarioPerfil.setDisable(true); // o tipo (classe) do usuário não muda na edição
        alterarPerfil();
        fUsuarioNome.setText(u.getNome());
        fUsuarioLogin.setText(u.getLogin());
        fUsuarioSenha.setText(u.getSenha());
        if (u instanceof Funcionario f) fUsuarioCargo.setText(f.getCargo());
    }

    private void desativarUsuario(Usuario u) {
        if (!u.isAtivo()) {
            formUsuario.aviso("Este usuário já está desativado.", Banner.BannerType.WARNING);
            return;
        }
        if (confirmar("Deseja desativar o usuário " + u.getNome() + "?")) {
            try {
                facade.desativarUsuario(u.getId());
                usuarios.setAll(facade.listarUsuario().values());
                if (u.getId().equals(formUsuario.idEditando)) {
                    limparUsuario();
                }
                formUsuario.aviso("Usuário desativado!", Banner.BannerType.SUCCESS);
            } catch (RuntimeException e) {
                mostrarErro(formUsuario, e);
            }
        }
    }

    @FXML public void salvarItem() {

        String id = fItemId.getText().trim();
        String nome = fItemNome.getText().trim();
        Categoria categoria = cbItemCategoria.getValue();
        Fornecedor fornecedor = cbItemFornecedor.getValue();

        if (id.isEmpty() || nome.isEmpty() || categoria == null || fornecedor == null
                || fItemTaxa.getText().isBlank() || fItemReposicao.getText().isBlank()) {
            formItem.aviso("Preencha os campos obrigatórios!", Banner.BannerType.DANGER);
            return;
        }

        BigDecimal taxa;
        BigDecimal reposicao;
        try {
            taxa = new BigDecimal(fItemTaxa.getText().trim().replace(",", "."));
            reposicao = new BigDecimal(fItemReposicao.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            formItem.aviso("Taxa diária e valor de reposição devem ser números.", Banner.BannerType.DANGER);
            return;
        }

        boolean edicao = formItem.emEdicao();
        executar(formItem, edicao ? "Item atualizado!" : "Item cadastrado!", () -> {
            if (edicao) {
                // Altera o objeto existente: o status não muda (a facade proíbe mudá-lo aqui)
                Item item = facade.buscarItem(formItem.idEditando);
                item.setNome(nome);
                item.setCategoria(categoria);
                item.setFornecedor(fornecedor);
                item.setTaxaDiaria(taxa);
                item.setValorReposicao(reposicao);
                facade.atualizarItem(item);
            } else {
                facade.cadastrarItem(new Item(id, nome, taxa, reposicao, "DISPONIVEL", categoria, fornecedor));
            }
            itens.setAll(facade.listarItem().values());
            limparItem();
        });

    }

    @FXML public void cancelarItem() {

        limparItem();
        formItem.limparAviso();

    }

    private void limparItem() {
        formItem.sairEdicao();
        fItemNome.setText("");
        fItemTaxa.setText("");
        fItemReposicao.setText("");
        cbItemCategoria.setValue(null);
        cbItemFornecedor.setValue(null);
    }

    private void editarItem(Item i) {
        formItem.entrarEdicao(i.getId());
        fItemNome.setText(i.getNome());
        cbItemCategoria.setValue(i.getCategoria() == null ? null :
                categorias.stream().filter(c -> c.getId().equals(i.getCategoria().getId())).findFirst().orElse(null));
        cbItemFornecedor.setValue(i.getFornecedor() == null ? null :
                fornecedores.stream().filter(f -> f.getId().equals(i.getFornecedor().getId())).findFirst().orElse(null));
        fItemTaxa.setText(String.valueOf(i.getTaxaDiaria()));
        fItemReposicao.setText(String.valueOf(i.getValorReposicao()));
    }

    private void removerItem(Item i) {
        if (confirmar("Deseja remover o item " + i.getNome() + "?")) {
            executar(formItem, "Item removido!", () -> {
                facade.deletarItem(i.getId());
                itens.setAll(facade.listarItem().values());
                if (i.getId().equals(formItem.idEditando)) limparItem();
            });
        }
    }


    @FXML public void salvarCategoria() {

        String id = fCategoriaId.getText().trim();
        String nome = fCategoriaNome.getText().trim();

        if (id.isEmpty() || nome.isEmpty()) {
            formCategoria.aviso("Preencha todos os campos!", Banner.BannerType.DANGER);
            return;
        }

        boolean edicao = formCategoria.emEdicao();
        executar(formCategoria, edicao ? "Categoria atualizada!" : "Categoria cadastrada!", () -> {
            if (edicao) {
                Categoria c = facade.buscarCategoria(formCategoria.idEditando);
                c.setNome(nome);
                facade.atualizarCategoria(c);
            } else {
                facade.cadastrarCategoria(new Categoria(id, nome));
            }
            categorias.setAll(facade.listarCategoria().values());
            itens.setAll(facade.listarItem().values()); // a tabela de itens mostra o nome da categoria
            limparCategoria();
        });

    }

    @FXML public void cancelarCategoria() {

        limparCategoria();
        formCategoria.limparAviso();

    }

    private void limparCategoria() {
        formCategoria.sairEdicao();
        fCategoriaNome.setText("");
    }

    private void editarCategoria(Categoria c) {
        formCategoria.entrarEdicao(c.getId());
        fCategoriaNome.setText(c.getNome());
    }

    private void removerCategoria(Categoria c) {
        if (confirmar("Deseja remover a categoria " + c.getNome() + "?")) {
            executar(formCategoria, "Categoria removida!", () -> {
                facade.deletarCategoria(c.getId());
                categorias.setAll(facade.listarCategoria().values());
                if (c.getId().equals(formCategoria.idEditando)) limparCategoria();
            });
        }
    }

    @FXML public void salvarFornecedor() {

        String id = fFornecedorId.getText().trim();
        String nome = fFornecedorNome.getText().trim();
        String cnpj = fFornecedorCnpj.getText().trim();
        String telefone = fFornecedorTelefone.getText().trim();

        if (id.isEmpty() || nome.isEmpty() || cnpj.isEmpty()) {
            formFornecedor.aviso("Preencha os campos obrigatórios!", Banner.BannerType.DANGER);
            return;
        }

        boolean edicao = formFornecedor.emEdicao();
        executar(formFornecedor, edicao ? "Fornecedor atualizado!" : "Fornecedor cadastrado!", () -> {
            if (edicao) {
                Fornecedor f = facade.buscarFornecedor(formFornecedor.idEditando);
                f.setNome(nome);
                f.setCnpj(cnpj);
                f.setTelefone(telefone);
                facade.atualizarFornecedor(f);
            } else {
                facade.cadastrarFornecedor(new Fornecedor(id, nome, cnpj, telefone));
            }
            fornecedores.setAll(facade.listarFornecedor().values());
            itens.setAll(facade.listarItem().values()); // a tabela de itens mostra o nome do fornecedor
            limparFornecedor();
        });

    }

    @FXML public void cancelarFornecedor() {

        limparFornecedor();
        formFornecedor.limparAviso();

    }

    private void limparFornecedor() {
        formFornecedor.sairEdicao();
        fFornecedorNome.setText("");
        fFornecedorCnpj.setText("");
        fFornecedorTelefone.setText("");
    }

    private void editarFornecedor(Fornecedor f) {
        formFornecedor.entrarEdicao(f.getId());
        fFornecedorNome.setText(f.getNome());
        fFornecedorCnpj.setText(f.getCnpj());
        fFornecedorTelefone.setText(f.getTelefone());
    }

    private void removerFornecedor(Fornecedor f) {
        if (confirmar("Deseja remover o fornecedor " + f.getNome() + "?")) {
            executar(formFornecedor, "Fornecedor removido!", () -> {
                facade.deletarFornecedor(f.getId());
                fornecedores.setAll(facade.listarFornecedor().values());
                if (f.getId().equals(formFornecedor.idEditando)) limparFornecedor();
            });
        }
    }


    @FXML
    public void handleSair() {
        navigator.showLogin();
    }


    //Executa uma ação da facade e mostra o resultado no Banner do formulário
    private void executar(FormState form, String msgSucesso, Runnable acao) {
        try {
            acao.run();
            form.aviso(msgSucesso, Banner.BannerType.SUCCESS);
        } catch (RuntimeException e) {
            logger.warn("Operação do administrador falhou: {}", e.getMessage(), e);
            form.aviso(mensagemSegura(e), Banner.BannerType.DANGER);
        }
    }

    // Registra o erro no log e mostra no Banner só mensagens seguras para o usuário
    private void mostrarErro(FormState form, RuntimeException e) {
        logger.warn("Operação do administrador falhou: {}", e.getMessage(), e);
        form.aviso(mensagemSegura(e), Banner.BannerType.DANGER);
    }


    //Só exibe mensagens escritas para o usuário; qualquer outra falha vai para o log
    private String mensagemSegura(RuntimeException e) {
        boolean mensagemDeNegocio = e instanceof IllegalArgumentException
                || e.getClass().getPackageName().equals("com.loja.exceptions");
        return mensagemDeNegocio && e.getMessage() != null
                ? e.getMessage()
                : "Ocorreu um erro inesperado. Consulte o log.";
    }


    private boolean confirmar(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, mensagem, ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirmação");
        alert.setHeaderText(null);
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }


    private void exibirAlertaErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    //Cor do StatusBadge
    private static StatusBadge.StatusType tipoDoBadge(String valor) {
        switch (valor.toUpperCase()) {
            case "ATIVO":
            case "DISPONIVEL":
                return StatusBadge.StatusType.SUCCESS;
            case "ALUGADO":
                return StatusBadge.StatusType.WARNING;
            case "INATIVO":
            case "MANUTENCAO":
            case "INDISPONIVEL":
                return StatusBadge.StatusType.DANGER;
            default:
                return StatusBadge.StatusType.INFO;
        }
    }

    //Alterna um formulário entre "Cadastrar" e "Modo de Edição".
    private static class FormState {
        private static final String BOTAO = "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 24; -fx-cursor: hand; -fx-background-color: ";


        private final Label titulo;
        private final HBox badge;
        private final Button btnSalvar;
        private final Button btnCancelar;
        private final VBox feedback;
        private final FormField campoId;
        private final String tituloNovo;
        private final String tituloEdicao;
        private final String textoSalvar;
        private String idEditando;


        FormState(Label titulo, HBox badge, Button btnSalvar, Button btnCancelar, VBox feedback,
                  FormField campoId, String tituloNovo, String tituloEdicao, String textoSalvar) {
            this.titulo = titulo;
            this.badge = badge;
            this.btnSalvar = btnSalvar;
            this.btnCancelar = btnCancelar;
            this.feedback = feedback;
            this.campoId = campoId;
            this.tituloNovo = tituloNovo;
            this.tituloEdicao = tituloEdicao;
            this.textoSalvar = textoSalvar;


            badge.getChildren().add(new StatusBadge("Modo de Edição", StatusBadge.StatusType.WARNING));
            btnCancelar.setStyle("-fx-background-color: #e5e7eb; -fx-text-fill: #374151; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
            sairEdicao();
        }


        boolean emEdicao() {
            return idEditando != null;
        }


        void entrarEdicao(String id) {
            idEditando = id;
            campoId.setText(id);
            campoId.getInputField().setDisable(true);
            titulo.setText(tituloEdicao);
            btnSalvar.setText("Salvar Alterações");
            btnSalvar.setStyle(BOTAO + "#d97706;");
            exibir(true);
            limparAviso();
        }


        void sairEdicao() {
            idEditando = null;
            campoId.setText("");
            campoId.getInputField().setDisable(false);
            titulo.setText(tituloNovo);
            btnSalvar.setText(textoSalvar);
            btnSalvar.setStyle(BOTAO + "#4f46e5;");
            exibir(false);
        }


        void aviso(String mensagem, Banner.BannerType tipo) {
            feedback.getChildren().setAll(new Banner(mensagem, tipo));
        }


        void limparAviso() {
            feedback.getChildren().clear();
        }


        private void exibir(boolean edicao) {
            badge.setVisible(edicao);
            badge.setManaged(edicao);
            btnCancelar.setVisible(edicao);
            btnCancelar.setManaged(edicao);
        }
    }

    //Coluna cujo texto é calculado (não vem direto de um getter)

    private abstract static class ColunaTexto<T>
            implements Callback<TableColumn.CellDataFeatures<T, String>, ObservableValue<String>> {

        @Override
        public ObservableValue<String> call(TableColumn.CellDataFeatures<T, String> dados) {
            return new SimpleStringProperty(valor(dados.getValue()));
        }

        protected abstract String valor(T item);
    }

    //Cria as células que mostram o texto dentro de um StatusBadge
    private static class BadgeCellFactory<T> implements Callback<TableColumn<T, String>, TableCell<T, String>> {
        @Override
        public TableCell<T, String> call(TableColumn<T, String> coluna) {
            return new BadgeCell<>();
        }
    }

    //Célula que mostra o texto dentro de um StatusBadge
    private static class BadgeCell<T> extends TableCell<T, String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);
            if (empty || item == null) {
                setGraphic(null);
                return;
            }
            StatusBadge badge = new StatusBadge(item, tipoDoBadge(item));
            badge.setMaxWidth(Region.USE_PREF_SIZE);
            setGraphic(badge);
        }
    }

    // O que os botões da coluna "Ações" fazem em cada tabela
    private interface AcoesTabela<T> {
        void editar(T item);

        void remover(T item);
    }

    // Cria as células da coluna "Ações"
    private static class AcoesCellFactory<T> implements Callback<TableColumn<T, Void>, TableCell<T, Void>> {
        private final String textoRemover;
        private final AcoesTabela<T> acoes;

        AcoesCellFactory(String textoRemover, AcoesTabela<T> acoes) {
            this.textoRemover = textoRemover;
            this.acoes = acoes;
        }

        @Override
        public TableCell<T, Void> call(TableColumn<T, Void> coluna) {
            return new AcoesCell<>(textoRemover, acoes);
        }
    }

    // Célula com os botões "Editar" e "Remover/Desativar"
    private static class AcoesCell<T> extends TableCell<T, Void> {
        private final HBox box;

        AcoesCell(String textoRemover, final AcoesTabela<T> acoes) {
            Button editar = new Button("Editar");
            editar.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #4f46e5; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 4 10; -fx-cursor: hand;");
            editar.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent evento) {
                    acoes.editar(itemDaLinha());
                }
            });

            Button remover = new Button(textoRemover);
            remover.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #dc2626; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 4 10; -fx-cursor: hand;");
            remover.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent evento) {
                    acoes.remover(itemDaLinha());
                }
            });

            box = new HBox(6, editar, remover);
            box.setAlignment(Pos.CENTER);
        }

        private T itemDaLinha() {
            return getTableView().getItems().get(getIndex());
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : box);
        }
    }
}