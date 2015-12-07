package core;

import java.util.*;

/**
 * gvb解释器使用的常数。名字起短点
 * @author Amlo
 *
 */

public abstract class C {
    public static final int DIM = 256,
            LET = 257,
            SWAP = 258,
            GOTO = 259,
            IF = 260,
            THEN = 261,
            ELSE = 262,
            ON = 263,
            FOR = 264,
            TO = 265,
            STEP = 266,
            NEXT = 267,
            WHILE = 268,
            WEND = 269,
            DEF = 270,//
            FN = 271,//
            GOSUB = 272,
            RETURN = 273,
            AND = 275,
            OR = 276,
            NOT = 277,
            READ = 278,
            DATA = 279,
            RESTORE = 280,
            INPUT = 281,
            PRINT = 282,
            LOCATE = 283,
            INVERSE = 284,
            INKEY = 285,
            PLAY = 286,
            BEEP = 287,
            GRAPH = 288,
            TEXT = 289,
            DRAW = 290,
            LINE = 291,
            BOX = 292,
            CIRCLE = 293,
            ELLIPSE = 294,
            OPEN = 295,
            CLOSE = 296,
            PUT = 298,//
            GET = 299,//
            RSET = 300,
            LSET = 301,
            CONT = 304,
            POP = 305,
            REM = 306,
            CLEAR = 307,
            WRITE = 308,
            OUTPUT = 309,
            RANDOM = 310,//
            AS = 311,//
            POKE = 313,
            CALL = 314,
            CLS = 315,
            FIELD = 316,//
            ID = 317,
            STRING = 318,
            REAL = 319,
            INTEGER = 320,
            GTE = 321,
            LTE = 322,
            END = 323,
            NEQ = 324,
            APPEND = 325,
            //expanded
            TEXTOUT = 326,
            SLEEP = 327,
            PAINT = 328,
            BINARY = 329,
            FREAD = 330,
            FWRITE = 331,
            FGET = 332,
            FSEEK = 333,
            FPUT = 334,
            LOAD = 335,
            FPUTC = 336;
    
    public static Map<String, Integer> keywords = new HashMap<>();
    static {
        keywords.put("dim", DIM);keywords.put("let", LET);keywords.put("swap", SWAP);keywords.put("goto", GOTO);keywords.put("if", IF);
        keywords.put("then", THEN);keywords.put("else", ELSE);keywords.put("on", ON);keywords.put("for", FOR);keywords.put("to", TO);
        keywords.put("step", STEP);keywords.put("next", NEXT);keywords.put("while", WHILE);keywords.put("wend", WEND);keywords.put("def", DEF);
        keywords.put("fn", FN);keywords.put("gosub", GOSUB);keywords.put("return", RETURN);keywords.put("and", AND);
        keywords.put("or", OR);keywords.put("not", NOT);keywords.put("read", READ);keywords.put("data", DATA);keywords.put("restore", RESTORE);
        keywords.put("input", INPUT);keywords.put("print", PRINT);keywords.put("locate", LOCATE);keywords.put("inverse", INVERSE);keywords.put("inkey$", INKEY);
        keywords.put("play", PLAY);keywords.put("beep", BEEP);keywords.put("graph", GRAPH);keywords.put("text", TEXT);keywords.put("draw", DRAW);
        keywords.put("line", LINE);keywords.put("box", BOX);keywords.put("circle", CIRCLE);keywords.put("ellipse", ELLIPSE);keywords.put("open", OPEN);
        keywords.put("close", CLOSE);keywords.put("lset", LSET);keywords.put("rset", RSET);keywords.put("cont", CONT);keywords.put("pop", POP);
        keywords.put("rem", REM);keywords.put("clear", CLEAR);keywords.put("write", WRITE);keywords.put("output", OUTPUT);keywords.put("random", RANDOM);
        keywords.put("as", AS);keywords.put("poke", POKE);keywords.put("call", CALL);keywords.put("cls", CLS);keywords.put("field", FIELD);
        keywords.put("end", END);keywords.put("append", APPEND);keywords.put("textout", TEXTOUT);keywords.put("sleep", SLEEP);
        keywords.put("paint", PAINT);keywords.put("binary", BINARY);keywords.put("get", GET);keywords.put("put", PUT);
        keywords.put("fread", FREAD);keywords.put("fwrite", FWRITE);keywords.put("fseek", FSEEK);keywords.put("fget", FGET);
        keywords.put("fput", FPUT);keywords.put("load", LOAD);keywords.put("fputc", FPUTC);
    }
    
    public static final String ABS = "abs",
            ATN = "atn",
            COS = "cos",
            EXP = "exp",
            INT = "int",
            LOG = "log",
            RND = "rnd",
            SGN = "sgn",
            SIN = "sin",
            SQR = "sqr",
            TAN = "tan",
            ASC = "asc",
            CHR = "chr$",
            LEFT = "left$",
            LEN = "len",
            MID = "mid$",
            RIGHT = "right$",
            MKS = "mks$",
            STR = "str$",
            VAL = "val",
            POS = "pos",
            CVI = "cvi$",
            CVS = "cvs$",
            MKI = "mki$",
            TAB = "tab",
            SPC = "spc",
            PEEK = "peek",
            LOF = "lof",
            EOF = "eof",
            //expanded
            POINT = "point",
            CHECKKEY = "checkkey",
            FTELL = "ftell",
            FGETC = "fgetc";
    
    public static Set<String> funs = new HashSet<>();
    static {
        funs.add(ATN);funs.add(COS);funs.add(EXP);funs.add(INT);funs.add(LOG);
        funs.add(RND);funs.add(SGN);funs.add(SIN);funs.add(SQR);funs.add(TAN);
        funs.add(ASC);funs.add(CHR);funs.add(LEFT);funs.add(LEN);funs.add(MID);
        funs.add(RIGHT);funs.add(MKS);funs.add(STR);funs.add(VAL);funs.add(POS);
        funs.add(CVI);funs.add(CVS);funs.add(MKI);funs.add(ABS);funs.add(PEEK);
        funs.add(POINT);funs.add(LOF);funs.add(EOF);funs.add(CHECKKEY);
        funs.add(FTELL);funs.add(FGETC);
    }
}
