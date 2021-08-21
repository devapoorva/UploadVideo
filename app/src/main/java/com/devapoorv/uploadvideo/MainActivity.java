package com.devapoorv.uploadvideo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int READ_REQUEST_CODE = 200;
    MultipartBody.Part part;
    ProgressBar progressBar;


    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK)
        {
            if( data != null) {
                Uri selectedUri = data.getData();
                FileUtils fileUtils = new FileUtils(MainActivity.this);
                String filePath = fileUtils.getPath(selectedUri);
                Toast.makeText(getApplicationContext(),filePath,Toast.LENGTH_SHORT).show();
                File file = new File(filePath);
                RequestBody image = RequestBody.create(file, MediaType.parse("multipart/form-data"));
                part = MultipartBody.Part.createFormData("video",file.getName(),image);
                uploadVideoToServer();
            }
            else {
                Toast.makeText(MainActivity.this,"Error",Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(MainActivity.this,"Error in select",Toast.LENGTH_SHORT).show();
        }
    }


    public boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = MainActivity.this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("video/*");
                startActivityForResult(intent, READ_REQUEST_CODE);
            } else {
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnUpload = findViewById(R.id.btn_upload);
        progressBar = findViewById(R.id.progress);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPermission())
                {
                    Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickIntent.setType("video/*");
                    startActivityForResult(pickIntent, READ_REQUEST_CODE);
                }
                else
                {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
                }
            }
        });

    }


    private void uploadVideoToServer(){
        progressBar.setVisibility(View.VISIBLE);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.216/totalrepair/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Api api = retrofit.create(Api.class);
        Call<ResponseBody> serverCom = api.uploadVideoToServer(part);
        serverCom.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this,"Upload Successfull",Toast.LENGTH_LONG).show();
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "Error message " + t.getMessage());
                Toast.makeText(MainActivity.this,t.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }
}