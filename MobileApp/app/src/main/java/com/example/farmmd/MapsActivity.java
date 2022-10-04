package com.example.farmmd;

import androidx.fragment.app.FragmentActivity;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;

    private double latitude = 12.981752;
    private double longitude=77.645696;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(latitude, longitude);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.addCircle(new CircleOptions()
                .center(new LatLng(12.983264, 77.646465))
                .radius(100)
                .strokeColor(Color.rgb(195,29,29))
                .fillColor(R.color.circleColor));
        mMap.addCircle(new CircleOptions()
                .center(new LatLng(12.982107, 77.6450028))
                .radius(50)
                .strokeColor(Color.rgb(195,29,29))
                .fillColor(R.color.circleColor));

        mMap.addCircle(new CircleOptions()
                .center(new LatLng(12.980273, 77.645999))
                .radius(50)
                .strokeColor(Color.rgb(195,29,29))
                .fillColor(R.color.circleColor));
        mMap.addMarker(new MarkerOptions().position(sydney).title("You are Here"));
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(sydney,17,0,0)));
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
}
