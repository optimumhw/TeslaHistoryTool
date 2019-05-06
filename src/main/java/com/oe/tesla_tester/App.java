package com.oe.tesla_tester;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    public static void main( String[] args )
    {
        try {
            javax.swing.UIManager.LookAndFeelInfo[] installedLookAndFeels
                    = javax.swing.UIManager.getInstalledLookAndFeels();

            for (int idx = 0; idx < installedLookAndFeels.length; idx++) {
                if ("Nimbus".equals(installedLookAndFeels[idx].getName())) {
                    javax.swing.UIManager.setLookAndFeel(installedLookAndFeels[idx].getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException |
                InstantiationException |
                IllegalAccessException |
                javax.swing.UnsupportedLookAndFeelException ex) {
            Logger logger = LoggerFactory.getLogger(App.class.getName());
            logger.error(null, ex);

        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GlueCode();
            }
        });
    }
}