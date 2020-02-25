package com.demons.update.manager;

import android.support.annotation.NonNull;

import com.demons.update.base.BaseHttpDownloadManager;
import com.demons.update.listener.OnDownloadListener;
import com.demons.update.utils.Constant;
import com.demons.update.utils.FileUtil;
import com.demons.update.utils.LogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * 库中默认的下载管理
 */
public class HttpDownloadManager extends BaseHttpDownloadManager {

    private static final String TAG = Constant.TAG + "HttpDownloadManager";
    private boolean shutdown = false;
    private String apkUrl, apkName, downloadPath;
    private OnDownloadListener listener;

    public HttpDownloadManager(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    @Override
    public void download(String apkUrl, String apkName, OnDownloadListener listener) {
        this.apkUrl = apkUrl;
        this.apkName = apkName;
        this.listener = listener;
        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
            @Override
            public Thread newThread(@NonNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName(Constant.THREAD_NAME);
                return thread;
            }
        });
        executor.execute(runnable);
    }

    @Override
    public void cancel() {
        shutdown = true;
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (FileUtil.fileExists(downloadPath, apkName)) {
                FileUtil.delete(downloadPath, apkName);
            }
            fullDownload();
        }
    };

    /**
     * 全部下载
     */
    private void fullDownload() {
        long downloadedLength = 0;
        File file = new File(downloadPath, apkName);
        //判断是否已经存在要下载的文件，如果存在的话则读取已经下载的字节数，实现断点续传的功能
        if (file.exists()) {
            downloadedLength = file.length();
        }
        //获取待下载文件的总长度
        long contentLength = getContentLength(apkUrl);
        if (contentLength == 0) {
            //若文件长度等于0，说明文件有问题
            listener.error(new Exception("下载失败：文件有问题!"));
            return;
        } else if (contentLength == downloadedLength) {
            //已下载的字节和文件总字节相等，说明已经下载完成了
            listener.done(file);
            return;
        }
        listener.start();
        final long startTime = System.currentTimeMillis();
        LogUtil.i(TAG, "startTime=" + startTime);
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(apkUrl)
                .addHeader("Connection", "close")
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                LogUtil.i(TAG, "download failed");
                listener.error(e);
                return;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                // 储存下载文件的目录
                File file = FileUtil.createFile(downloadPath, apkName);
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1 && !shutdown) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        LogUtil.e(TAG, "download progress : " + progress);
                        listener.downloading(progress);
                    }
                    if (shutdown) {
                        //取消了下载 同时再恢复状态
                        shutdown = false;
                        LogUtil.d(TAG, "fullDownload: 取消了下载");
                        listener.cancel();
                    } else {
                        listener.done(file);
                    }
                    fos.flush();
                    LogUtil.e(TAG, "download success");
                    LogUtil.e(TAG, "totalTime=" + (System.currentTimeMillis() - startTime));
                } catch (Exception e) {
                    e.printStackTrace();
                    listener.error(e);
                    LogUtil.e(TAG, "download failed : " + e.getMessage());
                    return;
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 获取下载文件大小
     *
     * @param downloadUrl
     * @return
     * @throws Exception
     */
    protected long getContentLength(String downloadUrl) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(downloadUrl)
                    .build();
            Response response = client.newCall(request).execute();
            if ((response != null) && response.isSuccessful()) {
                long contentLength = response.body().contentLength();
                response.close();
                return contentLength;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
