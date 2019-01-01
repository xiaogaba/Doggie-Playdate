package com.example.xin.pre_project;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xin.pre_project.Common.Common;
import com.example.xin.pre_project.Fragments.ProfileFragment;
import com.example.xin.pre_project.Model.FCMResponse;
import com.example.xin.pre_project.Model.Notification;
import com.example.xin.pre_project.Model.Sender;
import com.example.xin.pre_project.Model.Token;
import com.example.xin.pre_project.Model.User;
import com.example.xin.pre_project.remote.IFCMService;
import com.example.xin.pre_project.remote.IGoogleAPI;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.ui.database.SnapshotParser;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.xin.pre_project.Common.Common.mLastLocation;

public class Welcome extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        com.google.android.gms.location.LocationListener  {

    private Map<Marker, String> user_marker_to_email_address = new HashMap<Marker, String>();
    private Map<Marker, User> user_marker_to_name = new HashMap<Marker, User>();
    private String selected_user_email;

    //message part variables init
    Button btnMessage, btnCreatePlaydate;
    final private String testUid = "MyvsPO4Zj7YvQxQaL4jqyBjnX7I2";



    private GoogleMap mMap;
    //Play Services
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    //private Location mLastLocation;

    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;


    // Bottom sheet
    ImageView imgExpandable;
    BottomSheetUserFragment mBottomSheet;
    //Button btnFindUsers;  is equal to Button btnPickupRequest

    // car animation
    private List<LatLng> polyLineList;
    private Marker carMarker;
    private float v;
    private double lat, lng;
    private Handler handler;
    private LatLng startPosition, endPosition, currentPosition;
    private int index, next;
    private PlaceAutocompleteFragment places;
    private String destination;
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private Polyline blackPolyline, greyPolyline;

    private IGoogleAPI mService = Common.getGoogleAPI();

    Runnable drawPathRunnable = new Runnable() {
        @Override
        public void run() {
            if(index < polyLineList.size() - 1){
                index++;
                next = index + 1;
            }
            if(index < polyLineList.size() - 1){
                startPosition = polyLineList.get(index);
                endPosition = polyLineList.get(next);
            }
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    v = valueAnimator.getAnimatedFraction();
                    lng = v * endPosition.longitude + (1 - v) * startPosition.longitude;
                    lat = v * endPosition.latitude + (1 - v) * startPosition.latitude;
                    LatLng newPos = new LatLng(lat, lng);
                    carMarker.setPosition(newPos);
                    carMarker.setAnchor(0.5f, 0.5f);
                    carMarker.setRotation(getBearing(startPosition, newPos));
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(newPos)
                                    .zoom(15.5f)
                                    .build()
                    ));
                }
            });
            valueAnimator.start();
            handler.postDelayed(this, 3000);
        }
    };

    private float getBearing(LatLng startPosition, LatLng newPos) {
        double lat = Math.abs(startPosition.latitude - endPosition.latitude);
        double lng = Math.abs(startPosition.longitude - endPosition.longitude);

        if(startPosition.latitude < endPosition.latitude && startPosition.longitude < endPosition.longitude){
            return (float) (Math.toDegrees(Math.atan(lng/lat)));
        } else if(startPosition.latitude >= endPosition.latitude && startPosition.longitude < endPosition.longitude){
            return (float) (90 - Math.toDegrees(Math.atan(lng/lat)) + 90);
        } else if(startPosition.latitude >= endPosition.latitude && startPosition.longitude >= endPosition.longitude){
            return (float) (Math.toDegrees(Math.atan(lng/lat)) + 180);
        } else if(startPosition.latitude < endPosition.latitude && startPosition.longitude >= endPosition.longitude){
            return (float) (90 - Math.toDegrees(Math.atan(lng/lat)) + 270);
        }
        return -1;
    }


    DatabaseReference drivers;
    GeoFire geoFire;
    Marker mCurrent;
    ArrayList<Marker> mOtherUserMarkers = new ArrayList<Marker>();
    MaterialAnimatedSwitch location_switch;
    SupportMapFragment mapFragment;
    boolean location_switch_state = false;


    // Find nearby drivers
    boolean isUserFound = false;
    String userID = "";
    int radius = 1;     // 1km
    int distance = 1;   // 1km
    private static final int LIMIT = 3;
    Button btnRequestDate;     // equal to Button btnPickupRequest
    Marker mUserMarker;

    // Navigation Drawer
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private TextView hamburgerMessageMarker;
    private ImageView navBarMessageMarker;

    public static int navItemIndex = 0;

    private static final String TAG_HOME = "home";
    private static final String TAG_MESSAGING = "messaging";
    private static final String TAG_CREATEPLAYDATE = "createplaydate";
    private static final String TAG_PROFILE = "profile";
    private static final String TAG_MYPLAYDATES = "myplaydates";
    private static final String TAG_ACCOUNTSETTINGS = "accountsettings";
    private static final String TAG_ADDDOG = "adddog";
    private static final String TAG_SCHEDULEPLAYDATE = "scheduleplaydate";
    public static String CURRENT_TAG = TAG_HOME;
    private Fragment fragment;

    private String userName;


    //Send Alert
    IFCMService mFCMService; //33:45
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_nav_drawer);

        getUserName();

        mFCMService = Common.getFCMService();
        // restore location_switch position
        if(savedInstanceState != null) {
            location_switch_state = savedInstanceState.getBoolean("toggle");
        }

        // Set up message onClick listener.
        btnMessage = (Button) findViewById(R.id.btnMessage);
        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selected_user_email == null) return;
                Intent intent = new Intent(Welcome.this, message.class);
                intent.putExtra("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid());
                intent.putExtra("target_id", selected_user_email);
                startActivity(intent);
            }
        });

        btnCreatePlaydate = findViewById(R.id.btnCreatePlaydate);
        btnCreatePlaydate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigationView.getMenu().getItem(navItemIndex).setChecked(false);
                navItemIndex = 2;
                selectNavMenu(navItemIndex);
                Intent go1 = new Intent(Welcome.this, HomeActivity.class);
                go1.putExtra("navItemIndex", 2);
                go1.putExtra("username", userName);
                startActivityForResult(go1, 0);
            }
        });


        toolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        navBarMessageMarker = (ImageView) navigationView.getMenu().findItem(R.id.navI_messaging).getActionView();
        hamburgerMessageMarker = (TextView) findViewById(R.id.hamburger_marker);

        /*
            TODO: check for unread messages
         */
        setUnreadMessages(false);

        setUpNavigationView();


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if(mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
        }
        mapFragment.getMapAsync(this);

        getSupportFragmentManager().beginTransaction().add(R.id.mapframe, mapFragment).commit();

        //init view
        location_switch = (MaterialAnimatedSwitch)findViewById(R.id.location_switch);
        if(location_switch_state) {
            location_switch.toggle();
        }
        location_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isOnline) {
                if(isOnline){
                    startLocationUpdates();
                    displayLocation();
                    location_switch_state = true;
                    Log.d("loc_switch", String.valueOf(location_switch_state));
                    Snackbar.make(mapFragment.getView(),"You are online", Snackbar.LENGTH_SHORT).show();
                }
                else{
                    stopLocationUpdates();
                    mCurrent.remove();
                    removeOtherUserMarkers();
                    mMap.clear();
                    location_switch_state = false;
                    Log.d("loc_switch", String.valueOf(location_switch_state));
                    //handler.removeCallbacks(drawPathRunnable);

                    Snackbar.make(mapFragment.getView(),"You are offline", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        // Place API
        places = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if(location_switch.isChecked()){
                    destination = place.getAddress().toString();
                    destination = destination.replace(" ", "+");
                    getDirection();
                } else{
                    Toast.makeText(Welcome.this, "Please change your status to ONLINE", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(Welcome.this, ""+status.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        polyLineList = new ArrayList<>();

        //Places API
        places = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if(location_switch.isChecked()){
                    destination = place.getAddress().toString();
                    destination = destination.replace("","+");
                    getDirection();
                }
                else{
                    Toast.makeText(Welcome.this,"Please change your status to ONLINE",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(Welcome.this,""+status.toString(),Toast.LENGTH_SHORT).show();
            }
        });

        //Geo Fire
        // drivers mean users here
        drivers = FirebaseDatabase.getInstance().getReference(Common.user_location_tb1);
        geoFire = new GeoFire(drivers);
        setUpLocation();

       // mService = Common.getGoogleAPI();

        updateFirebaseToken();

        // Init view of bottom sheet
        imgExpandable = (ImageView)findViewById(R.id.imgExpandable);
        mBottomSheet = BottomSheetUserFragment.newInstance("User bottom sheet");
        imgExpandable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheet.show(getSupportFragmentManager(),mBottomSheet.getTag());
            }
        });

        // TODO
        // Find the closest user and message this guy
        btnRequestDate = (Button) findViewById(R.id.btn_request_date);
        btnRequestDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //29:14
                if(!isUserFound)
                    requestDateHere(FirebaseAuth.getInstance().getCurrentUser().getUid());
                else
                    sendRequestToUser(userID);
            }



        });
        setUpLocation();
        updateFirebaseToken();
    }

    private void updateFirebaseToken() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_tb1);

        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(token);
    }
    //rider App 34:44
    private void sendRequestToUser(String userID) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tb1);
        tokens.orderByKey().equalTo(userID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                            Token token = postSnapshot.getValue(Token.class);// Get Token object form database with key
                            //Make raw payload - convert LatLng to json
                            String json_lat_lng = new Gson().toJson(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                            String riderToken = FirebaseInstanceId.getInstance().getToken();
                            Notification data = new Notification(riderToken,json_lat_lng);// send to Driver app and we will deserialize it again
                            Sender content = new Sender(token.getToken(),data);
                            mFCMService.sendMessage(content)
                                    .enqueue(new Callback<FCMResponse>() {
                                        @Override
                                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                            if(response.body().success == 1)
                                                Toast.makeText(Welcome.this, "Request sent!", Toast.LENGTH_SHORT).show();
                                            else
                                                Toast.makeText(Welcome.this, "Failed !", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                                            Log.e("ERROR",t.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void selectNavMenu(int navItemIndex) {
        navigationView.getMenu().getItem(navItemIndex).setChecked(false);
        navigationView.getMenu().getItem(0).setChecked(true);
        if (drawer != null)
            drawer.closeDrawers();
    }

    private void setUpNavigationView() {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {

                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.navI_home:
                                navigationView.getMenu().getItem(navItemIndex).setChecked(false);
                                navItemIndex = 0;
                                selectNavMenu(navItemIndex);
                                CURRENT_TAG = TAG_HOME;
                                break;
                            case R.id.navI_messaging:
                                navigationView.getMenu().getItem(navItemIndex).setChecked(false);
                                navItemIndex = 1;
                                selectNavMenu(navItemIndex);
                                Intent go0 = new Intent(Welcome.this, HomeActivity.class);
                                go0.putExtra("navItemIndex", 1);
                                go0.putExtra("username", userName);
                                startActivityForResult(go0, 0);
                                break;
                            case R.id.navI_makeplaydate:
                                navigationView.getMenu().getItem(navItemIndex).setChecked(false);
                                navItemIndex = 2;
                                selectNavMenu(navItemIndex);
                                Intent go1 = new Intent(Welcome.this, HomeActivity.class);
                                go1.putExtra("navItemIndex", 2);
                                go1.putExtra("username", userName);
                                startActivityForResult(go1, 0);
                                break;
                            case R.id.navI_playdates:
                                navigationView.getMenu().getItem(navItemIndex).setChecked(false);
                                navItemIndex = 3;
                                selectNavMenu(navItemIndex);
                                Intent go2 = new Intent(Welcome.this, HomeActivity.class);
                                go2.putExtra("navItemIndex", 3);
                                go2.putExtra("username", userName);
                                startActivityForResult(go2, 0);
                                break;
                            case R.id.navI_userprofile:
                                navigationView.getMenu().getItem(navItemIndex).setChecked(false);
                                navItemIndex = 4;
                                selectNavMenu(navItemIndex);
                                Intent go = new Intent(Welcome.this, HomeActivity.class);
                                go.putExtra("navItemIndex", 4);
                                go.putExtra("username", userName);
                                startActivityForResult(go, 0);
                                break;
                            case R.id.navI_settings:
                                navigationView.getMenu().getItem(navItemIndex).setChecked(false);
                                navItemIndex = 5;
                                selectNavMenu(navItemIndex);
                                Intent go3 = new Intent(Welcome.this, HomeActivity.class);
                                go3.putExtra("navItemIndex", 5);
                                go3.putExtra("username", userName);
                                startActivityForResult(go3, 0);
                                break;
                            case R.id.action_logout:
                                Toast.makeText(getApplicationContext(), "Logging out..", Toast.LENGTH_SHORT).show();
                                FirebaseAuth.getInstance().signOut();
                                Intent goToLogin = new Intent(Welcome.this, MainActivity.class);
                                startActivity(goToLogin);
                                finish();
                                break;
                            default:
                                navItemIndex = 0;
                                selectNavMenu(navItemIndex);
                        }

                        return true;
                    }
                });

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);
                }

                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                }
            };

            drawer.setDrawerListener(actionBarDrawerToggle);

            actionBarDrawerToggle.syncState();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 0) {
            if(resultCode == Activity.RESULT_OK){
                navItemIndex = data.getIntExtra("navItemIndex", 0);
                selectNavMenu(navItemIndex);
                CURRENT_TAG = TAG_HOME;
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Intent goToLogin = new Intent(Welcome.this, MainActivity.class);
                startActivity(goToLogin);
                finish();
            }
        }
    }

    // TODO
    // Related with 'find nearby users'
    // part 7 21:26 and part 6
    // requestDateHere is equal with requestPickupHere
    private void requestDateHere(String uid) {

        // Todo: might have issues here

        DatabaseReference dbRequest = FirebaseDatabase.getInstance().getReference(Common.date_request_tb1);
        GeoFire mGeoFire = new GeoFire(dbRequest);
        mGeoFire.setLocation(uid, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

        if(mUserMarker != null && mUserMarker.isVisible()) {
            mUserMarker.remove();
        }
        // Add nre marker
        mUserMarker = mMap.addMarker(new MarkerOptions()
                .title("Date Here")
                .snippet("")
                .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        );
        mUserMarker.showInfoWindow();
        btnRequestDate.setText("Waiting for response...");
        findUser();
    }

    private void findUser() {

        DatabaseReference users = FirebaseDatabase.getInstance().getReference(Common.user_location_tb1);
        GeoFire gfUsers = new GeoFire(users);
        GeoQuery geoQuery = gfUsers.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()),radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {

            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if(!isUserFound && !FirebaseAuth.getInstance().getCurrentUser().getUid().equals(key)) {

                    isUserFound = true;
                    userID = key;
                    btnRequestDate.setText("Request nearest date");
                    Toast.makeText(Welcome.this,""+key,Toast.LENGTH_SHORT);

                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                // if still not found driver, increase distance
                if(!isUserFound) {

                    radius++;
                    findUser();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    private void getDirection() {
        currentPosition = new LatLng(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude());

        String requestApi = null;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + currentPosition.latitude + "," + currentPosition.longitude + "&" +
                    "destination=" + destination + "&" +
                    "key=" + getResources().getString(R.string.google_direction_api);
            Log.d("EDMTDEV", requestApi); // print URL for debug
            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {

                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray jsonArray = jsonObject.getJSONArray("routes");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject route = jsonArray.getJSONObject(i);
                                    JSONObject poly = route.getJSONObject("overview_polyline");
                                    String polyline = poly.getString("points");
                                    polyLineList = decodePoly(polyline);
                                }
                                //Adjusting bounds
                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                for (LatLng latLng : polyLineList) {
                                    builder.include(latLng);
                                }
                                LatLngBounds bounds = builder.build();
                                CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);
                                mMap.animateCamera(mCameraUpdate);

                                polylineOptions = new PolylineOptions();
                                polylineOptions.color(Color.GRAY);
                                polylineOptions.width(5);
                                polylineOptions.startCap(new SquareCap());
                                polylineOptions.endCap(new SquareCap());
                                polylineOptions.jointType(JointType.ROUND);
                                polylineOptions.addAll(polyLineList);
                                greyPolyline = mMap.addPolyline(polylineOptions);

                                blackPolylineOptions = new PolylineOptions();
                                blackPolylineOptions.color(Color.BLACK);
                                blackPolylineOptions.width(5);
                                blackPolylineOptions.startCap(new SquareCap());
                                blackPolylineOptions.endCap(new SquareCap());
                                blackPolylineOptions.jointType(JointType.ROUND);
                                blackPolyline = mMap.addPolyline(blackPolylineOptions);

                                mMap.addMarker(new MarkerOptions().
                                        position(polyLineList.get(polyLineList.size() - 1))
                                        .title("Meet Location")
                                );

                                // Animation
                                ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0, 100);
                                polyLineAnimator.setDuration(2000);
                                polyLineAnimator.setInterpolator(new LinearInterpolator());
                                polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                        List<LatLng> points = greyPolyline.getPoints();
                                        int percentValue = (int) valueAnimator.getAnimatedValue();
                                        int size = points.size();
                                        int newPoints = (int) (size * (percentValue / 100.0f));
                                        List<LatLng> p = points.subList(0, newPoints);
                                        blackPolyline.setPoints(p);
                                    }
                                });
                                polyLineAnimator.start();

                                carMarker = mMap.addMarker(new MarkerOptions().position(currentPosition)
                                        .flat(true)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.dog)));

                                handler = new Handler();
                                index = -1;
                                next = 1;
                                handler.postDelayed(drawPathRunnable, 3000);


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(Welcome.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }catch(Exception e){
            e.getStackTrace();
        }

    }

    private List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(checkPlayService()){
                        buildGoogleApiclient();
                        createLocationRequest();
                        if(location_switch.isChecked())
                            displayLocation();
                    }
                }
        }
    }

    private void setUpLocation() {
        if((ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED))
        {
            //Request runtime permission
            ActivityCompat.requestPermissions(this,new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            },MY_PERMISSION_REQUEST_CODE);
        }
        else{
            if(checkPlayService()){
                buildGoogleApiclient();
                createLocationRequest();
                if(location_switch.isChecked()){
                    displayLocation();
                }
            }
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiclient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayService() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS){
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICE_RES_REQUEST).show();
            else {
                Toast.makeText(this, "Thia device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void stopLocationUpdates() {
        if((ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)){
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
    }

    private void displayLocation() {
        if((ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)){
            return;
        }
        Common.mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(Common.mLastLocation != null){

            if(location_switch.isChecked()) {

                final double latitude = Common.mLastLocation.getLatitude();
                final double longtitude = Common.mLastLocation.getLongitude();
                removeOtherUserMarkers();
                // update to firebase
                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                        new GeoLocation(latitude, longtitude),
                        new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {
                                if (mCurrent != null)
                                    mCurrent.remove(); //remove already marker

                                mCurrent = mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(latitude, longtitude))
                                        .title("Your Location"));

                                //Move camera to this position
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longtitude), 15.0f));

                                loadAllAvailableUser();

                                Log.d("Error", String.format("Your location was changed : %f / %f", latitude, longtitude));

                            }
                        });
            }

        }
        else{
            Log.d("Error","Can not get your location");
        }
    }

    private void loadAllAvailableUser() {

        // Load all available users in distance 3km
        DatabaseReference userLocation = FirebaseDatabase.getInstance().getReference(Common.user_location_tb1);
        GeoFire gf = new GeoFire(userLocation);

        GeoQuery geoQuery = gf.queryAtLocation(new GeoLocation(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude()),distance);
        geoQuery.removeAllListeners();

        removeOtherUserMarkers();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {

                FirebaseDatabase.getInstance().getReference(Common.user_info_tb1)
                        .child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        User user = dataSnapshot.getValue(User.class);
                        if (!FirebaseAuth.getInstance().getCurrentUser().getEmail().equals(user.getEmail())) {
                            // add user to map

                            Marker marker = mMap.addMarker(new MarkerOptions().
                                    position(new LatLng(location.latitude, location.longitude))
                                    .flat(true)
                                    .title(user.getName())
                                    .snippet("Phone: " + user.getPhone())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.dog)));
                            user_marker_to_email_address.put(marker, dataSnapshot.getKey());
                            user_marker_to_name.put(marker, user);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                if(distance <= LIMIT) {

                    distance++;
                    loadAllAvailableUser();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void rotateMarker(final Marker mCurrent, final int i, GoogleMap mMap) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = mCurrent.getRotation();
        final long duration = 1500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float)elapsed/duration);
                float rot = t*i+(1-t)*startRotation;
                mCurrent.setRotation(-rot>180?rot/2:rot);
                if(t<1.0){
                    handler.postDelayed(this,16);
                }
            }
        });

    }

    @TargetApi(23)
    private void startLocationUpdates() {
        if(Build.VERSION.SDK_INT >= 23 && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)){
            return;
        }
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMarkerClickListener(this);
    }


    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override
    public void onLocationChanged(Location location) {
        Common.mLastLocation = location;
        displayLocation();
    }
/*
    @Override
    public void onLocationChanged(Location var1)
    {}
*/

    /*
        TODO: use setUnreadMessage to mark hamburger and navbar for unread messages
     */
    void setUnreadMessages(boolean showMarkers) {
        if(showMarkers) {
            navBarMessageMarker.setVisibility(View.VISIBLE);
            if(hamburgerMessageMarker != null)
                hamburgerMessageMarker.setVisibility(View.VISIBLE);
        }
        else {
            navBarMessageMarker.setVisibility(View.INVISIBLE);
            if(hamburgerMessageMarker != null)
                hamburgerMessageMarker.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
           setUpNavigationView();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){

        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onMarkerClick (Marker marker) {
        if (user_marker_to_email_address.get(marker) != null) {
            selected_user_email = user_marker_to_email_address.get(marker);

            //ToDo: query UsersInformation table and get the selected_user_name and toast it
            User user = user_marker_to_name.get(marker);
            Toast t = Toast.makeText(this, "User Name is: " + user.getName() + "\nEmail name is: " + user.getEmail(), Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER, 0, -20);
            t.show();

            Log.d("aaa", selected_user_email);
        }
        return true;
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

    private void removeOtherUserMarkers()
    {
        for (int i = 0; i < mOtherUserMarkers.size(); ++i) {
            mOtherUserMarkers.get(i).remove();
        }
        mOtherUserMarkers.clear();

    }

}
