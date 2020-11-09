package com.starmedia.leisure_support_java;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.starmedia.tinysdk.StarMedia;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button infoFlow = findViewById(R.id.btn_infoflow);
        Button gameCenter = findViewById(R.id.btn_gamecenter);

        infoFlow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, InfoFlowActivity.class));
            }
        });

        gameCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StarMedia.openStarGame(MainActivity.this, null, null);
            }
        });
    }
}