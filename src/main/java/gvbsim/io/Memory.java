package gvbsim.io;

public interface Memory {
    /**
     * 获取指定地址的一个字节
     */
    public byte peek(int addr);
    /**
     * 获取指定地址的size个字节
     */
    public byte[] peek(int addr, int size);
    /**
     * 修改内存
     */
    public void poke(int addr, byte val);
    /**
     * 修改内存
     */
    public void poke(int addr, byte[] val);
    /**
     * 获取本体ram
     */
    public byte[] getRAM();
    /**
     * 调用指定地址的机器码
     * @return 调用是否成功
     */
    public boolean call(int addr);
}
