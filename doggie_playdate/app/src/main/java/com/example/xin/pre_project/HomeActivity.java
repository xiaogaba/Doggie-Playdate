package com.example.xin.pre_project;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xin.pre_project.Fragments.AccountSettingsFragment;
import com.example.xin.pre_project.Fragments.AddDogFragment;
import com.example.xin.pre_project.Fragments.CreatePlaydateFragment;
import com.example.xin.pre_project.Fragments.MessagingFragment;
import com.example.xin.pre_project.Fragments.MyPlaydatesFragment;
import com.example.xin.pre_project.Fragments.ProfileFragment;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity

        implements
        MyPlaydatesFragment.PlaydateListener,
        CreatePlaydateFragment.AfterCreatePlaydate,
        ProfileFragment.AddDogFAB,
        AddDogFragment.ReturnToProfile {
    public static Context gContext;

    private NavigationView navigationView;
    private DrawerLayout drawer;
    private Toolbar toolbar;

    private TextView hamburgerMessageMarker, toolbarTitle;
    private ImageView navBarMessageMarker;

    private Bundle b;
    public static int navItemIndex = 0;

    private static final String TAG_HOME = "home";
    private static final String TAG_MESSAGING = "messaging";
    private static final String TAG_CREATEPLAYDATE = "createplaydate";
    private static final String TAG_PROFILE = "profile";
    private static final String TAG_MYPLAYDATES = "myplaydates";
    private static final String TAG_ACCOUNTSETTINGS = "accountsettings";
    private static final String TAG_ADDDOG = "adddog";

    private Fragment fragment;
    public static String CURRENT_TAG = TAG_HOME;

    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_nav_drawer);

        gContext = this;

        toolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        toolbarTitle = findViewById(R.id.toolbar_title);

        navBarMessageMarker = (ImageView) navigationView.getMenu().findItem(R.id.navI_messaging).getActionView();
        hamburgerMessageMarker = (TextView) findViewById(R.id.hamburger_marker);

        /*
            TODO: check for unread messages
         */
        setUnreadMessages(false);

        setUpNavigationView();

        b = getIntent().getExtras();

        if(savedInstanceState != null) {
            navItemIndex = savedInstanceState.getInt("saveIndex", 0);
            switch(navItemIndex) {
                case 1:
                    CURRENT_TAG = TAG_MESSAGING;
                    selectNavMenu(navItemIndex);
                    setToolbarTitle(1);
                    fragment = getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
                    if (fragment == null)
                        fragment = new MessagingFragment();
                    navToFragment(fragment);
                    break;
                case 2:
                    CURRENT_TAG = TAG_CREATEPLAYDATE;
                    selectNavMenu(navItemIndex);
                    setToolbarTitle(2);
                    fragment = getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
                    if (fragment == null)
                        fragment = new CreatePlaydateFragment();
                    navToFragment(fragment);
                    break;
                case 3:
                    CURRENT_TAG = TAG_MYPLAYDATES;
                    selectNavMenu(navItemIndex);
                    setToolbarTitle(3);
                    fragment = getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
                    if (fragment == null)
                        fragment = new MyPlaydatesFragment();
                    navToFragment(fragment);
                    break;
                case 4:
                    CURRENT_TAG = TAG_PROFILE;
                    selectNavMenu(navItemIndex);
                    setToolbarTitle(4);
                    fragment = getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
                    if (fragment == null)
                        fragment = new ProfileFragment();
                    navToFragment(fragment);
                    break;
                case 5:
                    CURRENT_TAG = TAG_ACCOUNTSETTINGS;
                    selectNavMenu(navItemIndex);
                    setToolbarTitle(5);
                    fragment = getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
                    if (fragment == null)
                        fragment = new AccountSettingsFragment();
                    navToFragment(fragment);
                    break;
                case 6:
                    CURRENT_TAG = TAG_ADDDOG;
                    selectNavMenu(navItemIndex);
                    setToolbarTitle(6);
                    fragment = getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
                    if (fragment == null)
                        fragment = new AddDogFragment();
                    navToFragment(fragment);
                    break;
                default:
                    CURRENT_TAG = TAG_MYPLAYDATES;
                    selectNavMenu(navItemIndex);
                    setToolbarTitle(3);
                    fragment = getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
                    if (fragment == null)
                        fragment = new MyPlaydatesFragment();
                    navToFragment(fragment);
            }

        }
        else if(b != null) {
            navItemIndex = b.getInt("navItemIndex", 1);
            userName = b.getString("username", "");
            switch (navItemIndex) {
                case 1:
//                    CURRENT_TAG = TAG_MESSAGING;
//                    selectNavMenu(navItemIndex);
//                    setToolbarTitle(1);
//                    fragment = getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
//                    if (fragment == null) {
//                        fragment = new MessagingFragment();
//                        b = new Bundle();
//                        b.putString("username", userName);
//                        fragment.setArguments(b);
//                    }
//                    navToFragment(fragment);
                    Intent intent = new Intent(HomeActivity.this, ChatList.class);
                    startActivity(intent);
                    break;
                case 2:
                    CURRENT_TAG = TAG_CREATEPLAYDATE;
                    selectNavMenu(navItemIndex);
                    setToolbarTitle(2);
                    fragment = getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
                    if (fragment == null) {
                        fragment = new CreatePlaydateFragment();
                        b = new Bundle();
                        b.putString("username", userName);
                        fragment.setArguments(b);
                    }
                    navToFragment(fragment);
                    break;
                case 3:
                    CURRENT_TAG = TAG_MYPLAYDATES;
                    selectNavMenu(navItemIndex);
                    setToolbarTitle(3);
                    fragment = getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
                    if (fragment == null) {
                        fragment = new MyPlaydatesFragment();
                        b = new Bundle();
                        b.putString("username", userName);
                        fragment.setArguments(b);
                    }
                    navToFragment(fragment);
                    break;
                case 4:
                    CURRENT_TAG = TAG_PROFILE;
                    selectNavMenu(navItemIndex);
                    setToolbarTitle(4);
                    fragment = getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
                    if (fragment == null) {
                        fragment = new ProfileFragment();
                        b = new Bundle();
                        b.putString("username", userName);
                        fragment.setArguments(b);
                    }
                    navToFragment(fragment);
                    break;
                case 5:
                    CURRENT_TAG = TAG_ACCOUNTSETTINGS;
                    selectNavMenu(navItemIndex);
                    setToolbarTitle(5);
                    fragment = getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
                    if (fragment == null) {
                        fragment = new AccountSettingsFragment();
                        b = new Bundle();
                        b.putString("username", userName);
                        fragment.setArguments(b);
                    }
                    navToFragment(fragment);
                    break;
                case 6:
                    CURRENT_TAG = TAG_ADDDOG;
                    selectNavMenu(navItemIndex);
                    setToolbarTitle(6);
                    fragment = getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
                    if (fragment == null) {
                        fragment = new AddDogFragment();
                        b = new Bundle();
                        b.putString("username", userName);
                        fragment.setArguments(b);
                    }
                    navToFragment(fragment);
                    break;
                default:
                    CURRENT_TAG = TAG_MYPLAYDATES;
                    selectNavMenu(navItemIndex);
                    setToolbarTitle(3);
                    fragment = getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
                    if (fragment == null) {
                        fragment = new MyPlaydatesFragment();
                        b = new Bundle();
                        b.putString("username", userName);
                        fragment.setArguments(b);
                    }
                    navToFragment(fragment);
                    break;
            }
        }
        else {
            // error in navigating, go back to home screen
            finish();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("saveIndex", navItemIndex);
        //outState.putString("CURRENT_TAG", CURRENT_TAG);
    }

    private void setUpNavigationView() {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {

                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.navI_home:
                                navItemIndex = 0;
                                selectNavMenu(navItemIndex);

                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("navItemIndex", navItemIndex);
                                setResult(Activity.RESULT_OK, returnIntent);
                                finish();
                                break;
                            case R.id.navI_messaging:
                                navItemIndex = 1;
                                CURRENT_TAG = TAG_MESSAGING;
                                selectNavMenu(navItemIndex);
                                setToolbarTitle(1);
                                fragment = getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
                                if(fragment == null) {
                                    fragment = new MessagingFragment();
                                    b = new Bundle();
                                    b.putString("username", userName);
                                    fragment.setArguments(b);
                                }
                                navToFragment(fragment);
                                break;
                            case R.id.navI_makeplaydate:
                                navItemIndex = 2;
                                CURRENT_TAG = TAG_CREATEPLAYDATE;
                                selectNavMenu(navItemIndex);
                                setToolbarTitle(2);
                                fragment = getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
                                if(fragment == null) {
                                    fragment = new CreatePlaydateFragment();
                                    b = new Bundle();
                                    b.putString("username", userName);
                                    fragment.setArguments(b);
                                }
                                navToFragment(fragment);
                                break;
                            case R.id.navI_playdates:
                                navItemIndex = 3;
                                CURRENT_TAG = TAG_MYPLAYDATES;
                                selectNavMenu(navItemIndex);
                                setToolbarTitle(3);
                                fragment = getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
                                if(fragment == null) {
                                    fragment = new MyPlaydatesFragment();
                                    b = new Bundle();
                                    b.putString("username", userName);
                                    fragment.setArguments(b);
                                }
                                navToFragment(fragment);
                                break;
                            case R.id.navI_userprofile:
                                navItemIndex = 4;
                                CURRENT_TAG = TAG_PROFILE;
                                selectNavMenu(navItemIndex);
                                setToolbarTitle(4);
                                fragment = getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
                                if(fragment == null) {
                                    fragment = new ProfileFragment();
                                    b = new Bundle();
                                    b.putString("username", userName);
                                    fragment.setArguments(b);
                                }
                                navToFragment(fragment);
                                break;
                            case R.id.navI_settings:
                                navItemIndex = 5;
                                CURRENT_TAG = TAG_ACCOUNTSETTINGS;
                                selectNavMenu(navItemIndex);
                                setToolbarTitle(5);
                                fragment = getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
                                if(fragment == null) {
                                    fragment = new AccountSettingsFragment();
                                    b = new Bundle();
                                    b.putString("username", userName);
                                    fragment.setArguments(b);
                                }
                                navToFragment(fragment);
                                break;
                            case R.id.action_logout:
                                Toast.makeText(getApplicationContext(), "Logging out..", Toast.LENGTH_SHORT).show();
                                FirebaseAuth.getInstance().signOut();
                                Intent returnIntent1 = new Intent();
                                setResult(Activity.RESULT_CANCELED, returnIntent1);
                                finish();

                                break;
                            default:
                                navItemIndex = 0;
                        }

                        return true;
                    }
                });

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
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

    private void selectNavMenu(int navItemIndex) {
        navigationView.getMenu().getItem(navItemIndex).setChecked(true);
        if (drawer != null)
            drawer.closeDrawers();
    }

    private void setToolbarTitle(int x) {
        if(toolbarTitle != null) {
            switch(x) {
                case 1: toolbarTitle.setText("Messages"); break;
                case 2: toolbarTitle.setText("Create Playdate"); break;
                case 3: toolbarTitle.setText("My Playdates"); break;
                case 4: toolbarTitle.setText("My Profile"); break;
                case 5: toolbarTitle.setText("Account Settings"); break;
            }
        }

    }

    public void navToFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                android.R.anim.fade_out);
        fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }
        super.onBackPressed();
    }

    // Implement PlaydatesListener interface
    public void playdatesFabGoToCreatePlaydateFragment() {
        navigationView.getMenu().getItem(navItemIndex).setChecked(false);
        navItemIndex = 2;
        CURRENT_TAG = TAG_CREATEPLAYDATE;
        selectNavMenu(navItemIndex);
        fragment = getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
        if(fragment == null)
            fragment = new CreatePlaydateFragment();
        navToFragment(fragment);
    }

    // Implement CreatePlaydatesFragment interface
    public void goToMyPlaydates() {
        navigationView.getMenu().getItem(navItemIndex).setChecked(false);
        navItemIndex = 3;
        CURRENT_TAG = TAG_MYPLAYDATES;
        selectNavMenu(navItemIndex);
        fragment = getSupportFragmentManager().findFragmentByTag(CURRENT_TAG);
        if(fragment == null)
            fragment = new MyPlaydatesFragment();
        Bundle bd = new Bundle();
        bd.putString("username", userName);
        fragment.setArguments(bd);
        navToFragment(fragment);
    }

    /*
        TODO: use setUnreadMessage to mark hamburger and navbar for unread messages
     */
    public void setUnreadMessages(boolean showMarkers) {
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

    // Implement AddDogFAB interface
    public void navToAddDog() {
        CURRENT_TAG = TAG_ADDDOG;
        AddDogFragment fragment = new AddDogFragment();
        navToFragment(fragment);
    }

    // Implement ReturnToProfile interface
    public void navToProfile() {
        CURRENT_TAG = TAG_PROFILE;
        Fragment fragment = new ProfileFragment();
            Bundle bd = new Bundle();
            bd.putString("username", userName);
            fragment.setArguments(bd);
        navToFragment(fragment);
    }
}