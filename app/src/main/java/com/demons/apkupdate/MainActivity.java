package com.demons.apkupdate;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.demons.update.config.UpdateConfiguration;
import com.demons.update.listener.OnDownloadListener;
import com.demons.update.manager.DownloadManager;

import java.io.File;


public class MainActivity extends AppCompatActivity implements OnDownloadListener {
    public static final String APK_NAME = "healthcareunit.apk";//apk名称
    private String url = "http://chm.lpyy8686.com/phs/healthy/app/2_v1.0.0.2_(09_20_0907)_HealthCareUnit_Official_release.apk";
    private DownloadManager manager;
    private TextView progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progress = findViewById(R.id.progress);
        initDownload(url);
    }

    private void initDownload(String url) {
        UpdateConfiguration configuration = new UpdateConfiguration()
                //设置下载过程的监听
                .setOnDownloadListener(this);
        manager = DownloadManager.getInstance(MainActivity.this)
                .setApkName(APK_NAME)
                .setConfiguration(configuration)
                .setApkUrl(url)
                .setSmallIcon(R.mipmap.ic_launcher);
    }

    @Override
    public void start() {

    }

    @Override
    public void downloading(int progressPercent) {
        progress.setText(progressPercent + "%");
    }

    @Override
    public void done(File apk) {

    }

    @Override
    public void cancel() {

    }

    @Override
    public void error(Exception e) {

    }

    public void start(View view) {
        manager.download();
    }

    public void cancel(View view) {
        manager.cancel();
    }

    public void pause(View view) {

    }
}
