package core;

import common.*;
import io.*;
import gui.*;

import java.awt.event.*;
import java.io.*;
import java.util.*;

/**
 * 程序与用户交互的控制器
 */

public class Controller implements common.Constants, GraphicalScreen, Memory {
    Graph g;
    Text t;
    Screen scr;
    Form frm;
    
    public Controller(Screen screen, Form form) {
        t = screen.getText();
        g = t.getGraph();
        scr = screen;
        frm = form;
        ram = new byte[65536];
        tbuf = Utilities.getTBUF();
        gbuf = Utilities.getGBUF();
        tbuf2 = tbuf + t.getBufferSize();
        gbuf2 = gbuf + g.getBufferSize();
        
        try {
            InputStream in = null;
            try {
                in = new BufferedInputStream(new FileInputStream("res/py.dat"));
                pyList = new byte[in.available()];
                in.read(pyList);
                in.close();
                
                in = new BufferedInputStream(new FileInputStream("res/gb.dat"));
                gbList = new byte[in.available()];
                in.read(gbList);
            } finally {
                if (in != null)
                    in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void text() {
        t.setTextMode(true);
        t.setCursor(true);
        t.clear();
        g.clear();
        scr.flash();
        scr.repaint();
    }
    
    public void graph() {
        t.setTextMode(false);
        t.setCursor(false);
        t.clear();
        g.clear();
        scr.stopFlash();
        scr.repaint();
    }
    
    public void cls() {
        t.clear();
        g.clear();
        scr.repaint();
    }
    
    /**
     * 显示
     * @param s 显示字符串。如果s=null则不调用显示函数，直接查看cr
     * @param cr 是否换行
     */
    public void print(S s, boolean cr) {
        if (s != null)
            t.append(s);
        if (cr && t.getX() > 0) //显示的文字刚好填满一行的时候就不用再换行了
            t.nextLine();
        g.setMode(Graph.COPY);
        t.update();
        scr.repaint();
    }
    public void print(String s, boolean cr) {
        if (s != null)
            t.append(s);
        if (cr && t.getX() > 0) //显示的文字刚好填满一行的时候就不用再换行了
            t.nextLine();
        g.setMode(Graph.COPY);
        t.update();
        scr.repaint();
    }
    
    public void locate(int y, int x) {
        t.setX(x);
        t.setY(y);
        scr.repaint();
    }
    
    void setMode(int mode) {
        switch (mode) {
        case CLEAR:
            g.setMode(Graph.NOT);
            break;
        case NOT:
            g.setMode(Graph.XOR);
            break;
        default:
            g.setMode(Graph.COPY);
            break;
        }
    }
    
    public void box(int x1, int y1, int x2, int y2, int fill, int mode) {
        setMode(mode);
        g.box(x1, y1, x2, y2, fill != 0);
        scr.repaint();
    }
    
    public void draw(int x, int y, int mode) {
        setMode(mode);
        g.setPoint(x, y);
        scr.repaint();
    }
    
    public void line(int x1, int y1, int x2, int y2, int mode) {
        setMode(mode);
        g.line(x1, y1, x2, y2);
        scr.repaint();
    }
    
    public void circle(int x, int y, int r, int fill, int mode) {
        setMode(mode);
        g.oval(x, y, r, r, fill != 0);
        scr.repaint();
    }
    
    public void ellipse(int x, int y, int rx, int ry, int fill, int mode) {
        setMode(mode);
        g.oval(x, y, rx, ry, fill != 0);
        scr.repaint();
    }

    int inputMethod;
    String[] imPrompt = new String[] {" [英]", " [中]"};
    
    byte[] pyList, gbList, emptyGB = new byte[0];
    /**
     * <b>缺陷</b>：输入的字符串在最后一行无法换行，无法输入全角符号，无法输入图形
     * <br>Ctrl切换中英输入法
     */
    public S input() throws InterruptedException {
        try {
            int p0 = t.getPosition(), p1 = p0;
            GFont f = t.getFont();
            int max = f.COLUMN * f.ROW - 1;
            
            //中文输入法要用的数据
            int[] p = g.copy();
            String py = "a";
            byte[] gb = emptyGB;
            int pos = 0, tot = 0;
            boolean canUp = false, canDown = false;
                    
            setFormTitle();
            while (true) {
                //显示输入法悬浮窗
                if (inputMethod == 1 && py.length() > 0) {
                    g.paste(p);
                    t.update();
                    int x = (t.getX()) * f.ASCII_WIDTH, y = t.getY();
                    if (y < (f.ROW >> 1))
                        y = (y + 1) * f.HEIGHT;
                    else
                        y = y * f.HEIGHT - 28;
                    if (x > W - 102)
                        x = W - 102;
                    g.setMode(Graph.NOT);
                    g.box(x, y, x + 101, y + 27, true);
                    g.setMode(Graph.OR);
                    g.box(x + 1, y + 1, x + 100, y + 26, false);
                    g.line(x + 2, y + 12, x + 100, y + 12);
                    g.bitBlt(logo, x + 90, y + 2, 10, 10);
                    g.tinyTextOut(py, x + 3, y + 3);
                    tot = (gb.length >> 1) - pos * 5;
                    if (canDown = tot > 5)
                        tot = 5;
                    for (int i = 0; i < tot; i++) {
                        g.bitBlt(opt[i], x + 3 + i * 18, y + 14, 3, 6);
                        g.drawChar((char) (((gb[(i + pos * 5) << 1] & 0xff) << 8) | (gb[((i + pos * 5) << 1) + 1] & 0xff)),
                                x + 7 + i * 18, y + 14, GFont.FONT12);
                    }
                    if (canUp = pos > 0)
                        g.bitBlt(pgUp, x + 93, y + 15, 5, 3);
                    if (canDown)
                        g.bitBlt(pgDn, x + 93, y + 21, 5, 3);
                    scr.repaint();
                } else {
                    pos = 0;
                    gb = emptyGB;
                    canUp = canDown = false;
                    tot = 0;
                }
                int k = frm.inkey(false);
                switch (inputMethod) {
                case 0:
                    switch (k) {
                    case KeyEvent.VK_BACK_SPACE:
                        if (p1 > p0) {
                            if (t.peek(p1 - 1) < 0)
                                t.poke(--p1, (byte) 32); //空格，原来输入过文字的地方图像会被清除
                            t.poke(--p1, (byte) 32);
                            t.setPosition(p1);
                            t.update();
                            scr.repaint();
                        }
                        break;
                    case KeyEvent.VK_ENTER:
                        return new S(t.getBuffer(), p0, p1);
                    case KeyEvent.VK_CONTROL:
                        inputMethod = 1;
                        setFormTitle();
                        py = "";
                        gb = emptyGB;
                        pos = 0;
                        break;
                    default:
                        if (!Utilities.isPrintableControl(k) && k > 0x1f && k < 0x7f && p1 < max) {
                            if (k != KeyEvent.VK_SHIFT) {
                                if (frm.checkKey(KeyEvent.VK_SHIFT))
                                    k = Utilities.getUpperChar(k);
                                else if (k >= 'A' && k <= 'Z')
                                    k |= 0x20;
                            }
                            t.poke(p1++, (byte) k);
                            t.setPosition(p1);
                            t.update();
                            scr.repaint();
                        }
                    }
                    break;
                case 1:
                    switch (k) {
                    case '1': case '2': case '3': case '4': case '5':
                        if (k <= 48 + tot && p1 < max - 1) {
                            t.append(new String(gb, pos * 10 + ((k - 49) << 1), 2));
                            p1 = t.getPosition();
                            py = "";
                            g.paste(p);
                            t.update();
                            scr.repaint();
                        }
                        break;
                    case KeyEvent.VK_BACK_SPACE:
                        if (py.length() > 0) {
                            py = py.substring(0, py.length() - 1);
                            pos = 0;
                            gb = getGB(py);
                            g.paste(p);
                        } else if (p1 > p0) {
                            if (t.peek(p1 - 1) < 0)
                                t.poke(--p1, (byte) 32);
                            t.poke(--p1, (byte) 32);
                            t.setPosition(p1);
                        }
                        t.update();
                        scr.repaint();
                        break;
                    case KeyEvent.VK_UP: case KeyEvent.VK_PAGE_UP: case KeyEvent.VK_LEFT:
                        if (canUp)
                            pos--;
                        break;
                    case KeyEvent.VK_PAGE_DOWN: case KeyEvent.VK_DOWN: case KeyEvent.VK_RIGHT:
                        if (canDown)
                            pos++;
                        break;
                    case KeyEvent.VK_CONTROL:
                        inputMethod = 0;
                        setFormTitle();
                        g.paste(p);
                        t.update();
                        scr.repaint();
                        break;
                    case KeyEvent.VK_ENTER:
                        if (py.length() == 0)
                            return new S(t.getBuffer(), p0, p1);
                        else if (p1 < max - py.length() + 1) {
                            for (int i = 0; i < py.length(); i++)
                                t.poke(p1++, (byte) py.charAt(i));
                            py = "";
                            t.setPosition(p1);
                            g.paste(p);
                            t.update();
                            scr.repaint();
                        }
                        break;
                    default:
                        if (k >= 'A' && k <= 'Z' && py.length() < 6) {
                            py += Character.toString((char) (k | 0x20));
                            pos = 0;
                            gb = getGB(py);
                        }
                    }
                }
            }
        } finally {
            frm.setTitle(frm.getDefaultTitle());
            t.nextLine();
            t.update();
            scr.repaint();
        }
    }
    
    byte[] getGB(String py) {
        byte[] p = (py + "\0").getBytes();
        int i = 0, l = pyList.length - p.length;
        
        L1:
        for (; i < l; i += 11) {
            if (pyList[i] == p[0]) {
                for (int j = 1; j < p.length; j++) {
                    if (pyList[i + j] != p[j])
                        continue L1;
                }
                break;
            }
        }
        
        if (i >= l || py.length() == 0)
            return emptyGB;
        int n = (short) ((pyList[i + 7] & 0xff) | ((pyList[i + 8] & 0xff) << 8)) << 1,
            o = (short) ((pyList[i + 9] & 0xff) | ((pyList[i + 10] & 0xff) << 8)) - 44;
        if (n < 0)
            return emptyGB;
        return Arrays.copyOfRange(gbList, o, o + n);
    }
    
//    /**
//     * 获取全角标点
//     * @param c 半角标点
//     * @return 全角标点
//     */
//    byte[] getGBSymbol(int c) {
//        switch (c) {
//        case '`': return '｀'; case '~': return '～'; case '!': return '！'; case '@': return ''; case '#': return ''; 
//        case '$': return ''; case '%': return ''; case '^': return ''; case '&': return ''; case '*': return ''; 
//        case '(': return ''; case ')': return ''; case '_': return ''; case '+': return ''; case '-': return ''; 
//        case '=': return ''; case '': return ''; case '': return ''; case '': return ''; case '': return ''; 
//        case '': return ''; case '': return ''; case '': return ''; case '': return ''; case '': return ''; 
//        default:
//            return null;
//        }
//    }
        
    void setFormTitle() {
        frm.setTitle(frm.getDefaultTitle() + imPrompt[inputMethod]);
    }
    
    int[] logo = {//哈哈哈
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,0,0,0,0,1,1,
            1,1,1,0,0,1,1,0,0,1,
            1,1,0,0,1,1,1,1,1,1,
            1,1,0,0,1,1,1,1,1,1,
            1,1,0,0,1,1,0,0,0,1,
            1,1,1,0,0,1,1,0,0,1,
            1,1,1,1,0,0,0,0,0,1,
            1,1,1,1,1,1,1,1,1,1,
            1,1,1,1,1,1,1,1,1,1},
        opt[] = {
            {0,1,0,1,1,0,0,1,0,0,1,0,0,1,0,1,1,1}, //1
            {0,1,0,1,0,1,0,0,1,0,1,0,1,0,0,1,1,1}, //2
            {1,1,0,0,0,1,0,1,0,0,0,1,0,0,1,1,1,0}, //3
            {0,0,1,0,1,1,1,0,1,1,1,1,0,0,1,0,0,1}, //4
            {1,1,1,1,0,0,1,1,0,0,0,1,0,0,1,1,1,0}, //5
        },
        pgUp = {0,0,1,0,0,0,1,1,1,0,1,1,1,1,1},
        pgDn = {1,1,1,1,1,0,1,1,1,0,0,0,1,0,0};

    @Override
    public void setFlash(boolean f) {
        t.setCursor(f);
        scr.repaint();
    }

    @Override
    public int getX() {
        return t.getX();
    }

    @Override
    public int getY() {
        return t.getY();
    }
    
    private byte[] ram;
    private int gbuf, gbuf2, tbuf, tbuf2;

    @Override
    public byte peek(int addr) {
        if (addr >= gbuf && addr < gbuf2)
            return g.peek(addr - gbuf);
        else if (addr >= tbuf && addr < tbuf2)
            return t.peek(addr - tbuf);
        else if (addr >= 0 && addr < ram.length)
            return ram[addr];
        else
            return 0;
    }

    @Override
    public byte[] peek(int addr, int size) {
        byte[] b = new byte[size];
        for (int i = 0; i < size; i++)
            b[i] = peek(addr++);
        return b;
    }

    @Override
    public void poke(int addr, byte val) {
        if (addr >= gbuf && addr < gbuf2) {
            g.poke(addr - gbuf, val);
            scr.repaint();
        } else if (addr >= tbuf && addr < tbuf2)
            t.poke(addr - tbuf, val);
        else if (addr >= 0 && addr < ram.length)
            ram[addr] = val;
    }
    
    public void poke(int addr, byte[] val) {
        boolean c = false;
        for (byte b : val) {
            if (addr >= gbuf && addr < gbuf2) {
                g.poke(addr - gbuf, b);
                c = true;
            } else if (addr >= tbuf && addr < tbuf2)
                t.poke(addr - tbuf, b);
            else if (addr >= 0 && addr < ram.length)
                ram[addr] = b;
            addr++;
        }
        if (c)
            scr.repaint();
    }
    
    public byte[] getRAM() {
        return ram;
    }

    /**
     * call未实现
     */
    public boolean call(int addr) {
        return false;
    }

    @Override
    public int point(int x, int y) {
        return g.getPoint(x, y);
    }

    @Override
    public void textOut(S s, int x, int y, int font, int mode) {
        g.setMode(mode);
        g.textOut(s, x, y, font == 0);
    }

    @Override
    public void paint(int addr, int x, int y, int w, int h, int mode) {
        int[] p = Utilities.toPointData(Arrays.copyOfRange(ram, addr, addr + ((w + 7) >>> 3) * h + 1), w, h);
        g.setMode(mode);
        g.bitBlt(p, x, y, w, h);
    }
}