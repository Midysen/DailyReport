# Android Scroller类详解
## 概述

Scroller类封装了滚动，我们可以使用Scroller 类来收集滑动动画过程中的数据，例如为了响应快速滚动动画。当View的位置发生变化的时候，Scroller会自动追踪scrollX 和scrollY的变化。
为了达到弹性动画的效果，我们必须自己获取和处理坐标数据。

## 主要用到的函数

-   startScroll()
-    computeScrollOffset()
-    getCurrX(),getCurrY()
-    View.computeScroll()
-    scrollTo()

### startScroll()
函数签名如下：
```
 public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        mMode = SCROLL_MODE;
        mFinished = false;
        mDuration = duration;
        mStartTime = AnimationUtils.currentAnimationTimeMillis();
        mStartX = startX;
        mStartY = startY;
        mFinalX = startX + dx;
        mFinalY = startY + dy;
        mDeltaX = dx;
        mDeltaY = dy;
        mDurationReciprocal = 1.0f / (float) mDuration;
    }

```
这个函数通过提供滑动起始点和滑动距离来开始滑动，默认的滑动时间为250毫秒.
startX和startY表示滑动的起点
dx和dy 表示要滑动的距离
duration表示整个滑动完成所需要的时间
我们可以看到startScroll这个函数内部并没有做滑动相关的事件处理，只是对一些变量进行赋值。

### computeScrollOffset()

函数签名
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
                ...

```
- 1：计算已经逝去的时间（当前时间减去动画开始的时间）
- 2：计算逝去的时间/动画完成的总时间
- 3：计算当前的mScrollX和mScrollY的值（等于起始值加上逝去时间上这一段发生的位移）

其实我们可以将mCurrX和mCurrY理解成View中的mScrollX和mScrollY.

当我们需要知道view当前滑动的位置调用这个方法，如果滑动还未结束则返回true，否则返回false.

### getCurrX(),getCurrY()

getCurrX()返回当前的X轴偏移量，值等于当前View位置的左边界减去View内容的左边界。可以理解为View 中的mScrollX。
getCurrY()：值等于View位置的上边界减去view内容的上边界。类似于View中的mScrollY.
View.computeScroll()

这是View中的一个方法，在View类中是一个空实现。
当mScrollX和mScrollY发生变化的时候，父布局会调用这个方法。典型的例子是被用在View中的内容滑动。
使用Scroller来实现弹性滑动

自定义了一个View，点击这个View中的区域，view的内容自动弹性滑动到点击点。
先上代码
```
package com.example.usescroller;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;
import android.widget.TextView;

/**
 * Created by pingkun.huang on 2016/4/22.
 */
public class MyView extends TextView implements View.OnTouchListener {
    private Context context;
    private Scroller scroller;
    public MyView(Context context) {
        super(context);
        initDataa(context);
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDataa(context);
    }

    public MyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDataa(context);
    }

    private void initDataa(Context context) {
        this.context = context;
        this.setOnTouchListener(this);
        scroller = new Scroller(context);
    }

    private void smoothScrollTo(int destX, int destY) {
        int scrollX = this.getScrollX();
        int scrollY = this.getScrollY();
        int deltaX = destX - scrollX;
        int deltaY = destY - scrollY;
        scroller.startScroll(scrollX, scrollY, deltaX, deltaY, 1000);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            this.scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                smoothScrollTo(-(int)event.getX(),-(int)event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                smoothScrollTo(-(int)event.getX(),-(int)event.getY());
                break;
        }
        return true;
    }
}

```   

先上一张图
[!tu](https://img-blog.csdn.net/20160422225117549)
这里写图片描述

图中绿色的区域是View的区域，黄色部分是View中的内容部分，我们所操纵的坐标都是相对于当前View而言的。假设原先内容已经移动到（50，50）位置，现在我们点击了（100，100）位置，之后内容移动到我们点击的（100，100）上。
- 1:当view的内容在（50，50）时调用
int scrollX = this.getScrollX();
int scrollY = this.getScrollY();
后scrollX =-50;scrollY =-50;
- 2:当点击（100，100）时调用了smoothScrollTo(-(int)event.getX(),-(int)event.getY()); 其中event.getX()=100;event.getY()=100;注意我们传入时两个参数都取反了，这是因为我们所有的计算都是基于mScrollX和mScrollY，当view内容的左边距位于View位置的左边距的右边是为负的。view内容的上边距位于View位置的上边距的下边是为负的。所以destX = -100;destY=-100;
- 3:调用int deltaX = destX - scrollX;
int deltaY = destY - scrollY;后
deltaX = -100+50=-50;
deltaY = -100+50 = -50;
- 4 调用scroller.startScroll(scrollX, scrollY, deltaX, deltaY, 1000);
scrollX = -50为负，view内容的左边缘位于View位置左边缘的右边
scrollY =-50为父，veiw内容的上边缘位于View位置上边缘的下边。
即位于（50，50）点上
deltaX = -50;负数将滑动到右侧。
deltaY = -50;负数将滑动到下侧。
滑动时间为1000毫秒
这个函数view的内容将从（50，50）向右下方（100，100）移动。
- 5：调用了InValidate（）方法来重绘这个View，重绘时会调用View的draw方法，在draw方法中会调用computeScroll方法。
- 6：因为computeScroll在View中只是空实现，所以我们必须自己实现，
if (scroller.computeScrollOffset()) {
this.scrollTo(scroller.getCurrX(), scroller.getCurrY());
如果动画还没结束，comcomputeScroll会去向Scoller获取当前的scrollX和scrollY；然后通过scrollTo来实现滑动。
- 7：调用postInvalidate()来进行二次重绘。这一次的重绘过程和第一次的重绘过程一样，还是会去调用computeScroll方法，继续获取scrollX和scrollY的值，并通过scrollTo滑动到新位置，往复这个过程，直到滑动过程结束。
## 总结一下Scroller的工作原理

Scroller本身并不能滑动，需要和computeScroll配合使用来实现滑动，这个方法通过postInvalidate()不断的让View重绘，每一次的重绘距离上一次滑动的起始时间有一个时间差，computeScrollOffset方法利用这个时间差来计算出view内容的当前滑动位置，知道了滑动位置就可以通过scrollTo方法来滑动。View的每一次重绘都会使得View小幅度的滑动，多次的滑动组成了弹性滑动。
