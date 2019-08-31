package com.example.kaival.smartschool;

import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String email;
    DatabaseReference location;
    Double lat,lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        location= FirebaseDatabase.getInstance().getReference("Locations");
        //get intent
        if(getIntent()!=null)
        {
            email=getIntent().getStringExtra("email");
            lat=getIntent().getDoubleExtra("lat",0);
            lng=getIntent().getDoubleExtra("lng",0);
        }

        if(!email.equals(""))
        {
            loadlocationforthisuser(email);
        }

        //ref to firebase first


    }

    private void loadlocationforthisuser(String email) {
        Query user_location=location.orderByChild("email").equalTo(email);
        user_location.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren())
                {
                    Tracking t= snapshot.getValue(Tracking.class);
                     //add marker for driver location

                    LatLng driverlocation =new LatLng(Double.parseDouble(t.getLat()),Double.parseDouble(t.getLng()));

                    //create location from parent coordinates
                    Location currentparent =new Location("");
                    currentparent.setLatitude(lat);
                    currentparent.setLongitude(lng);

                    //Create location from driver coordinates
                    Location driver =new Location("");
                    driver.setLatitude(Double.parseDouble(t.getLat()));
                    driver.setLongitude(Double.parseDouble(t.getLng()));

                    mMap.clear();

                    //add driver marker on map
                    mMap.addMarker(new MarkerOptions()
                            .position(driverlocation)
                            .snippet("Distance "+new DecimalFormat("#.#")
                                    .format(currentparent.distanceTo(driver)/1000)+"km").title(""+t.getEmail())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(t.getLat()),Double.parseDouble(t.getLng())),12.0f));

                    //add parent marker
                    LatLng current=new LatLng(lat,lng);
                    mMap.addMarker(new MarkerOptions()
                    .position(current).title(FirebaseAuth.getInstance().getCurrentUser().getEmail()+" (me)")
                    );
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private double distance(Location currentparent, Location driver) {
        double theta = currentparent.getLongitude() - driver.getLongitude();
        double dist=Math.sin(deg2rad(currentparent.getLatitude()))
                *Math.sin(deg2rad(driver.getLatitude()))
                *Math.cos(deg2rad(currentparent.getLatitude()))
                *Math.cos(deg2rad(driver.getLatitude()))
                *Math.cos(deg2rad(theta));
        dist=Math.acos(dist);
        dist=rad2deg(dist);
        dist=dist*60*1.1515;
        return (dist);
    }

    private double rad2deg(double rad) {
        return (rad*180/Math.PI);
    }

    private double deg2rad(double deg) {
        return (deg*Math.PI/180.0);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }
}
