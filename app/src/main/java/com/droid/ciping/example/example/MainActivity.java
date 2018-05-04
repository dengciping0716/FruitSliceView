package com.droid.ciping.example.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private FruitSliceView viewById;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewById = (FruitSliceView)findViewById(R.id.view);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        viewById.onTouch(ev);
        return super.dispatchTouchEvent(ev);
    }
}
