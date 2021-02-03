#  Android9.0 完全隐藏导航栏、状态栏

## 按照google的官方办法，设置如下几个Flag就可以隐藏导航栏：

```
View decorView = getWindow().getDecorView();
// Hide both the navigation bar and the status bar.
// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
// a general rule, you should design your app to hide the status bar whenever you
// hide the navigation bar.
int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
              | View.SYSTEM_UI_FLAG_FULLSCREEN;
decorView.setSystemUiVisibility(uiOptions);
```
## google对这个方案做了说明：
- With this approach, touching anywhere on the screen causes the navigation bar (and status bar) to reappear and remain visible. The user interaction 
causes the flags to be be cleared.（触摸屏幕任何位置，导航栏都会重新出现并保持可见。因为用户交互导致设置的flag被清除了）
- Once the flags have been cleared, your app needs to reset them if you want to hide the bars again. See Responding to UI Visibility Changes for a 
discussion of how to listen for UI visibility changes so that your app can respond accordingly.（如果想要导航栏再次隐藏，就要重新设置flag）
- Where you set the UI flags makes a difference. If you hide the system bars in your activity's onCreate() method and the user presses Home, the 
system bars will reappear. When the user reopens the activity, onCreate() won't get called, so the system bars will remain visible. 
If you want system UI changes to persist as the user navigates in and out of your activity, set UI flags in onResume() or onWindowFocusChanged().
（不同地方设置UI Flag效果会有影响。在onResume或者onWindowFouncChanged()函数里设置flag永久生效）

- The method setSystemUiVisibility() only has an effect if the view you call it from is visible.（只有在View是可见状态下，调用setSystemUiVisiblity才会生效）

- Navigating away from the view causes flags set with setSystemUiVisibility() to be cleared. （离开当前view，会导致利用setSystemUiVisiblity()函数设置的flag被清除）

即该方案有一个问题：一触摸屏幕，导航栏又重新出现。

## 该问题的解决办法是，将flag设置为如下就可以完全全屏，导航栏和状态栏都被隐藏（亲测有效）
```
private WindowManager mWM = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
View mMainView = inflater.inflate(R.layout.main, null);

WindowManager.LayoutParams lp = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);//显示的优先级可以调整，显示的顺序可以查看WindowManagerPolicy中getWindowLayerFromTypeLw函数中的返回值
lp.x = 0;//显示的起始位置x
lp.y = 0;//显示的起始位置y
lp.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
lp.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |         
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | 
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE | 
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
mWM.addView(mMainView, lp);
```
## 官方对这几个flag的解释：
- SYSTEM_UI_FLAG_HIDE_NAVIGATION
```
 /**
     * Flag for {@link #setSystemUiVisibility(int)}: View has requested that the
     * system navigation be temporarily hidden.
     *
     * <p>This is an even less obtrusive state than that called for by
     * {@link #SYSTEM_UI_FLAG_LOW_PROFILE}; on devices that draw essential navigation controls
     * (Home, Back, and the like) on screen, <code>SYSTEM_UI_FLAG_HIDE_NAVIGATION</code> will cause
     * those to disappear. This is useful (in conjunction with the
     * {@link android.view.WindowManager.LayoutParams#FLAG_FULLSCREEN FLAG_FULLSCREEN} and
     * {@link android.view.WindowManager.LayoutParams#FLAG_LAYOUT_IN_SCREEN FLAG_LAYOUT_IN_SCREEN}
     * window flags) for displaying content using every last pixel on the display.
     *
     * <p>There is a limitation: because navigation controls are so important, the least user
     * interaction will cause them to reappear immediately.  When this happens, both
     * this flag and {@link #SYSTEM_UI_FLAG_FULLSCREEN} will be cleared automatically,
     * so that both elements reappear at the same time.
     *
     * @see #setSystemUiVisibility(int)
     */
    public static final int SYSTEM_UI_FLAG_HIDE_NAVIGATION = 0x00000002;

```
和FLAG_FULLSCREEN、FLAG_LAYOUT_IN_SCREEN一起使用会暂时隐藏导航栏。一旦用户与界面发生交互，导航栏又会出现。

- SYSTEM_UI_FLAG_IMMERSIVE
```
/**
     * Flag for {@link #setSystemUiVisibility(int)}: View would like to remain interactive when
     * hiding the navigation bar with {@link #SYSTEM_UI_FLAG_HIDE_NAVIGATION}.  If this flag is
     * not set, {@link #SYSTEM_UI_FLAG_HIDE_NAVIGATION} will be force cleared by the system on any
     * user interaction.
     * <p>Since this flag is a modifier for {@link #SYSTEM_UI_FLAG_HIDE_NAVIGATION}, it only
     * has an effect when used in combination with that flag.</p>
     */
    public static final int SYSTEM_UI_FLAG_IMMERSIVE = 0x00000800;
```
 只有和SYSTEM_UI_FLAG_HIDE_NAVIGATION一起使用才会有效。如果仅仅设置了SYSTEM_UI_FLAG_HIDE_NAVIGATION，没有设置SYSTEM_UI_FLAG_IMMERSIVE，那么只要用户与界面进行交互，导航栏则又会出现。
 
- SYSTEM_UI_FLAG_IMMERSIVE_STICKY
```
  /**
     * Flag for {@link #setSystemUiVisibility(int)}: View would like to remain interactive when
     * hiding the status bar with {@link #SYSTEM_UI_FLAG_FULLSCREEN} and/or hiding the navigation
     * bar with {@link #SYSTEM_UI_FLAG_HIDE_NAVIGATION}.  Use this flag to create an immersive
     * experience while also hiding the system bars.  If this flag is not set,
     * {@link #SYSTEM_UI_FLAG_HIDE_NAVIGATION} will be force cleared by the system on any user
     * interaction, and {@link #SYSTEM_UI_FLAG_FULLSCREEN} will be force-cleared by the system
     * if the user swipes from the top of the screen.
     * <p>When system bars are hidden in immersive mode, they can be revealed temporarily with
     * system gestures, such as swiping from the top of the screen.  These transient system bars
     * will overlay app’s content, may have some degree of transparency, and will automatically
     * hide after a short timeout.
     * </p><p>Since this flag is a modifier for {@link #SYSTEM_UI_FLAG_FULLSCREEN} and
     * {@link #SYSTEM_UI_FLAG_HIDE_NAVIGATION}, it only has an effect when used in combination
     * with one or both of those flags.</p>
     */
    public static final int SYSTEM_UI_FLAG_IMMERSIVE_STICKY = 0x00001000;
    ```
    只有和SYSTEM_UI_FLAG_FULLSCREEN、SYSTEM_UI_FLAG_HIDE_NAVIGATION其中的一个或两个一起使用时才会有效果。
    
    
