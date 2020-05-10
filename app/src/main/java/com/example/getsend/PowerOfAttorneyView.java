package com.example.getsend;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PowerOfAttorneyView extends AppCompatActivity {

    private final long ONE_MEGABYTE = 1024 * 1024;
    private SharedPreferences sharedPref;
    private ImageView imageView;
    private TextView power_of_attorney_content;
    private ImageButton btn_exit;
    private Package pack;
    private String userKey, packageOwnerKey, packageId, poa_content, todayString, packKey;
    private StorageReference signatureRef;
    private SimpleDateFormat dateFormat;
    private Date todayDate;
    private User currUser, user2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_of_attorney_view);
        btn_exit = (ImageButton) findViewById(R.id.btn_exit);
        imageView = (ImageView) findViewById(R.id.imageView);
        power_of_attorney_content = (TextView) findViewById(R.id.power_of_attorney_content);

        //get current user details
        // store from local memory the current user
        sharedPref = getSharedPreferences("userDetails", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPref.getString("currUser", "");
        currUser = gson.fromJson(json, User.class);
        userKey = sharedPref.getString("userKey", "");
        json = sharedPref.getString("user2", "");
        user2 = gson.fromJson(json, User.class);

        //get current package from previous activity
        Bundle mBundle = getIntent().getExtras();
        if (mBundle != null) {
            String packStr = mBundle.getString("package");
            packKey = mBundle.getString("packageKey");
            getIntent().removeExtra("showMessage");
            // convert json to Package object
            Gson gson_2 = new Gson();
            pack = gson_2.fromJson(packStr , Package.class);
        }

        //get owner details
        packageOwnerKey = pack.getPackageOwnerId();
        packageId = pack.getPackageId();


        todayDate = Calendar.getInstance().getTime();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        todayString = dateFormat.format(todayDate);

        poa_content = getString(R.string.poa_content_1) + " " + currUser.getName() + " \n" + getString(R.string.poa_content_2) + " " + currUser.getId() + "\n"+ getString(R.string.poa_content_3) + " " + pack.getPackageId() + "\n"+ getString(R.string.poa_content_4)+ " " + user2.getName() + "\n"+getString(R.string.poa_content_5) + " " + user2.getId() + "\n"+getString(R.string.poa_content_6) + " " +todayString+"\n"+getString(R.string.poa_content_7);
        power_of_attorney_content.setText(poa_content);

        signatureRef = FirebaseStorage.getInstance().getReference("Signatures/"+ packKey + ".JPEG");
        signatureRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        DisplayMetrics dm = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(dm);
                        imageView.setImageBitmap(bm);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Toast.makeText(PowerOfAttorneyView.this,R.string.error_message , Toast.LENGTH_SHORT);
            }
        });
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
