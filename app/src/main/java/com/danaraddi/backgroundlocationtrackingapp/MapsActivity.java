package com.danaraddi.backgroundlocationtrackingapp;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, RoutingListener {

    private GoogleMap mMap;
    private Button startShiftButton, endShiftButton;
    private LatLng startLanLng, endLatLng;
    private ProgressDialog progressDialog;
    private ArrayList<Polyline> polylines;
    private CoordinatorLayout coordinatorLayout;
    private Snackbar snackbar;
    private ArrayList<LatLng> latLngs ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void init() {
        startShiftButton = (Button) findViewById(R.id.start_shift_button);
        startShiftButton.setOnClickListener(this);
        endShiftButton = (Button) findViewById(R.id.end_shift_button);
        endShiftButton.setOnClickListener(this);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.cordinator_layout);
        startShiftButton.setBackgroundColor(Color.RED);
        endShiftButton.setBackgroundColor(Color.RED);
        polylines = new ArrayList<>();
        latLngs = new ArrayList<>();


    }

    private void onStartShiftClicked() {
        clearMap();
        startShiftButton.setBackgroundColor(Color.GREEN);
        endShiftButton.setOnClickListener(this);
        endShiftButton.setBackgroundColor(Color.RED);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.addMarker(new MarkerOptions().position(latLng));
                startLanLng = latLng;
                latLngs.add(latLng);

            }
        });


    }

    private void onEndShiftClicked() {
        endShiftButton.setBackgroundColor(Color.GREEN);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                endShiftButton.setOnClickListener(null);
                mMap.setOnMapClickListener(null);
                endLatLng = latLng;
                latLngs.add(latLng);
                mMap.addMarker(new MarkerOptions().position(latLng));
                showDistanceBetweenShifts();
            }
        });

    }

    private void clearMap() {
        latLngs.clear();
        mMap.clear();
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
        }
    }

    private void showDistanceBetweenShifts() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Fetching route information.", true);
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(startLanLng, endLatLng)
                .build();
        routing.execute();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        init();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_shift_button:
                onStartShiftClicked();
                break;
            case R.id.end_shift_button:
                onEndShiftClicked();
                break;
        }
    }


    @Override
    public void onRoutingFailure(RouteException e) {
        progressDialog.hide();

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int i) {
        progressDialog.hide();


        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        if (route.isEmpty()) {
            Toast.makeText(getApplicationContext(), "No Routes Available", Toast.LENGTH_SHORT).show();
            clearMap();
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int j = 0; j < route.size(); j++) {
            PolylineOptions polyOptions = new PolylineOptions();

            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);
            System.out.println();
            Toast.makeText(getApplicationContext(), "Route " + (i + 1) + ": distance - " + route.get(i).getDistanceValue() + ": duration - " + route.get(i).getDurationValue(), Toast.LENGTH_LONG).show();


            snackbar = Snackbar.make(coordinatorLayout, "Total Shift Time : " + route.get(i).getDurationText(), Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }
    }

    @Override
    public void onRoutingCancelled() {
        progressDialog.hide();
    }
}
