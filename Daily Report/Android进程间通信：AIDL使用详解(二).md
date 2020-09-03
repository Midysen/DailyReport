# 回调机制
在基本用法中，只实现了客户端向服务端发送调用请求的单向通信，但在很多场景下，同时也需要实现服务端主动向客户端发送数据进行双向通信，比如在观察者模式中，当有多个客户端绑定服务端，如果想要实现在服务端数据变化时主动通知所有与它建立绑定的客户端时，这个时候就需要用到AIDL的回调机制了。

在服务端aidl文件夹下新建一个AIDL文件，用于定义回调接口，并声明onSuccess和onFailed方法，这两个方法是用于业务层的，比如服务端添加数据失败时调用onFailed，取决于具体场景：
```
// ITaskCallback.aidl
package com.sqchen.aidltest;

interface ITaskCallback {

void onSuccess(String result);

void onFailed(String errorMsg);
}
```
- 修改IStudentService.aidl，添加register和unregister方法用于客户端注册回调和解除回调：
```
// IStudentService.aidl
package com.sqchen.aidltest;

import com.sqchen.aidltest.Student;
//注意：aidl接口也要显式import
import com.sqchen.aidltest.ITaskCallback;

interface IStudentService {

List<Student> getStudentList();

void addStudent(inout Student student);

void register(ITaskCallback callback);

void unregister(ITaskCallback callback);
}
```
- 修改StudentService.java：
```
package com.sqchen.aidltest;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class StudentService extends Service {

private static final String TAG = "StudentService";

private CopyOnWriteArrayList<Student> mStuList;

private static RemoteCallbackList<ITaskCallback> sCallbackList;

private Binder mBinder = new IStudentService.Stub() {

    @Override
    public void register(ITaskCallback callback) throws RemoteException {
        if (callback == null) {
            Log.i(TAG, "callback == null");
            return;
        }
        sCallbackList.register(callback);
    }

    @Override
    public void unregister(ITaskCallback callback) throws RemoteException {
        if (callback == null) {
            return;
        }
        sCallbackList.unregister(callback);
    }

    @Override
    public List<Student> getStudentList() throws RemoteException {
        return mStuList;
    }

    @Override
    public void addStudent(Student student) throws RemoteException {
        if (mStuList == null) {
            dispatchResult(false, "add student failed, mStuList = null");
        } else {
            mStuList.add(student);
            dispatchResult(true, "add student successfully");
        }
    }
};

@Override
public void onCreate() {
    super.onCreate();
    Log.i(TAG, "onCreate");
    init();
}

private void init() {
    mStuList = new CopyOnWriteArrayList<>();
    sCallbackList = new RemoteCallbackList<>();
}

/**
 * 分发结果
 * @param result
 * @param msg
 */
private void dispatchResult(boolean result, String msg) {
    int length = sCallbackList.beginBroadcast();
    for (int i = 0; i < length; i++) {
        ITaskCallback callback = sCallbackList.getBroadcastItem(i);
        try {
            if (result) {
                callback.onSuccess(msg);
            } else {
                callback.onFailed(msg);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    sCallbackList.finishBroadcast();
}

@Override
public IBinder onBind(Intent intent) {
    return mBinder;
}
}
```
在StudentService.java中，Binder对象实现了IStudentService.aidl中新声明的两个方法，register和unregister，并创建了一个RemoteCallbackList

RemoteCallbackList 是系统专门提供的用于跨进程传递callback的一种接口，这个接口是泛型，支持管理所有AIDL接口。这里不能使用普通的List来存放callback，
因为在进程间通信时，客户端的List对象和服务端接收到的List对象不在不同的内存空间中。正是因为不是在同一个内存空间中，不同进程之间的数据不能进行共享，
所以才有进程间通信这个机制。

- 那么，为什么RemoteCallbackList能实现传输前后都是相同对象呢？查看RemoteCallbackList源码可以发现，其内部创建了一个ArrayMap用于保存callback：

ArrayMap mCallbacks = new ArrayMap();
这个Map的key是IBinder对象，而value是Callback对象，当客户端通过register方法注册回调时，将callback传递给服务端，服务端再通过RemoteCallbackList.register方法真正将回调进行保存：
```
//RemoteCallbackList
public boolean register(E callback, Object cookie) {

synchronized (mCallbacks) {
    if (mKilled) {
        return false;
    }
    // Flag unusual case that could be caused by a leak. b/36778087
    logExcessiveCallbacks();
    IBinder binder = callback.asBinder();
    try {
        Callback cb = new Callback(callback, cookie);
        binder.linkToDeath(cb, 0);
        mCallbacks.put(binder, cb);
        return true;
    } catch (RemoteException e) {
        return false;
    }
}
}
```
将我们关心的部分抽出来：
```
IBinder binder = callback.asBinder();
Callback cb = new Callback(callback, cookie);
mCallbacks.put(binder, cb);
```
将客户端传递过来的Callback对象转为IBinder对象作为key，封装一个Callback作为value。客户端传递过来的Callback对象虽然在服务端被重新序列化生成一个对象，但它们底层的Binder对象是同一个，所以可以实现Callback的跨进程传输。

- 在服务端注册客户端的回调后，服务端就可以通过这个回调主动向客户端传递数据了。比如，在addStudent中，当添加数据成功时，将操作的执行结果或者其他数据分发给所有向该服务端注册监听的客户端：
```
/**

    分发结果@param result@param msg
    */

private void dispatchResult(boolean result, String msg) {

int length = sCallbackList.beginBroadcast();
for (int i = 0; i < length; i++) {
    ITaskCallback callback = sCallbackList.getBroadcastItem(i);
    try {
        if (result) {
            callback.onSuccess(msg);
        } else {
            callback.onFailed(msg);
        }
    } catch (RemoteException e) {
        e.printStackTrace();
    }
}
//在调用beginBroadcast之后，必须调用该方法
sCallbackList.finishBroadcast();
}
```
- 在客户端中创建ITaskCallback对象：
```
//MainActivity.java

ITaskCallback mCallback = new ITaskCallback.Stub() {

@Override
public void onSuccess(String result) throws RemoteException {
    Log.i(TAG, "result = " + result);
}

@Override
public void onFailed(String errorMsg) throws RemoteException {
    Log.e(TAG, "errorMsg = " + errorMsg);
}
};

修改ServiceConnection，在建立连接、调用onServiceConnected方法时，进行Callback的注册：

private ServiceConnection mConnection = new ServiceConnection() {

@Override
public void onServiceConnected(ComponentName name, IBinder service) {
    mStudentService = IStudentService.Stub.asInterface(service);
    if (mStudentService == null) {
        Log.i(TAG, "mStudentService == null");
        return;
    }
    try {
        if (mCallback != null) {
            Log.i(TAG, "mCallback != null");
            mStudentService.register(mCallback);
        } else {
            Log.i(TAG, "mCallback == null");
        }
    } catch (RemoteException e) {
        e.printStackTrace();
    }
}

@Override
public void onServiceDisconnected(ComponentName name) {

}
};
```
此时，客户端与服务端的连接已经建立，且客户端向服务端注册了回调，当客户端向服务端添加数据，服务端执行addStudent方法时，服务端会通过回调将添加数据的执行结果返回给客户端，从而实现了双向通信。
## 权限验证

默认情况下，如果没有加入权限验证功能，那么我们的远程服务是所有进程都可以进行连接的，从系统安全性的角度出发，我们还需要有相应的权限验证机制来保证系统的安全，有两种方式：

- 1、在建立连接之前

在客户端通过bindService方法绑定远程服务时，我们会在服务端的onBind方法中将Binder对象返回给客户端，那么我们可以在onBind方法中对来自客户端的请求进行权限验证。

- 2、在客户端请求执行服务端的AIDL方法时

实际上，每个AIDL方法都有一个唯一的方法标识code，服务端在Binder.onTransact中根据这个code判断并确定客户端想要调用的是哪个AIDL方法，所以，我们可以在Binder.onTransact中进行权限验证，拦截非法的客户端调用。

常用的权限验证机制有包名验证和权限验证，即根据客户端的包名或所声明的权限是否符合服务端要求来进行验证。

修改StudentService.java中的Binder对象：
```
private Binder mBinder = new IStudentService.Stub() {


...

@Override
public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
    //包名验证
    String[] packages = getPackageManager().getPackagesForUid(getCallingUid());
    String pkgName = null;
    if (packages != null && packages.length > 0) {
        pkgName = packages[0];
    }
    if (TextUtils.isEmpty(pkgName) || !pkgName.startsWith("com.sqchen")) {
        Log.i(TAG, "invalid pkgName : " + pkgName);
        return false;
    }
    return super.onTransact(code, data, reply, flags);
}
};
```
这样，如果客户端的包名不是以"com.sqchen"开头的话，则认为是非法请求，在onTranscat中返回false将使得客户端的请求失败，从而达到权限验证的目的。
