package com.subgen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Location currentLocation;
    private double lat, lon;
    private String poi, api_key = "AIzaSyBAApr46WIYU85Exk312QXSqaZqWOKVhcM";
    private String tag = "Testing MapsActivity";
    private List<Location> locationList;
    private ArrayList<String> names;
    private AlertDialog progressDialog;
    private TextView tv_message;
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        currentLocation = new Location("");
        currentLocation.setLatitude(getIntent().getDoubleExtra("lat",0));
        currentLocation.setLongitude(getIntent().getDoubleExtra("lon",0));

        progressDialog = new AlertDialog.Builder(this).create();
        inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progress_dialog, null);
        tv_message = dialogView.findViewById(R.id.tv_message);

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
        LatLng myLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(myLocation).title("My current location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15.0f));
    }

    public void findRestaurants(View v)
    {
        tv_message.setText("Searching restaurants nearby...");
        tv_message.setVisibility(View.VISIBLE);
        progressDialog.show();
        poi = "restaurant";
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        StringBuilder url = new StringBuilder();
        url.append("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=")
                .append(currentLocation.getLatitude())
                .append(",")
                .append(currentLocation.getLongitude())
                .append("&radius=1500&type=")
                .append(poi)
                .append("&key=")
                .append(api_key);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url.toString(), null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.wtf(tag, response.getString("status"));
                            if (response.getString("status").equalsIgnoreCase("OK"))
                            {
                                jsonParser(response.getJSONArray("results"));
                                addPOItoMap(mMap);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.wtf(tag, error);
                    }
                });
        queue.add(jsonObjectRequest);
    }

    public void jsonParser(JSONArray jsonArray) throws JSONException {
        locationList = new ArrayList<>();
        names = new ArrayList<>();

        Location location;
        Double lat, lon;
        for (int i = 0, size = jsonArray.length(); i < size; i++) {
            lat = jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
            lon = jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
            location = new Location("");
            location.setLatitude(lat);
            location.setLongitude(lon);
            locationList.add(location);
            names.add(jsonArray.getJSONObject(i).getString("name"));
            Log.wtf(tag, names.get(i));
        }
    }

    public void addPOItoMap(GoogleMap map)
    {
        for (int i = 0; i < locationList.size(); i++) {
            Location location = locationList.get(i);
            map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title(names.get(i)));
        }
        progressDialog.dismiss();
    }
}