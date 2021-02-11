package com.subgen;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


public class GuideActivity extends AppCompatActivity{

    final private int REQUEST_LOCATION_CODE = 123;
    final private int REQUEST_CHECK_SETTINGS = 111;
    private AlertDialog.Builder dialog;
    private String tag = "Test GuideActivity: ";
    private Location currentLocation;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Location mLastLocation;
    private FusedLocationProviderClient fusedLocationClient;
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    private static final int UPDATE_INTERVAL_IN_SECONDS = 60;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 60;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    private GoogleMap mMap;
    private AlertDialog  progressDialog;
    private TextView tv_message;
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        progressDialog = new AlertDialog.Builder(this).create();
        inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progress_dialog, null);
        tv_message = dialogView.findViewById(R.id.tv_message);
        progressDialog.setView(dialogView);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.wtf(tag, "No location results");
                    return;
                }
                //stop location updates
                stopLocationUpdates();
                for (Location location : locationResult.getLocations()) {
                    // assign the location to currentLocation
                    currentLocation = location;
                }//end foreach
            }//end onLocationResult
        };
        progressDialog.show();
        createLocationRequest();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*
        if change is made to the location settings,
        ask the user to grant location permission.
         */
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                checkPermission();
            }//end inner if
        }//end if
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //proceed with the functionality
                startLocationUpdates();
            }//end nested if
            else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(GuideActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    dialog = new AlertDialog.Builder(this);

                    dialog.setTitle("PLEASE NOTE")
                            .setMessage("This permission is important to be able to search and locate places of interest. Click 'OK' to allow.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    ActivityCompat.requestPermissions(GuideActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                            REQUEST_LOCATION_CODE);
                                }
                            })
                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(GuideActivity.this, "Cannot locate places", Toast.LENGTH_LONG).show();
                                }
                            });
                    //display the dialog
                    dialog.show();
                }//end nested if
            }//end else if
            else {
                Toast.makeText(GuideActivity.this, "Cannot locate places", Toast.LENGTH_LONG).show();
            }//end else
        }
    }

    public void myLocationBtn(View v) {

        //tv_message.setText("Searching your location...");
        //tv_message.setVisibility(View.VISIBLE);
        progressDialog.show();
        if (currentLocation != null)
        {
            Log.d(tag, String.valueOf(currentLocation.getLatitude()) + " " + String.valueOf(currentLocation.getLongitude()));
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("lat", currentLocation.getLatitude());
            intent.putExtra("lon", currentLocation.getLongitude());
            startActivity(intent);
            finish();
        }
         else return;
    }

    private void startLocationUpdates() {
        //request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;
        fusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null);
    }

    private void stopLocationUpdates() {

        //stop Location Updates
        fusedLocationClient.removeLocationUpdates(mLocationCallback);
        progressDialog.dismiss();


    }//end stopLocationUpdates function

    /*
   check if the location settings is enabled
    */
    protected void createLocationRequest()
    {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {

                //All location settings are satisfied.
                //Check for location permissions
                checkPermission();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                if (e instanceof ResolvableApiException)
                {
                    try
                    {
                        //Location settings are not satisfied but this can be fixed by showing the user a dialog
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(GuideActivity.this,REQUEST_CHECK_SETTINGS);
                    }//end try block
                    catch (IntentSender.SendIntentException sendEx)
                    {
                        //ignore error
                    }//end catch block
                }//end if
            }//end onFailure function
        });

    }//end createLocationRequest function
    public void checkPermission()
    {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(GuideActivity.this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_CODE);
        }
        else
        {
            //proceed with the functionality
            startLocationUpdates();
        }
    }//end checkPermission function

}