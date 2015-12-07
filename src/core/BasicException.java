package core;

/**
 * gvbΩ‚ Õ∆˜“Ï≥£
 * @author Amlo
 *
 */

public class BasicException extends Exception implements E {
    String des;

    public BasicException(String s) {
        des = s;
    }
    
    public BasicException(int type) {
        switch (type) {
        case SYNTAX:
            des = "Syntax";
            break;
        case STMT_DUPLICATE:
            des = "Statement duplicate";
            break;
        case REDIM_ARRAY:
            des = "Redimed array";
            break;
        case TYPE_MISMATCH:
            des = "Type mismatch";
            break;
        case ILLEGAL_QUANTITY:
            des = "Illegal quantity";
            break;
        case DIVISION_BY_ZERO:
            des = "Division by zero";
            break;
        case BAD_SUBSCRIPT:
            des = "Bad subscript";
            break;
        case NEXT_WITHOUT_FOR:
            des = "Next without for";
            break;
        case RETURN_WITHOUT_GOSUB:
            des = "Return without gosub";
            break;
        case OUT_OF_DATA:
            des = "Out of data";
            break;
        case UNDEFD_STMT:
            des = "Undefined statement";
            break;
        case UNDEFD_FUNC:
            des = "Undefined function";
            break;
        case WEND_WITHOUT_WHILE:
            des = "Wend without while";
            break;
        case FILE_NUMBER:
            des = "File number";
            break;
        case FILE_REOPEN:
            des = "File reopen";
            break;
        case FILE_CLOSE:
            des = "File close";
            break;
        case FILE_OPEN:
            des = "File open";
            break;
        case FILE_MODE:
            des = "File mode";
            break;
        case FILE_WRITE:
            des = "File write";
            break;
        case FILE_READ:
            des = "File read";
            break;
        case RECORD_NUMBER:
            des = "Record number";
            break;
        case FILE_LENGTH_READ:
            des = "File length read";
            break;
        case NOT_ASK_CACHE:
            des = "Not ask cache";
            break;
        case FILE_SEEK:
            des = "Seek file";
            break;
        case ASK_CACHE:
            des = "Ask cache";
            break;
        case STMT_ORDER:
            des = "Statment order";
            break;
        default:
            des = "Unknown";
        }
    }
    
    public String toString() {
        return des + " error";
    }
}
