package com.example.xin.pre_project.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xin.pre_project.Dog;
import com.example.xin.pre_project.HomeActivity;
import com.example.xin.pre_project.ImageManager;
import com.example.xin.pre_project.R;
import com.example.xin.pre_project.SQLiteManager;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class AccountSettingsFragment extends Fragment implements Spinner.OnItemSelectedListener {

    RelativeLayout baseFrame;
    ImageView profilePic;
    Spinner userStatusSpinner, removeDogSpinner;
    Button changeProfilePic, removeDog, confirmRemoveDog, cancelRemoveDog;
    TextView tvNoDogs;

    ArrayAdapter<String> spinnerAdapter;

    ArrayList<String> dogs;
    int userStatus = 0;
    int indexOfDogToRemove = 0;
    String userName;

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int GALLERY_REQUEST = 1999;

    public AccountSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_settings, container, false);

        baseFrame = view.findViewById(R.id.rootLayout);
        profilePic = view.findViewById(R.id.settingsProfilePic);
        changeProfilePic = view.findViewById(R.id.buttonChangeProfilePic);
        userStatusSpinner = view.findViewById(R.id.userStatusSpinner);
        removeDog = view.findViewById(R.id.buttonRemoveDog);
        removeDogSpinner = view.findViewById(R.id.removeDogSpinner);
        confirmRemoveDog = view.findViewById(R.id.buttonConfirmRemoveDog);
        cancelRemoveDog = view.findViewById(R.id.buttonCancelRemoveDog);
        tvNoDogs = view.findViewById(R.id.tvNoDogs);

        Bundle bd = getArguments();
        if(bd != null) {
            userName = bd.getString("username");
        }
        else {
            getUserName();
        }

        ImageManager im = new ImageManager(HomeActivity.gContext, userName);
        Bitmap b = im.loadFromStorage("");
        if(b != null)
            profilePic.setImageBitmap(b);

        changeProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                alertDialog.setTitle("Set Your Profile Picture");
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
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                alertDialog.setTitle("Set Your Profile Picture");
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

        // setup User Status spinner
        userStatusSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.user_status_arr, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userStatusSpinner.setAdapter(adapter);



        removeDog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDog.setVisibility(View.GONE);
                // setup Remove Dog spinner
                updateDogData();
                setupRemoveDogSpinner();
            }
        });
        confirmRemoveDog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast t = Toast.makeText(getContext(), "Removing " + dogs.get(indexOfDogToRemove)
                        + " from your profile", Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER,0,0);
                t.show();

                String dogName = dogs.get(indexOfDogToRemove);
                SQLiteManager db = new SQLiteManager(getContext(), userName);
                db.removeDog(dogName);

                dogs.remove(indexOfDogToRemove);

                spinnerAdapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_item, dogs);

                removeDogSpinner.setVisibility(View.INVISIBLE);
                confirmRemoveDog.setVisibility(View.INVISIBLE);
                cancelRemoveDog.setVisibility(View.INVISIBLE);
                removeDog.setVisibility(View.VISIBLE);
            }
        });
        cancelRemoveDog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDogSpinner.setVisibility(View.INVISIBLE);
                confirmRemoveDog.setVisibility(View.INVISIBLE);
                cancelRemoveDog.setVisibility(View.INVISIBLE);
                tvNoDogs.setVisibility(View.INVISIBLE);
                removeDog.setVisibility(View.VISIBLE);
            }
        });

        return view;
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // user status spinner
        if(parent.getId() == R.id.userStatusSpinner) {
            userStatus = pos;
            /*
                TODO: Update user status in DB
                0 = Active      1 = Inactive
             */
        }
        // remove dog spinner
        else if(parent.getId() == R.id.removeDogSpinner) {
            indexOfDogToRemove = pos;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void updateDogData() {
        SQLiteManager db = new SQLiteManager(getContext(),userName);
        ArrayList<Dog>  myDogs = db.getAllDogs();
        dogs = new ArrayList<>();
        for(Dog d : myDogs) {
            dogs.add(d.name);
        }
    }

    public void setupRemoveDogSpinner() {
        if(dogs.size() > 0) {
            removeDogSpinner.setOnItemSelectedListener(this);
            spinnerAdapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_spinner_item, dogs);
            removeDogSpinner.setAdapter(spinnerAdapter);
            removeDogSpinner.setVisibility(View.VISIBLE);
            confirmRemoveDog.setVisibility(View.VISIBLE);
            cancelRemoveDog.setVisibility(View.VISIBLE);
        }
        else {
            removeDog.setVisibility(View.INVISIBLE);
            tvNoDogs.setVisibility(View.VISIBLE);
            cancelRemoveDog.setVisibility(View.VISIBLE);
        }
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
            profilePic.setImageBitmap(photo);
            ImageManager im = new ImageManager(HomeActivity.gContext, userName);
            im.saveToInternalStorage(photo);
            Log.d("savephoto", "result");
            savePhotoInFB(photo);
        }

        if(requestCode == GALLERY_REQUEST && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            try {
                photo = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                profilePic.setImageBitmap(photo);
                ImageManager im = new ImageManager(getContext(), userName);
                im.saveToInternalStorage(photo);
                Log.d("savephoto", "result");
                savePhotoInFB(photo);

            } catch (FileNotFoundException e) {
                Toast.makeText(getContext(), "Error saving file", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void savePhotoInFB(Bitmap photo) {
        final StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(userName + "/profile.jpg");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();


        UploadTask uploadTask = storageRef.putBytes(data);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return storageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    String photoURI = downloadUri.toString();

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(downloadUri)
                            .build();

                    user.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("savephoto", "User profile updated.");
                                    }
                                }
                            });



                } else {
                    // Handle failures
                    // ...
                }
            }
        });
    }

}
