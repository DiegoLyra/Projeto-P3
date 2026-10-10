package com.loja.ui.controllers;


import java.util.function.Consumer;
import java.util.function.Function;

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
import javafx.stage.Stage;
import javafx.util.StringConverter;


public class MenuAdminController {


   private static final Logger logger = LoggerFactory.getLogger(MenuAdminController.class);


   private ILojaFacade facade;
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


   @FXML
   public void initialize() {
       navModulos.addTab("Usuários", true, () -> mostrarPainel(paneUsuarios));
       navModulos.addTab("Itens", false, () -> mostrarPainel(paneItens));
       navModulos.addTab("Categorias", false, () -> mostrarPainel(paneCategorias));
       navModulos.addTab("Fornecedores", false, () -> mostrarPainel(paneFornecedores));


       cbUsuarioPerfil.setItems(FXCollections.observableArrayList("CLIENTE", "FUNCIONARIO", "ADMINISTRADOR"));
       cbItemCategoria.setItems(categorias);
       cbItemCategoria.setConverter(conversor(c -> c.getId() + " - " + c.getNome()));
       cbItemFornecedor.setItems(fornecedores);
       cbItemFornecedor.setConverter(conversor(f -> f.getId() + " - " + f.getNome()));


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
   }


   public void initData(ILojaFacade facade, Administrador usuarioLogado) {
       this.facade = facade;
       this.usuarioLogado = usuarioLogado;


       if (usuarioLogado != null && usuarioLogado.getNome() != null) {
           lblBoasVindas.setText("PAINEL ADMINISTRATIVO: " + usuarioLogado.getNome().toUpperCase());
       }


       carregarTudo();
   }


   private void mostrarPainel(VBox painel) {
       for (VBox p : new VBox[]{paneUsuarios, paneItens, paneCategorias, paneFornecedores}) {
           p.setVisible(p == painel);
           p.setManaged(p == painel);
       }
   }


   //tabelas


   private void configurarTabelas() {

        // Usuários
        tblUsuarios.setItems(usuarios);
        tblUsuarios.setPlaceholder(new EmptyState("Nenhum usuário cadastrado."));
        colUsuarioId.setCellValueFactory(c -> new SimpleStringProperty("#" + c.getValue().getId()));
        colUsuarioPerfil.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPerfil()));
        colUsuarioPerfil.setCellFactory(col -> new BadgeCell<>(s -> StatusBadge.StatusType.INFO));
        colUsuarioNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colUsuarioLogin.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLogin()));
        colUsuarioCargo.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue() instanceof Funcionario f && f.getCargo() != null && !f.getCargo().isBlank() ? f.getCargo() : "-"));
        colUsuarioSituacao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isAtivo() ? "ATIVO" : "INATIVO"));
        colUsuarioSituacao.setCellFactory(col -> new BadgeCell<>(s ->
                s.equals("ATIVO") ? StatusBadge.StatusType.SUCCESS : StatusBadge.StatusType.DANGER));
        colUsuarioAcoes.setCellFactory(col -> new AcoesCell<>("Desativar", this::editarUsuario, this::desativarUsuario));

   }




   private void carregarTudo() {
       try {
          usuarios.setAll(facade.listarUsuario().values());
       } catch (RuntimeException e) {
           logger.error("Falha ao carregar dados do painel administrativo: {}", e.getMessage(), e);
           exibirAlertaErro("Erro", "Falha ao carregar dados: " + e.getMessage());
       }
   }


   @FXML public void alterarPerfil() {
        
        boolean funcionario = "FUNCIONARIO".equals(cbUsuarioPerfil.getValue());
        fUsuarioCargo.setVisible(funcionario);
        fUsuarioCargo.setManaged(funcionario);
        if (!funcionario) fUsuarioCargo.setText("");
    
    }

   @FXML public void salvarUsuario() {

        String id = fUsuarioId.getText().trim();
        String nome = fUsuarioNome.getText().trim();
        String login = fUsuarioLogin.getText().trim();
        String senha = fUsuarioSenha.getText();
        String cargo = fUsuarioCargo.getText().trim();

        if (id.isEmpty() || nome.isEmpty() || login.isEmpty() || senha.isEmpty()) {
            formUsuario.aviso("Preencha os campos obrigatórios!", Banner.BannerType.DANGER);
            return;
        }

        boolean edicao = formUsuario.emEdicao();
        executar(formUsuario, edicao ? "Usuário atualizado!" : "Usuário cadastrado!", () -> {
            if (edicao) {
                Usuario u = facade.buscarUsuario(formUsuario.idEditando);
                u.setNome(nome);
                u.setLogin(login);
                u.setSenha(senha);
                if (u instanceof Funcionario f) f.setCargo(cargo);
                facade.atualizarUsuario(u.getId(), u);
            } else {
                switch (cbUsuarioPerfil.getValue()) {
                    case "FUNCIONARIO" -> facade.cadastrarFuncionario(new Funcionario(id, nome, login, senha, cargo));
                    case "ADMINISTRADOR" -> facade.cadastrarAdm(new Administrador(id, nome, login, senha));
                    default -> facade.cadastrarCliente(new Cliente(id, nome, login, senha));
                }
            }
            usuarios.setAll(facade.listarUsuario().values());
            limparUsuario();
        });

    }

   @FXML public void cancelarUsuario() {

        limparUsuario();
        formUsuario.limparAviso();

    }

    private void limparUsuario() {
        formUsuario.sairEdicao();
        fUsuarioNome.setText("");
        fUsuarioLogin.setText("");
        fUsuarioSenha.setText("");
        cbUsuarioPerfil.setDisable(false);
        cbUsuarioPerfil.setValue("CLIENTE");
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
            executar(formUsuario, "Usuário desativado!", () -> {
                facade.desativarUsuario(u.getId());
                usuarios.setAll(facade.listarUsuario().values());
                if (u.getId().equals(formUsuario.idEditando)) limparUsuario();
            });
        }
    }

   @FXML public void salvarItem() { }
   @FXML public void cancelarItem() { }


   @FXML public void salvarCategoria() { }
   @FXML public void cancelarCategoria() { }


   @FXML public void salvarFornecedor() { }
   @FXML public void cancelarFornecedor() { }


   @FXML
   public void handleSair() {
       Stage stage = (Stage) lblBoasVindas.getScene().getWindow();
       stage.close();
   }


   //Executa uma ação da facade e mostra o resultado no Banner do formulário
   private void executar(FormState form, String msgSucesso, Runnable acao) {
       try {
           acao.run();
           form.aviso(msgSucesso, Banner.BannerType.SUCCESS);
       } catch (RuntimeException e) {
           logger.warn("Operação do administrador falhou: {}", e.getMessage(), e);
           form.aviso(e.getMessage() != null ? e.getMessage() : "Erro inesperado.", Banner.BannerType.DANGER);
       }
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


   private static <T> StringConverter<T> conversor(Function<T, String> texto) {
       return new StringConverter<>() {
           @Override public String toString(T obj) { return obj == null ? "" : texto.apply(obj); }
           @Override public T fromString(String s) { return null; }
       };
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


   //Célula que mostra o texto dentro de um StatusBadge.
   private static class BadgeCell<T> extends TableCell<T, String> {
       private final Function<String, StatusBadge.StatusType> tipo;


       BadgeCell(Function<String, StatusBadge.StatusType> tipo) {
           this.tipo = tipo;
       }


       @Override
       protected void updateItem(String item, boolean empty) {
           super.updateItem(item, empty);
           setText(null);
           if (empty || item == null) {
               setGraphic(null);
               return;
           }
           StatusBadge badge = new StatusBadge(item, tipo.apply(item));
           badge.setMaxWidth(Region.USE_PREF_SIZE);
           setGraphic(badge);
       }
   }


   //Célula com os botões "Editar" e "Remover/Desativar"
   private static class AcoesCell<T> extends TableCell<T, Void> {
       private final HBox box;


       AcoesCell(String textoRemover, Consumer<T> onEditar, Consumer<T> onRemover) {
           Button editar = new Button("Editar");
           editar.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #4f46e5; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 4 10; -fx-cursor: hand;");
           editar.setOnAction(e -> onEditar.accept(getTableView().getItems().get(getIndex())));


           Button remover = new Button(textoRemover);
           remover.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #dc2626; -fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 4 10; -fx-cursor: hand;");
           remover.setOnAction(e -> onRemover.accept(getTableView().getItems().get(getIndex())));


           box = new HBox(6, editar, remover);
           box.setAlignment(Pos.CENTER);
       }


       @Override
       protected void updateItem(Void item, boolean empty) {
           super.updateItem(item, empty);
           setGraphic(empty ? null : box);
       }
   }
}
