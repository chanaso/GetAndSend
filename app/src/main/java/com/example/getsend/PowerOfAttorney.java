package com.example.getsend;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PowerOfAttorney extends AppCompatActivity {

    private TextView power_of_attorney_content;
    private Button btn_get_sign, mClear, mGetSign, mCancel, btn_confirm;
    private File file;
    private Dialog dialog;
    private LinearLayout mContent;
    private View view;
    private signature mSignature;
    private Bitmap bitmap;
    private Package pack;
    private StorageReference signaturesRef;
    private String userKey, StoredPath, poa_content, todayString, packKey;
    private SharedPreferences sharedPref;
    private User currUser, user2;
    private Uri downloadUri;
    private ImageView imageView;
    private SimpleDateFormat dateFormat;
    private Date todayDate;
    private DatabaseReference refPackage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setContentView(R.layout.activity_power_of_attorney);
        // Create a storage reference
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(bitmap);
        imageView.setVisibility(View.VISIBLE);
        signaturesRef = FirebaseStorage.getInstance().getReference("Signatures");
        // Setting ToolBar as ActionBar
        power_of_attorney_content = (TextView) findViewById(R.id.power_of_attorney_content);

        // Button to open signature panel
        btn_get_sign = (Button) findViewById(R.id.signature);
        btn_confirm = (Button) findViewById(R.id.confirm);

        // Dialog Function
        dialog = new Dialog(PowerOfAttorney.this);
        // Removing the features of Normal Dialogs
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_signature);
        dialog.setCancelable(true);

        refPackage = FirebaseDatabase.getInstance().getReference().child("Package");

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

        // get today date
        StoredPath = userKey + ".JPEG";
        todayDate = Calendar.getInstance().getTime();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        todayString = dateFormat.format(todayDate);
        poa_content = getString(R.string.poa_content_1) + " " + user2.getName() + " \n" + getString(R.string.poa_content_2) + " " + user2.getId() + "\n"+ getString(R.string.poa_content_3) + " " +pack.getPackageId() + "\n"+ getString(R.string.poa_content_4)+ " " + currUser.getName() + "\n"+getString(R.string.poa_content_5) + " " +currUser.getId() + "\n"+getString(R.string.poa_content_6) + " " +todayString+"\n"+getString(R.string.poa_content_7);
        power_of_attorney_content.setText(poa_content);

        btn_get_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Function call for Digital Signature
                dialog_action();
            }
        });
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refPackage.child(packKey).child("status").setValue("On the way...");
                //ToDo
                //sms to deliveryman that the delivery approve
                finish();
            }
        });
    }

    // Function for Digital Signature
    public void dialog_action() {

        mContent = (LinearLayout) dialog.findViewById(R.id.linearLayout);
        mSignature = new signature(getApplicationContext(), null);
        mSignature.setBackgroundColor(Color.WHITE);
        // Dynamically generating Layout through java code
        mContent.addView(mSignature, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mClear = (Button) dialog.findViewById(R.id.clear);
        mGetSign = (Button) dialog.findViewById(R.id.getsign);
        mGetSign.setEnabled(false);
        mCancel = (Button) dialog.findViewById(R.id.cancel);
        view = mContent;


        mClear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mSignature.clear();
                mGetSign.setEnabled(false);
            }
        });
        mGetSign.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                mSignature.save(view, StoredPath);
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), "Successfully Saved", Toast.LENGTH_SHORT).show();
                // Calling the same class
//                Picasso.with(PowerOfAttorney.this).load(downloadUri).into(imageView);
                imageView.setImageBitmap(bitmap);
                onRestart();
                onStart();
            }
        });
        mCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                // Calling the same class
                recreate();
            }
        });
        dialog.show();
    }
    public Bitmap getBitmapFromView(View view)
    {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    public Bitmap getBitmapFromView(View view,int defaultColor)
    {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(defaultColor);
        view.draw(canvas);
        return bitmap;
    }

    // signature class contains method to catch the signature view and upload to storage
    public class signature extends View {
        private static final float STROKE_WIDTH = 5f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        private Paint paint = new Paint();
        private Path path = new Path();

        private float lastTouchX;
        private float lastTouchY;
        private final RectF dirtyRect = new RectF();

        public signature(Context context, AttributeSet attrs) {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }

        @SuppressLint("WrongThread")
        public void save(View v, String StoredPath){
            bitmap = getBitmapFromView(v);
            Bitmap bitmapColored = getBitmapFromView(v,Color.WHITE);
            // create a storage path
            StorageReference currSignRef = signaturesRef.child(StoredPath);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmapColored.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            // upload to firebase storage
            UploadTask uploadTask = currSignRef.putBytes(data);
        }

        public void clear() {
            path.reset();
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();
            mGetSign.setEnabled(true);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:
                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;
                default:
                    debug("Ignored touch event: " + event.toString());
                    return false;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void debug(String string) {
            Log.v("log_tag", string);
        }

        private void expandDirtyRect(float historicalX, float historicalY) {
            if (historicalX < dirtyRect.left) {
                dirtyRect.left = historicalX;
            } else if (historicalX > dirtyRect.right) {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top) {
                dirtyRect.top = historicalY;
            } else if (historicalY > dirtyRect.bottom) {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY) {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }
    }
}
