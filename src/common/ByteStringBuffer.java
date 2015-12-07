package common;

import java.util.Arrays;

/**
 * 使用byte数组组建字符串
 */

public class ByteStringBuffer {
    byte[] b;
    int pos;
    
    static final int cap = 32;
    
    /**
     * 无参构造函数，默认byte数组大小32字节
     */
    public ByteStringBuffer() {
        this(cap);
    }
    
    /**
     * 自定义默认byte数组大小
     * @param capacity 数组大小
     */
    public ByteStringBuffer(int capacity) {
        b = new byte[capacity];
    }
    
    /**
     * 使用一个byte数组初始化
     * @param str 用于初始化的byte数组
     */
    public ByteStringBuffer(byte[] str) {
        b = Arrays.copyOf(str, cap);
    }
    
    /**
     * 使用一个字符串初始化
     * @param str 用于初始化的字符串
     */
    public ByteStringBuffer(String str) {
        this(str.getBytes());
    }
    
    /**
     * 清空数组
     */
    public ByteStringBuffer clear() {
        pos = 0;
        b = new byte[cap];
        return this;
    }
    
    /**
     * 将一字节追加到数组尾部
     * @param by 字节
     * @return this
     */
    public ByteStringBuffer append(byte by) {
        checkCapacity(1);
        b[pos++] = by;
        return this;
    }
    
    /**
     * 将1字节追加到数组尾部
     * @param c 会转化为byte
     * @return this
     */
    public ByteStringBuffer append(int c) {
        return append((byte) c);
    }
    
    /**
     * 将字节数组追加到数组尾部
     * @param by 字节数组
     * @return this
     */
    public ByteStringBuffer append(byte[] by) {
        checkCapacity(by.length);
        for (int i = 0; i < by.length; i++)
            b[pos++] = by[i];
        return this;
    }
    
    /**
     * 将字符串追加到数组尾部
     * @param s 字符串
     * @return this
     */
    public ByteStringBuffer append(String s) {
        return append(s.getBytes());
    }
    
    /**
     * 将一个object的字符串形式追加到数组尾部
     * @param o object
     * @return this
     */
    public ByteStringBuffer append(Object o) {
        return append(String.valueOf(o));
    }
    
    void checkCapacity(int l) {
        while (pos + l > b.length)
            b = Arrays.copyOf(b, b.length << 1);
    }
    
    /**
     * 获取当前数组大小
     * @return 数组大小
     */
    public int capacity() {
        return b.length;
    }
    
    /**
     * 得到字符串
     */
    public String toString() {
        return new String(b, 0, pos);
    }
    
    public S toS() {
        return new S(b, 0, pos);
    }
    
    /**
     * 获取指定下标的byte
     * @param index 下标
     * @return byte
     */
    public byte byteAt(int index) {
        return b[index];
    }
}
