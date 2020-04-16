package com.example.getsend;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.FirebaseError;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;

import org.json.JSONObject;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class InviteDeliveryActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edtxtWeight, edtxtSize, edtxtLocation, edtxtDestination;

    private Button btnEnter;
    private DatabaseReference refPackage, refUser;
    private Package new_package;
    private User user;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private static final int USER_TYPE_DELIVERY_GETTER = 1;
    private static final String DELIMITER = " ";
    private int flagLocation = 0;
    private Point firstResultPoint, locationPoint, destinationPoint;
    private MapboxGeocoding mapboxGeocoding;
    private String locationToGeo, phone, lastPackageKey;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_delivery);
        Mapbox.getInstance(this, getString(R.string.access_token));
        edtxtWeight = (EditText) findViewById(R.id.edtxt_weight);
        edtxtSize = (EditText) findViewById(R.id.edtxt_size);
        edtxtLocation = (EditText) findViewById(R.id.edtxt_location);
        findViewById(R.id.edtxt_location).setOnClickListener(this);
        edtxtDestination = (EditText) findViewById(R.id.edtxt_destination);
        findViewById(R.id.edtxt_destination).setOnClickListener(this);
        btnEnter = (Button) findViewById(R.id.btn_enter);
        //create a new DB table of package if not exist
        refPackage = FirebaseDatabase.getInstance().getReference().child("Package");
        refUser = FirebaseDatabase.getInstance().getReference().child("User");
        btnEnter.setOnClickListener(this);
        new_package = new Package();
        user = new User();
        sharedPref = getSharedPreferences("userDetails", MODE_PRIVATE);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_enter:
                //integrity check
                checkAllInputs();

                final String size = edtxtSize.getText().toString().trim();
                final double weight = Double.parseDouble(edtxtWeight.getText().toString());
                new_package.setSize(size);
                new_package.setWeight(weight);
                new_package.setPackageOwnerId(sharedPref.getString("userKey", ""));

                //push package to DB
                DatabaseReference newRefPackage = refPackage.push();
                lastPackageKey = newRefPackage.getKey();
                newRefPackage.setValue(new_package);
                Toast.makeText(InviteDeliveryActivity.this, "Package registered successfully!", Toast.LENGTH_LONG).show();
                userTypeUpdate();
                user.setPhone(phone);
                user.setName(sharedPref.getString("name", ""));
                addPacakgeToCurrentUser();
                cleanEdtTexts();
                break;
            case R.id.edtxt_location:
                flagLocation = 1;
                placeAutoCoplete();
                break;
            case R.id.edtxt_destination:
                flagLocation = 0;
                placeAutoCoplete();
                break;
        }
    }

    private void addPacakgeToCurrentUser() {
        refUser.orderByChild("phone").equalTo(phone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot datas : dataSnapshot.getChildren()) {
                        String userPackages = datas.child("packages").getValue().toString();
                        refUser.child(datas.getKey()).child("packages").setValue(userPackages + lastPackageKey + DELIMITER);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void userTypeUpdate() {
        phone = sharedPref.getString("phone", "");
        // find user by his phone numder
        Query query = refUser.orderByChild("phone").equalTo(phone);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //change this user type in the database
                refUser.child(dataSnapshot.getKey()).child("type").setValue(USER_TYPE_DELIVERY_GETTER);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }

        });
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putString("type", String.valueOf(USER_TYPE_DELIVERY_GETTER));
        prefEditor.commit();
    }


    // check input correction anf if all edtxt filled
    private void checkAllInputs() {
        if(edtxtWeight.getText().toString().matches("")){
            this.edtxtWeight.setError("Weight required");
            this.edtxtWeight.requestFocus();
            return;
        }
        if(edtxtSize.getText().toString().matches("")){
            this.edtxtSize.setError("Size required");
            this.edtxtSize.requestFocus();
            return;
        }
        if(edtxtLocation.getText().toString().matches("")){
            this.edtxtLocation.setError("Location required");
            this.edtxtLocation.requestFocus();
            return;
        }
        if(edtxtDestination.getText().toString().matches("")){
            this.edtxtDestination.setError("Destination required");
            this.edtxtDestination.requestFocus();
            return;
        }
    }

    private void cleanEdtTexts() {
        edtxtWeight.setText("");
        edtxtSize.setText("");
        edtxtLocation.setText("");
        edtxtDestination.setText("");
    }

    public void placeAutoCoplete() {
        Intent intent = new PlaceAutocomplete.IntentBuilder()
                .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.access_token))
                .placeOptions(PlaceOptions.builder()
                        .country("IL")
                        .backgroundColor(Color.parseColor("#EEEEEE"))
                        .limit(10)
                        .build(PlaceOptions.MODE_CARDS))
                .build(InviteDeliveryActivity.this);
        startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
        return;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            CarmenFeature feature = PlaceAutocomplete.getPlace(data);
            //location case
            if (flagLocation == 1) {
                locationToGeo = feature.text();
                locationPoint = mapboxGeocoding(locationToGeo);
                //check if the address found on the map in geocode
                if(locationPoint != null) {
                    new_package.setGeoLocation(locationPoint.coordinates().toString());// set delivery location to package
                    new_package.setLocation(locationToGeo);
                    edtxtLocation.setText(feature.text());
                }else{
                    Toast.makeText(InviteDeliveryActivity.this, R.string.reEnter_location, Toast.LENGTH_LONG).show();
                }
                //destination case
            } else {
                locationToGeo = feature.text();
                destinationPoint = mapboxGeocoding(locationToGeo);
                //check if the address found on the map in geocode
                if(destinationPoint != null){
                    new_package.setGeoDestination(destinationPoint.coordinates().toString());// set delivery destination to package
                    new_package.setDestination(locationToGeo);
                    edtxtDestination.setText(feature.text());
                }else{
                    Toast.makeText(InviteDeliveryActivity.this, R.string.reEnter_location, Toast.LENGTH_LONG).show();
                }
            }

        }
    }
    // gets a string address as street name, city ect, and return geojson point
    public Point mapboxGeocoding(String locationToGeo) {
        mapboxGeocoding = MapboxGeocoding.builder()
                .accessToken(getString(R.string.access_token))
                .query(locationToGeo)
                .build();
        mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                List<CarmenFeature> results = response.body().features();

                if (results.size() > 0) {
                    // Log the first results Point.
                    firstResultPoint = results.get(0).center();
                    Log.d("s", "onResponse: " + firstResultPoint.toString());
                } else {
                    // No result for your request were found.
                    Log.d("f", "onResponse: No result found");
                }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        return firstResultPoint;
    }


    public void onDestroy() {

        super.onDestroy();
        mapboxGeocoding.cancelCall();
    }
}
