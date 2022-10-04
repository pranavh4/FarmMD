package com.example.farmmd;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;


public class picture_upload extends AppCompatActivity implements LocationListener {

    private static final int GALLERY_REQUEST_CODE = 1;
    private static final int CAMERA_REQUEST = 1888;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int LOCATION_PERMISSION_CODE = 101;
    private static final int WRITE_PERMISSION_CODE = 102;
    private String cameraFilePath;
    private String image;
    private String file_name;
    private double latitude = 12.908;
    private double longitude=77.565;
    Uri selected_image;
    Bitmap bitmap;
    Interpreter interpreter;
    private ProgressBar spinner;


    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        String MODEL_ASSETS_PATH = "nec.tflite";
        AssetFileDescriptor assetFileDescriptor = this.getApplicationContext().getAssets().openFd(MODEL_ASSETS_PATH) ;
        FileInputStream fileInputStream = new FileInputStream( assetFileDescriptor.getFileDescriptor() ) ;
        FileChannel fileChannel = fileInputStream.getChannel() ;
        long startoffset = assetFileDescriptor.getStartOffset() ;
        long declaredLength = assetFileDescriptor.getDeclaredLength() ;
        return fileChannel.map( FileChannel.MapMode.READ_ONLY , startoffset , declaredLength ) ;
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    private static Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {

        ExifInterface ei = new ExifInterface(selectedImage.getPath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Log.e("lat",latitude+"");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.e("Latitude","disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.e("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.e("Latitude","status");
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //This is the directory in which the file will be created. This is the default location of Camera photos
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for using again
        cameraFilePath = "file://" + image.getAbsolutePath();
        Log.e("custom","created image");
        return image;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_upload);

        try {
            interpreter = new Interpreter(loadModelFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        spinner = (ProgressBar)findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);
        ImageView img = findViewById(R.id.image);
        Button choose_file = findViewById(R.id.choose_file);
        Button take_pic = findViewById(R.id.take_pic);
        Button upload = findViewById(R.id.upload);
        img.setImageResource(R.drawable.empty_image);

        final LocationManager l = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        while(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET}, LOCATION_PERMISSION_CODE);
        }
        l.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, picture_upload.this);

        choose_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.setVisibility(View.GONE);
                if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_CODE);
                }
                else
                {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    String[] mimeTypes = {"image/jpeg", "image/png"};
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                    startActivityForResult(intent, GALLERY_REQUEST_CODE);
                }
            }
        });

        take_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.setVisibility(View.GONE);
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_CODE);
                }
                else if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                }
                else
                {
                    try {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(picture_upload.this, BuildConfig.APPLICATION_ID + ".provider", createImageFile()));
                        startActivityForResult(intent, CAMERA_REQUEST);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.setVisibility(View.VISIBLE);
                String url = "https://farmserver23.herokuapp.com/";
//               String url = "http://10.1.4.16:5000/";

                String [] classes = {"Apple Rust", "Apple Healthy", "Corn Rust", "Corn Healthy"};
                float[][][][] input = new float[1][224][224][3];
                int batchNum = 0;
                for (int x = 0; x < 224; x++) {
                    for (int y = 0; y < 224; y++) {
                        int pixel = bitmap.getPixel(x, y);
                        input[batchNum][x][y][0] = (Color.red(pixel)) / 255.0f;
                        input[batchNum][x][y][1] = (Color.green(pixel)) / 255.0f;
                        input[batchNum][x][y][2] = (Color.blue(pixel)) / 255.0f;
                    }
                }
                float[][] output = new float[1][4];
                interpreter.run(input,output);
                float max = 0;
                int index = 0;
                for(int i=0;i<4;i++){
                    if(output[0][i]>max){
                        max = output[0][i];
                        index = i;
                    }
                }
                final String pred_class = classes[index];
                if(isNetworkAvailable(picture_upload.this.getApplicationContext())){
                    JSONObject img = new JSONObject();
                    try {
                        Log.e("lat",String.valueOf(latitude));
                        Log.e("lng",String.valueOf(longitude));
//                   img.put("img", image);
//                   img.put("filename",filee);
                        img.put("latitude",latitude);
                        img.put("longitude",longitude);
                        img.put("pred_class",pred_class);
                    }
                    catch(Exception e){}
                    Log.e("net","net there");
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                            (Request.Method.POST, url, img, new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {
                                    //textView.setText("Response: " + response.toString());
                                    Log.e("succ",response.toString());
                                    Intent i = new Intent(picture_upload.this.getApplicationContext(),Result.class);
                                    try {
                                        i.putExtra("class", pred_class);
                                        i.putExtra("hum", response.getString("humidity"));
                                        i.putExtra("lat", response.getString("latitude"));
                                        i.putExtra("lang", response.getString("longitude"));
                                        i.putExtra("pres", response.getString("pressure"));
                                        i.putExtra("temp", response.getString("temp"));
                                    }
                                    catch (Exception e) {}
                                    spinner.setVisibility(View.GONE);
                                    startActivity(i);
                                }
                            }, new Response.ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    //textView.setText("Response: " + error.toString());
                                    Log.e("fai;",error.toString());
                                    Intent i = new Intent(picture_upload.this.getApplicationContext(),Result_No_Net.class);
                                    try{
                                        i.putExtra("class", pred_class);
                                    }
                                    catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    spinner.setVisibility(View.GONE);
                                    startActivity(i);
                                }
                            });

                    MySingleton.getInstance(picture_upload.this).addToRequestQueue(jsonObjectRequest);
                }
                else{
                    Intent i = new Intent(picture_upload.this.getApplicationContext(),Result_No_Net.class);
                    try{
                        i.putExtra("class", pred_class);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    spinner.setVisibility(View.GONE);
                    startActivity(i);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        ImageView img = findViewById(R.id.image);
        Log.e("custom","result returned");
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY_REQUEST_CODE) {
                //data.getData returns the content URI for the selected Image
                selected_image = data.getData();
//                img.setImageURI(selected_image);
                try {
                    bitmap = Bitmap.createScaledBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), selected_image),224,224,true);
                    img.setImageBitmap(bitmap);
//                    //InputStream inputStream = new FileInputStream(new File(selected_image.getPath()));//You can get an inputStream using any IO API
//                    InputStream inputStream = getContentResolver().openInputStream(selected_image);
//                    file_name = selected_image.getLastPathSegment();
//                    Log.e("file",file_name);
//                    byte[] bytes;
//                    byte[] buffer = new byte[8192];
//                    int bytesRead;
//                    ByteArrayOutputStream output = new ByteArrayOutputStream();
//                    try {
//                        while ((bytesRead = inputStream.read(buffer)) != -1) {
//                            output.write(buffer, 0, bytesRead);
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    bytes = output.toByteArray();
//                    image = Base64.encodeToString(bytes, Base64.DEFAULT);

                }
                catch (Exception e){Log.e("err",e.toString());}
            }
            else if(requestCode==CAMERA_REQUEST)
            {
                selected_image = Uri.parse(cameraFilePath);
//                img.setImageURI(selected_image);
                try {
                    bitmap = rotateImageIfRequired(Bitmap.createScaledBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), selected_image),224,224,true),selected_image);
                    img.setImageBitmap(bitmap);
                    //InputStream inputStream = new FileInputStream(image);
//                    InputStream inputStream = getContentResolver().openInputStream(selected_image);
//                    //Log.e("path",cameraFilePath);//You can get an inputStream using any IO API
//                    file_name = selected_image.getLastPathSegment();
//                    Log.e("file",file_name);
//                    byte[] bytes;
//                    byte[] buffer = new byte[8192*2];
//                    int bytesRead;
//                    ByteArrayOutputStream output = new ByteArrayOutputStream();
//                    try {
//                        while ((bytesRead = inputStream.read(buffer)) != -1) {
//                            output.write(buffer, 0, bytesRead);
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    bytes = output.toByteArray();
//                    image = Base64.encodeToString(bytes, Base64.DEFAULT);
                }
                catch (Exception e){Log.e("err",e.toString());}
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}
