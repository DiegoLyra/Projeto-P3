package com.loja.ui;

import com.loja.business.*;
import com.loja.business.interfaces.*;
import com.loja.padrao.facade.LojaFacade;
import com.loja.padrao.facade.interfaces.ILojaFacade;
import com.loja.repositories.*;
import com.loja.repositories.interfaces.*;
import com.loja.ui.navigation.Navigator;
import javafx.application.Application;
import javafx.stage.Stage;

public class LojaApp extends Application {

    private ILojaFacade facade;

    public static ILojaFacade montarFacade() {
        IUsuarioRepository usuarioRepo = new UsuarioPersistenciaCSV("dados/usuarios.csv");
        IItemRepository itemRepo = new ItemPersistenciaCSV("dados/itens.csv");
        ICategoriaRepository categoriaRepo = new CategoriaPersistenciaCSV("dados/categorias.csv");
        IFornecedorRepository fornecedorRepo = new FornecedorPersistenciaCSV("dados/fornecedores.csv");
        IContratoRepository contratoRepo = new ContratoPersistenciaCSV("dados/contratos.csv");
        IMultaRepository multaRepo = new MultaPersistenciaCSV("dados/multas.csv");

        IUsuarioBusiness usuarioBusiness = new UsuarioBusiness(usuarioRepo);
        ICategoriaBusiness categoriaBusiness = new CategoriaBusiness(categoriaRepo);
        IFornecedorBusiness fornecedorBusiness = new FornecedorBusiness(fornecedorRepo);
        IItemBusiness itemBusiness = new ItemBusiness(itemRepo, categoriaRepo, fornecedorRepo);
        IContratoBusiness contratoBusiness = new ContratoBusiness(contratoRepo, itemBusiness, usuarioBusiness);
        IMultaBusiness multaBusiness = new MultaBusiness(multaRepo);

        return new LojaFacade(usuarioBusiness, itemBusiness, categoriaBusiness,
                fornecedorBusiness, contratoBusiness, multaBusiness);
    }

    @Override
    public void start(Stage stage) {
        facade = montarFacade();
        stage.setTitle("Loja que Aluga de um Tudo");
        new Navigator(stage, facade).showLogin();
        stage.show();
    }

    @Override
    public void stop() {
        if (facade != null) {
            facade.salvarTudo();
        }
    }
}