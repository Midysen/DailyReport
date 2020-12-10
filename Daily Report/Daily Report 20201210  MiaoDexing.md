 - adb  shell  text 

该命令主要是用于向获得焦点的EditText控件输入内容，

Eg : adb shell input text  "hello,world"

    adb input keyevent 

           该命令主要是向系统发送一个按键指令，实现模拟用户在键盘上的按键动作:

Eg : 

- adb shell input keyevent 26

or

adb shell input keyevent "KEYCODE_POWER"

关于键值宏的定义在 KeyEvent.java文件中有定义，一般都会用默认值，这里也包括黑屏手势的宏定义。

- adb shell input tap 

该命令是用于向设备发送一个点击操作的指令，参数是<x> <y>坐标

adb shell input tap 100 100

    input swipe [duration(ms)] 

向设备发送一个滑动指令，并且可以选择设置滑动时长。

//滑动操作

adb shell input swipe 100 100 200 200  300 //从 100 100 经历300毫秒滑动到 200 200

//长按操作

adb shell input swipe 100 100 100 100  1000 //在 100 100 位置长按 1000毫秒

    press  rool  是轨迹球操作，在手机上没啥用。
