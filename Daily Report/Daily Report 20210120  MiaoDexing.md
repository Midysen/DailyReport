 # BeanAdapterService的push分支
 
 git push origin HEAD:refs/for/master


#  获取Android设备的方向，Sensor和SensorManager实现手机旋转角度

带有g-sensor的Android设备上可通过API获取到设备的运动加速度，应用程序通过一些假设和运算，可以从加速度计算出设备的方向
获取设备运动加速度的基本代码是：
```
 public class SensorActivity extends Activity implements SensorEventListener {
     private final SensorManager mSensorManager;
     private final Sensor mAccelerometer;

     public SensorActivity() {
         mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
         mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
     }

     protected void onResume() {
         super.onResume();
         mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
     }

     protected void onPause() {
         super.onPause();
         mSensorManager.unregisterListener(this);
     }

     public void onAccuracyChanged(Sensor sensor, int accuracy) {
     }

     public void onSensorChanged(SensorEvent event) {
     float[] values = event.values;  
                float gx = values[0];  
                float gy = values[1];  
                float gz = values[2]; 
     }
 }
 
```

SendorEventListener 通过 SendorEvent 回调参数获得当前设备在坐标系x、y、z轴上的加速度分量。SensorEvent 的 api doc 中定义了这里使用的坐标系为：

![avatar](http://www.jcodecraeer.com/uploads/allimg/121009/2210292623-0.png)


精确地说，Sensor Event 所提供的加速度数值，是设备以地球为参照物的加速度减去重力加速度的叠加后的值。我是这样理解的：当以重力加速度g向地面作自由落体运动时，手机处于失重状态，g-sensor以这种状态作为加速度的0；而当手机处于静止状态（相对于地面）时，为了抵御自由落体运动的趋势，它有一个反向（向上）的g的加速度。因此，得出一个结论：当设备处于静止或者匀速运动状态时，它有一个垂直地面向上的g的加速度，这个g投影到设备坐标系的x、y、z轴上，就是SensorEvent 提供给我们的3个分量的数值。在“设备处于静止或者匀速运动状态”的假设的前提下，可以根据SensorEvent所提供的3个加速度分量计算出设备相对于地面的方向。

前面所提到的“设备的方向”是一个含糊的说法。这里我们精确地描述设备方向为：以垂直于地面的方向为正方向，用设备坐标系x、y、z轴与正方向轴之间的夹角Ax、Ay、Az来描述设备的方向，如下图所示。可以看出，设备还有一个自由度，即：绕着正方向轴旋转，Ax、Ay、Az不变。但Ax、Ay、Az的约束条件，对于描述设备相对于正方向轴的相对位置已经足够了。如果需要完全约束设备相对于地面的位置，除了正方向轴外，还需要引入另一个参照轴，例如连接地球南、北极的地轴（如果设备上有地磁强度Sensor，则可满足该约束条件）。

![avatar](http://www.jcodecraeer.com/uploads/allimg/121009/2210292623-1.png)


Ax、Ay、Az的范围为[0, 2*PI)。例如，当Ay=0时，手机y轴竖直向上；Ay=PI时，手机y轴向下；Ay=PI/2时，手机水平、屏幕向上；Ay=3*PI/2时，手机水平、屏幕向下

根据3D矢量代数的法则，可知：

Gx=g*cos(Ax)

Gy=g*cos(Ay)

Gz=g*cos(Az)

g^2=Gz^2+Gy^2+Gz^2

因此，根据Gx、Gy、Gz，可以计算出Ax、Ay、Az
