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


public class MainActivity extends AppCompatActivity {

    public static final int MSG_FROM_CLIENT_WHAT = 100;
    private TextView mTextView;
    private Messenger mSerMessenger;
    private boolean isConnected;


    //接收Service消息进行处理
    private Messenger mMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mTextView.setText(msg.what + "");
            super.handleMessage(msg);
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.tv);
        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected) {
                    try {
                        Message msg = Message.obtain();
                        msg.what = MSG_FROM_CLIENT_WHAT;
                        msg.replyTo = mMessenger;
                        //往服务端发消息
                        mSerMessenger.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        //绑定服务
        bindService(new Intent(this, ServerService.class), mServiceConnection, Service.BIND_AUTO_CREATE);
    }


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mSerMessenger = new Messenger(service);
            mTextView.setText("Server connected");
            isConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isConnected = false;
            mSerMessenger = null;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解绑服务
        unbindService(mServiceConnection);
    }
}
