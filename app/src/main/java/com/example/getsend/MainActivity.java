package com.example.getsend;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
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
import com.squareup.picasso.Picasso;

import java.util.List;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, PermissionsListener, NavigationView.OnNavigationItemSelectedListener {
    private final int OWNER = 1, DELIVERYMAN = 0;
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private MapView mapView;
    private User currUser;
    private DrawerLayout drawer;
    private String userKey;
    private SharedPreferences sharedPref;
    private Button btnJoin, btnInvite;
    private DatabaseReference refUser;
    private TextView txt_contactUs, txt_dialog, txt_imgNote;
    private ImageView imageView;
    private StorageReference imagesRef;
    final long ONE_MEGABYTE = 1024 * 1024 * 5;
    private static int SELECT_PICTURE = 1;
    private Dialog dialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token));

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        btnInvite = (Button) findViewById(R.id.btnInvite);
        btnJoin = (Button) findViewById(R.id.btnJoin);
        txt_contactUs = findViewById(R.id.txt_contactUsID);

        //navBar view
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                txt_imgNote.setVisibility(View.VISIBLE);
                txt_imgNote.setText(getString(R.string.click_here_to_update_your_profile_image));
                //make the textView update note disappear
                txt_imgNote.postDelayed(() -> txt_imgNote.setVisibility(View.INVISIBLE), 3000);
            }
        };

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        refUser = FirebaseDatabase.getInstance().getReference().child("User");

        // store from local memory the current user
        sharedPref = getSharedPreferences("userDetails", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPref.getString("currUser", "");
        currUser = gson.fromJson(json, User.class);
        userKey = sharedPref.getString("userKey", "");

        checkUserExist();

        //navBar view
        NavigationView nav_view = (NavigationView) findViewById(R.id.nav_view);//this is navigation view from my main xml where i call another xml file
        View header = nav_view.getHeaderView(0);//set View header to nav_view first element (i guess)
        TextView txt = (TextView) header.findViewById(R.id.UserNameID);//now assign textview imeNaloga to header.id since we made View header.
        txt_imgNote = header.findViewById(R.id.txt_updateImgNoteID);
        txt.setText(currUser.getName());// And now just set text to that textviewn

        imageView = header.findViewById(R.id.userImageID);

        nav_view.setNavigationItemSelectedListener(this);
        nav_view.bringToFront();

        btnInvite.setOnClickListener(this);
        btnJoin.setOnClickListener(this);
        txt_contactUs.setOnClickListener(this);
        imageView.setOnClickListener(this);
        showImg();

    }

    private void checkUserExist() {
        //  check if the user exist in the db
        refUser.child(userKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    signOut();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, R.string.error_message, Toast.LENGTH_LONG).show();
            }
        });
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

    @SuppressWarnings({"MissingPermission"})
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
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_layout, (ViewGroup) findViewById(R.id.toast_root));

        TextView toastText = layout.findViewById(R.id.toast_text);
        ImageView toastImage = layout.findViewById(R.id.toast_image);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);

        toast.show();
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(style -> enableLocationComponent(style));
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
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

    //handle device back button
    @Override
    public void onBackPressed() {
        // add confirmation dialog
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_sentiment)
                .setTitle(R.string.exit_title)
                .setMessage(R.string.exit_message)
                .setPositiveButton("לצאת", (dialog, which) -> finish())
                .setNegativeButton("להישאר", null)
                .show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnInvite: {
                startActivity(new Intent(MainActivity.this, InviteDeliveryActivity.class));
                finish();
                break;
            }
            case R.id.btnJoin: {
                startActivity(new Intent(MainActivity.this, JoinAsDeliverymanActivity.class));
                finish();
                break;
            }
            case R.id.txt_contactUsID: {
                txt_contactUs.setMovementMethod(LinkMovementMethod.getInstance());
                break;
            }
            case R.id.userImageID: {
                dialog = new Dialog(this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_image);
                dialog.show();

                Button bt_yes = dialog.findViewById(R.id.btnYesID);
                Button bt_no = dialog.findViewById(R.id.btnNoID);

                bt_yes.setOnClickListener(v12 -> {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
                    txt_dialog = dialog.findViewById(R.id.txt_dialogImgID);
                    txt_dialog.setText(getString(R.string.loading_image));
                    bt_yes.setVisibility(View.GONE);
                    bt_no.setVisibility(View.GONE);
                });

                bt_no.setOnClickListener(v1 -> dialog.dismiss());
                break;
            }
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {

                //Get ImageURi and load with help of picasso
                Uri selectedImageURI = data.getData();
                Picasso.with(MainActivity.this).load(data.getData()).noPlaceholder().centerCrop().fit()
                        .into((ImageView) findViewById(R.id.userImageID));

                imagesRef = FirebaseStorage.getInstance().getReference("Images/"+ userKey + ".jpg");

                // adding listeners on upload
                // or failure of image
                imagesRef.putFile(selectedImageURI).addOnSuccessListener(taskSnapshot -> {
                            // Image uploaded successfully
                    dialog.dismiss();
                    Toast.makeText(MainActivity.this, R.string.update_img_successfully, Toast.LENGTH_LONG).show();
                }).addOnFailureListener((OnFailureListener) e -> Toast.makeText(MainActivity.this, R.string.faild_to_upload_img, Toast.LENGTH_SHORT).show());
            }
        }
    }

    private void showImg() {

        imagesRef = FirebaseStorage.getInstance().getReference("Images/"+ userKey + ".jpg");
        imagesRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            imageView.setImageBitmap(bm);
        }).addOnFailureListener(exception -> Toast.makeText(MainActivity.this, R.string.error_message , Toast.LENGTH_SHORT));
    }

    // update user type to be 1- user as a delivery getter
    private void userTypeUpdate(int type) {
        currUser.getRefUser().child(userKey).child("type").setValue(type);
        currUser.setType(type);
    }

    private void updateCurrUserInSP() {
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(currUser);
        prefEditor.putString("currUser", json);
        prefEditor.commit();
    }

    // navbar selection list and move to the selected option/activity
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_profile:{

                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                break;
            }
            case R.id.nav_myPackeges:{
                if(currUser.getType() != OWNER){
                    userTypeUpdate(OWNER);
                    updateCurrUserInSP();
                }
                startActivity(new Intent(MainActivity.this, NavbarPackagesActivity.class));
                break;
            }
            case R.id.nav_packegesToDeliver: {
                if (currUser.getType() != DELIVERYMAN) {
                    userTypeUpdate(DELIVERYMAN);
                    updateCurrUserInSP();
                }
                startActivity(new Intent(MainActivity.this, NavbarPackagesActivity.class));
                break;
            }
            case R.id.nav_sign_out:{
                signOut();
                break;
            }
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signOut() {
        //delete the existing userName
        sharedPref.edit().clear().commit();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }
}
