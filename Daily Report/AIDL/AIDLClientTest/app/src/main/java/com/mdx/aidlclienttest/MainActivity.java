package com.mdx.aidlclienttest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mdx.aidltest.IStudentInterface;
import com.mdx.aidltest.ITaskCallback;
import com.mdx.aidltest.Student;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button button1, button2;
    TextView textView;

    private IStudentInterface si;
    private ITaskCallback mCallback;
    private String TAG = "MDX";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initListener();

        initCallback();
        attemptBind();

    }

    private void initCallback() {
        mCallback = new ITaskCallback.Stub() {
            @Override
            public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

            }

            @Override
            public void onSuccess(String result) throws RemoteException {
                Log.i(TAG, "result " + result);
            }

            @Override
            public void onFailed(String errorMsg) throws RemoteException {
                Log.e(TAG, "error " + errorMsg);
            }
        };
    }


    private void attemptBind() {
        Intent intent = new Intent();
        intent.setPackage("com.mdx.aidltest");
        intent.setAction("com.con.mdx.action");

        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                si = IStudentInterface.Stub.asInterface(iBinder);
                if(si == null){
                    Log.e(TAG,"si is null");
                    return ;
                }
                if ( mCallback != null) {
                    Log.i(TAG, "mCallback != null" + mCallback);
                    try {
                        si.register(mCallback);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.i(TAG, "mCallback == null");
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        }, BIND_AUTO_CREATE);
    }

    private void initListener() {
        button1.setOnClickListener(MainActivity.this);
        button2.setOnClickListener(MainActivity.this);
    }

    private void initView() {
        textView = findViewById(R.id.text);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button1:
                try {
                    textView.setText("www " + si.getStudentList().get(0).getId() + " " + si.getStudentList().get(0).getName());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.button2:
                try {
                    Log.i("MDX", "button2");
                    si.addStudent(new Student(123, "mdx"));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
        }
    }


}
