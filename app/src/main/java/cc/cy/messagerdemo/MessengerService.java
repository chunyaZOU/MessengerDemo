package cc.cy.messagerdemo;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;

/**
 * Created by zcy on 2018/6/6.
 */

public class MessengerService extends Service {

    //接收client消息并处理
    private Messenger mMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int i = 0;
            try {
                for (; ; ) {
                    //模拟耗时操作
                    Thread.sleep(2000);
                    i++;
                    Message msg2Client = Message.obtain();
                    msg2Client.what = i;
                    Bundle bundle = new Bundle();
                    bundle.putString("msg", "hello messenger");
                    msg2Client.setData(bundle);
                    //回送消息给客户端
                    msg.replyTo.send(msg2Client);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            super.handleMessage(msg);
        }
    });

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
}