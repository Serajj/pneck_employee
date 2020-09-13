package com.pneck.employee.ViewHolders;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pneck.employee.LaunchActivityClass;
import com.pneck.employee.R;
import com.pneck.employee.models.UserOrderModel;

public class UserOrderViewHolder extends RecyclerView.ViewHolder {

    private TextView orderId;
    private TextView orderStatus;
    private TextView orderAcceptedOn;
    private TextView orderDeliveredOn;
    private TextView orderSubTotal;
    private TextView orderBookingCharge;
    private TextView orderGrandTotal;
    private TextView bookingCompletedAt;

    public UserOrderViewHolder(View itemView) {
        super(itemView);
        orderId = (TextView)itemView.findViewById( R.id.order_id );
        orderStatus = (TextView)itemView.findViewById( R.id.order_status );
        orderAcceptedOn = (TextView)itemView.findViewById( R.id.order_accepted_on );
        orderDeliveredOn = (TextView)itemView.findViewById( R.id.order_delivered_on );
        orderSubTotal = (TextView)itemView.findViewById( R.id.order_sub_total );
        orderBookingCharge = (TextView)itemView.findViewById( R.id.order_booking_charge );
        orderGrandTotal = (TextView)itemView.findViewById( R.id.order_grand_total );
        bookingCompletedAt = (TextView)itemView.findViewById( R.id.booking_completed_at );
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setUpCardView(final UserOrderViewHolder holder, final Activity context,
                              final UserOrderModel data, final int position){


        orderId.setText(data.getOrder_number());

        String currentBookingStatus =data.getOrder_status();
        if (currentBookingStatus.equals("awaited")){
            currentBookingStatus="Order Not Yet Accepted";
        }else if (currentBookingStatus.equals("accepted")){
            currentBookingStatus="Order Accepted";
        }else if (currentBookingStatus.equals("accepted_otp_confirmed")){
            currentBookingStatus="OTP Confirmed";
        }else if (currentBookingStatus.equals("order_info_provided")){
            currentBookingStatus="Order Information Added";
        }else if (currentBookingStatus.equals("delivery_otp_confirmed")){
            currentBookingStatus="Delivery OTP Confirmed";
        }else if (currentBookingStatus.equals("order_request_payment")){
            currentBookingStatus="Order Payment Request";
        }else if (currentBookingStatus.equals("order_completed")){
            currentBookingStatus="Order Completed";
        }

        orderStatus.setText(currentBookingStatus);


        orderAcceptedOn.setText("Order Accepted On : "+data.getAccept_otp_confirm_at());
        orderDeliveredOn.setText("Order Delivered On : "+data.getDelivery_confirm_at());
        orderSubTotal.setText("Order Subtotal : "+data.getOrder_subtotal());
        orderBookingCharge.setText("Order Booking Charge : "+data.getBooking_charge());
        orderGrandTotal.setText("Order Grand Total : "+data.getGrand_total());
        bookingCompletedAt.setText("Order Completed On : "+data.getBooking_complete_at());
    }

}