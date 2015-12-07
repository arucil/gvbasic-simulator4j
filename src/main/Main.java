package main;

import javax.swing.*;

public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        System.setProperty("java.library.path", System.getProperty("user.dir") + "\\res;" +
               System.getProperty("java.library.path"));
        System.out.println(System.getProperty("java.library.path"));
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new gui.Form();
            }
        });
    }

}
