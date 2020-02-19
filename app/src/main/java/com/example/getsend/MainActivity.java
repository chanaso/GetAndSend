package com.example.getsend;

import android.location.Location;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, PermissionsListener, LocationEngineListener{

    private MapView mapView;
    private MapboxMap map;
    private Button goBtn ;
    private DrawerLayout drawer;
    private TextView edtTxt;
    FirebaseAuth mAuth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this,"sk.eyJ1IjoiYW5hZWx6IiwiYSI6ImNrM2c2YnVtbDBiZ2QzaXAwcjM1ZjF3NWUifQ.8sjrOysub87S9Ic-ENb7qg");
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        edtTxt = (TextView) findViewById(R.id.UserNameID);
        edtTxt.setText("vhfjdkf");
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {

    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {

    }

    @Override
    public void onClick(View v) {

    }
}
