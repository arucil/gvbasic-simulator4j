# gvbasic-simulator4j

java版本的gvbasic模拟器。

## 特性

- input时Ctrl切换中英输入法, 英文输入法可以使用Shift输入大写字母和符号。  
    中文输入法是google输入法^_^

- 关键字、变量不区分大小写
- 查找WQX上特殊按键的对应键可以查看res\config.ini

## 和真实gvbasic的不同

1. `print "a"spc(2)"b"`  
    这在gvb上会显示为a&nbsp;&nbsp;&nbsp;b(三个空格), 而模拟器会显示两个空格

2. `locate 5,20:print "a";`  
    在gvb上屏幕会上滚一行, 模拟器不会

3. gvb在if中不能return，模拟器没有这个限制

## 新语法
- 延时语句  
    `SLEEP milliseconds`  
    用于控制游戏的帧率。。。

- 屏幕任意位置绘制字符串  
    `TEXTOUT  S$, x, y [, isSmall [, mode] ]`  
    `isSmall`&nbsp;&nbsp;&nbsp;&nbsp;`=1` 小字体12x12； `=0` 大字体16x16  
    &nbsp;&nbsp;&nbsp;&nbsp;默认大字体  
    `mode`&nbsp;&nbsp;&nbsp;&nbsp;`bit2  =1` 透明; `=0` 不透明	`bit0~bit1` 同gvb绘图语句的绘图模式  
    &nbsp;&nbsp;&nbsp;&nbsp;默认不透明, OR模式
    
- 绘制图片  
    `PAINT addr, x, y, w, h [, mode]`  
    `addr`&nbsp;&nbsp;&nbsp;&nbsp;是存储图片8bit点阵数据的内存地址  
    `mode`&nbsp;&nbsp;&nbsp;&nbsp;同Lava的TextOut

- 批量载入数据到内存  
    `LOAD addr, data1 [, ... ]`  
    把若干个0-255之间的数载入到addr开始的内存

- 二进制文件操作  
    - 语句
        -  `OPEN S$ FOR BINARY AS #n`  
            打开二进制文件
        - `FREAD #n, addr, size`  
            从文件读取size字节到addr开始的内存
        - `FWRITE #n, addr, size`  
            从addr开始的内存写入size字节到文件
        - `FSEEK #n, pos`  
            定位文件指针
        - `FGET #n, id / id$ / id% [, ... ]`  
            从文件读取若干个基本类型变量.  
            实数占8字节, 整数占2字节, 字符串则根据原先的字符串长决定读取的字节数
        - `FPUT #n, id / id$ / id% [, ... ]`  
            写入若干个基本类型变量到文件
        - `FPUTC #n, expr / expr$ [, ... ]`  
    - 函数
        - `FGETC(n)`  
            从文件获取一字节（数值）
        - `FTELL(n)`  
            获取当前文件指针
        - `POINT(x, y)`  
            判断某点是否为黑，如果坐标超出屏幕范围则总是返回1
        - `CHECKKEY(ascii)`  
            判断某个键是否被按下，ascii是按键对应的ascii码
    