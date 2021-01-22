# 陀螺仪
陀螺传感器的原始数据由三个float值组成 ，指定器件沿X，Y和Z轴的角速度。每个值的单位是每秒弧度。在沿着任何轴的逆时针旋转的情况下，与该轴相关联的值将为正。在顺时针旋转的情况下，它将为负。
陀螺仪测量设备围绕 x、y、z 轴旋转的速率，单位是 rad/s。

习惯上以机体正前方(x轴)、机体正右方(y轴)、机体垂直正上方(z轴)建立机体坐标轴。
一般定义载体的右、前、上三个方向构成右手系

绕向前的轴旋转就是横滚角-Roll angle；

绕向右的轴旋转就是俯仰角-Pitch angle； 

绕向上的轴旋转就是航向角-Azimuth angle.

![blockchain](https://imgconvert.csdnimg.cn/aHR0cDovL2ZpbGUuZWxlY2ZhbnMuY29tL3dlYjEvTTAwLzU2LzMwL280WUJBRnM4YkFHQUE1XzZBQUJRZms4WEk0STk0OS5wbmc)
陀螺仪的 坐标系 与加速度传感器的相同。逆时针方向旋转用正值表示，也就是说，从 x、y、z 轴的正向位置观看处于原始方位的设备，如果设备逆时针旋转，将会收到正值。 这是标准的数学意义上的正向旋转定义，而与方向传感器定义的转动不同。

通常，陀螺仪的输出反映了转动时的角度变化速率。例如：
```
// 创建常量，把纳秒转换为秒。
private static final float NS2S = 1.0f / 1000000000.0f;
private final float[] deltaRotationVector = new float[4]();
private float timestamp;

public void onSensorChanged(SensorEvent event) {
  // 根据陀螺仪采样数据计算出此次时间间隔的偏移量后，它将与当前旋转向量相乘。
  if (timestamp != 0) {
    final float dT = (event.timestamp - timestamp) * NS2S;
    // 未规格化的旋转向量坐标值，。
    float axisX = event.values[0];
    float axisY = event.values[1];
    float axisZ = event.values[2];

    // 计算角速度
    float omegaMagnitude = sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);

    // 如果旋转向量偏移值足够大，可以获得坐标值，则规格化旋转向量
    // (也就是说，EPSILON 为计算偏移量的起步值。小于该值的偏移视为误差，不予计算。)
    if (omegaMagnitude > EPSILON) {
      axisX /= omegaMagnitude;
      axisY /= omegaMagnitude;
      axisZ /= omegaMagnitude;
    }

    // 为了得到此次取样间隔的旋转偏移量，需要把围绕坐标轴旋转的角速度与时间间隔合并表示。
    // 在转换为旋转矩阵之前，我们要把围绕坐标轴旋转的角度表示为四元组。
    float thetaOverTwo = omegaMagnitude * dT / 2.0f;
    float sinThetaOverTwo = sin(thetaOverTwo);
    float cosThetaOverTwo = cos(thetaOverTwo);
    deltaRotationVector[0] = sinThetaOverTwo * axisX;
    deltaRotationVector[1] = sinThetaOverTwo * axisY;
    deltaRotationVector[2] = sinThetaOverTwo * axisZ;
    deltaRotationVector[3] = cosThetaOverTwo;
  }
  timestamp = event.timestamp;
  float[] deltaRotationMatrix = new float[9];
  SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
    // 为了得到旋转后的向量，用户代码应该把我们计算出来的偏移量与当前向量叠加。
    // rotationCurrent = rotationCurrent * deltaRotationMatrix;
   }
}
```

标准的陀螺仪能够提供未经过滤的原始旋转数据，或是经过噪声及漂移修正的数据。 实际生活中，陀螺仪的噪声和漂移都会引入误差，这是需要补偿的。 通常你要利用其它传感器来确定漂移和噪声值，比如重力传感器或加速计。


- 4元数，旋转矩阵，卡尔曼滤波，互补滤波


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
