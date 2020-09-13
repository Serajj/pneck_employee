package com.pneck.employee;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.pneck.employee.Activities.Registration.EmployeeSignInScreen;
import com.pneck.employee.utills.Tools;

public class ChooseType extends AppCompatActivity {
    Button button_driver, button_delivery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_type);
        Tools.setSystemBarColor(this, R.color.grey_5);
        Tools.setSystemBarLight(this);
        button_delivery = findViewById(R.id.btn_delivery_boy);
        button_driver = findViewById(R.id.btn_driver);


        button_driver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proceedAsDriver();
            }
        });

        button_delivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proceedAsDelivery();

            }
        });

    }

    private void proceedAsDelivery() {
        LaunchActivityClass.LaunchSignInScreen(ChooseType.this);
        finish();

    }

    private void proceedAsDriver() {
        LaunchActivityClass.LaunchSignInScreen(ChooseType.this);
        finish();
    }
}