package common;

import java.util.*;

/**
 * byte数组实现的字符串
 */

public final class S implements Comparable<S> {
    private final byte[] b;
    private int hash;
    
    public S() {
        b = new byte[0];
        calcHash();
    }

    public S(String s) {
        b = s.getBytes();
        calcHash();
    }
    
    public S(byte[] bytes) {
        b = Arrays.copyOf(bytes, bytes.length);
        calcHash();
    }
    
    public S(byte[] bytes, int begin, int end) {
        b = Arrays.copyOfRange(bytes, begin, end);
        calcHash();
    }
    
    /**
     * 创建一个1字节的字符串
     */
    public S(byte c) {
        b = new byte[] {c};
        calcHash();
    }
    
    private void calcHash() {
        hash = 1;
        if (b.length > 0) {
            for (int i = 0; i < b.length; i++) {
                hash = hash * 37 + b[i];
            }
        }
    }
    
    public int length() {
        return b.length;
    }
    
    public byte byteAt(int index) {
        return b[index];
    }
    
    public int charAt(int index) {
        return b[index] & 0xff;
    }
    
    /**
     * 字符串的后缀字符，若字符串长度为0则返回-1
     */
    public int suffix() {
        return b.length > 0 ? b[b.length - 1] & 0xff : -1;
    }
    
    public byte[] getBytes() {
        return Arrays.copyOf(b, b.length);
    }
    
    public S concat(S s) {
        return concat(this, s);
    }
    
    public static S concat(S s1, S s2) {
        byte[] b = s1.b, c = s2.b;
        int l1 = b.length, l2 = c.length;
        byte[] r = Arrays.copyOf(b, l1 + l2);
        
        for (int i = 0; i < l2; i++) {
            r[i + l1] = c[i];
        }
        return new S(r);
    }
    
    public S substring(int begin) {
        return substring(begin, b.length);
    }
    
    public S substring(int begin, int end) {
        return new S(b, begin, end);
    }
    
    public S toLowerCase() {
        int l = b.length;
        byte[] c = new byte[l];
        byte d;
        
        for (int i = 0; i < l; i++) {
            d = b[i];
            c[i] = (byte) (d >= 'A' && d <= 'Z' ? d | 0x20 : d);
        }
        return new S(c);
    }
    
    public S toUpperCase() {
        int l = b.length;
        byte[] c = new byte[l];
        byte d;
        
        for (int i = 0; i < l; i++) {
            d = b[i];
            c[i] = (byte) (d >= 'a' && d <= 'z' ? d & 0xdf : d);
        }
        return new S(c);
    }
    
    public boolean contains(byte c) {
        for (int i = 0; i < b.length; i++) {
            if (b[i] == c)
                return true;
        }
        return false;
    }
    
    @Override
    public int compareTo(S o) {
        return compare(this, o);
    }
    
    public static int compare(S s1, S s2) {
        byte[] b = s1.b, c = s2.b;
        int l1 = b.length, l2 = c.length, min = l1 > l2 ? l2 : l1;
        
        for (int i = 0; i < min; i++) {
            if (b[i] != c[i])
                return b[i] - c[i];
        }
        return l1 - l2;
    }
    
    public String toString() {
        return new String(b);
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof S))
            return false;
        S s = (S) o;
        byte[] c = s.b;
        int l1 = b.length, l2 = c.length, min = l1 > l2 ? l2 : l1;
        
        for (int i = 0; i < min; i++) {
            if (b[i] != c[i])
                return false;
        }
        return l1 == l2;
    }
    
    public int hashCode() {
        return hash;
    }
    
    public static void main(String[] args) {
    }
}
