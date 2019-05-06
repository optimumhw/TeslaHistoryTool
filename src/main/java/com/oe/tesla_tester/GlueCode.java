package com.oe.tesla_tester;

import controller.Controller;
import model.EnumBaseURLs;
import model.EnumUsers;
import model.TeslaAPIModel;
import view.MainFrame;

public class GlueCode {

    public GlueCode() {
        TeslaAPIModel model = new TeslaAPIModel();
        MainFrame view = new MainFrame();
        Controller controller = new Controller();

        controller.tellControllerAboutTheModel(model);
        controller.tellTheControllerAboutTheView(view);

        view.setController(controller);
        view.setLocationRelativeTo(null);
        view.setVisible(true);

        controller.initModel( EnumBaseURLs.LocalHost, EnumUsers.DevOps);

        view.fillAPIHosts();
        view.fillUsers();
    }
}
