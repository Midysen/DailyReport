# 如何惯性滑动起来
- 经过上一小节，我们已经知道如何绘制这一简单却又常见的柱形图了，但美中不足的就是没有 fling 的效果。
所以我们需要先借住 VelocityTracker 进行获取我们当前手指的滑动速度，但这里需要注意的是，要限制其最大和最小速度。
因为速度过快和过慢，都会导致交互效果不佳。获取代码如下:
```
mMaximumVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
mMinimumVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();

```

然后根据我们在 VelocityTracker小结 中的套路，进行获取手指离屏时的水平速度。以下是只保留 VelocityTracker 相关代码
```
/**
 * 控制屏幕不越界
 *
 * @param event
 * @return
 */
@Override
public boolean onTouchEvent(MotionEvent event) {
   	// 省略无关代码...
   	
    if (mVelocityTracker == null) {
        mVelocityTracker = VelocityTracker.obtain();
    }
    mVelocityTracker.addMovement(event);

    if (MotionEvent.ACTION_DOWN == event.getAction()) {
        // 省略无关代码...
    } else if (MotionEvent.ACTION_MOVE == event.getAction()) {
        // 省略无关代码...
    } else if (MotionEvent.ACTION_UP == event.getAction()) {
        // 计算当前速度， 1000表示每秒像素数等
        mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
        // 获取横向速度
        int velocityX = (int) mVelocityTracker.getXVelocity();

        // 速度要大于最小的速度值，才开始滑动
        if (Math.abs(velocityX) > mMinimumVelocity) {
        	// 省略无关代码...
        }

        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }
    return super.onTouchEvent(event);
}

```
获取完水平的速度，接下来我们需要进行真正的 fling 效果。通过一个线程来进行不断的 移动 画布，
从而达到滚动效果（RecycleView中的滚动也是通过线程达到效果，有兴趣的同学可以进入RecycleView 的源码进行查看，该线程类的名字为 ViewFlinger ）。

```
/**
 * 滚动线程
 */
private class FlingRunnable implements Runnable {

    private Scroller mScroller;

    private int mInitX;
    private int mMinX;
    private int mMaxX;
    private int mVelocityX;

    FlingRunnable(Context context) {
        this.mScroller = new Scroller(context, null, false);
    }

    void start(int initX,
               int velocityX,
               int minX,
               int maxX) {
        this.mInitX = initX;
        this.mVelocityX = velocityX;
        this.mMinX = minX;
        this.mMaxX = maxX;

        // 先停止上一次的滚动
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }

        // 开始 fling
        mScroller.fling(initX, 0, velocityX,
                0, 0, maxX, 0, 0);
        post(this);
    }

    @Override
    public void run() {

        // 如果已经结束，就不再进行
        if (!mScroller.computeScrollOffset()) {
            return;
        }

        // 计算偏移量
        int currX = mScroller.getCurrX();
        int diffX = mInitX - currX;

        // 用于记录是否超出边界，如果已经超出边界，则不再进行回调，即使滚动还没有完成
        boolean isEnd = false;

        if (diffX != 0) {

            // 超出右边界，进行修正
            if (getScrollX() + diffX >= mCanvasWidth - mViewWidth) {
                diffX = (int) (mCanvasWidth - mViewWidth - getScrollX());
                isEnd = true;
            }

            // 超出左边界，进行修正
            if (getScrollX() <= 0) {
                diffX = -getScrollX();
                isEnd = true;
            }
            
            if (!mScroller.isFinished()) {
                scrollBy(diffX, 0);
            }
            mInitX = currX;
        }

        if (!isEnd) {
            post(this);
        }
    }

    /**
     * 进行停止
     */
    void stop() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
    }
}
```

最后就是使用起这个线程，而使用的地方主要有两个点，
一个手指按下时（即MotionEvent.ACTION_DOWN）和手指抬起时（即 MotionEvent.ACTION_UP ），删除了不相关代码，剩余代码如下。
```
public boolean onTouchEvent(MotionEvent event) {
    // 省略不相关代码...

    if (MotionEvent.ACTION_DOWN == event.getAction()) {
		// 省略不相关代码...
        mFling.stop();
    } else if (MotionEvent.ACTION_MOVE == event.getAction()) {
        // 省略不相关代码...
    } else if (MotionEvent.ACTION_UP == event.getAction()) {
        // 省略不相关代码...

        // 速度要大于最小的速度值，才开始滑动
        if (Math.abs(velocityX) > mMinimumVelocity) {

            int initX = getScrollX();

            int maxX = (int) (mCanvasWidth - mViewWidth);
            if (maxX > 0) {
                mFling.start(initX, velocityX, initX, maxX);
            }
        }
        // 省略不相关代码...
    }

    return super.onTouchEvent(event);

}
```

当我们 MotionEvent.ACTION_DOWN 时，我们需要停止滚动的效果，达到立马停止到手指触碰的地方。

当我们 MotionEvent.ACTION_UP 时，我们需要计算 fling 方法所需的最小值和最大值。
根据我们在线程中的计算方式，所以我们的最小值和初始值为 getScrollX() 的值 而最大值为 mCanvasWidth - mViewWidth。
