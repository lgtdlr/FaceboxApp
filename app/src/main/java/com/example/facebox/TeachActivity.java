package com.example.facebox;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
    public static final String TAG = "LOG_TAG";

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
        AndroidNetworking.post("http://192.168.102.158:8080/facebox/teach")
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
                                Log.i(TAG, response.toString());
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
            final String fullPhotoPath = getPath(this, fullPhotoUri);
            imageSelected.setImageURI(fullPhotoUri);
            if (fullPhotoPath == null){
                return;
            }
            File john = new File(fullPhotoPath); //Same as fullPhotoUri.toString()
            AndroidNetworking.post("http://192.168.102.158:8080/facebox/teach?name={name}")
                    .addHeaders("Accept","application/json; charset=utf-8")
                    //.setContentType("multipart/form-data; charset=utf-8")
                    .addQueryParameter("name", nameEditText.getText().toString())
                    .addFileBody(john)
                    .setPriority(Priority.LOW)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i(TAG, response.toString());
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
                                button.setText("No success");
                            }
                        }
                        @Override
                        public void onError(ANError error) {
                            // handle error
                            //postResponseText.setText(error.getMessage());
                            postResponseText.setText(error.getErrorBody());

                            button.setText("Failed");

                        }
                    });
        }
    }

    public static String getPath(final Context context, final Uri uri)
    {

        //check here to KITKAT or new version
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://<span id=\"IL_AD1\" class=\"IL_AD\">downloads</span>/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }


    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }


    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}