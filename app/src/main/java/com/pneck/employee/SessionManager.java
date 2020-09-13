package com.pneck.employee;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {



    static SharedPreferences pref;
    static SharedPreferences.Editor editor;

    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "p$24n346e@n2ck342E324mpl4y33";

    private static final String IS_LOGIN = "IsLoggedIn";
    private static final String USER_FIRST_NAME = "userFName";
    private static final String USER_LAST_NAME = "userLName";
    private static final String KEY_USER_ID="user_id";
    private static final String KEY_USER_PHONE ="user_phone";
    private static final String KEY_TOKEN="key_token";
    private static final String USER_EMAIL = "userEmail";
    private static final String USER_TYPE = "userType";
    private static final String IS_SERVICE_STARTED="is_service_started";

    private static final String CURRENT_ORDER_BOOKING_ID="current_booking_id";
    private static final String CURRENT_ORDER_USER_NAME="current_user_order_name";
    private static final String CURRENT_ORDER_USER_PHONE="current_order_user_phone";
    private static final String CURRENT_ORDER_USER_ID="current_user_id";
    private static final String CURRENT_ORDER_ID="current_order_id";
    private static final String CURRENT_ORDER_INFO="current_order_info";


    private static final String CURRENT_EMPLOYEE_LATITUDE="current_emp_latitude";
    private static final String CURRENT_EMPLOYEE_LONGITUDE="current_emp_longitude";

    private static final String CURRENT_USER_PROFILE_PIC="current_user_profile_pic";
    private static final String USER_PASSWORD="user_password";

    public enum UserType{
        TYPE_DELIVERY,
        TYPE_DRIVER
    }

    public void saveOrderDetail(String orderId,String orderInfo){
        editor.putString(CURRENT_ORDER_ID,orderId);
        editor.putString(CURRENT_ORDER_INFO,orderInfo);
        editor.commit();
    }
    public String getCurrentOrderId(){
        return pref.getString(CURRENT_ORDER_ID,"");
    }
    public String getCurrentOrderInfo(){
        return pref.getString(CURRENT_ORDER_INFO,"");
    }
    public void setPhoneAndPass(String phone,String pass){
        editor.putString(KEY_USER_PHONE,phone);
        editor.putString(USER_PASSWORD,pass);
        editor.commit();
    }

    public String getUserPassword() {
        return pref.getString(USER_PASSWORD,"");
    }

    public void setEmployeeLocation(String latitude, String longitude){
        editor.putString(CURRENT_EMPLOYEE_LATITUDE,latitude);
        editor.putString(CURRENT_EMPLOYEE_LONGITUDE,longitude);
        editor.commit();
    }

    public String getEmployeeCurrentLatitude(){
        return pref.getString(CURRENT_EMPLOYEE_LATITUDE,"");
    }
    public String getEmployeeCurrentLongitude(){
        return pref.getString(CURRENT_EMPLOYEE_LONGITUDE,"");
    }

    public void saveCurrentOrderBookingId(String bookingId){
        editor.putString(CURRENT_ORDER_BOOKING_ID,bookingId);
        editor.commit();
    }
    public void createOrderSession(String currntbookingId,String userName,String phone,String userId) {
        editor.putString(CURRENT_ORDER_BOOKING_ID,currntbookingId);
        editor.putString(CURRENT_ORDER_USER_NAME,userName);
        editor.putString(CURRENT_ORDER_USER_PHONE,phone);
        editor.putString(CURRENT_ORDER_USER_ID,userId);
        editor.commit();
    }
    public boolean clearOrderSession(){
        editor.putString(CURRENT_ORDER_BOOKING_ID,"");
        editor.putString(CURRENT_ORDER_USER_NAME,"");
        editor.putString(CURRENT_ORDER_USER_PHONE,"");
        editor.putString(CURRENT_ORDER_USER_ID,"");
        editor.putString(CURRENT_ORDER_ID,"");
        editor.putString(CURRENT_ORDER_INFO,"");
        editor.commit();
        return true;
    }
    public String getCurrentOrderUserId(){
        return pref.getString(CURRENT_ORDER_USER_ID,"");
    }

    public String getCurrentBookingOrderId(){
        return pref.getString(CURRENT_ORDER_BOOKING_ID,"");
    }
    public String getCurrentOrderUserName(){
        return pref.getString(CURRENT_ORDER_USER_NAME,"");
    }
    public String getCurrentOrderUserPhone(){
        return pref.getString(CURRENT_ORDER_USER_PHONE,"");
    }

    public boolean setServiceStarted(boolean isServiceStarted){
        editor.putBoolean(IS_SERVICE_STARTED,isServiceStarted);
        editor.commit();
        return true;
    }
    public boolean isServiceStarted(){
        return pref.getBoolean(IS_SERVICE_STARTED,false);
    }



    public String getEmployeeToken(){
        return pref.getString(KEY_TOKEN,"");
    }

    public String getUserPhone(){
        return pref.getString(KEY_USER_PHONE,"");
    }

    public SessionManager(Context ctx){
        pref = ctx.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public boolean logout(){
        editor.clear();
        editor.commit();
        return true;
    }
    public void saveUserImage(String userImage){
        editor.putString(CURRENT_USER_PROFILE_PIC,userImage);
        editor.commit();
    }

    public String getUserImage(){
        return pref.getString(CURRENT_USER_PROFILE_PIC,"");
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }
    public String getUserFirstName(){
        return pref.getString(USER_FIRST_NAME, null);
    }
    public String getUserLastName(){
        return pref.getString(USER_LAST_NAME, null);
    }

    public UserType getUserType(){
        String savedType = pref.getString(USER_TYPE,"1");
        if (savedType.equals("1")) return UserType.TYPE_DELIVERY;
        else return UserType.TYPE_DRIVER;
    }

    public boolean createSession(String userId,String userToken,String first_name,String lastName,
                                 String mobileNo,String user_email,String image, String userType){
        editor.putBoolean(IS_LOGIN,true);
        editor.putString(KEY_USER_ID,userId);
        editor.putString(USER_FIRST_NAME,first_name);
        editor.putString(USER_LAST_NAME,lastName);
        editor.putString(KEY_USER_PHONE,mobileNo);
        editor.putString(KEY_TOKEN,userToken);
        editor.putString(USER_EMAIL,user_email);
        editor.putString(USER_TYPE,userType);
        editor.putString(CURRENT_USER_PROFILE_PIC,image);
        editor.commit();
        return true;
    }
    public boolean signOut(){
        editor.clear();
        editor.commit();
        return true;
    }


    public String getEmployeeId() {
        return pref.getString(KEY_USER_ID,"");
    }

    public String  getUserProfilePic() {
        return "empty";
    }

    public boolean checkLogin(Activity activity) {
        return pref.getBoolean(IS_LOGIN, false);
    }

}
