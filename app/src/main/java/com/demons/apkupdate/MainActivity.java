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
    public static final String APK_NAME = "ApkUpdate.apk";//apk名称
//    private String url = "http://chm.lpyy8686.com/phs/healthy/app/2_v1.0.0.2_(09_20_0907)_HealthCareUnit_Official_release.apk";
    private String url ="http://oss.pgyer.com/7c36a36f24feb2917baa9f126f6dc1a6.apk?auth_key=1582731737-f88eef1ac1d487d9bffa5dff848770db-0-0f3b81898519a8e9677e5f370032595f&response-content-disposition=attachment%3B+filename%3Dapp-debug.apk";
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
                .setApkMD5("A9ECF2A7A37B7E20888EC6CE5C2AE369")
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
