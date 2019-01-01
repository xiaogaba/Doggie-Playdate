package com.example.xin.pre_project;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlaydatesAdapter extends ArrayAdapter<Playdate> {

    private Context mContext;
    private ArrayList<Playdate> data;
    private TextView usersList;

    private int id = 1848;
    private int picOffset = 0;

    public PlaydatesAdapter(Context context, ArrayList<Playdate> playdates) {
        super(context, 0, playdates);
        this.data = playdates;
        this.mContext = context;
    }

    //@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if(v == null) {
            LayoutInflater li;
            li = LayoutInflater.from(mContext);
            v = li.inflate(R.layout.playdates_list_item, null);
        }


        usersList = v.findViewById(R.id.tvUsers);
        String usersString = "";
        Playdate p = data.get(position);
        if(p != null) {
            usersString = "You, " + p.user2.name;
            usersList.setText(usersString);

            final TextView location = v.findViewById(R.id.playdateLocation);
            TextView dateTime = v.findViewById(R.id.playdateDate);

            if(Geocoder.isPresent()) {
                // convert location to address
                String[] address = locationFromLatLon(p.latitude, p.longitude);
                if(address != null) {
                    String addressString = "";
                    addressString += address[0];
                    //addressString += address[1] + ", " + address[2] + " " + address[3];
                    location.setText(addressString);
                }
                else
                    location.setText("Lat: " + data.get(position).latitude + "\n" + "Lon: " +
                            data.get(position).longitude);
            }
            else {
                location.setText("Lat: " + data.get(position).latitude + "\n" + "Lon: " +
                                                    data.get(position).longitude);
            }

            dateTime.setText(p.dToString());

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String address = location.getText().toString();
                    String x = "google.navigation:q=" + address + "&mode=b";
                            Uri gmmIntentUri = Uri.parse(x);  //
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(mContext.getPackageManager()) != null) {
                        mContext.startActivity(mapIntent);
                    }
                }
            });
        }

        return v;
    }

    private String[] locationFromLatLon(double latitude, double longitude) {
        List<Address> addresses = new ArrayList<>();
        Geocoder geocoder;
        String[] result = new String[5];


        geocoder = new Geocoder(mContext, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(addresses.size() > 0) {
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();

            result[0] = address;
            result[1] = city;
            result[2] = state;
            result[3] = postalCode;
            result[4] = knownName;

            return result;
        }
        else
            return null;
    }
}
