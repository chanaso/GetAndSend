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

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class InviteDeliveryActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edtxt_Weight, edtxt_Size, edtxt_Location, edtxt_Destination, edtxt_PackageId;

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
        edtxt_Weight = findViewById(R.id.edtxt_WeightID);
        edtxt_Size = findViewById(R.id.edtxt_SizeID);
        edtxt_Location = findViewById(R.id.edtxt_LocationID);
        edtxt_PackageId = findViewById(R.id.edtxt_PackageIdID);
        edtxt_Destination = findViewById(R.id.edtxt_DestinationID);

        findViewById(R.id.btnEnterID).setOnClickListener(this);
        findViewById(R.id.edtxt_LocationID).setOnClickListener(this);
        findViewById(R.id.edtxt_DestinationID).setOnClickListener(this);

        //create a new DB table of package, User if not exist
        refPackage = FirebaseDatabase.getInstance().getReference().child("Package");
        refUser = FirebaseDatabase.getInstance().getReference().child("User");
        new_package = new Package();
        user = new User();
        sharedPref = getSharedPreferences("userDetails", MODE_PRIVATE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnEnterID:
                //integrity check
                checkAllInputs();

                String packageId = edtxt_PackageId.getText().toString().trim();
                String size = edtxt_Size.getText().toString().trim();
                double weight = Double.parseDouble(edtxt_Weight.getText().toString());

                //setting the new package by rhe inputs
                new_package.setPackageId(packageId);
                new_package.setSize(size);
                new_package.setWeight(weight);
                new_package.setPackageOwnerId(sharedPref.getString("userKey", ""));

                //push package to DB
                DatabaseReference newRefPackage = refPackage.push();
                lastPackageKey = newRefPackage.getKey();
                newRefPackage.setValue(new_package);
                Toast.makeText(InviteDeliveryActivity.this, "Package registered successfully!", Toast.LENGTH_LONG).show();

                userTypeUpdate();
                user.setName(sharedPref.getString("name", ""));
                addPacakgeToCurrentUser();
                cleanEdtTexts();
                break;
            case R.id.edtxt_LocationID:
                flagLocation = 1;
                placeAutoCoplete();
                break;
            case R.id.edtxt_DestinationID:
                flagLocation = 0;
                placeAutoCoplete();
                break;
        }
    }

    //add the package key that added to the current user list of keys packages
    private void addPacakgeToCurrentUser() {
        refUser.orderByChild("phone").equalTo(phone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot datas : dataSnapshot.getChildren()) {
                        String userPackages = datas.child("packages").getValue().toString();
                        //set the previous keys + the new package key
                        refUser.child(datas.getKey()).child("packages").setValue(userPackages + lastPackageKey + DELIMITER);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // update user type to be 1- user as a delivery getter
    private void userTypeUpdate() {
        phone = sharedPref.getString("phone", "");
        user.setPhone(phone);
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
        //saved in the local memeory
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putString("type", String.valueOf(USER_TYPE_DELIVERY_GETTER));
        prefEditor.commit();
    }


    // check input correction and if all edtxt_ filled
    private void checkAllInputs() {
        if(edtxt_Weight.getText().toString().matches("")){
            this.edtxt_Weight.setError("Weight required");
            this.edtxt_Weight.requestFocus();
            return;
        }
        if(edtxt_PackageId.getText().toString().matches("")){
            this.edtxt_PackageId.setError("Package Id required");
            this.edtxt_PackageId.requestFocus();
            return;
        }
        if(edtxt_Size.getText().toString().matches("")){
            this.edtxt_Size.setError("Size required");
            this.edtxt_Size.requestFocus();
            return;
        }
        if(edtxt_Location.getText().toString().matches("")){
            this.edtxt_Location.setError("Location required");
            this.edtxt_Location.requestFocus();
            return;
        }
        if(edtxt_Destination.getText().toString().matches("")){
            this.edtxt_Destination.setError("Destination required");
            this.edtxt_Destination.requestFocus();
            return;
        }
    }

    private void cleanEdtTexts() {
        edtxt_PackageId.setText("");
        edtxt_Weight.setText("");
        edtxt_Size.setText("");
        edtxt_Location.setText("");
        edtxt_Destination.setText("");
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
                    edtxt_Location.setText(feature.text());
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
                    edtxt_Destination.setText(feature.text());
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
