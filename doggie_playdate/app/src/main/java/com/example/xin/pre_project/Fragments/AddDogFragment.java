package com.example.xin.pre_project.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.xin.pre_project.Dog;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;

public class AddDogFragment extends android.support.v4.app.Fragment {

    ReturnToProfile mCallback;

    EditText addDogEditName, addDogBreed;

    // Birthday Date Picker
    EditText chooseBirthday;
    DatePickerDialog picker;

    // RadioButtons
    RadioGroup radioGender, radioSize;
    int genderSelectedRadioId = -1; // 0 = male  1 = female
    int sizeSelectedRadioId = -1;   // 0 = small 1 = medium 2 = large

    // Dog Pic
    ImageView dogPic;
    String picPath, dogName;
  
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int GALLERY_REQUEST = 1999;

    String name, breed;
    int gender, year, month, day;
    String userName;

    Button addDog;

    public AddDogFragment() {
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
        View view = inflater.inflate(R.layout.fragment_add_dog, container, false);
        bindViews(view);
        Bundle bd = getArguments();
        if(bd != null) {
            userName = bd.getString("username");
        }
        else {
            getUserName();
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AddDogFragment.ReturnToProfile) {
            mCallback = (AddDogFragment.ReturnToProfile) context;
        }
        else {
            throw new RuntimeException(context.toString()
                    + " must implement MadeMealListener and/or ScheduleMealListener");
        }
    }

    private void bindViews(View view) {
        addDogEditName = view.findViewById(R.id.addDogEditName);
        addDogBreed = view.findViewById(R.id.addDogEditBreed);

        getUserName();

        /*
            BIRTHDAY DATE PICKER
         */
        chooseBirthday = view.findViewById(R.id.addDogEditBirthday);

        chooseBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);
                picker = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                chooseBirthday.setText((monthOfYear + 1) + "/" + dayOfMonth  + "/" + year);
                                extractDate();
                            }
                        }, year, month, day);
                picker.show();
            }
        });


        /*
            Radio buttons
         */
        radioGender = view.findViewById(R.id.addDogRadioGroupGender);
        radioSize = view.findViewById(R.id.addDogRadioGroupSize);

        radioGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int radioButtonID = radioGender.getCheckedRadioButtonId();
                View radioButton = radioGender.findViewById(radioButtonID);
                genderSelectedRadioId = radioGender.indexOfChild(radioButton);
                switch(genderSelectedRadioId) {
                    case 1: genderSelectedRadioId = 0; break;
                    case 3: genderSelectedRadioId = 1; break;
                }
            }
        });
        radioSize.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int radioButtonID = radioSize.getCheckedRadioButtonId();
                View radioButton = radioSize.findViewById(radioButtonID);
                sizeSelectedRadioId = radioSize.indexOfChild(radioButton);
                switch(sizeSelectedRadioId) {
                    case 2: sizeSelectedRadioId = 1; break;
                    case 4: sizeSelectedRadioId = 2; break;
                    default: sizeSelectedRadioId = 0; break;
                }
            }
        });

        /*
            Dog Pic Choosing
         */
        dogPic = view.findViewById(R.id.dogPic);

        dogPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                alertDialog.setTitle("Add a picture");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Take a Picture",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(cameraIntent, CAMERA_REQUEST);
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Choose from Gallery",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI), GALLERY_REQUEST);
                            }
                        });
                alertDialog.show();
            }
        });

        addDog = view.findViewById(R.id.addDogButton);
        addDog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                name = addDogEditName.getText().toString();
                breed = addDogBreed.getText().toString();
                String bday = chooseBirthday.getText().toString();
                if(name.isEmpty() || breed.isEmpty() || bday.isEmpty()) {
                    Toast t2 = Toast.makeText(getContext(), "You must fill out all fields", Toast.LENGTH_SHORT);
                    t2.setGravity(Gravity.CENTER, 0,0);
                    t2.show();
                }
                else {
                    // save dog photo to local storage
                    ImageManager im = new ImageManager(HomeActivity.gContext, userName);
                    Bitmap dogBitmap = ((BitmapDrawable)dogPic.getDrawable()).getBitmap();
                    picPath = im.saveToInternalStorage(dogBitmap, name);

                    Dog newDog = new Dog(name, breed, genderSelectedRadioId, sizeSelectedRadioId, year, month, day, picPath);
                    SQLiteManager db = new SQLiteManager(getContext(), userName);
                    int result = (int)db.addDog(newDog);

                    switch(result) {
                        case -1: Toast t = Toast.makeText(getContext(), "Error adding " + name + " to Dogs List", Toast.LENGTH_SHORT);
                            t.setGravity(Gravity.CENTER, 0,0);
                            t.show();
                            break;
                        default: Toast t1 = Toast.makeText(getContext(), "Added " + name + " to Dogs List", Toast.LENGTH_SHORT);
                            t1.setGravity(Gravity.CENTER, 0,0);
                            t1.show();
                            break;
                    }
                    mCallback.navToProfile();
                }

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

    // Birthday DatePicker accessory method
    public void extractDate() {
        String[] date = chooseBirthday.getText().toString().split("/");
        month = Integer.parseInt(date[0]);
        day = Integer.parseInt(date[1]);
        year = Integer.parseInt(date[2]);
        Toast.makeText(getContext(), "Birthday: " + month + "/" + day + "/" + year, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new
                        Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(getContext(), "camera permission denied", Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap photo;
        if(requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            photo = (Bitmap) data.getExtras().get("data");
            dogPic.setImageBitmap(photo);


        }

        if(requestCode == GALLERY_REQUEST && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            try {
                photo = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                dogPic.setImageBitmap(photo);

              
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public interface ReturnToProfile {
        void navToProfile();
    }
}