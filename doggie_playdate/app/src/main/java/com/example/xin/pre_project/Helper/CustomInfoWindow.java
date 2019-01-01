package com.example.xin.pre_project.Helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.xin.pre_project.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindow implements GoogleMap.InfoWindowAdapter {

    View myView;

    public CustomInfoWindow(Context context){
        myView = LayoutInflater.from(context).inflate(R.layout.custom_user_info_window, null);
    }


    @Override
    public View getInfoWindow(Marker marker) {
        TextView txtUserInfoTitle = (TextView)myView.findViewById(R.id.txtUserInfo);
        txtUserInfoTitle.setText(marker.getTitle());

        TextView txtUserInfoSnippet = (TextView) myView.findViewById(R.id.txtUserSnippet);
        txtUserInfoSnippet.setText(marker.getSnippet());

        return myView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
