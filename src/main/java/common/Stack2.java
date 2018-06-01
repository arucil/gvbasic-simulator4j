package common;

/**
 * 功能简单的栈
 * @author Amlo
 *
 * @param <E> 栈元素类型
 */

public class Stack2<E> {
    class Node {
        E e;
        Node next;
        Node(E element, Node next) {
            e = element;
            this.next = next;
        }
    }
    
    Node top;
    
    public void push(E e) {
        top = new Node(e, top);
    }
    
    /**
     * 弹出元素，若栈空，则返回null
     * @return 栈顶元素
     */
    public E pop() {
        try {
            E e = top.e;
            top = top.next;
            return e;
        } catch (NullPointerException e) {
            return null;
        }
    }
    
    public boolean empty() {
        return top == null;
    }
    
    public void clear() {
        top = null;
    }
    
    /**
     * 查看栈顶元素，若栈空，则返回null
     * @return 栈顶元素
     */
    public E peek() {
        try {
            return top.e;
        } catch (NullPointerException e) {
            return null;
        }
    }
    
    public String toString() {
        Node n = top;
        StringBuffer sb = new StringBuffer("[");
        while (n != null) {
            sb.append(n.e);
            if (n.next != null)
                sb.append(" -> ");
            n = n.next;
        }
        return sb.append("]").toString();
    }
    
    Node xtop;
    public void xreset() {
        xtop = top;
    }
    public E xpop() {
        try {
            E e = xtop.e;
            xtop = xtop.next;
            return e;
        } catch (NullPointerException e) {
            return null;
        }
    }
    public E xpeek() {
        try {
            return xtop.e;
        } catch (NullPointerException e) {
            return null;
        }
    }
}
