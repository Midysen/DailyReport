# AndroidP CarService 和 Vehicle
-  **我们就从空调如何set get温度来走一遭，看看set温度的时候设置的具体温度如何从应用层到HAL Vehicle的，温度发生变化时，Vehicle又如何将实时的温度传递给应用层。**

## 一、Java Framework层
### CarService
CarService也是继承自service的，所以Carservice也是一个service，代码目录在 packages/services/Car/service/src/com/android/car/CarService.java，我自己理解的这个service就是直接hidl调用底的VehicleService（C++）、然后将各个Car相关的service拉起来，这里的其他car service并不是直接继承service的，而是继承自CarServiceBase的各个“server”，这里其实和systemUI中启动Navigation、power、volumeBar等有点像，都是继承自一个基类，然后逐个拉起，但有所不同的是Car “server”的各个子类都是一个aidl，方便后面应用层直接调用，我们上代码看看启动过程。
```
其中省略了一些不必要的代码段。
public class CarService extends Service {

     ......... 
    private ICarImpl mICarImpl;
    private IVehicle mVehicle;
    ............
    public void onCreate() {
        Log.i(CarLog.TAG_SERVICE, "Service onCreate");
        ..........
        mVehicle = getVehicle(); // 在这个方法里面get hidl vehicle service，
        ...........
        mICarImpl = new ICarImpl(this,
                mVehicle,
                SystemInterface.Builder.defaultSystemInterface(this).build(),
                mCanBusErrorNotifier,
                mVehicleInterfaceName); // 在这里负责将其他的car 相关的“service” new出来。
        mICarImpl.init(); 
        ........
        ServiceManager.addService("car_service", mICarImpl);
        super.onCreate();
    }
    ........
    private static IVehicle getVehicle() {
        try {
            return android.hardware.automotive.vehicle.V2_0.IVehicle.getService(); //hidl直接调用。后面我们分析HAL层的时候会分析
        } catch (RemoteException e) {
            Log.e(CarLog.TAG_SERVICE, "Failed to get IVehicle service", e);
        } catch (NoSuchElementException e) {
            Log.e(CarLog.TAG_SERVICE, "IVehicle service not registered yet");
        }
        return null;
    }
```
- packages/services/Car/service/src/com/android/car/ICarImpl.java
```
  ................
    private final Context mContext;
    private final VehicleHal mHal; // 这个是 HAL层的server，在上面的carServer中get到的传递进来的

    private final SystemInterface mSystemInterface;

    // 这些就是我说的基础自 CarServiceBase的“server”
    private final SystemActivityMonitoringService mSystemActivityMonitoringService;
    private final CarPowerManagementService mCarPowerManagementService;
    private final CarPackageManagerService mCarPackageManagerService;
    private final CarInputService mCarInputService;
    private final CarDrivingStateService mCarDrivingStateService;
    private final CarUxRestrictionsManagerService mCarUXRestrictionsService;
    private final CarAudioService mCarAudioService;
    private final CarProjectionService mCarProjectionService;
    private final CarPropertyService mCarPropertyService;
    private final CarNightService mCarNightService;
    private final AppFocusService mAppFocusService;
    private final GarageModeService mGarageModeService;
    private final InstrumentClusterService mInstrumentClusterService;
    private final CarLocationService mCarLocationService;
    private final SystemStateControllerService mSystemStateControllerService;
    private final CarBluetoothService mCarBluetoothService;
    private final PerUserCarServiceHelper mPerUserCarServiceHelper;
    private final CarDiagnosticService mCarDiagnosticService;
    private final CarStorageMonitoringService mCarStorageMonitoringService;
    private final CarConfigurationService mCarConfigurationService;
    private final CarUserManagerHelper mUserManagerHelper;
    private CarUserService mCarUserService;
    private VmsSubscriberService mVmsSubscriberService;
    private VmsPublisherService mVmsPublisherService;

   // 用于保存上面的service
    private final CarServiceBase[] mAllServices;


    /** Test only service. Populate it only when necessary. */
    @GuardedBy("this")
    private CarTestService mCarTestService;

    @GuardedBy("this")
    private ICarServiceHelper mICarServiceHelper;

    private final String mVehicleInterfaceName;

    public ICarImpl(Context serviceContext, IVehicle vehicle, SystemInterface systemInterface,
            CanBusErrorNotifier errorNotifier, String vehicleInterfaceName) {
        // 在这里new出来各个继承carServiceBase的aidl服务。然后保存在数组allServices中
        mContext = serviceContext;
        mSystemInterface = systemInterface;
        mHal = new VehicleHal(vehicle); //注意vehicle传参到这里去了，这里有一个变量 HalClient，其实Hal vehicle 
                                        //和 Java framework层的信息传递就是通过作为“client”连接下面的hal service，后面会讲到
        mVehicleInterfaceName = vehicleInterfaceName;
        mSystemActivityMonitoringService = new SystemActivityMonitoringService(serviceContext);
        mCarPowerManagementService = new CarPowerManagementService(mContext, mHal.getPowerHal(),
                systemInterface);
        mCarPropertyService = new CarPropertyService(serviceContext, mHal.getPropertyHal());
        mCarDrivingStateService = new CarDrivingStateService(serviceContext, mCarPropertyService);
        mCarUXRestrictionsService = new CarUxRestrictionsManagerService(serviceContext,
                mCarDrivingStateService, mCarPropertyService);
        mCarPackageManagerService = new CarPackageManagerService(serviceContext,
                mCarUXRestrictionsService,
                mSystemActivityMonitoringService);
        mCarInputService = new CarInputService(serviceContext, mHal.getInputHal());
        mCarProjectionService = new CarProjectionService(serviceContext, mCarInputService);
        mGarageModeService = new GarageModeService(mContext, mCarPowerManagementService);
        mAppFocusService = new AppFocusService(serviceContext, mSystemActivityMonitoringService);
        mCarAudioService = new CarAudioService(serviceContext);
        mCarNightService = new CarNightService(serviceContext, mCarPropertyService);
        mInstrumentClusterService = new InstrumentClusterService(serviceContext,
                mAppFocusService, mCarInputService);
        mSystemStateControllerService = new SystemStateControllerService(serviceContext,
                mCarPowerManagementService, mCarAudioService, this);
        mPerUserCarServiceHelper = new PerUserCarServiceHelper(serviceContext);
        mCarBluetoothService = new CarBluetoothService(serviceContext, mCarPropertyService,
                mPerUserCarServiceHelper, mCarUXRestrictionsService);
        mVmsSubscriberService = new VmsSubscriberService(serviceContext, mHal.getVmsHal());
        mVmsPublisherService = new VmsPublisherService(serviceContext, mHal.getVmsHal());
        mCarDiagnosticService = new CarDiagnosticService(serviceContext, mHal.getDiagnosticHal());
        mCarStorageMonitoringService = new CarStorageMonitoringService(serviceContext,
                systemInterface);
        //---------------begin modify ---------------------
        //mCarConfigurationService =
        //        new CarConfigurationService(serviceContext, new JsonReaderImpl());
        mCarConfigurationService =
                new CarConfigurationService(serviceContext, new JsonImpl());
        //---------------end-------------------------------
        mUserManagerHelper = new CarUserManagerHelper(serviceContext);
        mCarLocationService = new CarLocationService(mContext, mCarPowerManagementService,
                mCarPropertyService, mUserManagerHelper);

        // Be careful with order. Service depending on other service should be inited later.
        List<CarServiceBase> allServices = new ArrayList<>();
        allServices.add(mSystemActivityMonitoringService);
        allServices.add(mCarPowerManagementService);
        allServices.add(mCarPropertyService);
        allServices.add(mCarDrivingStateService);
        allServices.add(mCarUXRestrictionsService);
        allServices.add(mCarPackageManagerService);
        allServices.add(mCarInputService);
        allServices.add(mGarageModeService);
        allServices.add(mAppFocusService);
        allServices.add(mCarAudioService);
        allServices.add(mCarNightService);
        allServices.add(mInstrumentClusterService);
        allServices.add(mCarProjectionService);
        allServices.add(mSystemStateControllerService);
        allServices.add(mCarBluetoothService);
        allServices.add(mCarDiagnosticService);
        allServices.add(mPerUserCarServiceHelper);
        allServices.add(mCarStorageMonitoringService);
        allServices.add(mCarConfigurationService);
        allServices.add(mVmsSubscriberService);
        allServices.add(mVmsPublisherService);
        ........
        allServices.add(mCarLocationService);
        mAllServices = allServices.toArray(new CarServiceBase[allServices.size()]);
    }

    @MainThread
    void init() {
        traceBegin("VehicleHal.init");
        mHal.init();
        traceEnd();
        traceBegin("CarService.initAllServices");
        for (CarServiceBase service : mAllServices) {
            // 初始化每一个car 相关的 aidl service。
            service.init();
        }
        traceEnd();
    }
```
上面这边多service我这边就不一一的介绍了，主要说一下CarPropertyService。
- packages/services/Car/service/src/com/android/car/CarPropertyService.java
```
public class CarPropertyService extends ICarProperty.Stub
        implements CarServiceBase, PropertyHalService.PropertyHalListener {
        ........
            @Override
    public void init() { //从 ICarImpl.java 中的init（）中启动的
        if (mConfigs == null) {
            // Cache the configs list to avoid subsequent binder calls
            mConfigs = mHal.getPropertyList();
            if (DBG) {
                Log.d(TAG, "cache CarPropertyConfigs " + mConfigs.size());
            }
        }
    }
    .........
    这里的主要的方法有getProperty setProperty onPropertyChange, get setProperty()最终会调用到HAL Vehicle中的实现， 
    onPropertyChange（）是HAL Vehicle 从CAN盒哪里读到变化通知Java层的方法。我们下面就围绕着三个方法
        @Override
    public List<CarPropertyConfig> getPropertyList() {
        List<CarPropertyConfig> returnList = new ArrayList<CarPropertyConfig>();
        for (CarPropertyConfig c : mConfigs.values()) {
            if (ICarImpl.hasPermission(mContext, mHal.getReadPermission(c.getPropertyId()))) {
                // Only add properties the list if the process has permissions to read it
                returnList.add(c);
            }
        }
        if (DBG) {
            Log.d(TAG, "getPropertyList returns " + returnList.size() + " configs");
        }
        return returnList;
    }

    @Override
    public CarPropertyValue getProperty(int prop, int zone) {
        if (mConfigs.get(prop) == null) {
            // Do not attempt to register an invalid propId
            Log.e(TAG, "getProperty: propId is not in config list:0x" + toHexString(prop));
            return null;
        }
        ICarImpl.assertPermission(mContext, mHal.getReadPermission(prop));
        return mHal.getProperty(prop, zone);
    }

    @Override
    public void setProperty(CarPropertyValue prop) {
        int propId = prop.getPropertyId();
        if (mConfigs.get(propId) == null) {
            // Do not attempt to register an invalid propId
            Log.e(TAG, "setProperty:  propId is not in config list:0x" + toHexString(propId));
            return;
        }
        ICarImpl.assertPermission(mContext, mHal.getWritePermission(propId));
        mHal.setProperty(prop);
    }
     // Implement PropertyHalListener interface
    @Override
    public void onPropertyChange(List<CarPropertyEvent> events) {
        Map<IBinder, Pair<ICarPropertyEventListener, List<CarPropertyEvent>>> eventsToDispatch =
                new HashMap<>();

        for (CarPropertyEvent event : events) {
            int propId = event.getCarPropertyValue().getPropertyId();
            List<Client> clients = mPropIdClientMap.get(propId);
            if (clients == null) {
                Log.e(TAG, "onPropertyChange: no listener registered for propId=0x"
                        + toHexString(propId));
                continue;
            }

            for (Client c : clients) {
                IBinder listenerBinder = c.getListenerBinder();
                Pair<ICarPropertyEventListener, List<CarPropertyEvent>> p =
                        eventsToDispatch.get(listenerBinder);
                if (p == null) {
                    // Initialize the linked list for the listener
                    p = new Pair<>(c.getListener(), new LinkedList<CarPropertyEvent>());
                    eventsToDispatch.put(listenerBinder, p);
                }
                p.second.add(event);
            }
        }
        // Parse the dispatch list to send events
        for (Pair<ICarPropertyEventListener, List<CarPropertyEvent>> p: eventsToDispatch.values()) {
            try {
                p.first.onEvent(p.second);
            } catch (RemoteException ex) {
                // If we cannot send a record, its likely the connection snapped. Let binder
                // death handle the situation.
                Log.e(TAG, "onEvent calling failed: " + ex);
            }
        }
    }

    @Override
    public void onPropertySetError(int property, int area) {
        List<Client> clients = mPropIdClientMap.get(property);
        if (clients != null) {
            List<CarPropertyEvent> eventList = new LinkedList<>();
            eventList.add(createErrorEvent(property, area));
            for (Client c : clients) {
                try {
                    c.getListener().onEvent(eventList);
                } catch (RemoteException ex) {
                    // If we cannot send a record, its likely the connection snapped. Let the binder
                    // death handle the situation.
                    Log.e(TAG, "onEvent calling failed: " + ex);
                }
            }
        } else {
            Log.e(TAG, "onPropertySetError called with no listener registered for propId=0x"
                    + toHexString(property));
        }
    }


```
我们可以看到上面CarPropertyService调用 set getProperty的地方都最后都是调用了mHal 对象的set 和 getProperty，而 CarPropertyService的mHal的赋值来自ICarImpl中。
ICarImpl构造函数 mHal = new VehicleHal(vehicle) -> VehicleHal构造函数 mPropertyHal = new PropertyHalService(this); -> PropertyHalService getPropertyHal() -> mCarPropertyService = new CarPropertyService(serviceContext, mHal.getPropertyHal());
-> CarPropertyService构造函数 CarPropertyService(Context context, PropertyHalService hal) { mHal = hal}

- 追到上面我们知道 set getProperty来自PropertyHalService 。那就继续看 PropertyHalService
- packages/services/Car/service/src/com/android/car/hal/PropertyHalService.java
```
public class PropertyHalService extends HalServiceBase {
.....
    private final VehicleHal mVehicleHal;
.....
    public interface PropertyHalListener {
        /**
         * This event is sent whenever the property value is updated
         * @param event
         */
        void onPropertyChange(List<CarPropertyEvent> events);
        /**
         * This event is sent when the set property call fails
         * @param property
         * @param area
         */
        void onPropertySetError(int property, int area);
    }
.....
       @Nullable
    public CarPropertyValue getProperty(int mgrPropId, int areaId) {
        int halPropId = managerToHalPropId(mgrPropId);
        if (halPropId == NOT_SUPPORTED_PROPERTY) {
            throw new IllegalArgumentException("Invalid property Id : 0x" +       toHexString(mgrPropId));
        }

        VehiclePropValue value = null;
        try {
            value = mVehicleHal.get(halPropId, areaId);
        } catch (PropertyTimeoutException e) {
            Log.e(CarLog.TAG_PROPERTY, "get, property not ready 0x" + toHexString(halPropId), e);
        }

        return value == null ? null : toCarPropertyValue(value, mgrPropId);
    }

    public void setProperty(CarPropertyValue prop) {
        int halPropId = managerToHalPropId(prop.getPropertyId());
        if (halPropId == NOT_SUPPORTED_PROPERTY) {
            throw new IllegalArgumentException("Invalid property Id : 0x"
                    + toHexString(prop.getPropertyId()));
        }
        VehiclePropValue halProp = toVehiclePropValue(prop, halPropId);
        try {
            mVehicleHal.set(halProp);
        } catch (PropertyTimeoutException e) {
            Log.e(CarLog.TAG_PROPERTY, "set, property not ready 0x" + toHexString(halPropId), e);
            throw new RuntimeException(e);
        }
    }
```
可以看到上面的 set get最后又是调用mVehicleHal里面的set get
- packages/services/Car/service/src/com/android/car/hal/VehicleHal.java
```
public class VehicleHal extends IVehicleCallback.Stub { 
    ......
    public VehicleHal(IVehicle vehicle) {
        mHandlerThread = new HandlerThread("VEHICLE-HAL");
        mHandlerThread.start();
        // passing this should be safe as long as it is just kept and not used in constructor
        mPowerHal = new PowerHalService(this);
        mPropertyHal = new PropertyHalService(this);
        mInputHal = new InputHalService(this);
        mVmsHal = new VmsHalService(this);
        mDiagnosticHal = new DiagnosticHalService(this);
        mAllServices.addAll(Arrays.asList(mPowerHal,
                mInputHal,
                mPropertyHal,
                mDiagnosticHal,
                mVmsHal));

        mHalClient = new HalClient(vehicle, mHandlerThread.getLooper(), this /*IVehicleCallback*/);
    }
    ......
        public VehiclePropValue get(int propertyId) throws PropertyTimeoutException {
        return get(propertyId, NO_AREA);
    }

    public VehiclePropValue get(int propertyId, int areaId) throws PropertyTimeoutException {
        if (DBG) {
            Log.i(CarLog.TAG_HAL, "get, property: 0x" + toHexString(propertyId)
                    + ", areaId: 0x" + toHexString(areaId));
        }
        VehiclePropValue propValue = new VehiclePropValue();
        propValue.prop = propertyId;
        propValue.areaId = areaId;
        return mHalClient.getValue(propValue);
    }
    ......
    void set(VehiclePropValue propValue) throws PropertyTimeoutException {
        mHalClient.setValue(propValue);
    }

    @CheckResult
    VehiclePropValueSetter set(int propId) {
        return new VehiclePropValueSetter(mHalClient, propId, NO_AREA);
    }

    @CheckResult
    VehiclePropValueSetter set(int propId, int areaId) {
        return new VehiclePropValueSetter(mHalClient, propId, areaId);
    }
    .......
}
```
从上面的代码我们又可以看到VehicleHal中的set get最后会调到HalClient中的setValue getValue
,在 HalCient中的 private final IVehicle mVehicle; mVehicle的赋值来自VehicleHal的构造函数 mHalClient = new HalClient(vehicle, mHandlerThread.getLooper(), this /IVehicleCallback/); …
最终 HalClient中的private final IVehicle mVehicle 其实来自 CarService的
```
@Nullable
    private static IVehicle getVehicle() {
        try {
            return android.hardware.automotive.vehicle.V2_0.IVehicle.getService();
        } catch (RemoteException e) {
            Log.e(CarLog.TAG_SERVICE, "Failed to get IVehicle service", e);
        } catch (NoSuchElementException e) {
            Log.e(CarLog.TAG_SERVICE, "IVehicle service not registered yet");
        }
        return null;
    }
```
到这里我们分析了CarPropertyService的set getProperty最后如何调用到HAL 的 set getProperty的，
像车门的开关及状态、hvac的温度、风速的设置及状态等等都是调用了CarPropertyService的set getProperty来传递到HAL Vehicle的。
- packages/services/Car/car-lib/src/android/car/hardware/hvac/CarHvacManager.java
```
public CarHvacManager(IBinder service, Context context, Handler handler) {
        mCarPropertyMgr = new CarPropertyManager(service, handler, DBG, TAG);
    }
    public void setBooleanProperty(@PropertyId int propertyId, int area, boolean val)
            throws CarNotConnectedException {
        if (mHvacPropertyIds.contains(propertyId)) {
            mCarPropertyMgr.setBooleanProperty(propertyId, area, val);
        }
    }

    /**
     * Set the value of a float property
     * @param propertyId
     * @param area
     * @param val
     * @throws CarNotConnectedException
     */
    public void setFloatProperty(@PropertyId int propertyId, int area, float val)
            throws CarNotConnectedException {
        if (mHvacPropertyIds.contains(propertyId)) {
            mCarPropertyMgr.setFloatProperty(propertyId, area, val);
        }
    }

    /**
     * Set the value of an integer property
     * @param propertyId
     * @param area
     * @param val
     * @throws CarNotConnectedException
     */
    public void setIntProperty(@PropertyId int propertyId, int area, int val)
            throws CarNotConnectedException {
        if (mHvacPropertyIds.contains(propertyId)) {
            mCarPropertyMgr.setIntProperty(propertyId, area, val);
        }
    }
```

mCarPropertyMgr 是CarPropertyService的代理类。
- packages/services/Car/car-lib/src/android/car/hardware/property/CarPropertyManager.java
```
public class CarPropertyManager implements CarManagerBase { 
  .......
  
  ........
  public CarPropertyManager(IBinder service, @Nullable Handler handler, boolean dbg, String tag)   {
        mDbg = dbg;
        mTag = tag;
        mService = ICarProperty.Stub.asInterface(service); // 通过aidl接口拿到 CarPropertyService对象
        try {
            mConfigs = mService.getPropertyList();
        } catch (Exception e) {
            Log.e(mTag, "getPropertyList exception ", e);
            throw new RuntimeException(e);
        }
        if (handler == null) {
            mHandler = null;
            return;
        }
}
.....
```
- 上面set getProperty基本算是结束了，下面继续分析HAL Vehicle的回调，调到Java层通知Property的改变。
set getProperty Java framework总结：

1、carService：就是一个简单的service，主要就是拉起ICarImpl、get HAL VehicleService。

2、ICarImpl：这个可以说是一个Car AutoMotive中的一个service的管理类，负责拉起所有继承CarServiceBase的aidl “服务”，并初始化这些服务。

3、CarPropertyService：这个是一个aidl服务，应用层设置property主要就是调用这里提供的set get接口

4、CarPropertyManager：就是CarPropertyService的代理类

5、PropertyHalService：这里类起作用的应该是后面HAL service中property的changed通知。

6、HalClient：这个其实是真正set get调到到HAL层对象，在这个类里面对HAL Service做操作。
## 接下来就理一理，HAL service是如何通知到JAVA层（重点在Java层）
这里我们先稍微提一下，HAL service，在HAL Vehicle Service 中提供了一个接口类hardware/interfaces/automotive/vehicle/2.0/IVehicleCallback.hal -> interface IVehicleCallback 这里有提供了三个方法

oneway onPropertyEvent(vec propValues);

oneway onPropertySet(VehiclePropValue propValue);

oneway onPropertySetError(StatusCode errorCode, int32_t propId, int32_t areaId);

，java层只要继承这个类然后实现里面的方法，注册到 HAL Vehicle Service就可以了。
- packages/services/Car/service/src/com/android/car/hal/HalClient.java
```
class  HalClient {
        private final IVehicleCallback mInternalCallback;
        private final IVehicle mVehicle;

        HalClient(IVehicle vehicle, Looper looper, IVehicleCallback callback) {
        mVehicle = vehicle; // vehicle 来自 CarService 中 get的HAL vehicle service
        Handler handler = new CallbackHandler(looper, callback);
        mInternalCallback = new VehicleCallback(handler);
       }
       ......
       private static class VehicleCallback extends IVehicleCallback.Stub {
        private Handler mHandler;

        VehicleCallback(Handler handler) {
            mHandler = handler;
        }

        // HAL Vehicle service 中 Property发生变化通知
        @Override
        public void onPropertyEvent(ArrayList<VehiclePropValue> propValues) {
            mHandler.sendMessage(Message.obtain(
                    mHandler, CallbackHandler.MSG_ON_PROPERTY_EVENT, propValues));
        }

        @Override
        public void onPropertySet(VehiclePropValue propValue) {
            mHandler.sendMessage(Message.obtain(
                    mHandler, CallbackHandler.MSG_ON_PROPERTY_SET, propValue));
        }

        @Override
        public void onPropertySetError(int errorCode, int propId, int areaId) {
            mHandler.sendMessage(Message.obtain(
                    mHandler, CallbackHandler.MSG_ON_SET_ERROR,
                    new PropertySetError(errorCode, propId, areaId)));
        }
    }
    
    // 注册到 HAL Vehicle service 中
    public void subscribe(SubscribeOptions... options) throws RemoteException {
        mVehicle.subscribe(mInternalCallback, new ArrayList<>(Arrays.asList(options)));
    }
    
    // 从HAL Vehicle service中注销
    public void unsubscribe(int prop) throws RemoteException {
        mVehicle.unsubscribe(mInternalCallback, prop);
    }
}
```
HalClient并没有直接调用到CarPropertyService的 onPropertyChange（），而是通过 VehicleHal -> PropertyHalService -> CarPropertyService的

- packages/services/Car/service/src/com/android/car/hal/VehicleHal.java
```
public class VehicleHal extends IVehicleCallback.Stub { // VehicleHal 也是继承自IVehicleCallback但是它的onPropertyEvent回调 实际是来自HalClient。

    ......
    public VehicleHal(IVehicle vehicle) {
        mHandlerThread = new HandlerThread("VEHICLE-HAL");
        mHandlerThread.start();
        // passing this should be safe as long as it is just kept and not used in constructor
        mPowerHal = new PowerHalService(this);   // 下面这几个 PowerHalService PropertyHalService InputHalService VmsHalService DiagnosticHalService 都是继承自 HalServiceBase
        mPropertyHal = new PropertyHalService(this);
        mInputHal = new InputHalService(this);
        mVmsHal = new VmsHalService(this);
        mDiagnosticHal = new DiagnosticHalService(this);
        mAllServices.addAll(Arrays.asList(mPowerHal,
                mInputHal,
                mPropertyHal,
                mDiagnosticHal,
                mVmsHal));

        mHalClient = new HalClient(vehicle, mHandlerThread.getLooper(), this /*IVehicleCallback*/); //将自己注册到HalClient中 
    }
    ......

    @Override
    public void onPropertyEvent(ArrayList<VehiclePropValue> propValues) {
        synchronized (this) {
            for (VehiclePropValue v : propValues) {
                HalServiceBase service = mPropertyHandlers.get(v.prop);
                if(service == null) {
                    Log.e(CarLog.TAG_HAL, "HalService not found for prop: 0x"
                        + toHexString(v.prop));
                    continue;
                }
                service.getDispatchList().add(v);
                mServicesToDispatch.add(service);
                VehiclePropertyEventInfo info = mEventLog.get(v.prop);
                if (info == null) {
                    info = new VehiclePropertyEventInfo(v);
                    mEventLog.put(v.prop, info);
                } else {
                    info.addNewEvent(v);
                }
            }
        }
        for (HalServiceBase s : mServicesToDispatch) {  //调用HalServiceBase 的子类 public class PropertyHalService extends HalServiceBase， handleHalEvents（）方法
            s.handleHalEvents(s.getDispatchList());
            s.getDispatchList().clear();
        }
        mServicesToDispatch.clear();
    }

    @Override
    public void onPropertySet(VehiclePropValue value) {
        // No need to handle on-property-set events in HAL service yet.
    }

    @Override
    public void onPropertySetError(int errorCode, int propId, int areaId) {
        Log.e(CarLog.TAG_HAL, String.format("onPropertySetError, errorCode: %d, prop: 0x%x, "
                + "area: 0x%x", errorCode, propId, areaId));
        if (propId != VehicleProperty.INVALID) {
            HalServiceBase service = mPropertyHandlers.get(propId);
            if (service != null) {
                service.handlePropertySetError(propId, areaId);
            }
        }
    }
    ........
}
```

- packages/services/Car/service/src/com/android/car/hal/PropertyHalService.java
```
public class PropertyHalService extends HalServiceBase { 
......
    public interface PropertyHalListener { // CarPropertyService继承自 PropertyHalListener 接口，
        /**
         * This event is sent whenever the property value is updated
         * @param event
         */
        void onPropertyChange(List<CarPropertyEvent> events);
        /**
         * This event is sent when the set property call fails
         * @param property
         * @param area
         */
        void onPropertySetError(int property, int area);
    }
......
    @Override
    public void handleHalEvents(List<VehiclePropValue> values) {
        PropertyHalListener listener;
        synchronized (mLock) {
            listener = mListener;
        }
        if (listener != null) {
            for (VehiclePropValue v : values) {
                int mgrPropId = halToManagerPropId(v.prop);
                if (mgrPropId == NOT_SUPPORTED_PROPERTY) {
                    Log.e(TAG, "Property is not supported: 0x" + toHexString(v.prop));
                    continue;
                }
                CarPropertyValue<?> propVal = toCarPropertyValue(v, mgrPropId);
                CarPropertyEvent event = new CarPropertyEvent(
                        CarPropertyEvent.PROPERTY_EVENT_PROPERTY_CHANGE, propVal);
                if (event != null) {
                    mEventsToDispatch.add(event);
                }
            }
            listener.onPropertyChange(mEventsToDispatch); // 回调到 CarPropertyService的 onPropertyChange（）方法
            mEventsToDispatch.clear();
        }
    }
......
}
```

- packages/services/Car/service/src/com/android/car/CarPropertyService.java

```

public class CarPropertyService extends ICarProperty.Stub
        implements CarServiceBase, PropertyHalService.PropertyHalListener { // 继承自 ICarProperty.Stub，是一个aidl服务，CarServiceBase 方便ICarImpl管理拉起各个服务，PropertyHalService.PropertyHalListener 收onPropertyChange(List<CarPropertyEvent> events) 通知

    public CarPropertyService(Context context, PropertyHalService hal) {
        if (DBG) {
            Log.d(TAG, "CarPropertyService started!");
        }
        mHal = hal;  // hal 来自IcarImpl 中。主要是调用 PropertyHalService  setListener（PropertyHalListener listener） 注册
        mContext = context;
    }
    ......
        @Override
    public void onPropertyChange(List<CarPropertyEvent> events) { //来自PropertyHalService 中的回调
        Map<IBinder, Pair<ICarPropertyEventListener, List<CarPropertyEvent>>> eventsToDispatch =
                new HashMap<>();

        for (CarPropertyEvent event : events) {
            int propId = event.getCarPropertyValue().getPropertyId();
            List<Client> clients = mPropIdClientMap.get(propId);
            if (clients == null) {
                Log.e(TAG, "onPropertyChange: no listener registered for propId=0x"
                        + toHexString(propId));
                continue;
            }

            for (Client c : clients) {
                IBinder listenerBinder = c.getListenerBinder();
                Pair<ICarPropertyEventListener, List<CarPropertyEvent>> p =
                        eventsToDispatch.get(listenerBinder);
                if (p == null) {
                    // Initialize the linked list for the listener
                    p = new Pair<>(c.getListener(), new LinkedList<CarPropertyEvent>());
                    eventsToDispatch.put(listenerBinder, p);
                }
                p.second.add(event);
            }
        }
        // Parse the dispatch list to send events
        for (Pair<ICarPropertyEventListener, List<CarPropertyEvent>> p: eventsToDispatch.values()) {
            try {
                p.first.onEvent(p.second);
            } catch (RemoteException ex) {
                // If we cannot send a record, its likely the connection snapped. Let binder
                // death handle the situation.
                Log.e(TAG, "onEvent calling failed: " + ex);
            }
        }
    }

    @Override
    public void onPropertySetError(int property, int area) {
        List<Client> clients = mPropIdClientMap.get(property);
        if (clients != null) {
            List<CarPropertyEvent> eventList = new LinkedList<>();
            eventList.add(createErrorEvent(property, area));
            for (Client c : clients) {
                try {
                    c.getListener().onEvent(eventList);
                } catch (RemoteException ex) {
                    // If we cannot send a record, its likely the connection snapped. Let the binder
                    // death handle the situation.
                    Log.e(TAG, "onEvent calling failed: " + ex);
                }
            }
        } else {
            Log.e(TAG, "onPropertySetError called with no listener registered for propId=0x"
                    + toHexString(property));
        }
    }
    ......
        
}

```

至此以上就是围绕 CarPropertyService 分析了 如何 setProperty 、getProperty 以及HAL Vehicle Service的回调通知.
