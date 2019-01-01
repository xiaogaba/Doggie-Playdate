package com.example.xin.pre_project;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xin.pre_project.Common.Common;
import com.example.xin.pre_project.Model.FCMResponse;
import com.example.xin.pre_project.Model.Notification;
import com.example.xin.pre_project.Model.Sender;
import com.example.xin.pre_project.Model.Token;
import com.example.xin.pre_project.R;
import com.example.xin.pre_project.Welcome;
import com.example.xin.pre_project.remote.IFCMService;
import com.example.xin.pre_project.remote.IFCMService_receiver;
import com.example.xin.pre_project.remote.IGoogleAPI;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerCall extends AppCompatActivity {

    TextView txtTime, txtAddress, txtDistance;
    Button btnCancel, btnAccept;
    MediaPlayer mediaPlayer;
    IGoogleAPI mService;
    IFCMService  mFCMService;
    String customerId;
    Double lat,lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_call);

        mService = Common.getGoogleAPI();
        mFCMService = Common.getFCMService();
        // InitView
        txtAddress = (TextView) findViewById(R.id.txtAddress);
        txtDistance = (TextView) findViewById(R.id.txtDistance);
        txtTime = (TextView) findViewById(R.id.txtTime);

        btnAccept = (Button)findViewById(R.id.btnAccept);
        btnCancel = (Button)findViewById(R.id.btnDecline);

        btnCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(customerId))
                    cancelBooking(customerId);
            }});
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptBooking(customerId);
                Intent intent = new Intent(CustomerCall.this,DriverTracking.class);
                //Send customer location to new activity
                intent.putExtra("lat",lat);
                intent.putExtra("lng",lng);
                intent.putExtra("customerId",customerId);

                startActivity(intent);
                finish();
                }
            });

        mediaPlayer = MediaPlayer.create(this, R.raw.ringstone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        if (getIntent() != null) {
            lat = getIntent().getDoubleExtra("lat", -1.0);
            lng = getIntent().getDoubleExtra("lng", -1.0);
            customerId = getIntent().getStringExtra("customer");
        }

        getDirection(lat, lng);
    }

    private void acceptBooking(String customerId)
    {
        Token token = new Token(customerId);

        Notification notification = new Notification("Accepted","User '" +
                FirebaseAuth.getInstance().getCurrentUser().getEmail() + "' has accepted your request");
        Sender sender = new Sender(token.getToken(),notification);
        mFCMService.sendMessage(sender)
                .enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        Toast.makeText(CustomerCall.this,"Accepted",Toast.LENGTH_SHORT).show();
                        finish();

                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {

                    }
                });
    }

    private void cancelBooking(String customerId) {
        Token token = new Token(customerId);

        Notification notification = new Notification("Cancel","User has cancelled your request");
        Sender sender = new Sender(token.getToken(),notification);
        mFCMService.sendMessage(sender)
                .enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        Toast.makeText(CustomerCall.this,"Cancelled",Toast.LENGTH_SHORT).show();
                        finish();

                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {

                    }
                });
    }

    private void getDirection(double lat, double lng) {

        String requestApi = null;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + Common.mLastLocation.getLatitude() + "," + Common.mLastLocation.getLongitude() + "&" +
                    "destination=" + lat + "," + lng + "&" +
                    "key=" + getResources().getString(R.string.google_direction_api);
            Log.d("EDMTDEV", requestApi); // print URL for debug
            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {

                            try { //part 9 46:07
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray routes = jsonObject.getJSONArray("routes");
                                //after get routes, just get first element of routes
                                JSONObject object = routes.getJSONObject(0);

                                //after get first element, we need get array with name "legs"
                                JSONArray legs = object.getJSONArray("legs");

                                //and get first element of legs array
                                JSONObject legsObject = legs.getJSONObject(0);

                                //Now, get Distance
                                JSONObject distance = legsObject.getJSONObject("distance");
                                txtDistance.setText(distance.getString("text"));

                                //getTime
                                JSONObject time = legsObject.getJSONObject("duration");
                                txtTime.setText(time.getString("text"));

                                //getAddress
                                String address = legsObject.getString("end_address");
                                txtAddress.setText(address);



                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(CustomerCall.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }catch(Exception e){
            e.getStackTrace();
        }

    }
    @Override
    protected void onStop(){
        mediaPlayer.release();
        super.onStop();
    }
    @Override
    protected void onPause(){
        mediaPlayer.release();
        super.onPause();
    }
    @Override
    protected void onResume(){
        super.onResume();
        mediaPlayer.start();
    }

}
