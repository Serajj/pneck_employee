package com.pneck.employee.models;

public class UserOrderModel {

        String id,order_number,order_status,accept_otp_confirm_at,
                delivery_confirm_at,order_subtotal,booking_charge,
                grand_total,booking_complete_at;

    public UserOrderModel(String id, String order_number, String order_status, String accept_otp_confirm_at, String delivery_confirm_at, String order_subtotal,
                          String booking_charge, String grand_total, String booking_complete_at) {
        this.id = id;
        this.order_number = order_number;
        this.order_status = order_status;
        this.accept_otp_confirm_at = accept_otp_confirm_at;
        this.delivery_confirm_at = delivery_confirm_at;
        this.order_subtotal = order_subtotal;
        this.booking_charge = booking_charge;
        this.grand_total = grand_total;
        this.booking_complete_at = booking_complete_at;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrder_number() {
        return order_number;
    }

    public void setOrder_number(String order_number) {
        this.order_number = order_number;
    }

    public String getOrder_status() {
        return order_status;
    }

    public void setOrder_status(String order_status) {
        this.order_status = order_status;
    }

    public String getAccept_otp_confirm_at() {
        return accept_otp_confirm_at;
    }

    public void setAccept_otp_confirm_at(String accept_otp_confirm_at) {
        this.accept_otp_confirm_at = accept_otp_confirm_at;
    }

    public String getDelivery_confirm_at() {
        return delivery_confirm_at;
    }

    public void setDelivery_confirm_at(String delivery_confirm_at) {
        this.delivery_confirm_at = delivery_confirm_at;
    }

    public String getOrder_subtotal() {
        return order_subtotal;
    }

    public void setOrder_subtotal(String order_subtotal) {
        this.order_subtotal = order_subtotal;
    }

    public String getBooking_charge() {
        return booking_charge;
    }

    public void setBooking_charge(String booking_charge) {
        this.booking_charge = booking_charge;
    }

    public String getGrand_total() {
        return grand_total;
    }

    public void setGrand_total(String grand_total) {
        this.grand_total = grand_total;
    }

    public String getBooking_complete_at() {
        return booking_complete_at;
    }

    public void setBooking_complete_at(String booking_complete_at) {
        this.booking_complete_at = booking_complete_at;
    }
}
