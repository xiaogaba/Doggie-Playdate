package com.example.xin.pre_project.Fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.xin.pre_project.Dog;
import com.example.xin.pre_project.DogRVAdapter;
import com.example.xin.pre_project.HomeActivity;
import com.example.xin.pre_project.ImageManager;
import com.example.xin.pre_project.R;
import com.example.xin.pre_project.SQLiteManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProfileFragment extends Fragment {

    TextView tvUsername, tvEmail;
    RecyclerView rvDogs;
    FloatingActionButton fab;
    AddDogFAB mCallback;
    de.hdodenhof.circleimageview.CircleImageView profilePic;

    String userName;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        bindViews(view);
        Bundle bd = getArguments();
        if(bd != null) {
            userName = bd.getString("username");
        }
        /*else {
            getUserName();
        }*/
        setUserInfo();
        getAndSetDogs();

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            userName = savedInstanceState.getString("username");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("username", userName);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ProfileFragment.AddDogFAB) {
            mCallback = (ProfileFragment.AddDogFAB) context;
        }
        else {
            throw new RuntimeException(context.toString()
                    + " must implement ProfileFragment AddDogFAB click listener");
        }
    }

    private void bindViews(View view) {
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        rvDogs = view.findViewById(R.id.rvMyDogs);
        fab = view.findViewById(R.id.fabAddDog);
        profilePic = view.findViewById(R.id.myProfilePic);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewDog();
            }
        });
    }

    private void getUserName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
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

    private void setUserInfo() {
        // pull from firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            String email = user.getEmail();

            tvUsername.setText(userName);
            tvEmail.setText(email);

            // get photo from firebase
            Uri photoUri = user.getPhotoUrl();
            Glide.with(getContext())
                    .load(photoUri)
                    .into(profilePic);
        }
        else {
            // check local storage
            Toast t = Toast.makeText(getContext(), "Error loading user profile from firebase", Toast.LENGTH_SHORT);
            t.setGravity(Gravity.TOP, 0,0);
            t.show();
            if(userName == null)
                getUserName();
            ImageManager im = new ImageManager(HomeActivity.gContext, userName);
            Bitmap bm = im.loadFromStorage("");
            if(bm != null)
                profilePic.setImageBitmap(bm);
            else {
                t = Toast.makeText(getContext(), "No local profile pic found - using default", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.TOP, 0,0);
                t.show();
                profilePic.setImageResource(R.drawable.default_profile_pic);
            }
        }
    }


    private void getAndSetDogs() {
        if(userName == null)
            getUserName();
        SQLiteManager db = new SQLiteManager(getContext(), userName);
        ArrayList<Dog> dogs = db.getAllDogs();
        DogRVAdapter dogAdapter = new DogRVAdapter(getContext(), dogs, userName);
        rvDogs.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvDogs.setAdapter(dogAdapter);
    }

    private void addNewDog() {
        mCallback.navToAddDog();
    }

    public interface AddDogFAB {
        void navToAddDog();
    }

}