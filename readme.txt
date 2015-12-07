input时Ctrl切换中英输入法，英文输入法可以使用Shift输入大写字母和符号。中文输入法是google输入法^_^

关键字、变量不区分大小写

查找WQX上特殊按键的对应键可以查看res\config.ini

======================
和gvb的几点不同：
1. print "a"spc(2)"b"
这在gvb上会显示为a   b（三个空格），而模拟器会显示两个空格

2. locate 5,20:print "a";
在gvb上屏幕会上滚一行，模拟器不会

3. gvb在if中不能return，模拟器没有这个限制

4. 新语法：

●延时语句：
SLEEP milliseconds
用于控制游戏的帧率。。。

●屏幕任意位置绘制字符串
TEXTOUT  S$, x, y [, isSmall [, mode] ]
isSmall	=1 小字体12x12； =0 大字体16x16
	默认大字体
mode	bit2  =1 透明；=0 不透明	bit0~bit1同gvb绘图语句的绘图模式
	默认不透明，OR模式

●绘制图片
PAINT addr, x, y, w, h [, mode]
addr	是存储图片8bit点阵数据的内存地址
mode	同textout

●批量载入数据到内存
LOAD addr, data1 [, ... ]
把若干个0-255之间的数载入到addr开始的内存

●二进制文件操作
☆OPEN S$ FOR BINARY AS #n

☆FREAD #n, addr, size
从文件读取size字节到addr开始的内存

☆FWRITE #n, addr, size
从addr开始的内存写入size字节到文件

☆FSEEK #n, pos
定位文件指针

☆FGET #n, id / id$ / id% [, ... ]
从文件读取若干个基本类型变量，实数占8字节，整数占2字节，字符串则根据原先的字符串长决定读取的字节数

☆FPUT #n, id / id$ / id% [, ... ]
写入若干个基本类型变量到文件

☆FPUTC #n, expr / expr$ [, ... ]
写入一字节到文件。若表达式为字符串，则写入字符串的第一个字节

☆FGETC(n)函数
从文件获取一字节（数值）

☆FTELL(n)函数
获取当前文件指针

●POINT(x, y)函数
判断某点是否为黑，如果坐标超出屏幕范围则总是返回1

●CHECKKEY(ascii)函数
判断某个键是否被按下，ascii是按键对应的ascii码