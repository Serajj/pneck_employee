package com.pneck.employee;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.pneck.employee.Activities.AboutUsScreen;
import com.pneck.employee.Activities.DriverDashboard;
import com.pneck.employee.Activities.EditProfileScreen;
import com.pneck.employee.Activities.EmployeeFeedbackScreen;
import com.pneck.employee.Activities.EmployeeRides;
import com.pneck.employee.Activities.HelpScreen;
import com.pneck.employee.Activities.OrderProcess.DeliveryCompleteOTPVerification;
import com.pneck.employee.Activities.OrderProcess.JobAcceptScreen;
import com.pneck.employee.Activities.OrderProcess.JobDetailAddByEmployee;
import com.pneck.employee.Activities.OrderProcess.JobRealTimeTrackingScreen;
import com.pneck.employee.Activities.OrderProcess.Job_OTP_VerifyScreen;
import com.pneck.employee.Activities.OrderProcess.OrderCompleteHappyScreen;
import com.pneck.employee.Activities.OrderProcess.PaymentStatusPendingStage;
import com.pneck.employee.Activities.OrderProcess.SubmitRequestOrderAmount;
import com.pneck.employee.Activities.Registration.EmployeeSignInScreen;
import com.pneck.employee.Activities.Registration.EmployeeSignUpScreen;
import com.pneck.employee.Activities.Registration.ForgetPassword;
import com.pneck.employee.Activities.Registration.OtpVerificationScreen;
import com.pneck.employee.Activities.MainActivity;
import com.pneck.employee.Activities.SplashScreen;
import com.pneck.employee.Activities.WebPageScreen;

public class LaunchActivityClass {

    public static void LaunchMainActivity(Activity activity) {
        Intent intent=new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    public static void LaunchDriverDashboard(Activity activity) {
        Intent intent=new Intent(activity, DriverDashboard.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    public static void LaunchOTPActivity(Activity activity, Bundle bundle) {
        Intent intent=new Intent(activity, OtpVerificationScreen.class);
        intent.putExtras(bundle);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in, R.anim.nothing);
    }

    public static void LaunchLoginScreen(Activity activity) {
        Intent intent=new Intent(activity, EmployeeSignInScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in, R.anim.nothing);
    }

    public static void LaunchForgotPasswordScreen(Activity activity) {
        Intent intent=new Intent(activity, ForgetPassword.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in, R.anim.nothing);
    }

    public static void LaunchSignUpScreen(Activity activity) {
        Intent intent=new Intent(activity, EmployeeSignUpScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in, R.anim.nothing);
    }

    public static void LaunchSignInScreen(Activity activity) {
        Intent intent=new Intent(activity, EmployeeSignInScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in, R.anim.nothing);
    }

    public static void LaunchJOB_OTPScreen(Context activity,Bundle bundle) {
        Intent intent=new Intent(activity, Job_OTP_VerifyScreen.class);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    public static void LaunchJOB_AcceptScreen(Context activity,Bundle bundle) {
        Intent intent=new Intent(activity, JobAcceptScreen.class);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    public static void LaunchJOBDetailsScreen(Context activity) {
        Intent intent=new Intent(activity, JobDetailAddByEmployee.class);
        activity.startActivity(intent);
    }

    public static void LaunchTrackingScreen(Context activity) {
        Intent intent=new Intent(activity, JobRealTimeTrackingScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    public static void LaunchEmployeeRidesScreen(Context activity) {
        Intent intent=new Intent(activity, EmployeeRides.class);
        activity.startActivity(intent);
    }
    public static void LaunchEditProfileScreen(Context activity) {
        Intent intent=new Intent(activity, EditProfileScreen.class);
        activity.startActivity(intent);
    }

    public static void LaunchOrderBookingOTPActivity(Context activity) {
        Intent intent=new Intent(activity, DeliveryCompleteOTPVerification.class);
        activity.startActivity(intent);
    }

    public static void LaunchOrderCompleteHappyScreen(Activity activity) {
        Intent intent=new Intent(activity, OrderCompleteHappyScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in, R.anim.nothing);
    }
    public static void LaunchOrderSubmitRequestScreen(Activity activity) {
        Intent intent=new Intent(activity, SubmitRequestOrderAmount.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in, R.anim.nothing);
    }

    public static void LaunchPaymentPendingScreen(Activity activity,Bundle bundle) {
        Intent intent=new Intent(activity, PaymentStatusPendingStage.class);
        intent.putExtras(bundle);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in, R.anim.nothing);
    }

    public static void LaunchHelpScreen(Activity activity) {
        Intent intent=new Intent(activity, HelpScreen.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in, R.anim.nothing);
    }

    public static void LaunchAboutUsScreen(Activity activity) {
        Intent intent=new Intent(activity, AboutUsScreen.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in, R.anim.nothing);
    }

    public static void LaunchFeedBackScreen(Activity activity) {
        Intent intent=new Intent(activity, EmployeeFeedbackScreen.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in, R.anim.nothing);
    }

    public static void LaunchWebScreen(Context activity,Bundle bundle) {
        Intent intent=new Intent(activity, WebPageScreen.class);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    public static void LaunchChooseType(Activity activity) {
        Intent intent=new Intent(activity, ChooseType.class);
        intent.putExtras(intent);
        activity.startActivity(intent);
    }
}
