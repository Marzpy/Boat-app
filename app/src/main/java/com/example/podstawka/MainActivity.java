package com.example.podstawka;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.InputFilter;
import android.view.View;
import android.os.Handler;
import android.view.MotionEvent;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import kotlin.jvm.internal.Intrinsics;

public class MainActivity extends AppCompatActivity {

      int speedV,course=1;
     private Button btnPlus,btnMin,btnSetcourse,btnspeed,btnhelp,btnstart;
     private TextView CourseText,SpeedText;
     private EditText editcourse,editSpeed;
    private TextView AddressText;
    //private Button LocationButton;
    private Button launchBTN;
    private LocationRequest locationRequest;
    static double latitude;
    static double longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //buttons


        btnPlus = findViewById(R.id.dimplus);
        btnMin=findViewById(R.id.dimminus);
        btnspeed=findViewById(R.id.speed);
        btnSetcourse=findViewById(R.id.setcourse);
        btnhelp=findViewById(R.id.stop);
        btnstart=findViewById(R.id.start);
        //textview
        launchBTN=findViewById(R.id.launch);
        CourseText=findViewById(R.id.CourseAngle);
        SpeedText=findViewById(R.id.textSpeed);

        //edittext
        editcourse=findViewById(R.id.editcourse);
        editSpeed= findViewById(R.id.editvelocity);


        editcourse.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "179")});

       // CourseText.setText(course);

        btnPlus.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           course++;
                                         String course1=Integer.toString(course);

                                           if (course==180) btnPlus.setEnabled(false);
                                           if (course>0) btnMin.setEnabled(true);
                                           CourseText.setText(course1+" °");
                                       }
        });

        btnMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                course--;
                String course1=Integer.toString(course);
                if(course<180) btnPlus.setEnabled(true);
                if (course==0) btnMin.setEnabled(false);
                CourseText.setText(course1+" °");


            }
        });


        btnSetcourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String course1=editcourse.getText().toString();
                     course=Integer.valueOf(course1);
                     CourseText.setText(course1+"°");}
        });

        btnspeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String speed1 = editSpeed.getText().toString();
                SpeedText.setText(" "+speed1+"mph");
            }
        });

        Handler handler = new Handler();

        btnhelp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        handler.postDelayed(run,2000);
                        break;

                    default:
                        handler.removeCallbacks(run);
                        break;
                }
                return true;
            }
        });

        btnstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //switchActivities();
            }
        });




        launchBTN.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent= new Intent (Intent.ACTION_VIEW);
            intent.setData(Uri.parse("geo:" + latitude + ","  +longitude ));
            Intent chooser = Intent.createChooser(intent,"Launch Maps");
            startActivity(chooser);
        }
    });
    AddressText = findViewById(R.id.addressText);
    //LocationButton = findViewById(R.id.locationButton);

    locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

       /* LocationButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            getCurrentLocation();
        }
    });*/


}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){

                if (isGPSEnabled()) {

                    getCurrentLocation();

                }else {

                    turnOnGPS();
                }
            }
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {

                getCurrentLocation();
            }
        }
    }

    private void getCurrentLocation() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                if (isGPSEnabled()) {

                    LocationServices.getFusedLocationProviderClient(MainActivity.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);

                                    LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                            .removeLocationUpdates(this);

                                    if (locationResult != null && locationResult.getLocations().size() >0){

                                        int index = locationResult.getLocations().size() - 1;
                                        //double latitude = locationResult.getLocations().get(index).getLatitude();
                                        //double longitude = locationResult.getLocations().get(index).getLongitude();
                                        //działające u góry
                                        latitude = locationResult.getLocations().get(index).getLatitude();
                                        longitude = locationResult.getLocations().get(index).getLongitude();
                                        //Long=longitude;
                                        AddressText.setText("Latitude: "+ latitude + "\n" + "Longitude: "+ longitude);
                                    }
                                }
                            }, Looper.getMainLooper());

                } else {
                    turnOnGPS();
                }

            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    private void turnOnGPS() {



        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(MainActivity.this, "GPS is already tured on", Toast.LENGTH_SHORT).show();

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(MainActivity.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });

    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = null;
        boolean isEnabled = false;

        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;

    }





    Runnable run = new Runnable() {

        @Override
        public void run() {
            getCurrentLocation();
            Toast.makeText(getApplicationContext(),"Touched",Toast.LENGTH_SHORT).show();


        }
    };
}





