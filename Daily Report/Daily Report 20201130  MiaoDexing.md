# onScroll

方法签名：

/**
该方法在滚动期间，会多次被调用

e1表示初始的ACTION_DOWN事件
e2表示滚动过程中的ACTION_MOVE事件
distanceX为本次在X轴上的滚动距离
distanceY为本次在Y轴上的滚动距离
这里的滚动距离是相对于上一次onScroll事件的距离，而不是e1 down事件和e2 move事件的距离
*/
```
boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);
```
说明：手指按下屏幕并滚动会触发该方法，即由初始的DOWN事件和一些列的MOVE事件驱动该方法。

# onFling

方法签名：

/**
e1表示初始的ACTION_DOWN事件
e2表示手指离开View时的ACTION_UP事件
veloctiyX，velocitY为手指离开当前View时的滚动速度，以这两个速度为初始速度做匀减速运动，就是现在快速拖动列表后的延迟滚动效果。
*/
```
boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY);
```
说明：在快速滚动屏幕，抬起手指后，滚动效果并不会立即停止。而是会以当前的滚动速度，做匀减速运动，直接速度为0，才会停止滚动。onFling方法就是在手指离开屏幕时触发的。

# View的内容滚动

这里讨论的View滚动指的是View内容的滚动，而不是View位置的移动。当View的内容过长时，通过Scroll的方式，可以一点点的查看。View的内容滚动实际是修改View的mScrollX和mScrollY属性。

要实现对View的滚动，可以通过View的scrollTo或者scrollBy方法来实现（scrollBy最终也是通过scrollBy来实现的）。同时，View滚动后，会回调onScrollChanged方法。如下所示：
```
 //这里是滚动到具体的位置
 public void scrollTo(int x, int y) {
    if (mScrollX != x || mScrollY != y) {
        int oldX = mScrollX;
        int oldY = mScrollY;
        mScrollX = x;
        mScrollY = y;
        invalidateParentCaches();
        //回调方法
        onScrollChanged(mScrollX, mScrollY, oldX, oldY);
        if (!awakenScrollBars()) { //唤醒滚动条
            postInvalidateOnAnimation();
        }
    }
}
```
这里要明确下mScrollX和mScrollY属性的具体含义：

    mScrollX:View左边缘和View内容左边缘在水平方向上的距离，并且当View内容左边缘在View左边缘的左边时，mScrollX为正值，否则为负值。
    mScrollY:View上边缘和View内容上边缘在垂直方向上的距离，并且当View内容上边缘在View上边缘的上边时，mScrollY为正值，否则为负值。
