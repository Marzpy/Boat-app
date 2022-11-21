package com.example.podstawka;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.view.View;
import android.os.Handler;
import android.view.MotionEvent;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import kotlin.jvm.internal.Intrinsics;

public class MainActivity extends AppCompatActivity {

      int speedV,course=1;
     private Button btnPlus,btnMin,btnSetcourse,btnspeed,btnhelp,btnstart;
     private TextView CourseText,SpeedText;
     private EditText editcourse,editSpeed;

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
                switchActivities();
            }
        });

    }
    Runnable run = new Runnable() {

        @Override
        public void run() {
            Toast.makeText(getApplicationContext(),"Touched",Toast.LENGTH_SHORT).show();


        }
    };

    private void switchActivities() {
        Intent switchActivityIntent = new Intent(this, LocationActivity.class);
        startActivity(switchActivityIntent);
    }



}





