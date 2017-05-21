package com.fupan.sxdial;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import  android.os.Build;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    final String ConfigFile="info.cfg";
    EditText edit_account;
    EditText edit_pwd;
    Button btn_dial;
    Button btn_status;
    Thread dialThread;
    String acc="";
    String pwd="";
    String Host="";
    String router_psw="";
    WebView webv_status;


    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Toast.makeText(MainActivity.this,msg.what,Toast.LENGTH_SHORT).show();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SharedPreferences sharedPreferences=getSharedPreferences(ConfigFile,MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();

        Host=sharedPreferences.getString("Host","192.168.1.1");
        router_psw=sharedPreferences.getString("RouterPsw","");
        acc=sharedPreferences.getString("Acc","");
        pwd=sharedPreferences.getString("Pwd","");



        edit_account=(EditText) findViewById(R.id.account);
        edit_pwd=(EditText)findViewById(R.id.pwd);
        btn_dial=(Button)findViewById(R.id.dial);
        btn_status=(Button)findViewById(R.id.status);
        webv_status=(WebView)findViewById(R.id.webv_status);
        edit_account.setText(acc);
       edit_pwd.setText(pwd);
        btn_dial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                acc=edit_account.getText().toString();
                pwd=edit_pwd.getText().toString();
               // Toast.makeText(MainActivity.this,acc+"  "+pwd,Toast.LENGTH_SHORT).show();
                if(acc.isEmpty()||pwd.isEmpty())
                {
                    Toast.makeText(MainActivity.this,"输入为空！",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(Host.isEmpty()||router_psw.isEmpty())
                {
                    Toast.makeText(MainActivity.this,"配置信息不完整！",Toast.LENGTH_SHORT).show();
                    return;
                }
                //关闭输入法
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm.isActive()){
                    imm.hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                }

                //检测网络状态

                ConnectivityManager connManager=(ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo nInfo=connManager.getActiveNetworkInfo();
                if(nInfo!=null) {
                    if(nInfo.getType()!=connManager.TYPE_WIFI){
                        Toast.makeText(MainActivity.this,"WIFI好像没有连接哦！",Toast.LENGTH_SHORT).show();
                        return;
                    }

                }
                else
                {
                    Toast.makeText(MainActivity.this,"WIFI好像没有连接哦！",Toast.LENGTH_SHORT).show();
                    return;
                }


                //保存用户数据
                SaveConfig();

                dialThread = new Thread(new Runnable() {
                        @Override
                        public void run() {

                           Router.Dial(Host, router_psw, Account.getAccount(acc), pwd);

                            Looper.prepare();
                            Toast.makeText(MainActivity.this,"数据包已发送，请稍后查看网络连接情况！",Toast.LENGTH_SHORT).show();
                            Looper.loop();

                        }
                    });
                    dialThread.start();

            }


        });


        btn_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //关闭输入法
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm.isActive()){
                    imm.hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                }

                if(!Host.isEmpty()&&!router_psw.isEmpty()) {

                    webv_status.getSettings().setBuiltInZoomControls(false);
                    webv_status.getSettings().setJavaScriptEnabled(true);
                    webv_status.getSettings().setUseWideViewPort(true);
                    webv_status.getSettings().setLoadWithOverviewMode(true);
                    Map<String,String> extraHeaders=new HashMap<String, String>();
                    extraHeaders.put("Referer","http://"+Host+"/userRpm/StatusRpm.htm");


                    CookieSyncManager.createInstance(MainActivity.this);
                    CookieManager cookieManager=CookieManager.getInstance();
                    cookieManager.setAcceptCookie(true);
                    String url="http://"+Host+"/";

                    cookieManager.setCookie(url,Router.Authorization(router_psw));

                    if (Build.VERSION.SDK_INT < 21) {
                        CookieSyncManager.getInstance().sync();
                    } else {
                        CookieManager.getInstance().flush();
                    }
                    webv_status.setWebViewClient(new WebViewClient());
                    webv_status.loadUrl("http://"+Host+"/userRpm/StatusRpm.htm",extraHeaders);



                }
                else{
                    Toast.makeText(MainActivity.this,"配置信息不完整,无法查看路由器状况！",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }








    public void SaveConfig(){

        SharedPreferences sharedPreferences=getSharedPreferences(ConfigFile,MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy年MM月dd日 "+"hh:mm:ss");

        editor.putString("author","傅攀");
        editor.putString("version","闪讯路由拨号器（For Android）v 1.0 2017年5月18日");
        editor.putString("cfg-time",sdf.format(new Date()));
        editor.putString("Host",Host);
        editor.putString("RouterPsw",router_psw);
        editor.putString("Acc",edit_account.getText().toString());
        editor.putString("Pwd",edit_pwd.getText().toString());
        editor.commit();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {

            Intent intent=new Intent(MainActivity.this,SettingsActivity.class);
            intent.putExtra("host",Host);
            intent.putExtra("router_psw",router_psw);
            startActivityForResult(intent,0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode==0){
            Host=intent.getStringExtra("host");

            router_psw=intent.getStringExtra("router_psw");
            SaveConfig();
        }



    }
}
