package com.loja.ui;

import com.loja.business.CategoriaBusiness;
import com.loja.business.ContratoBusiness;
import com.loja.business.FornecedorBusiness;
import com.loja.business.ItemBusiness;
import com.loja.business.MultaBusiness;
import com.loja.business.UsuarioBusiness;
import com.loja.business.interfaces.ICategoriaBusiness;
import com.loja.business.interfaces.IContratoBusiness;
import com.loja.business.interfaces.IFornecedorBusiness;
import com.loja.business.interfaces.IItemBusiness;
import com.loja.business.interfaces.IMultaBusiness;
import com.loja.business.interfaces.IUsuarioBusiness;
import com.loja.padrao.facade.LojaFacade;
import com.loja.padrao.facade.interfaces.ILojaFacade;
import com.loja.repositories.CategoriaPersistenciaJPA;
import com.loja.repositories.ContratoPersistenciaCSV;
import com.loja.repositories.FornecedorPersistenciaCSV;
import com.loja.repositories.ItemPersistenciaCSV;
import com.loja.repositories.MultaPersistenciaCSV;
import com.loja.repositories.UsuarioPersistenciaCSV;
import com.loja.repositories.UsuarioPersistenciaJPA;
import com.loja.repositories.interfaces.ICategoriaRepository;
import com.loja.repositories.interfaces.IContratoRepository;
import com.loja.repositories.interfaces.IFornecedorRepository;
import com.loja.repositories.interfaces.IItemRepository;
import com.loja.repositories.interfaces.IMultaRepository;
import com.loja.repositories.interfaces.IUsuarioRepository;
import com.loja.repositories.jpa.JPAUtil;
import com.loja.ui.navigation.Navigator;

import javafx.application.Application;
import javafx.stage.Stage;

public class LojaApp extends Application {

    private ILojaFacade facade;

    public static ILojaFacade montarFacade() {
        IUsuarioRepository usuarioRepo = new UsuarioPersistenciaJPA();
        IItemRepository itemRepo = new ItemPersistenciaCSV("dados/itens.csv");
        ICategoriaRepository categoriaRepo = new CategoriaPersistenciaJPA();
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
        JPAUtil.close();
    }
}