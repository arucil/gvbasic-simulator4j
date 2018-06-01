package gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class TButton extends JButton {
    protected static Color[] rollOver, pressed, border;
    protected static final int colorCount = 13;
    protected ButtonModel m = getModel();
    static {
        rollOver = new Color[colorCount];
        border = new Color[colorCount];
        pressed = new Color[colorCount];
        for (int i = 0; i < colorCount; i++) {
            int t = i * 20;
            if (t > 255)
                t = 255;
            rollOver[i] = new Color(0xfd, 0xfc, 0xd0, t);
            border[i] = new Color(0x00, 0x00, 0x80, t);
            pressed[i] = new Color(0xf8, 0x86, 0x55, t);
        }
    }
    
    public TButton() {
        super();
        initialize();
    }
    
    public TButton(Action a) {
        super(a);
        initialize();
    }
    
    public TButton(String text) {
        super(text);
        initialize();
    }
    
    public TButton(Icon icon) {
        super(icon);
        initialize();
    }
    
    public TButton(String text, Icon icon) {
        super(text, icon);
        initialize();
    }
    
    private void initialize() {
        setContentAreaFilled(false);
    }
    
    public void paintBorder(Graphics g) {
        g.setColor(border[t]);
        g.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
    }
    
    int t;
    Timer fadeIn = new Timer(16, new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (t < colorCount - 1) {
                t++;
                repaint();
            } else
                fadeIn.stop();
        }
    }), fadeOut = new Timer(10, new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (t > 0) {
                t--;
                repaint();
            } else
                fadeOut.stop();
        }
    });
    public void paintComponent(Graphics g) {
        if (m.isPressed()) {
            t = colorCount - 1;
            g.setColor(pressed[t]);
            g.fillRect(0, 0, getSize().width - 1, getSize().height - 1);
            g.setColor(border[t]);
            
        } else {
            g.setColor(rollOver[t]);
            g.fillRect(0, 0, getSize().width - 1, getSize().height - 1);
            g.setColor(border[t]);
            if (m.isRollover()) {
                if (t < colorCount - 1 && !fadeIn.isRunning()) {
                    fadeOut.stop();
                    fadeIn.start();
                }
            } else {
                if (t > 0 && !fadeOut.isRunning()) {
                    fadeIn.stop();
                    fadeOut.start();
                }
            }
        }
        
        super.paintComponent(g);
    }
}