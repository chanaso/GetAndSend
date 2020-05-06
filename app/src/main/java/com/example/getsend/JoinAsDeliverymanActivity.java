package com.example.getsend;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

// Use the LocationComponent to easily add a device location "puck" to a Mapbox map.

public class JoinAsDeliverymanActivity extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener,MapboxMap.OnMapClickListener {

    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private MapView mapView;
    private User currUser;
    private DatabaseReference refPackage;
    private List<String> locationsList;
    private static final String SOURCE_ID = "SOURCE_ID";
    private static final String ICON_ID = "ICON_ID";
    private static final String LAYER_ID = "LAYER_ID";
    private static final int USER_TYPE_DELIVERYMAN = 0;
    private List<Feature> symbolLayerIconFeatureList;
    private SharedPreferences sharedPref;
    private String userKey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token));

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_join_as_deliveryman);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        refPackage = FirebaseDatabase.getInstance().getReference().child("Package");
        locationsList = new ArrayList<>();

        // store from local memory the current user
        sharedPref = getSharedPreferences("userDetails", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPref.getString("currUser", "");
        currUser = gson.fromJson(json, User.class);
        userKey = sharedPref.getString("userKey", "");

    }


    private void convertLoctionPointToFeatures() {
        symbolLayerIconFeatureList = new ArrayList<>();
        for (String loc:locationsList) {
            String cleanString = loc.replaceAll("[\\[\\](){}]","");
            String[] splitToLngLat = cleanString.split(",", 2);
                    //  convert all locations points to features
                    symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(Double.parseDouble(splitToLngLat[0]), Double.parseDouble(splitToLngLat[1]))));
        }
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        //  extract packages location from packages DB
        refPackage.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot datas : dataSnapshot.getChildren()) {
                            Package pack = datas.getValue(Package.class);
                            if(pack.getStatus().equals("Waiting for delivery") && !pack.getPackageOwnerId().equals(userKey)) {
                                String location = pack.getGeoLocation();
                                locationsList.add(location);
                            }
                        }
                    }
                convertLoctionPointToFeatures();
                // shows packages location on the map
                JoinAsDeliverymanActivity.this.mapboxMap = mapboxMap;
                mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/streets-v11")
                                // Add the SymbolLayer icon image to the map style
                                .withImage(ICON_ID, BitmapFactory.decodeResource(
                                        JoinAsDeliverymanActivity.this.getResources(), R.drawable.red_marker))
                                // Adding a GeoJson source for the SymbolLayer icons.
                                .withSource(new GeoJsonSource(SOURCE_ID,
                                        FeatureCollection.fromFeatures(symbolLayerIconFeatureList)))
                                // Adding the actual SymbolLayer to the map style. An offset is added that the bottom of the red
                                // marker icon gets fixed to the coordinate, rather than the middle of the icon being fixed to
                                // the coordinate point.
                                .withLayer(new SymbolLayer(LAYER_ID, SOURCE_ID)
                                        .withProperties(PropertyFactory.iconImage(ICON_ID),
                                                iconAllowOverlap(true),
                                                iconOffset(new Float[] {0f, -9f}))
                                )

                        , new Style.OnStyleLoaded() {
                            @Override
                            public void onStyleLoaded(@NonNull Style style) {
                                enableLocationComponent(style);
                                mapboxMap.addOnMapClickListener(JoinAsDeliverymanActivity.this);
                            }
                        });
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
        Toast.makeText(this, "user_location_permission_explanation", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(style -> enableLocationComponent(style));
        } else {
            Toast.makeText(this, "user_location_permission_not_granted", Toast.LENGTH_LONG).show();
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
    public boolean onMapClick(@NonNull LatLng point) {
        //check with of the packages clicked save package id and open in a new page
//        return handleClickIcon(mapboxMap.getProjection().toScreenLocation(point));
        return handleClickIcon(point.getLongitude(), point.getLatitude());
    }


    //  check if the clicked point is a package mark
    private boolean handleClickIcon(double longitude, double latitude) {
        DecimalFormat three = new DecimalFormat("#0.000"); // will round and display the number to four decimal places. No more, no less.
        // the four zeros after the decimal point above specify how many decimal places to be accurate to.
        // the zero to the left of the decimal place above makes it so that numbers that start with "0." will display "0.____" vs just ".____" If you don't want the "0.", replace that 0 to the left of the decimal point with "#"
        List<Feature> featureList = FeatureCollection.fromFeatures(symbolLayerIconFeatureList).features();
        if (featureList != null) {
            for (int i = 0; i < featureList.size(); i++) {
                Point p = (Point) featureList.get(i).geometry();
                double longitudeF = p.longitude(), latitudeF =p.latitude();
                if (three.format(longitudeF).equals(three.format(longitude)) &&  three.format(latitudeF).equals(three.format(latitude))) {
                    Intent intent = new Intent(JoinAsDeliverymanActivity.this, PickedPackagesListActivity.class);
                    // transfer the selected packagelocation as json to PackagePickedActivity
                    intent.putExtra("packageLocation", p.coordinates().toString());
                    startActivity(intent);
                    finish();
                    break;
                } else {
                    DecimalFormat two = new DecimalFormat("#0.00");
                    if (two.format(longitudeF).equals(two.format(longitude)) &&  two.format(latitudeF).equals(two.format(latitude))) {
                        Toast.makeText(JoinAsDeliverymanActivity.this, "Please zoom in the map\nto pick the package!",Toast.LENGTH_LONG).show();
                    }

                }
            }
        }
        return true;
    }

}