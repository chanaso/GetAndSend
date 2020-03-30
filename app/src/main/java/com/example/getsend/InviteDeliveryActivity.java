package com.example.getsend;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.light.Position;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.ui.geocoder.GeocoderAutoCompleteView;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;


public class InviteDeliveryActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText edtxtWeight;
    private EditText edtxtSize;
    private EditText edtxtLocation;
    private EditText edtxtDestination;
    private Button btnEnter;
    private DatabaseReference reff;
    private Package new_package;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private CarmenFeature home;
    private CarmenFeature work;

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
        reff = FirebaseDatabase.getInstance().getReference().child("Package");
        btnEnter.setOnClickListener(this);
        new_package = new Package();

    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_enter:
                //create a new object of package
                final String size = edtxtSize.getText().toString().trim();
                //final String location = edtxtLocation.getText().toString().trim();
                final String destination = edtxtDestination.getText().toString().trim();
                final double weight = Double.parseDouble(edtxtWeight.getText().toString());

                new_package.setSize(size);
                new_package.setDestination(destination);
                new_package.setWeight(weight);

                //push package to DB
                reff.push().setValue(new_package);
                Toast.makeText(InviteDeliveryActivity.this, "Package registered successfully!", Toast.LENGTH_LONG).show();
                break;
            default:
                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.access_token))
                        .placeOptions(PlaceOptions.builder()
                                .country("IL")
                                .backgroundColor(Color.parseColor("#EEEEEE"))
                                .limit(10)
                                .build(PlaceOptions.MODE_CARDS))
                        .build(InviteDeliveryActivity.this);
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            CarmenFeature feature = PlaceAutocomplete.getPlace(data);

        }
    }

}
