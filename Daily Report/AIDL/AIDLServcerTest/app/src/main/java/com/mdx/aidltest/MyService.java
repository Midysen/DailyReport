package com.mdx.aidltest;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import  com.mdx.aidltest.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MyService extends Service {
    private CopyOnWriteArrayList<Student> list;
    private static RemoteCallbackList<ITaskCallback> sCallbackList;

    @Override
    public void onCreate() {
        super.onCreate();
        list = new CopyOnWriteArrayList<Student>();
        sCallbackList = new RemoteCallbackList<>();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new MyBinder();
    }


    class MyBinder extends  IStudentInterface.Stub{


        @Override
        public List<Student> getStudentList() throws RemoteException {
            if(list.isEmpty()){
                list = new CopyOnWriteArrayList<Student>();
            }
            Log.i("MDX","getStudentList");
            return list;
        }

        @Override
        public void addStudent(Student stu) throws RemoteException {
            if(list.contains(stu)){
                Log.i("MDX", "list contains Student" + stu.getName() + " " + stu.getId());
                return ;
            }
            if (stu == null) {
                Log.i("MDX","stu  is null");
                dispatchResult(false, "add student failed, mStuList = null");
            } else {
                dispatchResult(true, "add student "+stu.getName() + " " + stu.getId()+ " success!");
                Log.i("MDX", "addStudent" + stu.getName() + " " + stu.getId());
                list.add(stu);
            }
        }

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void register(ITaskCallback callback) throws RemoteException {
            if (callback == null) {
                Log.e("MDX","register callback is null");
                return;
            }
            sCallbackList.register(callback);
        }

        @Override
        public void unregister(ITaskCallback callback) throws RemoteException {
            if (callback == null) {
                Log.e("MDX","unregister callback is null");
                return;
            }
            sCallbackList.unregister(callback);
        }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String[] packages = getPackageManager().getPackagesForUid(getCallingUid());
            String pkgName = null;
            if (packages != null && packages.length > 0) {
                pkgName = packages[0];
            }
            if (TextUtils.isEmpty(pkgName) || !pkgName.startsWith("com.mdx.aidlclienttest")) {
                Log.i("MDX", "invalid pkgName : " + pkgName);
                return false;
            }
            return super.onTransact(code, data, reply, flags);
        }
    }

    private void dispatchResult(boolean b, String s) {
        int length = sCallbackList.beginBroadcast();
        for (int i = 0; i < length; i++) {
            ITaskCallback callback = sCallbackList.getBroadcastItem(i);
            if (b) {
                try {
                    callback.onSuccess(s);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    callback.onFailed(s);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
        }
        sCallbackList.finishBroadcast();
    }

}
