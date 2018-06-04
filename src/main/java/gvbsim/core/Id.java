package gvbsim.core;
/**
 * 变量
 * @author Amlo
 *
 */
public class Id {
    public static final int REAL = 0, INTEGER = 1, STRING = 2, ARRAY = 3;
    
    public final String id;
    public final int type;
    
    public Id(String id, int type) {
        this.id = id;
        this.type = type;
    }
    
    public int hashCode() {
        return id.hashCode() + type * 166303;
    }
    
    /**
     * 还是要比较类型
     */
    public boolean equals(Object o) {
        if (!(o instanceof Id))
            return false;
        Id i = (Id) o;
        return id.equals(i.id) && i.type == type;
    }
    
    public String toString() {
        return id;
    }
}
