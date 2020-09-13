package com.pneck.employee.Activities;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.pneck.employee.LaunchActivityClass;
import com.pneck.employee.R;
import com.pneck.employee.SessionManager;
import com.pneck.employee.services.LocationService;
import com.pneck.employee.utills.Tools;

public class SplashScreen extends AppCompatActivity {

    SessionManager sessionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        Tools.setSystemBarColor(this, R.color.colorAccent);
        Tools.setSystemBarLight(this);

        sessionManager=new SessionManager(this);

        goToNextScreen();


    }

    public void goToNextScreen() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (sessionManager.isLoggedIn()){
                    if (sessionManager.getUserType()== SessionManager.UserType.TYPE_DELIVERY) LaunchActivityClass.LaunchMainActivity(SplashScreen.this);
                    else LaunchActivityClass.LaunchDriverDashboard(SplashScreen.this);
                }else {
                    //TODO : older flow
                    //LaunchActivityClass.LaunchChooseType(SplashScreen.this);

                    //TODO : new flow
                    LaunchActivityClass.LaunchSignInScreen(SplashScreen.this);
                }

            }
        }, 2000); // wait for 3 seconds
    }
}
