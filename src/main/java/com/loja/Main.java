package com.loja;

import com.loja.padrao.facade.interfaces.ILojaFacade;
import com.loja.ui.LojaApp;
import com.loja.ui.MenuLogin;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--console")) {
            ILojaFacade facade = LojaApp.montarFacade();
            new MenuLogin(facade).iniciar();
        } else {
            Application.launch(LojaApp.class, args);
        }
    }
}