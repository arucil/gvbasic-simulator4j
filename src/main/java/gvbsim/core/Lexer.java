package gvbsim.core;

import java.util.*;
import java.io.*;
import static gvbsim.common.Utilities.*;
import gvbsim.common.*;

/**
 * basic词法分析器
 */

public class Lexer {
    byte[] z, z_bak;
    int p, line, p_bak, line_bak, c_bak;
    
    Map<String, Integer> keyword;
    
    int tok;
    int ival;
    double rval;
    String sval;
    ByteString bsval;
    
    public Lexer(InputStream in) throws IOException {
            try {
                z = new byte[in.available()];
                in.read(z);
            } finally {
                in.close();
            }
        keyword = C.keywords;
        line = 1;
    }
    
    /**
     * 获取当前地址
     * @return 地址
     */
    public Addr getAddr() {
        //注意，c中可能存有向前看的字符，此时要将偏移减1
        return new Addr(p - (c == ' ' ? 0 : 1), line);
    }
    
    /**
     * 跳转到指定地址
     * @param a 地址
     */
    public void resumeAddr(Addr a) {
        p = a.addr;
        line = a.line;
        c = ' ';
    }
    
    /**
     * 重置地址
     */
    public void reset() {
        p = 0;
        line = 1;
        c = ' ';
    }
    
    /**
     * 备份当前字节流和地址
     */
    public void backup() {
        z_bak = z;
        p_bak = p;
        line_bak = line;
        c_bak = c;
    }
    
    /**
     * 恢复备份的字节流和地址
     */
    public void restore() {
        if (z_bak != null) {
            z = z_bak;
            p = p_bak;
            line = line_bak;
            c = c_bak;
            z_bak = null;
        }
    }
    
    /**
     * 设置新的字节流，地址初始化为0。不会自动备份
     * @param newb 新字节流
     */
    public void setByteStream(byte[] newb) {
        z = newb;
        p = 0;
    }
    
    /**
     * 读取一个字节
     */
    public int getc() {
        try {
            return z[p++] & 0xff;
        } catch (ArrayIndexOutOfBoundsException e) {
            return -1;
        }
    }
    
    /**
     * 读取一个字节，会保存到向前看
     */
    public int peek() {
        return c = getc();
    }
    
    boolean getc(int ch) {
        peek();
        if (c != ch)
            return false;
        peek();
        return true;
    }
    
    int c = ' ';
    /**
     * 获取下一个词法单元
     * @return token。若返回INTEGER，则ival和rval都有值
     */
    public int getToken() throws BasicException {
        while (isControl(c)) {
            if (c == 0xa) {
                line++;
                break;
            }
            peek();
        }
        switch (c) {
        case 0xa:
            peek();
            return tok = 10;
        case '<':
            if (getc('>'))
                return tok = C.NEQ;
            else if (c == '=') {
                peek();
                return tok = C.LTE;
            }
            return tok = '<';
        case '>':
            if (getc('='))
                return tok = C.GTE;
            return tok = '>';
        case '"': {
            ByteArrayOutputStream bout = new ByteArrayOutputStream(255);
            while (peek() != '"' && c != 0xd && c != 0xa && c != -1) {
                bout.write(c);
            }
            if (c == '"')
                peek();
            bsval = new ByteString(bout.toByteArray());
            return tok = C.STRING;
        }
        default:
            try {
                if (c >= '0' && c <= '9' || c == '.') {
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    while (c >= '0' && c <= '9') {
                        bout.write(c);
                        peek();
                    }
                    if (c != '.' && c != 'E' && c != 'e') {
                        rval = ival = Integer.parseInt(bout.toString());
                        return tok = C.INTEGER;
                    }
                    if (c == '.')
                        do {
                            bout.write(c);
                        } while (peek() >= '0' && c <= '9');
                    if (c == 'E' || c == 'e') {
                        bout.write('E');
                        peek();
                        if (c == '+' || c == '-') {
                            bout.write(c);
                            peek();
                        }
                        while (c >= '0' && c <= '9') {
                            bout.write(c);
                            peek();
                        }
                    }
                    rval = Double.parseDouble(bout.toString());
                    return tok = C.REAL;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                throw new BasicException(E.SYNTAX);
            }

            if (isAlpha(c)) {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                do {
                    bout.write(toLowerCase(c));
                } while (isWord(peek()));
                if (c == '%' || c == '$') {
                    bout.write((byte) c);
                    peek();
                }
                sval = new String(bout.toByteArray());
                Integer i = keyword.get(sval);
                if (i != null)
                    return tok = i;
                return tok = C.ID;
            }
            tok = c;
            peek();
            return tok;
        }
    }
    
    /**
     * 调到下一行，会保留0xa或-1
     */
    public void skipToNewLine() {
        while (c != 10 && c != -1)
            peek();
    }
    
    public String toString() {
        switch (tok) {
        case C.ID:
            return line + ": <ID, " + sval + ">";
        case C.STRING:
            return line + ": <String, \"" + bsval + "\">";
        case C.REAL:
            return line + ": <Real, " + rval + ">";
        case C.INTEGER:
            return line + ": <Integer, " + ival + ">";
        case C.GTE:
            return line + ": < >= >";
        case C.LTE:
            return line + ": < <= >";
        case C.NEQ:
            return line + ": < <> >";
        default:
            if (tok > 31 && tok < 127)
                return line + ": <" + ((char) tok) + ">";
            return line + ": <" + tok + ">";
        }
    }
    
    public static void main(String[] args) throws Exception {
        InputStream i = new BufferedInputStream(new FileInputStream("bas/1.txt"));
        Lexer l = new Lexer(i);
        System.setOut(new PrintStream(new File("s.txt")));
        int k = 0;
        do {
            k = l.getToken();
            System.out.println(l.toString());
        } while (k != -1);
        
    }

}
