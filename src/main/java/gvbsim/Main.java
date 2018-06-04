package gvbsim;

import javax.swing.*;

public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SwingUtilities.invokeLater(gvbsim.gui.Form::new);
    }

}
