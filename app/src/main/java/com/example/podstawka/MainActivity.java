package com.example.podstawka;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.widget.ArrayAdapter;
import android.widget.CompoundButton;

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
import android.text.InputFilter;
import android.view.View;
import android.os.Handler;
import android.view.MotionEvent;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
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


import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.telephony.SmsManager;
import android.widget.ToggleButton;


import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD_ITEM =1 ;
    int speedV=5,course=1;
    private Button btnPlus,btnMin,btnSetcourse,btnspeed,btnhelp;
    private Button speedPlus,speedMin;
    private TextView CourseText,SpeedText;
    private EditText editcourse,editSpeed;
    private Switch switch1,switch2;
    private TextView AddressText;
    private ToggleButton tugle1,tugle2;
    //private Button LocationButton;
    private Button launchBTN;
    private LocationRequest locationRequest;
    static double latitude;
    static double longitude;
    static int frame=0;
    MqttAndroidClient client;
    TextView subText;
    List<String> array=new ArrayList<String>();
    List<String> array2=new ArrayList<String>();

    private static ArrayList<String> items = new ArrayList<>();


    private Button editListButton;
    private Button sendSmsButton;
    private ListView listView;

    private ArrayAdapter<String> adapter;
    private ConstraintLayout containerRL;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // containerRL = findViewById(R.id.constrainlayout);
        //  containerRL.setBackground(getResources().getDrawable(R.drawable.background));

        ToggleButton tugle1=(ToggleButton)findViewById(R.id.toggleButton);
        ToggleButton tugle2=(ToggleButton)findViewById(R.id.toggleButton2);
        switch1=findViewById(R.id.switch1);
        switch2=findViewById(R.id.switch2);
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), "tcp://broker.mqttdashboard.com:1883",clientId);

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS,Manifest.permission.READ_PHONE_STATE}, PackageManager.PERMISSION_GRANTED);



        editListButton = findViewById(R.id.editListButton);


        //buttons

        speedMin=findViewById(R.id.minspeed);
        speedPlus=findViewById(R.id.plusspeed);

        btnPlus = findViewById(R.id.dimplus);
        btnMin=findViewById(R.id.dimminus);
        btnspeed=findViewById(R.id.speed);
        btnSetcourse=findViewById(R.id.setcourse);
        btnhelp=findViewById(R.id.stop);

        //textview
        launchBTN=findViewById(R.id.launch);
        CourseText=findViewById(R.id.CourseAngle);
        SpeedText=findViewById(R.id.textSpeed);

        //edittext
        editcourse=findViewById(R.id.editcourse);
        editSpeed= findViewById(R.id.editvelocity);


        editcourse.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "359")});

        offset();
         tugle1.setEnabled(false);
        tugle2.setEnabled(false);
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    seton();
                    tugle2.setEnabled(true);
                }
                else {
                    offset();
                    tugle2.setEnabled(false);
                }
            }
        });
        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    conn();
                       tugle1.setEnabled(true);
                }
                else {
                     tugle1.setChecked(false);
                    //disconn();
                    Toast.makeText(MainActivity.this,"disconnected",Toast.LENGTH_LONG).show();


                    tugle1.setEnabled(false);
                }
            }
        });
        // CourseText.setText(course);

        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                course++;
                String course1=Integer.toString(course);

                if (course==360) btnPlus.setEnabled(false);
                if (course>0) btnMin.setEnabled(true);
                CourseText.setText(course1+" °");
                array.set(0,""+course+","+speedV);
            }
        });

        btnMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                course--;
                String course1=Integer.toString(course);
                if(course<360) btnPlus.setEnabled(true);
                if (course==0) btnMin.setEnabled(false);
                CourseText.setText(course1+" °");
                array.set(0,""+course+","+speedV);

            }
        });

       speedMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speedV--;
                String speed=Integer.toString(speedV);
                if(speedV<20) speedPlus.setEnabled(true);
                if (speedV==0) speedMin.setEnabled(false);
                SpeedText.setText(speed+" kmh");
                array.set(0,""+course+","+speedV);

            }
        });

        speedPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speedV++;
                String speed=Integer.toString(speedV);
                if(speedV==20) speedPlus.setEnabled(false);
                if (speedV>0) speedMin.setEnabled(true);
                SpeedText.setText(speed+" kmh");
                array.set(0,""+course+","+speedV);

            }
        });


        array.add(0,course+","+speedV);

        btnSetcourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String course1=editcourse.getText().toString();
                course=Integer.valueOf(course1);
                CourseText.setText(course1+"°");
                array.set(0,""+course+","+speedV);
                //  array.set(course,""+speedV);
            }

        });

        btnspeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String speed1 = editSpeed.getText().toString();
                speedV=Integer.valueOf(speed1);
                SpeedText.setText(" "+speed1+"kmh");
                array.set(0,""+course+","+speedV);
                //array.set(0+speedV,"");
            }
        });

        Handler handler = new Handler();

        btnhelp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                getCurrentLocation();
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




// LaunchMap
        launchBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentLocation();
                if (latitude!=0){
                Intent intent= new Intent (Intent.ACTION_VIEW);
                intent.setData(Uri.parse("geo:" + latitude + ","  +longitude ));
                Intent chooser = Intent.createChooser(intent,"Launch Maps");
                startActivity(chooser);
            }}
        });


        tugle1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    new Timer().scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            List<String> array3=new ArrayList<String>();
                            if (array3==array)
                            {



                            }
                            else{
                                published();

                            }


                        }
                    }, 0, 5000);


                } else {

                }
            }
        });
        tugle2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    setonCourse();
               // switch2.setEnabled(true);
            }else {
                    setoffCourse();
               // switch2.setEnabled(false);
                }
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

        if (getIntent().hasExtra("items")) {
            items = (ArrayList<String>) getIntent().getSerializableExtra("items");
        } else {
            items = new ArrayList<>();
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        // listView.setAdapter(adapter);
        if (items != null && !items.isEmpty()) {
            if (items == null || items.isEmpty()) {
                Toast.makeText(this, "Lista numerów jest pusta", Toast.LENGTH_SHORT).show();
                return;
            }}
        editListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ListActivity.class);
                // intent.putExtra("items", items);
                startActivityForResult(intent, REQUEST_CODE_ADD_ITEM);
            }
        });

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
        if (requestCode == REQUEST_CODE_ADD_ITEM && resultCode == RESULT_OK) {
            items = data.getStringArrayListExtra("items");
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
                                       // AddressText.setText("Latitude: "+ latitude + "\n" + "Longitude: "+ longitude);
                                        //AddressText.setText("Latitude: "+ latitude + "\n" + "Longitude: "+ longitude);
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

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(MainActivity.this,"connected!!",Toast.LENGTH_LONG).show();
                    setSubscription();

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MainActivity.this,"connection failed!!",Toast.LENGTH_LONG).show();
                }
            });
        } catch (
                MqttException e) {
            e.printStackTrace();
        }

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
               // array3.set(new String(message.getPayload()));
                //subText.setText(new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

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


    public void offset()
    {
        editcourse.setEnabled(false);
        editSpeed.setEnabled(false);
        switch2.setEnabled(false);
        launchBTN.setEnabled(false);
        btnPlus.setEnabled(false);
        btnMin.setEnabled(false);
        btnspeed.setEnabled(false);
        btnhelp.setEnabled(false);
        btnSetcourse.setEnabled(false);
       // tugle1.setEnabled(false);
        //tugle2.setEnabled(false);

    }


    public void seton()
    {
        //editcourse.setEnabled(true);
        //editSpeed.setEnabled(true);
      //  switch2.setEnabled(true);
        launchBTN.setEnabled(true);
        switch2.setEnabled(true);
        //btnPlus.setEnabled(true);
        //btnMin.setEnabled(true);
        //btnspeed.setEnabled(true);
        btnhelp.setEnabled(true);
        //btnSetcourse.setEnabled(true);
       // tugle2.setEnabled(true);
    }



    public void setoffCourse()
    {
        editcourse.setEnabled(false);
        editSpeed.setEnabled(false);


        btnPlus.setEnabled(false);
        btnMin.setEnabled(false);
        btnspeed.setEnabled(false);
        // btnhelp.setEnabled(false);
        btnSetcourse.setEnabled(false);

    }

    public void setonCourse()
    {
        editcourse.setEnabled(true);
        editSpeed.setEnabled(true);

        launchBTN.setEnabled(true);
        btnPlus.setEnabled(true);
        btnMin.setEnabled(true);
        btnspeed.setEnabled(true);
        //btnhelp.setEnabled(true);
        btnSetcourse.setEnabled(true);

    }
    public void published(){


        String topic = "event";
        String message = ""+array;
        try {
            client.publish(topic, message.getBytes(),0,false);
           // Toast.makeText(this,"Published Message",Toast.LENGTH_SHORT).show();
        } catch ( MqttException e) {
            e.printStackTrace();
        }
    }

    private void setSubscription(){

        try{

            client.subscribe("event",0);


        }catch (MqttException e){
            e.printStackTrace();
        }
    }







    public void conn(){

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(MainActivity.this,"connected!!",Toast.LENGTH_LONG).show();
                    setSubscription();

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MainActivity.this,"connection failed!!",Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    public void disconn(){

        try {
            IMqttToken token = client.disconnect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(MainActivity.this,"Disconnected!!",Toast.LENGTH_LONG).show();


                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MainActivity.this,"Could not diconnect!!",Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void sendSms() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Użytkownik nie ma uprawnień", Toast.LENGTH_SHORT).show();
        } else{
            if (items != null && !items.isEmpty()) {
                SmsManager smsManager = SmsManager.getDefault();
                for (String number : items) {
                    smsManager.sendTextMessage(number, null, "NEED HELP "+latitude + ","  +longitude, null, null);
                }
                Toast.makeText(this, "Wiadomość została wysłana", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Lista numerów jest pusta", Toast.LENGTH_SHORT).show();
            }}
    }




    Runnable run = new Runnable() {

        @Override
        public void run() {
            getCurrentLocation();
           sendSms();

        }
    };
}





