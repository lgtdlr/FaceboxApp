package com.example.facebox;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
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

public class IdentifyActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://192.168.102.158:8080/facebox/check";
    private static final int PICK_IMAGE = 1;

    ImageView selectedImage;
    TextView requestResult, infoText;
    ProgressDialog p;

    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identify);

        selectedImage = (ImageView) findViewById(R.id.selectedImage);
        infoText = (TextView)findViewById(R.id.infoText);
        requestResult = findViewById(R.id.requestResult);
    }

    public void onUpload(View view) {
        selectImage();
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
            selectedImage.setImageURI(fullPhotoUri);

            File file = new File(fullPhotoPath);
            String fileName = file.getName();

            PostParameters params = new PostParameters(BASE_URL, file);
            AsyncTaskExample asyncTask = new AsyncTaskExample();
            if(asyncTask.execute(params).equals("Success")){
                infoText.setText("Let's go");
            }
        }
    }

    private class PostParameters {
        String string;
        File file;

        PostParameters(String string, File file) {
            this.file = file;
            this.string = string;
        }
    }

    private class AsyncTaskExample extends AsyncTask<PostParameters, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(IdentifyActivity.this);
            p.setMessage("Please wait... Downloading");
            p.setIndeterminate(false);
            p.setCancelable(false);
            p.show();
        }
        @Override
        protected String doInBackground(PostParameters... params) {
            try {
                Bitmap bitmap = ((BitmapDrawable) selectedImage.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageInByte = baos.toByteArray();
                //error here...
                RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("file", params[0].file.getName(), RequestBody.create(MediaType.parse("image/*jpg"), imageInByte))
                        .build();
                Request request = new Request.Builder()
                        .url(params[0].string)
                        .post(requestBody)
                        .addHeader("Accept", "application/json; charset=utf-8")
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    return response.body().string();
                }

            } catch (Exception ex) {
                // Handle the error
                ex.printStackTrace();
                return "Failure";
            }
        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);
            if(selectedImage !=null) {
                p.hide();
                selectedImage.isOpaque();
                //Parse JSONObject here
                Log.i("TAG_1", string);

                try {
                    JSONObject mainObject = new JSONObject(string);
                    if(mainObject.getBoolean("success")) {
                        JSONArray faces = mainObject.getJSONArray("faces");
                        infoText.setText("Name: " + faces.getJSONObject(0).getString("name") + ", Confidence: " + faces.getJSONObject(0).getDouble("confidence"));
                    }
                } catch (Exception e) { }

            }else {
                p.show();
            }
        }
    }
}