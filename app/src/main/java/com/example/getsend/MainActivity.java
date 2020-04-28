package com.example.getsend;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.IntentCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.List;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, PermissionsListener, NavigationView.OnNavigationItemSelectedListener
{
    private static final int USER_TYPE_DELIVERYMAN = 0;
    private static final int USER_TYPE_DELIVERY_GETTER = 1;
    private static final int USER_TYPE_IN_PROCCESS = 2;

    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private MapView mapView;
    private Button goBtn ;
    private DrawerLayout drawer;
    private String userName, phone, type, rate, userKey;
    private SharedPreferences sharedPref;
    Button btnJoin, btnInvite;
    DatabaseReference refUser;

    @Override
    protected void  onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token));

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        refUser = FirebaseDatabase.getInstance().getReference().child("User");

        btnInvite = (Button) findViewById(R.id.btnInvite);
        btnJoin = (Button) findViewById(R.id.btnJoin);


        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        //getting the current username from the sp
        sharedPref = getSharedPreferences("userDetails", MODE_PRIVATE);
        userName = sharedPref.getString("name", "");
        phone = sharedPref.getString("phone", "");
        rate = sharedPref.getString("rate", "");
        type = sharedPref.getString("type","");
        userKey = sharedPref.getString("userKey","");

        checkType();
        checkUserExist();
        NavigationView nav_view= (NavigationView)findViewById(R.id.nav_view);//this is navigation view from my main xml where i call another xml file
        View header = nav_view.getHeaderView(0);//set View header to nav_view first element (i guess)
        TextView txt = (TextView)header.findViewById(R.id.UserNameID);//now assign textview imeNaloga to header.id since we made View header.
        txt.setText(userName);// And now just set text to that textview
        nav_view.setNavigationItemSelectedListener(this);
        nav_view.bringToFront();


        btnInvite.setOnClickListener(this);
        btnJoin.setOnClickListener(this);

    }

    private void checkUserExist() {
        refUser.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Log.d("ic", "userExist");
                } else {
                    // User does not exist.
                    signOut();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void checkType() {
        if(type.equals(String.valueOf(USER_TYPE_DELIVERY_GETTER))){
            btnJoin.setVisibility(View.GONE);
            btnInvite.setText("i want another delivery service");
            btnInvite.setGravity(Gravity.CENTER);
        }
        if(type.equals(String.valueOf(USER_TYPE_DELIVERYMAN))){
            btnInvite.setVisibility(View.GONE);
            btnJoin.setText("i want to take another delivery");
            btnJoin.setGravity(Gravity.CENTER);
            /////do somthinggggg
            //TODO
        }
        if(type.equals(String.valueOf(USER_TYPE_IN_PROCCESS))){
            btnInvite.setVisibility(View.GONE);
            btnJoin.setText("Waiting for confirmation...");
            btnJoin.setEnabled(false);
            btnJoin.setGravity(Gravity.CENTER);
            /////do somthinggggg
            //TODO
        }

    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        MainActivity.this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(style);
                    }
                });
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            // Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnInvite:
            {
                startActivity(new Intent(MainActivity.this, InviteDeliveryActivity.class));
                break;
            }
            case R.id.btnJoin:
            {
                startActivity(new Intent(MainActivity.this, JoinAsDeliverymanActivity.class));
                finish();
                break;
            }

        }
    }
    // navbar selection list and move to the selected option/activity
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_profile:
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                break;
            case R.id.nav_packeges:
                startActivity(new Intent(MainActivity.this, PackagesActivity.class));
                break;
            case R.id.nav_sign_out:
                signOut();
                break;

        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signOut() {
        //delete the existing userName
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putString("name","");
        prefEditor.commit();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }
}
