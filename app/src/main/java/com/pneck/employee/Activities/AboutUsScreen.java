package com.pneck.employee.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.pneck.employee.LaunchActivityClass;
import com.pneck.employee.R;

public class AboutUsScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us_screen);

        findViewById(R.id.term_of_use).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle=new Bundle();
                bundle.putString("url","https://pneck.in/termsandconditions");
                bundle.putBoolean("is_privacy",false);
                LaunchActivityClass.LaunchWebScreen(AboutUsScreen.this,bundle);
            }
        });
        findViewById(R.id.go_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        findViewById(R.id.privacy_policy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle=new Bundle();
                bundle.putString("url","https://pneck.in/termsandconditions");
                bundle.putBoolean("is_privacy",true);
                LaunchActivityClass.LaunchWebScreen(AboutUsScreen.this,bundle);
                //Launch privacy policy
            }
        });
    }
}
