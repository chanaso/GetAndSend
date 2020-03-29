package com.example.getsend;
import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.google.gson.JsonObject;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.light.Position;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;


public class InviteDeliveryActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText edtxtWeight;
    private EditText edtxtSize;
    private EditText edtxtLocation;
    private EditText edtxtDestination;
    private Button btnEnter;
    private DatabaseReference reff;
    private Package new_package;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_delivery);

        edtxtWeight = (EditText) findViewById(R.id.edtxt_weight);
        edtxtSize = (EditText) findViewById(R.id.edtxt_size);
        edtxtLocation =(EditText) findViewById(R.id.edtxt_location);
        GeocoderAutoCompleteView autocomplete = (GeocoderAutoCompleteView) findViewById (R.id.edtxt_location);
        Autocomplete.setAccessToken (Mapbox.getAccessToken ());
        Autocomplete.setType (GeocodingCriteria.TYPE_ADDRESS);
        Autocomplete.setOnFeatureListener (new GeocoderAutoCompleteView.OnFeatureListener () {
            @Override
            Public void onFeatureClick (CarmenFeature feature) {
                Position position = feature.asPosition ();
            }
        });
        edtxtDestination = (EditText) findViewById(R.id.edtxt_destination);
        btnEnter = (Button) findViewById(R.id.btn_enter);
        //create a new DB table of package if not exist
        reff = FirebaseDatabase.getInstance().getReference().child("Package");

        btnEnter.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        //create a new object of package
        final String size = edtxtSize.getText().toString().trim();
//        final String location = edtxtLocation.getText().toString().trim();
        final String destination = edtxtDestination.getText().toString().trim();
        final double weight = Double.parseDouble(edtxtWeight.getText().toString());

        new_package = new Package(size, destination, destination, weight);

        //push package to DB
        reff.push().setValue(new_package);
        Toast.makeText(InviteDeliveryActivity.this, "Package registered successfully!", Toast.LENGTH_LONG).show();
    }
}
