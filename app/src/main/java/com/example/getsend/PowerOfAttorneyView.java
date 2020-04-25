package com.example.getsend;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;

public class PowerOfAttorneyView extends AppCompatActivity {
    private DatabaseReference refUser;
    private SharedPreferences sharedPref;
    private ImageView imageView;
    String sign;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_of_attorney_view);
//        imageView = (ImageView) findViewById(R.id.imageView);
//        refUser = FirebaseDatabase.getInstance().getReference().child("User");
//        sharedPref = getSharedPreferences("userDetails", MODE_PRIVATE);
//        refUser.child("-M5gp8TxJG8aQ0WZvRRg").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//                sign= (String) snapshot.getValue();  //prints "Do you have data? You'll love Firebase."
//            }
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//        });
//        byte[] decodedString = Base64.decode(sign);
//        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
////        byte[] decodedString = Base64.decode(person_object.getPhoto(),Base64.NO_WRAP);
//        InputStream inputStream  = new ByteArrayInputStream(decodedString);
//        Bitmap bitmap  = BitmapFactory.decodeStream(inputStream);
//        imageView.setImageBitmap(bitmap);
    }
}
