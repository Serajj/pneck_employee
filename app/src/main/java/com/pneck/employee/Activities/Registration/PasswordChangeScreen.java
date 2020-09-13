package com.pneck.employee.Activities.Registration;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.pneck.employee.R;
import com.pneck.employee.utills.Tools;

public class PasswordChangeScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_change_screen);
        Tools.setSystemBarColor(this, R.color.grey_5);
        Tools.setSystemBarLight(this);
    }
}
