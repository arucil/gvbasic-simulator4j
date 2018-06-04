package gvbsim.io;

import java.io.*;
import gvbsim.common.*;

/**
 * wqx文件管理器。写入内容时不会自动追加分隔符，读取时会自动跳过分隔符
 * <br><b>示例:</b>
 * <br><code>FileManager fm = new FileManager(3); //最多同时打开三个文件
 * <br>System.out.println(fm.open("1.DAT", 0, FileManager.INPUT)); //打开一个数据文件，并显示是否成功
 * <br>System.out.println(fm.readReal(0)); //读取一个实数，并显示
 * <br>fm.closeAll(); //退出程序前确保文件全都关闭，否则更改无法更新文件
 * </code>
 * @author Amlo
 *
 */

public class FileManager {
    public static final FileAttr INPUT = new FileAttr("i", true, false, false),
            OUTPUT = new FileAttr("w", false, true, false),
            APPEND = new FileAttr("a", false, true, true), //append是可定位的，但是在程序中要限制不能定位
            RANDOM = new FileAttr("r", true, true, true);
    
    GFile[] files;
    String dir;
    
    /**
     * 默认初始化。支持同时打开10个文件
     */
    public FileManager() {
        this(10);
    }
    
    /**
     * 初始化一个文件管理器
     * @param maxFile 支持同时打开的最多文件数
     */
    public FileManager(int maxFile) {
        files = new GFile[maxFile];
        dir = "";
    }
    
    /**
     * 切换工作目录
     * @return 是否切换成功
     */
    public boolean chDir(String dir) {
        if (new File(dir).isDirectory()) {
            this.dir = dir;
            if (!this.dir.endsWith("/"))
                this.dir += "/";
            return true;
        }
        return false;
    }
    
    /**
     * 打开一个文件
     * @param filename 文件名
     * @param fnum 文件号(0 - 最大文件数)
     * @param fa 文件属性
     * @return 文件打开是否成功
     */
    public boolean open(String filename, int fnum, FileAttr fa) {
        try {
            GFile f = files[fnum];
            if (f != null && (f.canRead() || f.canPosition() || f.canWrite())) //文件已打开
                return false;
            files[fnum] = new GFile(dir + filename, fa.canRead, fa.canWrite, fa.canPosition);
            switch (fa.description) {
            case "a":
                files[fnum].position(files[fnum].length()); //把文件指针定位到文件末尾
                break;
            case "w":
                files[fnum].clear(); //清空文件内容
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 关闭文件
     * @param fnum 文件号
     * @return 是否关闭成功
     */
    public boolean close(int fnum) {
        try {
            files[fnum].close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 关闭所有已打开的文件
     * @return 是否关闭成功
     */
    public boolean closeAll() {
        boolean r = true;
        for (int i = 0; i < files.length; i++) {
            if (files[i] != null) {
                try {
                    files[i].close();
                } catch (IOException e) {
                    r = false;
                }
            }
        }
        return r;
    }
    
    /**
     * 返回文件指针位置。若发生错误，则返回-1
     * @param fnum 文件号
     * @return 文件指针
     */
    public int tell(int fnum) {
        try {
            return files[fnum].position();
        } catch (Exception e) {
            return -1;
        }
    }
    
    /**
     * 定位文件指针
     * @param pos 文件指针
     * @param fnum 文件号
     * @return 是否定位成功
     */
    public boolean seek(int pos, int fnum) {
        try {
            files[fnum].position(pos);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 读取一个字符
     * @param fnum 文件号
     * @return 读取的字符。若读取失败则返回null
     */
    public Byte readByte(int fnum) {
        try {
            return files[fnum].read();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 读取一串byte
     * @param size 串大小
     * @param fnum 文件号
     * @return 读取的字符串，以byte数组形式返回
     */
    public byte[] readBytes(int size, int fnum) {
        try {
            return files[fnum].read(size);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 读取一个整数
     * @param fnum 文件号
     * @return 读取到的整数。若读取失败则返回null
     */
    public Integer readInteger(int fnum) {
        try {
            return Integer.parseInt(getContent(fnum));
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 读取一个实数
     * @param fnum 文件号
     * @return 读取到的实数。若读取失败则返回null
     */
    public Double readReal(int fnum) {
        try {
            return Double.parseDouble(getContent(fnum));
        } catch (Exception e) {
            return null;
        }
    }
    
    public ByteString readS(int fnum) {
        try {
            GFile f = files[fnum];
            if (f.read() != '"')
                return null;
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            int c = 0;
            try {
                while ((c = f.read()) != '"')
                    b.write(c);
            } catch (IOException e) {
            }
            if (c != '"')
                return null;
            else
                f.read(); //跳过分隔符
            return new ByteString(b.toByteArray(), false);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 写入一个实数
     * @param a 实数
     * @param fnum 文件号
     * @return 是否写入成功
     */
    public boolean writeReal(double a, int fnum) {
        try {
            files[fnum].write(gvbsim.common.Utilities.realToString(a).getBytes());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 写入一个整数
     * @param a 整数
     * @param fnum 文件号
     * @return 写入是否成功
     */
    public boolean writeInteger(short a, int fnum) {
        try {
            files[fnum].write(Short.toString(a).getBytes());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public boolean writeInteger(int a, int fnum) {
        return writeInteger((short) a, fnum);
    }
    
    /**
     * 写入一个字符
     * @param a ascii字符
     * @param fnum 文件号
     * @return 是否写入成功
     */
    public boolean writeByte(byte a, int fnum) {
        try {
            files[fnum].write(a);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public boolean writeByte(int a, int fnum) {
        return writeByte((byte) a, fnum);
    }
    
    /**
     * 写入一个byte数组
     * @param b 数组
     * @param fnum 文件号
     * @return 写入是否成功
     */
    public boolean writeBytes(byte[] b, int fnum) {
        try {
            files[fnum].write(b);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 写入一个逗号
     * @param fnum 文件号
     * @return 写入是否成功
     */
    public boolean writeComma(int fnum) {
        return writeByte((byte) ',', fnum);
    }
    
    /**
     * 写入一个EOF (0xff)
     * @param fnum 文件号
     * @return 是否写入成功
     */
    public boolean writeEOF(int fnum) {
        return writeByte((byte) 0xff, fnum);
    }
    
    /**
     * 写入一个字符串，自动用双引号括起
     * @param s 字符串
     * @param fnum 文件号
     * @return 是否写入成功
     */
    public boolean writeQuotedString(String s, int fnum) {
        try {
            files[fnum].write(("\"" + s + "\"").getBytes());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean writeQuotedS(ByteString s, int fnum) {
        try {
            files[fnum].write((byte) '"');
            files[fnum].write(s.getBytes());
            files[fnum].write((byte) '"');
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 写入一个字符串，没有双引号
     * @param s 字符串
     * @param fnum 文件号
     * @return 是否写入成功
     */
    public boolean writeS(ByteString s, int fnum) {
        try {
            files[fnum].write(s.getBytes());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 读取一段以逗号或0xff分隔开的文件内容，并跳过分隔符
     * @param fnum 文件号
     * @return 获取到的内容，以字符串表示
     * @throws Exception 读取错误
     */
    private String getContent(int fnum) throws Exception {
        GFile f = files[fnum];
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int c;
        try {
            while ((c = f.read()) != ',' && (c & 0xff) != 0xff)
                bout.write(c);
        } catch (IOException e) { //到文件末尾
        }
        return bout.toString();
    }
    
    /**
     * 获取文件长度
     * @param fnum 文件号
     * @return 文件长度。若获取失败则返回null
     */
    public Integer getLength(int fnum) {
        try {
            return files[fnum].length();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 文件指针是否到达文件末尾
     */
    public boolean eof(int fnum) {
        try {
            return files[fnum].position() >= files[fnum].length();
        } catch (Exception e) {
            return true;
        }
    }

    public static void main(String[] args) throws Exception {
        FileManager fm = new FileManager(3);
        //System.out.println(fm.open("LZ.DAT",0,FileManager.INPUT));
        System.out.println(fm.open("1.txt",1,FileManager.RANDOM));
        //System.out.println(fm.getString(0));
        //System.out.println(fm.getString(0));
        System.out.println(fm.seek(12,1));
        System.out.println(fm.eof(1));
        //System.out.println(fm.getLength(1));
        //System.out.println(fm.readByte(1));
        //System.out.println(fm.readReal(1));
//        String s;
//        while ((s = fm.getString(0)) != null) {
//            fm.writeString(s, 1);
//            fm.writeByte(',', 1);
//        }
        fm.closeAll();
    }

}

class FileAttr {
    /**
     * 初始化文件属性
     * @param des 属性描述
     * @param a 可读
     * @param b 可写
     * @param c 可定位
     */
    FileAttr(String des, boolean a, boolean b, boolean c) {
        description = des;
        canRead = a;
        canWrite = b;
        canPosition = c;
    }
    final boolean canRead, canWrite, canPosition;
    final String description;
}