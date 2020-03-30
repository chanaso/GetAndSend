package com.example.getsend;
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
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.light.Position;
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
    private AppCompatAutoCompleteTextView autoTextView;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
//    private MapView mapView;
//    private MapboxMap mapboxMap;
    private CarmenFeature home;
    private CarmenFeature work;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_delivery);
        Mapbox.getInstance(this, getString(R.string.access_token));
        edtxtWeight = (EditText) findViewById(R.id.edtxt_weight);
        edtxtSize = (EditText) findViewById(R.id.edtxt_size);
//        edtxtLocation =(EditText) findViewById(R.id.edtxt_location);
//        autoTextView = (AppCompatAutoCompleteTextView) findViewById(R.id.autoTextViewTextView);
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>
//                (this, android.R.layout.select_dialog_item, );
//        autoTextView.setThreshold(1); //will start working from first character
//        autoTextView.setAdapter(adapter);
        // Set up autocomplete widget
//        GeocoderAutoCompleteView autocomplete = (GeocoderAutoCompleteView) findViewById(R.id.autoTextView);
//        autocomplete.setAccessToken(getString(R.string.access_token));
//        autocomplete.setType(GeocodingCriteria.TYPE_POI);
//        autocomplete.setOnFeatureListener(new GeocoderAutoCompleteView.OnFeatureListener() {
//            @Override
//            public void OnFeatureClick(GeocodingFeature feature) {
//                Position position = feature.asPosition();
//                updateMap(position.getLatitude(), position.getLongitude());
//            }
//        });

        edtxtDestination = (EditText) findViewById(R.id.edtxt_destination);
        btnEnter = (Button) findViewById(R.id.btn_enter);
        //create a new DB table of package if not exist
        reff = FirebaseDatabase.getInstance().getReference().child("Package");
        btnEnter.setOnClickListener(this);

    }
    private void initSearchFab() {
        findViewById(R.id.autoTextView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.access_token))
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#EEEEEE"))
                                .limit(10)
//                                .addInjectedFeature(home)
//                                .addInjectedFeature(work)
                                .build(PlaceOptions.MODE_CARDS))
                        .build(InviteDeliveryActivity.this);
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
            }
        });
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
