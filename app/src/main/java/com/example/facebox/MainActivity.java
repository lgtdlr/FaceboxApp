package com.example.facebox;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.androidnetworking.interfaces.DownloadProgressListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    Button button;
    TextView postResponseText;
    ImageView imageSelected;
    private static final int PICK_IMAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AndroidNetworking.initialize(getApplicationContext());

        requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        button = (Button)findViewById(R.id.button);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                //File POST
                selectImage();

//                //URL POST (will not work with MEC atm since it requires access to internet)
//                AndroidNetworking.post("http://192.168.0.12:8080/facebox/teach")
//                        .addBodyParameter("url", "https://machinebox.io/samples/faces/john.jpg")
//                        .addHeaders("Accept","application/json; charset=utf-8")
//                        .setContentType("application/json; charset=utf-8")
//                        .addBodyParameter("name", "John Lennon")
//                        .addBodyParameter("id", "john.jpg")
////                        .addHeaders("name", "John Lennon")
////                        .addHeaders("id", "john.jpg")
////                        .addQueryParameter("name", "Jonn Lennon")
////                        .addQueryParameter("id", "john.jpg")
////                        .addBodyParameter("file", "@/storage/emulated/0/Download/john.jpg")
////                        .addFileBody(john)
//                        .setPriority(Priority.LOW)
//                        .build()
//                        .getAsJSONObject(new JSONObjectRequestListener() {
//                            @Override
//                            public void onResponse(JSONObject response) {
//                                // do anything with response
//                                try {
//                                    if (response.getBoolean("success")){
//                                        button.setText("Success");
//                                    }
//                                    else {
//                                        postResponseText.setText("Failed");
//                                    }
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                    postResponseText.setText((e.getMessage()));
//                                    button.setText("No success");
////                                    button.setText(e.getMessage());
//                                    //postResponseText.setText(e.getLocalizedMessage());
//                                }
//                            }
//                            @Override
//                            public void onError(ANError error) {
//                                // handle error
////                                button.setText(error.getErrorDetail());
//                                postResponseText.setText(error.getMessage());
//                                postResponseText.setText(error.getErrorBody());
//                                //postResponseText.setText(error.getErrorDetail());
//                                button.setText("Failed");
//
//                                //button.setText("No response");
//                            }
//                        });
            }
        });
    }

    public void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, PICK_IMAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        button = (Button)findViewById(R.id.button);
        postResponseText = (TextView)findViewById(R.id.postResponseText);
        imageSelected = (ImageView) findViewById(R.id.imageSelected);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            Uri fullPhotoUri = data.getData();
            String fullPhotoString = data.getDataString();
            imageSelected.setImageURI(fullPhotoUri);
            if (fullPhotoString == null){
                return;
            }
            File john = new File(fullPhotoString); //Same as fullPhotoUri.toString()
            AndroidNetworking.post("http://192.168.102.158:8080/facebox/teach?name={name}&id={id}")
                    .addHeaders("Accept","application/json; charset=utf-8")
                    //.setContentType("multipart/form-data; charset=utf-8")
                    .addFileBody(john)
                    .setPriority(Priority.LOW)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response.getBoolean("success")){
                                    button.setText("Success");
                                }
                                else {
                                    postResponseText.setText("Failed");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                postResponseText.setText((e.getMessage()));
                                button.setText("No success");
                            }
                        }
                        @Override
                        public void onError(ANError error) {
                            // handle error
                            postResponseText.setText(error.getMessage());

                            button.setText("Failed");

                        }
                    });
        }
    }


}