package com.sample.login;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
public class SplashActivity2 extends AppCompatActivity{

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash2);

        //ImageView splash = (ImageView) findViewById(R.id.splash);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity2.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        },1000);//x*1000   x초간 보여줌

    }
}
