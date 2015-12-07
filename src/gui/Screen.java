package gui;

import javax.swing.*;
import java.awt.image.*;
import java.awt.*;
import java.awt.event.*;

import io.*;
import common.*;

public class Screen extends JPanel implements Constants {
    BufferedImage im;
    Graph p;
    Text t;
    boolean cursor;
    
    public Screen(Text txt) {
        p = txt.getGraph();
        t = txt;
        im = new BufferedImage(W, H, BufferedImage.TYPE_BYTE_BINARY,
                new IndexColorModel(1, 2, new byte[] {-1, 0}, new byte[] {-1, 0}, new byte[] {-1, 0}));
        setPreferredSize(new Dimension(W * PIXEL_SIZE, H * PIXEL_SIZE));
    }
    
    public Text getText() {
        return t;
    }
    
    public void paintComponent(Graphics g) {
        im.setRGB(0, 0, W, H, p.getRGB(), 0, W);
        g.drawImage(im, 0, 0, W * PIXEL_SIZE, H * PIXEL_SIZE, null);
        if (t.isCursor() && cursor) {
            int s = t.getPositionState();
            GFont f = t.getFont();
            int x = t.getX() * f.ASCII_WIDTH * PIXEL_SIZE,
                    y = t.getY() * f.HEIGHT * PIXEL_SIZE;
            
            g.setColor(Color.black);
            g.setXORMode(Color.white);
            
            if (s == Text.IS_ASCII) {
                g.fillRect(x, y, f.ASCII_WIDTH * PIXEL_SIZE, f.GBK_WIDTH * PIXEL_SIZE);
            } else if (s == Text.IS_FORMER_GBK) {
                g.fillRect(x, y, f.GBK_WIDTH * PIXEL_SIZE, f.GBK_WIDTH * PIXEL_SIZE);
            } else {
                g.fillRect(x - PIXEL_SIZE * f.ASCII_WIDTH, y, f.GBK_WIDTH * PIXEL_SIZE, f.GBK_WIDTH * PIXEL_SIZE);
            }
        }
    }
    
    Timer timer = new Timer(500, new FlashAction());
    public void flash() {
        timer.start(); //π‚±Í…¡À∏
    }
    
    public void stopFlash() {
        cursor = false;
        timer.stop();
    }
    
    class FlashAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (t.isCursor()) {
                cursor = !cursor;
                repaint();
            }
        }
    }
}