package gui;

import io.*;
import core.*;
import common.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.*;

import java.io.*;
import java.lang.Thread.State;

public class Form extends JFrame implements ActionListener {
    Graph graph = new Graph();
    Text text = new Text(graph);
    Screen scr = new Screen(text);
    Controller ctrl = new Controller(scr, this);
    Basic b = new Basic(ctrl, ctrl, this);

    static final String title = "GVBASIC模拟器";
    public Form() {
        super(title);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (exec != null && exec.isAlive()) {
                    exec.interrupt();
                    try {
                        exec.join();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
                if (imd != null)
                    imd.dispose();
                dispose();
                System.exit(0);
            }
        });
        setLayout(new GridBagLayout());
        
        GridBagConstraints cs = new GridBagConstraints();
        cs.insets = new Insets(3, 2, 3, 2);
        cs.anchor = GridBagConstraints.BASELINE_LEADING;
        cs.weightx = .3;
        //打开
        btnLoad.addActionListener(this);
        btnLoad.setFocusable(false); //设置为无法获取焦点，防止按回车时触发按钮
        add(btnLoad, cs);
        //运行
        cs.gridy = 0;
        cs.gridx = 1;
        btnRun.addActionListener(this);
        btnRun.setFocusable(false);
        add(btnRun, cs);
        //截图
        cs.gridy = 0;
        cs.gridx = 2;
        btnCap.addActionListener(this);
        btnCap.setFocusable(false);
        add(btnCap, cs);
        //屏幕
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 3;
        add(scr, cs);
        //信息栏
        cs.gridy = 2;
        infoLabel.setPreferredSize(new Dimension(320, 20));
        infoLabel.setBorder(new EtchedBorder());
        add(infoLabel, cs);
        //金山游侠
        cs.gridy = 3;
        btnHack.addActionListener(this);
        btnHack.setFocusable(false);
        add(btnHack, cs);
        //图形浏览
        cs.gridx = 2;
        cs.gridy = 3;
        btnImage.addActionListener(this);
        btnImage.setFocusable(false);
        add(btnImage, cs);
        
        //变量列表
        vartable = new JTable(new VTableModel());
        jvt = new JScrollPane(vartable);
        jvt.setPreferredSize(new Dimension(320, 180));
        vartable.setShowVerticalLines(false);
        vartable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        vartable.setToolTipText("双击条目修改变量");
        vartable.addMouseListener(new VarListener());
        
        pack();
        Dimension sz = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(new Point((sz.width - getWidth()) / 2, (int) ((sz.height - getHeight()) * .309)));
        setVisible(true);
        
        jf.setCurrentDirectory(new File("bas"));
        jf.setFileFilter(new FileNameExtensionFilter("文本文件(*.txt)", "txt"));
        
        mem = ctrl.getRAM();
        
        //监听全局键盘事件
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            public void eventDispatched(AWTEvent event) {
                if (!isFocused())
                    return;
                KeyEvent e = (KeyEvent) event;
                int k = e.getKeyCode();
                if (k == 192) //`键
                    k = 96;
//                else if (k >= 112 && k <= 123 || k >= 33 && k <= 40) //F快捷键
//                    k = 0;
                switch (e.getID()) {
                case KeyEvent.KEY_PRESSED:
                    if (k > 0 && k < 256) {
                        key = k;
                        tmpk = Utilities.convertKeyCode(k);
                        mem[199] = (byte) (tmpk | 0x80);
                        tmpk = Utilities.mapWQXKey(tmpk);
                        if (tmpk != 0) {
                            mem[191 + (tmpk >>> 8)] &= ~tmpk & 0xff;
                        }
                        keyList[k] = true;
                    }
                    break;
                case KeyEvent.KEY_RELEASED:
                    if (k > 0 && k < 256) {
                        key = 0;
                        tmpk = Utilities.mapWQXKey(Utilities.convertKeyCode(k));
                        if (tmpk != 0) {
                            mem[191 + (tmpk >>> 8)] |= tmpk & 0xff;
                        }
                        keyList[k] = false;
                    }
                }
            }
        }, AWTEvent.KEY_EVENT_MASK);
    }
    
    public String getDefaultTitle() {
        return title;
    }
    
    TButton btnLoad = new TButton("打开"), btnRun = new TButton("运行"), btnHack = new TButton("变量"),
            btnCap = new TButton("截图"), btnImage = new TButton("图形浏览");
    
    Thread exec;
    
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
        case "打开":
            //打开文件,检查b.load返回值
            error("");
            loadFile();
            btnRun.setText("运行");
            break;
        case "停止":
            exec.interrupt();
            try {
                exec.join();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            break;
        case "运行": //若程序暂停，则设置basic运行标志；若程序刚载入，则运行
            if (exec == null || exec.getState() == State.TERMINATED)
                return;
            if (!exec.isAlive()) {
                exec.start();
                btnLoad.setText("停止");
            } else
                b.cont();
            btnRun.setText("暂停");
            error("");
            break;
        case "暂停":
            b.pause();
            btnRun.setText("运行");
            break;
        case "变量":
            if (vtShow) {
                remove(jvt);
                scr.requestFocus(); //焦点转移到屏幕，否则无法检测按键
            } else {
                GridBagConstraints cs = new GridBagConstraints();
                cs.gridy = 4;
                cs.gridwidth = 3;
                add(jvt, cs);
            }
            vtShow = !vtShow;
            pack();
            break;
        case "截图":
            screenshot();
            break;
        case "图形浏览":
            if (imd == null)
                imd = new ImageDialog(this);
            break;
        }
    }
    
    JFileChooser jf = new JFileChooser();
    
    void loadFile() {
        int r = jf.showOpenDialog(this);
        if (r == JFileChooser.APPROVE_OPTION) {
            File f = jf.getSelectedFile();
            if (!f.exists()) {
                error("File not exist!");
                return;
            }
            InputStream in;
            try {
                in = new BufferedInputStream(new FileInputStream(f));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                error("File read failed!");
                return;
            }
            if (!b.load(in)) {
                error("File load failed!");
            }
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //thread只能start一次，因此每次新建一个
            exec = new Executor();
        }
    }
    
    /**
     * 信息提示栏
     */
    JLabel infoLabel = new JLabel();
    
    /**
     * 信息栏显示错误信息
     * @param s 信息
     */
    void error(String s) {
        infoLabel.setForeground(Color.red);
        infoLabel.setText(s);
        infoLabel.repaint();
    }
    
    /**
     * 图形浏览器
     */
    public ImageDialog imd;
    
    /**
     * 变量列表。修改变量Map后，需要revalidate来更新列表
     */
    public JTable vartable;
    JScrollPane jvt;
    boolean vtShow;
    
    class VTableModel extends AbstractTableModel {
        String[] colName =  {"变量名", "变量类型", "值"};
        
        public String getColumnName(int column) {
            return colName[column];
        }

        @Override
        public int getRowCount() {
            synchronized (b.vars) {
                return b.vars.size();
            }
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            synchronized (b.vars) {
                Id id = b.vars.keySet().toArray(new Id[0])[rowIndex];
                
                switch (columnIndex) {
                case 0:
                    return id.id.toUpperCase();
                case 1:
                    switch (id.type) {
                    case Id.REAL:
                        return "实数";
                    case Id.INTEGER:
                        return "整数";
                    case Id.STRING:
                        return "字符串";
                    case Id.ARRAY:
                        Object v = ((Array<?>) b.vars.get(id)).value;
                        if (v instanceof Double[])
                            return "实数数组";
                        else if (v instanceof Integer[])
                            return "整数数组";
                        else if (v instanceof S[])
                            return "字符串数组";
                        else
                            return "未知类型数组";
                    default:
                        return "未知类型";
                    }
                case 2:
                    switch (id.type) {
                    case Id.ARRAY:
                        return "...";
                    case Id.REAL:
                        return Utilities.realToString((Double) b.vars.get(id));
                    case Id.INTEGER: case Id.STRING:
                        return b.vars.get(id);
                    default:
                        return "???";
                    }
                default:
                    return null;
                }
            }
        }
    }
    
    class VarListener extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                Id id = b.vars.keySet().toArray(new Id[0])[vartable.getSelectedRow()];
                if (id.type == Id.ARRAY) {
                    new ArrayHacker(id.id.toUpperCase(), (Array<?>) b.vars.get(id));
                    return;
                }
                synchronized (b.vars) {
                    String s = JOptionPane.showInputDialog(Form.this, "请输入新值",
                            vartable.getModel().getValueAt(vartable.getSelectedRow(), 2));
                    if (s == null)
                        return;
                    switch (id.type) {
                    case Id.INTEGER:
                        try {
                            b.vars.put(id, Integer.parseInt(s));
                        } catch (NumberFormatException e1) {
                            error("Modify variable failed!");
                        }
                        break;
                    case Id.REAL:
                        try {
                            b.vars.put(id, Double.parseDouble(s));
                        } catch (NumberFormatException e1) {
                            error("Modify variable failed!");
                        }
                        break;
                    case Id.STRING:
                        b.vars.put(id, new S(s));
                    }
                }
            }
        }
        class ArrayHacker extends JDialog implements ActionListener, ChangeListener {
            JSpinner[] sp;
            Array<?> arry;
            JTextField tf = new JTextField(6);
            TButton b = new TButton("确定");
            
            public ArrayHacker(String name, Array<?> arr) {
                super(Form.this, "", true);
                setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                setLayout(new FlowLayout());
                
                arry = arr;
                int t = arr.bound.length;
                sp = new JSpinner[t];
                StringBuffer sb = new StringBuffer(name);
                sb.append("(");
                for (int i = 0; i < t; i++) {
                    add(sp[i] = new JSpinner(new SpinnerNumberModel(0, 0, (int) arr.bound[i], 1)));
                    sp[i].addChangeListener(this);
                    sb.append(arr.bound[i]);
                    if (i < t - 1)
                        sb.append(", ");
                }
                sb.append(")");
                
                setTitle(sb.toString());
                tf.setText(arr.value[0].toString());
                add(tf);
                b.addActionListener(this);
                add(b);
                pack();
                setLocationRelativeTo(Form.this);
                setVisible(true);
            }
            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent e) {
                int t = 0, z = arry.bound.length;
                for (int i = 0; i < z; i++)
                    t += arry.base[i] * (Integer) sp[i].getValue();
                synchronized (arry) {
                    Object v = arry.value;
                    if (v instanceof Double[]) {
                        try {
                            ((Array<Double>) arry).value[t] = Double.parseDouble(tf.getText());
                        } catch (NumberFormatException e1) {
                            e1.printStackTrace();
                        }
                    } else if (v instanceof Integer[]) {
                        try {
                            ((Array<Integer>) arry).value[t] = Integer.parseInt(tf.getText());
                        } catch (NumberFormatException e1) {
                            e1.printStackTrace();
                        }
                    } else {
                        ((Array<S>) arry).value[t] = new S(tf.getText());
                    }
                }
                dispose();
            }
            public void stateChanged(ChangeEvent e) {
                int t = 0, z = arry.bound.length;
                for (int i = 0; i < z; i++)
                    t += arry.base[i] * (Integer) sp[i].getValue();
                tf.setText(arry.value[t].toString());
            }
        }
    }
    
    static int shot_id = 1;
    static byte[] BMPHeader = {
        0x42, 0x4D, 0x7E, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x3E, 0x00, 0x00, 0x00, 0x28, 0x00, 
        0x00, 0x00, (byte) 0xA0, 0x00, 0x00, 0x00, 0x50, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x00, 
        0x00, 0x00, 0x40, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00
    };
    void screenshot() {
        try {
            OutputStream o = new BufferedOutputStream(new FileOutputStream("Screenshot_" + shot_id + ".bmp"));
            shot_id++;
            o.write(BMPHeader);
            byte[] b = Utilities.toByteData(graph.copy(), 160, 80);
            for (int j = 79; j >= 0; j--) {
                for (int i = 0; i < 20; i++) {
                    o.write(b[i + j * 20]);
                }
            }
            o.close();
        } catch (Exception e) {
            error("Capture screen failed!");
            e.printStackTrace();
        }
    }
    
    public Screen getScreen() {
        return scr;
    }
    
    byte[] mem;
    int key, tmpk;
    boolean[] keyList = new boolean[256];
    
    /**
     * 获取pc键位，注意<b>字母是大写</b>。文本模式下等待时光标闪烁
     * @return PC键值
     */
    public int inkey() throws InterruptedException {
        return inkey(true);
    }
    
    /**
     * 是否有键按下
     */
    public boolean keyPressed() {
        return key != 0;
    }
    
    /**
     * 获取pc键位。注意<b>字母是大写</b>
     * @param ctrlFlash 是否控制等待时光标的闪烁(文本模式下等待按键时光标会闪烁)
     * @return pc键位
     */
    public int inkey(boolean ctrlFlash) throws InterruptedException {
        if (ctrlFlash)
            scr.flash();
        while (key == 0) {
            Thread.sleep(50);
        }
        mem[199] &= 0x7f;
        int k = key;
        key = 0;
        if (ctrlFlash)
            scr.stopFlash();
        return k;
    }
    
    /**
     * 判断某键是否被按下
     * @param rawKey pc键值
     */
    public boolean checkKey(int rawKey) {
        return keyList[rawKey];
    }
    
    class Executor extends Thread {
        public void run() {
            error(b.run()); //需要处理返回的信息
            scr.stopFlash();
            scr.repaint();
            btnRun.setText("运行");
            btnLoad.setText("打开");
        }
    }
    
    public static void main(String[] args) throws Exception {
//        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        new Form();
    }
}

class ImageDialog extends JDialog {
    static BufferedImage bi;
    static Color sel = new Color(0xff, 0, 0, 0xd0);
    static {
        try {
            bi = ImageIO.read(new File("res/image.bmp"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    int lx = -1, ly;
    byte[] gb = new byte[2];
    JPanel jp = new JPanel() {
        {
            setPreferredSize(new Dimension(bi.getWidth(), bi.getHeight()));
            addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    int x = e.getX() >>> 4, y = e.getY() >>> 4, t = y * 20 + x;
                    if (t < 527) {
                        lx = x << 4;
                        ly = y << 4;
                        gb[0] = (byte) (0xf8 + t / 94);
                        gb[1] = (byte) (0xa1 + t % 94);
                        //System.out.println(t+" " +Integer.toHexString(((gb[0] & 0xff) << 8) + (gb[1] & 0xff)));
                        jp2.repaint();
                        repaint();
                    }
                    if (e.getClickCount() == 2) {
                        Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new StringSelection(new String(gb)), null);
                    }
                }
            });
            setToolTipText("双击将图形GB码复制到剪贴板");
        }
        public void paintComponent(Graphics g) {
            g.drawImage(bi, 0, 0, null);
            if (lx >= 0) {
                g.setColor(sel);
                g.drawRect(lx, ly, 15, 15);
            }
        }
    }, jp2 = new JPanel() {
        {
            setPreferredSize(new Dimension(48, 60));
        }
        public void paintComponent(Graphics g) {
            g.clearRect(0, 0, getWidth(), getHeight());
            if (lx >= 0) {
                g.drawImage(bi.getSubimage(lx, ly, 16, 16), 0, 0, 48, 48, null);
                g.setFont(new Font("Consolas", Font.BOLD, 16));
                g.drawString(Integer.toHexString(((gb[0] & 0xff) << 8) + (gb[1] & 0xff)).toUpperCase(), 6, 59);
            }
        }
    };
    
    public ImageDialog(JFrame j) {
        super(j, "图形浏览器");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                ((Form) getOwner()).imd = null;
                dispose();
            }
        });
        
        setLayout(new FlowLayout());
        
        add(jp);
        add(jp2);
        
        pack();
        setLocationRelativeTo(j);
        setVisible(true);
    }
}