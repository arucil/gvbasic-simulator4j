package gvbsim.core;

import java.util.*;
/**
 * 数组
 * @param <T> 数组类型
 */
public class Array<T> {
    /**
     * 各维的权值
     */
    public int[] base;
    /**
     * 各维的最大下标
     */
    public Integer[] bound;
    /**
     * 展开后的数组元素
     */
    public T[] value;
    
    /**
     * 显示：数组最大下标，权值，元素
     */
    public String toString() {
        return "array" + Arrays.toString(bound) + ": (" + Arrays.toString(base)
                + "val=" + (value.length > 20 ? "..." : Arrays.toString(value)) + ")";
    }
}
