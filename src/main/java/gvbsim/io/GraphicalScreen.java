package gvbsim.io;

import gvbsim.common.ByteString;

public interface GraphicalScreen {
    /**
     * GraphicScreen.<i>OR</i> 黑色<br>GraphicScreen.<i>CLEAR</i> 白色<br>GraphicScreen.<i>NOT</i> 反色
     */
    int OR = 1, CLEAR = 0, NOT = 2;

    /**
     * <i>FILL</i> 填充<br><i>HOLLOW</i> 不填充
     */
    int FILL = 1, HOLLOW = 0;
    
    /**
     * 光标定位
     * @param y 纵坐标,0开始
     * @param x 横坐标0开始
     */
    void locate(int y, int x);

    /**
     * 获取光标横坐标（0开始）
     */
    int getX();

    /**
     * 获取光标纵坐标（0开始）
     */
    int getY();

    /**
     * 显示文字
     * @param s 文字
     * @param cr 是否换行
     */
    default void print(ByteString s, boolean cr) {
        if (s != null) {
            print(s.getBytes(), cr);
        }
    }
    void print(byte[] bytes, boolean cr);

    /**
     * 输入文字
     * <br>Ctrl切换中英文输入法。中文输入法是Google输入法^_^
     * @return 输入的字符串
     */
    ByteString input() throws InterruptedException;

    /**
     * 清屏
     */
    void cls();

    /**
     * 切换到文本模式(光标闪烁)
     */
    void text();

    /**
     * 切换到图形模式
     */
    void graph();

    /**
     * 设置光标闪烁
     * @param f 是否闪烁
     */
    void setFlash(boolean f);

    /**
     * 画点
     * @param x 横坐标
     * @param y 纵坐标
     * @param mode 绘图模式
     */
    void draw(int x, int y, int mode);

    /**
     * 画线
     * @param x1 点1横纵坐标
     * @param y1
     * @param x2 点2横纵坐标
     * @param y2
     * @param mode 绘图模式
     */
    void line(int x1, int y1, int x2, int y2, int mode);

    /**
     * 画矩形
     * @param x1 点1横纵坐标
     * @param y1
     * @param x2 点2横纵坐标
     * @param y2
     * @param fill 是否填充
     * @param mode 绘图模式
     */
    void box(int x1, int y1, int x2, int y2, int fill, int mode);
    
    /**
     * 画圆
     * @param x 圆心横坐标
     * @param y 圆心纵坐标
     * @param r 半径
     * @param fill 是否填充
     * @param mode 绘图模式
     */
    void circle(int x, int y, int r, int fill, int mode);

    /**
     * 画椭圆
     * @param x 圆心横坐标
     * @param y 圆心纵坐标
     * @param rx X轴半径
     * @param ry Y轴半径
     * @param fill 是否填充
     * @param mode 绘图模式
     */
    void ellipse(int x, int y, int rx, int ry, int fill, int mode);

    /**
     * 获取像素点
     * @return 若有点则返回非零值否则返回0。若坐标超出屏幕则返回1
     */

    int point(int x, int y);

    /**
     * 任意位置显示文字
     * @param s 字符串
     * @param x
     * @param y
     * @param font 为零大字体；不为零小字体
     * @param mode 绘图模式。1=COPY,2=OR,3=AND,4=NOT,5=XOR
     */
    void textOut(ByteString s, int x, int y, int font, int mode);

    /**
     * 贴图
     * @param addr 图像点阵ram地址
     * @param x 横坐标
     * @param y 纵坐标
     * @param w 宽度（像素）
     * @param h 高度（像素）
     * @param mode 绘图模式。同textOut
     */
    void paint(int addr, int x, int y, int w, int h, int mode);
}
