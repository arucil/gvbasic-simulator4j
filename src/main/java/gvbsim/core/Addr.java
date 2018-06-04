package gvbsim.core;

/**
 * 用于词法分析的地址类，包含字节流偏移（地址）和当前行
 * @author Amlo
 *
 */

public class Addr {
    public int addr, line;

    public Addr(int addr, int line) {
        this.addr = addr;
        this.line = line;
    }

    @Override
    public String toString() {
        return addr + " (" + line + ")";
    }
    
    public boolean equals(Object o) {
        return o instanceof Addr && ((Addr) o).addr == addr;
    }
}
