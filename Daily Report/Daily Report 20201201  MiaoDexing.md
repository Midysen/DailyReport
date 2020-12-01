# 关于OverScroller平滑减速滑动的分析

scroller用的地方非常多，几乎所有可以滑动的控件都有scroller存在的身影。它源码里究竟写了什么，才让它具备这样强大的功能呢？
其实，scroller的源码中绝大部分代码都是在写算法，全是在计算…


开始就有一部分计算：
```
private static final float INFLEXION = 0.35f; 
	private static final float START_TENSION = 0.5f;
	private static final float END_TENSION = 1.0f;
	private static final float P1 = START_TENSION * INFLEXION;
	private static final float P2 = 1.0f - END_TENSION * (1.0f - INFLEXION);

	private static final int NB_SAMPLES = 100;
	private static final float[] SPLINE_POSITION = new float[NB_SAMPLES + 1];
	private static final float[] SPLINE_TIME = new float[NB_SAMPLES + 1];

	static {
		float x_min = 0.0f;
		float y_min = 0.0f;
		for (int i = 0; i < NB_SAMPLES; i++) {
			final float alpha = (float) i / NB_SAMPLES;

			float x_max = 1.0f;
			float x, tx, coef;
			while (true) {
                x = x_min + (x_max - x_min) / 2.0f;
                coef = 3.0f * x * (1.0f - x);
                tx = coef * ((1.0f - x) * P1 + x * P2) + x * x * x;
				if (Math.abs(tx - alpha) < 1E-5) break;
				if (tx > alpha) x_max = x;
				else x_min = x;
			}
            SPLINE_POSITION[i] = coef * ((1.0f - x) * START_TENSION + x) + x * x * x;

			float y_max = 1.0f;
			float y, dy;
			while (true) {
				y = y_min + (y_max - y_min) / 2.0f;
				coef = 3.0f * y * (1.0f - y);
				dy = coef * ((1.0f - y) * START_TENSION + y) + y * y * y;
				if (Math.abs(dy - alpha) < 1E-5) break;
				if (dy > alpha) y_max = y;
				else y_min = y;
			}
            SPLINE_TIME[i] = coef * ((1.0f - y) * P1 + y * P2) + y * y * y;
		}
        SPLINE_POSITION[NB_SAMPLES] = SPLINE_TIME[NB_SAMPLES] = 1.0f;
	}


```

各种固定系数，各种样本值，算出两组典型值，学名叫作：SPLINE—三次样条数据插值。
这两组值如果画在二维坐标轴上可能会明显些，这里把数组索引作为横坐标，值作为纵坐标；
首先是这个时间的数据SPLINE_TIME：
![avatar](https://img-blog.csdnimg.cn/20190330210014388.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2lmbXlsb3ZlMjAxMQ==,size_16,color_FFFFFF,t_70)


再来是位置的数据SPLINE_POSITION:


![avatar](https://img-blog.csdnimg.cn/2019033021010219.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2lmbXlsb3ZlMjAxMQ==,size_16,color_FFFFFF,t_70)

基本可以理解成这两组值为Scroller提供了"平滑滑动"的依据。
而在源码中这两组值在computeScrollOffset方法中会反复使用以判断当前滑动是否停止。
computeScrollOffset的源码是这样的：
```
 public boolean computeScrollOffset() {
        if (mFinished) {
            return false;
        }

        int timePassed = (int)(AnimationUtils.currentAnimationTimeMillis() - mStartTime);
    
        if (timePassed < mDuration) {
            switch (mMode) {
            case SCROLL_MODE:
                final float x = mInterpolator.getInterpolation(timePassed * mDurationReciprocal);
                mCurrX = mStartX + Math.round(x * mDeltaX);
                mCurrY = mStartY + Math.round(x * mDeltaY);
                break;
            case FLING_MODE:
                final float t = (float) timePassed / mDuration;
                final int index = (int) (NB_SAMPLES * t);
                float distanceCoef = 1.f;
                float velocityCoef = 0.f;
                if (index < NB_SAMPLES) {
                    final float t_inf = (float) index / NB_SAMPLES;
                    final float t_sup = (float) (index + 1) / NB_SAMPLES;
                    final float d_inf = SPLINE_POSITION[index];
                    final float d_sup = SPLINE_POSITION[index + 1];
                    velocityCoef = (d_sup - d_inf) / (t_sup - t_inf);
                    distanceCoef = d_inf + (t - t_inf) * velocityCoef;
                }

                mCurrVelocity = velocityCoef * mDistance / mDuration * 1000.0f;
                
                mCurrX = mStartX + Math.round(distanceCoef * (mFinalX - mStartX));
                // Pin to mMinX <= mCurrX <= mMaxX
                mCurrX = Math.min(mCurrX, mMaxX);
                mCurrX = Math.max(mCurrX, mMinX);
                
                mCurrY = mStartY + Math.round(distanceCoef * (mFinalY - mStartY));
                // Pin to mMinY <= mCurrY <= mMaxY
                mCurrY = Math.min(mCurrY, mMaxY);
                mCurrY = Math.max(mCurrY, mMinY);

                if (mCurrX == mFinalX && mCurrY == mFinalY) {
                    mFinished = true;
                }
                break;
            }
        }
        else {
            mCurrX = mFinalX;
            mCurrY = mFinalY;
            mFinished = true;
        }
        return true;
    }

```

这个方法会根据已经滑动的时间、插值器、滑动速度、滑动系数、阻尼系数等值计算出当前是否已经滑动结束就行了。

实际应用中，经常会配合computeScroll来判断当前控件是否滑动完毕，由此实现自定义的滑动效果。
因为Scroller本身只负责计算，滑动行为是View本身调用scrollBy与scrollTo（准确地说是调用后者，因为scrollBy也是调用了scrollTo）发起的，是View内置的API，而不是Scroller。Scroller的存在是为了更好更美观的滑动，也就是为更好的滑动提供数据。
这一点在Scroller的构造方法中就可以看出个大概：
```
public Scroller(Context context, Interpolator interpolator, boolean flywheel) {
        mFinished = true;
        if (interpolator == null) {
            mInterpolator = new ViscousFluidInterpolator();
        } else {
            mInterpolator = interpolator;
        }
        mPpi = context.getResources().getDisplayMetrics().density * 160.0f;
        mDeceleration = computeDeceleration(ViewConfiguration.getScrollFriction());
        mFlywheel = flywheel;

        mPhysicalCoeff = computeDeceleration(0.84f); // look and feel tuning
    }

```

1. 插值器ViscousFluidInterpolator，ViscousFluid是什么意思？—“黏性流体”，这意味着默认的Scroller始终只提供平滑的滑动效果，就像流体那样的滑动效果。
2. mPpi，在SnapHelper的分析中有类似的概念，Pixels Per Inch，即每英寸像素数量。基本等价于DPI。
3. 用computeDeceleration分别计算出来了滑动系数与物理系数。


看一下computeDeceleration的计算：
```

 private float computeDeceleration(float friction) {
    	//public static final float GRAVITY_EARTH = 9.80665f;
        return SensorManager.GRAVITY_EARTH   // g (m/s^2)
                      * 39.37f               // inch/meter
                      * mPpi                 // pixels per inch
                      * friction;
    }
```


没有错，重力加速度都出现了，9.8，熟悉的数字; 39.37是英寸与米的换算，即一米为39.37英寸，再乘以传进来的摩擦系数，可以得出一个新的加速度，也就是一个用于阻力减速的加速度，单位为pixels/s^2。

那么，在一个DPI为480的手机上，这两个值可以计算出来；
当使用friction=ViewConfiguration.getScrollFriction()值（为0.015）时，这个加速度为：

```
0.015 * 9.80665 * 39.37 * 480
                  = 2779.8322356
```

当使用friction=0.84时，这个加速度为：

```
0.84 * 9.80665 * 39.37 * 480
                  = 155670.6051936

```

实际应用中，可以使用API进行调整friction：

```
 public final void setFriction(float friction) {
        mDeceleration = computeDeceleration(friction);
        mFlingFriction = friction;
    }

```


这两个值分别会用在什么地方？首先看前者：

```
public float getCurrVelocity() {
        return mMode == FLING_MODE ?
                mCurrVelocity : mVelocity - mDeceleration * timePassed() / 2000.0f;
    }

```


还是提供数据用，这里是用于计算当前的速度。

另一个：
```
    private double getSplineDeceleration(float velocity) {
        return Math.log(INFLEXION * Math.abs(velocity) / (mFlingFriction * mPhysicalCoeff));
    }
    
    private double getSplineFlingDistance(float velocity) {
        final double l = getSplineDeceleration(velocity);
        final double decelMinusOne = DECELERATION_RATE - 1.0;
        return mFlingFriction * mPhysicalCoeff * Math.exp(DECELERATION_RATE / decelMinusOne * l);
    }

```
两个私有方法，根据传进的速度参数算出将要fling的距离，是的，getSplineFlingDistance仅在fling方法中调用了。依旧是为了计算。

至于其他如startScroll、fling方法都是为Scroller初始化与更新数据用的，这样在视图绘制时，才有数据可以依据。
