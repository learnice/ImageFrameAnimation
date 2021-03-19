## ImageFrameAnimation

Android 高性能帧动画组件，具有很高的灵活性、扩展性

### 基本用法

组件默认实现了 ResFrameInfo、FileFrameInfo 两种动画资源类型，分别代表 resourceID 和文件照片来源。如果需要通过其它方式加载图片（比如网络），可以继承 AbsFrameInfo 并实现 decodeBitmap() 方法（可参考 FileFrameInfo 的实现）  

ImageFrameAnimation#startAnim 方法接收的是 List<AbsFrameInfo> 类型参数，所以可以在 List 中添加任意 AbsFrameInfo 子类的对象

```
    private void initView() {
        // ImageView
        mIvFrame = findViewById(R.id.iv_frame_perfect);
        // 初始化
        imageFrameAnimation = new ImageFrameAnimation(mIvFrame);
        // 运行帧率（默认为 30）
        imageFrameAnimation.setFps(30);
        // 设置 RepeatMode 重新开始（默认为 RESTART）
        imageFrameAnimation.setRepeatMode(ImageFrameAnimation.RESTART);
        // 设置 RepeatCount 无限循环 （默认为 0 次）
        imageFrameAnimation.setRepeatCount(ImageFrameAnimation.INFINITE);
        // 监听
        imageFrameAnimation.setListener(new AnimationListener() {
            @Override
            public void onAnimationStart() {
                Log.d(TAG, "onAnimationStart: ");
            }

            @Override
            public void onAnimationEnd() {
                Log.d(TAG, "onAnimationEnd: ");
            }

            @Override
            public void onAnimationCancel() {
                Log.d(TAG, "onAnimationCancel: ");
            }

            @Override
            public void onFrame(AbsFrameInfo frameInfo) {
                Log.d(TAG, "onFrame: " + frameInfo.getFrameName());
            }
        });
    }
    
    /**
     * 初始化数据，从资源文件加载
     */
    private void generateFrameInfoResData() {
        int[] res = getRes();
        List<AbsFrameInfo> list = new ArrayList<>();
        for (int j = 0; j < res.length; j++) {
            ResFrameInfo frameInfo = new ResFrameInfo(res[j]);
            list.add(frameInfo);
        }
        mData = list;
    }
    
    // 开始动画，需要传入配置好的帧数据（主线程调用）
    imageFrameAnimation.startAnim(mData);
    // 停止动画（主线程调用）
    imageFrameAnimation.cancelAnim();
    
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 不要忘记关闭动画，产生内存泄漏
        if (imageFrameAnimation != null) {
            imageFrameAnimation.cancelAnim();
        }
    }
```

### 扩展用法

组件默认实现了 Normal、Live 两种动画运行方式：  
**Normal** 方式按照添加进来的动画资源顺序播放动画；  
**Live** 方式类似直播的形式，播完一帧动画后就移除了这帧的资源  
当然你可以自己实现 IExecutionMode 接口来定制你想要的动画运行方式。比如：跳帧运行...


```
    // Live 运行方式
    IExecutionMode liveExecutionMode = new LiveExecutionMode();
    // Normal 运行方式
    IExecutionMode normalExecutionMode = new NormalExecutionMode();
    // 创建 ImageFrameAnimation 传入需要的 IExecutionMode
    imageFrameAnimation = new ImageFrameAnimation(mIvFrame, normalExecutionMode);
    
    
    // 如果选用了 Live 运行方式，需要调用 addAnim 给组件添加资源，供组件消费（主线程调用）
    imageFrameAnimation.addAnim(mData);
```

### 高性能

Android 原生帧动画框架在初始化的时候会将所有的帧加载、解码到内存中，这样做保证了动画运行期间内存的平稳，但是代价就是初始化时间、内存占用都会很大，给应用带来不小的压力  

组件将动画的加载、解码都放到运行时，初始化成本很小。为了防止运行期间频繁分配、回收 Bitmap 内存抖动，组件会根据帧的大小提前分配两张 Bitmap， 在动画运行时，所有的帧的内存都会复用它们。为了避免频繁通过 IO 加载图片，组件会缓存图片文件的字节，已经加载过的图片只需要解码即可  

耗时：组件初始化没有任何耗时操作，图片的加载、解码、内存复用全部在子线程  
CPU ：组件运行期间只需要对图片解码然后交给 ImageView 去显示  
内存：只需要两张 Bitmap 内存并且很稳定

### 现有应用

唱吧打分 Perfect 动画（Live 运行方式）

![image](https://github.com/learnice/Resources/blob/main/ImageFrameAnimation/ImageFrameAnimation%E7%BA%BF%E4%B8%8A%E4%BD%BF%E7%94%A8%E7%A4%BA%E4%BE%8B.gif?raw=true)

### 展望

提供与 Android 原生帧动画低成本切换方法