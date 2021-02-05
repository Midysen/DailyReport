# 新增PM2.5等级数据显示
由于PM2.5属于空调模块，所以查看的是包android.car.hardware.hvac，首先看CAR工程下的manager
## GwmCarHvacManager.java 
```
public static final int ID_GWM_T_Box_FD1_T_Box_PM25_LEVEL = VehicleProperty.GWM_T_Box_FD1_T_Box_PM25AirQLvl;
```
- 这里的ID_GWM_T_Box_FD1_T_Box_PM25_LEVEL是自定义的，查看相应SRS知道，3.1.3.19	[SWR18_001_31]PM2.5显示，信号名是T_Box_PM25AirQLvl，
在10.255.33.240 / q8155p / es13 / Android / platform / hardware / interfaces / refs/heads/gwm_v35_es16_dev / . / automotive / vehicle / 2.0 / types.hal
文件中，知道HAL提供的信号是GWM_T_Box_FD1_T_Box_PM25AirQLvl，所以这里完成一个自定义，int值一般是在HAL信号前加ID

- 在 ID_GWM_T_BOX_FD1_T_BOX_PM25_DENS对应位置下分别加入新添加的"ID_GWM_T_Box_FD1_T_Box_PM25_LEVEL"
     
     
#  再看工程BeanAdapterServer下的CarHvacAdapter.java
- 如果需要HAL层数据回传到FW及APP，需要添加Callback
```
private GwmCarHvacManager.GwmCarHvacEventCallback mHvacEventCallback = new GwmCarHvacManager.GwmCarHvacEventCallback() {
......

case GwmCarHvacManager.ID_GWM_T_BOX_FD1_T_BOX_PM25_DENS:
                    case GwmCarHvacManager.ID_GWM_T_BOX_FD1_T_BOX_PM25_LEVEL:
                    case GwmCarHvacManager.ID_GWM_AC2_ACHMIDISPCMD:
                    case GwmCarHvacManager.ID_GWM_AC1_ACDRVTEMPSTEPLESSSETENASTS:
                    case GwmCarHvacManager.ID_GWM_AC1_ACFRNTPASSTEMPSTEPLESSSETENASTS:
                    case GwmCarHvacManager.ID_GWM_AC1_ACSTEPLESSSPDSETENASTS:
                        dataValue = String.valueOf((int) value.getValue());
                        break;
                        
        ...........


```


- CarHvacDataManager

```
mGetDataIDMap.put(DATA_ID_CAR_HVAC_PM2_5_VALUE, GwmCarHvacManager.ID_GWM_T_BOX_FD1_T_BOX_PM25_DENS);
mGetDataIDMap.put(DATA_ID_CAR_HVAC_PM2_5_LEVEL, GwmCarHvacManager.ID_GWM_T_BOX_FD1_T_BOX_PM25_LEVEL);

```
这里添加到MAP表中的目的是将 BeanID 与 property id  对应，因为APP层是通过BeanID获取数据的

- CarHvacDataManager
```
 mCallbackDataIdMap.put(GwmCarHvacManager.ID_GWM_T_BOX_FD1_T_BOX_PM25_DENS, DATA_ID_CAR_HVAC_PM2_5_VALUE);
 mCallbackDataIdMap.put(GwmCarHvacManager.ID_GWM_T_BOX_FD1_T_BOX_PM25_LEVEL, DATA_ID_CAR_HVAC_PM2_5_LEVEL);
```
这里添加到MAP表中的目的是将 property id  与BeanID 对应，底层数据发生变化是改变的property id对应的值


# MyDataAdapter.java
## 这里的BeanID  "car.hvac.pm2.5_level"是由仙豆提供，需要提前确认是否存在，否则要提QA
```
public static final String DATA_ID_CAR_HVAC_PM2_5_LEVEL = "car.hvac.pm2.5_level";
```

## 这里要去建立BeanId对应的方法,仙豆写法
```
mRequestDataOperationList.put( DATA_ID_CAR_HVAC_PM2_5_VALUE        ,mdoGetCarHvacPm25Value);
```
## 实现方法
```
 IRequestDataOperation    mdoGetCarHvacPm25Value = (String dataId) -> {
        Log.i(TAG, "doGetCarHvacPm25Value:"+ dataId);
        mCarHvacAdapter.doGetCarHvacPm25Value(dataId);
    };
 IRequestDataOperation    mdoGetCarHvacPm25Level = (String dataId) -> {
     Log.i(TAG, "doGetCarHvacPm25Value:"+ dataId);
     mCarHvacAdapter.doGetCarHvacPm25Level(dataId);
 };
```

## CarHvacAdapter.java

```
 public void doGetCarHvacPm25Value(String dataId) {

        Log.i(TAG, "doGetCarHvacPm25Value");

//      $$DOGETCARHVACPM25VALUE$$START
        doGetIntProperty(dataId, false);
//      $$DOGETCARHVACPM25VALUE$$END

    }

    public void doGetCarHvacPm25Level(String dataId) {

        Log.i(TAG, "doGetCarHvacPm25Value");

//      $$DOGETCARHVACPM25VALUE$$START
        doGetIntProperty(dataId, false);
//      $$DOGETCARHVACPM25VALUE$$END

    }
```

继续跟踪

```

private void doGetIntProperty(String dataId, boolean isInit) {

        Log.i(TAG, "doGetIntProperty");

        if (!initGwmHvacManager()) {
            Log.d(TAG, "doGetIntProperty: initGwmHvacManager false");
            return;
        }

        int propId = mCarHvacDataManager.getPropertyIdForGet(dataId);
        if (propId == CarHvacDataManager.NO_DATA) {
            Log.e(TAG, "doGetIntProperty: propId is empty!");
            return;
        }

        try {
            boolean isAvailable = mGwmHvacManager.isPropertyAvailable(propId, 0);
            String beanValue = STATUS_UNAVAILABLE_BEAN;
            if (isAvailable) {
                int dataValue = mGwmHvacManager.getIntProperty(propId, 0);
                beanValue = String.valueOf(dataValue);
                if (propId == GwmCarHvacManager.ID_GWM_AC2_ACAIRINLETSTS) {
                    if (dataValue != GwmCarHvacPropertyValues.GWM_AC2_ACAIRINLETSTS.GWM_AC2_ACAIRINLETSTS_0) { //if ACAirInletSts =0x1(Fresh), ACAirInletSts=0x2(Auto) or ACAirInletSts =0x3(Initial value), Fresh
                        beanValue = CarHvacDataManager.CYCLE_MODE_FRESH_VALUE_BEAN;
                    } else {
                        beanValue = CarHvacDataManager.CYCLE_MODE_RECIRCULATION_VALUE_BEAN;
                    }
                }
            } else {
                if (isInit) {
                    beanValue = CarHvacDataManager.DEFAULT_CLEAR_BEAN_STRING;
                } else {
                    Log.i(TAG, "doGetIntProperty: no action");
                    return;
                }
            }
            Log.i(TAG, "doGetIntProperty: setData beanId=" + dataId + " beanValue=" + beanValue);
            PlatformAdapterServer.getInstance().getDataApdater().setData(dataId, beanValue);
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
    }

```
### 这里重点分析相应逻辑
- 1、
```
 int propId = mCarHvacDataManager.getPropertyIdForGet(dataId);
```
  - getPropertyIdForGet
  ```
   public int getPropertyIdForGet(String beanId) {
        Integer propertyID = mGetDataIDMap.get(beanId);
        return propertyID == null ? NO_DATA : propertyID;
    }
  ```
这里的mGetDataIDMap就是之前创建的beanId与property id的一一映射表

- 2、
```
 int dataValue = mGwmHvacManager.getIntProperty(propId, 0);
```
### 这里的getIntProperty方法是GwmHvacManager中的，所以我们又回到了CAR工程

- GwmHvacManager.java
```
 public int getIntProperty(@PropertyId int propertyId, int area)
            throws CarNotConnectedException {
        if (DBG) {
            Log.d(TAG, "getIntProperty propertyId=" + propertyId + " area=" + area);
        }
        return mCarPropertyMgr.getIntProperty(propertyId, area);
    }
```
- CarPropertyManager.java
```
 /**
     * Returns value of a integer property
     *
     * @param prop Property ID to get
     * @param area Zone of the property to get
     */
    public int getIntProperty(int prop, int area) throws CarNotConnectedException {
        CarPropertyValue<Integer> carProp = getProperty(Integer.class, prop, area);
        return carProp != null ? carProp.getValue() : 0;
    }

```

继续跟进
```
    public <E> CarPropertyValue<E> getProperty(Class<E> clazz, int propId, int area)
            throws CarNotConnectedException {
        if (mDbg) {
            Log.d(mTag, "getProperty, propId: 0x" + toHexString(propId)
                    + ", area: 0x" + toHexString(area) + ", class: " + clazz);
        }
        try {
            CarPropertyValue<E> propVal = mService.getProperty(propId, area);
            if (propVal != null && propVal.getValue() != null) {
                Class<?> actualClass = propVal.getValue().getClass();
                if (actualClass != clazz) {
                    throw new IllegalArgumentException("Invalid property type. " + "Expected: "
                            + clazz + ", but was: " + actualClass);
                }
            }
            return propVal;
        } catch (RemoteException e) {
            Log.e(mTag, "getProperty failed with " + e.toString()
                    + ", propId: 0x" + toHexString(propId) + ", area: 0x" + toHexString(area), e);
            throw new CarNotConnectedException(e);
        }
    }
```

- mService类型是ICarProperty，实际在ICarProperty.aidl中，这个接口的实现在CarPropertyService.java

- CarPropertyService.java
```
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

```
这里要去分析mHal.getProperty(prop, zone)

- PropertyHalService.java
```
 /**
     * Returns property or null if property is not ready yet.
     * @param mgrPropId
     * @param areaId
     */
    @Nullable
    public CarPropertyValue getProperty(int mgrPropId, int areaId) {
        int halPropId = managerToHalPropId(mgrPropId);
        if (halPropId == NOT_SUPPORTED_PROPERTY) {
            throw new IllegalArgumentException("Invalid property Id : 0x" + toHexString(mgrPropId));
        }

        VehiclePropValue value = null;
        try {
            value = mVehicleHal.get(halPropId, areaId);
        } catch (PropertyTimeoutException e) {
            Log.e(CarLog.TAG_PROPERTY, "get, property not ready 0x" + toHexString(halPropId), e);
        }

        return value == null ? null : toCarPropertyValue(value, mgrPropId);
    }

```

这里要去分析mVehicleHal.get(halPropId, areaId);


- VehicleHal.java

```
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
```
这里要去分析 mHalClient.getValue(propValue);

- HalClient.java

```

 VehiclePropValue getValue(VehiclePropValue requestedPropValue) throws PropertyTimeoutException {
        final ObjectWrapper<VehiclePropValue> valueWrapper = new ObjectWrapper<>();
        int status = invokeRetriable(() -> {
            ValueResult res = internalGet(requestedPropValue);
            valueWrapper.object = res.propValue;
            return res.status;
        }, WAIT_CAP_FOR_RETRIABLE_RESULT_MS, SLEEP_BETWEEN_RETRIABLE_INVOKES_MS);

        int propId = requestedPropValue.prop;
        int areaId = requestedPropValue.areaId;
        if (StatusCode.INVALID_ARG == status) {
            throw new IllegalArgumentException(
                    String.format("Failed to get value for: 0x%x, areaId: 0x%x", propId, areaId));
        }

        if (StatusCode.TRY_AGAIN == status) {
            throw new PropertyTimeoutException(propId);
        }

        if (StatusCode.OK != status || valueWrapper.object == null) {
            throw new IllegalStateException(
                    String.format("Failed to get property: 0x%x, areaId: 0x%x, "
                            + "code: %d", propId, areaId, status));
        }

        return valueWrapper.object;
    }
```
这里去看ValueResult res = internalGet(requestedPropValue);

-  HalClient.java
```
 private ValueResult internalGet(VehiclePropValue requestedPropValue) {
        final ValueResult result = new ValueResult();
        try {
            mVehicle.get(requestedPropValue,
                    (status, propValue) -> {
                        result.status = status;
                        result.propValue = propValue;
                    });
        } catch (RemoteException e) {
            Log.e(CarLog.TAG_HAL, "Failed to get value from vehicle HAL", e);
            result.status = StatusCode.TRY_AGAIN;
        }

        return result;
    }
```
这里重点去看
```
            mVehicle.get(requestedPropValue,
                    (status, propValue) -> {
                        result.status = status;
                        result.propValue = propValue;
                    });
```
跟踪发现IVehicle是在这里导入的` import android.hardware.automotive.vehicle.V2_0.IVehicle;`，而此代码是有诺博也就是HAL提供，我们看不到源码。我们只能根据在需要编译的Android整体源码目录下
去找到相应的jar包，反编译可以看得到里面提供的方法。

