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

##  在x-y平面上的2D简化
当Ax、Ay确定时，Az有两种可能的值，二者相差PI，确定了设备屏幕的朝向是向上还是向下。大多数情况下，我们只关心Ax、Ay（因为程序UI位于x-y平面？），而忽略Az，例如，Android的屏幕自动旋转功能，不管使用者是低着头看屏幕（屏幕朝上）、还是躺在床上看（屏幕朝下），UI始终是底边最接近地心的方向

那么我们设Gx与Gy的矢量和为g'（即：g在x-y平面上的投影），将计算简化到x-y 2D平面上。记y轴相对于g'的偏角为A，以A来描述设备的方向。以逆时针方向为正，A的范围为[0, 2*PI)
有：

g'^2=Gx^2+Gy^2

Gy=g'*cos(A)

Gx=g'*sin(A)

则：

g'=sqrt(Gx^2+Gy^2)

A=arccos(Gy/g')

由于arccos函数值范围为[0, PI]；而A>PI时，Gx=g'*sin(A)<0，因此，根据Gx的符号分别求A的值为：

当Gx>=0时，A=arccos(Gy/g')

当Gx<0时，A=2*PI-arccos(Gy/g')

注意：由于cos函数曲线关于直线x=n*PI 对称，因此arccos函数的曲线如果在y轴方向[0, 2*PI]范围内补全的话，则关于直线y=PI对称，因此有上面当Gx<0时的算法

## 考虑应用程序的屏幕旋转
前面计算出了Android设备的“物理屏幕”相对于地面的旋转角度，而应用程序的UI又相对于“物理屏幕”存在0、90、180、270度4种可能的旋转角度，要综合考虑进来。也就是说：

UI相对于地面的旋转角度=物理屏幕相对于地面的旋转角度-UI相对于物理屏幕的旋转角度

Android应用获取屏幕旋转角度的方法为：
```
int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();  
int degree= 90 * rotation;  
float rad = (float)Math.PI / 2 * rotation;
```
根据上面的算法，我写了一个“不倒翁”的Demo，当设备旋转时，不倒翁始终是站立的。软件市场上不少“水平尺”一类的应用，其实现原理应该是与此相同的
- Activity实现了SensorEventListener，并且注册到SensorManager。同时设置屏幕方向固定为LANDSCAPE：

```
private GSensitiveView gsView;  
    private SensorManager sm;  
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);  
        super.onCreate(savedInstanceState);  
        gsView = new GSensitiveView(this);  
        setContentView(gsView);  
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);  
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);  
    }  
    @Override  
    protected void onDestroy() {  
        sm.unregisterListener(this);  
        super.onDestroy();  
    }
```

- 当g-sensor数据变化时的回调如下。这里就是根据我们前面推论的算法计算出UI旋转的角度，并且调用GSensitiveView.setRotation()方法通知View更新
```
public void onSensorChanged(SensorEvent event) {  
        if (Sensor.TYPE_ACCELEROMETER != event.sensor.getType()) {  
            return;  
        }  
        float[] values = event.values;  
        float ax = values[0];  
        float ay = values[1];  
        double g = Math.sqrt(ax * ax + ay * ay);  
        double cos = ay / g;  
        if (cos > 1) {  
            cos = 1;  
        } else if (cos < -1) {  
            cos = -1;  
        }  
        double rad = Math.acos(cos);  
        if (ax < 0) {  
            rad = 2 * Math.PI - rad;  
        }  
        int uiRot = getWindowManager().getDefaultDisplay().getRotation();  
        double uiRad = Math.PI / 2 * uiRot;  
        rad -= uiRad;  
        gsView.setRotation(rad);  
    }
```

- GSensitiveView是扩展ImageView的自定义类，主要是根据旋转角度绘制图片：
```
private static class GSensitiveView extends ImageView {  
        private Bitmap image;  
        private double rotation;  
        private Paint paint;  
        public GSensitiveView(Context context) {  
            super(context);  
            BitmapDrawable drawble = (BitmapDrawable) context.getResources().getDrawable(R.drawable.budaow);  
            image = drawble.getBitmap();  
            paint = new Paint();  
        }  
        @Override  
        protected void onDraw(Canvas canvas) {  
            // super.onDraw(canvas);  
            double w = image.getWidth();  
            double h = image.getHeight();  
            Rect rect = new Rect();  
            getDrawingRect(rect);  
            int degrees = (int) (180 * rotation / Math.PI);  
            canvas.rotate(degrees, rect.width() / 2, rect.height() / 2);  
            canvas.drawBitmap(image, //  
                    (float) ((rect.width() - w) / 2),//    
                    (float) ((rect.height() - h) / 2),//    
                    paint);  
        }  
        public void setRotation(double rad) {  
            rotation = rad;  
            invalidate();  
        }  
    }
```
