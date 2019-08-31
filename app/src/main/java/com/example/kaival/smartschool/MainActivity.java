package com.example.kaival.smartschool;

import android.content.ComponentName;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
Button psignin,dsignin;
private final int LOGIN=1000;
boolean driverbtnpressed=false;
boolean parentbtnpressed=false;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        psignin=findViewById(R.id.parentbtn);
        dsignin=findViewById(R.id.driverbtn);
        auth=FirebaseAuth.getInstance();
        startService(new Intent(this,Myservice.class));
        Toast.makeText(this,"CODED BY KAIVAL PATEL",Toast.LENGTH_LONG).show();
        psignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        AuthUI.getInstance().createSignInIntentBuilder().setAllowNewEmailAccounts(true).setTheme(R.style.LoginTheme)
                                .setLogo(R.drawable.applogo).setIsSmartLockEnabled(true).build(),LOGIN);
                parentbtnpressed=true;
                driverbtnpressed=false;



            }
        });
        dsignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        AuthUI.getInstance().createSignInIntentBuilder().setAllowNewEmailAccounts(true).setTheme(R.style.LoginTheme)
                                .setLogo(R.drawable.applogo).setIsSmartLockEnabled(true).build(),LOGIN);
                parentbtnpressed=false;
                driverbtnpressed=true;


            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==LOGIN)
        {
            startnewActivity(resultCode,data);
        }
    }

    private void startnewActivity(int resultCode, Intent data) {
        if(resultCode==RESULT_OK && parentbtnpressed==true)
        {
            Intent i=new Intent(MainActivity.this,OnlineList.class);
            i.putExtra("button","parent");
            startActivity(i);
            finish();
        }
        if(resultCode==RESULT_OK && driverbtnpressed==true)
        {
            Intent i=new Intent(MainActivity.this,OnlineList.class);
            i.putExtra("button","driver");
            startActivity(i);
            finish();
        }

    }

}
