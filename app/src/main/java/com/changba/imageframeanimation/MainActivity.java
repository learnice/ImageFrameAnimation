package com.changba.imageframeanimation;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.changba.imageframeanimation.config.ActivityConfigAdapter;
import com.changba.imageframeanimation.config.MainActivityConfig;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("MainActivity");
        initView();
    }

    private void initView() {
        mRv = findViewById(R.id.rv);
        mRv.setAdapter(new ActivityConfigAdapter<>(MainActivityConfig.values()));
        mRv.setLayoutManager(new LinearLayoutManager(this));
    }
}