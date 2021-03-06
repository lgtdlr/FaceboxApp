package com.example.facebox;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class TeachAltActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://192.168.102.158:8080/facebox/teach";
    private static final int PICK_IMAGE = 100;
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static Button button;
    private static TextView postResponseText;
    private static ImageView imageSelected;
    private static EditText urlEditText, nameEditText;
    private static String urlInput, nameInput;

    private static OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teach_alt);
        urlEditText = (EditText)findViewById(R.id.urlEditTextAlt);
        nameEditText = (EditText)findViewById(R.id.nameEditTextAlt);
        button = (Button)findViewById(R.id.buttonAlt);
        postResponseText = (TextView)findViewById(R.id.postResponseTextAlt);
        imageSelected = (ImageView) findViewById(R.id.imageSelectedAlt);

    }

    public void onUploadClick(View view) {
        nameInput = nameEditText.getText().toString();
        urlInput = urlEditText.getText().toString();
        if(nameInput.equals("")){
            postResponseText.setText("Make sure a name has been entered before selecting a file");
            return;
        }

        selectImage();
    }

    public void onPostClick(View view) throws IOException {
        nameInput = nameEditText.getText().toString();
        urlInput = urlEditText.getText().toString();

        postResponseText.setText(postUrl(BASE_URL, urlInput, nameInput));
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
            final String fullPhotoPath = ImageSelect.getPath(this, fullPhotoUri);
            imageSelected.setImageURI(fullPhotoUri);

            File file = new File(fullPhotoPath);

            if (uploadFile(BASE_URL, file)){
                postResponseText.setText("Successful");
            } else {
                return;
            }

        }
    }

    public Boolean uploadFile(String serverURL, File file) {
        final Intent teachIntent = new Intent(this, TeachAltActivity.class);

        try {

            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.getName(), RequestBody.create(file, MediaType.get("image/jpeg")))
                    .addFormDataPart("name", nameInput)
                    .build();

            Request request = new Request.Builder()
                    .url(serverURL)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(final Call call, final IOException e) {
                    // Handle the error
                    e.printStackTrace();
                    startActivity(teachIntent);
                    return;
                }

                @Override
                public void onResponse(final Call call, final Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        // Handle the error
                        startActivity(teachIntent);
                    }
                    Log.i("ASHWIN", response.toString());
                    // Upload successful
                }
            });

            return true;
        } catch (Exception ex) {
            // Handle the error
            ex.printStackTrace();
        }
        return false;
    }

    String postUrl(String serverUrl, String sourceUrl, String name) throws IOException {
        final Intent teachIntent = new Intent(this, TeachAltActivity.class);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("name", name)
                .addFormDataPart("url", sourceUrl)
                .build();

        Request request = new Request.Builder()
                .url(serverUrl)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(final Call call, final IOException e) {
                // Handle the error
                e.printStackTrace();
                startActivity(teachIntent);
                return;
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    // Handle the error
                    startActivity(teachIntent);
                }
                // Upload successful
            }
        });
        return "Success";
    }

}