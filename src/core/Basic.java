package core;

import gui.*;
import io.*;
import common.*;
import static common.Utilities.*;

import java.io.*;
import java.util.*;

/**
 * gvb解释器核心代码
 */

public class Basic {
    GraphicalScreen scr;
    Form frm;
    Lexer l;
    
    Stack2<For> fors = new Stack2<>();
    Stack2<While> whiles = new Stack2<>();
    Map<String, Fn> funs = new HashMap<>();
    
    /**
     * 设为public用于frm监视变量，需要synchronized
     */
    public Map<Id, Object> vars = new HashMap<>();
    
    /**
     * 行号。地址是一行开头（包括行号）。不需要记录行号
     */
    Map<Integer, Addr> stmts = new HashMap<>();
    
    /**
     * gosub用子程序栈
     */
    Stack2<Pack> subs = new Stack2<>();
    
    /**
     * 运行延迟
     */
    int delay;
    
    /**
     * 内置函数名称集合
     */
    Set<String> infuns;
    
    DataReader dr = new DataReader();
    
    Memory ram;

    FileState[] files = new FileState[3];
    FileManager fm = new FileManager(3);
    
    public Basic(GraphicalScreen screen, Memory m, Form f) {
        scr = screen;
        frm = f;
        delay = getDelay();
        infuns = C.funs;
        ram = m;
        for (int i = 0; i < files.length; i++)
            files[i] = new FileState();
        fm.chDir("dat");
    }
    
    /**
     * 重置解释器。将所有数据清空，不会影响lexer
     */
    public void reset() {
        dr.clear();
        fors.clear();
        whiles.clear();
        funs.clear();
        vars.clear();
        stmts.clear();
        subs.clear();
        fm.closeAll();
        fns.clear();
        fnvar.clear();
        for (FileState fs : files)
            fs.close();
        pb = ram.getRAM();
        
        pb[191] = pb[192] = pb[193] = pb[194] = pb[195] =
                pb[196] = pb[197] = pb[198] = -1;
        pb[199] = 13;
    }
    
    /**
     * 载入basic源程序
     * @param in 输入流
     * @return 是否载入成功
     */
    public boolean load(InputStream in) {
        try {
            //reset();
            l = new Lexer(in);

            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * 执行程序
     * @return 程序返回的信息。若为null则正常结束，否则异常结束
     */
    public String run() {
        if (l != null) {
            try {
                reset();
                scanStmt();
                program();
            } catch (BasicException e) {
                e.printStackTrace(); //debug
                return e + " in " + stmt;
            } finally {
                System.out.println("current label:" + stmt);
                System.out.println(vars); //debug
                //System.out.println(fors);
                fm.closeAll();
            }
        } else
            return "Program hasn't been loaded!";
        return null;
    }
    
    /**
     * 暂停
     */
    public void pause() {
        paused = true;
    }
    
    /**
     * 继续执行
     */
    public void cont() {
        paused = false;
    }
    
    Boolean paused;
    
    /**
     * 预处理，扫描行号，记录地址
     */
    void scanStmt() throws BasicException {
        l.reset();
        Addr a = l.getAddr();
        peek();
        stmt = 0;
        
        while (tok != -1) {
            if (tok != C.INTEGER)
                error(E.SYNTAX);
            if (l.ival < stmt)
                error(E.STMT_ORDER);
            stmt = l.ival;
            if (stmts.containsKey(l.ival))
                error(E.STMT_DUPLICATE);
            if (stmt < 0)
                error(E.ILLEGAL_QUANTITY);
            stmts.put(l.ival, a);
            while (tok != 0xa && tok != -1) {
                if (tok == C.REM) {
                    l.skipToNewLine();
                    break;
                } else if (tok == C.DATA) { //读取数据
                    int c = l.peek();
                    boolean quote = false;
                    dr.mark(stmt);
                    while (c != 0xd && c != ':' && c != 0xa && c != -1) {
                        if (c == '"')
                            quote = !quote;
                        dr.append(c);
                        c = l.peek();
                        if (c == ':' && quote) {
                            dr.append(':');
                            c = l.peek();
                        }
                    }
                    if (quote)
                        dr.append('"');
                    dr.addComma();
                    if (c != ':')
                        break;
                    peek();
                } else
                    peek();
            }
            if (tok != 0xa)
                peek(); //跳过换行符
            a = l.getAddr();
            peek();
        }
    }
    
    int tok;
    /**
     * 向前看
     * @throws BasicException
     */
    void peek() throws BasicException {
        tok = l.getToken();
    }
    
    /**
     * 匹配词法单元，若匹配则向前看，否则抛出syntax异常
     */
    void match(int t) throws BasicException {
        if (t != tok)
            error(E.SYNTAX);
        peek();
    }
    
    /**
     * 当前行号
     */
    int stmt;
    
    /**
     * 不为零则表示位于if内部
     */
    int ifs;
    
    /**
     * 是否是新的一行
     */
    boolean newline;
    
    /**
     * 执行的步数。用于延时
     */
    int count;
    
    /**
     * 执行,异常由run处理
     */
    void program() throws BasicException {
        paused = false;
        l.reset();
        ifs = 0;
        newline = true;
        count = 0;
        stmt = 0;
        scr.text();
        peek();
        
        while (tok != -1) {
            try {
                if (newline) {
                    if (tok != C.INTEGER)
                        error(E.SYNTAX);
                    stmt = l.ival;
                    peek();
                    ifs = 0;
                    newline = false;
                }
                switch (tok) {
                case C.GRAPH: //图形模式
                    scr.graph();
                    peek();
                    break;
                case C.TEXT: //文本模式
                    scr.text();
                    peek();
                    break;
                case C.CLS: //清屏
                    scr.cls();
                    peek();
                    break;
                case C.REM: //注释
                    l.skipToNewLine();
                    peek();
                    break;
                case C.DATA:
                    int c = l.peek();
                    boolean quote = false;
                    while (c != 0xd && c != ':' && c != 0xa && c != -1) {
                        if (c == '"')
                            quote = !quote;
                        c = l.peek();
                        if (c == ':' && quote)
                            c = l.peek();
                    }
                    peek();
                    break;
                case C.INKEY: //按键
                    frm.inkey();
                    peek();
                    break;
                case C.DIM: //定义数组
                    exe_dim();
                    break;
                case C.LET: //let赋值
                    peek();
                case C.ID: //赋值
                    exe_assign();
                    break;
                case C.PRINT: //显示
                    exe_print();
                    break;
                case C.END: //结束
                    peek();
                    throw new InterruptedException();
                case C.INPUT: //输入
                    exe_input();
                    break;
                case C.LOCATE: //定位
                    exe_locate();
                    break;
                case C.SWAP: //交换变量
                    exe_swap();
                    break;
                case C.GOTO: //跳转
                    exe_goto();
                    break;
                case C.IF:
                    exe_if();
                    break;
                case C.INTEGER: //if语句中的行号
                    if (ifs == 0)
                        error(E.SYNTAX);
                    jump();
                    break;
                case C.GOSUB:
                    exe_gosub();
                    break;
                case C.RETURN:
                    exe_return();
                    break;
                case C.POP: //弹栈
                    peek();
                    if (subs.empty())
                        error(E.RETURN_WITHOUT_GOSUB);
                    subs.pop();
                    break;
                case C.CLEAR:
                    peek();
                    dr.restore();
                    fors.clear();
                    whiles.clear();
                    funs.clear();
                    subs.clear();
                    fm.closeAll();
                    for (FileState fs : files)
                        fs.close();
                    synchronized (vars) {
                        vars.clear();
                        frm.vartable.revalidate();
                    }
                    break;
                case C.INVERSE: case C.CONT: case C.BEEP: //都是无效的
                    peek();
                    break;
                case C.PLAY: //无效
                    peek();
                    expr(E_STRING);
                    break;
                case C.DRAW:
                    exe_draw();
                    break;
                case C.BOX:
                    exe_box();
                    break;
                case C.LINE:
                    exe_line();
                    break;
                case C.CIRCLE:
                    exe_circle();
                    break;
                case C.ELLIPSE:
                    exe_ellipse();
                    break;
                case C.ON:
                    exe_on();
                    break;
                case C.READ:
                    exe_read();
                    break;
                case C.RESTORE:
                    peek();
                    if (tok == C.INTEGER) {
                        if (!stmts.containsKey(l.ival))
                            error(E.UNDEFD_STMT);
                        dr.restore(l.ival);
                        peek();
                    } else
                        dr.restore();
                    break;
                case C.POKE:
                    exe_poke();
                    break;
                case C.CALL:
                    exe_call();
                    break;
                case C.WHILE:
                    exe_while();
                    break;
                case C.WEND:
                    exe_wend();
                    break;
                case C.FOR:
                    exe_for();
                    break;
                case C.NEXT:
                    exe_next();
                    break;
                case C.TEXTOUT:
                    exe_textout();
                    break;
                case C.SLEEP:
                    exe_sleep();
                    break;
                case C.PAINT:
                    exe_paint();
                    break;
                case C.LSET:
                    exe_lset();
                    break;
                case C.RSET:
                    exe_rset();
                    break;
                case C.OPEN:
                    exe_open();
                    break;
                case C.CLOSE:
                    exe_close();
                    break;
                case C.WRITE:
                    exe_write();
                    break;
                case C.FIELD:
                    exe_field();
                    break;
                case C.PUT:
                    exe_put();
                    break;
                case C.GET:
                    exe_get();
                    break;
                case C.FSEEK:
                    exe_fseek();
                    break;
                case C.FGET:
                    exe_fget();
                    break;
                case C.FPUT:
                    exe_fput();
                    break;
                case C.FREAD:
                    exe_fread();
                    break;
                case C.FWRITE:
                    exe_fwrite();
                    break;
                case C.LOAD:
                    exe_load();
                    break;
                case C.DEF:
                    exe_def();
                    break;
                }
                //检查是否暂停
                if (++count == 200 || paused) {
                    count = 0;
                    do {
                        Thread.sleep(delay);
                    } while (paused);
                }

                switch (tok) {
                case C.ELSE:
                    if (ifs > 0) {
                        do {
                            peek();
                        } while (tok != 0xa);
                    } else
                        error(E.SYNTAX);
                case 0xa:
                    newline = true;
                    ifs = 0;
                case ':':
                    peek();
                case -1:
                    break;
                default:
                    System.out.println(l);//debug
                    error(E.SYNTAX);
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    }
    
    List<Integer> blist = new ArrayList<>();
    Integer[] uslsi = new Integer[0];
    /**
     * 定义数组（下标只能是整型常数），不能定义变量
     */
    void exe_dim() throws BasicException, InterruptedException {
        peek();
        String s;
        Array<?> ar;
        Id id;
        int i;
        
        do {
            blist.clear();
            s = l.sval;
            id = new Id(s, Id.ARRAY);
            match(C.ID);
            match('(');
            if (vars.containsKey(id))
                error(E.REDIM_ARRAY);
            switch (s.charAt(s.length() - 1)) {
            case '$':
                ar = new Array<S>();
                break;
            case '%':
                ar = new Array<Integer>();
                break;
            default:
                ar = new Array<Double>();
            }
            do {
                i = (int) (double) expr(E_NUMBER);
                blist.add(i);
                if (tok == ')')
                    break;
                match(',');
            } while (true);
            peek();
            ar.bound = blist.toArray(uslsi);
            ar.base = new int[ar.bound.length];
            ar.base[0] = 1;
            for (i = 1; i < ar.base.length; i++)
                ar.base[i] = ar.base[i - 1] * (ar.bound[i - 1] + 1);
            i--;
            switch (s.charAt(s.length() - 1)) {
            case '$':
                @SuppressWarnings("unchecked")
                Array<S> ar2 = (Array<S>) ar;
                ar2.value = new S[ar.base[i] * (ar.bound[i] + 1)];
                for (i = 0; i < ar.value.length; i++)
                    ar2.value[i] = new S();
                break;
            case '%':
                @SuppressWarnings("unchecked")
                Array<Integer> ar3 = (Array<Integer>) ar;
                ar3.value = new Integer[ar.base[i] * (ar.bound[i] + 1)];
                for (i = 0; i < ar.value.length; i++)
                    ar3.value[i] = 0;
                break;
            default:
                @SuppressWarnings("unchecked")
                Array<Double> ar4 = (Array<Double>) ar;
                ar4.value = new Double[ar.base[i] * (ar.bound[i] + 1)];
                for (i = 0; i < ar.value.length; i++)
                    ar4.value[i] = 0d;
            }
            synchronized (vars) {
                vars.put(id, ar);
                frm.vartable.revalidate();
            }
            if (tok != ',')
                break;
            peek();
        } while (true);
    }
    
    /**
     * 访问数组的接口
     */
    class ArrayAccess extends Access {
        public Array<?> arr;
        public int index;
        /**
         * 创建一个数组访问对象
         * @param id 数组的id
         * @param type 数组元素的类型
         * @param array 数组
         * @param index 元素所在下标（展开的）
         */
        public ArrayAccess(Id id, int type, Array<?> array, int index) {
            super(id, type);
            arr = array;
            this.index = index;
        }
        public Object get() {
            return arr.value[index];
        }
        @SuppressWarnings("unchecked")
        public void put(Object val) { //修改数组不影响变量列表，不用同步
            synchronized (arr) {
                switch (type) {
                case Id.STRING:
                    ((Array<S>) arr).value[index] = (S) val;
                    break;
                case Id.INTEGER:
                    ((Array<Integer>) arr).value[index] = (Integer) val;
                    break;
                default:
                    ((Array<Double>) arr).value[index] = (Double) val;
                }
            }
        }
        public String toString() {
            return id + "[" + index + "]";
        }
    }
    /**
     * 访问变量的接口
     */
    class IdAccess extends Access {
        public IdAccess(Id id) {
            super(id, id.type);
        }
        public Object get() {
            return vars.get(id);
        }
        public void put(Object val) {
            synchronized (vars) {
                vars.put(id, val);
                frm.vartable.revalidate();
            }
        }
        public String toString() {
            return id.toString();
        }
    }
    
    /**
     * 赋值
     */
    void exe_assign() throws BasicException, InterruptedException {
        Access a = getAccess();
        match('=');
        switch (a.type) {
        case Id.INTEGER:
            a.put((int) (double) expr(E_NUMBER));
            break;
        case Id.STRING:
            a.put((S) expr(E_STRING));
            break;
        default:
            a.put((Double) expr(E_NUMBER));
        }
    }
    
    boolean cr;
    Object ps;
    int pt;
    byte[] pb;
    /**
     * 显示
     */
    void exe_print() throws BasicException, InterruptedException {
        peek();
        while (tok != ':' && tok != 0xa && tok != -1 && tok != C.ELSE) {
            if (tok != ';' && tok != ',') {
                if (tok == C.ID && l.sval.equals(C.TAB) || l.sval.equals(C.SPC)) {
                    if (l.sval.equals(C.TAB)) {
                        peek();
                        match('(');
                        pt = (int) (double) expr(E_NUMBER);
                        match(')');
                        if (pt < 1 || pt > 20) //大字体
                            error(E.ILLEGAL_QUANTITY);
                        if (scr.getX() >= pt)
                            scr.print((S) null, true);
                        scr.locate(scr.getY(), pt - 1);
                        ps = null;
                    } else {
                        peek();
                        match('(');
                        pt = (int) (double) expr(E_NUMBER);
                        match(')');
                        if (pt < 0)
                            error(E.ILLEGAL_QUANTITY);
                        if (pt > 0) {
                            pb = new byte[pt];
                            Arrays.fill(pb, (byte) 32);
                            ps = new S(pb);
                        } else
                            ps = null;
                    }
                } else
                    ps = expr();
            } else
                ps = null;
            cr = tok == ',' || tok == ':' || tok == 0xa || tok == -1 || tok == C.ELSE;
            if (ps instanceof Double)
                scr.print(realToString((Double) ps), cr);
            else
                scr.print((S) ps, cr);
            if (tok == ';' || tok == ',')
                peek();
        }
        scr.print((S) null, false);
    }
    
    String iprm;
    Access iacc;
    Integer ishr;
    Double idbl;
    S ss, iinp;
    /**
     * 文件或屏幕输入
     * <br>input [#n / str$], id / id$ [, ...]
     */
    void exe_input() throws BasicException, InterruptedException {
        peek();
        if (tok == '#' || tok == C.INTEGER) { //文件读取
            pt = getFileNumber();
            if (files[pt].state != FileState.INPUT) //只有input模式才能读取
                error(E.FILE_MODE);
            match(',');
            while (true) {
                iacc = getAccess();
                switch (iacc.type) {
                case Id.INTEGER:
                    ishr = fm.readInteger(pt);
                    if (ishr == null)
                        error(E.FILE_READ);
                    iacc.put(ishr);
                    break;
                case Id.STRING:
                    ss = fm.readS(pt);
                    if (ss == null)
                        error(E.FILE_READ);
                    iacc.put(ss);
                    break;
                default:
                    idbl = fm.readReal(pt);
                    if (idbl == null)
                        error(E.FILE_READ);
                    iacc.put(idbl);
                } //自动跳过分隔符，不用读取
                if (tok == ',')
                    peek();
                else
                    break;
            }
        } else { //屏幕读取
            if (tok == C.STRING) {
                iprm = l.sval;
                peek();
                match(';');
            } else
                iprm = null;
            iacc = getAccess();
            while (true) {
                if (iprm != null) {
                    scr.print(iprm, false);
                    iprm = null;
                } else
                    scr.print("?", false);
                iinp = scr.input();
                switch (iacc.type) {
                case Id.INTEGER:
                    try {
                        iacc.put(Integer.parseInt(iinp.toString()));
                    } catch (NumberFormatException e) {
                        break;
                    }
                    break;
                case Id.STRING:
                    iacc.put(iinp);
                    break;
                default:
                    try {
                        iacc.put(Double.parseDouble(iinp.toString()));
                    } catch (NumberFormatException e) {
                        break;
                    }
                    break;
                }
                if (tok == ',') {
                    peek();
                    iacc = getAccess();
                } else
                    break;
            }
        }
    }
    
    /**
     * 光标定位，省略y则自动下一行
     * <br>locate y, x
     */
    void exe_locate() throws BasicException, InterruptedException {
        peek();
        int y;
        if (tok != ',')
            y = (int) (double) expr(E_NUMBER);
        else
            y = scr.getY() + 1;
        if (y < 1 || y > 5) //大字体
            error(E.ILLEGAL_QUANTITY);
        match(',');
        int x = (int) (double) expr(E_NUMBER);
        if (x < 1 || x > 20) //大字体
            error(E.ILLEGAL_QUANTITY);
        scr.locate(y - 1, x - 1);
    }
    
    /**
     * 交换变量
     */
    void exe_swap() throws BasicException, InterruptedException {
        peek();
        Access a2, a1 = getAccess();
        match(',');
        a2 = getAccess();
        if (a1.type != a2.type)
            error(E.TYPE_MISMATCH);
        Object obj = a1.get();
        a1.put(a2.get());
        a2.put(obj);
    }
    
    /**
     * 跳转
     */
    void exe_goto() throws BasicException {
        peek();
        jump();
    }
    
    /**
     * 读入一个行号，跳转
     */
    void jump() throws BasicException {
        int i = l.ival;
        match(C.INTEGER);
        Addr a = stmts.get(i);
        if (a == null)
            error(E.UNDEFD_STMT);
        l.resumeAddr(a);
        tok = 0xa; //跳转到一行开头，假装读入了换行符
    }

    /**
     * 条件语句
     */
    void exe_if() throws BasicException, InterruptedException {
        peek();
        if (doubleIsZero((Double) expr(E_NUMBER))) {
            if (tok != C.THEN && tok != C.GOTO)
                error(E.SYNTAX);
            if (tok == C.THEN) {
                peek();
                int nest = 0;
                while (true) {
                    if (tok == C.IF)
                        nest++;
                    else if (tok == C.ELSE) {
                        if (nest == 0)
                            break;
                        nest--;
                    } else if (tok == 0xa || tok == -1)
                        break;
                    peek();
                }
            } else {
                peek();
                match(C.INTEGER);
                if (tok == ':')
                    peek();
            }
            if (tok == C.ELSE) {
                ifs++;
                tok = ':';
            }
        } else {
            ifs++;
            if (tok == C.THEN) {
                tok = ':';
            } else if (tok == C.GOTO) {
                peek();
                jump();
            } else
                error(E.SYNTAX);
        }
    }
    
    /**
     * 跳转子程序
     */
    void exe_gosub() throws BasicException {
        peek();
        int i = l.ival;
        match(C.INTEGER);
        Addr a = stmts.get(i);
        if (a == null)
            error(E.UNDEFD_STMT);
        subs.push(getAddr());
        l.resumeAddr(a);
        tok = 0xa;
    }
    
    /**
     * 子程序返回
     */
    void exe_return() throws BasicException {
        peek();
        if (tok != 0xa && tok != ':' && tok != -1 && tok != C.ELSE)
            error(E.SYNTAX);
        if (subs.empty())
            error(E.RETURN_WITHOUT_GOSUB);
        resumeAddr(subs.pop(), true);
    }
    
    int x1, y1, x2, y2, fill, ptype;
    /**
     * 画点
     */
    void exe_draw() throws BasicException, InterruptedException {
        peek();
        x1 = (int) (double) expr(E_NUMBER);
        match(',');
        y1 = (int) (double) expr(E_NUMBER);
        if (tok == ',') {
            peek();
            ptype = (int) (double) expr(E_NUMBER);
        } else
            ptype = 1;
        ptype &= 3;
        if (ptype == 3)
            ptype = 2;
        scr.draw(x1, y1, ptype);
    }
    
    void exe_line() throws BasicException, InterruptedException {
        peek();
        x1 = (int) (double) expr(E_NUMBER);
        match(',');
        y1 = (int) (double) expr(E_NUMBER);
        match(',');
        x2 = (int) (double) expr(E_NUMBER);
        match(',');
        y2 = (int) (double) expr(E_NUMBER);
        if (tok == ',') {
            peek();
            ptype = (int) (double) expr(E_NUMBER);
        } else
            ptype = 1;
        ptype &= 3;
        if (ptype == 3)
            ptype = 2;
        scr.line(x1, y1, x2, y2, ptype);
    }
    
    void exe_box() throws BasicException, InterruptedException {
        peek();
        x1 = (int) (double) expr(E_NUMBER);
        match(',');
        y1 = (int) (double) expr(E_NUMBER);
        match(',');
        x2 = (int) (double) expr(E_NUMBER);
        match(',');
        y2 = (int) (double) expr(E_NUMBER);
        if (tok == ',') {
            peek();
            fill = (int) (double) expr(E_NUMBER);
            if (tok == ',') {
                peek();
                ptype = (int) (double) expr(E_NUMBER);
            } else
                ptype = 1;
        } else {
            fill = 0;
            ptype = 1;
        }
        fill &= 1;
        ptype &= 3;
        if (ptype == 3)
            ptype = 2;
        scr.box(x1, y1, x2, y2, fill, ptype);
    }
    
    void exe_circle() throws BasicException, InterruptedException {
        peek();
        x1 = (int) (double) expr(E_NUMBER);
        match(',');
        y1 = (int) (double) expr(E_NUMBER);
        match(',');
        x2 = (int) (double) expr(E_NUMBER); //r
        if (tok == ',') {
            peek();
            fill = (int) (double) expr(E_NUMBER);
            if (tok == ',') {
                peek();
                ptype = (int) (double) expr(E_NUMBER);
            } else
                ptype = 1;
        } else {
            fill = 0;
            ptype = 1;
        }
        fill &= 1;
        ptype &= 3;
        if (ptype == 3)
            ptype = 2;
        scr.circle(x1, y1, x2, fill, ptype);
    }

    void exe_ellipse() throws BasicException, InterruptedException {
        peek();
        x1 = (int) (double) expr(E_NUMBER);
        match(',');
        y1 = (int) (double) expr(E_NUMBER);
        match(',');
        x2 = (int) (double) expr(E_NUMBER); //rx
        match(',');
        y2 = (int) (double) expr(E_NUMBER); //ry
        if (tok == ',') {
            peek();
            fill = (int) (double) expr(E_NUMBER);
            if (tok == ',') {
                peek();
                ptype = (int) (double) expr(E_NUMBER);
            } else
                ptype = 1;
        } else {
            fill = 0;
            ptype = 1;
        }
        fill &= 1;
        ptype &= 3;
        if (ptype == 3)
            ptype = 2;
        scr.ellipse(x1, y1, x2, y2, fill, ptype);
    }
    
    List<Integer> onlist = new ArrayList<>();
    /**
     * on ... goto / gosub
     */
    void exe_on() throws BasicException, InterruptedException {
        peek();
        x1 = (int) (double) expr(E_NUMBER) - 1;
        if (tok != C.GOTO && tok != C.GOSUB)
            error(E.SYNTAX);
        y1 = tok;
        onlist.clear();
        peek();
        while (true) {
            onlist.add(l.ival);
            match(C.INTEGER);
            if (tok == ',')
                peek();
            else
                break;
        }
        if (tok != ':' && tok != 0xa && tok != -1 && tok != C.ELSE)
            error(E.SYNTAX);
        if (x1 >= 0 && x1 < onlist.size()) {
            Addr a = stmts.get(onlist.get(x1));
            if (a == null)
                error(E.UNDEFD_STMT);
            if (y1 == C.GOSUB) {
                Pack p = getAddr();
                subs.push(p);
            }
            l.resumeAddr(a);
            tok = 0xa;
        }
    }
    
    /**
     * 读取数据
     * <br>read id [, ...]
     */
    void exe_read() throws BasicException, InterruptedException {
        peek();
        while (true) {
            Access a = getAccess();
            switch (a.type) {
            case Id.INTEGER:
                a.put((int) (double) dr.readDouble());
                break;
            case Id.REAL:
                a.put(dr.readDouble());
                break;
            default:
                a.put(dr.readS());
            }
            if (tok != ',')
                break;
            peek();
        }
    }
    
    /**
     * 修改内存
     */
    void exe_poke() throws BasicException, InterruptedException {
        peek();
        x1 = (int) (double) expr(E_NUMBER);
        match(',');
        y1 = (int) (double) expr(E_NUMBER);
        ram.poke(x1, (byte) y1);
    }
    
    /**
     * 调用机器码
     */
    void exe_call() throws BasicException, InterruptedException {
        peek();
        ram.call((int) (double) expr(E_NUMBER));
    }
    
    While w;
    
    void exe_while() throws BasicException, InterruptedException {
        w = new While(getAddr()); //恢复地址后需要peek
        peek();
        //查找是否有重复的循环
        whiles.xreset();
        while (whiles.xpeek() != null && !whiles.xpeek().equals(w)) {
            whiles.xpop();
        }
        if (w.equals(whiles.xpeek())) {
            while (!whiles.pop().equals(w));
        }
        
        if (!while_public()) {
            whiles.push(w);
        }
    }
    
    void exe_wend() throws BasicException, InterruptedException {
        if (whiles.empty())
            error(E.WEND_WITHOUT_WHILE);
        resumeAddr(whiles.peek().addr, false);
        peek();
        if (while_public())
            whiles.pop();
    }
    
    boolean while_public() throws BasicException, InterruptedException {
        if (doubleIsZero((Double) expr(E_NUMBER))) {
            if (tok != ':' && tok != 0xa && tok != -1)
                error(E.SYNTAX);
            peek();
            int nest = 0;
            while (tok != -1) {
                if (tok == C.WHILE)
                    nest++;
                else if (tok == C.WEND) {
                    if (nest == 0)
                        break;
                    nest--;
                }
                peek();
            }
            peek();
            return true;
        } else
            return false;
    }
    
    For ff;
    Object fo;
    Id fid;
    String fs;
    /**
     * for id = exp1 to exp2 [ step exp3 ]
     */
    void exe_for() throws BasicException, InterruptedException {
        peek();
        ff = new For();
        fs = l.sval;
        ff.var = getAccess();
        if (ff.var.id.type == Id.ARRAY) //for的变量不能是数组
            error(E.SYNTAX);
        match('=');
        fo = expr(E_NUMBER);
        if (ff.var.type == Id.REAL)
            ff.var.put(fo);
        else
            ff.var.put((int) (double) fo);
        match(C.TO);
        ff.dest = (Double) expr(E_NUMBER);
        if (tok == C.STEP) {
            peek();
            ff.step = (Double) expr(E_NUMBER);
        } else
            ff.step = 1d;
        ff.addr = getAddr(); //for之后只可能是: 0xa或-1或:,else(不用储存词素了)，其他都会错误
        
      //查找是否有重复的循环
        fors.xreset();
        while (fors.xpeek() != null && !fors.xpeek().equals(ff))
            fors.xpop();
        if (ff.equals(fors.xpeek())) {
            while (!fors.pop().equals(ff));
        }
        
        if (ff.step > 0d && Double.compare((Double) ff.var.get(), ff.dest) > 0 ||
                ff.step < 0d && Double.compare((Double) ff.var.get(), ff.dest) < 0) {
            int nest = 0;
            while (tok != -1) {
                if (tok == C.FOR) {
                    nest++;
                    peek();
                } else if (tok == C.NEXT) {
                    peek();
                    while (tok == C.ID) {
                        if (l.sval.equals(ff.var.id.id))
                            nest = 0;
                        peek();
                        if (tok == ',')
                            peek();
                    }
                    if (nest == 0)
                        break;
                    nest--;
                } else
                    peek();
            }
        } else
            fors.push(ff);
    }
    
    /**
     * next [ id [, ...] ]
     */
    void exe_next() throws BasicException {
        peek();
        while (true) {
            if (tok == C.ID) {
                while (!fors.peek().var.id.id.equals(l.sval)) {
                    fors.pop();
                    if (fors.empty())
                        error(E.NEXT_WITHOUT_FOR);
                }
                ff = fors.peek();
                peek();
            } else if (fors.empty())
                error(E.NEXT_WITHOUT_FOR);
            else
                ff = fors.peek();
            if (ff.var.type == Id.INTEGER)
                ff.var.put((int) ((int) ff.var.get() + ff.step));
            else
                ff.var.put((Double) ff.var.get() + ff.step);
            if (ff.step > 0d && Double.compare((Double) ff.var.get(), ff.dest) > 0 ||
                    ff.step < 0d && Double.compare((Double) ff.var.get(), ff.dest) < 0) { //跳出循环
                fors.pop();
                if (tok == ',') {
                    peek();
                    if (tok != C.ID)
                        error(E.SYNTAX);
                } else
                    break;
            } else { //继续
                resumeAddr(ff.addr, true);
                break;
            }
        }
    }
    
    /**
     * Draw a string at any coordinate on the screen.
     * <br><b>Usage:</b>
     * <br>textout str$, x, y [, isSmall [, mode] ]
     * <br><b>mode:</b>
     * <br>&nbsp&nbsp&nbsp0 clear
     * <br>&nbsp&nbsp&nbsp1 or
     * <br>&nbsp&nbsp&nbsp2 not
     * <br>&nbsp&nbsp&nbspbit2 = 1 transparent; = 0 opaque
     */
    void exe_textout() throws BasicException, InterruptedException {
        peek();
        ss = (S) expr(E_STRING);
        match(',');
        x1 = (int) (double) expr(E_NUMBER);
        match(',');
        y1 = (int) (double) expr(E_NUMBER);
        if (tok == ',') {
            peek();
            fill = (int) (double) expr(E_NUMBER);
            if (tok == ',') {
                peek();
                ptype = (int) (double) expr(E_NUMBER);
            } else
                ptype = 1;
        } else
            fill = 0;
        scr.textOut(ss, x1, y1, fill, ptype);
    }
    
    /**
     * Delay for certain milliseconds.
     * <br><b>Usage:</b>
     * <br>sleep millisecond
     */
    void exe_sleep() throws BasicException, InterruptedException {
        peek();
        x1 = (int) (double) expr(E_NUMBER);
        frm.getScreen().stopFlash();
        Thread.sleep(x1);
        frm.getScreen().flash();
    }
    
    /**
     * Draw a picture stored in ram at any coordinate on the screen.
     * <br><b>Usage:</b>
     * <br>paint addr, x, y, w, h [, mode]
     * <br><b>mode:</b>
     * <br>&nbsp&nbsp&nbspthe same as textout
     */
    void exe_paint() throws BasicException, InterruptedException {
        peek();
        fill = (int) (double) expr(E_NUMBER);
        match(',');
        x1 = (int) (double) expr(E_NUMBER);
        match(',');
        y1 = (int) (double) expr(E_NUMBER);
        match(',');
        x2 = (int) (double) expr(E_NUMBER); //w
        match(',');
        y2 = (int) (double) expr(E_NUMBER); //h
        if (tok == ',') {
            peek();
            ptype = (int) (double) expr(E_NUMBER);
        } else
            ptype = 1;
        scr.paint(fill, x1, y1, x2, y2, ptype);
    }
    
    /**
     * 左对齐字符串
     * lset id$ = exp$
     */
    void exe_lset() throws BasicException, InterruptedException {
        peek();
        iacc = getAccess(E_STRING);
        match('=');
        pb = ((S) expr(E_STRING)).getBytes();
        byte[] pb2 = ((S) iacc.get()).getBytes();
        for (int i = 0, j = pb.length > pb2.length ? pb2.length : pb.length; i < j; i++)
            pb2[i] = pb[i];
        iacc.put(new S(pb2));
    }
    
    /**
     * 右对齐字符串
     * rset id$ = exp$
     */
    void exe_rset() throws BasicException, InterruptedException {
        peek();
        iacc = getAccess(E_STRING);
        match('=');
        pb = ((S) expr(E_STRING)).getBytes();
        byte[] pb2 = ((S) iacc.get()).getBytes();
        for (int i = pb2.length - 1, j = pb.length - 1; i >= 0 && j >= 0; i--, j--)
            pb2[i] = pb[j];
        iacc.put(new S(pb2));
    }
    
    /**
     * 获取文件号
     * @return 文件号（0～2）
     */
    int getFileNumber() throws BasicException {
        if (tok == '#')
            peek();
        int i = l.ival - 1;
        match(C.INTEGER);
        if (i > 2 || i < 0)
            error(E.FILE_NUMBER);
        return i;
    }
    
    /**
     * open str$ for FILE_MODE as #n
     * <br><b>FILE_MODE:</b>
     * <br>&nbsp&nbsp&nbspinput
     * <br>&nbsp&nbsp&nbspoutput
     * <br>&nbsp&nbsp&nbspappend
     * <br>&nbsp&nbsp&nbsprandom
     * <br>&nbsp&nbsp&nbspbinary
     */
    void exe_open() throws BasicException, InterruptedException {
        peek();
        ss = (S) expr(E_STRING);
        if (!ss.contains((byte) '.'))
            ss = ss.concat(new S(".dat"));
        match(C.FOR);
        pt = tok;
        if (tok != C.INPUT && tok != C.OUTPUT && tok != C.APPEND && tok != C.RANDOM && tok != C.BINARY)
            error(E.FILE_MODE);
        peek();
        match(C.AS);
        x1 = getFileNumber();
        if (files[x1].state != FileState.CLOSE)
            error(E.FILE_REOPEN);
        if (pt == C.RANDOM && tok == C.ID && l.sval.equals("len")) { //LEN = n
            peek();
            match('=');
            y1 = files[x1].len = l.ival;
            match(C.INTEGER);
            if (y1 < 1)
                error(E.ILLEGAL_QUANTITY);
        }
        switch (pt) {
        case C.INPUT:
            files[x1].state = FileState.INPUT;
            cr = fm.open(ss.toString(), x1, FileManager.INPUT);
            break;
        case C.OUTPUT:
            files[x1].state = FileState.OUTPUT;
            cr = fm.open(ss.toString(), x1, FileManager.OUTPUT);
            break;
        case C.APPEND:
            files[x1].state = FileState.APPEND;
            cr = fm.open(ss.toString(), x1, FileManager.APPEND);
            break;
        case C.BINARY:
            files[x1].state = FileState.BINARY;
            cr = fm.open(ss.toString(), x1, FileManager.RANDOM);
            break;
        default:
            files[x1].state = FileState.RANDOM;
            cr = fm.open(ss.toString(), x1, FileManager.RANDOM);
            files[x1].rec = null;
        }
        if (!cr)
            error(E.FILE_OPEN);
        files[x1].size = fm.getLength(x1);
    }
    /**
     * close #n
     */
    void exe_close() throws BasicException {
        peek();
        pt = getFileNumber();
        if (files[pt].state == FileState.CLOSE)
            error(E.FILE_CLOSE);
        files[pt].close();
        if (!fm.close(pt))
            error(E.FILE_CLOSE);
    }
    
    /**
     * write #n, expr [, ...]
     */
    void exe_write() throws BasicException, InterruptedException {
        peek();
        pt = getFileNumber();
        if (files[pt].state != FileState.APPEND && files[pt].state != FileState.OUTPUT)
            error(E.FILE_MODE);
        match(',');
        while (true) {
            fo = expr();
            if (fo instanceof S)
                fm.writeQuotedS((S) fo, pt);
            else
                fm.writeReal((Double) fo, pt);
            if (tok == ',') {
                peek();
                if (!fm.writeComma(pt))
                    error(E.FILE_WRITE);
            } else {
                if (!fm.writeEOF(pt))
                    error(E.FILE_WRITE);
                break;
            }
        }
    }
    
    Record rec;
    List<Integer> fil = new ArrayList<>();
    List<Access> fal = new ArrayList<>();
    Access[] uslsac = new Access[0];
    /**
     * 分配文件缓冲区。id$会用'\0'填充
     * field #n, m as id$ [, ...]
     */
    void exe_field() throws BasicException, InterruptedException {
        peek();
        pt = getFileNumber();
        if (files[pt].state != FileState.RANDOM)
            error(E.FILE_MODE);
        match(',');
        rec = new Record();
        fil.clear();
        fal.clear();
        while (true) {
            x1 = l.ival;
            match(C.INTEGER);
            if (x1 < 1)
                error(E.ILLEGAL_QUANTITY);
            match(C.AS);
            iacc = getAccess();
            if (iacc.type != Id.STRING || iacc.id.type == Id.ARRAY) //不能用数组
                error(E.SYNTAX);
            fil.add(x1);
            fal.add(iacc);
            iacc.put(new S(new byte[x1])); //字符串用0填充
            rec.total += x1;
            if (tok == ',')
                peek();
            else
                break;
        }
        rec.size = fil.toArray(uslsi);
        rec.acc = fal.toArray(uslsac);
        files[pt].rec = rec;
        if (files[pt].len > 0 && rec.total != files[pt].len)
            error(E.ASK_CACHE);
    }
    
    /**
     * put #n, expr
     */
    void exe_put() throws BasicException, InterruptedException {
        peek();
        pt = getFileNumber();
        if (files[pt].state != FileState.RANDOM)
            error(E.FILE_MODE);
        match(',');
        x1 = (int) (double) expr(E_NUMBER) - 1;
        if (x1 < 0)
            error(E.ILLEGAL_QUANTITY);
        rec = files[pt].rec;
        if (rec == null)
            error(E.NOT_ASK_CACHE);
        if (!fm.seek(rec.total * x1, pt))
            error(E.RECORD_NUMBER);
        for (x2 = 0; x2 < rec.size.length; x2++) {
            if (!fm.writeBytes(Arrays.copyOf(((S) rec.acc[x2].get()).getBytes(), rec.size[x2]), pt))
                error(E.FILE_WRITE);
        }
    }
    
    /**
     * get #n, expr
     */
    void exe_get() throws BasicException, InterruptedException {
        peek();
        pt = getFileNumber();
        if (files[pt].state != FileState.RANDOM)
            error(E.FILE_MODE);
        match(',');
        x1 = (int) (double) expr(E_NUMBER) - 1;
        if (x1 < 0)
            error(E.ILLEGAL_QUANTITY);
        rec = files[pt].rec;
        if (rec == null)
            error(E.NOT_ASK_CACHE);
        if (!fm.seek(rec.total * x1, pt) || fm.eof(pt))
            error(E.RECORD_NUMBER);
        for (x2 = 0; x2 < rec.size.length; x2++) {
            pb = fm.readBytes(rec.size[x2], pt);
            if (pb == null)
                error(E.FILE_READ);
            rec.acc[x2].put(new S(pb));
        }
    }
    
    /**
     * Locate binary file pointer.
     * <br><b>Usage:</b>
     * <br>fseek #n, pos
     * <br>&nbsp&nbsppos: 0-indexed
     */
    void exe_fseek() throws BasicException, InterruptedException {
        peek();
        pt = getFileNumber();
        if (files[pt].state != FileState.BINARY)
            error(E.FILE_MODE);
        match(',');
        x1 = (int) (double) expr(E_NUMBER);
        if (x1 < 0)
            error(E.ILLEGAL_QUANTITY);
        if (!fm.seek(x1, pt))
            error(E.FILE_SEEK);
    }
    
    /**
     * Read data to ram from binary file.
     * <br><b>Usage:</b>
     * <br>fread #n, addr, size
     */
    void exe_fread() throws BasicException, InterruptedException {
        peek();
        pt = getFileNumber();
        if (files[pt].state != FileState.BINARY)
            error(E.FILE_MODE);
        match(',');
        x1 = (int) (double) expr(E_NUMBER);
        match(',');
        y1 = (int) (double) expr(E_NUMBER);
        if (y1 < 0)
            error(E.ILLEGAL_QUANTITY);
        if (y1 > 0) {
            pb = fm.readBytes(y1, pt);
            if (pb == null)
                error(E.FILE_READ);
            ram.poke(x1, pb);
        }
    }
    
    /**
     * Write data to binary file from ram.
     * <br><b>Usage:</b>
     * <br>fwrite #n, addr, size
     */
    void exe_fwrite() throws BasicException, InterruptedException {
        peek();
        pt = getFileNumber();
        if (files[pt].state != FileState.BINARY)
            error(E.FILE_MODE);
        match(',');
        x1 = (int) (double) expr(E_NUMBER);
        match(',');
        y1 = (int) (double) expr(E_NUMBER);
        if (y1 < 0)
            error(E.ILLEGAL_QUANTITY);
        if (y1 > 0 && !fm.writeBytes(ram.peek(x1, y1), pt))
                error(E.FILE_WRITE);
    }
    
    long fln;
    /**
     * Read primitive from binary file.
     * <br><b>Usage:</b>
     * <br>fget #n, access / access$ / access% [, ...]
     */
    void exe_fget() throws BasicException, InterruptedException {
        peek();
        pt = getFileNumber();
        if (files[pt].state != FileState.BINARY)
            error(E.FILE_MODE);
        match(',');
        while (true) {
            iacc = getAccess();
            switch (iacc.type) {
            case Id.INTEGER:
                pb = fm.readBytes(2, pt);
                if (pb == null)
                    error(E.FILE_READ);
                iacc.put((pb[0] & 0xff) | pb[1]); //little endian
                break;
            case Id.REAL:
                pb = fm.readBytes(8, pt);
                if (pb == null)
                    error(E.FILE_READ);
                for (x1 = 7; x1 >= 0; x1--) {
                    fln <<= 8;
                    fln |= pb[x1] & 0xff;
                }
                iacc.put(Double.longBitsToDouble(fln));
                break;
            default:
                ss = (S) iacc.get();
                if (ss.length() > 0) {
                    pb = fm.readBytes(ss.length(), pt);
                    if (pb == null)
                        error(E.FILE_READ);
                    iacc.put(new S(pb));
                }
            }
            if (tok == ',')
                peek();
            else
                break;
        }
    }
    
    int fshr;
    /**
     * Write primitive to binary file.
     * <br><b>Usage:</b>
     * <br>fwrite #n, access / access$ / access% [, ...]
     */
    void exe_fput() throws BasicException, InterruptedException {
        peek();
        pt = getFileNumber();
        if (files[pt].state != FileState.BINARY)
            error(E.FILE_MODE);
        match(',');
        while (true) {
            iacc = getAccess();
            switch (iacc.type) {
            case Id.INTEGER:
                fshr = (int) iacc.get();
                if (!(fm.writeByte(fshr, pt) && fm.writeByte(fshr >>> 8, pt)))
                        error(E.FILE_WRITE);
                break;
            case Id.REAL:
                fln = Double.doubleToLongBits((Double) iacc.get());
                for (x1 = 0; x1 < 8; x1++) {
                    if (!fm.writeByte((byte) fln, pt))
                        error(E.FILE_WRITE);
                    fln >>>= 8;
                }
                break;
            default:
                pb = ((S) iacc.get()).getBytes();
                if (pb.length > 0 && !fm.writeBytes(pb, pt))
                    error(E.FILE_WRITE);
            }
            if (tok == ',')
                peek();
            else
                break;
        }
    }
    
    /**
     * Write byte(s) to binary file.
     * <br><b>Usage:</b>
     * <br>fputc #n, expr / expr$ [, ...]
     * <br>If expr is a string, then the first byte of the string will
     * <br>be written to file; otherwise the lowest 8 bit of integer
     * <br>presentation of expr will be written.
     */
    void exe_fputc() throws BasicException, InterruptedException {
        peek();
        pt = getFileNumber();
        if (files[pt].state != FileState.BINARY)
            error(E.FILE_MODE);
        match(',');
        while (true) {
            fo = expr();
            if (fo instanceof Double) {
                if (!fm.writeByte((int) (double) fo, pt))
                    error(E.FILE_WRITE);
            } else {
                if (!fm.writeByte(((S) fo).byteAt(0), pt))
                    error(E.FILE_WRITE);
            }
            if (tok == ',')
                peek();
            else
                break;
        }
    }
    
    /**
     * Load direct data to ram
     * <br><b>Usage:</b>
     * <br>load addr, exp1 [, ...]
     */
    void exe_load() throws BasicException, InterruptedException {
        peek();
        pt = (int) (double) expr(E_NUMBER);
        match(',');
        while (true) {
            ram.poke(pt++, (byte) (int) (double) expr(E_NUMBER));
            if (tok == ',')
                peek();
            else
                break;
        }
    }
    
    /**
     * def fn f(x) = ...
     */
    void exe_def() throws BasicException {
        peek();
        match(C.FN);
        Fn f = new Fn();
        fs = l.sval;
        match(C.ID);
        match('(');
        f.var = l.sval;
        match(C.ID);
        match(')');
        f.addr = getAddr();
        match('=');
        while (tok != ':' && tok != 0xa && tok != -1)
            peek();
        if (fs.charAt(fs.length() - 1) == '$')
            f.ftype = E_STRING;
        else
            f.ftype = E_NUMBER;
        if (f.var.charAt(f.var.length() - 1) == '$')
            f.vtype = E_STRING;
        else
            f.vtype = E_NUMBER;
        funs.put(fs, f);
    }
    
    /**
     * 获取指定类型的访问接口
     * @param type 类型常数，和expr一样
     */
    Access getAccess(int type) throws BasicException, InterruptedException {
        Access gaacc = getAccess();
        if (type == E_NUMBER && gaacc.type == Id.STRING ||
                type == E_STRING && gaacc.type != Id.STRING)
            error(E.TYPE_MISMATCH);
        return gaacc;
    }
    
    /**
     * 获取id访问接口，会检查是否以id开头
     */
    Access getAccess() throws BasicException, InterruptedException {
        String s = l.sval;
        Id id;
        int index = 0, t, base, d;
        
        match(C.ID);
        if (tok == '(') { //数组
            peek();
            id = new Id(s, Id.ARRAY);
            if (!vars.containsKey(id)) { //数组未定义，根据下标个数定义数组
                base = d = 1;
                switch (s.charAt(s.length() - 1)) {
                case '$':
                    Array<S> sar = new Array<>();
                    while (true) {
                        t = (int) (double) expr(E_NUMBER);
                        if (t > 10 || t < 0)
                            error(E.BAD_SUBSCRIPT);
                        index += t * base;
                        base *= 11;
                        if (tok == ',') {
                            peek();
                            d++;
                        } else
                            break;
                    }
                    match(')');
                    
                    sar.base = new int[d];
                    sar.bound = new Integer[d];
                    base = 1;
                    for (int i = 0; i < d; i++) {
                        sar.base[i] = base;
                        base *= 11;
                        sar.bound[i] = 10;
                    }
                    sar.value = new S[base];
                    for (int i = 0; i < base; i++)
                        sar.value[i] = new S();
                    synchronized (vars) {
                        vars.put(id, sar);
                        frm.vartable.revalidate();
                    }
                    
                    return new ArrayAccess(id, Id.STRING, sar, index);
                case '%':
                    Array<Integer> iar = new Array<>();
                    while (true) {
                        t = (int) (double) expr(E_NUMBER);
                        if (t > 10 || t < 0)
                            error(E.BAD_SUBSCRIPT);
                        index += t * base;
                        base *= 11;
                        if (tok == ',') {
                            peek();
                            d++;
                        } else
                            break;
                    }
                    match(')');
                    
                    iar.base = new int[d];
                    iar.bound = new Integer[d];
                    base = 1;
                    for (int i = 0; i < d; i++) {
                        iar.base[i] = base;
                        base *= 11;
                        iar.bound[i] = 10;
                    }
                    iar.value = new Integer[base];
                    for (int i = 0; i < base; i++)
                        iar.value[i] = 0;
                    synchronized (vars) {
                        vars.put(id, iar);
                        frm.vartable.revalidate();
                    }
                    
                    return new ArrayAccess(id, Id.INTEGER, iar, index);
                default:
                    Array<Double> rar = new Array<>();
                    while (true) {
                        t = (int) (double) expr(E_NUMBER);
                        if (t > 10 || t < 0)
                            error(E.BAD_SUBSCRIPT);
                        index += t * base;
                        base *= 11;
                        if (tok == ',') {
                            peek();
                            d++;
                        } else
                            break;
                    }
                    match(')');
                    
                    rar.base = new int[d];
                    rar.bound = new Integer[d];
                    base = 1;
                    for (int i = 0; i < d; i++) {
                        rar.base[i] = base;
                        base *= 11;
                        rar.bound[i] = 10;
                    }
                    rar.value = new Double[base];
                    for (int i = 0; i < base; i++)
                        rar.value[i] = 0d;
                    synchronized (vars) {
                        vars.put(id, rar);
                        frm.vartable.revalidate();
                    }
                    
                    return new ArrayAccess(id, Id.REAL, rar, index);
                }
            }
            switch (s.charAt(s.length() - 1)) { //获取数组下标
            case '$':
                @SuppressWarnings("unchecked")
                Array<S> sar = (Array<S>) vars.get(id);
                for (int i = 0; i < sar.bound.length; i++) {
                    t = (int) (double) expr(E_NUMBER);
                    if (t > sar.bound[i] || t < 0)
                        error(E.BAD_SUBSCRIPT);
                    index += t * sar.base[i];
                    if (i < sar.bound.length - 1)
                        match(',');
                }
                match(')');
                return new ArrayAccess(id, Id.STRING, sar, index);
            case '%':
                @SuppressWarnings("unchecked")
                Array<Integer> iar = (Array<Integer>) vars.get(id);
                for (int i = 0; i < iar.bound.length; i++) {
                    t = (int) (double) expr(E_NUMBER);
                    if (t > iar.bound[i] || t < 0)
                        error(E.BAD_SUBSCRIPT);
                    index += t * iar.base[i];
                    if (i < iar.bound.length - 1)
                        match(',');
                }
                match(')');
                return new ArrayAccess(id, Id.INTEGER, iar, index);
            default:
                @SuppressWarnings("unchecked")
                Array<Double> rar = (Array<Double>) vars.get(id);
                for (int i = 0; i < rar.bound.length; i++) {
                    t = (int) (double) expr(E_NUMBER);
                    if (t > rar.bound[i] || t < 0)
                        error(E.BAD_SUBSCRIPT);
                    index += t * rar.base[i];
                    if (i < rar.bound.length - 1)
                        match(',');
                }
                match(')');
                return new ArrayAccess(id, Id.REAL, rar, index);
            }
        } else { //普通变量
            switch (s.charAt(s.length() - 1)) {
            case '$':
                id = new Id(s, Id.STRING);
                break;
            case '%':
                id = new Id(s, Id.INTEGER);
                break;
            default:
                id = new Id(s, Id.REAL);
            }
            if (!vars.containsKey(id)) { //变量未定义，定义一个新变量
                synchronized (vars) {
                    switch (id.type) {
                    case Id.INTEGER:
                        vars.put(id, 0);
                        break;
                    case Id.STRING:
                        vars.put(id, new S());
                        break;
                    default:
                        vars.put(id, 0d);
                    }
                    frm.vartable.revalidate();
                }
            }
            return new IdAccess(id);
        }
    }
    
    static int E_NUMBER = 1, E_STRING = 2;
    
    /**
     * 读取指定类型表达式
     * @param type 类型<br><i>E_NUMBER</i> 或 <i>E_STRING</i>
     */
    Object expr(int type) throws BasicException, InterruptedException {
        Object r = E(0);
        if (type == E_NUMBER && r instanceof S ||
                type == E_STRING && !(r instanceof S))
            error(E.TYPE_MISMATCH);
        return r;
    }
    /**
     * 读取表达式
     */
    Object expr() throws BasicException, InterruptedException {
        return E(0);
    }
    
    Object E(int p) throws BasicException, InterruptedException {
        Object result = F();
        int i;
        while (getp(tok) > p) {
            i = tok;
            peek();
            result = arith(i, result, E(getp(i)));
        }
        return result;
    }

    Stack2<String> fns = new Stack2<>(); //函数自变量名
    Stack2<Object> fnvar = new Stack2<>(); //函数自变量值
    //存储上一个随机数
    double rnd;
    /**
     * 只会返回Double或S
     */
    Object F() throws BasicException, InterruptedException { //num, string, var, +-, not, inkey, (
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
        
        Object o = null;
        String s;
        S s2;
        int t, t2;
        long lt;
        byte[] b;
        double d;
        
        switch (tok) {
        case C.INTEGER: case C.REAL:
            Double r = l.rval;
            peek();
            return r;
        case C.STRING:
            s = l.sval;
            peek();
            return new S(s);
        case C.ID:
            if (infuns.contains(l.sval)) { //内置函数
                s = l.sval;
                peek();
                match('(');
                switch (s) {
                case C.ABS: //绝对值
                    o = Math.abs((Double) expr(E_NUMBER));
                    break;
                case C.ASC: //ascii码，字符串长度为0则返回0
                    s2 = (S) expr(E_STRING);
                    if (s2.length() == 0)
                        o = 0d;
                    else
                        o = (double) s2.charAt(0);
                    break;
                case C.ATN: //反正切值
                    o = Math.atan((Double) expr(E_NUMBER));
                    break;
                case C.CHR: //ascii码转为字符串，取低八位。有问题：如果bit7=1，则会自动转换为问号
                    o = new S((byte) (double) expr(E_NUMBER));
                    break;
                case C.COS: //余弦值
                    o = Math.cos((Double) expr(E_NUMBER));
                    break;
                case C.CVI: //字符串前2byte转为整数.little endian
                    b = Arrays.copyOf(((S) expr(E_STRING)).getBytes(), 2);
                    o = (double) ((b[0] & 0xff) + ((b[1] & 0xff) << 8));
                    break;
                case C.MKI: //整数转换为2byte字符串
                    t = (int) (double) expr(E_NUMBER);
                    o = new S(new byte[] {(byte) t, (byte) (t >>> 8)});
                    break;
                case C.CVS: //字符串前8字节转换为double
                    b = Arrays.copyOf(((S) expr(E_STRING)).getBytes(), 8);
                    for (lt = t = 0; t < 8; t++) {
                        lt |= b[7 - t];
                        lt <<= 8;
                    }
                    o = Double.longBitsToDouble(lt);
                    break;
                case C.MKS: //double转换为8字节字符串
                    lt = Double.doubleToLongBits((Double) expr(E_NUMBER));
                    b = new byte[8];
                    for (t = 0; t < 8; t++) {
                        b[t] = (byte) lt;
                        lt >>>= 8;
                    }
                    o = new S(b);
                    break;
                case C.EXP: //e的n次方
                    o = Math.exp((Double) expr(E_NUMBER));
                    break;
                case C.INT: //取整
                    o = Math.floor((Double) expr(E_NUMBER));
                    break;
                case C.LEFT: //取字符串的前n字节
                    s2 = (S) expr(E_STRING);
                    b = s2.getBytes();
                    match(',');
                    t = (int) (double) expr(E_NUMBER);
                    if (t < 1)
                        error(E.ILLEGAL_QUANTITY);
                    if (t > b.length)
                        t = b.length;
                    o = new S(b, 0, t);
                    break;
                case C.LEN: //字符串长（字节）
                    o = (double) ((S) expr(E_STRING)).length();
                    break;
                case C.LOG: //ln
                    o = Math.log((Double) expr(E_NUMBER));
                    break;
                case C.MID: //取字符串第m个字节开始的n字节，若省略n则n=1
                    s2 = (S) expr(E_STRING);
                    b = s2.getBytes();
                    match(',');
                    t = (int) (double) expr(E_NUMBER) - 1;
                    if (tok == ',') {
                        peek();
                        t2 = (int) (double) expr(E_NUMBER);
                    } else
                        t2 = 1;
                    if (t >= b.length || t < 0 || t2 < 1)
                        error(E.ILLEGAL_QUANTITY);
                    o = new S(b, t, t + t2);
                    break;
                case C.POS: //获取光标横坐标。参数没用
                    expr(E_NUMBER);
                    o = (double) (scr.getX() + 1);
                    break;
                case C.RIGHT: //取字符串后n个字节
                    s2 = (S) expr(E_STRING);
                    b = s2.getBytes();
                    match(',');
                    t = (int) (double) expr(E_NUMBER);
                    if (t < 1)
                        error(E.ILLEGAL_QUANTITY);
                    if (t > b.length)
                        t = b.length;
                    o = new S(b, b.length - t, b.length);
                    break;
                case C.RND: //随机数，若参数为0则返回上一个随机数
                    if (doubleIsZero((Double) expr(E_NUMBER)))
                        o = rnd;
                    else
                        o = rnd = Math.random();
                    break;
                case C.SGN:
                    d = (Double) expr(E_NUMBER);
                    o = doubleIsZero(d) ? 0d : Double.compare(d, 0d) > 0 ? 1d : -1d;
                    break;
                case C.SIN:
                    o = Math.sin((Double) expr(E_NUMBER));
                    break;
                case C.SQR:
                    o = Math.sqrt((Double) expr(E_NUMBER));
                    break;
                case C.STR: //实数转字符串
                    o = new S(realToString((Double) expr(E_NUMBER)));
                    break;
                case C.TAN:
                    o = Math.tan((Double) expr(E_NUMBER));
                    break;
                case C.VAL: //字符串转实数，若字符串为空则返回0.忽略前导空白符
                    o = str2d(((S) expr(E_STRING)).toString());
                    break;
                case C.PEEK: //超出内存范围返回0
                    o = (double) (ram.peek((int) (double) expr(E_NUMBER)) & 0xff);
                    break;
                case C.POINT: //获取点，若超出屏幕则返回1
                    t = (int) (double) expr(E_NUMBER);
                    match(',');
                    t2 = (int) (double) expr(E_NUMBER);
                    o = (double) scr.point(t, t2);
                    break;
                case C.CHECKKEY: //检测某键是否被按下,是返回1
                    t = (int) (double) expr(E_NUMBER);
                    if (t > 127 || t < 0)
                        error(E.ILLEGAL_QUANTITY);
                    o = frm.checkKey(recoverKeyCode(t)) ? 1d : 0d;
                    break;
                case C.EOF: //是否到达文件尾，是返回0，否则返回1.只在input模式下有效.参数必须是常数
                    t = getFileNumber();
                    if (files[t].state != FileState.INPUT)
                        error(E.FILE_MODE);
                    o = fm.eof(t) ? 0d : 1d;
                    break;
                case C.LOF: //返回文件长度。只要文件打开就可以调用.参数必须是常数
                    o = (double) files[getFileNumber()].size;
                    break;
                case C.FTELL: //返回当前文件指针。只在binary下有效
                    t = getFileNumber();
                    if (files[t].state != FileState.BINARY)
                        error(E.FILE_MODE);
                    o = (double) fm.tell(t);
                    break;
                case C.FGETC: //获取一个字节。只在BINARY下有效
                    t = getFileNumber();
                    if (files[t].state != FileState.BINARY)
                        error(E.FILE_MODE);
                    try {
                        o = (double) (fm.readByte(t) & 0xff); //unsigned
                    } catch (NullPointerException e) {
                        error(E.FILE_READ);
                    }
                    break;
                }
                match(')');
                return o;
            } else if (!fns.empty() && l.sval.equals(fns.peek())) { //函数自变量
                peek();
                return fnvar.peek();
            } else { //是变量
                Object oo = getAccess().get();
                if (oo instanceof Integer)
                    return (double) (int) oo;
                else
                    return oo;
            }
        case '+': case '-':
            t = tok;
            peek();
            o = E(5);
            if (!(o instanceof Double))
                error(E.TYPE_MISMATCH);
            return t == '-' ? -(Double) o : o;
        case C.NOT:
            peek();
            o = E(12);
            if (!(o instanceof Double))
                error(E.TYPE_MISMATCH);
            return doubleIsZero((Double) o) ? 1d : 0d;
        case C.INKEY:
            peek();
            return new S((byte) convertKeyCode(frm.inkey(false)));
        case '(':
            peek();
            Object ooo = E(0);
            match(')');
            return ooo;
        case C.FN:
            peek();
            s = l.sval;
            match(C.ID);
            match('(');
            Fn fn = funs.get(s);
            if (fn == null)
                error(E.UNDEFD_FUNC);
            o = expr(fn.vtype); //自变量
            Pack p = getAddr();
            fns.push(fn.var);
            fnvar.push(o);
            resumeAddr(fn.addr, false);
            peek();
            o = expr(fn.ftype); //函数值
            resumeAddr(p, true);
            match(')');
            fns.pop();
            fnvar.pop();
            return o;
        default:
            error(E.SYNTAX);
            return 0d;
        }
    }
    
    int getp(int op) {
        switch (op) {
        case C.GTE: case C.LTE: case '=': case C.NEQ: case '>': case '<':
            return 2;
        case '+': case '-':
            return 4;
        case '*': case '/':
            return 6;
        case '^':
            return 8;
        case C.AND: case C.OR:
            return 1;
        default:
            return 0;
        }
    }
    
    Object arith(int op, Object a, Object b) throws BasicException {
        switch (op) {
        case '+':
            if (a instanceof Double && b instanceof Double)
                return (Double) a + (Double) b;
            else if (a instanceof S && b instanceof S)
                return S.concat((S) a, (S) b);
            break;
        case '-':
            if (a instanceof Double && b instanceof Double)
                return (Double) a - (Double) b;
            break;
        case '*':
            if (a instanceof Double && b instanceof Double)
                return (Double) a * (Double) b;
            break;
        case '/':
            if (a instanceof Double && b instanceof Double) {
                if (doubleIsZero((Double) b))
                        error(E.DIVISION_BY_ZERO);
                return (Double) a / (Double) b;
            }
            break;
        case '^':
            if (a instanceof Double && b instanceof Double)
                return Math.pow((Double) a, (Double) b);
            break;
        case C.GTE:
            if (a instanceof Double && b instanceof Double)
                return Double.compare((Double) a, (Double) b) > 0 ||
                        doubleEqual((Double) a, (Double) b) ? 1d : 0d;
            else if (a instanceof S && b instanceof S)
                return ((S) a).compareTo((S) b) >= 0 ? 1d : 0d;
            break;
        case C.LTE:
            if (a instanceof Double && b instanceof Double)
                return Double.compare((Double) a, (Double) b) < 0 ||
                        doubleEqual((Double) a, (Double) b) ? 1d : 0d;
            else if (a instanceof S && b instanceof S)
                return ((S) a).compareTo((S) b) <= 0 ? 1d : 0d;
            break;
        case '>':
            if (a instanceof Double && b instanceof Double)
                return Double.compare((Double) a, (Double) b) > 0 ? 1d : 0d;
            else if (a instanceof S && b instanceof S)
                return ((S) a).compareTo((S) b) > 0 ? 1d : 0d;
            break;
        case '<':
            if (a instanceof Double && b instanceof Double)
                return Double.compare((Double) a, (Double) b) < 0 ? 1d : 0d;
            else if (a instanceof S && b instanceof S)
                return ((S) a).compareTo((S) b) < 0 ? 1d : 0d;
            break;
        case '=':
            if (a instanceof Double && b instanceof Double)
                return doubleEqual((Double) a, (Double) b) ? 1d : 0d;
            else if (a instanceof S && b instanceof S)
                return ((S) a).equals(b) ? 1d : 0d;
            break;
        case C.NEQ:
            if (a instanceof Double && b instanceof Double)
                return doubleEqual((Double) a, (Double) b) ? 0d : 1d;
            else if (a instanceof S && b instanceof S)
                return ((S) a).equals(b) ? 0d : 1d;
            break;
        case C.OR:
            if (a instanceof Double && b instanceof Double)
                return doubleIsZero((Double) a) && doubleIsZero((Double) b) ? 0d : 1d;
            break;
        case C.AND:
            if (a instanceof Double && b instanceof Double)
                return doubleIsZero((Double) a) || doubleIsZero((Double) b) ? 0d : 1d;
            break;
        default:
            error(E.SYNTAX);
        }
        error(E.TYPE_MISMATCH);
        return null;
    }
    
    /**
     * 抛出异常
     * @param type 异常类型
     */
    void error(int type) throws BasicException {
        throw new BasicException(type);
    }
    
    /**
     * 获取当前地址（词法分析器地址+行号+if嵌套+当前词法单元）
     */
    Pack getAddr() {
        return new Pack(l.getAddr(), stmt, ifs, tok);
    }
    
    /**
     * 恢复地址
     * @param p 打包的地址
     * @param b 是否恢复源地址的词法单元
     */
    void resumeAddr(Pack p, boolean b) {
        l.resumeAddr(p.addr);
        stmt = p.stmt;
        ifs = p.ifs;
        if (b)
            tok = p.tok;
    }
    
    /**
     * 获取wqx键值
     */
    int inkey() throws InterruptedException {
        return convertKeyCode(frm.inkey());
    }
}

/**
 * for循环信息
 */
class For {
    /**
     * 自变量
     */
    public Access var;
    /**
     * 目标值
     */
    public double dest;
    /**
     * 步长
     */
    public double step;
    /**
     * 包装好的地址
     */
    public Pack addr;
    
    public boolean equals(Object o) {
        return o instanceof For ? var.equals(((For) o).var) : false;
    }
    
    public String toString() {
        return "for:" + var + " to " + dest + " step " + step + ":" + addr;
    }
}

/**
 * while循环信息
 */
class While {
    public Pack addr;
    
    public boolean equals(Object o) {
        return o instanceof While ? addr.equals(((While) o).addr) : false;
    }
    
    public While() {}
    
    public While(Pack a) {
        addr = a;
    }
    
    public String toString() {
        return "while:" + addr;
    }
}

/**
 * 自定义函数
 */
class Fn {
    
    /**
     * 自变量。必须是double型
     */
    public String var;
    /**
     * 函数表达式类型,E_NUMBER或E_STRING
     */
    public int ftype;
    /**
     * 自变量类型，同上
     */
    public int vtype;
    
    /**
     * 函数表达式所在的地址
     */
    public Pack addr;
    
    public String toString() {
        return "(" + var + "):" + addr;
    }
}

/**
 * 把行号和地址包装起来
 */
class Pack {
    public final Addr addr;
    public final int stmt, ifs, tok;
    
    public boolean equals(Object o) {
        return o instanceof Pack ? addr.equals(((Pack) o).addr) : false;
    }
    
    /**
     * 包装
     * @param a 词法分析器的地址
     * @param s 当前行号
     */
    public Pack(Addr a, int s, int ifs, int tok) {
        addr = a;
        stmt = s;
        this.ifs = ifs;
        this.tok = tok;
    }
    
    public String toString() {
        return "[P " + addr + " S:" + stmt + "]";
    }
}

/**
 * 用于变量访问的接口
 */
abstract class Access {
    public final Id id;
    public final int type;
    public Access(Id id, int type) {
        this.id = id;
        this.type = type;
    }
    public abstract Object get();
    public abstract void put(Object val);
    public boolean equals(Object o) {
        return o instanceof Access && id.equals(((Access) o).id);
    }
}

/**
 * 数据读取器
 */
class DataReader {
    private byte[] z;
    private int cap; //容量
    private int pos; //读取位置
    
    private List<Integer> stmt = new ArrayList<>(), mark = new ArrayList<>();
    
    public DataReader() {
        this(1024);
    }
    
    public DataReader(int capacity) {
        z = new byte[capacity];
        stmt.clear();
        mark.clear();
    }
    
    public void clear() {
        cap = 0;
        pos = 0;
        z = new byte[1024];
    }
    
    public void append(byte b) {
        ensureCapacity(1);
        z[cap++] = b;
    }
    
    public void append(int b) {
        append((byte) b);
    }
    
    void ensureCapacity(int i) {
        while (cap + i > z.length)
            z = Arrays.copyOf(z, z.length + 1024);
    }
    
    public void addComma() {
        append(',');
    }
    
    int getc() {
        return pos < cap ? z[pos++] & 0xff : -1;
    }
    
    void peek() {
        c = getc();
    }
    
    int c;
    ByteStringBuffer bsb = new ByteStringBuffer();
    
    public String readString() throws BasicException {
        peek();
        bsb.clear();
        if (c == -1)
            throw new BasicException(E.OUT_OF_DATA);
        if (c == '"') {
            peek();
            while (c != '"' && c != -1) {
                bsb.append(c);
                peek();
            }
            if (c == '"') //跳过逗号
                peek();
        } else {
            while (c != ',') {
                bsb.append(c);
                peek();
            }
        }
        return bsb.toString();
    }
    
    public S readS() throws BasicException {
        peek();
        bsb.clear();
        if (c == -1)
            throw new BasicException(E.OUT_OF_DATA);
        if (c == '"') {
            peek();
            while (c != '"' && c != -1) {
                bsb.append(c);
                peek();
            }
            if (c == '"') //跳过逗号
                peek();
        } else {
            while (c != ',') {
                bsb.append(c);
                peek();
            }
        }
        return bsb.toS();
    }
    
    String s;
    
    public double readDouble() throws BasicException {
        peek();
        bsb.clear();
        if (c == -1)
            throw new BasicException(E.OUT_OF_DATA);
        while (c != ',') {
            bsb.append(c);
            peek();
        }
        s = bsb.toString();
        if (s.length() > 0) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                throw new BasicException(E.SYNTAX);
            }
        } else
            return 0d;
    }
    
    public void mark(int stm) {
        if (!stmt.contains(stm)) {
            stmt.add(stm);
            mark.add(cap);
        }
    }
    
    public void restore() {
        pos = 0;
    }
    
    public void restore(int stm) {
        int i;
        for (i = 0; i < stmt.size(); i++) {
            if (stmt.get(i) >= stm)
                break;
        }
        if (i < stmt.size())
            pos = mark.get(i);
        else
            pos = cap;
    }
    
    public String toString() {
        return "data:" + new String(z, 0, cap);
    }
}

/**
 * 随机文件记录
 */
class Record {
    public int total;
    public Integer[] size;
    public Access[] acc;
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("record[").append(total).append("]:{");
        for (int i = 0; i < size.length; i++) {
            sb.append(acc).append("[").append(size).append("]");
            if (i < size.length - 1)
                sb.append(",");
        }
        return sb.append("}").toString();
    }
}
/**
 * 文件状态
 */
class FileState {
    public static final int INPUT = 1, OUTPUT = 2, APPEND = 3, RANDOM = 4, BINARY = 5, CLOSE = 0;
    
    public int state; //文件状态
    public int size; //文件大小
    
    Record rec; //随机文件的记录
    public int len; //随机文件缓冲区大小
    
    public void close() {
        state = CLOSE;
        len = 0;
    }
}