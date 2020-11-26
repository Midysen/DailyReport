# Scroller
- Scroller开始滚动后（startScroll()）计算数据的逻辑，其中startScroll()的逻辑如下:
  -  core/java/android/widget/Scroller.java
```

368  public void startScroll(int startX, int startY, int dx, int dy) {                                                                                                                                   
369         startScroll(startX, startY, dx, dy, DEFAULT_DURATION);
370     }
371 
372     /**
373      * Start scrolling by providing a starting point, the distance to travel,
374      * and the duration of the scroll.
375      * 
376      * @param startX Starting horizontal scroll offset in pixels. Positive
377      *        numbers will scroll the content to the left.
378      * @param startY Starting vertical scroll offset in pixels. Positive numbers
379      *        will scroll the content up.
380      * @param dx Horizontal distance to travel. Positive numbers will scroll the
381      *        content to the left.
382      * @param dy Vertical distance to travel. Positive numbers will scroll the
383      *        content up.
384      * @param duration Duration of the scroll in milliseconds.
385      */
386     public void startScroll(int startX, int startY, int dx, int dy, int duration) {
387         mMode = SCROLL_MODE;
388         mFinished = false;
389         mDuration = duration;
390         mStartTime = AnimationUtils.currentAnimationTimeMillis();
391         mStartX = startX;
392         mStartY = startY;
393         mFinalX = startX + dx;
394         mFinalY = startY + dy;
395         mDeltaX = dx;
396         mDeltaY = dy;
397         mDurationReciprocal = 1.0f / (float) mDuration;
398     }


```
