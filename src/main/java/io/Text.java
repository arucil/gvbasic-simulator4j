package io;

import common.*;

public class Text implements Constants {
    int x, y; //光标位置
    byte[] t; //文字缓冲
    Graph g;
    boolean text; //是否文字模式
    boolean cursor; //光标是否闪烁
    GFont font;
    
    public Text(Graph g) {
        this.g = g;
        font = GFont.FONT16;
        t = new byte[GFont.FONT12.COLUMN * GFont.FONT12.ROW];
        text = true;
        cursor = true;
    }
    
    /**
     * 是否文字模式（文字模式下刷新屏幕，图像会消失）
     * @return 结果
     */
    public boolean isText() {
        return text;
    }

    /**
     * 设置文字模式
     * @param text 是否文字模式
     */
    public void setTextMode(boolean text) {
        this.text = text;
    }

    /**
     * 光标是否闪烁
     * @return 闪烁
     */
    public boolean isCursor() {
        return cursor;
    }

    /**
     * 设置光标闪烁
     * @param cursor 是否闪烁
     */
    public void setCursor(boolean cursor) {
        this.cursor = cursor;
    }

    /**
     * 获取横坐标（0开始）
     * @return
     */
    public int getX() {
        return x;
    }
    
    /**
     * 获取纵坐标（0开始）
     * @return
     */
    public int getY() {
        return y;
    }
    
    /**
     * 设置光标横坐标（0开始）
     * @param x 
     */
    public void setX(int x) {
        if (x < 0 || x >= font.COLUMN)
            return;
        this.x = x;
    }
    
    /**
     * 设置纵坐标（0开始）
     * @param y
     */
    public void setY(int y) {
        if (y < 0 || y >= font.ROW)
            return;
        this.y = y;
    }
    
    @Deprecated
    public void locate(int y, int x) {
        if (x < 0 || y < 0 || x >= font.COLUMN || y >= font.ROW)
            return;
        this.y = y;
        this.x = x;
    }
    
    /**
     * 当前位置显示文字（符合wqx的显示）
     * @param s 文字
     */
    public void append(String s) {
        byte[] txt = s.getBytes();
        for (int i = x; i < font.COLUMN; i++) //把当前行之后的文字全清除
            t[i + y * font.COLUMN] = 0;
        for (int i = 0; i < txt.length; i++) {
            if ((txt[i] & 0xff) > 0xa0 && x == font.COLUMN - 1) {
                t[y * font.COLUMN + x] = 32;
                x = 0;
                y++;
            }
            if (y >= font.ROW)
                moveLine();
            t[y * font.COLUMN + x++] = txt[i];
            if (i < txt.length - 1 && (txt[i] & 0xff) > 0xa0)
                t[y * font.COLUMN + x++] = txt[++i];
            if (x >= font.COLUMN) {
                x = 0;
                y++;
            }
        }
    }
    public void append(S s) {
        byte[] txt = s.getBytes();
        for (int i = x; i < font.COLUMN; i++) //把当前行之后的文字全清除
            t[i + y * font.COLUMN] = 0;
        for (int i = 0; i < txt.length; i++) {
            if ((txt[i] & 0xff) > 0xa0 && x == font.COLUMN - 1) {
                t[y * font.COLUMN + x] = 32;
                x = 0;
                y++;
            }
            if (y >= font.ROW)
                moveLine();
            t[y * font.COLUMN + x++] = txt[i];
            if (i < txt.length - 1 && (txt[i] & 0xff) > 0xa0)
                t[y * font.COLUMN + x++] = txt[++i];
            if (x >= font.COLUMN) {
                x = 0;
                y++;
            }
        }
    }
    
    /**
     * 文字上移一行, x不会置0
     */
    void moveLine() {
        for (int j = 0; j < font.ROW - 1; j++) {
            for (int k = 0; k < font.COLUMN; k++)
                t[k + j * font.COLUMN] = t[k + (j + 1) * font.COLUMN];
        }
        for (int j = 0; j < font.COLUMN; j++)
            t[(font.ROW - 1) * font.COLUMN + j] = 0;
        y--;
        
        //更新Graph
        g.scroll(Graph.UP_MASK, 0, font.HEIGHT);
    }
    
    /**
     * 光标移到下一行
     */
    public void nextLine() {
        y++;
        x = 0;
        if (y >= font.ROW)
            moveLine();
    }
    
    /**
     * 设置字体
     * @param isBig 是否大字体
     */
    public void setFont(boolean isBig) {
        if (isBig)
            font = GFont.FONT16;
        else
            font = GFont.FONT12;
    }
    
    /**
     * 获取字体
     * @return 字体
     */
    public GFont getFont() {
        return font;
    }
    
    /**
     * 获取显存
     * @return 显存
     */
    public Graph getGraph() {
        return g;
    }
    
    /**
     * 清空文字缓冲，并把光标设为(0, 0)。不会清空屏幕
     */
    public void clear() {
        java.util.Arrays.fill(t, (byte) 0);
        x = y = 0;
    }
    
    /**
     * 把缓冲区文字刷到Graph<br>
     */
    public void update() {
        if (text)
            g.clear();
        int p = 0;
        char c;
        for (int j = 0; j < font.ROW; j++) {
            for (int i = 0; i < font.COLUMN; i++) {
                c = (char) (t[p++] & 0xff);
                if (c > 0xa0)
                    c = (char) ((c << 8) | (t[p++] & 0xff));
                if (c != 0)
                    g.drawChar(c, i * font.ASCII_WIDTH, j * font.HEIGHT, font);
                if (c > 0xa0)
                    i++;
            }
        }
    }
    
    /**
     * 直接修改文字缓存
     * @param pos 偏移
     * @param value byte值
     */
    public void poke(int pos, byte value) {
        t[pos] = value;
    }
    
    /**
     * 获取文字缓存的值
     * @param pos 位置
     * @return byte值
     */
    public byte peek(int pos) {
        return t[pos];
    }
    
    /**
     * 直接设置文字缓存偏移
     * @param pos 偏移
     */
    public void setPosition(int pos) {
        y = pos / font.COLUMN;
        x = pos % font.COLUMN;
    }
    
    /**
     * 获取文字缓存偏移
     * @return 偏移
     */
    public int getPosition() {
        return y * font.COLUMN + x;
    }
    
    /**
     * 获取文字缓存
     * @return 文字缓存（引用，非复制）
     */
    public byte[] getBuffer() {
        return t;
    }
    
    /**
     * 获取缓存大小
     * @return 缓存大小
     */
    public int getBufferSize() {
        return t.length;
    }
    
    public static final int IS_ASCII = 0, IS_FORMER_GBK = 1, IS_LATTER_GBK = 2;
    /**
     * 获取当前光标状态
     * @return Text.<i>IS_ASCII</i> 光标位于ascii字符<br>Text.<i>IS_FORMER_GBK</i> 光标位于GBK字符的前半部分<br>Text.<i>IS_LATTER_GBK</i> 光标位于GBK字符的后半部分
     */
    public int getPositionState() {
        int i = y * font.COLUMN, j, s = 0;
        for (j = 0; j <= x; j++) {
            if ((t[i + j] & 0xff) >= 161) { //0xa0
                if (s == IS_FORMER_GBK)
                    s = IS_LATTER_GBK;
                else
                    s = IS_FORMER_GBK;
            } else {
                s = IS_ASCII;
            }
        }
        return s;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer("[");
        for (int i = 0, j = font.COLUMN * font.ROW; i < j; i++) {
            sb.append(Integer.toHexString(t[i] & 0xff));
            if (i < j - 1)
                sb.append(", ");
        }
        return sb.append("]").toString();
    }
}
