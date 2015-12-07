package common;

import java.io.*;
import java.util.*;

/**
 * 实用工具
 */

public abstract class Utilities implements Constants {
    static Map<Integer, Integer> pc2wqx = new HashMap<>(),
            wqx2pc = new HashMap<>(),
            mapping = new HashMap<>();
    static int gbuf = 0, tbuf = 0, delay = 0;
    static {
        System.loadLibrary("utilities");
        
        IniEditor ini = new IniEditor();
        try {
            ini.load("res/config.ini");
            gbuf = Integer.parseInt(ini.get("Ram", "graphbuffer"), 16);
            tbuf = Integer.parseInt(ini.get("Ram", "textbuffer"), 16);
            delay = Integer.parseInt(ini.get("Interpreter", "delay"));
            List<String> l = ini.optionNames("KeyValue");
            for (String s : l) {
                Integer i1 = Integer.parseInt(s),
                        i2 = Integer.parseInt(ini.get("KeyValue", s));
                wqx2pc.put(i1, i2);
                pc2wqx.put(i2, i1);
            }
            l = ini.optionNames("Mapping");
            for (String s : l) {
                Integer i1 = Integer.parseInt(s),
                        i2 = Integer.parseInt(ini.get("Mapping", s), 16);
                mapping.put(i1, i2);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("load ini file failed.");
        }
    }
    
    /**
     * 获取  <i>Shift + 某个键</i> 所获得的字符
     * @param key PC键值
     * @return 获得的字符
     */
    public static int getUpperChar(int key) {
        if (key >= 'A' && key <= 'Z')
            return key & 0xdf;
        switch (key) {
        case '`':
            return '~';
        case '1':
            return '!';
        case '2':
            return '@';
        case '3':
            return '#';
        case '4':
            return '$';
        case '5':
            return '%';
        case '6':
            return '^';
        case '7':
            return '&';
        case '8':
            return '*';
        case '9':
            return '(';
        case '0':
            return ')';
        case '-':
            return '_';
        case '=':
            return '+';
        case '\\':
            return '|';
        case '[':
            return '{';
        case ']':
            return '}';
        case ';':
            return ':';
        case '\'':
            return '"';
        case ',':
            return '<';
        case '.':
            return '>';
        case '/':
            return '?';
        default:
            return key;
        }
    }
    
    /**
     * 判断两个浮点数是否相等，精度1e-9
     * @param a
     * @param b
     * @return 判断结果
     */
    public static boolean doubleEqual(double a, double b) {
        return Math.abs(a - b) < 1e-9;
    }
    
    /**
     * 判断浮点数是否为0
     * @param a 浮点数
     * @return 判断结果
     */
    public static boolean doubleIsZero(double a) {
        return Math.abs(a) < 1e-9;
    }
    
    /**
     * 把实数转换为字符串。精度为1E-9
     */
    public static native String realToString(double a);
    
    /**
     * 把1字节转换为字符串。缺陷：若b < 0 (-128 ~ -1), 则会变为问号
     */
    public static native String byteToString(byte b);
    
    /**
     * 字符串转换为double。c语言str2d实现
     */
    public static native double str2d(String s);
    
    /**
     * 判断字符是否是可打印的控制字符（<i>F1 - F12, Up, Down, Left, Right, PageUp, PageDown, End, Home</i>）
     * @param c 字符
     * @return 判断结果
     */
    public static boolean isPrintableControl(int c) {
        return c >= 112 && c <= 123 || c >= 33 && c <= 40;
    }
    
    /**
     * 判断字符是否是空白符（不包含换行符0xa）
     * @param c 字符
     * @return 判断结果
     */
    public static boolean isWhiteSpace(int c) {
        return c == ' ' || c == '\t' || c == '\f' || c == 0xd;
    }
    
    /**
     * 判断字符是否是控制字符
     * @param c 字符
     * @return 判断结果
     */
    public static boolean isControl(int c) {
        return c >= 0 && c < 0x21 || c == 0x7f;
    }
    
    /**
     * 判断字符是否是字母
     * @param c 字符
     * @return 判断结果
     */
    public static boolean isAlpha(int c) {
        return (c | 0x20) >= 'a' && (c | 0x20) <= 'z';
    }
    
    /**
     * 把大写字母转换成小写字母
     * @param c 字符
     * @return 转换结果，非大写字母则返回原字符
     */
    public static int toLowerCase(int c) {
        return c >= 'A' && c <= 'Z' ? c | 0x20 : c;
    }
    
    /**
     * 判断字符是否是gvb标识符
     * @param c 字符
     * @return 判断结果
     */
    public static boolean isWord(int c) {
        return (c | 0x20) >= 'a' && (c | 0x20) <= 'z' || c >= '0' && c <= '9';
    }
    
    /**
     * pc键位转wqx键位,pc键位的大写字母自动转小写
     * @param rawKey pc键位
     * @return wqx键位
     */
    public static int convertKeyCode(int rawKey) {
        Integer i = pc2wqx.get(rawKey);
        if (rawKey >= 'A' && rawKey <= 'Z')
            return rawKey | 0x20;
        return i == null ? rawKey : i;
    }
    
    /**
     * wqx键位转pc键位
     * @param wqxKey 
     * @return pc键位
     */
    public static int recoverKeyCode(int wqxKey) {
        Integer i = wqx2pc.get(wqxKey);
        if (i == null && wqxKey >= 'a' && wqxKey <= 'z')
            return wqxKey & 0xdf;
        return i == null ? wqxKey : i;
    }
    
    /**
     * 获取按键映射值，若映射值不存在则返回0
     * @param wqxKey wqx键值
     * @return 映射值
     * <br><i>bit0 - bit7</i> : 键值表示的位
     * <br><i>bit8 - bit10</i> : 键值映射的内存偏移
     */
    public static int mapWQXKey(int wqxKey) {
        Integer i = mapping.get(wqxKey);
        return i == null ? 0 : i;
    }
    
    /**
     * 获取显示缓存
     * @return 显示缓存
     */
    public static int getGBUF() {
        return gbuf;
    }
    
    /**
     * 获取文字缓存
     * @return 文字缓存
     */
    public static int getTBUF() {
        return tbuf;
    }
    
    public static int getDelay() {
        return delay;
    }
    
    /**
     * byte数组模式匹配
     * @param s 要匹配的数组
     * @param a 模式数组
     * @return 找到的位置，找不到返回-1
     */
    public static int byteArrayMatch(byte[] s, byte[] a) {
        L1:
        for (int i = 0, l = s.length - a.length; i < l; i++) {
            if (s[i] == a[0]) {
                for (int j = 1; j < a.length; j++) {
                    if (s[i + j] != a[j])
                        continue L1;
                }
                return i;
            }
        }
        return -1;
    }
    
    /**
     * 把像素点阵数据转换为byte点阵数据
     * @param graph 像素点阵
     * @param w 像素宽度
     * @param h 像素高度
     * @return byte点阵
     */
    public static byte[] toByteData(int[] graph, int w, int h) {
        int bw = (w + 7) >>> 3, bw_ = w >>> 3;
        byte[] b = new byte[bw * h];
        
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < bw_; i++) {
                byte bb = 0;
                for (int k = 0; k < 8; k++) {
                    if (graph[j * w + (i << 3) + k] != 0)
                        bb |= pmask[k];
                }
                b[j * bw + i] = bb;
            }
            if (bw > bw_) {
                byte bb = 0;
                for (int i = 0, k = w - (bw_ << 3); i < k; i++) {
                    if (graph[j * w + (bw_ << 3) + i] != 0)
                        bb |= pmask[i];
                }
                b[j * bw + bw_] = bb;
            }
        }
        
        return b;
    }
    
    /**
     * 把8byte点阵转换为像素点阵
     * @param b 8byte点阵
     * @param w 宽度（像素）
     * @param h 高度（像素）
     * @return 像素点阵
     */
    public static int[] toPointData(byte[] b, int w, int h) {
        int[] p = new int[w * h];
        int t1 = 0, t2 = 0;
        
        for (int o = 0; o < h; o++) {
            for (int i = 0, j = w >>> 3; i < j; i++) {
                for (int k = 0, l = b[t2]; k < 8; k++)
                    p[t1] = (l & pmask[k]) == 0 ? 0 : 1;
                t1++;
                t2++;
            }
            for (int i = 0, j = w & 7, k = b[t2]; i < j; i++) {
                p[t1] = (k & pmask[i]) == 0 ? 0 : 1;
                t1++;
            }
            if ((w & 7) != 0)
                t2++;
        }
        return p;
    }
    
    public static void main(String[] args) throws IOException {
//        int[] a={1,1,1,1,1,1,1,1,1,1,1,
//                0,0,0,0,0,0,0,0,1,0,0,
//                0,0,0,0,1,1,1,1,1,1,0,
//                1,1,1,1,0,0,0,0,0,0,1};
        //byte[] b=toByteData(a,11,4);
        //System.out.println(Arrays.toString(toPointData(new byte[]{1,2,4,8,16,32,64,-1},4,8)));
        System.out.println(new String(new byte[] {(byte)141,103}));
        
//        for (int i = 0;i < b.length;i++)
//            System.out.print(Integer.toHexString(b[i]&0xff)+",");
        
//        OutputStream o =new BufferedOutputStream(new FileOutputStream("1.bmp"));
//        save1ColorBMP(b,11,4,o);
//        o.close();
    }
}
