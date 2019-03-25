package cc.cy.messagerdemo;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import cc.cy.messagerdemo.aidl.IMyAidlInterface;

/**
 * 跨进程通信
 * Messenger（也是基于AIDL的封装）
 * AIDL
 */
public class MainActivity extends AppCompatActivity {

    public static final int MSG_FROM_CLIENT_WHAT = 100;
    private TextView mTvMessenger, mTvAidl;
    //Messenger
    private Messenger mSerMessenger;
    //AIDL接口
    private IMyAidlInterface mAidlInterface;

    //Manifest中
    //接收Service消息进行处理
    private Messenger mMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mTvMessenger.setText(msg.what + msg.getData().getString("msg"));
            super.handleMessage(msg);
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvMessenger = findViewById(R.id.tvMessenger);
        mTvMessenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Message msg = Message.obtain();
                    msg.what = MSG_FROM_CLIENT_WHAT;
                    msg.replyTo = mMessenger;
                    //往服务端发消息
                    //只能发送序列化的消息（implements Parcelable）
                    mSerMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        });
        //绑定Messenger服务
        bindService(new Intent(this, MessengerService.class), mMessengerConnection, Service.BIND_AUTO_CREATE);

        mTvAidl = findViewById(R.id.tvAidl);
        mTvAidl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String msg = mAidlInterface.getStrFromAidl("Hello World");
                    mTvAidl.setText(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        //绑定AIDL服务
        bindService(new Intent(this, AidlService.class), mAidlConnection, Service.BIND_AUTO_CREATE);
    }


    private ServiceConnection mMessengerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //通过IBinder获取到服务端Messenger
            mSerMessenger = new Messenger(service);
            mTvMessenger.setText("Messenger Service Connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mTvMessenger.setText("Messenger Service Disconnected");
            mSerMessenger = null;
        }
    };

    private final ServiceConnection mAidlConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //通过接口获取服务端接口
            mAidlInterface = IMyAidlInterface.Stub.asInterface(service);
            mTvAidl.setText("AIDL Service Connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mTvAidl.setText("AIDL Service Disconnected");
            mAidlInterface = null;
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解绑服务
        unbindService(mMessengerConnection);
        unbindService(mAidlConnection);
    }
}
