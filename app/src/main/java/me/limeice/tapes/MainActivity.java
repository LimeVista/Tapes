package me.limeice.tapes;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import me.limeice.tapesdb.Tapes;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Tapes.init(this);
    }
}
