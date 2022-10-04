package com.example.farmmd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class Result extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Intent i = getIntent();
        String clas = i.getStringExtra("class");
        String hum = i.getStringExtra("hum");
        String lat = i.getStringExtra("lat");
        if(lat.length()>6)
            lat = lat.substring(0,6);
        String lang = i.getStringExtra("lang");
        if(lang.length()>6)
            lang = lang.substring(0,6);
        String pres = i.getStringExtra("pres");
        String temp = i.getStringExtra("temp");

        TextView clas_t = findViewById(R.id.res);
        TextView hum_t = findViewById(R.id.hum);
        TextView temp_t = findViewById(R.id.temp);
        TextView pres_t = findViewById(R.id.pres);
        TextView cord_t = findViewById(R.id.co_ord);

        clas_t.setText(clas);
        hum_t.setText("Humidity\n"+hum);
        pres_t.setText("Pressure\n"+pres+" bar");
        temp_t.setText("Temperature\n"+temp+" K");
        cord_t.setText(lat+" N "+"   "+lang+" W");
    }
}
