package es.guiguegon.sineview.sample;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import butterknife.BindView;
import butterknife.ButterKnife;
import es.guiguegon.sineview.SineView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.sine_view_1)
    SineView sineView1;
    @BindView(R.id.sine_view_2)
    SineView sineView2;
    @BindView(R.id.sine_view_3)
    SineView sineView3;
    @BindView(R.id.sine_view_4)
    SineView sineView4;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        fab.setOnClickListener(this);
        sineView1.setOnClickListener(this);
        sineView2.setOnClickListener(this);
        sineView3.setOnClickListener(this);
        sineView4.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sine_view_1:
                if (sineView1.isAnimating()) {
                    sineView1.pauseWave();
                } else {
                    sineView1.resumeWave();
                }
                break;
            case R.id.sine_view_2:
                if (sineView2.isAnimating()) {
                    sineView2.pauseWave();
                } else {
                    sineView2.resumeWave();
                }
                break;
            case R.id.sine_view_3:
                if (sineView3.isAnimating()) {
                    sineView3.pauseWave();
                } else {
                    sineView3.resumeWave();
                }
                break;
            case R.id.sine_view_4:
                if (sineView4.isAnimating()) {
                    sineView4.pauseWave();
                } else {
                    sineView4.resumeWave();
                }
                break;
            case R.id.fab:
                if (sineView1.isAnimating()) {
                    sineView1.stopWave();
                    sineView2.stopWave();
                    sineView3.stopWave();
                    sineView4.stopWave();
                    fab.setImageDrawable(ContextCompat.getDrawable(this, android.R.drawable.ic_media_play));
                } else {
                    sineView1.startWave();
                    sineView2.startWave();
                    sineView3.startWave();
                    sineView4.startWave();
                    fab.setImageDrawable(ContextCompat.getDrawable(this, android.R.drawable.ic_media_pause));
                }
                break;
        }
    }
}
