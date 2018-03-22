package com.tom.yueap;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class ReceiveService extends Service {

    private ServerSocket server;
    private Intent sendintent;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static final String TAG = "ReceiveService";

    @Override
    public void onCreate(){
        try {
            server = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public int onStartCommand(Intent intent,int flag,int startID){

        sendintent = new Intent();
        sendintent.setAction("YueAp.receiveService");

        sendintent.putExtra("port",""+server.getLocalPort());
        LocalBroadcastManager.getInstance(this).sendBroadcast(sendintent);

        new Thread(){

            @Override
            public void run() {
                Socket socket;
                try {

                    while(true) {
                        socket = server.accept();

                        sendintent.putExtra("netip",""+socket.getInetAddress().getHostAddress().toString());

                        InputStreamReader reader = new InputStreamReader(socket.getInputStream());
                        int c;
                        StringBuilder sb = new StringBuilder();
                        while ((c = reader.read()) != -1) {
                            sb.append((char) c);
                        }
                        //发送广播
                        sendintent.putExtra("message", sb.toString());
                        LocalBroadcastManager.getInstance(ReceiveService.this).sendBroadcast(sendintent);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        return super.onStartCommand(intent,flag,startID);
    }

}
