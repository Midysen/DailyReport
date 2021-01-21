# 陀螺仪
陀螺传感器的原始数据由三个float值组成 ，指定器件沿X，Y和Z轴的角速度。每个值的单位是每秒弧度。在沿着任何轴的逆时针旋转的情况下，与该轴相关联的值将为正。在顺时针旋转的情况下，它将为负。

习惯上以机体正前方(x轴)、机体正右方(y轴)、机体垂直正上方(z轴)建立机体坐标轴。

![avatar](https://imgconvert.csdnimg.cn/aHR0cDovL2ZpbGUuZWxlY2ZhbnMuY29tL3dlYjEvTTAwLzU2LzMwL280WUJBRnM4YkFHQUE1XzZBQUJRZms4WEk0STk0OS5wbmc)

# opengl中的glRotatef 函数
函数原型：glRotatef(GLfloat angle,GLfloat x,GLfloat y,GLfloat z)

该函数用来设置opengl中绘制实体的自转方式，即物体如何旋转

参数说明：

- angle：旋转的角度，单位为度；

- x,y,z表示绕着那个轴旋转，如果取值都为0，则表示默认的绕x轴逆时针旋转。

x,y为0，z不为0时，表示绕z轴旋转；x,z为0，y不为0时，表示绕y轴旋转；y,z为0，x不为0，表示绕x轴旋转。

旋转的逆顺时针是通过x，y，z值得正负来确定的：取值为正时，表示逆时针旋转；取值为负时，表示顺时针旋转。

例：glRotatef(30,0,-1,0)；

表示绕y轴顺时针方向旋转30度。

关于逆时针与顺时针，可用右手定则：

即手握住某个坐标轴，大拇指指向某轴的正方向，其余四个手指的弯曲方向即为绕某轴旋转的逆时针方向；反之为顺时针方向。
