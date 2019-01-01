package com.example.xin.pre_project.Fragments;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.xin.pre_project.PDAttendee;
import com.example.xin.pre_project.Playdate;
import com.example.xin.pre_project.PlaydatesAdapter;
import com.example.xin.pre_project.R;
import com.example.xin.pre_project.SQLiteManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MyPlaydatesFragment extends Fragment {

    ListView lv;
    FloatingActionButton fabAddPlaydate;

    ArrayAdapter<Playdate> adapter;
    String myUID, userName;

    PlaydateListener mCallback;

    public MyPlaydatesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
        getUserName();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_myplaydates, container, false);
        Bundle bd = getArguments();
        if(bd != null) {
            userName = bd.getString("username");
        }
        bindViews(view);

        //getUserName();
        setAndAttachLVAdapter();

        return view;
    }

    private void bindViews(View view) {
        lv = view.findViewById(R.id.lvPlaydates);
        fabAddPlaydate = view.findViewById(R.id.fabNewPlaydate);
        fabAddPlaydate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.playdatesFabGoToCreatePlaydateFragment();
            }
        });
    }

    private void setAndAttachLVAdapter() {
        ArrayList<Playdate> myPlaydates = new ArrayList<>();
        ArrayList<String> attendees = new ArrayList<>();

        SQLiteManager dbManager = new SQLiteManager(getContext(), userName);
        // get Playdates for days >= today
        Date todaysDate = Calendar.getInstance().getTime();
        SimpleDateFormat myFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        String todaysString = myFormat1.format(todaysDate);

        /*
        //Cursor cursor = dbManager.getPlaydatesAfter(todaysString);
        Cursor cursor = dbManager.getAllPlaydates();
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                do {
                    Log.d("mypd", "cursor not null");
                    String user2uid = cursor.getString(cursor.getColumnIndexOrThrow("user2UID"));
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    Date date = new Date();
                    try {
                        date = df.parse(cursor.getString(cursor.getColumnIndexOrThrow("date")));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Log.d("mypd", "cursor not null2");
                    float latitude = cursor.getFloat(cursor.getColumnIndexOrThrow("latitude"));
                    float longitude = cursor.getFloat(cursor.getColumnIndexOrThrow("longitude"));
                    myPlaydates.add(new Playdate(new PDAttendee(myUID), new PDAttendee(user2uid), date, latitude, longitude));
                    Log.d("mypd", "cursor not null3");
                } while (cursor.moveToNext());
            }

        }
        else {
            /*
            FOR DATE:
                month is 0 indexed  - 0 = January
                year is 1900 as base - For 2016, input 116 for year

            attendees.add("abc");
            attendees.add("def");
            Date date = new Date(118, 1, 1, 15, 30);
            Location meetingPlace = new Location("");
            meetingPlace.setLatitude(37.3382);
            meetingPlace.setLongitude(-121.8863);
            //myPlaydates.add(new Playdate(attendees, date, meetingPlace));
            date = new Date(118, 11, 30, 8, 15);
            //myPlaydates.add(new Playdate(attendees, date, meetingPlace));

            Log.d("mypd", "cursor IS null");
        }
        */
        myPlaydates = dbManager.getAllPlaydates();


        adapter = new PlaydatesAdapter(getActivity(), myPlaydates);
        lv.setAdapter(adapter);
    }

    // Ensures that Activity has implemented FiltersFragmentListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PlaydateListener) {
            mCallback = (PlaydateListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FiltersFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    private void getUserName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        myUID = user.getUid();
        if(user != null) {
            final String uid = user.getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("UsersInformation");
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {

                        if (ds.getKey().equals(uid)) {
                            String name = ds.child("name").getValue(String.class);
                            setUserName(name);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
    private void setUserName(String name) {
        userName = name;
    }

    public interface PlaydateListener {
        void playdatesFabGoToCreatePlaydateFragment();
    }

}