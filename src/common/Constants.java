package common;

/**
 * 通用常数
 * @author Amlo
 *
 */

public interface Constants {
    int W = 160, H = 80;
    int BYTE_W = W >>> 3;
    int PIXEL_SIZE = 2;
    
    int[] pmask = new int[] {0x80, 0x40, 0x20, 0x10, 8, 4, 2, 1};
}
