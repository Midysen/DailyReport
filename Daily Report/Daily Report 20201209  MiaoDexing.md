Scroller 是一个让视图滚动起来的工具类，负责根据我们提供的数据计算出相应的坐标，Scroller类中提供了很多方法完成视图的滚动，
比如以下方法：
- 第一个方法
```
public void startScroll(int startX, int startY, int dx, int dy, int duration)
```
方法描述：
通过提供起点，行程距离和滚动持续时间，进行滚动的一种方式，即 SCROLL_MODE。该方法可以用于实现像ViewPager的滑动效果。

参数解析：
第一个参数 startX： 开始点的x坐标

第二个参数 startY： 开始点的y坐标

第三个参数 dx： 水平方向的偏移量，正数会将内容向左滚动。

第四个参数 dy： 垂直方向的偏移量，正数会将内容向上滚动。

第五个参数 duration： 滚动的时长

- 第二个方法：
```
public boolean computeScrollOffset()
```

方法描述：

计算滚动中的新坐标，会配合着 getCurrX 和 getCurrY 方法使用，达到滚动效果。值得注意的是，如果返回true，说明动画还未完成。相反，返回false，说明动画已经完成或是被终止了。


实际应用中，经常使用computeScroll来判断当前控件是否滑动完毕，由此实现自定义的滑动效果。
因为Scroller本身只负责计算，滑动行为是View本身调用scrollBy与scrollTo（准确地说是调用后者，
因为scrollBy也是调用了scrollTo）发起的，是View内置的API，而不是Scroller。Scroller的存在是为了更好更美观的滑动，也就是为更好的滑动提供数据。

- 第三个方法：
```
public void fling(int startX, int startY, int velocityX, int velocityY,
                      int minX, int maxX, int minY, int maxY)
```
方法描述：
用于带速度的滑动，行进的距离将取决于投掷的初始速度。可以用于实现类似 RecycleView 的滑动效果。

参数解析：
第一个参数 startX： 开始滑动点的x坐标

第二个参数 startY： 开始滑动点的y坐标

第三个参数 velocityX： 水平方向的初始速度，单位为每秒多少像素（px/s）

第四个参数 velocityY： 垂直方向的初始速度，单位为每秒多少像素（px/s）

第五个参数 minX： x坐标最小的值，最后的结果不会低于这个值；

第六个参数 maxX： x坐标最大的值，最后的结果不会超过这个值；

第七个参数 minY： y坐标最小的值，最后的结果不会低于这个值；

第八个参数 maxY： y坐标最大的值，最后的结果不会超过这个值；


当用户手指快速划过屏幕，然后快速离开屏幕时，系统会判定用户执行了一个Fling手势。
视图会快速滚动，并且在手指立刻屏幕之后也会滚动一段时间。fling就是根据你的滑动方向与轻重，自动滑动一段距离。
Filing手势在android交互设计中应用非常广泛：电子书的滑动翻页、ListView滑动删除item、滑动解锁等。

Scroller提供了很多方法，但这些只是工具，具体的滚动逻辑还是由我们程序猿来进行移动内容实现。
