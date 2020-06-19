package com.example.facebox;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class TeachActivity extends AppCompatActivity {

    Button button;
    TextView postResponseText;
    ImageView imageSelected;
    EditText urlEditText;
    EditText nameEditText;

    private static final int PICK_IMAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teach);
        AndroidNetworking.initialize(getApplicationContext());
        urlEditText = (EditText)findViewById(R.id.urlEditText);
        nameEditText = (EditText)findViewById(R.id.nameEditText);
        button = (Button)findViewById(R.id.button);
        postResponseText = (TextView)findViewById(R.id.postResponseText);
        imageSelected = (ImageView) findViewById(R.id.imageSelected);
    }

    public void onUploadClick(View view) {
        selectImage();
    }

    public void onPostClick(View view) {

                //URL POST (will not work with MEC atm since it requires access to internet)
                AndroidNetworking.post("http://192.168.0.12:8080/facebox/teach")
                        .addBodyParameter("url", urlEditText.getText().toString())
                        .addBodyParameter("name", nameEditText.getText().toString())
                        .addHeaders("Accept","application/json; charset=utf-8")
                        .setContentType("application/json; charset=utf-8")
//                        .addBodyParameter("id", "john.jpg")
//                        .addHeaders("name", "John Lennon")
//                        .addHeaders("id", "john.jpg")
//                        .addQueryParameter("name", "Jonn Lennon")
//                        .addQueryParameter("id", "john.jpg")
//                        .addBodyParameter("file", "@/storage/emulated/0/Download/john.jpg")
//                        .addFileBody(john)
                        .setPriority(Priority.LOW)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // do anything with response
                                try {
                                    if (response.getBoolean("success")){
                                        postResponseText.setText("Success");
                                    }
                                    else {
                                        postResponseText.setText("Failed");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    postResponseText.setText((e.getMessage()));
                                }
                            }
                            @Override
                            public void onError(ANError error) {
                                // handle error
                                postResponseText.setText(error.getMessage());
                                postResponseText.setText(error.getErrorBody());
                                //postResponseText.setText(error.getErrorDetail());

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

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            Uri fullPhotoUri = data.getData();
            String fullPhotoString = data.getDataString();
            imageSelected.setImageURI(fullPhotoUri);
            if (fullPhotoString == null){
                return;
            }
            File john = new File(fullPhotoString); //Same as fullPhotoUri.toString()
            AndroidNetworking.post("http://192.168.0.12:8080/facebox/teach?name={name}")
                    .addHeaders("Accept","application/json; charset=utf-8")
                    //.setContentType("multipart/form-data; charset=utf-8")
                    .addQueryParameter("name", nameEditText.getText().toString())
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