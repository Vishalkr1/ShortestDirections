package com.example.one17task;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.one17task.DiretionHelper.FetchURL;
import com.example.one17task.DiretionHelper.TaskLoadedCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, TaskLoadedCallback, GoogleMap.OnMapLongClickListener, RoutingListener {

    private static final int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;
    private GoogleMap mMap;
    private Polyline currentPolyline;
    private ArrayList<LatLng> list = new ArrayList<>();
    private Button btn;
    private int i=0;
    private List<Polyline> polylines;
    protected LatLng start;
    protected LatLng end;
    private static final int[] COLORS = new int[]{R.color.primary_dark,R.color.primary,R.color.primary_light,R.color.accent,R.color.primary_dark_material_light};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        btn = findViewById(R.id.GetDirectionBtn);

        if(list.size()>=1) {
            for (int i = 0; i < list.size(); i++) {
                new FetchURL(MapsActivity.this).execute(getUrl(list.get(i), list.get(i+1)), "driving");
            }
        }
        if(list.size()==2)
        {
            new FetchURL(MapsActivity.this).execute(getUrl(list.get(0), list.get(1)), "driving");
            new FetchURL(MapsActivity.this).execute(getUrl(list.get(1), list.get(2)), "driving");
            new FetchURL(MapsActivity.this).execute(getUrl(list.get(0), list.get(2)), "driving");
        }
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

        // Add a marker in New Delhi and move the camera
        LatLng sydney = new LatLng(28.6139, 77.2090);
        //mMap.addMarker(new MarkerOptions().position(sydney).title(getCOmpleteAddress(sydney.latitude, sydney.longitude)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 14F));
        mMap.setOnMapLongClickListener(this);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (Build.VERSION.SDK_INT >= 29) {
            //We need background permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                handleMapLongClick(latLng);
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    //We show a dialog and ask for permission
                    ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                } else {
                    ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                }
            }

        } else {
            handleMapLongClick(latLng);

        }

    }

    private void handleMapLongClick(LatLng latLng) {
        if(list.size()<=2) {
            list.add(latLng);
            mMap.addMarker(new MarkerOptions().position(latLng).title(getCOmpleteAddress(latLng.latitude, latLng.longitude)));
            Toast.makeText(this, getCOmpleteAddress(latLng.latitude, latLng.longitude) + " selected", Toast.LENGTH_SHORT).show();
            new FetchURL(MapsActivity.this).execute(getUrl(list.get(0), list.get(i)), "driving");
            i++;
        }
        else{
            Toast.makeText(this, "You can't select more than 3 locations", Toast.LENGTH_SHORT).show();
        }

    }

    private String getCOmpleteAddress(double Latitude, double Longtitude) {

        String address = "";

        Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());

        try {

            List<Address> addresses = geocoder.getFromLocation(Latitude, Longtitude, 1);

            if (address != null) {

                Address returnAddress = addresses.get(0);
                StringBuilder stringBuilderReturnAddress = new StringBuilder("");

                for (int i = 0; i <= returnAddress.getMaxAddressLineIndex(); i++) {
                    stringBuilderReturnAddress.append(returnAddress.getAddressLine(i)).append("\n");
                }

                address = stringBuilderReturnAddress.toString();

            } else {
                Toast.makeText(this, "Address not found", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }


        return address;
    }

    private String getUrl(LatLng origin, LatLng destination)
    {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_destination = "destination=" + destination.latitude + "," + destination.longitude;
        String mode = "mode=" + "driving";
        String params = str_origin + "&" + str_destination + "&" + mode;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?"  + params + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }

    public void GetDirections(View view) {
        if(list.size()==3) {
            Routing routing1 = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(true)
                    .waypoints(list.get(0), list.get(1))
                    .build();
            routing1.execute();

            Routing routing2 = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(true)
                    .waypoints(list.get(2), list.get(1))
                    .build();
            routing2.execute();

            Routing routing3 = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(list.get(0), list.get(2))
                    .build();
            routing3.execute();
        }
        else{
            Toast.makeText(this, "Please select three locations first", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {

        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingCancelled() {

    }
}