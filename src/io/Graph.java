package io;

import java.util.*;

import common.*;

public class Graph implements Constants {
    public static final int COPY = 1, OR = 2, AND = 3, NOT = 4, XOR = 5;
    
    int[] p = new int[W * H];
    int mode = COPY;
    
    public void clear() {
        p = new int[W * H];
    }
    
    public void setMode(int m) {
        mode = m & 7;
    }
    
    public void setPoint(int x, int y) {
        if (x < 0 || y < 0 || x >= W || y >= H)
            return;
        int o = y * W + x;
        switch (mode) {
        case OR: case COPY:
            p[o] = 1;
            break;
        case NOT:
            p[o] = 0;
            break;
        case XOR:
            p[o] ^= 1;
        }
    }
    @Deprecated
    public void setPoint(int x, int y, int mode) {
        if (x < 0 || y < 0 || x >= W || y >= H)
            return;
        int o = y * W + x;
        switch (mode) {
        case OR: case COPY:
            p[o] = 1;
            break;
        case NOT:
            p[o] = 0;
            break;
        case XOR:
            p[o] ^= 1;
        }
    }
    
    void draw(int x, int y, int a) {
        if (x < 0 || y < 0 || x >= W || y >= H)
            return;
        int o = y * W + x;
        switch (mode) {
        case OR:
            p[o] |= a;
            break;
        case COPY:
            p[o] = a;
            break;
        case NOT:
            p[o] = 1 ^ a;
            break;
        case XOR:
            p[o] ^= a;
        case AND:
            p[o] &= a;
        }
    }
    
    public int getPoint(int x, int y) {
        if (x < 0 || y < 0 || x >= W || y >= H)
            return 1;
        return p[y * W + x];
    }
    
    public void line(int x1, int y1, int x2, int y2) {
        if (x1 > x2) {
            int t = x1;
            x1 = x2;
            x2 = t;
            t = y1;
            y1 = y2;
            y2 = t;
        }
        
        int dx = x2 - x1, dy = y2 - y1, sgn = 1, tx = 0, ty = 0;
        if (dy < 0) {
            dy = -dy;
            sgn = -1;
        }
        int m = dx > dy ? dx : dy, i = m;
        while (i-- >= 0) {
            setPoint(x1, y1);
            tx += dx;
            ty += dy;
            if (tx >= m) {
                x1++;
                tx -= m;
            }
            if (ty >= m) {
                y1 += sgn;
                ty -= m;
            }
        }
    }
    
    public void box(int x1, int y1, int x2, int y2, boolean fill) {
        if (x1 > x2) {
            int t = x1;
            x1 = x2;
            x2 = t;
        }
        if (y1 > y2) {
            int t = y1;
            y1 = y2;
            y2 = t;
        }
        
        if (fill) {
            for (; y1 <= y2; y1++)
                hoLine(x1, x2, y1);
        } else {
            hoLine(x1, x2, y1);
            hoLine(x1, x2, y2);
            for (; y1 <= y2; y1++) {
                setPoint(x1, y1);
                setPoint(x2, y1);
            }
        }
    }
    
    //水平线
    void hoLine(int x1, int x2, int y) {
        for (; x1 <= x2; x1++) {
            setPoint(x1, y);
        }
    }
    
    void ovalPoint(int ox, int oy, int x, int y) {
        setPoint(ox - x, oy - y);
        setPoint(ox - x, oy + y);
        setPoint(ox + x, oy - y);
        setPoint(ox + x, oy + y);
    }
    
    public void oval(int x, int y, int rx, int ry, boolean fill) {
        int asq = rx * rx, bsq = ry * ry;
        int asq2 = asq * 2, bsq2 = bsq * 2;
        int p;
        int x1 = 0, y1 = ry;
        int px = 0, py = asq2 * y1;
        p = bsq - asq * ry + ((asq + 2) >> 2);
        while (px < py) {
            x1++;
            px += bsq2;
            if (p < 0) {
                p += bsq + px;
            } else {
                if (fill) {
                    hoLine(x - x1 + 1, x + x1 - 1, y + y1);
                    hoLine(x - x1 + 1, x + x1 - 1, y - y1);
                }
                y1--;
                py -= asq2;
                p += bsq + px - py;
            }
            if (!fill) {
                ovalPoint(x, y, x1, y1);
            }

        }
        if (fill) {
            hoLine(x - x1, x + x1, y + y1);
            hoLine(x - x1, x + x1, y - y1);
        }
        p = bsq * x1 * x1 + bsq * x1 + asq * (y1 - 1) * (y1 - 1) - asq * bsq + ((bsq + 2) >> 2);
        while (--y1 > 0) {
            py -= asq2;
            if (p > 0) {
                p += asq - py;
            } else {
                x1++;
                px += bsq2;
                p += asq - py + px;
            }
            if (fill) {
                hoLine(x - x1, x + x1, y + y1);
                hoLine(x - x1, x + x1, y - y1);
            } else {
                ovalPoint(x, y, x1, y1);
            }
        }
        if (fill) {
            hoLine(x - rx, x + rx, y);
        } else {
            setPoint(x, y + ry);
            setPoint(x, y - ry);
            setPoint(x + rx, y);
            setPoint(x - rx, y);
        }
    }
    
    /**
     * 绘制字符串，不会换行
     * @param s 要绘制的字符串
     * @param x 横坐标
     * @param y 纵坐标
     * @param isBig 是否大字体
     */
    public void textOut(String s, int x, int y, boolean isBig) {
        GFont f = isBig ? GFont.FONT16 : GFont.FONT12;
//        try {
            byte[] txt = s.getBytes(); //getBytes("gb2312");
            for (int i = 0; i < txt.length; i++) {
                int c = txt[i] & 0xff;
                if (c < 161) {
                    drawChar((char) c, x, y, f);
                    x += f.ASCII_WIDTH;
                } else {
                    drawChar((char) ((c << 8) | (txt[i + 1] & 0xff)), x, y, f);
                    i++;
                    x += f.GBK_WIDTH;
                }
            }
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
    }
    
    public void textOut(S s, int x, int y, boolean isBig) {
        GFont f = isBig ? GFont.FONT16 : GFont.FONT12;
            byte[] txt = s.getBytes();
            for (int i = 0; i < txt.length; i++) {
                int c = txt[i] & 0xff;
                if (c < 161) {
                    drawChar((char) c, x, y, f);
                    x += f.ASCII_WIDTH;
                } else {
                    drawChar((char) ((c << 8) | (txt[i + 1] & 0xff)), x, y, f);
                    i++;
                    x += f.GBK_WIDTH;
                }
            }
    }
    
    /**
     * 绘制一个字符
     * @param c 全角或半角（可以是wqx图形）
     * @param x
     * @param y
     * @param f 字体
     */
    public void drawChar(char c, int x, int y, GFont f) {
        if (c < 161) {
            bitBlt(f.getASCII(c), x, y, f.ASCII_WIDTH, f.GBK_WIDTH);
        } else {
            bitBlt(f.getGBK(c), x, y, f.GBK_WIDTH, f.GBK_WIDTH);
        }
    }
    
    /**
     * 绘制点阵图
     * @param x 左上角横坐标
     * @param y 左上角纵坐标
     * @param w 宽度（像素）
     * @param h 高度（像素）
     * @param points 像素点阵
     */
    public void bitBlt(int[] points, int x, int y, int w, int h) {
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                draw(x + i, y + j, points[j * w + i]);
            }
        }
    }
    
    /**
     * 绘制透明点阵图
     * @param x 左上角横坐标
     * @param y 左上角纵坐标
     * @param w 像素宽度
     * @param h 像素高度
     * @param points 像素点阵
     */
//    public void transparentBlt(int[] points, int x, int y, int w, int h) {
//        for (int j = 0; j < h; j++) {
//            for (int i = 0; i < w; i++) {
//                if (points[j * w + i] != 0)
//                    setPoint(x + i, y + j);
//            }
//        }
//    }
    
    /**
     * 绘制8x8小字字符串
     * @param s 要绘制的字符串，只能是ascii
     * @param x 横坐标
     * @param y 纵坐标
     */
    public void tinyTextOut(String s, int x, int y) {
        byte[] t = s.getBytes();
        for (int i = 0; i < t.length; i++) {
            drawTinyChar((char) (t[i] & 0x7f), x, y);
            x += GFont.FONT8.ASCII_WIDTH;
        }
    }
    public void tinyTextOut(S s, int x, int y) {
        byte[] t = s.getBytes();
        for (int i = 0; i < t.length; i++) {
            drawTinyChar((char) (t[i] & 0x7f), x, y);
            x += GFont.FONT8.ASCII_WIDTH;
        }
    }
    
    /**
     * 绘制8x8字符
     * @param c ASCII字符
     * @param x 横坐标
     * @param y 纵坐标
     */
    public void drawTinyChar(char c, int x, int y) {
        bitBlt(GFont.FONT8.getASCII(c), x, y, GFont.FONT8.ASCII_WIDTH, GFont.FONT8.HEIGHT);
    }
    
    public static final int LEFT_MASK = 1, RIGHT_MASK = 2, UP_MASK = 4, DOWN_MASK = 8;
    /**
     * 屏幕滚动
     * @param d 方向 bit0, bit1: 左右; bit2, bit3:上下
     * @param hp 横向滚动点数
     * @param vp 纵向滚动点数
     */
    public void scroll(int d, int hp, int vp) {
        switch (d & 3) {
        case LEFT_MASK:
            for (int j = 0; j < p.length; j += W) {
                for (int i = 0, t = W - hp; i < t; i++)
                    p[j + i] = p[j + i + hp];
                for (int i = W - hp; i < W; i++)
                    p[j + i] = 0;
            }
            break;
        case RIGHT_MASK:
            for (int j = 0; j < p.length; j += W) {
                for (int i = W - hp - 1; i >= 0; i--)
                    p[j + i + hp] = p[j + i];
                for (int i = 0; i < hp; i++)
                    p[j + i] = 0;
            }
            break;
        }
        switch (d & 12) {
        case UP_MASK:
            p = Arrays.copyOfRange(p, vp * W, p.length + vp * W);
            break;
        case DOWN_MASK:
            for (int t = vp * W, i = p.length - 1; i >= t; i--)
                p[i] = p[i - t];
            for (int i = 0, t = vp * W; i < t; i++)
                p[i] = 0;
            break;
        }
    }
    
    /**
     * 直接修改显存
     * @param addr 偏移
     * @param value 8bit点阵数据
     */
    public void poke(int addr, byte value) {
        for (int i = addr << 3, j = 0; j < 8; j++) {
            p[i + j] = (value & pmask[j]) == 0 ? 0 : 1;
        }
    }
    
    /**
     * 获取显存点阵数据
     * @param addr 偏移
     * @return 8bit点阵数据
     */
    public byte peek(int addr) {
        byte v = 0;
        for (int i = addr << 3, j = 0; j < 8; j++) {
            if (p[i + j] != 0)
                v |= pmask[j];
        }
        return v;
    }
    
    /**
     * 全屏截图
     * @return 屏幕点阵
     */
    public int[] copy() {
        return Arrays.copyOf(p, p.length);
    }
    
    /**
     * 全屏粘贴
     * @param pic 屏幕点阵
     */
    public void paste(int[] pic) {
        p = Arrays.copyOf(pic, pic.length);
    }
    
    /**
     * 获取显存大小
     * @return 缓存大小
     */
    public int getBufferSize() {
        return 1600;
    }

    /**
     * 获取真彩颜色点阵数据
     * @return 点阵数据
     */
    public int[] getRGB() {
        int[] pp = new int[p.length];
        for (int i = 0; i < pp.length; i++) {
            if (p[i] == 0)
                pp[i] = 0xffffff; //白色
            else
                pp[i] = 0;
        }
        return pp;
    }
}