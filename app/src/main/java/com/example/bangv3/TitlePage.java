package com.example.bangv3;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;


public class TitlePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_title_page);

        //launches the main map activity after a 3 second delay
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {

                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(intent);
                TitlePage.this.finish();

            }
        }, 3000);

    }
}
