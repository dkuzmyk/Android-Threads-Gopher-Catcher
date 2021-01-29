package com.dkuzmy3.project_4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    guessByGuess guessByGuess = new guessByGuess();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void startContinuous(View w){
        Intent intent = new Intent(getApplicationContext(), continuousActivity.class);
        startActivity(intent);
    }

    public void startGuessByGuess(View w){
        Intent intent1 = new Intent(getApplicationContext(), guessByGuess.class);
        startActivity(intent1);
    }
}