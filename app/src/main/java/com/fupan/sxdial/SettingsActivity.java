package com.fupan.sxdial;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

/**
 * Created by fupan on 2017/5/18.
 */

public class SettingsActivity extends Activity {

    LinearLayout settings_view;

    static float mPosX=0,mPosY=0,mCurPosX=0,mCurPosY=0,mPrePosX=0,mPrePosY=0,top;
    int height,bottom;
    boolean first=true;
    boolean Returned=false;
    boolean added=false;

    String host="",router_psw="";

    EditText edit_host,edit_router_psw;
    Button btn_save;
    ImageButton down;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        host=this.getIntent().getStringExtra("host");
        router_psw=this.getIntent().getStringExtra("router_psw");

        edit_host=(EditText)findViewById(R.id.host);
        edit_router_psw=(EditText)findViewById(R.id.router_psw);
        btn_save=(Button)findViewById(R.id.save);
        down=(ImageButton)findViewById(R.id.down);
        edit_host.setText(host);
        edit_router_psw.setText(router_psw);
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHide();
            }
        });
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=getIntent();
                intent.putExtra("host",edit_host.getText().toString());
                intent.putExtra("router_psw",edit_router_psw.getText().toString());
                SettingsActivity.this.setResult(0,intent);
                Returned=true;
                DialogHide();
            }
        });

        settings_view=(LinearLayout)findViewById(R.id.settings_view);



        settings_view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(first){
                    height=settings_view.getHeight();
                    bottom=settings_view.getBottom();
                    first=false;
                }


                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        mPosX = event.getRawX();
                        mPosY = event.getRawY();
                        mPrePosX=mPosX;
                        mPrePosY=mPosY;
                        mCurPosX=mPosX;
                        mCurPosY=mPosY;
                        top=settings_view.getTop();


                      //  Log.v("pos",flag+" "+String.valueOf(mPosX)+" "+String.valueOf(mPosY));
                        break;
                    case MotionEvent.ACTION_MOVE:

                        mPrePosX=mCurPosX;
                        mPrePosY=mCurPosY;

                        mCurPosX = event.getRawX();
                        mCurPosY = event.getRawY();




                        float offsetY=mPrePosY-mCurPosY;
                        if(offsetY>0){
                            offsetY=offsetY*(settings_view.getTop()/top);       //top为整型 会出错
                        }
                        if(offsetY<-100){
                            DialogHide();
                        }




                        int l=settings_view.getLeft();
                        int t=(int)(settings_view.getTop()-offsetY);
                        int r=settings_view.getWidth()+l;
                        int b=settings_view.getBottom();

                       // Log.v("offset",String.valueOf(offsetX)+" "+String.valueOf(offsetY));
               //         Log.v("pos",String.valueOf(mCurPosX)+" "+String.valueOf(mCurPosY));
                        settings_view.layout(l,t,r,b);

                        if(settings_view.getHeight()<height){
                            DialogHide();
                       }


                        if(settings_view.getHeight()>(height+200)&&!added)
                        {


                            ((TextView)findViewById(R.id.extra_text)).setVisibility(View.VISIBLE);


                        }


                    case MotionEvent.ACTION_UP:



                        break;
                }

                return false;
            }
        });
    }

    void DialogHide(){
        //int t=settings_view.getTop();

          //  settings_view.layout(settings_view.getLeft(),t,settings_view.getRight(),settings_view.getBottom());
        if(!Returned)
        {
            SettingsActivity.this.setResult(1);
        }

        ObjectAnimator animator=new ObjectAnimator();

        animator.ofFloat(settings_view,"translationY",0,settings_view.getHeight()).setDuration(500).start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                SettingsActivity.this.finish();
            }
        },500);



    }




}
