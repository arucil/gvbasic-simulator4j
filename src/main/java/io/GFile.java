package io;

import java.io.*;
import java.util.*;

/**
 * 模拟wqx文件的基本类，仅有读取写入字节的功能
 */

public class GFile {
    private static final int BUFFER_SIZE = 1024;
    
    private byte[] buf;
    private int pos, capacity;
    
    private boolean canRead, canWrite, canPosition;
    private final File file;
    
    public GFile(String filename, boolean readable, boolean writable, boolean positionable)
            throws FileNotFoundException, IOException {
        this(new File(filename), readable, writable, positionable);
    }
    
    public GFile(File file, boolean readable, boolean writable, boolean positionable)
            throws FileNotFoundException, IOException {
        this.file = file;
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(file));
            buf = new byte[capacity = in.available()];
            in.read(buf);
            in.close();
        } catch (FileNotFoundException e) {
            if (writable) { //新建文件
                new FileOutputStream(file).close();
                buf = new byte[BUFFER_SIZE];
            }
        } catch (IOException e) {
            if (readable)
                throw e;
            else
                buf = new byte[BUFFER_SIZE];
        }
        
        canRead = readable;
        canWrite = writable;
        if (readable && !file.canWrite())
            throw new IOException("can't write file");
        canPosition = positionable;
    }
    
    /**
     * 读取一字节
     * @return 读取的字节
     * @throws IOException 文件不可读或没有内容可供读取
     */
    public byte read() throws IOException {
        if (!canRead)
            throw new IOException("can't read");
        try {
            return buf[pos++];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("out of file");
        }
    }
    
    /**
     * 读取一串byte，以数组形式返回
     * @param size 要读取的字节数
     * @return byte数组
     * @throws IOException 文件不可读或没有足够的内容供读取
     */
    public byte[] read(int size) throws IOException {
        if (!canRead)
            throw new IOException("can't read");
        try {
            if (pos + size > buf.length)
                throw new IOException("out of file");
            return Arrays.copyOfRange(buf, pos, pos += size);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("out of file");
        }
    }
    
    /**
     * 写入一字节。实际上写入的是内存
     * @param b 要写入的字节
     * @throws IOException 文件不可写
     */
    public void write(byte b) throws IOException {
        if (!canWrite)
            throw new IOException("can't write");
        ensureCapacity(1);
        buf[pos++] = b;
    }
    
    /**
     * 写入一串字节
     * @param b 一串字节
     * @throws IOException 文件不可读
     */
    public void write(byte[] b) throws IOException {
        if (!canWrite)
            throw new IOException("can't write");
        ensureCapacity(b.length);
        for (byte c : b)
            buf[pos++] = c;
    }
    
    /**
     * 获取文件指针位置
     * @return 文件指针
     */
    public int position() {
        return pos;
    }
    
    /**
     * 定位文件指针
     * @param p 文件指针
     * @throws IOException 指针超出文件大小
     */
    public void position(int p) throws IOException {
        if (!canPosition || p > capacity)
            throw new IOException("can't position file pointer");
        pos = p;
    }
    
    /**
     * 返回文件大小
     * @return 文件大小
     */
    public int length() {
        return capacity;
    }
    
    /**
     * 关闭文件，如果文件可写入，则更新文件内容
     * @throws IOException 无法写入文件
     */
    public void close() throws IOException {
        if (canWrite) {
            OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            out.write(buf, 0, capacity);
            out.close();
        }
        canRead = canWrite = canPosition = false;
    }
    
    /**
     * 清空文件内容
     */
    public void clear() {
        capacity = pos = 0;
    }
    
    /**
     * 重置文件指针
     */
    public void rewind() {
        pos = 0;
    }
    
    /**
     * 文件是否可读
     */
    public boolean canRead() {
        return canRead;
    }
    
    /**
     * 文件是否可写
     */
    public boolean canWrite() {
        return canWrite;
    }
    
    /**
     * 文件是否可定位
     */
    public boolean canPosition() {
        return canPosition;
    }

    /**
     * 确保供写入的缓冲区足够大
     * @param i 要写入多少字节
     */
    private void ensureCapacity(int i) {
        while (buf.length < pos + i) {
            buf = Arrays.copyOf(buf, buf.length + 1024);
        }
        if (pos + i > capacity)
            capacity = pos + i;
    }

    public static void main(String[] args) {
        byte[] a=new byte[] {0,1,2};
        System.out.println(Arrays.toString(Arrays.copyOfRange(a,0,1)));
    }

}
