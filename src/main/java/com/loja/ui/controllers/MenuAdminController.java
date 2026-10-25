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

import javafx.beans.value.ChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import java.text.Normalizer;
import java.util.function.Predicate;

import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import com.loja.model.ContratoAluguel;
import com.loja.model.Multa;
import java.time.temporal.ChronoUnit;
import javafx.scene.control.DatePicker;

//ignorar pedido para colocar lambda, uma vez que o professor pediu para não ser utilizado
@SuppressWarnings("java:S1604") 

public class MenuAdminController {


    private static final Logger logger = LoggerFactory.getLogger(MenuAdminController.class);

    private static final String PERFIL_CLIENTE = "CLIENTE";
    private static final String PERFIL_FUNCIONARIO = "FUNCIONARIO";
    private static final String PERFIL_ADMINISTRADOR = "ADMINISTRADOR";
    private static final String ACAO_REMOVER = "Remover";
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ILojaFacade facade;
    private final Navigator navigator;
    private Administrador usuarioLogado;


    // Listas que alimentam as tabelas e os ComboBox
    private final ObservableList<Usuario> usuarios = FXCollections.observableArrayList();
    private final ObservableList<Item> itens = FXCollections.observableArrayList();
    private final ObservableList<Categoria> categorias = FXCollections.observableArrayList();
    private final ObservableList<Fornecedor> fornecedores = FXCollections.observableArrayList();
    private final ObservableList<ContratoAluguel> alugados = FXCollections.observableArrayList();


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

    @FXML private TextField txtPesquisaUsuarios;
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

    @FXML private TextField txtPesquisaItens;
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

    @FXML private TextField txtPesquisaCategorias;
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

    @FXML private TextField txtPesquisaFornecedores;
    @FXML private TableView<Fornecedor> tblFornecedores;
    @FXML private TableColumn<Fornecedor, String> colFornecedorId;
    @FXML private TableColumn<Fornecedor, String> colFornecedorNome;
    @FXML private TableColumn<Fornecedor, String> colFornecedorCnpj;
    @FXML private TableColumn<Fornecedor, String> colFornecedorTelefone;
    @FXML private TableColumn<Fornecedor, Void> colFornecedorAcoes;

    // relatórios
    @FXML private VBox paneRelatorios;
    @FXML private VBox feedbackRelatorios;
    @FXML private DatePicker dpFaturamentoInicio;
    @FXML private DatePicker dpFaturamentoFim;
    @FXML private TextArea txtFaturamento;
    @FXML private Label lblResumoAlugados;
    @FXML private TableView<ContratoAluguel> tblAlugados;
    @FXML private TableColumn<ContratoAluguel, String> colAlugadoContrato;
    @FXML private TableColumn<ContratoAluguel, String> colAlugadoItem;
    @FXML private TableColumn<ContratoAluguel, String> colAlugadoCliente;
    @FXML private TableColumn<ContratoAluguel, String> colAlugadoRetirada;
    @FXML private TableColumn<ContratoAluguel, String> colAlugadoPrevisao;
    @FXML private TableColumn<ContratoAluguel, String> colAlugadoSituacao;
    @FXML private TableColumn<ContratoAluguel, String> colAlugadoAtraso;
    @FXML private TableColumn<ContratoAluguel, String> colAlugadoValor;


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
        navModulos.addTab("Relatórios", false, new Runnable() {
            @Override
            public void run() {
                mostrarPainel(paneRelatorios);
                carregarItensAlugados();
            }
        });

        // Período padrão do faturamento: do dia 1º do mês até hoje
        dpFaturamentoInicio.setValue(hoje().withDayOfMonth(1));
        dpFaturamentoFim.setValue(hoje());

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
        VBox[] paineis = {paneUsuarios, paneItens, paneCategorias, paneFornecedores, paneRelatorios};
        for (VBox p : paineis) {
            p.setVisible(p == painel);
            p.setManaged(p == painel);
        }
    }


    //tabelas


    private void configurarTabelas() {

         //Usuários
        tblUsuarios.setPlaceholder(new EmptyState("Nenhum usuário cadastrado."));

        new Pesquisa<Usuario>() {
            @Override
            protected String textoPesquisavel(Usuario u) {
                String cargo = (u instanceof Funcionario) ? ((Funcionario) u).getCargo() : null;
                return texto(u.getId(), u.getNome(), u.getLogin(), u.getPerfil(), cargo,
                        u.isAtivo() ? "ativo" : "inativo");
            }
        }.ligar(txtPesquisaUsuarios, tblUsuarios, usuarios);

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
        colUsuarioAcoes.setCellFactory(new AcoesCellFactory<Usuario>("Desativar", "Histórico", new AcoesTabela<Usuario>() {
            @Override
            public void editar(Usuario u) {
                editarUsuario(u);
            }

            @Override
            public void remover(Usuario u) {
                desativarUsuario(u);
            }

            @Override
            public boolean temExtra(Usuario u) {
                return u instanceof Cliente;
            }

            @Override
            public void extra(Usuario u) {
                relatorioHistoricoCliente(u);
            }
        }));

        //Categorias
        tblCategorias.setPlaceholder(new EmptyState("Nenhuma categoria cadastrada."));

        new Pesquisa<Categoria>() {
            @Override
            protected String textoPesquisavel(Categoria c) {
                return texto(c.getId(), c.getNome());
            }
        }.ligar(txtPesquisaCategorias, tblCategorias, categorias);

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
        tblFornecedores.setPlaceholder(new EmptyState("Nenhum fornecedor cadastrado."));

        new Pesquisa<Fornecedor>() {
            @Override
            protected String textoPesquisavel(Fornecedor f) {
                return texto(f.getId(), f.getNome(), f.getCnpj(), f.getTelefone(),
                        somenteDigitos(f.getCnpj()), somenteDigitos(f.getTelefone()));
            }
        }.ligar(txtPesquisaFornecedores, tblFornecedores, fornecedores);

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
        tblItens.setPlaceholder(new EmptyState("Nenhum item cadastrado."));

        new Pesquisa<Item>() {
            @Override
            protected String textoPesquisavel(Item i) {
                Categoria c = i.getCategoria();
                Fornecedor f = i.getFornecedor();
                return texto(i.getId(), i.getNome(), i.getStatus(), i.getTaxaDiaria(),
                        c != null ? c.getId() : null, c != null ? c.getNome() : null,
                        f != null ? f.getId() : null, f != null ? f.getNome() : null);
            }
        }.ligar(txtPesquisaItens, tblItens, itens);

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

        //relatorios
                // Itens alugados (aba Relatórios)
        tblAlugados.setItems(alugados);
        tblAlugados.setPlaceholder(new EmptyState("Nenhum item alugado no momento."));
        colAlugadoContrato.setCellValueFactory(new PropertyValueFactory<ContratoAluguel, String>("id"));
        colAlugadoItem.setCellValueFactory(new ColunaTexto<ContratoAluguel>() {
            @Override
            protected String valor(ContratoAluguel c) {
                return c.getItem() != null ? c.getItem().getNome() : "-";
            }
        });
        colAlugadoCliente.setCellValueFactory(new ColunaTexto<ContratoAluguel>() {
            @Override
            protected String valor(ContratoAluguel c) {
                return c.getCliente() != null ? c.getCliente().getNome() : "-";
            }
        });
        colAlugadoRetirada.setCellValueFactory(new ColunaTexto<ContratoAluguel>() {
            @Override
            protected String valor(ContratoAluguel c) {
                return formatarData(c.getDataRetirada());
            }
        });
        colAlugadoPrevisao.setCellValueFactory(new ColunaTexto<ContratoAluguel>() {
            @Override
            protected String valor(ContratoAluguel c) {
                return formatarData(c.getDataPrevDevolucao());
            }
        });
        colAlugadoSituacao.setCellValueFactory(new ColunaTexto<ContratoAluguel>() {
            @Override
            protected String valor(ContratoAluguel c) {
                return estaAtrasado(c) ? "ATRASADO" : "EM DIA";
            }
        });
        colAlugadoSituacao.setCellFactory(new BadgeCellFactory<ContratoAluguel>());
        colAlugadoAtraso.setCellValueFactory(new ColunaTexto<ContratoAluguel>() {
            @Override
            protected String valor(ContratoAluguel c) {
                return estaAtrasado(c) ? diasDeAtraso(c) + " dia(s)" : "-";
            }
        });
        colAlugadoValor.setCellValueFactory(new ColunaTexto<ContratoAluguel>() {
            @Override
            protected String valor(ContratoAluguel c) {
                return formatarMoeda(c.getValorTotal());
            }
        });
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

    private void relatorioHistoricoCliente(Usuario cliente) {
        try {
            Map<String, ContratoAluguel> contratos = facade.consultarHistoricoCliente(cliente.getId());
            Map<String, Multa> multas = facade.listarMultaPorCliente(cliente.getId());
            LocalDate dataHoje = hoje();

            StringBuilder atuais = new StringBuilder();
            StringBuilder encerrados = new StringBuilder();
            StringBuilder irregularidades = new StringBuilder();
            int qtdAtuais = 0;
            int qtdEncerrados = 0;
            int qtdIrregularidades = 0;

            for (ContratoAluguel c : contratos.values()) {
                LocalDate previsao = c.getDataPrevDevolucao();
                if (c.getDataEfetivaDevolucao() == null) {
                    atuais.append(linhaContrato(c));
                    qtdAtuais++;
                    if (previsao != null && dataHoje.isAfter(previsao)) {
                        irregularidades.append(String.format("   Contrato #%s: EM ATRASO há %d dia(s) (devolução prevista para %s)%n",
                                c.getId(), ChronoUnit.DAYS.between(previsao, dataHoje), formatarData(previsao)));
                        qtdIrregularidades++;
                    }
                } else {
                    encerrados.append(linhaContrato(c));
                    qtdEncerrados++;
                    if (previsao != null && c.getDataEfetivaDevolucao().isAfter(previsao)) {
                        irregularidades.append(String.format("   Contrato #%s: devolvido com %d dia(s) de atraso%n",
                                c.getId(), ChronoUnit.DAYS.between(previsao, c.getDataEfetivaDevolucao())));
                        qtdIrregularidades++;
                    }
                }
            }

            for (Multa m : multas.values()) {
                irregularidades.append(String.format("   Multa #%s: %s | %s | %s%n",
                        m.getId(), m.getMotivo(), formatarMoeda(m.getValorTotal()), m.getStatus()));
                qtdIrregularidades++;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("=== HISTÓRICO DO CLIENTE ===\n");
            sb.append("Cliente : ").append(cliente.getNome()).append(" (#").append(cliente.getId()).append(")\n");
            sb.append("Login   : ").append(cliente.getLogin()).append('\n');
            sb.append("Gerado em ").append(formatarData(dataHoje)).append("\n\n");

            sb.append("ALUGUÉIS ATUAIS (").append(qtdAtuais).append(")\n");
            sb.append(qtdAtuais == 0 ? "   Nenhum.\n" : atuais.toString()).append('\n');

            sb.append("ALUGUÉIS ENCERRADOS (").append(qtdEncerrados).append(")\n");
            sb.append(qtdEncerrados == 0 ? "   Nenhum.\n" : encerrados.toString()).append('\n');

            sb.append("IRREGULARIDADES (").append(qtdIrregularidades).append(")\n");
            sb.append(qtdIrregularidades == 0 ? "   Nenhuma.\n" : irregularidades.toString()).append('\n');

            if (facade.possuiMultaPendente(cliente.getId())) {
                sb.append("⚠ ATENÇÃO: este cliente possui multa pendente.\n");
            }

            mostrarRelatorio("Histórico de " + cliente.getNome(), sb.toString());
        } catch (RuntimeException e) {
            mostrarErro(formUsuario, e);
        }
    }

    private static String linhaContrato(ContratoAluguel c) {
        String item = c.getItem() != null ? c.getItem().getNome() : "-";
        String devolucao = c.getDataEfetivaDevolucao() != null
                ? "devolvido em " + formatarData(c.getDataEfetivaDevolucao())
                : "previsto para " + formatarData(c.getDataPrevDevolucao());
        return String.format("   #%-8s %-25s retirada %s, %s | %s%n",
                c.getId(), item, formatarData(c.getDataRetirada()), devolucao, formatarMoeda(c.getValorTotal()));
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
        try {
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
            formItem.aviso(edicao ? "Item atualizado!" : "Item cadastrado!", Banner.BannerType.SUCCESS);
        } catch (RuntimeException e) {
            mostrarErro(formItem, e);
        }

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
        cbItemCategoria.setValue(categoriaDaLista(i.getCategoria()));
        cbItemFornecedor.setValue(fornecedorDaLista(i.getFornecedor()));
        fItemTaxa.setText(String.valueOf(i.getTaxaDiaria()));
        fItemReposicao.setText(String.valueOf(i.getValorReposicao()));
    }

    //Acha, na lista do ComboBox, a categoria com o mesmo ID
    private Categoria categoriaDaLista(Categoria categoria) {
        if (categoria == null) {
            return null;
        }
        for (Categoria c : categorias) {
            if (c.getId().equals(categoria.getId())) {
                return c;
            }
        }
        return null;
    }

    //Acha, na lista do ComboBox, o fornecedor com o mesmo ID
    private Fornecedor fornecedorDaLista(Fornecedor fornecedor) {
        if (fornecedor == null) {
            return null;
        }
        for (Fornecedor f : fornecedores) {
            if (f.getId().equals(fornecedor.getId())) {
                return f;
            }
        }
        return null;
    }

    private void removerItem(Item i) {
        if (confirmar("Deseja remover o item " + i.getNome() + "?")) {
            try {
                facade.deletarItem(i.getId());
                itens.setAll(facade.listarItem().values());
                if (i.getId().equals(formItem.idEditando)) {
                    limparItem();
                }
                formItem.aviso("Item removido!", Banner.BannerType.SUCCESS);
            } catch (RuntimeException e) {
                mostrarErro(formItem, e);
            }
        }
    }

    // Cruza os itens disponíveis com as categorias cadastradas
    @FXML
    public void relatorioDisponiveisPorCategoria() {
        try {
            Map<String, Item> disponiveis = facade.listarItemPorStatus("DISPONIVEL");
            Map<String, Categoria> todasCategorias = facade.listarCategoria();

            StringBuilder sb = new StringBuilder();
            sb.append("=== ITENS DISPONÍVEIS POR CATEGORIA: ").append(formatarData(hoje())).append(" ===\n\n");

            for (Categoria categoria : todasCategorias.values()) {
                StringBuilder linhas = new StringBuilder();
                int quantidade = 0;
                for (Item item : disponiveis.values()) {
                    if (item.getCategoria() != null && categoria.getId().equals(item.getCategoria().getId())) {
                        linhas.append(String.format("   #%-8s %-30s %s/dia%n",
                                item.getId(), item.getNome(), formatarMoeda(item.getTaxaDiaria())));
                        quantidade++;
                    }
                }
                sb.append(categoria.getNome()).append(" (").append(quantidade).append(")\n");
                sb.append(quantidade == 0 ? "   Nenhum item disponível.\n" : linhas.toString());
                sb.append('\n');
            }

            // Itens disponíveis cuja categoria não está cadastrada
            StringBuilder semCategoria = new StringBuilder();
            for (Item item : disponiveis.values()) {
                boolean encontrou = false;
                if (item.getCategoria() != null) {
                    for (Categoria categoria : todasCategorias.values()) {
                        if (categoria.getId().equals(item.getCategoria().getId())) {
                            encontrou = true;
                        }
                    }
                }
                if (!encontrou) {
                    semCategoria.append(String.format("   #%-8s %s%n", item.getId(), item.getNome()));
                }
            }
            if (semCategoria.length() > 0) {
                sb.append("Sem categoria\n").append(semCategoria).append('\n');
            }

            sb.append("Total de itens disponíveis: ").append(disponiveis.size()).append('\n');
            mostrarRelatorio("Itens disponíveis por categoria", sb.toString());
        } catch (RuntimeException e) {
            mostrarErro(formItem, e);
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
        try {
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
            formCategoria.aviso(edicao ? "Categoria atualizada!" : "Categoria cadastrada!", Banner.BannerType.SUCCESS);
        } catch (RuntimeException e) {
            mostrarErro(formCategoria, e);
        }

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
            try {
                facade.deletarCategoria(c.getId());
                categorias.setAll(facade.listarCategoria().values());
                if (c.getId().equals(formCategoria.idEditando)) {
                    limparCategoria();
                }
                formCategoria.aviso("Categoria removida!", Banner.BannerType.SUCCESS);
            } catch (RuntimeException e) {
                mostrarErro(formCategoria, e);
            }
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
        try {
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
            formFornecedor.aviso(edicao ? "Fornecedor atualizado!" : "Fornecedor cadastrado!", Banner.BannerType.SUCCESS);
        } catch (RuntimeException e) {
            mostrarErro(formFornecedor, e);
        }

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
            try {
                facade.deletarFornecedor(f.getId());
                fornecedores.setAll(facade.listarFornecedor().values());
                if (f.getId().equals(formFornecedor.idEditando)) {
                    limparFornecedor();
                }
                formFornecedor.aviso("Fornecedor removido!", Banner.BannerType.SUCCESS);
            } catch (RuntimeException e) {
                mostrarErro(formFornecedor, e);
            }
        }
    }

    //relatorios

    @FXML
    public void gerarFaturamento() {
        feedbackRelatorios.getChildren().clear();
        LocalDate inicio = dpFaturamentoInicio.getValue();
        LocalDate fim = dpFaturamentoFim.getValue();

        if (inicio == null || fim == null) {
            avisoRelatorio("Escolha a data inicial e a data final.", Banner.BannerType.DANGER);
            return;
        }

        try {
            // A fachada valida o período (ex.: início depois do fim) e soma aluguéis + multas
            txtFaturamento.setText(facade.gerarRelatorioFaturamento(inicio, fim));
        } catch (RuntimeException e) {
            logger.warn("Falha ao gerar relatório de faturamento: {}", e.getMessage(), e);
            avisoRelatorio(mensagemSegura(e), Banner.BannerType.DANGER);
        }
    }

    private void avisoRelatorio(String mensagem, Banner.BannerType tipo) {
        feedbackRelatorios.getChildren().setAll(new Banner(mensagem, tipo));
    }

    @FXML
    public void carregarItensAlugados() {
        try {
            alugados.setAll(facade.listarContratosAtivos().values());
            int atrasados = 0;
            for (ContratoAluguel c : alugados) {
                if (estaAtrasado(c)) {
                    atrasados++;
                }
            }
            lblResumoAlugados.setText("Total: " + alugados.size() + "  |  Em atraso: " + atrasados);
        } catch (RuntimeException e) {
            logger.warn("Falha ao carregar itens alugados: {}", e.getMessage(), e);
            avisoRelatorio(mensagemSegura(e), Banner.BannerType.DANGER);
        }
    }

    private static boolean estaAtrasado(ContratoAluguel c) {
        return c.getDataPrevDevolucao() != null && hoje().isAfter(c.getDataPrevDevolucao());
    }

    private static long diasDeAtraso(ContratoAluguel c) {
        return estaAtrasado(c) ? ChronoUnit.DAYS.between(c.getDataPrevDevolucao(), hoje()) : 0;
    }

    //fim relatorio

    @FXML
    public void handleSair() {
        navigator.showLogin();
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

        // Abre uma janela com o texto de um relatório (fonte monoespaçada para alinhar as colunas)
    private void mostrarRelatorio(String titulo, String texto) {
        TextArea area = new TextArea(texto);
        area.setEditable(false);
        area.setWrapText(false);
        area.setPrefSize(680, 440);
        area.setStyle("-fx-font-family: monospace; -fx-font-size: 13px;");

        Dialog<Void> janela = new Dialog<>();
        janela.setTitle(titulo);
        janela.setHeaderText(titulo);
        janela.getDialogPane().setContent(area);
        janela.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        janela.setResizable(true);
        janela.showAndWait();
    }

    // Data de hoje no fuso da loja
    private static LocalDate hoje() {
        return LocalDate.now(ZoneId.of("America/Recife"));
    }

    private static String formatarData(LocalDate data) {
        return data == null ? "-" : data.format(FORMATO_DATA);
    }

    private static String formatarMoeda(BigDecimal valor) {
        return valor == null ? "-" : String.format("R$ %.2f", valor);
    }

    //Cor do StatusBadge
    private static StatusBadge.StatusType tipoDoBadge(String valor) {
        switch (valor.toUpperCase()) {
            case "ATIVO":
            case "DISPONIVEL":
            case "EM DIA":
                return StatusBadge.StatusType.SUCCESS;
            case "ALUGADO":
                return StatusBadge.StatusType.WARNING;
            case "INATIVO":
            case "MANUTENCAO":
            case "INDISPONIVEL":
            case "ATRASADO":
                return StatusBadge.StatusType.DANGER;
            default:
                return StatusBadge.StatusType.INFO;
        }
    }

    //Junta vários valores num texto só, ignorando os nulos
    private static String texto(Object... partes) {
        StringBuilder sb = new StringBuilder();
        for (Object parte : partes) {
            if (parte != null) {
                sb.append(parte).append(' ');
            }
        }
        return sb.toString();
    }

    //Minúsculas e sem acento
    private static String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim();
    }

    //12.345.678/0001-99" vira "12345678000199" (para achar CNPJ/telefone digitado sem pontuação)
    private static String somenteDigitos(String valor) {
        return valor == null ? null : valor.replaceAll("\\D", "");
    }

    //Liga um campo de pesquisa a uma tabela: filtra enquanto o usuário digita. cada aba cria uma subclasse anonima dizendo em textoPesquisavel() onde procurar.
    private abstract static class Pesquisa<T> {

        protected abstract String textoPesquisavel(T item);

        void ligar(TextField campo, final TableView<T> tabela, ObservableList<T> dados) {
            final FilteredList<T> filtrados = new FilteredList<>(dados);
            SortedList<T> ordenados = new SortedList<>(filtrados);
            ordenados.comparatorProperty().bind(tabela.comparatorProperty()); // mantém a ordenação por coluna
            tabela.setItems(ordenados);

            final Node vazioOriginal = tabela.getPlaceholder();
            campo.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> obs, String antigo, String texto) {
                    final String termo = normalizar(texto);
                    filtrados.setPredicate(new Predicate<T>() {
                        @Override
                        public boolean test(T item) {
                            return termo.isEmpty() || normalizar(textoPesquisavel(item)).contains(termo);
                        }
                    });
                    if (termo.isEmpty()) {
                        tabela.setPlaceholder(vazioOriginal);
                    } else {
                        tabela.setPlaceholder(new EmptyState("Nenhum resultado para \"" + texto.trim() + "\"."));
                    }
                }
            });
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

        // Botão extra opcional (ex.: "Histórico" só para clientes). Por padrão não aparece.
        default boolean temExtra(T item) {
            return false;
        }

        default void extra(T item) {
        }
    }

    // Cria as células da coluna "Ações"
    private static class AcoesCellFactory<T> implements Callback<TableColumn<T, Void>, TableCell<T, Void>> {
        private final String textoRemover;
        private final String textoExtra;
        private final AcoesTabela<T> acoes;

        AcoesCellFactory(String textoRemover, AcoesTabela<T> acoes) {
            this(textoRemover, null, acoes);
        }

        AcoesCellFactory(String textoRemover, String textoExtra, AcoesTabela<T> acoes) {
            this.textoRemover = textoRemover;
            this.textoExtra = textoExtra;
            this.acoes = acoes;
        }

        @Override
        public TableCell<T, Void> call(TableColumn<T, Void> coluna) {
            return new AcoesCell<>(textoRemover, textoExtra, acoes);
        }
    }

    // Célula com os botões "Editar" e "Remover/Desativar"
    private static class AcoesCell<T> extends TableCell<T, Void> {
        private final HBox box;
        private final Button extra;
        private final AcoesTabela<T> acoes;

        AcoesCell(String textoRemover, String textoExtra, final AcoesTabela<T> acoes) {
            this.acoes = acoes;

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

            if (textoExtra != null) {
                extra = new Button(textoExtra);
                extra.setStyle("-fx-background-color: #ecfdf5; -fx-text-fill: #047857; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 4 10; -fx-cursor: hand;");
                extra.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent evento) {
                        acoes.extra(itemDaLinha());
                    }
                });
                box.getChildren().add(extra);
            } else {
                extra = null;
            }
        }

        private T itemDaLinha() {
            return getTableView().getItems().get(getIndex());
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
                return;
            }
            if (extra != null) {
                boolean mostrar = acoes.temExtra(itemDaLinha());
                extra.setVisible(mostrar);
                extra.setManaged(mostrar);
            }
            setGraphic(box);
        }
    }
}