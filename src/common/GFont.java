package common;

import java.io.*;

/**
 * 字体通用类
 * @author Amlo
 *
 */

public abstract class GFont implements Constants {
    public final int COLUMN, ROW, ASCII_WIDTH, GBK_WIDTH, HEIGHT;
    
    GFont(int a, int b, int c, int d, int e) {
        COLUMN = a;
        ROW = b;
        ASCII_WIDTH = c;
        GBK_WIDTH = d; //汉字的宽度和高度一样
        HEIGHT = e;
    }
    
    byte[] ascii, gbk;
    public int[] getASCII(int c) {
        int of = c * GBK_WIDTH;
        int[] b = new int[GBK_WIDTH * ASCII_WIDTH];
        if (of < 0 || of + GBK_WIDTH > ascii.length)
            return b;
        for (int i = 0; i < GBK_WIDTH; i++) {
            for (int k = ascii[of + i], j = 0; j < ASCII_WIDTH; j++) {
                b[i * ASCII_WIDTH + j] = (k & pmask[j]) == 0 ? 0 : 1;
            }
        }
        return b;
    }
    
    public String toString() {
        return "GFont:[" + ASCII_WIDTH + " x " + GBK_WIDTH + "]";
    }
    
    public abstract int[] getGBK(int c);
    
    public static final GFont FONT16 = new GFont(20, 5, 8, 16, 16) {
        byte[] image;
        {
            try {
                InputStream in = null;
                try {
                    in = new BufferedInputStream(new FileInputStream("res/ascii8.bin"));
                    ascii = new byte[in.available()];
                    in.read(ascii);
                    in.close();
                    
                    in = new BufferedInputStream(new FileInputStream("res/gbfont16.bin"));
                    gbk = new byte[in.available()];
                    in.read(gbk);
                    in.close();
                    
                    in = new BufferedInputStream(new FileInputStream("res/image16.bin"));
                    image = new byte[in.available()];
                    in.read(image);
                } finally {
                    if (in != null)
                        in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public int[] getGBK(int c) {
            byte[] d;
            int of;
            int[] b = new int[32 * 8];
            
            if (c >= 0xf8a1 && c <= 0xfdd9 && (c & 0xff) > 160) {
                of = (((c >> 8) - 248) * 94 + (c & 0xff) - 0xa1) << 5;
                d = image;
            } else {
                int h = (c >> 8) - 0xa1;
                if (h > 8)
                    h -= 6;
                of = (h * 94 + (c & 0xff) - 0xa1) << 5;
                if (of < 0 || of + 32 > gbk.length)
                    return b;
                d = gbk;
            }
            for (int i = 0; i < 32; i++) {
                for (int k = d[of + i], j = 0; j < 8; j++) {
                    b[i * 8 + j] = (k & pmask[j]) == 0 ? 0 : 1;
                }
            }
            return b;
        }
    }, FONT12 = new GFont(26, 6, 6, 12, 13) {
        {
            try {
                InputStream in = null;
                try {
                    in = new BufferedInputStream(new FileInputStream("res/ascii.bin"));
                    ascii = new byte[in.available()];
                    in.read(ascii);
                    in.close();
                    
                    in = new BufferedInputStream(new FileInputStream("res/gbfont.bin"));
                    gbk = new byte[in.available()];
                    in.read(gbk);
                } finally {
                    if (in != null)
                        in.close();
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }

        @Override
        public int[] getGBK(int c) {
            int h = (c >> 8) - 0xa1;
            if (h > 8)
                h -= 6;
            int of = (h * 94 + (c & 0xff) - 0xa1) * 24;
            int[] b = new int[24 * 8];
            if (of < 0 || of + 24 > gbk.length)
                return b;
            for (int i = 0; i < 12; i++) {
                for (int k = gbk[of + (i << 1)], j = 0; j < 8; j++) {
                    b[i * 12 + j] = (k & pmask[j]) == 0 ? 0 : 1;
                }
                for (int k = gbk[of + (i << 1) + 1], j = 0; j < 6; j++) {
                    b[i * 12 + j + 8] = (k & pmask[j]) == 0 ? 0 : 1;
                }
            }
            return b;
        }
    }, FONT8 = new GFont(20, 10, 8, 8, 8) {
        {
            try {
                InputStream in = null;
                try {
                    in = new BufferedInputStream(new FileInputStream("res/ascii8x8.dat"));
                    ascii = new byte[in.available()];
                    in.read(ascii);
                } finally {
                    if (in != null)
                        in.close();
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }

        @Override
        public int[] getGBK(int c) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException();
        }
        
    };
}
