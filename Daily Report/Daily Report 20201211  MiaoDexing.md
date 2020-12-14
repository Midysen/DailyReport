# dumpsys命令详解
dumpsys是一个能帮助我们对手机进行性能分析的命令，它可以帮助我们获取电池、内存、cpu、磁盘、wifi等等信息，具体能查询的信息可以通过命令：
```
adb shell dumpsys | grep 'DUMP OF SERVICE'

DUMP OF SERVICE DockObserver:
DUMP OF SERVICE SmartShowManager:
DUMP OF SERVICE SurfaceFlinger:
DUMP OF SERVICE accessibility:
DUMP OF SERVICE account:
DUMP OF SERVICE activity:
DUMP OF SERVICE alarm:
DUMP OF SERVICE alipay_service:
DUMP OF SERVICE android.security.keystore:
DUMP OF SERVICE appops:
DUMP OF SERVICE appwidget:
DUMP OF SERVICE assetatlas:
DUMP OF SERVICE audio:
DUMP OF SERVICE backup:
DUMP OF SERVICE battery:
DUMP OF SERVICE batteryproperties:
DUMP OF SERVICE batterystats:
DUMP OF SERVICE bbk_touch_screen_service:
DUMP OF SERVICE bluetooth_manager:
DUMP OF SERVICE clipboard:
DUMP OF SERVICE cneservice:
DUMP OF SERVICE com.qti.snapdragon.sdk.display.IColorService:
DUMP OF SERVICE com.qualcomm.qti.auth.fidocryptodaemon:
DUMP OF SERVICE commontime_management:
DUMP OF SERVICE connectivity:
DUMP OF SERVICE consumer_ir:
DUMP OF SERVICE content:
DUMP OF SERVICE country_detector:
DUMP OF SERVICE cpuinfo:
DUMP OF SERVICE dbinfo:
DUMP OF SERVICE device_policy:
DUMP OF SERVICE devicestoragemonitor:
DUMP OF SERVICE diskstats:
DUMP OF SERVICE display:
DUMP OF SERVICE display.qservice:
DUMP OF SERVICE dpmservice:
DUMP OF SERVICE dreams:
DUMP OF SERVICE drm.drmManager:
DUMP OF SERVICE dropbox:
DUMP OF SERVICE engineer_utile:
DUMP OF SERVICE entropy:
DUMP OF SERVICE fingerprint:
DUMP OF SERVICE getuk.service:
DUMP OF SERVICE gfxinfo:
DUMP OF SERVICE goodix.fp:
DUMP OF SERVICE hall_state_service:
DUMP OF SERVICE hardware:
DUMP OF SERVICE imms:
DUMP OF SERVICE ims:
DUMP OF SERVICE input:
DUMP OF SERVICE input_method:
DUMP OF SERVICE iphonesubinfo:
DUMP OF SERVICE isms:
DUMP OF SERVICE isub:
DUMP OF SERVICE jobscheduler:
DUMP OF SERVICE launcherapps:
DUMP OF SERVICE location:
DUMP OF SERVICE lock_settings:
DUMP OF SERVICE media.audio_flinger:
DUMP OF SERVICE media.audio_policy:
DUMP OF SERVICE media.camera:
DUMP OF SERVICE media.player:
DUMP OF SERVICE media.servicehub:
DUMP OF SERVICE media.sound_trigger_hw:
DUMP OF SERVICE media_projection:
DUMP OF SERVICE media_router:
DUMP OF SERVICE media_session:
DUMP OF SERVICE meminfo:
DUMP OF SERVICE motion_manager:
DUMP OF SERVICE mount:
DUMP OF SERVICE netpolicy:
DUMP OF SERVICE netstats:
DUMP OF SERVICE network_management:
DUMP OF SERVICE network_score:
DUMP OF SERVICE notification:
DUMP OF SERVICE package:
DUMP OF SERVICE permission:
DUMP OF SERVICE persistent_data_block:
DUMP OF SERVICE phone:
DUMP OF SERVICE power:
DUMP OF SERVICE print:
DUMP OF SERVICE procstats:
DUMP OF SERVICE profile:
DUMP OF SERVICE qti.ims.connectionmanagerservice:
DUMP OF SERVICE restrictions:
DUMP OF SERVICE rttmanager:
DUMP OF SERVICE samplingprofiler:
DUMP OF SERVICE scheduling_policy:
DUMP OF SERVICE search:
DUMP OF SERVICE sensor_log:
DUMP OF SERVICE sensorservice:
DUMP OF SERVICE serial:
DUMP OF SERVICE servicediscovery:
DUMP OF SERVICE simphonebook:
DUMP OF SERVICE sip:
DUMP OF SERVICE statusbar:
DUMP OF SERVICE telecom:
DUMP OF SERVICE telephony.registry:
DUMP OF SERVICE textservices:
DUMP OF SERVICE trust:
DUMP OF SERVICE uimode:
DUMP OF SERVICE updatelock:
DUMP OF SERVICE usagestats:
DUMP OF SERVICE usb:
DUMP OF SERVICE user:
DUMP OF SERVICE vendor.qcom.PeripheralManager:
DUMP OF SERVICE vibrator:
DUMP OF SERVICE vivo_daemon.service:
DUMP OF SERVICE vivo_fingerprints_service:
DUMP OF SERVICE vivo_fp_service:
DUMP OF SERVICE vivo_perf_service:
DUMP OF SERVICE vivo_permission:
DUMP OF SERVICE vivo_permission_service:
DUMP OF SERVICE vivo_prox_cali_service:
DUMP OF SERVICE vivosmartmultiwindowservice:
DUMP OF SERVICE voiceinteraction:
DUMP OF SERVICE wallpaper:
DUMP OF SERVICE webviewupdate:
DUMP OF SERVICE wifi:
DUMP OF SERVICE wifip2p:
DUMP OF SERVICE wifiscanner:
DUMP OF SERVICE window:
```

下面是一些比较常用的：
```
dumpsys cpuinfo //打印一段时间进程的CPU使用百分比排行榜
dumpsys meminfo -h  //查看dump内存的帮助信息
dumpsys package <packagename> //查看指定包的信息
dumpsys activity top //当前界面app状态
dumpsys activity oom //进程oom状态
dumpsys display | grep DisplayDeviceInfo //获取屏幕分辨率
dumpsys battery //电池信息
dumpsys cpuinfo  //cpu信息
dumpsys meminfo （+package）  //内存信息（对应包的）
dumpsys activity top  //获取当前界面的UI信息
dumpsys activity top | grep ACTIVITY  //获取当前页面的activity
dumpsys package PACKAGE_NAME //获取package信息
dumpsys notification //获取通知栏信息
dumpsys wifi  //获取到当前连接的wifi名、搜索到的wifi列表、wifi强度等
dumpsys power  
//可以获取到是否处于锁屏状态：mWakefulness=Asleep或者mScreenOn=false
亮度值：mScreenBrightness=255
屏幕休眠时间：Screen off timeout: 60000 ms
屏幕分辨率：mDisplayWidth=1440，mDisplayHeight=2560
dumpsys telephony.registry
//mCallState值为0，表示待机状态、1表示来电未接听状态、2表示电话占线状态
mCallForwarding=false #是否启用呼叫转移
mDataConnectionState=2 #0：无数据连接 1：正在创建数据连接 2：已连接
mDataConnectionPossible=true  #是否有数据连接
mDataConnectionApn=   #APN名称

```
