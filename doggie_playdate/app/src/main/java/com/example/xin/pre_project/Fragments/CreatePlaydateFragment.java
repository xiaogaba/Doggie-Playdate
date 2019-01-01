package com.example.xin.pre_project.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.util.GregorianCalendar;
import android.location.Location;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.xin.pre_project.PDAttendee;
import com.example.xin.pre_project.Playdate;
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

public class CreatePlaydateFragment extends Fragment {

    Button buttonInvite, buttonCreatePlaydate;
    EditText cpInviteList, chooseDate, chooseTime;
    Spinner filterSpinner;

    ArrayAdapter<CharSequence> filterSpinnerAdapter;
    ArrayList<String> userEmailList, userNameList, userUIDList;
    String playdatePartner;
    ArrayList<Float> userDistanceList;
    int indexFilterSelection = 0,      // 0 = 1 mile, 1 = 2 mile, 2 = 5 mile
            chosenUserIndex = -1;

    DatePickerDialog datePicker;
    int dmonth= -1, dday = -1, dyear = -1;
    TimePickerDialog timePicker;
    int thour = -1, tminute = -1;

    String userName, myUID, friendUID;

    EditText inputLat, inputLon;

    Location meetingLocation;    // String meetingAddress ?

    AfterCreatePlaydate mCallback;

    public CreatePlaydateFragment() {
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
        View view = inflater.inflate(R.layout.fragment_create_playdate, container, false);
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

    private void bindViews(View view) {
        buttonInvite = view.findViewById(R.id.cpInvite);
        buttonCreatePlaydate = view.findViewById(R.id.cpButtonCreatePlaydate);
        cpInviteList = view.findViewById(R.id.cpInviteList);
        filterSpinner = view.findViewById(R.id.cpFilterSpinner);
        chooseDate = view.findViewById(R.id.cpETDate);
        chooseTime = view.findViewById(R.id.cpETTime);

        inputLat = view.findViewById(R.id.inputLatitude);
        inputLon = view.findViewById(R.id.inputLongitude);

        userEmailList = new ArrayList<>();
        userNameList = new ArrayList<>();
        userUIDList = new ArrayList<>();

        myUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        /*
            TODO: populate user data
            get List<String> uids
            userUIDList = uids;
         */
        //fillUserLists(uids);

        userNameList.add("Bob");
        userNameList.add("Sally");
        userNameList.add("Frank");
        userNameList.add("Betty");
        userEmailList.add("a@a.com");
        userEmailList.add("b@b.com");
        userEmailList.add("c@c.com");
        userEmailList.add("d@d.com");

        userDistanceList = new ArrayList<>();

        // set up buttonInvite
        buttonInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUsers(indexFilterSelection);
            }
        });

        // set up buttonCreatePlaydate
        buttonCreatePlaydate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPlaydate();
            }
        });

        // set up Filter-by distance spinner
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                indexFilterSelection = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        filterSpinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.filter_by_arr,
                                                              android.R.layout.simple_spinner_item);
        filterSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(filterSpinnerAdapter);


        // set up date picker
        chooseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                final int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);
                datePicker = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                chooseDate.setText((monthOfYear + 1) + "/" + dayOfMonth  + "/" + year);
                                extractDate(year, monthOfYear, dayOfMonth);
                            }
                        }, year, month, day);
                datePicker.setTitle("Choose a Date");
                datePicker.show();
            }
        });
        // set up time picker
        chooseTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar currentTime = Calendar.getInstance();
                int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                int minute = currentTime.get(Calendar.MINUTE);
                timePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        chooseTime.setText(hourOfDay + ":" + minute);
                        extractTime(hourOfDay, minute);
                    }
                }, hour, minute, true);
                timePicker.setTitle("Pick a Time");
                timePicker.show();
            }
        });
    }

    private void fillUserLists(ArrayList<String> uids) {
        // given ArrayList<String> of UIDs, fill out name and email
        for(final String uid : uids) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user != null) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("UsersInformation");
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {

                            if (ds.getKey().equals(uid)) {
                                userNameList.add(ds.child("name").getValue(String.class));
                                userEmailList.add(ds.child("email").getValue(String.class));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    // DatePicker accessory method
    public void extractDate(int year, int month, int day) {
        dyear = year;
        dmonth = month + 1;
        dday = day;
    }

    // TimePicker accessory method
    public void extractTime(int hour, int minute) {
        thour = hour;
        tminute = minute;
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

    public void showUsers(int indexFilterSelection) {
        // Show dialog box w/ list of users, FILTERS APPLIED

        /*
            TODO: DB call to get list of users, apply filter
            indexFilterSelection     0 = 1 mile, 1 = 2 miles, 2 = 5 miles
            save user emails in userEmailList (string vals) - EMAIL FOR BACKEND
            save user names in userNameList (string vals) - display in UI
            save user distances in userDistanceList (float vals) - display in UI

         */


        AlertDialog.Builder chooseUserDialog = new AlertDialog.Builder(getContext());
        chooseUserDialog.setTitle("Invite: ");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.select_dialog_singlechoice, userNameList);

        chooseUserDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                dialog.dismiss();
            }
        });

        chooseUserDialog.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                addUserToPlaydate(index);
                hideInviteButton();
            }
        });
        chooseUserDialog.show();
    }

    private void addUserToPlaydate(int index) {
        String userEmail = userEmailList.get(index);
        String userName = userNameList.get(index);
        //String userUID = userUIDList.get(index);
        chosenUserIndex = index;

        //cpInviteList.setText(cpInviteList.getText().toString() + userName + " ");
        cpInviteList.setText("tnANrHrfHtQmQi4mYkBG5loAt113");
    }

    private void hideInviteButton() {
        buttonInvite.setVisibility(View.INVISIBLE);
    }

    private void createPlaydate() {
        // create meetingLocation from LOCATION SEARCH BAR
        meetingLocation = new Location("");
        meetingLocation.setLatitude(37.3503);
        meetingLocation.setLongitude(-121.9607);

        float latitude = 37.3503f;
        float longitude = -121.9607f;

        // check for empty fields
        if(dyear == -1 || thour == -1 || cpInviteList.getText().toString().isEmpty()  || meetingLocation == null
                || inputLat.getText().toString().isEmpty() || inputLon.getText().toString().isEmpty()) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("Error");
            alert.setMessage("You must fill out all fields to continue");
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.show();
        }
        else {
            String m, d, hr, min;
            Date date = new Date();
            m = (dmonth < 10) ? "0"+ (Integer.toString(dmonth)) : Integer.toString(dmonth);
            d = (dday < 10) ? "0"+ Integer.toString(dday) : Integer.toString(dday);
            hr = (thour < 10) ? "0"+ Integer.toString(thour) : Integer.toString(thour);
            min = (tminute < 10) ? "0"+ Integer.toString(tminute) : Integer.toString(tminute);
            String dt = dyear + "-" + m + "-" + d + " " + hr + ":" + min;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            try {
                date = format.parse(dt);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            latitude = Float.parseFloat(inputLat.getText().toString());
            longitude = Float.parseFloat(inputLon.getText().toString());

            //Playdate pd = new Playdate(new PDAttendee(myUID), new PDAttendee(userUIDList.get(chosenUserIndex)), date, latitude, longitude);
            Playdate pd = new Playdate(new PDAttendee(myUID), new PDAttendee("tnANrHrfHtQmQi4mYkBG5loAt113"), date, latitude, longitude);

            /*
                TODO: save playdate to firebase

             */
            addPlaydateToDeviceCalendar(pd);
            SQLiteManager dbManager = new SQLiteManager(getContext(), userName);
            int x = dbManager.addPlaydate(pd, getContext());
            Log.d("mypd", "create pd: " + Integer.toString(x));
            navToMyPlaydates();
        }
    }

    private void addPlaydateToDeviceCalendar(Playdate pd) {
        Intent calIntent = new Intent(Intent.ACTION_INSERT);
        calIntent.setType("vnd.android.cursor.item/event");
        calIntent.putExtra(CalendarContract.Events.TITLE, "Doggie Playdate w/ " + pd.user2.name);
        calIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, "SOME LOCATION");
        calIntent.putExtra(CalendarContract.Events.DESCRIPTION, "Doggie Playdate");
/*
        GregorianCalendar calDate = new GregorianCalendar(dyear, dmonth, dday, thour, tminute);

        calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                calDate.getTimeInMillis());
        calIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                calDate.getTimeInMillis()+(30*60000));
*/
        Calendar cal = Calendar.getInstance();
        cal.set(dyear, dmonth, dday, thour, tminute);
        calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                cal.getTimeInMillis());
        calIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                cal.getTimeInMillis()+(30*60000));

        startActivity(calIntent);
    }
  
    private void savePlaydateToSQLiteDB(Playdate pd) {
        SQLiteManager dbManager = new SQLiteManager(getContext(), userName);
        dbManager.addPlaydate(pd, getContext());
    }

    private void navToMyPlaydates() {
        mCallback.goToMyPlaydates();
    }

    // Ensures that Activity has implemented FiltersFragmentListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CreatePlaydateFragment.AfterCreatePlaydate) {
            mCallback = (CreatePlaydateFragment.AfterCreatePlaydate) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AfterCreatePlaydate");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    public interface AfterCreatePlaydate {
        void goToMyPlaydates();
    }

}