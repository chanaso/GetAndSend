package com.example.getsend;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;


public class InviteDeliveryActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edtxt_Weight, edtxt_Size, edtxt_Location, edtxt_Destination, edtxt_PackageId;

    private User currUser;
    private DatabaseReference refPackage, refUser;
    private Package new_package;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private static final int USER_TYPE_DELIVERY_GETTER = 1;
    private int flagLocation = 0;
    private String locationToGeo, userKey, lastPackageKey;
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

        findViewById(R.id.btn_EnterID).setOnClickListener(this);
        findViewById(R.id.edtxt_LocationID).setOnClickListener(this);
        findViewById(R.id.edtxt_DestinationID).setOnClickListener(this);

        //create a new DB table of package, User if not exist
        refPackage = FirebaseDatabase.getInstance().getReference().child("Package");
        refUser = FirebaseDatabase.getInstance().getReference().child("User");
        new_package = new Package();

        // store from local memory the current user
        sharedPref = getSharedPreferences("userDetails", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPref.getString("currUser", "");
        currUser = gson.fromJson(json, User.class);
        userKey = sharedPref.getString("userKey", "");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_EnterID:
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
                Toast.makeText(InviteDeliveryActivity.this, R.string.package_added, Toast.LENGTH_LONG).show();

                addPackageToCurrentUser();
                cleanEdtTxts();
                updateCurrUserInSP();
                startActivity(new Intent(InviteDeliveryActivity.this, MainActivity.class));
                finish();
                break;
            case R.id.edtxt_LocationID:
                flagLocation = 1;
                placeAutoComplete();
                break;
            case R.id.edtxt_DestinationID:
                flagLocation = 0;
                placeAutoComplete();
                break;
        }
    }

    private void updateCurrUserInSP() {
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(currUser);
        prefEditor.putString("currUser", json);
        prefEditor.commit();
    }

    //add the package key that added to the current user list of keys packages
    private void addPackageToCurrentUser() {
        currUser.setMyPackages(lastPackageKey, userKey);
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

    private void cleanEdtTxts() {
        edtxt_PackageId.setText("");
        edtxt_Weight.setText("");
        edtxt_Size.setText("");
        edtxt_Location.setText("");
        edtxt_Destination.setText("");
    }

    public void placeAutoComplete() {
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
                Point p = (Point) feature.geometry();
                new_package.setGeoLocation(p.coordinates().toString());// set delivery location to package
                new_package.setLocation(locationToGeo);
                edtxt_Location.setText(feature.text());
            } else {
                // destination case
                locationToGeo = feature.text();
                Point p = (Point) feature.geometry();
                new_package.setGeoDestination(p.coordinates().toString());// set delivery destination to package
                new_package.setDestination(locationToGeo);
                edtxt_Destination.setText(feature.text());
            }

        }
    }

    public void onDestroy() {
        super.onDestroy();
    }

    //handle device back button
    @Override
    public void onBackPressed() {
        startActivity(new Intent(InviteDeliveryActivity.this, MainActivity.class));
        finish();
    }
}
