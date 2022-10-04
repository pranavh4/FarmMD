package com.example.farmmd;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class Result_No_Net extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result__no__net);
        TextView res = findViewById(R.id.res);
        res.setText(this.getIntent().getStringExtra("class"));
    }
}
