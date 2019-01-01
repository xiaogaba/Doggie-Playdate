package com.example.xin.pre_project;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PDAttendee {
    public String uid;
    public String name;
    public String email;

    public PDAttendee(String uid) {
        this.uid = uid;
        //getName(uid);
        //getEmail(uid);
    }

    public static PDAttendee newAttendee(String uid) {
        PDAttendee temp = new PDAttendee(uid);
        temp.getName(uid);
        temp.getEmail(uid);
        return temp;
    }

    private void getName(final String uid) {
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

    private void setUserName(String name) {
        this.name = name;
    }

    private void getEmail(String uid) {
        this.email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    }
}
