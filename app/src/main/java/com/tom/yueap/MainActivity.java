package com.tom.yueap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;



public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ConnectivityManager connectivityManager = null;
    private NetworkInfo networkInfo = null;
    private BroadcastReceiver broadcastReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //运行服务接受信息
        startService(new Intent(this,ReceiveService.class));
        //注册广播接受器
        broadcastReceiver = new BroadcastMain();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("YueAp.receiveService");
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,intentFilter);

        connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo==null){
            Toast.makeText(MainActivity.this,"无网络连接",Toast.LENGTH_SHORT).show();
        }
        else{
            String m = getIP();
            TextView localip = findViewById(R.id.ipTextView);
            localip.setText(m);

        }

    }

    public void beginConnect(View view) {

        EditText netIPet = findViewById(R.id.netipeditText);
        EditText netportet = findViewById(R.id.netporteditText);
        TextView localport = findViewById(R.id.portTextView);
        TalkingActivity.sendMessage(netIPet.getText().toString(),Integer.parseInt(netportet.getText().toString()),""+localport.getText());

        Intent intent = new Intent(MainActivity.this,TalkingActivity.class);
        intent.putExtra("netip",netIPet.getText().toString());
        intent.putExtra("netport",netportet.getText().toString());
        startActivity(intent);
    }

    public class BroadcastMain extends BroadcastReceiver{

        private String netip;
        private String netport;

        @Override
        public void onReceive(Context context, Intent intent) {
            TextView localport = findViewById(R.id.portTextView);
            localport.setText(intent.getStringExtra("port"));

            if (intent.getStringExtra("netip")!=null){

                netip = intent.getStringExtra("netip");
                netport = intent.getStringExtra("message");

                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setTitle("连接请求");
                dialog.setMessage("收到来自"+intent.getStringExtra("netip")+"连接请求");
                dialog.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Intent aintent = new Intent(MainActivity.this,TalkingActivity.class);
                        aintent.putExtra("netip",BroadcastMain.this.netip);
                        aintent.putExtra("netport",BroadcastMain.this.netport);
                        startActivity(aintent);

                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                dialog.show();
            }
        }
    }

    @Override
    public void onDestroy(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    private static String getIP(){
        try{
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            while(enumeration.hasMoreElements()){
                NetworkInterface intf = enumeration.nextElement();
                Enumeration<InetAddress> enumIPAddr = intf.getInetAddresses();
                while(enumIPAddr.hasMoreElements()){
                    InetAddress inetAddress = enumIPAddr.nextElement();
                    if(!inetAddress.isLoopbackAddress()&&inetAddress instanceof Inet4Address)
                        if(inetAddress.getHostAddress().toString().startsWith("192.168"))
                            return inetAddress.getHostAddress().toString();
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

}
