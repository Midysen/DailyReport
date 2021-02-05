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
- 这里的BeanID  "car.hvac.pm2.5_level"是由仙豆提供，需要提前确认是否存在，否则要提QA
```
public static final String DATA_ID_CAR_HVAC_PM2_5_LEVEL = "car.hvac.pm2.5_level";
```

- 这里要去建立BeanId对应的方法,仙豆写法
```
mRequestDataOperationList.put( DATA_ID_CAR_HVAC_PM2_5_VALUE        ,mdoGetCarHvacPm25Value);
```
- 实现方法
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

- CarHvacAdapter.java

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





