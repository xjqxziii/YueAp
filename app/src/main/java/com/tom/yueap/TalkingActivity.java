package com.tom.yueap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TalkingActivity extends AppCompatActivity {

    private BroadcastTalk broadcastReceiver;
    private static final String TAG = "TalkingActivity";
    private String netIP;
    private String netport;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talking);

        broadcastReceiver = new BroadcastTalk();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("YueAp.receiveService");
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,intentFilter);

        Intent intent = getIntent();
        netIP = intent.getStringExtra("netip");
        netport = intent.getStringExtra("netport");
    }

    public void beginSend(View view) {
        EditText ed = findViewById(R.id.sendeditText);
        sendMessage(netIP,Integer.parseInt(netport),ed.getText().toString());
    }

    public class BroadcastTalk extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            TextView message = findViewById(R.id.receivetextView);
            message.setText(intent.getStringExtra("message"));;
        }
    }

    static void sendMessage(final String remoteIP, final int port, final String message) {

        new Thread(){
            InetSocketAddress isa = new InetSocketAddress(remoteIP, port);
            @Override
            public void run() {
                try {
                    Socket socket = new Socket();
                    socket.connect(isa);
                    OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());
                    writer.write(message);
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void onDestroy(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
}
