package com.example.kaival.smartschool;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Checkable;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class OnlineList extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
DatabaseReference onlineref,currentuserref,counterref,accounttype,locations;
FirebaseRecyclerAdapter<User,ListOnlineViewHolder>adapter;
RecyclerView listonline;
RecyclerView.LayoutManager layoutManager;
String logintype;
//Location
    private static final int MY_PERMISSION_REQCODE=7171;
    private static final int PLAY_SERVICE_REQ=7172;
    private LocationRequest mlocationRequest;
    private GoogleApiClient mGoogleapiclient;
    private Location lastlocation;
    private static int UPDATE_INTERVAL=5000;
    private static int FASTEST_INTERVAL=3000;
    private static int DISTANCE=10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_list);
        listonline=(RecyclerView)findViewById(R.id.listonline);
        listonline.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        listonline.setLayoutManager(layoutManager);
        Bundle bundle=getIntent().getExtras();
        logintype=bundle.getString("button");
        System.out.println("LOGIN TYPE::::::::::::::::::"+logintype);


        locations=FirebaseDatabase.getInstance().getReference("Locations");

        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED
        )
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSION_REQCODE);

        }
        else
        {
            if(checkPlayServices())
            {
                buildGoogleapiclient();
                createlocationRequest();
                displayLocation();
            }
        }


        if(logintype.equals("driver")) {
            onlineref = FirebaseDatabase.getInstance().getReference().child(".info/connected");
            counterref = FirebaseDatabase.getInstance().getReference("lastonline");
            currentuserref = FirebaseDatabase.getInstance().getReference("lastonline").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            setupsys();
            updateList();
        }
        if(logintype.equals("parent"))
        {
            counterref = FirebaseDatabase.getInstance().getReference("lastonline");
            updateList();
        }

    }

    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED
        ){return;}
        lastlocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleapiclient);
        if(lastlocation!=null)
        {
            //update to firebase

            locations.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(new Tracking(FirebaseAuth.getInstance().getCurrentUser().getEmail()
                    ,FirebaseAuth.getInstance().getCurrentUser().getUid(),String.valueOf(lastlocation.getLatitude()),String.valueOf(lastlocation.getLongitude())));
            System.out.println("LATITUDE:"+lastlocation.getLatitude());
            System.out.println("LONGITUDE:"+lastlocation.getLongitude());

        }
        else{
        }
    }



    private void buildGoogleapiclient() {
        mGoogleapiclient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleapiclient.connect();
    }

    private boolean checkPlayServices() {
        int resultcode= GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultcode!= ConnectionResult.SUCCESS)
        {
            if(GooglePlayServicesUtil.isUserRecoverableError(resultcode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultcode,this,PLAY_SERVICE_REQ).show();
            }
            else{
                Toast.makeText(this, "This device isnt Supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }


    private void updateList() {
        adapter=new FirebaseRecyclerAdapter<User, ListOnlineViewHolder> (
            User.class,R.layout.user_cardlayout,ListOnlineViewHolder.class,counterref)
        {
            @Override
            protected void populateViewHolder(ListOnlineViewHolder viewHolder, final User model, int position) {
                if(model.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail()))
                {
                    viewHolder.txtlogintype.setText("Driver"+" (me)");
                    viewHolder.txtlogintype.setTextColor(Color.parseColor("#1ba506"));
                }
                else
                {
                    viewHolder.txtlogintype.setText("Driver");
                    viewHolder.txtlogintype.setTextColor(Color.parseColor("#ed774d"));
                }
                viewHolder.txtEmail.setText(model.getEmail());

                viewHolder.mview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!model.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail()))
                        {
                            System.out.println("INTENT OF MAP DETECTED::::::::::::::::::::::");
                            System.out.println("emailsent:"+model.getEmail());
                            Intent map=new Intent(OnlineList.this,MapActivity.class);
                            map.putExtra("email",model.getEmail());
                            map.putExtra("lat",lastlocation.getLatitude());
                            map.putExtra("lng",lastlocation.getLongitude());
                            startActivity(map);
                        }
                    }
                });
            }
        };
        adapter.notifyDataSetChanged();
        listonline.setAdapter(adapter);
    }

    private void setupsys() {
        onlineref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.getValue(Boolean.class))
            {
                currentuserref.onDisconnect().removeValue();
                counterref.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .setValue(new User(FirebaseAuth.getInstance().getCurrentUser().getEmail(),"" +
                                "Online"));
                adapter.notifyDataSetChanged();
            }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        counterref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren())
                {
                    User user=snapshot.getValue(User.class);
                    System.out.println(""+user.getEmail()+" is "+ user.getStatus());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.option_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.logout:
                if(currentuserref!=null)
                {
                currentuserref.removeValue();}
                FirebaseAuth mauth=FirebaseAuth.getInstance();
                mauth.signOut();
                Intent i=new Intent(OnlineList.this,MainActivity.class);
                startActivity(i);
                finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        AlertDialog.Builder ad =new AlertDialog.Builder(this).setTitle("Exit and Logout?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseAuth mauth=FirebaseAuth.getInstance();
                mauth.signOut();
                Intent i=new Intent(OnlineList.this,MainActivity.class);
                startActivity(i);
                finish();
            }
        }).setMessage("Are you Sure you want to Exit this room?").setNegativeButton("No",null).setIcon(R.drawable.warning);
        AlertDialog alertDialog=ad.create();
        alertDialog.show();
        return;
    }
    @SuppressLint("RestrictedApi")
    private void createlocationRequest() {
        mlocationRequest=new LocationRequest();
        mlocationRequest.setInterval(UPDATE_INTERVAL);
        mlocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mlocationRequest.setSmallestDisplacement(DISTANCE);
        mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED
        ){return;}
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleapiclient,mlocationRequest,this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleapiclient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lastlocation=location;
        displayLocation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleapiclient!=null)
        {
            mGoogleapiclient.connect();
        }
    }

    @Override
    protected void onStop() {
        if(mGoogleapiclient!=null)
        {
            mGoogleapiclient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        checkPlayServices();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode)
        {
            case MY_PERMISSION_REQCODE:
            {
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    if(checkPlayServices())
                    {
                        buildGoogleapiclient();
                        createlocationRequest();
                        displayLocation();
                    }
                }
            }
        }
    }
}
