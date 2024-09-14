package com.jw.media.jvideoplayer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.jw.media.jvideoplayer.R;

/**
 * Created by Joyce.wang on 2024/9/12 15:04
 *
 * @Description TODO
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button openPlayPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }
    private void initView() {
        openPlayPage = findViewById(R.id.open_play_page);

        openPlayPage.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.open_play_page) {
            startActivity(new Intent(this, SimplePlayActivity.class));
        }
    }
}
