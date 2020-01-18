package com.demons.apkupdate;

import android.os.Bundle;

import com.demons.update.config.UpdateConfiguration;
import com.demons.update.listener.OnDownloadListener;
import com.demons.update.manager.DownloadManager;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements OnDownloadListener {
    public static final String APK_NAME = "healthcareunit.apk";//apk名称
    private String url = "http://chm.lpyy8686.com/phs/healthy/app/2_v1.0.0.2_(09_20_0907)_HealthCareUnit_Official_release.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDownload(url);
    }

    private void initDownload(String url) {
        UpdateConfiguration configuration = new UpdateConfiguration()
                //设置下载过程的监听
                .setOnDownloadListener(this);
        DownloadManager manager = DownloadManager.getInstance(MainActivity.this);
        manager.setApkName(APK_NAME)
                .setConfiguration(configuration)
                .setApkUrl(url)
                .setSmallIcon(R.mipmap.ic_launcher)
                .download();
    }

    @Override
    public void start() {

    }

    @Override
    public void downloading(int max, int progress) {

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
}
