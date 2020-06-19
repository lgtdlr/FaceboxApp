package com.example.facebox;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AndroidNetworking.initialize(getApplicationContext());


        requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }

    public void onTeachClick(View view) {
        Intent teachIntent = new Intent(this, TeachActivity.class);
        startActivity(teachIntent);
    }
}